#!/usr/bin/env gs

#@workflow
#  Stage 4 (data, request side): deserializes rawBody into the operation's DTO
#  using the Content-Type header. The result is written back into the request
#  args ("body" and "entity") so downstream CRUD stages consume it normally.
#
#  The stage is gated externally on the presence of rawBody (Mode A only).
#  This script also short-circuits when the operation does not expect a body
#  (read/delete) — the stage is a no-op in that case.
#
#  @in  operationRequest: [0] IOperationRequest
#  @in  apiContext: [1] IApi
#  @out output -> output: Object
#  @return 0: SUCCESS (body deserialized, or skipped when no body expected)
#  @return 400: malformed body
#  @return 415: unsupported Content-Type
#  @return 500: internal error resolving body type
#@end

operation <- :arg(@0, "operation")

// Skip if the operation does not expect a body (read/delete)
expectsBody <- operationExpectsBody(@operation)
requirePresent(if(@expectsBody, 1))
! -> 0

rawBody <- :arg(@0, "rawBody")
contentType <- :arg(@0, "contentType")

serializer <- resolveSerializer(@1, @contentType)
! => recordCaughtException(@0, @exception) -> 415

targetType <- resolveBodyType(@operation, @1)
! => recordCaughtException(@0, @exception) -> 500

body <- deserialize(@serializer, @rawBody, @targetType)
! => recordCaughtException(@0, @exception) -> 400

// Write deserialized body back into request for downstream CRUD stages
setRequestArg(@0, "body", @body)
setRequestArg(@0, "entity", @body)

output <- @body -> 0
