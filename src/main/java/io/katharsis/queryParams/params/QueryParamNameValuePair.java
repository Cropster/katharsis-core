package io.katharsis.queryParams.params;

/**
 * Represents a pair (parameterName, parameterValue). Allows you to build links containing
 * parameter data. Note the toString() method.
 */
public class QueryParamNameValuePair {

    private final String name;

    private final String value;

    public QueryParamNameValuePair(String name, String value) {
        if (name == null || value == null)
            throw new IllegalArgumentException("Name and value must be non-null");

        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryParamNameValuePair that = (QueryParamNameValuePair) o;

        if (!name.equals(that.name)) return false;
        return value.equals(that.value);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    /**
     * Get a string representation like "filter[projects][name]=projectX"
     * @return parameterName=value 
     */
    @Override
    public String toString() {
        return name + "=" + value;
    }


}
