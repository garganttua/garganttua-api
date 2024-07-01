package com.garganttua.api.spec.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.geojson.GeoJsonObject;

import com.garganttua.api.spec.GGAPIExceptionCode;

import lombok.Getter;
import lombok.Setter;

public class GGAPILiteral implements IGGAPIFilter {

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
	private String name;

	@Getter
	@Setter
	private Object value;

	@Getter
	@Setter
	private List<IGGAPIFilter> literals;
	
	private GGAPILiteral() {
		
	}

	private GGAPILiteral(String operator, Object value, List<IGGAPIFilter> subs) {
		this.name = operator;
		this.value = value;
		this.literals = subs;
	}
	
	public void removeSubLiteral(IGGAPIFilter child) {
        if (literals != null) {
            Iterator<IGGAPIFilter> iterator = literals.iterator();
            while (iterator.hasNext()) {
            	IGGAPIFilter current = iterator.next();
                if (current.equals(child)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }
	
	public void replaceSubLiteral(IGGAPIFilter actual, IGGAPIFilter futur) {
        if (literals != null && actual != null && futur != null) {
            replaceSubLiteral(literals, actual, futur);
        }
    }

    private void replaceSubLiteral(List<IGGAPIFilter> literals, IGGAPIFilter actual, IGGAPIFilter futur) {
        for (int i = 0; i < literals.size(); i++) {
        	IGGAPIFilter subLiteral = literals.get(i);
            if (subLiteral.equals(actual)) {
                literals.set(i, futur);
            } else {
                List<IGGAPIFilter> subLiterals = subLiteral.getLiterals();
                if (subLiterals != null) {
                    replaceSubLiteral(subLiterals, actual, futur);
                }
            }
        }
    }

	public static void validate(IGGAPIFilter literal) throws GGAPILiteralException {
		if( literal == null ) {
			return;
		}
		
		if (literal.getName() != null && !literal.getName().startsWith(OPERATOR_PREFIX)) {
			throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Invalid literal name, should start with $");
		}
		if( literal.getName() != null ) {
			switch (literal.getName()) {
			case OPERATOR_EQUAL:
			case OPERATOR_NOT_EQUAL:
			case OPERATOR_GEOLOC:
			case OPERATOR_GEOLOC_SPHERE:
			case OPERATOR_GREATER_THAN:
			case OPERATOR_GREATER_THAN_EXCLUSIVE:
			case OPERATOR_LOWER_THAN:
			case OPERATOR_LOWER_THAN_EXCLUSIVE:
			case OPERATOR_REGEX:
				if (literal.getValue() == null ) {
					throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Value cannot be null with literal of type "+literal.getName());
				}
				if (literal.getLiterals() != null && !literal.getLiterals().isEmpty()) {
					throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Literal of type "+literal.getName()+" does not accept sub literals");
				}
				break;
			case OPERATOR_IN:
			case OPERATOR_NOT_IN:
				if (literal.getValue() != null ) {
					throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Value must be null with literal of type "+literal.getName());
				}
				if (literal.getLiterals() == null || literal.getLiterals().size() < 1) {
					throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Literal of type "+literal.getName()+" needs at least 1 sub literals");
				}
				for( IGGAPIFilter sub: literal.getLiterals()) {
					if( sub.getName() != null && !sub.getName().isEmpty() ) {
						throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Literal of type "+literal.getName()+" cannot have sub literal with a name");
					}
					if( sub.getValue() == null ) {
						throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Literal of type "+literal.getName()+" cannot have sub literal without value");
					}
					if (sub.getLiterals() != null && sub.getLiterals().size() > 0) {
						throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Literal of type "+literal.getName()+" cannot have sub literals with sub literals");
					}
				}
				
				break;
			case OPERATOR_TEXT:
				if (literal.getValue() == null ) {
					throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Value must not be null with literal of type "+literal.getName());
				}
				if (literal.getLiterals() == null || literal.getLiterals().size() < 1) {
					throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Literal of type "+literal.getName()+" needs at least 1 sub literals");
				}
				for( IGGAPIFilter sub: literal.getLiterals()) {
					if( sub.getName() != null && !sub.getName().isEmpty() && !sub.getName().equals(OPERATOR_FIELD) ) {
						throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Literal of type "+literal.getName()+" cannot have sub literal other than $field");
					}
					if( sub.getValue() == null ) {
						throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Literal of type "+literal.getName()+" cannot have sub literal without value");
					}
					if (sub.getLiterals() != null && sub.getLiterals().size() > 0) {
						throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Literal of type "+literal.getName()+" cannot have sub literals with sub literals");
					}
				}
				
				break;
			case OPERATOR_EMPTY:
				if (literal.getValue() != null ) {
					throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Value must be null with literal of type "+literal.getName());
				}
				if (literal.getLiterals() != null && !literal.getLiterals().isEmpty()) {
					throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Literal of type "+literal.getName()+" does not accept sub literals");
				}
				break;
			case OPERATOR_OR:
			case OPERATOR_AND:
			case OPERATOR_NOR:
				if (literal.getValue() != null ) {
					throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Value must be null with literal of type "+literal.getName());
				}
				if (literal.getLiterals() == null || literal.getLiterals().size() < 2) {
					throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Literal of type "+literal.getName()+" needs at least 2 sub literals");
				}
				break;
			case OPERATOR_FIELD:
				if (literal.getValue() == null ) {
					throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Value cannot be null with literal of type "+literal.getName());
				}
				if (literal.getLiterals() != null && literal.getLiterals().size() > 1) {
					throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Literal of type "+literal.getName()+" needs 0 or 1 sub literals");
				}
				if( literal.getLiterals()!=null && literal.getLiterals().size() == 1 && !isFinal(literal.getLiterals().get(0)) ) {
					throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Literal of type "+literal.getName()+" needs exactly 1 sub literals of type equals, not equals, greater than, greater than exclusive, lower than, lower than exclusive, regex, empty, in, not in, geoWithin or geoWithinSphere.");
				}
				break;
			default:
				throw new GGAPILiteralException(GGAPIExceptionCode.BAD_REQUEST, "Invalid literal name " + literal.getName());
			}
		}

		if (literal.getLiterals() != null) {
			literal.getLiterals().forEach(l -> {
				try {
					validate(l);
				} catch (GGAPILiteralException e) {
					 throw new RuntimeException(e);
				}
			});
		}
	}

	public static boolean isFinal(IGGAPIFilter literal) {
		return finalOperators.contains(literal.getName());
	}

	public static GGAPILiteral and(IGGAPIFilter ...filters) {
		return new GGAPILiteral(GGAPILiteral.OPERATOR_AND, null, new ArrayList<IGGAPIFilter>(Arrays.asList(filters)));
	}

	public static GGAPILiteral eq(String fieldName, Object value) {
		return operator(GGAPILiteral.OPERATOR_EQUAL, fieldName, value);
	}

	private static GGAPILiteral operator(String operator, String fieldName, Object value) {
		GGAPILiteral valueLiteral = new GGAPILiteral(operator, value, null);
		List<IGGAPIFilter> fieldLiterals = new ArrayList<IGGAPIFilter>();
		fieldLiterals.add(valueLiteral);

		return new GGAPILiteral(GGAPILiteral.OPERATOR_FIELD, fieldName, fieldLiterals);
	}

	public static GGAPILiteral ne(String fieldName, Object value) {
		return operator(GGAPILiteral.OPERATOR_NOT_EQUAL, fieldName, value);
	}

	public static GGAPILiteral gt(String fieldName, Object value) {
		return operator(GGAPILiteral.OPERATOR_GREATER_THAN, fieldName, value);
	}
	
	public static GGAPILiteral gte(String fieldName, Object value) {
		return operator(GGAPILiteral.OPERATOR_GREATER_THAN_EXCLUSIVE, fieldName, value);
	}
	
	public static GGAPILiteral lt(String fieldName, Object value) {
		return operator(GGAPILiteral.OPERATOR_LOWER_THAN, fieldName, value);
	}
	
	public static GGAPILiteral lte(String fieldName, Object value) {
		return operator(GGAPILiteral.OPERATOR_LOWER_THAN_EXCLUSIVE, fieldName, value);
	}

	public static GGAPILiteral empty(String fieldName) {
		return operator(GGAPILiteral.OPERATOR_EMPTY, fieldName, null);
	}

	public static GGAPILiteral regex(String fieldName, String regex) {
		Pattern pattern = Pattern.compile(regex);
		return operator(GGAPILiteral.OPERATOR_REGEX, fieldName, regex);
	}

	public static GGAPILiteral in(String fieldName, Object ...values) {	
		return operatorWithManyValues(GGAPILiteral.OPERATOR_IN, fieldName, values);
	}

	private static GGAPILiteral operatorWithManyValues(String operator, String fieldName, Object... values) {
		GGAPILiteral literal = operator(operator, fieldName, null);
		ArrayList<IGGAPIFilter> valuesLiterals = new ArrayList<IGGAPIFilter>();
		for(Object value: values) {
			valuesLiterals.add(new GGAPILiteral(null, value, null));
		}
		literal.getLiterals().get(0).setLiterals(valuesLiterals);
		return literal;
	}

	public static GGAPILiteral nin(String fieldName, Object ...values) {		
		return operatorWithManyValues(GGAPILiteral.OPERATOR_NOT_IN, fieldName, values);
	}

	public static GGAPILiteral text(String fieldName, String value) {
		return operator(GGAPILiteral.OPERATOR_TEXT, fieldName, value);
	}

	public static GGAPILiteral or(IGGAPIFilter ...filters) {
		return new GGAPILiteral(GGAPILiteral.OPERATOR_OR, null, new ArrayList<IGGAPIFilter>(Arrays.asList(filters)));
	}
	
	public static GGAPILiteral nor(IGGAPIFilter ...filters) {
		return new GGAPILiteral(GGAPILiteral.OPERATOR_NOR, null, new ArrayList<IGGAPIFilter>(Arrays.asList(filters)));
	}

	public GGAPILiteral andOperator(IGGAPIFilter ...filters) {
		if( this.name.equals(GGAPILiteral.OPERATOR_AND)) {
			List<IGGAPIFilter> filterList = new ArrayList<IGGAPIFilter>(Arrays.asList(filters));
			this.literals.addAll(filterList); 
			return this;
		} else {
			List<IGGAPIFilter> filterList = new ArrayList<IGGAPIFilter>(Arrays.asList(filters));
			filterList.add(this);	
			GGAPILiteral[] arr = new GGAPILiteral[filterList.size()];
			return GGAPILiteral.and(filterList.toArray(arr));
		}
	}
	
	public GGAPILiteral orOperator(IGGAPIFilter ...filters) {
		if( this.name.equals(GGAPILiteral.OPERATOR_OR)) {
			List<IGGAPIFilter> filterList = new ArrayList<IGGAPIFilter>(Arrays.asList(filters));
			this.literals.addAll(filterList); 
			return this;
		} else {
			List<IGGAPIFilter> filterList = new ArrayList<IGGAPIFilter>(Arrays.asList(filters));
			filterList.add(this);	
			IGGAPIFilter[] arr = new IGGAPIFilter[filterList.size()];
			return GGAPILiteral.or(filterList.toArray(arr));
		}
	}
	
	public GGAPILiteral norOperator(GGAPILiteral ...filters) {
		if( this.name.equals(GGAPILiteral.OPERATOR_NOR)) {
			List<IGGAPIFilter> filterList = new ArrayList<IGGAPIFilter>(Arrays.asList(filters));
			this.literals.addAll(filterList); 
			return this;
		} else {
			List<IGGAPIFilter> filterList = new ArrayList<IGGAPIFilter>(Arrays.asList(filters));
			filterList.add(this);	
			IGGAPIFilter[] arr = new IGGAPIFilter[filterList.size()];
			return GGAPILiteral.nor(filterList.toArray(arr));
		}
	}

	public static GGAPILiteral geolocWithin(String fieldName, GeoJsonObject object) {
		return operator(GGAPILiteral.OPERATOR_GEOLOC, fieldName, object);
	}
	
	public static GGAPILiteral geolocWithinSphere(String fieldName, GeoJsonObject object) {
		return operator(GGAPILiteral.OPERATOR_GEOLOC_SPHERE, fieldName, object);
	}
	
	@Override
	public IGGAPIFilter clone() {
        try {
        	IGGAPIFilter cloned = (IGGAPIFilter) super.clone();

            if (this.literals != null) {
                List<IGGAPIFilter> clonedLiterals = new ArrayList<>();
                for (IGGAPIFilter literal : this.getLiterals()) {
                    clonedLiterals.add(literal.clone());
                }
                cloned.setLiterals(clonedLiterals);
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
        GGAPILiteral that = (GGAPILiteral) o;
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
        sb.append("GGAPILiteral{name='").append(name).append('\'')
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

}
