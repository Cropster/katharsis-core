package io.katharsis.queryParams.params;

import java.util.Set;

import io.katharsis.queryParams.include.Inclusion;

/**
 * Interface  for any filter that uses a one-dimensional string-indexed array in the parameter name.<br/>
 * Eg. group[...]
 *
 * @param <P> Type of the parameter values that may be extracted. Right now either {@link String}
 * or {@link Inclusion}
 */
public interface OneDimensionalQueryParam<P> {

    Set<P> getParams();
}