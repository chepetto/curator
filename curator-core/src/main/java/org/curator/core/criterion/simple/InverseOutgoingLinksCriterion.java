package org.curator.core.criterion.simple;

import org.apache.log4j.Logger;
import org.curator.common.exceptions.CuratorException;
import org.curator.core.criterion.AbstractSimpleCriterion;
import org.curator.core.criterion.Goal;
import org.curator.core.criterion.Performance;
import org.curator.core.criterion.SinglePerformance;
import org.curator.core.model.Article;
import org.curator.core.model.MetricName;
import org.curator.core.model.MetricProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@LocalBean
@Stateless
public class InverseOutgoingLinksCriterion extends AbstractSimpleCriterion implements MetricProvider {

    private static final transient Logger LOGGER = Logger.getLogger(InverseOutgoingLinksCriterion.class);

    @Override
    public Performance evalCriterion(final Article source, final Goal goal) throws CuratorException {

        // -- ASSERTIONS -- --------------------------------------------------------------------------------------------
        if (source == null) {
            throw new IllegalArgumentException("source is null");
        }
        if (source.getContent() == null || source.getContent().getHtml() == null) {
            return null;
        }
        if (!source.hasMetricResult(MetricName.OUTGOING_LINK_COUNT)) {
            LOGGER.warn("Too few metric results: " + MetricName.OUTGOING_LINK_COUNT);
            return null;
        }
        // -- ----------------------------------------------------------------------------------------------------------

        final Double linkCount = source.getMetricResult(MetricName.OUTGOING_LINK_COUNT);
        double result;
        if (linkCount > 0) {
            result = 1d / Math.sqrt(linkCount);
        } else {
            result = 0d;
        }
        LOGGER.trace("eval " + name() + ": " + result);
        return new SinglePerformance(this, result);
    }

    @Override
    public void pushMetricResults(Article article) throws CuratorException {
        Double linkCount;
        try {
            linkCount = countLinks(groupByDomain(findLinks(article)));
        } catch (Throwable t) {
            linkCount = 0d;
        }
        article.addMetricResult(MetricName.OUTGOING_LINK_COUNT, linkCount);
    }

    private Map<String, Integer> groupByDomain(List<String> links) {
        Map<String, Integer> groupCountMap = new HashMap<String, Integer>(links.size());

        // todo work with url object, use getHost e.g.
        for (String link : links) {

            int endIndex = link.indexOf('/', 8);
            final String domain = link.substring(0, endIndex > 0 ? endIndex : link.length());

            if (groupCountMap.containsKey(domain)) {
                groupCountMap.put(domain, groupCountMap.get(domain) + 1);
            } else {
                groupCountMap.put(domain, 1);
            }
        }

        return groupCountMap;
    }

    private Double countLinks(Map<String, Integer> links) {
        Double linkCount = 0d;

        for (String link : links.keySet()) {
            linkCount += 1d / links.get(link);
        }

        return linkCount;
    }

    private List<String> findLinks(Article source) {


        if (source.getContent() == null) {
            throw new IllegalArgumentException("no content available");
        }

        Document soup = Jsoup.parse(source.getContent().getHtml());
        if (soup == null) {
            throw new IllegalArgumentException("no markup in content available");
        }

        List<String> links = new LinkedList<String>();
        for (Element a : soup.select("a[href]")) {
            links.add(a.attr("href"));
        }
        return links;
    }

    @Override
    public String name() {
        return "InverseOutgoingLinksCriterion";
    }
}
