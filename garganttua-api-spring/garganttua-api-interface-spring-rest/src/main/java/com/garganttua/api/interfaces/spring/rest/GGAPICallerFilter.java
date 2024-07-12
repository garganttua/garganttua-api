package com.garganttua.api.interfaces.spring.rest;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.service.GGAPIServiceResponse;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.factory.GGAPIEntityIdentifier;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.service.GGAPIServiceAccess;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("callerFilter")
public class GGAPICallerFilter implements Filter {

	public static final String CALLER_ATTRIBUTE_NAME = "caller";

	@Value(value = "${com.garganttua.api.interface.spring.rest.ownerIdHeaderName:tenantId}")
	private String tenantIdHeaderName = "tenantId";

	@Value(value = "${com.garganttua.api.interface.spring.rest.requestedTenantIdHeaderName:requestedTenantId}")
	private String requestedTenantIdHeaderName = "requestedTenantId";

	@Value(value = "${com.garganttua.api.interface.spring.rest.ownerIdHeaderName:ownerId}")
	private String ownerIdHeaderName = "ownerId";

	@Value(value = "${com.garganttua.api.interface.spring.rest.superOnwerId:0}")
	private String superOwnerId = "0";

	@Value(value = "${com.garganttua.api.interface.spring.rest.superTenantId:0}")
	private String superTenantId = "0";

	@Autowired
	private IGGAPIEngine engine;

	private IGGAPIEntityFactory<?> tenantsFactory;

	private Optional<IGGAPIDomain> tenantsDomain;

	private Optional<IGGAPIDomain> ownersDomain;

	private IGGAPIEntityFactory<?> ownersFactory;

