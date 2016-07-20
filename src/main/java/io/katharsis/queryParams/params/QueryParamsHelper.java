package io.katharsis.queryParams.params;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.RestrictedPaginationKeys;
import io.katharsis.queryParams.RestrictedSortingValues;
import io.katharsis.queryParams.include.Inclusion;
import static io.katharsis.resource.RestrictedQueryParamsMembers.fields;
import static io.katharsis.resource.RestrictedQueryParamsMembers.filter;
import static io.katharsis.resource.RestrictedQueryParamsMembers.group;
import static io.katharsis.resource.RestrictedQueryParamsMembers.include;
import static io.katharsis.resource.RestrictedQueryParamsMembers.page;
import static io.katharsis.resource.RestrictedQueryParamsMembers.sort;

import java.util.*;
import java.util.Map.Entry;

/**
 * Eases the extraction of parameters and allows collecting all passed parameters.
 * Usage examples:
 * <pre>
 * &#47;&#47; get all filter parameter values 
 * Set&lt;String&gt; projectNames = 
 * 	QueryParamsHelper
 * 		.with(queryParams)
 * 		.filters()
 * 		.get("projects", "name")
 * 		.all();
 * 
 * &#47;&#47; get one specific sort param value
 * QueryParamsHelper helper = new QueryParamsHelper(queryParams);
 * RestrictedSortingValues projectNameSort = helper.sortings().get("project", "name").one();
 * 
 * &#47;&#47; collect all filter parameters there are 
 * Set&lt;QueryParamNameValuePair&gt; all = helper.filters().all();
 * </pre>
 */
public class QueryParamsHelper {

    private QueryParams queryParams;

    public QueryParamsHelper(QueryParams params){
        if (params == null)
            throw new IllegalArgumentException("QueryParams must not be null");
        queryParams = params;
    }

    public static QueryParamsHelper with(QueryParams queryParams) {
        return new QueryParamsHelper(queryParams);
    }

    public TwoDimQueryable<String> filters() {
        return new TwoDimQueryable<>(queryParams.getFilters(), filter.name());
    }
    
    public OneDimQueryable<String> groupings() {
    	return new OneDimQueryable<String>(queryParams.getGrouping(), group.name());
    }
    
    public OneDimQueryable<String> fields() {
    	return new OneDimQueryable<String>(queryParams.getIncludedFields(), fields.name());
    }
    
    public OneDimQueryable<Inclusion> inclusions() {
    	return new OneDimQueryable<Inclusion>(queryParams.getIncludedRelations(), include.name());
    }
    
    public TwoDimOneValueQueryable<RestrictedSortingValues> sortings() {
    	return new TwoDimOneValueQueryable<>(queryParams.getSorting(), sort.name());
    }
    
    public PageHelper page() {
    	return new PageHelper(queryParams.getPagination());
    }
    
    public class PageHelper {
    	
    	private Map<RestrictedPaginationKeys, String> nameValueMap;
    	
    	private PageHelper(Map<RestrictedPaginationKeys, String> data) {
    		this.nameValueMap = data;
    	}
    	
    	private Integer lookupValue(RestrictedPaginationKeys key) {
    		if (nameValueMap == null)
        		return null;
        	
        	String value = nameValueMap.get(key);
        	if (value == null)
        		return null;
        	
        	return Integer.parseInt(value);
    	}
    	
    	public Integer number() {
    		return lookupValue(RestrictedPaginationKeys.number);
    	}
    	
    	public Integer size() {
    		return lookupValue(RestrictedPaginationKeys.size);
    	}
    	
    	public Integer limit() {
    		return lookupValue(RestrictedPaginationKeys.limit);
    	}
    	
    	public Integer offset() {
    		return lookupValue(RestrictedPaginationKeys.offset);
    	}
    	
