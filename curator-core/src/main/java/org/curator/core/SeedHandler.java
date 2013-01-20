package org.curator.core;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.curator.common.configuration.Configuration;
import org.curator.core.crawler.Harvester;
import org.curator.core.crawler.impl.ComplexHarvestInstruction;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import java.io.FileReader;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@LocalBean
@Singleton
//@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SeedHandler {

    private static final Logger LOGGER = Logger.getLogger(SeedHandler.class);

    @Inject
    private Harvester harvester;

    private boolean available = true;

    @Resource(shareable = false)
    private TimerService timerService;

    //@Schedule(persistent = false, minute = "0", second = "0", hour = "*/12")
    public void watchSeeds() {
        try {
            if (available) {
                timerService.createSingleActionTimer(new Date(), new TimerConfig());
            }
        } catch (Throwable t) {
            LOGGER.error("Cannot import seeds: " + t.getMessage());
        }
    }

    @Timeout
    public void importSeeds() {

        boolean retrieve = Configuration.getBooleanValue(Configuration.RETRIEVE_SEEDS);

        if (retrieve && available) {

            LOGGER.trace("import seeds");

            available = false;

            //_callComplexImports();

            available = true;
        }
    }


    private void _callComplexImports() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ComplexHarvestInstruction[] instructions = mapper.readValue(new FileReader("/home/damoeb/dev/curator/curator-core/src/main/resources/seeds.json"), ComplexHarvestInstruction[].class);

            LOGGER.trace("Importing " + instructions.length + " seeds");

            Set<String> instructionIds = new HashSet<String>(100);
            for (ComplexHarvestInstruction template : instructions) {

                LOGGER.trace("seed " + template.getId() + " roots: " + StringUtils.join(template.getRoots(), ", "));

                if (instructionIds.contains(template.getId())) {
                    LOGGER.warn("Duplicate instruction-id '" + template.getId() + "'");
                    continue;
                }
                instructionIds.add(template.getId());

                for (String root : template.getRoots()) {

                    try {
                        ComplexHarvestInstruction instruction = new ComplexHarvestInstruction(template);
                        instruction.setUrl(new URL(root));

                        harvester.schedule(instruction);

                    } catch (Throwable t) {
                        LOGGER.error("Failed to schedule instruction for " + root, t);
                    }
                }
            }
        } catch (Throwable t) {
            LOGGER.error(t);
        }
    }

}
