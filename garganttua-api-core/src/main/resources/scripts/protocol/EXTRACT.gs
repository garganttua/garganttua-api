#!/usr/bin/env gs

#@workflow
#  Stage 1 (protocol, request side): resolves the IProtocol whose requestType
#  matches the class of `rawRequest` and delegates extraction of every transport
#  field (body, auth, content-type, accept, path, method, query params, caller)
#  into the operation request's arg map. Downstream stages read these args
#  transparently, just like in Mode B.
#
#  The stage is gated externally on the presence of rawRequest (Mode A only).
#
#  @in  operationRequest: [0] IOperationRequest
#  @in  apiContext:       [1] IApi
#  @out output -> output: ICaller
#  @return 0: SUCCESS
#  @return 400: extraction failure (getXxx threw)
#  @return 415: no protocol registered for the rawRequest's class
#@end

rawRequest  <- :arg(@0, "rawRequest")

protocol    <- resolveProtocol(@1, @rawRequest)
! => recordCaughtException(@0, @exception) -> 415

rawBody     <- extractRawBody(@protocol, @rawRequest)
! => recordCaughtException(@0, @exception) -> 400

contentType <- extractContentType(@protocol, @rawRequest)
! => recordCaughtException(@0, @exception) -> 400

accept      <- extractAccept(@protocol, @rawRequest)
! => recordCaughtException(@0, @exception) -> 400

path        <- extractPath(@protocol, @rawRequest)
! => recordCaughtException(@0, @exception) -> 400

method      <- extractMethod(@protocol, @rawRequest)
! => recordCaughtException(@0, @exception) -> 400

auth        <- extractAuthorization(@protocol, @rawRequest)
! => recordCaughtException(@0, @exception) -> 400

params      <- extractQueryParameters(@protocol, @rawRequest)
! => recordCaughtException(@0, @exception) -> 400

caller      <- extractCaller(@protocol, @rawRequest)
! => recordCaughtException(@0, @exception) -> 400

// Write extracted fields into the operation request for downstream stages
setRequestArg(@0, "rawBody", @rawBody)
setRequestArg(@0, "contentType", @contentType)
setRequestArg(@0, "accept", @accept)
setRequestArg(@0, "path", @path)
setRequestArg(@0, "method", @method)
setRequestArg(@0, "rawAuthorization", @auth)
setRequestArg(@0, "queryParameters", @params)
setCallerArgs(@0, @caller)

output <- @caller -> 0
