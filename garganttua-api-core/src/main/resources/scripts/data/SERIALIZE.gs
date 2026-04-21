#!/usr/bin/env gs

#@workflow
#  Stage 9 (data, response side): serializes the operation output to raw bytes
#  using content negotiation over the Accept header. Runs at the end of the
#  pipeline, after the business stage.
#
#  The stage is gated externally on the presence of the Accept header (Mode A).
#
#  @in  operationRequest: [0] IOperationRequest
#  @in  apiContext: [1] IApi
#  @in  previousOutput: [2] Object
#  @out output -> output: byte[]
#  @return 0: SUCCESS
#  @return 406: no acceptable serializer
#  @return 500: serialization failure
#@end

accept <- :arg(@0, "accept")

serializer <- negotiateSerializer(@1, @accept)
! -> 406

rawBody <- serialize(@serializer, @2)
! -> 500

output <- @rawBody -> 0
