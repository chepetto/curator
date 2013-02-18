package org.curator.core.util;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class SeedUtils {

    private static final Logger LOGGER = Logger.getLogger(SeedUtils.class);
    private static final String SEEDS_FILE = "/feeds.xml";
    private static List<String> seeds = null;

    public static Collection<String> getSeeds() {

        if (seeds == null) {
            seeds = new LinkedList<String>();

            SAXBuilder builder = new SAXBuilder(false);
            try {
                LOGGER.trace(String.format("Loading seed file from %s", SeedUtils.class.getResource(SEEDS_FILE)));
                Document dom = builder.build(SeedUtils.class.getResourceAsStream(SEEDS_FILE));

                XPath path = XPath.newInstance("//feed/@url");

                List<?> list = path.selectNodes(dom);
                for (Object obj : list) {
                    Attribute attr = (Attribute) obj;
                    seeds.add(attr.getValue());
                }

            } catch (Exception e) {
                LOGGER.fatal(String.format("Cannot load seed file %s. %s", SEEDS_FILE, e.getMessage()));
                LOGGER.debug(e);
            }
        }
        return seeds;
    }

}
