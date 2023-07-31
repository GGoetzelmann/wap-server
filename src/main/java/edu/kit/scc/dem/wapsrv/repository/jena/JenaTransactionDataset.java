package edu.kit.scc.dem.wapsrv.repository.jena;

import edu.kit.scc.dem.wapsrv.model.rdf.RDF4JUtilities;
import edu.kit.scc.dem.wapsrv.repository.TransactionDataset;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import java.util.List;

public class JenaTransactionDataset implements TransactionDataset {

    @Override
    public void add(Statement statement) {
        org.apache.jena.rdf.model.Statement jenaStatement = RDF4JUtilities.toJenaStatement(statement);
    }

    @Override
    public void remove(Statement statement) {

    }

    @Override
    public List<Statement> query(Resource subject, IRI predicate, Resource object) {
        return null;
    }

    @Override
    public void commit() {

    }
}
