package org.curator.core.crawler;

import java.util.concurrent.Future;

import org.curator.core.crawler.impl.ComplexHarvestInstruction;
import org.curator.core.crawler.impl.CrawlerResult;
import org.curator.core.crawler.impl.HarvestInstruction;

public interface Harvester {

    void shutdown();

    Future<CrawlerResult> submit(HarvestInstruction instruction);

    void schedule(HarvestInstruction instruction);

}
