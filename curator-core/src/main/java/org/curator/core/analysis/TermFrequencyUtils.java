package org.curator.core.analysis;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.curator.common.configuration.Configuration;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.*;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Pattern;

@LocalBean
@Stateless
public class TermFrequencyUtils {

    private static final Logger LOGGER = Logger.getLogger(TermFrequencyUtils.class);

    private static final double MIN_TFIDF_SCORE = 60d;
    private static final double MIN_MUTUAL_INFORMATION_SCORE = -5d;

    private CommonsHttpSolrServer server;

    private static final Set<String> STOPWORDS = new HashSet<String>(300) {{

        addAll(Arrays.asList("die", "und", "der", "das", "es", "in", "ist", "zu", "den", "von", "ein", "auf",
                "nicht", "ich", "auch", "eine", "mit", "das", "als", "aber", "oder", "sich", "dass", "sie", "man", "dem", "im", "des", "wie", "noch",
                "aus", "so", "an", "bei", "was", "für", "nur", "er", "wir", "wenn", "vor", "sind", "denn", "werden", "wird", "einer", "um", "hat",
                "einen", "mehr", "diese", "einem", "nach", "über", "zur", "dann", "zum", "haben", "kann", "durch", "da", "am", "keine", "doch",
                "war", "uns", "bis", "vom", "ihr", "ihre", "ihres", "ihrer", "immer", "sein", "seine", "schon", "gibt", "wurde", "habe", "ja",
                "weil", "geht", "also", "dieser", "viel", "mir", "du", "viele", "jetzt", "wieder", "damit", "unter", "sehr", "muss", "mich", "hab",
                "alle", "waren", "ob", "dazu", "gar", "oft", "würde", "könnte", "diesem", "erst", "weniger", "sondern", "bin", "the", "this", "wäre",
                "wer", "de", "nichts", "während", "kommen", "at", "heißt", "sagen", "seit", "eines", "daran", "wo", "sieht", "dieses", "große",
                "stehen", "brauchen", "halten", "soll", "letzten", "mal", "macht", "sowie", "würden", "sonst", "wirklich", "weiß", "findet",
                "erreichen", "andere", "einmal", "lesen", "kein", "ab", "nämlich", "kein", "ab", "dabei", "warum", "sagt", "beim", "groß", "leisten",
                "darf", "nachdem", "liegt", "wenig", "sowieso", "kaum", "kleinen", "fand", "unsere", "eher", "bereits", "alles", "möglich", "statt",
                "eigentlich", "tatsächlich", "passiert", "zwischen", "wollen", "davon", "eigenen", "deiner", "lauf", "dürften",
                // wiki and html words
                "nbsp", "overline", "rightarrow", "dort", "hin", "sei", "ihrem", "ihren", "keiner", "zwar"));
    }};

    //private static final Pattern VALID_WORD = Pattern.compile("^[a-zäöü]+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALID_WORD = Pattern.compile("^[A-ZÄÖÜ]+[a-zäöü]*$");

    private Long totalDocumentCount = null;
    private static final Comparator<? super Term> COMPARATOR_BEST_SCORE_FIRST = new Comparator<Term>() {
        @Override
        public int compare(Term t1, Term t2) {
            Double f1 = t1.getScore();
            Double f2 = t2.getScore();
            if (f1.equals(f2)) return -1;
            return f2.compareTo(f1);
        }
    };

    public static class Term {

        private String value;
        private Double score;
        private Double probability;

