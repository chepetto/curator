package org.curator.core.analysis;

import java.util.List;

public interface Analyzable {

    void addAnalyzer(Analyzer analyzer);

    List<Analyzer> getAnalyzer();

}
