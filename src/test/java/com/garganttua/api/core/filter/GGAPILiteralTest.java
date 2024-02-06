package com.garganttua.api.core.filter;

import org.junit.jupiter.api.Test;

public class GGAPILiteralTest {
	
	@Test
	public void test() {
		GGAPILiteral lit = GGAPILiteral.getFilterForTestingFieldEquality("type", "toto");
		System.out.println(lit);
	}

}
