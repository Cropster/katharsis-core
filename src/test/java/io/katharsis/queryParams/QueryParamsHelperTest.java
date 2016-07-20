package io.katharsis.queryParams;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.QueryParamNameValuePair;
import io.katharsis.queryParams.params.QueryParamsHelper;

public class QueryParamsHelperTest {
	
    private Map<String, Set<String>> paramDataMap;
    private QueryParamsParser parser = new DefaultQueryParamsParser();

    @Before
    public void prepare() {
        paramDataMap = new HashMap<>();
    }
	
    @Test
    public void testNullPointerSafeness() {
    	QueryParams params = new QueryParams();
        QueryParamsHelper qpHelper = new QueryParamsHelper(params);
        
        assertEquals(0, qpHelper.filters().all().size());
        assertEquals(0, qpHelper.filters().get("doesnt", "matter").all().size());
        assertNull(qpHelper.filters().get("doesnt", "matter").one());
        
        assertEquals(0, qpHelper.sortings().all().size());
        assertEquals(0, qpHelper.sortings().get("doesnt", "matter").all().size());
        assertNull(qpHelper.sortings().get("doesnt", "matter").one());
        
        assertEquals(0, qpHelper.groupings().all().size());
        assertEquals(0, qpHelper.groupings().get("igual").all().size());
        assertNull(qpHelper.groupings().get("igual").one());
        
        assertEquals(0, qpHelper.inclusions().all().size());
        assertEquals(0, qpHelper.inclusions().get("igual").all().size());
        assertNull(qpHelper.inclusions().get("igual").one());
        
        assertEquals(0, qpHelper.fields().all().size());
        assertEquals(0, qpHelper.fields().get("igual").all().size());
        assertNull(qpHelper.fields().get("igual").one());
        
        assertNull(qpHelper.page().size());
        assertNull(qpHelper.page().limit());
        assertNull(qpHelper.page().number());
        assertNull(qpHelper.page().offset());
    }
    
	@Test
	public void testQueryParamsHelperFilters() {
        paramDataMap.put("filter[users][name]", Collections.singleton("John"));
        paramDataMap.put("random[users][name]", Collections.singleton("John"));

        QueryParams params = new QueryParams();
        QueryParamsHelper qpHelper = new QueryParamsHelper(params);
        
        params.setFilters(parser.parseFiltersParameters(paramDataMap));
        assertEquals(1, qpHelper.filters().all().size());      
        
        paramDataMap.put("filter[users][name]", new HashSet<String>(Arrays.asList(new String[]{"John", "John John"})));
        params.setFilters(parser.parseFiltersParameters(paramDataMap));
        Set<QueryParamNameValuePair> filters = qpHelper.filters().all();
        assertEquals(2, filters.size());
        
        assertTrue(filters.contains(new QueryParamNameValuePair("filter[users][name]", "John")));
        assertTrue(filters.contains(new QueryParamNameValuePair("filter[users][name]", "John John")));
        
        Set<String> values = qpHelper.filters().get("users", "name").all();
        assertEquals(2, values.size());
        assertTrue(values.contains("John"));
        assertTrue(values.contains("John John"));
        
        String value = QueryParamsHelper.with(params).filters().get("users", "name").one();
        assertEquals(value, "John");
	}
	
	@Test
	public void testQueryParamsHelperGrouping()
	{
        paramDataMap.put("group[users]", Collections.singleton("name"));

        QueryParams params = new QueryParams();
        QueryParamsHelper qpHelper = new QueryParamsHelper(params);
        
        params.setGrouping(parser.parseGroupingParameters(paramDataMap));
        assertEquals(1, qpHelper.groupings().all().size());      
        
        paramDataMap.put("group[users]", new HashSet<String>(Arrays.asList(new String[]{"name", "age"})));
        params.setGrouping(parser.parseGroupingParameters(paramDataMap));
        Set<QueryParamNameValuePair> groupings = qpHelper.groupings().all();
        assertEquals(2, groupings.size());
        
        assertTrue(groupings.contains(new QueryParamNameValuePair("group[users]", "name")));
        assertTrue(groupings.contains(new QueryParamNameValuePair("group[users]", "age")));
        
        Set<String> values = qpHelper.groupings().get("users").all();
        assertEquals(2, values.size());
        assertTrue(values.contains("name"));
        assertTrue(values.contains("age"));
        
        String value = QueryParamsHelper.with(params).groupings().get("users").one();
        assertEquals(value, "name");
	}
	
