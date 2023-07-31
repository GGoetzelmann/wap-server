package edu.kit.scc.dem.wapsrv.repository.jena;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;

import edu.kit.scc.dem.wapsrv.model.rdf.RDF4JUtilities;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb2.DatabaseMgr;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.NotExistentException;
import edu.kit.scc.dem.wapsrv.model.WapObject;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfBackend;
import edu.kit.scc.dem.wapsrv.repository.CollectedRepository;
import edu.kit.scc.dem.wapsrv.repository.TransactionRepository;
import org.apache.jena.query.ReadWrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * The Jena repository implementation. <br>
 * IMPORTANT: Only the externally called functions are intercepted! Make sure to
 * add @JenaTransaction Annotation with the needed Type in front of every called
 * Method.
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@Repository("jena")
@Primary
public class JenaRepository extends CollectedRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Dataset dataBase;
    /**
     * Database
     */
    @Autowired
    private JenaDataBase dataBaseSource;
    /**
     * The application configuration to use
     */
    @Autowired
    private WapServerConfig wapServerConfig;
    /**
     * The RDF backend
     */
    @Autowired
    private RdfBackend rdfBackend;

    /**
     * Sets the configuration to use
     *
     * @param wapServerConfig The application configuration
     */
    @Autowired
    public void setWapServerConfig(WapServerConfig wapServerConfig) {
        this.wapServerConfig = wapServerConfig;
    }

    @PostConstruct
    private void init() {
        if (wapServerConfig == null) {
            throw new RuntimeException("WapServerConfiguration not set in JenaRepository");
        }
        if (dataBaseSource != null) {
            setDataBase(dataBaseSource.getDataBase());
        }
    }

    @Override
    public org.eclipse.rdf4j.model.Model getWapObject(String iri) {
        org.eclipse.rdf4j.model.Model  retDs = new LinkedHashModel();
        //TODO: Continue to remove Apache Commons
        if (!dataBase.containsNamedModel(iri)) {
            throw new NotExistentException("the requested container does not exist");
        }
        Model readModel = dataBase.getNamedModel(iri);
        readModel.listStatements().forEachRemaining(s -> {
            //s.asJenaDatasetGraph().getDefaultGraph().add(s.asTriple());
            retDs.add(RDF4JUtilities.toRDF4JStatement(s));
        });
        return retDs;
    }

    @Override
    public String backupDatabase() {
        DatabaseMgr.backup(dataBase.asDatasetGraph());
        Path path = FileSystems.getDefault().getPath(WapServerConfig.getInstance().getDataBasePath() + "/Backups/");
        return path.toUri().toString();
    }

    /**
     * Gets the database dataset.
     *
     * @return the dataBase dataset
     */
    @Override
    public Dataset getDataBase() {
        return dataBase;
    }

    /**
     * Sets the database dataset.
     *
     * @param dataBase the dataBase dataset to set
     */
    public void setDataBase(Dataset dataBase) {
        this.dataBase = dataBase;
    }

    @Override
    public boolean beginTransaction(TransactionRepository.Type type) {
        if (!dataBase.isInTransaction()) {
            log.trace("Beginning {} transaction.", type);
            dataBase.begin(translateType(type));
            log.trace("{} transaction now active.", type);
            return true;
        }
        return false;
    }

    @Override
    public void abortTransaction() {
        if (dataBase.isInTransaction()) {
            log.trace("Aborting {} transaction.");
            dataBase.abort();
            log.trace("Transaction aborted.");
            log.trace("Ending dataset.");
            dataBase.end();
            log.trace("Dataset ended.");
        }
    }

    @Override
    public void endTransaction(boolean wasOpend) {
        if (wasOpend) {
            try {
                if (dataBase.isInTransaction()) {
                    log.trace("Committing dataset.");
                    dataBase.commit();
                    log.trace("Dataset committed.");
                }
            } finally {
                log.trace("Ending dataset.");
                dataBase.end();
                log.trace("Dataset ended.");
            }
        }
    }

    private ReadWrite translateType(TransactionRepository.Type type) {
        if (type.equals(TransactionRepository.Type.Read)) {
            return ReadWrite.READ;
            //return TxnType.READ;
        }
        if (type.equals(TransactionRepository.Type.Write)) {
            return ReadWrite.WRITE;
            //return TxnType.WRITE;
        }
        return null;
    }

    @Override
    public void addElementToRdfSeq(String modelIri, String seqIri, String objIri) {
        Model model = dataBase.getNamedModel(modelIri);
        Seq seq = model.getSeq(seqIri);
        Resource objectResource = model.getResource(objIri);
        seq.add(objectResource);
    }

    @Override
    public void removeElementFromRdfSeq(String modelIri, String seqIri, String objIri) {
        Model model = dataBase.getNamedModel(modelIri);
        Seq seq = model.getSeq(seqIri);
        Resource objectResource = model.getResource(objIri);
        int indexOfContainer = seq.indexOf(objectResource);
        if (indexOfContainer > 0) {
            seq.remove(indexOfContainer);
        }
    }

    @Override
    public int countElementsInSeq(String modelIri, String seqIri) {
        Seq seq = dataBase.getNamedModel(modelIri).getSeq(seqIri);
        return seq.size();
    }

    @Override
    public void writeObjectToDatabase(WapObject wapObject) {
        org.eclipse.rdf4j.model.Model dataset = wapObject.getDataset();

        Model jenaModel = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        HashMap<String, Resource> bnodeMapping = new HashMap<>();
        for (org.eclipse.rdf4j.model.Statement rdf4jStatement: dataset) {
            org.apache.jena.rdf.model.Statement jenaStatement = RDF4JUtilities.toJenaStatement(rdf4jStatement, bnodeMapping);

            jenaModel.add(jenaStatement);
        }

        String iriString = wapObject.getIriString();
        dataBase.addNamedModel(iriString, jenaModel);
        // Model returnValue = dataBase.getNamedModel(iriString);
    }

    @Override
    public List<String> getRangeOfObjectIrisFromSeq(String containerIri, String seqIri, int firstIndex, int lastIndex) {
        List<String> retValue = new ArrayList<>();
        Model containerModel = dataBase.getNamedModel(containerIri);
        Seq annoSeq = containerModel.getSeq(seqIri);
        for (int i = firstIndex; i <= lastIndex; i++) {
            String annotationIri = annoSeq.getObject(i).asResource().toString();
            retValue.add(annotationIri);
        }
        return retValue;
    }

    @Override
    public List<String> getAllObjectIrisOfSeq(String modelIri, String seqIri) {
        Model containerModel = dataBase.getNamedModel(modelIri);
        Seq annoSeq = containerModel.getSeq(seqIri);
        return getRangeOfObjectIrisFromSeq(modelIri, seqIri, 1, annoSeq.size());
    }

    @Override
    public org.eclipse.rdf4j.model.Model getTransactionDataset() {

        ModelBuilder modelBuilder = new ModelBuilder();
        dataBase.listNames().forEachRemaining(graphName -> {
            Model jenaModel = dataBase.getNamedModel(graphName);
            StmtIterator iterator = jenaModel.listStatements();
            while (iterator.hasNext()) {
                Statement jenaStatement = iterator.nextStatement();
                org.eclipse.rdf4j.model.Statement stat = RDF4JUtilities.toRDF4JStatement(jenaStatement);
                org.eclipse.rdf4j.model.Resource subject = stat.getSubject();
                org.eclipse.rdf4j.model.IRI predicate = stat.getPredicate();
                org.eclipse.rdf4j.model.Value object = stat.getObject();
                modelBuilder.add(subject, predicate, object);
            }
        });

        org.eclipse.rdf4j.model.Model transactionDataset = modelBuilder.build();
        return transactionDataset;
    }

    @Override
    public void emptySeq(String modelIri, String seqIri) {
        Model model = dataBase.getNamedModel(modelIri);
        Resource subject = model.createResource(seqIri);
        model.removeAll(subject, null, null);
        // regenerate the seq.
        model.createSeq(seqIri);
    }

    @Override
    public void updateTriple(String modelIri, org.eclipse.rdf4j.model.Statement oldStatement, org.eclipse.rdf4j.model.Statement newStatement) {
        Model model = dataBase.getNamedModel(modelIri);

        removeTriple(modelIri, oldStatement);
        addTriple(modelIri, newStatement);
        /*
        Statement jenaStatement1 = RDF4JUtilities.toJenaStatement(oldStatement);
        Statement jenaStatement2 = RDF4JUtilities.toJenaStatement(newStatement);
        Resource oldSubject = jenaStatement1.getSubject();
        Property oldPredicate = jenaStatement1.getPredicate();
        RDFNode oldObject = jenaStatement1.getObject();
        Resource newSubject = jenaStatement2.getSubject();
        Property newPredicate = jenaStatement2.getPredicate();
        RDFNode newObject = jenaStatement2.getObject();

        model.remove(oldSubject, oldPredicate, oldObject);
        model.add(newSubject, newPredicate, newObject);

         */
    }

    @Override
    public void addTriple(String modelIri, org.eclipse.rdf4j.model.Statement statement) {
        Model model = dataBase.getNamedModel(modelIri);
        Statement jenaStatement1 = RDF4JUtilities.toJenaStatement(statement);
        Resource subject = jenaStatement1.getSubject();
        Property predicate = jenaStatement1.getPredicate();
        RDFNode object = jenaStatement1.getObject();
        model.add(subject, predicate, object);
    }

    @Override
    public void removeTriple(String modelIri, org.eclipse.rdf4j.model.Statement statement) {
        Model model = dataBase.getNamedModel(modelIri);
        Statement jenaStatement1 = RDF4JUtilities.toJenaStatement(statement);
        Resource subject = jenaStatement1.getSubject();
        Property predicate = jenaStatement1.getPredicate();
        RDFNode object = jenaStatement1.getObject();
        model.remove(subject, predicate, object);
    }

    @Override
    public void removeAll(String iri) {
        Model model = dataBase.getNamedModel(iri);
        //TODO: check if that was the intended function before: removes all triples with ID as the subject from the named graph with the same ID as the subject
        model.removeAll(ModelFactory.createDefaultModel().createResource(iri), null, null);
    }
}
