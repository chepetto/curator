package org.curator.core.dao;

public class HostelIdGenerator extends RandomizedSequenceGenerator {

    public HostelIdGenerator() {
        setSuffixLength(4);
    }
}
