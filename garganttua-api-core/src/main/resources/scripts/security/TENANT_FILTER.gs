# TENANT_FILTER.gs - Apply tenant and owner filters
# This script applies security filters based on caller's tenant and owner

# Get caller from context
caller <- :getCaller()

# If anonymous or super tenant/owner, no filtering needed for certain operations
| :isNull(caller) => -> 0
| caller.superTenant() & caller.superOwner() => -> 0

# Get the filter from context if already set
existingFilter <- :getContextValue("filter")

# Apply security filter (tenant and owner restrictions)
securedFilter <- :applySecurityFilter(existingFilter, caller)

# Store the secured filter back in context
:setContextValue("filter", securedFilter)

# Success
-> 0
