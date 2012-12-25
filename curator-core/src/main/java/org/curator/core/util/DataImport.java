package org.curator.core.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.curator.common.model.Content;
import org.curator.core.analysis.TermFrequencyUtils;
import org.curator.core.interfaces.TopicManager;

@LocalBean
@Singleton
public class DataImport implements Serializable {

    private static Logger LOGGER = Logger.getLogger(DataImport.class);
    private static final String WIKI_FILE = "/home/damoeb/dev/curator/tools/dewiki-20121009-pages-articles.xml";
    public static final String SOLR_URL = "http://localhost:8983/solr";

    @Inject
    private TermFrequencyUtils termFrequencyUtils;
    @Inject
    private TopicManager topicManager;

    private void setupFulltextIndex() {

        InputStream in;
        XMLStreamReader parser = null;
        StreamingUpdateSolrServer server = null;

        try {

            in = new FileInputStream(WIKI_FILE);

            server = new StreamingUpdateSolrServer(SOLR_URL, 400, 6);

            XMLInputFactory factory = XMLInputFactory.newInstance();
            parser = factory.createXMLStreamReader(in);

            long id = 1000;
            /*
            title, text
             */
            SolrInputDocument document = null;
            List<SolrInputDocument> documentList = new ArrayList<SolrInputDocument>();

            while (parser.hasNext()) {


                parser.next();

                switch (parser.getEventType()) {

                    case XMLStreamConstants.START_ELEMENT:

                        try {
                            if (parser.getLocalName().equalsIgnoreCase("title") || parser.getLocalName().equalsIgnoreCase("text")) {
                                if (document == null) {
                                    document = new SolrInputDocument();
                                    document.setField("id", id++);
                                    System.out.println(id);
                                }
                                String name = parser.getLocalName();
                                parser.next();
                                String t = _removeWikiMarkup(parser.getText());
                                document.addField(name, t);
                            }
                        } catch (Throwable t) {
                            LOGGER.warn(t);
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        if (parser.getLocalName().equalsIgnoreCase("page")) {
                            documentList.add(document);
                            if(documentList.size()>1000) {
                                server.add(documentList);
                                server.commit();
                                documentList.clear();
                            }
                            document = null;
                        }

                        break;
                }
            }

        } catch (Throwable t) {
            LOGGER.error(t);
            try {
                server.rollback();
            } catch (Exception e) {
                //
            }

        } finally {

            if (parser != null) {
                try {
                    parser.close();
                } catch (XMLStreamException e) {
                    //
                }
            }
            if (server != null) {
                try {
                    server.commit();
                } catch (Exception e) {
                    //
                }
            }
        }
    }

    private static Pattern p1 = Pattern.compile("[='-]{2,}");
    private static Pattern p2 = Pattern.compile("^[^a-zA-Z]+");
    private static Pattern p3 = Pattern.compile("[^a-zA-Z0-9]+$");

    private static String _removeWikiMarkup(String token) {
        token = p1.matcher(token).replaceAll("");// token.replaceAll("[='-]{2,}",
        // "").trim();
        token = p2.matcher(token).replaceFirst("");// token.replaceFirst("^[^a-zA-Z]+",
        // "");
        token = p3.matcher(token).replaceFirst("");
        return token;
    }

    public static void main(String[] args) throws Exception {

        DataImport ws = new DataImport();
        //ws.setupFulltextIndex();
        ws.setupRelatedTermsIndex();

    }

    public void setupRelatedTermsIndex() {

        LOGGER.trace("find related terms");

        InputStream in;
        XMLStreamReader parser = null;
        StreamingUpdateSolrServer server = null;

        try {

            in = new FileInputStream(WIKI_FILE);

            server = new StreamingUpdateSolrServer(SOLR_URL, 400, 6);

            XMLInputFactory factory = XMLInputFactory.newInstance();
            parser = factory.createXMLStreamReader(in);

            /*
            title, text
             */
            SolrInputDocument document = null;

            boolean read = true;
            int docsToProcess = 1;

            while (read && parser.hasNext()) {

                parser.next();

                switch (parser.getEventType()) {

                    case XMLStreamConstants.START_ELEMENT:

                        try {
                            if (parser.getLocalName().equalsIgnoreCase("title") || parser.getLocalName().equalsIgnoreCase("text")) {
                                if (document == null) {
                                    document = new SolrInputDocument();
                                }
                                String name = parser.getLocalName();
                                parser.next();
                                String t = _removeWikiMarkup(parser.getText());
                                document.addField(name, t);
                            }
                        } catch (Throwable t) {
                            LOGGER.warn(t);
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        if (parser.getLocalName().equalsIgnoreCase("page")) {

                            // todo skip redirects

                            if(!((String)document.get("title").getValue()).startsWith("Liste")) {
                                // get text
                                String title = (String) document.get("title").getValue();
                                LOGGER.trace("import wiki-article "+title);

                                String text = title + " " + document.get("text").getValue();

                                // get related terms
                                Set<String> relevantTerms = termFrequencyUtils.getRelevantTerms(new Content(text));
                                topicManager.buildRelations(relevantTerms);

                                //todo remove
//                                if(--docsToProcess<=0) {
//                                    read = false;
//                                }
                            }


                            document = null;
                        }

                        break;
                }
            }

        } catch (Throwable t) {
            LOGGER.error("related terms setup failed", t);

        } finally {

            if (parser != null) {
                try {
                    parser.close();
                } catch (XMLStreamException e) {
                    //
                }
            }
            if (server != null) {
                try {
                    server.commit();
                } catch (Exception e) {
                    //
                }
            }
        }
    }
}