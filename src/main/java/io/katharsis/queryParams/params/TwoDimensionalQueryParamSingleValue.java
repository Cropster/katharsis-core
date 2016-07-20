package io.katharsis.queryParams.params;

import java.util.Map;
import io.katharsis.queryParams.include.Inclusion;

/**
 * Interface  for any filter that uses a one-dimensional string-indexed array in the parameter name
 * and cannot return more than just a single value.
 * Eg. sort[...][...]=(asc|desc)
 *
 * @param <P> Type of the parameter values that may be extracted. Right now either {@link String}
 * or {@link Inclusion}
 */
public interface TwoDimensionalQueryParamSingleValue<P> {

	public Map<String, P> getParams();
}