package com.garganttua.api.interfaces.security.spring.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.interfaces.spring.rest.CallerFilter;
import com.garganttua.api.interfaces.spring.rest.ServiceMethodToHttpMethodBinder;
import com.garganttua.api.security.spring.core.ISpringSecurityRestConfigurer;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.security.IAccessRule;
import com.garganttua.api.spec.security.ISecurityEngine;
import com.garganttua.api.spec.security.authentication.IAuthenticationInterfacesRegistry;
import com.garganttua.api.spec.service.ServiceAccess;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@EnableWebSecurity
public class SpringSecurityRestConfiguration {

  @Autowired
  private IEngine engine;

  @Autowired
  private ISecurityEngine security;

  @Autowired
  private SpringTenantVerifierFilter tenantVerifier;

  @Autowired
  private SpringOwnerVerifierFilter ownerVerifier;

  @Autowired
  private SpringAuthorizationFilter authorizationFilter;

  @Value("${com.garganttua.api.spring.interface.rest.security.cors.enabled}")
  private boolean cors = true;

  @Value("${com.garganttua.api.spring.interface.rest.security.csrf.enabled}")
  private boolean csrf = false;

  @Autowired
  private CallerFilter callerFilter;

  @Autowired
  private List<ISpringSecurityRestConfigurer> configurers = new ArrayList<ISpringSecurityRestConfigurer>();

  @Autowired
  private SpringAuthenticatorSecurityProcessor securityProcessor;

  @PostConstruct
  private void init() {
    IAuthenticationInterfacesRegistry reg = this.security.getAuthenticationInterfacesRegistry();
    reg.getInterfaces().forEach(interfasse -> {
      if (ISpringSecurityRestConfigurer.class.isAssignableFrom(interfasse.getClass())) {
        this.configurers.add((ISpringSecurityRestConfigurer) interfasse);
      }
    });
  }

  @Bean
  public DefaultSecurityFilterChain configureFilterChain(HttpSecurity http) throws SecurityException {

    try {
      if (!this.csrf) {
        http.csrf().disable();
      }

      if (!this.cors) {
        http.cors().disable();
      }

      this.configurers.stream().forEach(config -> {
        try {
          config.configureFilterChain(http);
        } catch (Exception e) {
          log.atWarn().log("Error occured", e);
        }
      });

      this.configureAuthorizations(http);

      http.authorizeHttpRequests().and().addFilterBefore(this.authorizationFilter, AuthorizationFilter.class);
      http.authorizeHttpRequests().and().addFilterBefore(this.callerFilter, SpringAuthorizationFilter.class);
      http.authorizeHttpRequests().and().addFilterAfter(this.tenantVerifier, AuthorizationFilter.class);
      http.authorizeHttpRequests().and().addFilterAfter(this.ownerVerifier, SpringTenantVerifierFilter.class);
      http.authorizeHttpRequests().and().addFilterAfter(this.securityProcessor, AuthorizationFilter.class);
      http.authorizeHttpRequests().and().authorizeHttpRequests().requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll();
      http.authorizeHttpRequests().and().authorizeHttpRequests().requestMatchers("*/**").permitAll();

      return http.build();

    } catch (Exception e) {
      throw new SecurityException(e);
    }
  }

  private void configureAuthorizations(HttpSecurity http) throws Exception {
    for (IAccessRule accessRule : this.engine.getAccessRules()) {

      log.info("Applying security configuration {}", accessRule);

      HttpMethod method = ServiceMethodToHttpMethodBinder.fromServiceMethod(accessRule.getOperation());

      if (accessRule.getAccess() == ServiceAccess.authenticated
          || accessRule.getAccess() == ServiceAccess.tenant
          || accessRule.getAccess() == ServiceAccess.owner) {
        if (accessRule.getAuthority() != null && !accessRule.getAuthority().isEmpty()) {
          http.authorizeHttpRequests().requestMatchers(method, accessRule.getEndpoint())
              .hasAnyAuthority(accessRule.getAuthority()).and().authorizeHttpRequests();
        } else {
          http.authorizeHttpRequests().requestMatchers(method, accessRule.getEndpoint()).authenticated().and()
              .authorizeHttpRequests();
        }

      } else if (accessRule.getAccess() == ServiceAccess.anonymous) {
        http.authorizeHttpRequests().requestMatchers(method, accessRule.getEndpoint()).permitAll().and()
            .authorizeHttpRequests();
      }
    }
  }
}
