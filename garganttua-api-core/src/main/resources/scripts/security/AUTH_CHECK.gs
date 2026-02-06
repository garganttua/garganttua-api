# AUTH_CHECK.gs - Authentication and Authorization check
# This script validates the caller and sets up security context

# Get the current context
ctx <- :getContext()

# Get request information
request <- :getRequest()

# Check if this is an anonymous access
| :isNull(request) => {
    # No request, create anonymous caller
    caller <- :createAnonymousCaller()
    :setCaller(caller)
    -> 0
}

# Check if request allows anonymous access
| request.anonymous() => {
    # Anonymous access allowed, create anonymous caller
    caller <- :createAnonymousCaller()
    :setCaller(caller)
    -> 0
}

# For authenticated requests, caller should already be set by protocol phase
caller <- :getCaller()

# If no caller set, this is an error for non-anonymous requests
| :isNull(caller) => {
    :abort(:unauthorizedResponse("Authentication required"))
    -> 401
}

# Validate caller has basic access
| !:validateAccess(caller) => {
    :abort(:forbiddenResponse("Access denied"))
    -> 403
}

# Success
-> 0