        public Term(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setValue(String value) {
            this.value = value;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public Double getProbability() {
            return probability;
        }

        public void setProbability(Double probability) {
            this.probability = probability;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @PostConstruct
    public void onInit() {

        if(server==null) {
            final String url = Configuration.getStringValue("solr.url", "http://localhost:8983/solr");
    //        final String url = Configuration.getValueProvidingDefaultValue("solr.url", "http://localhost:8983/solr");
            LOGGER.info("Property solr.url= "+url);

            try {
                server = new CommonsHttpSolrServer(url);

                server.setSoTimeout(5000);  // socket read timeout
                server.setConnectionTimeout(100);
                server.setDefaultMaxConnectionsPerHost(100);
                server.setMaxTotalConnections(100);
                server.setFollowRedirects(false);  // defaults to false
                // allowCompression defaults to false.
                // Server side must support gzip or deflate for this to have any effect.
                server.setAllowCompression(true);
                server.setMaxRetries(1);

            } catch (MalformedURLException e) {
                LOGGER.error("Cannot init solr client", e);
            }
        }
    }

    /**
     * Creates Sets of Terms that are related and significant for the provided content.
     * @param content the content
     * @return the related sets
     * @throws CuratorException
     */
    public Set<String> getRelevantTerms(Content content) throws CuratorException {

        SortedSet<Term> terms = _getRelevanceScoredTerms(content);

        List<Term> relevantTerms = new ArrayList<Term>(10);

        // filter unrelevant terms
        for (TermFrequencyUtils.Term term : terms) {
            if (term.getScore() < MIN_TFIDF_SCORE) {
                // terms ordered by score, desc
                break;
            }
            relevantTerms.add(term);
        }

        LOGGER.trace(terms.size() - relevantTerms.size() + " non-significant terms dropped");

        Set<String> relatedTerms = new HashSet<String>(20);

//        for (int i = 0; i < relevantTerms.size(); i++) {
//            Term termA = relevantTerms.get(i);
//            for (int j = i + 1; j < relevantTerms.size(); j++) {
//                Term termB = relevantTerms.get(j);
//
//                double mip = getMututalInformation(termA, termB);
//
//                if (mip > MIN_MUTUAL_INFORMATION_SCORE) {
//
//                }
//            }
//        }

        for (TermFrequencyUtils.Term term : relevantTerms) {
            relatedTerms.add(term.getValue());
        }

        LOGGER.info(String.format("%s related terms: %s", relatedTerms.size(), relatedTerms));

        return relatedTerms;

    }

    /**
     * Mutual information is defined as follows:
     * I(w1,w2) = log(P(w1,w2)/P(w1)*P(w2))
     * see paper "A New Measure for Extracting Semantically Related Words"
     * @param w1 Term A
     * @param w2 Term B
     * @return
     */
    private double getMututalInformation(Term w1, Term w2) {

        double pW1w2 = _getMutualOccurrenceProbability(w1, w2);
        double pW1 = w1.getProbability();
        double pW2 = w2.getProbability();

        if(pW1w2==0) {
            return 0;
        }

        return Math.log(pW1w2/Math.max(1, pW1 * pW2));
    }

    private double _getMutualOccurrenceProbability(Term termA, Term termB) {
        SolrQuery q = new SolrQuery("text:" + termA.getValue()+" AND text:"+termB.getValue());
        q.setRows(0);
        try {
            double resultCount = server.query(q).getResults().getNumFound();
            return resultCount/_getDocumentCount();
        } catch (SolrServerException e) {
            LOGGER.error("_getMutualOccurrenceProbability", e);
            return 0;
        }
    }

    /**
     * Calculates the relevance-score of terms inside the content usind TF-IDF.
     * @param content the content
     * @return the scored terms
     * @throws CuratorException
     */
    private SortedSet<Term> _getRelevanceScoredTerms(Content content) throws CuratorException {

        final Map<String, Integer> tf = new HashMap<String, Integer>(500);

        _getTermFrequency(content, 1, tf);

        int termCount = tf.size();
        LOGGER.trace(termCount+" terms extracted");

        _filterTerms(tf);
        LOGGER.trace((termCount-tf.size())+" terms filtered");

        return _retrieveTermFrequency(tf);
    }

    private void _filterTerms(Map<String, Integer> tf) {
        HashMap<String, Integer> filtered = new HashMap<String, Integer>(tf.size());

        for (String key : tf.keySet()) {
            if (key.length() < 4) continue;
            if (NumberUtils.isNumber(key)) continue;
            if (STOPWORDS.contains(key)) continue;
            if (!VALID_WORD.matcher(key).find()) continue;

            filtered.put(key, tf.get(key));
        }

        tf.clear();
        tf.putAll(filtered);
    }

    public SortedSet<Term> tfidf(Article article) throws CuratorException {
        final Map<String, Integer> tf = new HashMap<String, Integer>(1000);

        _getTermFrequency(article.getContent(), 1, tf);
//        if(!StringUtils.isBlank(article.getDescription())) {
//            _getTermFrequency(new Content(article.getDescription()), 2, tf);
//        }
        if(!StringUtils.isBlank(article.getTitle())) {
            _getTermFrequency(new Content(article.getTitle()), 3, tf);
        }
        return _retrieveTermFrequency(tf);
    }

    private SortedSet<Term> _retrieveTermFrequency(Map<String, Integer> tf) throws CuratorException {
        try {
            final long docsTotalCount = _getDocumentCount();

            final SortedSet<Term> scoredTerms = new TreeSet<Term>(COMPARATOR_BEST_SCORE_FIRST);

            LOGGER.trace("retrieving frequency of "+tf.size()+" terms using solr");



            long startTime = System.currentTimeMillis();

            for (String value : tf.keySet()) {
                try {
                    value = value.trim();

                    LOGGER.trace("frequency of " + value);

                    SolrQuery q = new SolrQuery("text:" + value);
                    q.setRows(0);
                    long docsContainingTerm = server.query(q).getResults().getNumFound();

                    Term term = new Term(value);
                    term.setScore(tf.get(value) * idf(docsTotalCount, docsContainingTerm));
                    double probabilityOfOccurrence = ((double) docsContainingTerm) / docsTotalCount;
                    term.setProbability(probabilityOfOccurrence);

                    if(probabilityOfOccurrence==0) {
                        LOGGER.warn("Ignoring '"+value+"' cause occurrence is 0");
                        continue;
                    }

                    scoredTerms.add(term);

                } catch (SolrServerException e) {
                    LOGGER.error(String.format("Cannot process term '%s'", value), e);
                }
            }

            LOGGER.trace("frequency resolved in "+Math.floor((System.currentTimeMillis()-startTime)/1000d)+"s");

            tf.clear();

            return scoredTerms;

        } catch (SolrServerException e) {
            throw new CuratorException("Cannot retrieve term frequency: "+e.getMessage(), e);
        }
    }

    private long _getDocumentCount() throws SolrServerException {

        if(totalDocumentCount==null) {
            SolrQuery q = new SolrQuery("*:*");
            q.setRows(0);  // don't actually request any data
            totalDocumentCount = server.query(q).getResults().getNumFound();
        }
        return totalDocumentCount;
    }

    private double idf(long docsTotalCount, long docsContainingTerm) {

//    private double idf(String term, long docsTotalCount) throws SolrServerException {
//
//        SolrQuery q = new SolrQuery("text:" + term);
//        q.setRows(0);
//        long docsContainingTerm = server.query(q).getResults().getNumFound();

        return Math.log(docsTotalCount / Math.max(1, docsContainingTerm));
    }

    /**
     * @param content the text
     * @param boost   the importance of this text from 1,2,..
     * @param tf      result map
     */
    private void _getTermFrequency(Content content, int boost, Map<String, Integer> tf) {

        if(content == null) {
            return;
        }
        //GermanStemmer stemmer = new GermanStemmer();
        for (Sentence s : content.getSentences()) {
            for (Word w : s.getWords()) {

                String val = w.getValue();
                if(val.length()<3
                        || STOPWORDS.contains(val.toLowerCase())
                        || val.toLowerCase().equals(val)) {
                    LOGGER.trace("Dropping word "+val);
                    continue;
                }

//                term = stemmer.stem(term);

                if (!tf.containsKey(val)) {
                    tf.put(val, 0);
                }

                tf.put(val, tf.get(val) + boost);
            }
        }
    }

}
