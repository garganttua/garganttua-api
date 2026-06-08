#!/usr/bin/env gs

#@workflow
#  Stage 10 (protocol, response side): converts the pipeline's final output
#  into a transport-native response via the same IProtocol that extracted the
#  request. The payload is either a byte[] (produced by the serialize stage) or
#  a raw Object (when serialize was skipped because no Accept header).
#
#  The stage is gated externally on the presence of rawRequest (Mode A only).
#
#  @in  operationRequest: [0] IOperationRequest
#  @in  apiContext:       [1] IApi
#  @in  previousOutput:   [2] Object (byte[] or raw)
#  @out output -> output: Object (transport response)
#  @return 0: SUCCESS
#  @return 500: buildResponse failure, or no protocol for rawRequest class
#@end

rawRequest  <- :arg(@0, "rawRequest")
status      <- :arg(@0, "exitCode")
contentType <- :arg(@0, "responseContentType")

protocol <- resolveProtocol(@1, @rawRequest)
! => recordCaughtException(@0, @exception) -> 500

response <- buildProtocolResponse(@protocol, @rawRequest, @2, @status, @contentType)
! => recordCaughtException(@0, @exception) -> 500

output <- @response -> 0
