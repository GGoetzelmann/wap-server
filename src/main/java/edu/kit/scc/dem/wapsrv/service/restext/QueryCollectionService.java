package edu.kit.scc.dem.wapsrv.service.restext;

import edu.kit.scc.dem.wapsrv.model.Page;
import org.springframework.data.util.Pair;

import java.util.Map;


public interface QueryCollectionService {

    enum MatchType {
        EXACT,
        CONTAINS
    }

    Page getPage(Map<String, Pair<String,MatchType>> propertyMap);
}
