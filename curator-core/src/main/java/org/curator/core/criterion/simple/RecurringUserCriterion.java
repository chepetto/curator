package org.curator.core.criterion.simple;

import org.apache.commons.lang.StringUtils;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Article;
import org.curator.common.model.Comment;
import org.curator.common.model.MetricName;
import org.curator.common.model.MetricProvider;
import org.curator.core.criterion.*;
import org.apache.log4j.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@LocalBean
@Stateless
public class RecurringUserCriterion extends AbstractSimpleCriterion implements MetricProvider {

    private static final Logger LOGGER = Logger.getLogger(RecurringUserCriterion.class);

    @Override
    public Performance evalCriterion(final Article source, final Goal goal) throws CuratorException {

        List<Comment> comments;

        // -- ASSERTIONS -- --------------------------------------------------------------------------------------------
        if (source == null) {
            throw new CuratorException("source is null");
        }

        comments = source.getComments();
        if (comments == null) {
            return null;
        }
        if(!source.hasMetricResult(MetricName.UNIQUE_USER_COUNT) || !source.hasMetricResult(MetricName.MENTIONED_USER_COUNT)) {
            LOGGER.warn(String.format("Too few metric results: at least one of %s is missing",
                    StringUtils.join(new MetricName[]{MetricName.UNIQUE_USER_COUNT, MetricName.MENTIONED_USER_COUNT}, ", ")
            ));
            return null;
        }
        // -- ----------------------------------------------------------------------------------------------------------

        if (comments.isEmpty()) {
            return new SinglePerformance(this, 0d);
        }

        final int commentCount = comments.size();

        Double uniqueUserCount = source.getMetricResult(MetricName.UNIQUE_USER_COUNT);
        double recurringFactor = uniqueUserCount == commentCount ? 0 : (commentCount - uniqueUserCount) / ((double) commentCount);
        LOGGER.trace("eval " + MetricName.RECURRING_FACTOR + ": " + recurringFactor);

        // 10% mentions is max
        Double mentionedUserCount = source.getMetricResult(MetricName.MENTIONED_USER_COUNT);
//        LOGGER.trace("eval " + MetricName.MENTIONED_USER_COUNT + ": " + mentionedUserCount);
        double mentionsFactor = mentionedUserCount > (commentCount / 10d) ? 1 : mentionedUserCount / (commentCount / 10d);

        return new SinglePerformance(this, (recurringFactor + mentionsFactor) / 2d);
    }

    @Override
    public void pushMetricResults(Article article) throws CuratorException {
        double uniqueUserCount = getUniqueUserCount(article.getComments());
        article.addMetricResult(MetricName.UNIQUE_USER_COUNT, uniqueUserCount);
        LOGGER.trace("eval " + MetricName.UNIQUE_USER_COUNT + ": " + uniqueUserCount);

        double mentionedUserCount = getUserMentionsCount(article.getComments());
        article.addMetricResult(MetricName.MENTIONED_USER_COUNT, mentionedUserCount);
        LOGGER.trace("eval " + MetricName.MENTIONED_USER_COUNT + ": " + mentionedUserCount);

    }

    private int getUserMentionsCount(List<Comment> comments) {
        if(comments==null) {
            return 0;
        }

        int mentions = 0;

        final Pattern mention = Pattern.compile("@[^ ]{4,}");

        for (Comment comment : comments) {
            Matcher matcher = mention.matcher(comment.getText());
            while (matcher.find()) {
                mentions++;
            }
        }

        return mentions;
    }

    private int getUniqueUserCount(final List<Comment> comments) {
        if(comments==null) {
            return 0;
        }
        final Set<String> users = new HashSet<String>(comments.size());
        for (Comment comment : comments) {
            users.add(comment.author());
        }
        return users.size();
    }

    @Override
    public String name() {
        return "RecurringUserCriterion";
    }

}
