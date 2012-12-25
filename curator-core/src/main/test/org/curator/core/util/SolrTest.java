package org.curator.core.util;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;

public class SolrTest {


    private StreamingUpdateSolrServer server;

    @Before
    public void onInit() throws MalformedURLException {
        server = new StreamingUpdateSolrServer(DataImport.SOLR_URL, 400, 6);
    }

    @Test
    public void testQuery() throws SolrServerException {


        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("*", "*");

        SolrQuery q = new SolrQuery("*:*");

        QueryResponse response = server.query(q);

    }
}
