package com.adobe.qe.toughday.tests.sequential.search;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.annotations.Tag;
import com.adobe.qe.toughday.api.annotations.ConfigArgGet;
import com.adobe.qe.toughday.api.annotations.ConfigArgSet;
import com.adobe.qe.toughday.tests.sequential.AEMTestBase;

@Tag(tags = { "author" })
@Description(desc = "Search that uses the Query Builder Json Rest Api")
public class QueryBuilderTest extends AEMTestBase {
    private static final String DEFAULT_QUERY = "type=cq:Page&group.1_path=/content&orderby=@jcr:score&orderby.sort=desc";
    private String query = DEFAULT_QUERY;

    public QueryBuilderTest() {
    }

    public QueryBuilderTest(String query) {
        this.query = query;
    }

    @Override
    public void test() throws Throwable {
        benchmark().measure(this, "RunQuery", getDefaultClient()).doGet("/bin/querybuilder.json?" + query);
    }

    @Override
    public AbstractTest newInstance() {
        return new QueryBuilderTest(query);
    }

    @ConfigArgSet(required = false, desc = "Query to be executed by the Query Builder servlet", defaultValue = DEFAULT_QUERY)
    public QueryBuilderTest setQuery(String query) {
        this.query = query;
        return this;
    }

    @ConfigArgGet
    public String getQuery() {
        return this.query;
    }
}