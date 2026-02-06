# FORMAT_JSON.gs - Response formatting phase
# This script handles any final response formatting

# Get the response from context
response <- :getResponse()

# If response is already set, nothing to do
| :isNotNull(response) => -> 0

# Get result from context
result <- :getContextValue("result")

# If there's a result but no response, create one
| :isNotNull(result) => {
    :successResponse(result)
    -> 0
}

# No result and no response - this might be an error
# But we don't abort here, let the caller handle it
-> 0