    	public Set<QueryParamNameValuePair> all() {
    		HashSet<QueryParamNameValuePair> allParams = new HashSet<>();
    		
    		for (RestrictedPaginationKeys key : RestrictedPaginationKeys.values()) {
    			Integer value = lookupValue(key);
    			if (value == null)
    				continue;
    			
    			String template = "%s[%s]";
    			String name = String.format(template, page.name(), key.name());
    			allParams.add(new QueryParamNameValuePair(name, value.toString()));
    		}
    		return allParams;
    	}
    }
    
    /**
     * Query Helper for parameters that only use a one dimensional array
     * @param <E> type of the parameter values to retrieve
     */
    public class TwoDimOneValueQueryable<E> {

		private Map<String, ? extends TwoDimensionalQueryParamSingleValue<E>> firstDimMap;
		
		private String topLevelParamName;

		private TwoDimOneValueQueryable(
				TypedParams<? extends TwoDimensionalQueryParamSingleValue<E>> param,
				String topLevelParamName) {
			
			if (param != null)
				this.firstDimMap = param.getParams();
			
			this.topLevelParamName = topLevelParamName;
		}
		
		public SetWrapper<E> get(String resourceName, String paramName) {
			if (firstDimMap == null)
				return new SetWrapper<>();
			
			if (!firstDimMap.containsKey(resourceName))
				return new SetWrapper<>();
			
            TwoDimensionalQueryParamSingleValue<E> secondDimension = firstDimMap.get(resourceName);
            if (secondDimension == null)
                return new SetWrapper<>();

            E value = secondDimension.getParams().get(paramName);
            return new SetWrapper<E>(value);
		}
		
        public Set<QueryParamNameValuePair> all() {

            String paramNameTemplate = topLevelParamName + "[%s][%s]";
            HashSet<QueryParamNameValuePair> allParams = new LinkedHashSet<>();
            
            if (firstDimMap == null)
            	return allParams;

            // iterate over everything in eg sort[...]
			for (Entry<String, ? extends TwoDimensionalQueryParamSingleValue<E>> entry : firstDimMap.entrySet()) {
				String resourceName = entry.getKey();

                TwoDimensionalQueryParamSingleValue<E> secondDim = entry.getValue();
                if (secondDim == null)
                	continue;
             
                // iterate over everything in eg sort[resourceName][...]
                for (Entry<String, E> secondDimEntry : secondDim.getParams().entrySet()) {
                	E value = secondDimEntry.getValue();
                	if (value == null)
                		continue;
                	
                	String paramName = secondDimEntry.getKey();                	
                	String name = String.format(paramNameTemplate, resourceName, paramName);
                    allParams.add(new QueryParamNameValuePair(name, value.toString()));
                }
            }
            return allParams;
        }    	
    }
    
    /**
     * Query Helper for parameters that only use a one dimensional array
     * @param <E> type of the parameter values to retrieve
     */
    public class OneDimQueryable<E> {

		private Map<String, ? extends OneDimensionalQueryParam<E>> nameValueMap;
		
		private String topLevelParamName;

		private OneDimQueryable(TypedParams<? extends OneDimensionalQueryParam<E>> param, String topLevelParamName) {
			if (param != null)
				this.nameValueMap = param.getParams();
			
			this.topLevelParamName = topLevelParamName;
		}
		
		/**
		 * Grouping parameters for the passed resourceName
		 * @param resourceName of group parameters to consider
		 */
		public SetWrapper<E> get(String resourceName) {
			if (nameValueMap == null)
				return new SetWrapper<>();
			
			if (!nameValueMap.containsKey(resourceName))
				return new SetWrapper<>();
			
			OneDimensionalQueryParam<E> mapValue = nameValueMap.get(resourceName);
			if (mapValue == null)
				return new SetWrapper<>();
			
			Set<E> values = nameValueMap.get(resourceName).getParams();
			return new SetWrapper<E>(values);
		}
		
