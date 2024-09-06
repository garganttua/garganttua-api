package com.garganttua.api.spec.engine;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.spec.security.IGGAPIAuthenticationManager;
import com.garganttua.api.spec.security.IGGAPIAuthorizationManager;
import com.garganttua.api.spec.security.IGGAPISecurityBuilder;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.properties.IGGPropertyLoader;

public interface IGGAPIBuilder {

	IGGAPIBuilder setAuthorizationManager(IGGAPIAuthorizationManager manager);
	
	IGGAPIBuilder setAuthenticationManager(IGGAPIAuthenticationManager manager);

	IGGAPIBuilder setBeanLoader(IGGBeanLoader loadder);
	
	IGGAPIBuilder setPackages(List<String> packages);

	IGGAPIEngine build();

	IGGAPIBuilder setPropertyLoader(IGGPropertyLoader loader);
	
	IGGAPIBuilder superTenantId(String superTenantId);
	
	IGGAPIBuilder superOwnerId(String superOwnerId);

	IGGAPIBuilder setSecurityBuilder(Optional<IGGAPISecurityBuilder> securityBuilder);

}
