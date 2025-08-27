package com.garganttua.api.core.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.geojson.GeoJsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.filter.IFilter;

import lombok.Getter;
import lombok.Setter;

public class Literal implements IFilter {

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
	public static final String OPERATOR_TEXT = OPERATOR_PREFIX + "text";
	public static final String OPERATOR_GEOLOC = OPERATOR_PREFIX + "geoWithin";
	public static final String OPERATOR_GEOLOC_SPHERE = OPERATOR_PREFIX + "geoWithinSphere";
	
	public static final String OPERATOR_IN = OPERATOR_PREFIX + "in";
	public static final String OPERATOR_NOT_IN = OPERATOR_PREFIX + "nin";
	
	public static final String OPERATOR_AND = OPERATOR_PREFIX + "and";
	public static final String OPERATOR_OR = OPERATOR_PREFIX + "or";
	public static final String OPERATOR_NOR = OPERATOR_PREFIX + "nor";

	public static final String CIRCLE_RADIUS = "radius";

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
		finalOperators.add(OPERATOR_GEOLOC);
		finalOperators.add(OPERATOR_GEOLOC_SPHERE);
	}

	@Getter
	@JsonProperty
	private String name;

	@Getter
	@Setter
	@JsonProperty
	private Object value;

	@JsonProperty
	private List<Literal> literals;
	
	private Literal() {
		
	}

	private Literal(String operator, Object value, List<Literal> subs) {
		this.name = operator;
		this.value = value;
		this.literals = subs;
	}
	
	@Override
	public void removeSubLiteral(IFilter child) {
        if (literals != null) {
            Iterator<Literal> iterator = literals.iterator();
            while (iterator.hasNext()) {
            	IFilter current = iterator.next();
                if (current.equals(child)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }
	
	@Override
	public void replaceSubLiteral(IFilter actual, IFilter futur) {
        if (literals != null && actual != null && futur != null) {
            this.replaceSubLiteral(literals, actual, futur);
        }
    }

    private void replaceSubLiteral(List<Literal> literals, IFilter actual, IFilter futur) {
        for (int i = 0; i < literals.size(); i++) {
        	Literal subLiteral = literals.get(i);
            if (subLiteral.equals(actual)) {
                literals.set(i, (Literal) futur);
            } else {
                List<Literal> subLiterals = subLiteral.literals;
                if (subLiterals != null) {
                    replaceSubLiteral(subLiterals, actual, futur);
                }
            }
        }
    }

	public static void validate(Literal literal) throws LiteralException {
		if( literal == null ) {
			return;
		}
		
		if (literal.name != null && !literal.name.startsWith(OPERATOR_PREFIX)) {
			throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Invalid literal name, should start with $");
		}
		if( literal.name != null ) {
			switch (literal.name) {
			case OPERATOR_EQUAL:
			case OPERATOR_NOT_EQUAL:
			case OPERATOR_GEOLOC:
			case OPERATOR_GEOLOC_SPHERE:
			case OPERATOR_GREATER_THAN:
			case OPERATOR_GREATER_THAN_EXCLUSIVE:
			case OPERATOR_LOWER_THAN:
			case OPERATOR_LOWER_THAN_EXCLUSIVE:
			case OPERATOR_REGEX:
				if (literal.value == null ) {
					throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Value cannot be null with literal of type "+literal.name);
				}
				if (literal.literals != null && !literal.literals.isEmpty()) {
					throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Literal of type "+literal.name+" does not accept sub literals");
				}
				break;
			case OPERATOR_IN:
			case OPERATOR_NOT_IN:
				if (literal.value != null ) {
					throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Value must be null with literal of type "+literal.value);
				}
				if (literal.literals == null || literal.literals.size() < 1) {
					throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Literal of type "+literal.name+" needs at least 1 sub literals");
				}
				for( Literal sub: literal.literals) {
					if( sub.name != null && !sub.name.isEmpty() ) {
						throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Literal of type "+literal.name+" cannot have sub literal with a name");
					}
					if( sub.value == null ) {
						throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Literal of type "+literal.name+" cannot have sub literal without value");
					}
					if (sub.literals != null && sub.literals.size() > 0) {
						throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Literal of type "+literal.name+" cannot have sub literals with sub literals");
					}
				}
				
				break;
			case OPERATOR_TEXT:
				if (literal.value == null ) {
					throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Value must not be null with literal of type "+literal.name);
				}
				if (literal.literals == null || literal.literals.size() < 1) {
					throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Literal of type "+literal.name+" needs at least 1 sub literals");
				}
				for( Literal sub: literal.literals) {
					if( sub.name != null && !sub.name.isEmpty() && !sub.name.equals(OPERATOR_FIELD) ) {
						throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Literal of type "+literal.name+" cannot have sub literal other than $field");
					}
					if( sub.value == null ) {
						throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Literal of type "+literal.name+" cannot have sub literal without value");
					}
					if (sub.literals != null && sub.literals.size() > 0) {
						throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Literal of type "+literal.name+" cannot have sub literals with sub literals");
					}
				}
				
				break;
			case OPERATOR_EMPTY:
				if (literal.value != null ) {
					throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Value must be null with literal of type "+literal.name);
				}
				if (literal.literals != null && !literal.literals.isEmpty()) {
					throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Literal of type "+literal.name+" does not accept sub literals");
				}
				break;
			case OPERATOR_OR:
			case OPERATOR_AND:
			case OPERATOR_NOR:
				if (literal.value != null ) {
					throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Value must be null with literal of type "+literal.name);
				}
				if (literal.literals == null || literal.literals.size() < 2) {
					throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Literal of type "+literal.name+" needs at least 2 sub literals");
				}
				break;
			case OPERATOR_FIELD:
				if (literal.value == null ) {
					throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Value cannot be null with literal of type "+literal.name);
				}
				if (literal.literals != null && literal.literals.size() > 1) {
					throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Literal of type "+literal.name+" needs 0 or 1 sub literals");
				}
				if( literal.literals!=null && literal.literals.size() == 1 && !isFinal(literal.literals.get(0)) ) {
					throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Literal of type "+literal.name+" needs exactly 1 sub literals of type equals, not equals, greater than, greater than exclusive, lower than, lower than exclusive, regex, empty, in, not in, geoWithin or geoWithinSphere.");
				}
				break;
			default:
				throw new LiteralException(CoreExceptionCode.BAD_REQUEST, "Invalid literal name " + literal.name);
			}
		}

		if (literal.literals != null) {
			literal.literals.forEach(l -> {
				try {
					validate(l);
				} catch (LiteralException e) {
					 throw new RuntimeException(e);
				}
			});
		}
	}

	public static boolean isFinal(Literal literal) {
		return finalOperators.contains(literal.name);
	}

	public static Literal and(Literal ...filters) {
		return new Literal(Literal.OPERATOR_AND, null, new ArrayList<Literal>(Arrays.asList(filters)));
	}

	public static Literal eq(String fieldName, Object value) {
		return operator(Literal.OPERATOR_EQUAL, fieldName, value);
	}

	private static Literal operator(String operator, String fieldName, Object value) {
		Literal valueLiteral = new Literal(operator, value, null);
		List<Literal> fieldLiterals = new ArrayList<Literal>();
		fieldLiterals.add(valueLiteral);

		return new Literal(Literal.OPERATOR_FIELD, fieldName, fieldLiterals);
	}

	public static Literal ne(String fieldName, Object value) {
		return operator(Literal.OPERATOR_NOT_EQUAL, fieldName, value);
	}

	public static Literal gt(String fieldName, Object value) {
		return operator(Literal.OPERATOR_GREATER_THAN, fieldName, value);
	}
	
	public static Literal gte(String fieldName, Object value) {
		return operator(Literal.OPERATOR_GREATER_THAN_EXCLUSIVE, fieldName, value);
	}
	
	public static Literal lt(String fieldName, Object value) {
		return operator(Literal.OPERATOR_LOWER_THAN, fieldName, value);
	}
	
	public static Literal lte(String fieldName, Object value) {
		return operator(Literal.OPERATOR_LOWER_THAN_EXCLUSIVE, fieldName, value);
	}

	public static Literal empty(String fieldName) {
		return operator(Literal.OPERATOR_EMPTY, fieldName, null);
	}

	public static Literal regex(String fieldName, String regex) {
		Pattern pattern = Pattern.compile(regex);
		return operator(Literal.OPERATOR_REGEX, fieldName, regex);
	}

	public static Literal in(String fieldName, Object ...values) {	
		return operatorWithManyValues(Literal.OPERATOR_IN, fieldName, values);
	}

	private static Literal operatorWithManyValues(String operator, String fieldName, Object... values) {
		Literal literal = operator(operator, fieldName, null);
		ArrayList<Literal> valuesLiterals = new ArrayList<Literal>();
		for(Object value: values) {
			valuesLiterals.add(new Literal(null, value, null));
		}
		literal.literals.get(0).literals = valuesLiterals;
		return literal;
	}

	public static Literal nin(String fieldName, Object ...values) {		
		return operatorWithManyValues(Literal.OPERATOR_NOT_IN, fieldName, values);
	}

	public static Literal text(String fieldName, String value) {
		return operator(Literal.OPERATOR_TEXT, fieldName, value);
	}

	public static Literal or(Literal ...filters) {
		return new Literal(Literal.OPERATOR_OR, null, new ArrayList<Literal>(Arrays.asList(filters)));
	}
	
	public static Literal nor(Literal ...filters) {
		return new Literal(Literal.OPERATOR_NOR, null, new ArrayList<Literal>(Arrays.asList(filters)));
	}

	public Literal andOperator(Literal ...filters) {
		if( this.name.equals(Literal.OPERATOR_AND)) {
			List<Literal> filterList = new ArrayList<Literal>(Arrays.asList(filters));
			this.literals.addAll(filterList); 
			return this;
		} else {
			List<Literal> filterList = new ArrayList<Literal>(Arrays.asList(filters));
			filterList.add(this);	
			Literal[] arr = new Literal[filterList.size()];
			return Literal.and(filterList.toArray(arr));
		}
	}
	
	public Literal orOperator(Literal ...filters) {
		if( this.name.equals(Literal.OPERATOR_OR)) {
			List<Literal> filterList = new ArrayList<Literal>(Arrays.asList(filters));
			this.literals.addAll(filterList); 
			return this;
		} else {
			List<Literal> filterList = new ArrayList<Literal>(Arrays.asList(filters));
			filterList.add(this);	
			Literal[] arr = new Literal[filterList.size()];
			return Literal.or(filterList.toArray(arr));
		}
	}
	
	public Literal norOperator(Literal ...filters) {
		if( this.name.equals(Literal.OPERATOR_NOR)) {
			List<Literal> filterList = new ArrayList<Literal>(Arrays.asList(filters));
			this.literals.addAll(filterList); 
			return this;
		} else {
			List<Literal> filterList = new ArrayList<Literal>(Arrays.asList(filters));
			filterList.add(this);	
			Literal[] arr = new Literal[filterList.size()];
			return Literal.nor(filterList.toArray(arr));
		}
	}

	public static Literal geolocWithin(String fieldName, GeoJsonObject object) {
		return operator(Literal.OPERATOR_GEOLOC, fieldName, object);
	}
	
	public static Literal geolocWithinSphere(String fieldName, GeoJsonObject object) {
		return operator(Literal.OPERATOR_GEOLOC_SPHERE, fieldName, object);
	}
	
	@Override
	public IFilter clone() {
        try {
        	Literal cloned = (Literal) super.clone();

            if (this.literals != null) {
                List<Literal> clonedLiterals = new ArrayList<>();
                for (Literal literal : this.literals) {
                    clonedLiterals.add((Literal) literal.clone());
                }
                cloned.literals = clonedLiterals;
            }

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Literal that = (Literal) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(value, that.value) &&
                Objects.equals(literals, that.literals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, literals);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Literal{name='").append(name).append('\'')
          .append(", value=").append(value)
          .append(", literals=").append(literalsToString())
          .append('}');
        return sb.toString();
    }

    private String literalsToString() {
        if (literals == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < literals.size(); i++) {
            sb.append(literals.get(i).toString());
            if (i < literals.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
        return sb.toString();
    }

	@Override
	public List<IFilter> getLiterals() {
		List<IFilter> returnedList = new ArrayList<IFilter>();
		if( this.literals != null )
			this.literals.forEach(lit -> {returnedList.add(lit);});
		return returnedList;
	}

	@Override
	@JsonIgnore
	public void setLiterals(List<IFilter> literals) {
		List<Literal> returnedList = new ArrayList<Literal>();
		if( this.literals != null )
			literals.forEach(lit->{returnedList.add((Literal) lit);});
		this.literals = returnedList;
	}
}
