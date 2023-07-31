package edu.kit.scc.dem.wapsrv.repository;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import java.util.List;

public interface TransactionDataset {

    void add(Statement statement);

    void remove(Statement statement);
    List<Statement> query(Resource subject, IRI predicate, Resource object);
    void commit();
}
