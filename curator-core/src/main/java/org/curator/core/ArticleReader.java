package org.curator.core;

import org.apache.log4j.Logger;
import org.curator.common.exceptions.CuratorException;
import org.curator.core.crawler.Harvester;
import org.curator.core.criterion.Goal;
import org.curator.core.eval.Evaluation;
import org.curator.core.eval.impl.ArticleEvaluator;
import org.curator.core.model.Article;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ArticleReader {

    private static final Logger LOGGER = Logger.getLogger(ArticleReader.class);

    @Inject
    private Harvester harvester;

    private Evaluation evaluate(ArticleEvaluator evaluator, Article toEvaluate, Goal goal) throws CuratorException {
        try {
            return evaluator.evaluate(toEvaluate, goal);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage());
            throw new CuratorException("", t);
        }
    }

    public static void main(final String[] args) throws Exception {

//        final ApplicationContext context = new ClassPathXmlApplicationContext("application.xml");
//
//        ArticleReader r = context.getBean(ArticleReader.class);
//
//        StringBuilder b = new StringBuilder(10000);
//        Scanner in = new Scanner(new FileInputStream("/home/damoeb/Downloads/diss10"));
//        while(in.hasNextLine()) {
//            b.append(in.nextLine());
//        }
//
//        // load spring context
//        final ApplicationContext context = new ClassPathXmlApplicationContext("application.xml");
//
//
//        try {
//            ArticleEvaluator evaluator = context.getBean(ArticleEvaluator.class);
//
//            ArticleReader r = context.getBean(ArticleReader.class);
//            Evaluation e = r.evaluate(evaluator, new Article(b.toString()), Goal.MODEST_TEXT);
//            System.out.println(e.quality());
//        } catch (CuratorException e) {
//            e.printStackTrace();
//        }
    }

}
