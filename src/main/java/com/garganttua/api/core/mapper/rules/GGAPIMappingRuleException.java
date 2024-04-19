package com.garganttua.api.core.mapper.rules;

import com.garganttua.api.core.exceptions.GGAPICoreException;
import com.garganttua.api.core.exceptions.GGAPICoreExceptionCode;

public class GGAPIMappingRuleException extends GGAPICoreException {

	public GGAPIMappingRuleException(GGAPICoreExceptionCode code, String string) {
		super(code, string);
	}

	public GGAPIMappingRuleException(Exception e) {
		super(e);
	}

	private static final long serialVersionUID = -5561497859151622878L;

}