        public Set<QueryParamNameValuePair> all() {

            String paramNameTemplate = topLevelParamName + "[%s]";
            HashSet<QueryParamNameValuePair> allParams = new LinkedHashSet<>();
            
            if (nameValueMap == null)
            	return allParams;

            // iterate over everything in eg group[...]
            for (Entry<String, ? extends OneDimensionalQueryParam<E>> entry : nameValueMap.entrySet()) {
                String resourceName = entry.getKey();

                if (entry.getValue() == null)
                	continue;
                
                // iterate over everything in group[resouceName]=...
                for (E value : entry.getValue().getParams()) {
                    String name = String.format(paramNameTemplate, resourceName);
                    allParams.add(new QueryParamNameValuePair(name, value.toString()));
                }
            }

            return allParams;
        }    	
    }
    

    public class TwoDimQueryable<E> {

        private Map<String, ? extends TwoDimensionalQueryParam<E>> firstDimMap;

        private String topLevelParamName;

        private TwoDimQueryable(TypedParams<? extends TwoDimensionalQueryParam<E>> param, String topLevelParamName) {
        	if (param != null)
        		this.firstDimMap = param.getParams();
        	
            this.topLevelParamName = topLevelParamName;
        }

        /**
         * Get access to the set of values identified through:
         * @param resourceName
         * @param paramName
         * @return SetWrapper instance that allows you to get one() or all() parameter values
         */
        public SetWrapper<E> get(String resourceName, String paramName) {
        	if (firstDimMap == null)
        		return new SetWrapper<>();
        	
            if (!firstDimMap.containsKey(resourceName))
                return new SetWrapper<>();

            TwoDimensionalQueryParam<E> secondDimension = firstDimMap.get(resourceName);
            if (secondDimension == null)
                return new SetWrapper<>();

            Set<E> values = secondDimension.getParams().get(paramName);
            return new SetWrapper<E>(values);
        }

        /**
         * @return all query parameters and their values that were collected under e.g. filter[...][...]
         */
        public Set<QueryParamNameValuePair> all() {

            String paramNameTemplate = topLevelParamName + "[%s][%s]";
            HashSet<QueryParamNameValuePair> allParams = new LinkedHashSet<>();
            
            if (firstDimMap == null)
            	return allParams;

            // iterate over everything in filter[...]
            for (Map.Entry<String, ? extends TwoDimensionalQueryParam<E>> entry : firstDimMap.entrySet()) {
                String resourceName = entry.getKey();

                TwoDimensionalQueryParam<E> secondDim = entry.getValue();
                if (secondDim == null)
                    continue;

                // iterate over everything in filter[resouceName][...]
                for(Map.Entry<String, Set<E>> secondDimEntry : secondDim.getParams().entrySet()) {
                    String paramName = secondDimEntry.getKey();

                    if (secondDimEntry.getValue() == null)
                        continue;

                    // iterate over everything in filter[resouceName][paramName]=...
                    for (E value : secondDimEntry.getValue()) {
                        String name = String.format(paramNameTemplate, resourceName, paramName);
                        allParams.add(new QueryParamNameValuePair(name, value.toString()));
                    }
                }
            }
            return allParams;
        }

    }

    public class SetWrapper<T>{

        private Set<T> parameterValues;

        private SetWrapper() {}
        
        /**
         * Constructor for cases where only one value may be associated
         * with a certain parameter
         * @param value associated with a certain parameter
         */
        private SetWrapper(T value) {
        	parameterValues = new HashSet<T>();
        	parameterValues.add(value);
        }

        /**
         * Constructor is fine if a null value is passed as argument.
         * The getter methods are null-safe.
         * @param values associated with a certain parameter
         */
        private SetWrapper(Set<T> values) {
            parameterValues = values;
        }

        /**
         * @return first parameter that is found in the set of passed parameters
         * or null if none were passed
         */
        public T one() {
            if (parameterValues == null)
                return null;

            return parameterValues.size() > 0 ? parameterValues.iterator().next() : null;
        }

        /**
         * @return either values that were passed or an empty set, but never null
         */
        public Set<T> all() {
            if (parameterValues == null)
                return new HashSet<>();

            return parameterValues;
        }

    }
}
