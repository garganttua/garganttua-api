# Bug Report: `#@workflow` headers not automatically parsed by the workflow engine

## Summary

`ScriptHeaderParser` exists and correctly parses `#@workflow` headers from `.gs` files, but it is never invoked during workflow building or script generation. Scripts loaded from files require manual `.input()` / `.output()` declarations on the `WorkflowScriptBuilder`, even though the metadata is already declared in the `#@workflow` header of the `.gs` file.

The expected behavior is that when a script is added to a stage via `.script(path)` (file mode, not inline), the workflow engine should automatically parse the `#@workflow` header and configure the input/output bindings accordingly.

## Impact

Without automatic header parsing, consumers of the workflow API are forced to either:
1. **Duplicate declarations** — declare `.input()` / `.output()` on the builder AND `@in` / `@out` in the script header (the header becomes dead documentation)
2. **Use inline wrappers** — write dispatch scripts that manually call `include()` + `run_script()` + `script_output()` to wire everything up, bypassing the builder's input/output mechanism entirely

This is exactly what `garganttua-api` does today in `DomainBuilder.java`:

```java
// Current workaround: inline wrapper script that manually wires everything
String dispatchScript =
    "requirePresent(if(equals(@_code, 405), true))\n"
  + "! -> 0\n"
  + "_ref <- include(\"classpath:" + scriptPath + "\")\n"
  + "_code <- run_script(@_ref, @0, @1, @2)\n"
  + "output <- if(equals(@_code, 0), script_output(@_ref), 0)\n";
mergedBuilder.stage(label)
    .script(dispatchScript)
        .inline()                              // ← forced to inline
        .up()
    .up();
```

Instead of the intended declarative approach:

```java
// Expected: just point to the script, the engine handles @in/@out
mergedBuilder.stage(label)
    .when("equals(businessOperation(@0), \"" + label + "\")")
    .script("classpath:scripts/business/CREATE_ONE.gs")
        .name(label)
        .up()
    .up();
```

This also blocks inter-stage data flow. For example, the `AUTHENTICATE.gs` script produces an `IAuthentication` result that `CREATE_AUTHORIZATION.gs` needs as input. With automatic header parsing, this would be declarative:

**AUTHENTICATE.gs:**
```
#@workflow
#  @in  operationRequest: [0] IOperationRequest
#  @in  repository: [1] IRepository
#  @in  domainContext: [2] IDomainContext
#  @out authResult -> output: IAuthentication
#  @return 0: SUCCESS
#  @return 401: UNAUTHORIZED
#@end
```

**CREATE_AUTHORIZATION.gs:**
```
#@workflow
#  @in  operationRequest: [0] IOperationRequest
#  @in  repository: [1] IRepository
#  @in  domainContext: [2] IDomainContext
#  @in  authResult: [3] IAuthentication
#  @out authorization -> output: Object
#  @return 0: SUCCESS
#@end
```

The workflow engine would then automatically wire `authResult` from the output of the authenticate stage to `@3` of the create-authorization stage.

## Root Cause

In `ScriptGenerator.appendScript()` (line 132), when a file-based script is loaded:

```java
boolean shouldInline = inlineAll || ws.isInline() || !ws.isFile();
if (ws.isFile() && !shouldInline) {
    // File -> include() + execute_script() + script_variable() pattern
    String refVarName = "_" + stageName + "_" + scriptName + "_ref";
    script.append(refVarName).append(" <- ");
    script.append("include(\"").append(escapeString(ws.getPath())).append("\")\n");
    // ...uses ws.getInputs() and ws.getOutputs() which are EMPTY unless manually set
}
```

The generator uses `ws.getInputs()` and `ws.getOutputs()` to generate `execute_script()` arguments and `script_variable()` output mappings. These maps come from `WorkflowScriptBuilder.inputs` and `WorkflowScriptBuilder.outputs`, which are only populated by explicit `.input()` and `.output()` calls. **`ScriptHeaderParser` is never called.**

Similarly, for inline scripts (line 242-246):
```java
String content = ws.loadContent();
content = replacePositionalVariables(content, ws.getInputs());
```

The content is loaded but the header is not parsed — `ws.getInputs()` is empty, so `replacePositionalVariables` is a no-op.

## Affected Classes

| Class | File | Issue |
|-------|------|-------|
| `WorkflowScriptBuilder` | `dsl/WorkflowScriptBuilder.java` | `build()` does not call `ScriptHeaderParser.parse()` on the source content |
| `ScriptGenerator` | `generator/ScriptGenerator.java` | `appendScript()` does not parse headers from loaded content |
| `WorkflowScript` | `WorkflowScript.java` | `loadContent()` returns raw content without stripping headers |

## Classes That Work Correctly (no changes needed)

| Class | File | Status |
|-------|------|--------|
| `ScriptHeaderParser` | `header/ScriptHeaderParser.java` | Parsing logic is correct and tested |
| `ScriptHeader` | `header/ScriptHeader.java` | Data model is complete (`HeaderInput`, `HeaderOutput`, return codes, catch expressions) |

## Proposed Fix

In `WorkflowScriptBuilder.build()`, after constructing the `WorkflowScript`, parse the header and merge metadata:

```java
@Override
public WorkflowScript build() throws DslException {
    WorkflowScript ws = WorkflowScript.builder()
            .name(name)
            .description(description)
            .source(source)
            .inline(inline)
            .condition(condition)
            .catchExpression(catchExpression)
            .catchDownstreamExpression(catchDownstreamExpression)
            .inputs(new LinkedHashMap<>(inputs))
            .outputs(new HashMap<>(outputs))
            .codeActions(new HashMap<>(codeActions))
            .build();

    // Auto-parse header from script content
    String content = ws.loadContent();
    if (content != null) {
        ScriptHeaderParser parser = new ScriptHeaderParser();
        parser.parse(content).ifPresent(header -> {
            // Merge header inputs (header first, explicit .input() overrides)
            for (HeaderInput hi : header.inputs()) {
                String key = hi.name();
                if (!ws.getInputs().containsKey(key)) {
                    ws.getInputs().put(key, "@" + hi.effectivePosition(0));
                }
            }
            // Merge header outputs
            for (HeaderOutput ho : header.outputs()) {
                if (!ws.getOutputs().containsKey(ho.name())) {
                    ws.getOutputs().put(ho.name(), ho.variable());
                }
            }
            // Merge catch expressions
            if (ws.getCatchExpression() == null && header.hasCatch()) {
                ws.setCatchExpression(header.catchExpression());
            }
            if (ws.getCatchDownstreamExpression() == null && header.hasCatchDownstream()) {
                ws.setCatchDownstreamExpression(header.catchDownstreamExpression());
            }
            // Set description if not explicitly set
            if (ws.getDescription() == null && header.hasDescription()) {
                ws.setDescription(header.description());
            }
        });
    }

    return ws;
}
```

Additionally, `WorkflowScript.loadContent()` should strip the header before returning content for execution, or `ScriptGenerator` should call `ScriptHeaderParser.stripHeader()` before emitting script content.

## Test Gap

The existing test `WorkflowScriptsTest` tests header parsing in isolation but has no test that verifies end-to-end behavior: loading a `.gs` file with a `#@workflow` header via the builder and verifying that inputs/outputs are automatically configured.

## Environment

- **garganttua-workflow version:** 2.0.0-ALPHA01
- **garganttua-script version:** 2.0.0-ALPHA01
- **Discovered in:** garganttua-api 3.0.0-ALPHA01, `DomainBuilder` workflow construction
