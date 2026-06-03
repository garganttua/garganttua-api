package com.garganttua.api.commons.caller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("OwnerIds qualified owner-id helper")
class OwnerIdsTest {

	@Nested
	@DisplayName("qualify")
	class Qualify {

		@Test
		@DisplayName("joins domain and id with a colon")
		void joins() {
			assertEquals("users:018f-c3", OwnerIds.qualify("users", "018f-c3"));
		}

		@Test
		@DisplayName("returns null when id is null (propagates absence)")
		void nullIdPropagates() {
			assertNull(OwnerIds.qualify("users", null));
		}

		@Test
		@DisplayName("throws when id is present but domain is null/blank")
		void requiresDomain() {
			assertThrows(IllegalArgumentException.class, () -> OwnerIds.qualify(null, "x"));
			assertThrows(IllegalArgumentException.class, () -> OwnerIds.qualify("", "x"));
			assertThrows(IllegalArgumentException.class, () -> OwnerIds.qualify("   ", "x"));
		}
	}

	@Nested
	@DisplayName("domainOf / idOf")
	class Split {

		@Test
		@DisplayName("round-trips a qualified value")
		void roundTrip() {
			String q = OwnerIds.qualify("tokens", "abc");
			assertEquals("tokens", OwnerIds.domainOf(q));
			assertEquals("abc", OwnerIds.idOf(q));
		}

		@Test
		@DisplayName("idOf returns the value unchanged when unqualified; domainOf returns null")
		void unqualified() {
			assertEquals("rawuuid", OwnerIds.idOf("rawuuid"));
			assertNull(OwnerIds.domainOf("rawuuid"));
		}

		@Test
		@DisplayName("idOf keeps everything after the first colon (ids may contain colons)")
		void idMayContainColon() {
			assertEquals("a:b", OwnerIds.idOf("users:a:b"));
			assertEquals("users", OwnerIds.domainOf("users:a:b"));
		}

		@Test
		@DisplayName("null in, null out")
		void nullSafe() {
			assertNull(OwnerIds.domainOf(null));
			assertNull(OwnerIds.idOf(null));
		}
	}

	@Nested
	@DisplayName("isQualified")
	class IsQualified {

		@Test
		@DisplayName("true only when a domain prefix precedes a colon")
		void detects() {
			assertTrue(OwnerIds.isQualified("users:1"));
			assertFalse(OwnerIds.isQualified("rawuuid"));
			assertFalse(OwnerIds.isQualified(":leadingcolon"));
			assertFalse(OwnerIds.isQualified(null));
		}
	}
}
