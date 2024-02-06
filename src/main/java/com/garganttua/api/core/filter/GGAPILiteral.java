package com.garganttua.api.core.filter;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GGAPILiteral {
	
	private static ObjectMapper mapper = new ObjectMapper();

	public static final String OPERATOR_PREFIX = "$";
	
	public static final String OPERATOR_FIELD = OPERATOR_PREFIX + "field";

	public static final String OPERATOR_EQUAL = OPERATOR_PREFIX + "eq";
	public static final String OPERATOR_NOT_EQUAL = OPERATOR_PREFIX + "ne";
	public static final String OPERATOR_GREATER_THAN = OPERATOR_PREFIX + "gt";
	public static final String OPERATOR_GREATER_THAN_EXCLUSIVE = OPERATOR_PREFIX + "gte";
	public static final String OPERATOR_LOWER_THAN = OPERATOR_PREFIX + "lt";
	public static final String OPERATOR_LOWER_THAN_EXCLUSIVE = OPERATOR_PREFIX + "lte";
	public static final String OPERATOR_REGEX = OPERATOR_PREFIX + "regex";
	public static final String OPERATOR_EMPTY = OPERATOR_PREFIX + "empty";
	
	public static final String OPERATOR_IN = OPERATOR_PREFIX + "in";
	public static final String OPERATOR_NOT_IN = OPERATOR_PREFIX + "nin";
	
	public static final String OPERATOR_AND = OPERATOR_PREFIX + "and";
	public static final String OPERATOR_OR = OPERATOR_PREFIX + "or";
	public static final String OPERATOR_NOR = OPERATOR_PREFIX + "nor";
	public static final String OPERATOR_TEXT = OPERATOR_PREFIX + "text";

	private static List<String> finalOperators = new ArrayList<String>();
	
	static {
		finalOperators.add(OPERATOR_EQUAL);
		finalOperators.add(OPERATOR_NOT_EQUAL);
		finalOperators.add(OPERATOR_GREATER_THAN);
		finalOperators.add(OPERATOR_GREATER_THAN_EXCLUSIVE);
		finalOperators.add(OPERATOR_LOWER_THAN);
		finalOperators.add(OPERATOR_LOWER_THAN_EXCLUSIVE);
		finalOperators.add(OPERATOR_REGEX);
		finalOperators.add(OPERATOR_EMPTY);
		finalOperators.add(OPERATOR_IN);
		finalOperators.add(OPERATOR_NOT_IN);
	}
	
	@JsonInclude
	private String name;

	@JsonInclude(Include.NON_NULL)
	private Object value;

	@JsonInclude(Include.NON_NULL)
	private List<GGAPILiteral> literals;

	public static void validate(GGAPILiteral literal) throws GGAPILiteralException {
		if( literal == null ) {
			return;
		}
		
		if (literal.name != null && !literal.name.startsWith(OPERATOR_PREFIX)) {
			throw new GGAPILiteralException("Invalid literal name, should start with $");
		}
		if( literal.name != null ) {
			switch (literal.name) {
			case OPERATOR_EQUAL:
			case OPERATOR_NOT_EQUAL:
			case OPERATOR_GREATER_THAN:
			case OPERATOR_GREATER_THAN_EXCLUSIVE:
			case OPERATOR_LOWER_THAN:
			case OPERATOR_LOWER_THAN_EXCLUSIVE:
			case OPERATOR_REGEX:
				if (literal.value == null ) {
					throw new GGAPILiteralException("Value cannot be null with literal of type "+literal.name);
				}
				if (literal.literals != null && !literal.literals.isEmpty()) {
					throw new GGAPILiteralException("Literal of type "+literal.name+" does not accept sub literals");
				}
				break;
			case OPERATOR_IN:
			case OPERATOR_NOT_IN:
				if (literal.value != null ) {
					throw new GGAPILiteralException("Value must be null with literal of type "+literal.name);
				}
				if (literal.literals == null || literal.literals.size() < 1) {
					throw new GGAPILiteralException("Literal of type "+literal.name+" needs at least 1 sub literals");
				}
				for( GGAPILiteral sub: literal.literals) {
					if( sub.name != null && !sub.name.isEmpty() ) {
						throw new GGAPILiteralException("Literal of type "+literal.name+" cannot have sub literal with a name");
					}
					if( sub.value == null ) {
						throw new GGAPILiteralException("Literal of type "+literal.name+" cannot have sub literal without value");
					}
					if (sub.literals != null && sub.literals.size() > 0) {
						throw new GGAPILiteralException("Literal of type "+literal.name+" cannot have sub literals with sub literals");
					}
				}
				
				break;
			case OPERATOR_TEXT:
				if (literal.value == null ) {
					throw new GGAPILiteralException("Value must not be null with literal of type "+literal.name);
				}
				if (literal.literals == null || literal.literals.size() < 1) {
					throw new GGAPILiteralException("Literal of type "+literal.name+" needs at least 1 sub literals");
				}
				for( GGAPILiteral sub: literal.literals) {
					if( sub.name != null && !sub.name.isEmpty() && !sub.name.equals(OPERATOR_FIELD) ) {
						throw new GGAPILiteralException("Literal of type "+literal.name+" cannot have sub literal other than $field");
					}
					if( sub.value == null ) {
						throw new GGAPILiteralException("Literal of type "+literal.name+" cannot have sub literal without value");
					}
					if (sub.literals != null && sub.literals.size() > 0) {
						throw new GGAPILiteralException("Literal of type "+literal.name+" cannot have sub literals with sub literals");
					}
				}
				
				break;
			case OPERATOR_EMPTY:
				if (literal.value != null ) {
					throw new GGAPILiteralException("Value must be null with literal of type "+literal.name);
				}
				if (literal.literals != null && !literal.literals.isEmpty()) {
					throw new GGAPILiteralException("Literal of type "+literal.name+" does not accept sub literals");
				}
				break;
			case OPERATOR_OR:
			case OPERATOR_AND:
			case OPERATOR_NOR:
				if (literal.value != null ) {
					throw new GGAPILiteralException("Value must be null with literal of type "+literal.name);
				}
				if (literal.literals == null || literal.literals.size() < 2) {
					throw new GGAPILiteralException("Literal of type "+literal.name+" needs at least 2 sub literals");
				}
				break;
			case OPERATOR_FIELD:
				if (literal.value == null ) {
					throw new GGAPILiteralException("Value cannot be null with literal of type "+literal.name);
				}
				if (literal.literals != null && literal.literals.size() > 1) {
					throw new GGAPILiteralException("Literal of type "+literal.name+" needs 0 or 1 sub literals");
				}
				if( literal.literals!=null && literal.literals.size() == 1 && !isFinal(literal.literals.get(0)) ) {
					throw new GGAPILiteralException("Literal of type "+literal.name+" needs exactly 1 sub literals of type equals, not equals, greater than, greater than exclusive, lower than, lower than exclusive, regex, empty, in or not in.");
				}
				break;
			default:
				throw new GGAPILiteralException("Invalid literal name " + literal.name);
			}
		}

		if (literal.literals != null) {
			literal.literals.forEach(l -> {
				try {
					validate(l);
				} catch (GGAPILiteralException e) {
					 throw new RuntimeException(e);
				}
			});
		}
	}

	public static boolean isFinal(GGAPILiteral literal) {
		return finalOperators.contains(literal.getName());
	}

	static public GGAPILiteral getFilterForTestingFieldEquality(String fieldName, String fieldvalue) {
		GGAPILiteral equalsToRequestedTenantId = new GGAPILiteral(GGAPILiteral.OPERATOR_EQUAL, fieldvalue, null);
		List<GGAPILiteral> fieldTenantIdLiterals = new ArrayList<GGAPILiteral>();
		fieldTenantIdLiterals.add(equalsToRequestedTenantId);
		
		GGAPILiteral fieldTenantId = new GGAPILiteral(GGAPILiteral.OPERATOR_FIELD, fieldName, fieldTenantIdLiterals);
		return fieldTenantId;
	}
	
	public static GGAPILiteral getFilterForTestingFieldEquality(String fieldName, boolean fieldvalue) {
		GGAPILiteral equalsToRequestedTenantId = new GGAPILiteral(GGAPILiteral.OPERATOR_EQUAL, fieldvalue, null);
		List<GGAPILiteral> fieldTenantIdLiterals = new ArrayList<GGAPILiteral>();
		fieldTenantIdLiterals.add(equalsToRequestedTenantId);
		
		GGAPILiteral fieldTenantId = new GGAPILiteral(GGAPILiteral.OPERATOR_FIELD, fieldName, fieldTenantIdLiterals);
		return fieldTenantId;
	}
	
	@Override
	public String toString() {
		
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
		}
		return "";
		
	}


}
