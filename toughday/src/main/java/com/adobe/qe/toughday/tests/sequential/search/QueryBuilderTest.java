package com.adobe.qe.toughday.tests.sequential.search;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.tests.sequential.SequentialTestBase;

@Description(name = "QueryBuilderTest", desc = "Search that uses the Query Builder Json Rest Api")
public class QueryBuilderTest extends SequentialTestBase {
    private static final String DEFAULT_QUERY = "type=cq:Page&group.1_path=/content&orderby=@jcr:score&orderby.sort=desc";
    private String query;

    public QueryBuilderTest() {
    }

    public QueryBuilderTest(String query) {
        this.query = query;
    }

    @Override
    public void test() throws Exception {
        getDefaultClient().doGet("/bin/querybuilder.json?" + query);
    }

    @Override
    public AbstractTest newInstance() {
        return new QueryBuilderTest(query);
    }

    @ConfigArg(required = false, desc = "Query to be executed by the Query Builder servlet", defaultValue = DEFAULT_QUERY)
    public void setQuery(String query) {
        this.query = query;
    }
}
