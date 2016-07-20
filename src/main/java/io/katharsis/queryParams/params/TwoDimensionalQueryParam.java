package io.katharsis.queryParams.params;

import java.util.Map;
import java.util.Set;

/**
 * Interface for any filter that uses a two-dimensional string-indexed array in the parameter name.<br/>
 * Eg. filter[...][...]
 *
 * @param <P> Type of the parameter values that may be extracted. Right now only {@link String} is used.
 */
public interface TwoDimensionalQueryParam<P> {

    Map<String, Set<P>> getParams();
    
}