	@PostConstruct
	private void init() {
		this.tenantsDomain = Optional.ofNullable(this.engine.getTenantDomain());
		if (this.tenantsDomain.isPresent()) {
			this.tenantsFactory = this.engine.getFactoriesRegistry()
					.getFactory(this.tenantsDomain.get().getEntity().getValue1().domain());
		}
		this.ownersDomain = Optional.ofNullable(this.engine.getOwnerDomain());
		if (this.ownersDomain.isPresent()) {
			this.ownersFactory = this.engine.getFactoriesRegistry()
					.getFactory(this.ownersDomain.get().getEntity().getValue1().domain());
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		GGAPICaller caller = new GGAPICaller();

		String tenantId = ((HttpServletRequest) request).getHeader(this.tenantIdHeaderName);
		String requestedtenantId = ((HttpServletRequest) request).getHeader(this.requestedTenantIdHeaderName);
		String ownerId = ((HttpServletRequest) request).getHeader(this.ownerIdHeaderName);

		this.doDomainFilter(caller, (HttpServletRequest) request);
		this.doAccessRuleFilter(caller, (HttpServletRequest) request);

		try {
			if (!this.doTenantIdFiltering(caller, tenantId, requestedtenantId, (HttpServletRequest) request,
					(HttpServletResponse) response))
				return;
			if (!this.doOwnerIdFiltering(caller, ownerId, requestedtenantId, (HttpServletRequest) request,
					(HttpServletResponse) response))
				return;

			request.setAttribute(CALLER_ATTRIBUTE_NAME, caller);
			chain.doFilter(request, response);
		} catch (IOException | ServletException e) {
			if( log.isDebugEnabled() ) {
				log.warn("Error : ", e);
			}
			throw e;
//			GGAPIServiceResponse responseObject = new GGAPIServiceResponse(e.getMessage(),
//					GGAPIServiceResponseCode.SERVER_ERROR);
//			ResponseEntity<?> responseEntity = GGAPIServiceResponseUtils.toResponseEntity(responseObject);
//
//			String json = new ObjectMapper().writeValueAsString(responseEntity.getBody());
//
//			((HttpServletResponse) response).setStatus(responseEntity.getStatusCode().value());
//			response.setContentType("application/json");
//			((HttpServletResponse) response).getWriter().write(json);
//			((HttpServletResponse) response).getWriter().flush();
		}
	}

	private void doDomainFilter(GGAPICaller caller, HttpServletRequest request) {
		IGGAPIDomain ddomain = this.engine.getDomainsRegistry().getDomain(this.getDomainNameFromRequestUri(request));
		caller.setDomain(ddomain);
	}

	private String getDomainNameFromRequestUri(HttpServletRequest request) {
		String uri = request.getRequestURI();

		String[] uriParts = uri.split("/");
		return uriParts[1];
	}

	private boolean doOwnerIdFiltering(GGAPICaller caller, String ownerId, String requestedtenantId, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if( caller.getOwnerId() == null ) {
			IGGAPIAccessRule accessRule = caller.getAccessRule();
			IGGAPIDomain domain = this.getDomain((HttpServletRequest) request);
			HttpMethod method = this.getHttpMethod(request);
			
			if( (accessRule != null && accessRule.getAccess() == GGAPIServiceAccess.owner) || (domain != null && domain.getEntity().getValue1().ownedEntity() && (method == HttpMethod.POST || method == HttpMethod.PATCH)  ) ) {
				if( ownerId == null || ownerId.isEmpty() ) {
					((HttpServletResponse) response).setStatus(400);
					GGAPIServiceResponse responseObject = new GGAPIServiceResponse(
							"No header "+this.ownerIdHeaderName+" found", GGAPIServiceResponseCode.CLIENT_ERROR);
					ResponseEntity<?> responseEntity = GGAPIServiceResponseUtils.toResponseEntity(responseObject);

					String json = new ObjectMapper().writeValueAsString(responseEntity.getBody());

					response.setStatus(responseEntity.getStatusCode().value());
					response.setContentType("application/json");
					response.getWriter().write(json);
					response.getWriter().flush();
					return false;
				}
			}
			
			if( ownerId != null && !ownerId.isEmpty() ) {
				caller.setOwnerId(ownerId);
					
				if( this.ownersDomain.isPresent() ) {
						try {
							Object owner = this.ownersFactory.getEntityFromRepository(caller, null, GGAPIEntityIdentifier.UUID, ownerId);
							IGGObjectQuery q = GGObjectQueryFactory.objectQuery(this.ownersDomain.get().getEntity().getValue0(), owner);
							
							caller.setOwnerId((String) q.getValue(this.ownersDomain.get().getEntity().getValue1().ownerIdFieldAddress()));
							caller.setSuperOwner((boolean) q.getValue(this.ownersDomain.get().getEntity().getValue1().superOnwerIdFieldAddress()));
						} catch (GGAPIException e) {
							GGAPIServiceResponse responseObject = new GGAPIServiceResponse(e.getMessage(),
									GGAPIServiceResponseCode.fromExceptionCode(e));
							ResponseEntity<?> responseEntity = GGAPIServiceResponseUtils.toResponseEntity(responseObject);

							String json = new ObjectMapper().writeValueAsString(responseEntity.getBody());

							response.setStatus(responseEntity.getStatusCode().value());
							response.setContentType("application/json");
							response.getWriter().write(json);
							response.getWriter().flush();
							return false;
						} catch ( GGReflectionException e) {
							
							GGAPIException apiException = GGAPIException.findFirstInException(e);
							if (apiException != null) {
								GGAPIServiceResponse responseObject = new GGAPIServiceResponse(apiException.getMessage(),GGAPIServiceResponseCode.fromExceptionCode(apiException));
								ResponseEntity<?> responseEntity = GGAPIServiceResponseUtils.toResponseEntity(responseObject);

								String json = new ObjectMapper().writeValueAsString(responseEntity.getBody());

								response.setStatus(responseEntity.getStatusCode().value());
								response.setContentType("application/json");
								response.getWriter().write(json);
								response.getWriter().flush();
								return false;
							} else {
								GGAPIServiceResponse responseObject = new GGAPIServiceResponse(e.getMessage(),
										GGAPIServiceResponseCode.SERVER_ERROR);
								ResponseEntity<?> responseEntity = GGAPIServiceResponseUtils.toResponseEntity(responseObject);

								String json = new ObjectMapper().writeValueAsString(responseEntity.getBody());

								response.setStatus(responseEntity.getStatusCode().value());
								response.setContentType("application/json");
								response.getWriter().write(json);
								response.getWriter().flush();
								return false;
							}
						}
				} else {
					if( ownerId.equals(this.superOwnerId) ) {
						caller.setSuperOwner(true);
					}
				}
			}
		}
		return true;
	}

	private void doAccessRuleFilter(GGAPICaller caller, HttpServletRequest request) {
		HttpMethod method = this.getHttpMethod(request);
		String uriTotest = this.getUri(request);
		IGGAPIAccessRule accessRule = this.engine.getSecurity().getAccessRulesRegistry()
				.getAccessRule(GGAPIServiceMethodToHttpMethodBinder.fromHttpMethod(method), uriTotest);

		caller.setAccessRule(accessRule);
	}

	private boolean doTenantIdFiltering(GGAPICaller caller, String tenantId, String requestedtenantId,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		IGGAPIAccessRule accessRule = caller.getAccessRule();

		if (accessRule != null && caller.getTenantId() == null) {

			if (accessRule != null && accessRule.getAccess() == GGAPIServiceAccess.anonymous) {
				caller.setAnonymous(true);
			}

			if ((tenantId == null || tenantId.isEmpty())
					&& (accessRule != null && accessRule.getAccess() != GGAPIServiceAccess.anonymous)) {
				GGAPIServiceResponse responseObject = new GGAPIServiceResponse(
						"No header " + this.tenantIdHeaderName + " found", GGAPIServiceResponseCode.CLIENT_ERROR);
				ResponseEntity<?> responseEntity = GGAPIServiceResponseUtils.toResponseEntity(responseObject);

				String json = new ObjectMapper().writeValueAsString(responseEntity.getBody());

				response.setStatus(responseEntity.getStatusCode().value());
				response.setContentType("application/json");
				response.getWriter().write(json);
				response.getWriter().flush();
				return false;
			}

			caller.setTenantId(tenantId);
			if (requestedtenantId != null && !requestedtenantId.isEmpty()) {
				caller.setRequestedTenantId(requestedtenantId);
			} else {
				caller.setRequestedTenantId(tenantId);
			}

			if (tenantId != null && tenantId.equals(this.superTenantId)) {
				caller.setSuperTenant(true);
			}

			if (this.tenantsDomain != null && tenantId != null) {
				try {
					GGAPICaller superCaller = new GGAPICaller();
					superCaller.setTenantId(tenantId);

					Object tenant = this.tenantsFactory.getEntityFromRepository(caller, null,
							GGAPIEntityIdentifier.UUID, tenantId);

					caller.setSuperTenant((boolean) GGObjectQueryFactory.objectQuery(tenant)
							.getValue(tenantsDomain.get().getEntity().getValue1().superTenantFieldAddress()));

					if (!caller.getTenantId().equals(caller.getRequestedTenantId())) {
						superCaller.setTenantId(requestedtenantId);
						this.tenantsFactory.getEntityFromRepository(caller, null, GGAPIEntityIdentifier.UUID,
								caller.getRequestedTenantId());
					}

				} catch (GGAPIException e) {
					GGAPIServiceResponse responseObject = new GGAPIServiceResponse(e.getMessage(),
							GGAPIServiceResponseCode.fromExceptionCode(e));
					ResponseEntity<?> responseEntity = GGAPIServiceResponseUtils.toResponseEntity(responseObject);

					String json = new ObjectMapper().writeValueAsString(responseEntity.getBody());

					response.setStatus(responseEntity.getStatusCode().value());
					response.setContentType("application/json");
					response.getWriter().write(json);
					response.getWriter().flush();
					return false;
				} catch (GGReflectionException e) {

					GGAPIException apiException = GGAPIException.findFirstInException(e);
					if (apiException != null) {
						GGAPIServiceResponse responseObject = new GGAPIServiceResponse(apiException.getMessage(), GGAPIServiceResponseCode.fromExceptionCode(apiException));
						ResponseEntity<?> responseEntity = GGAPIServiceResponseUtils.toResponseEntity(responseObject);

						String json = new ObjectMapper().writeValueAsString(responseEntity.getBody());

						response.setStatus(responseEntity.getStatusCode().value());
						response.setContentType("application/json");
						response.getWriter().write(json);
						response.getWriter().flush();
						return false;
					} else {
						GGAPIServiceResponse responseObject = new GGAPIServiceResponse(e.getMessage(), GGAPIServiceResponseCode.SERVER_ERROR);
						ResponseEntity<?> responseEntity = GGAPIServiceResponseUtils.toResponseEntity(responseObject);

						String json = new ObjectMapper().writeValueAsString(responseEntity.getBody());

						response.setStatus(responseEntity.getStatusCode().value());
						response.setContentType("application/json");
						response.getWriter().write(json);
						response.getWriter().flush();
						return false;
					}
				}
			}
			if (caller.isSuperTenant()) {
				if (caller.getRequestedTenantId() == null) {
					if (GGAPIServiceMethodToHttpMethodBinder
							.fromServiceMethod(accessRule.getMethod()) != HttpMethod.GET) {
						caller.setRequestedTenantId(caller.getTenantId());
					}
				}
			} else {
				if (caller.getRequestedTenantId() == null) {
					caller.setRequestedTenantId(caller.getTenantId());
				}
			}
		}
		return true;
	}

	private String getUri(ServletRequest request) {
		String uri = ((HttpServletRequest) request).getRequestURI();
		String uriTotest = uri;
		String[] uriParts = uri.split("/");

		if (uriParts.length > 2) {
			uriTotest = "/" + uriParts[1] + "/*";
		}
		return uriTotest;
	}

	private HttpMethod getHttpMethod(ServletRequest request) {
		HttpMethod method = HttpMethod.GET;
		switch (((HttpServletRequest) request).getMethod()) {
		case "GET":
			method = HttpMethod.GET;
			break;
		case "POST":
			method = HttpMethod.POST;
			break;
		case "PATCH":
			method = HttpMethod.PATCH;
			break;
		case "DELETE":
			method = HttpMethod.DELETE;
			break;
		}
		return method;
	}

	private IGGAPICaller getCaller(ServletRequest request) {
		return (IGGAPICaller) request.getAttribute(GGAPICallerFilter.CALLER_ATTRIBUTE_NAME);
	}

	private IGGAPIDomain getDomain(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String[] uriParts = uri.split("/");
		return this.engine.getDomainsRegistry().getDomain(uriParts[1]);
	}

}
