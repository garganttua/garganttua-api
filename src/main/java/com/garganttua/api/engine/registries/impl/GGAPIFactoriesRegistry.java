package com.garganttua.api.engine.registries.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.entity.factory.IGGAPIEntityFactory;
import com.garganttua.api.engine.GGAPIDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.engine.registries.IGGAPIFactoriesRegistry;
import com.garganttua.api.engine.registries.IGGAPIRepositoriesRegistry;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("factoriesRegistry")
public class GGAPIFactoriesRegistry implements IGGAPIFactoriesRegistry {
	
	@Setter
	private GGAPIDomain domain;
	
	@Setter
	private IGGAPIEngine engine;
	
	@Autowired
	private ApplicationContext context;
	
    @Autowired
    private Environment environment;
    
    @Autowired 
    private IGGAPIRepositoriesRegistry repositoriesRegistry;


	@PostConstruct
	private void init() throws GGAPIEngineException {

		log.info("Creating Factories ...");

	}


	@Override
	public List<IGGAPIEntityFactory<?>> getFactories() {
		// TODO Auto-generated method stub
		return null;
	}
}