	@Test
	public void testQueryParamsHelperFields()
	{
        paramDataMap.put("fields[users]", Collections.singleton("name"));

        QueryParams params = new QueryParams();
        QueryParamsHelper qpHelper = new QueryParamsHelper(params);
        
        params.setIncludedFields(parser.parseIncludedFieldsParameters(paramDataMap));
        assertEquals(1, qpHelper.fields().all().size());      
        
        paramDataMap.put("fields[users]", new HashSet<String>(Arrays.asList(new String[]{"name", "age"})));
        params.setIncludedFields(parser.parseIncludedFieldsParameters(paramDataMap));
        Set<QueryParamNameValuePair> fields = qpHelper.fields().all();
        assertEquals(2, fields.size());
        
        assertTrue(fields.contains(new QueryParamNameValuePair("fields[users]", "name")));
        assertTrue(fields.contains(new QueryParamNameValuePair("fields[users]", "age")));
        
        Set<String> values = qpHelper.fields().get("users").all();
        assertEquals(2, values.size());
        assertTrue(values.contains("name"));
        assertTrue(values.contains("age"));
        
        String value = QueryParamsHelper.with(params).fields().get("users").one();
        assertEquals(value, "name");
	}
	
	@Test
	public void testQueryParamsHelperInclusions()
	{
        paramDataMap.put("include[projects]", Collections.singleton("tasks"));

        QueryParams params = new QueryParams();
        QueryParamsHelper qpHelper = new QueryParamsHelper(params);
        
        params.setIncludedRelations(parser.parseIncludedRelationsParameters(paramDataMap));
        assertEquals(1, qpHelper.inclusions().all().size());
        
        paramDataMap.put("include[projects]", new HashSet<String>(Arrays.asList(new String[]{"tasks", "owner"})));
        params.setIncludedRelations(parser.parseIncludedRelationsParameters(paramDataMap));
        Set<QueryParamNameValuePair> fields = qpHelper.inclusions().all();
        assertEquals(2, fields.size());
        
        assertTrue(fields.contains(new QueryParamNameValuePair("include[projects]", "tasks")));
        assertTrue(fields.contains(new QueryParamNameValuePair("include[projects]", "owner")));
        
        Set<Inclusion> values = qpHelper.inclusions().get("projects").all();
        assertEquals(2, values.size());
        assertTrue(values.contains(new Inclusion("tasks")));
        assertTrue(values.contains(new Inclusion("owner")));
        
        Inclusion value = QueryParamsHelper.with(params).inclusions().get("projects").one();
        
        // set gives no guarantee about order of elements, so in this case we end up
        // getting the owner, not the tasks value
        assertEquals(value, new Inclusion("owner"));
	}
	
	@Test
	public void testQueryParamsHelperSorting()
	{
        paramDataMap.put("sort[projects][name]", Collections.singleton("asc"));

        QueryParams params = new QueryParams();
        QueryParamsHelper qpHelper = new QueryParamsHelper(params);
        
        params.setSorting(parser.parseSortingParameters(paramDataMap));
        assertEquals(1, qpHelper.sortings().all().size());      
        
        // we may only get one value for sorting, even if we received two values
        paramDataMap.put("sort[projects][name]", new HashSet<String>(Arrays.asList(new String[]{"asc", "desc"})));
        params.setSorting(parser.parseSortingParameters(paramDataMap));
        Set<QueryParamNameValuePair> sortings = qpHelper.sortings().all();
        assertEquals(1, sortings.size()); // note it says 1!
        
        assertTrue(sortings.contains(new QueryParamNameValuePair("sort[projects][name]", "asc")));
        
        Set<RestrictedSortingValues> values = qpHelper.sortings().get("projects", "name").all();
        assertEquals(1, values.size());
        assertTrue(values.contains(RestrictedSortingValues.asc));
        
        RestrictedSortingValues value = QueryParamsHelper.with(params).sortings().get("projects", "name").one();
        assertEquals(value, RestrictedSortingValues.asc);
	}
	
	@Test
	public void testQueryParamsHelperPagination()
	{
        paramDataMap.put("page[size]", Collections.singleton("12"));
        paramDataMap.put("page[number]", Collections.singleton("11"));
        paramDataMap.put("page[offset]", Collections.singleton("3"));
        paramDataMap.put("page[limit]", Collections.singleton("8"));

        QueryParams params = new QueryParams();
        params.setPagination(parser.parsePaginationParameters(paramDataMap));
        QueryParamsHelper qpHelper = new QueryParamsHelper(params);
        
        assertEquals(12, (int) qpHelper.page().size());
        assertEquals(11, (int) qpHelper.page().number());
        assertEquals(3, (int) qpHelper.page().offset());
        assertEquals(8, (int) qpHelper.page().limit());
        
        assertEquals(4, qpHelper.page().all().size());
	}
	
}
