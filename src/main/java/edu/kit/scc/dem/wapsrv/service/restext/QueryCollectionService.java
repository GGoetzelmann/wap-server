package edu.kit.scc.dem.wapsrv.service.restext;

import edu.kit.scc.dem.wapsrv.model.Page;

import java.util.Map;


public interface QueryCollectionService {

    Page getPage(Map<String, String> propertyMap);
}
