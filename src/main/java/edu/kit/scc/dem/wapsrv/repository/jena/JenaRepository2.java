package edu.kit.scc.dem.wapsrv.repository.jena;

import edu.kit.scc.dem.wapsrv.dao.ISequenceDao;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

import edu.kit.scc.dem.wapsrv.model.rdf.RDF4JUtilities;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.tdb2.DatabaseMgr;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.NotExistentException;
import edu.kit.scc.dem.wapsrv.model.WapObject;
import edu.kit.scc.dem.wapsrv.model.ext.SequenceResource;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfBackend;
import edu.kit.scc.dem.wapsrv.repository.CollectedRepository;
import edu.kit.scc.dem.wapsrv.repository.TransactionRepository;
import java.util.Optional;
import org.apache.jena.sparql.core.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * The Jena repository implementation. <br>
 * IMPORTANT: Only the externally called functions are intercepted! Make sure to
 * add @JenaTransaction Annotation with the needed Type in front of every called
 * Method.
 *
 * This implementation of a Jena Repository is identical to the default
 * implementation except that sequence information are stored in a separate
 * relational database instead of the RDF store. As a result, the performance
 * for adding new elements won't change depending on the number of elements per
 * container. Typically, this is relevant only if containers with a size of more
 * than 10.000 elements are expected.
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @author Thomas Jejkal
 * @version 1.1
 */
@Repository("jena+jdbc")
public class JenaRepository2 extends CollectedRepository{

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private Dataset dataBase;
  /**
   * Database
   */
  @Autowired
  private JenaDataBase dataBaseSource;

  @Autowired
  private ISequenceDao iSequenceDao;
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
  public void setWapServerConfig(WapServerConfig wapServerConfig){
    this.wapServerConfig = wapServerConfig;
  }

  @PostConstruct
  private void init(){
    if(wapServerConfig == null){
      throw new RuntimeException("WapServerConfiguration not set in JenaRepository");
    }
    if(dataBaseSource != null){
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
  public void updateTriple(String modelIri, org.eclipse.rdf4j.model.Statement oldStatement, org.eclipse.rdf4j.model.Statement newStatement) {
    //not implemented yet
  }

  @Override
  public void addTriple(String modelIri, org.eclipse.rdf4j.model.Statement statement) {
    //not implemented yet
  }

  @Override
  public void removeTriple(String modelIri, org.eclipse.rdf4j.model.Statement statement) {
    //not implemented yet
  }

  @Override
  public void removeAll(String modelIri) {
    //not implemented yet
  }

  @Override
  public String backupDatabase(){
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
  public Dataset getDataBase(){
    return dataBase;
  }

  /**
   * Sets the database dataset.
   *
   * @param dataBase the dataBase dataset to set
   */
  public void setDataBase(Dataset dataBase){
    this.dataBase = dataBase;
  }

  @Override
  public boolean beginTransaction(TransactionRepository.Type type){
    if(!dataBase.isInTransaction()){
      dataBase.begin(translateType(type));
      return true;
    }
    return false;
  }

  @Override
  public void abortTransaction(){
    if(dataBase.isInTransaction()){
      dataBase.abort();
      dataBase.end();
    }
  }

  @Override
  public void endTransaction(boolean wasOpend){
    if(wasOpend & dataBase.isInTransaction()){
        
      if(dataBase.isInTransaction()){
        dataBase.commit();
      }
      dataBase.end();
    }
  }

  private ReadWrite translateType(TransactionRepository.Type type){
    if(type.equals(TransactionRepository.Type.Read)){
      return ReadWrite.READ;
    }
    if(type.equals(TransactionRepository.Type.Write)){
      return ReadWrite.WRITE;
    }
    return null;
  }

  @Override
  public void addElementToRdfSeq(String modelIri, String seqIri, String objIri){
    log.trace("Entering addElementToRdfSeq({}, {}, {})).", modelIri, seqIri, objIri);
    SequenceResource res = SequenceResource.create(seqIri, objIri);
    log.trace("Persisting sequence resource {}.", res);
    res = iSequenceDao.save(res);
    log.trace("Successfully persisted sequence resource. Received id {}.", res.getId());
  }

  @Override
  public void removeElementFromRdfSeq(String modelIri, String seqIri, String objIri){
    log.trace("Entering removeElementFromRdfSeq({}, {}, {}).", modelIri, seqIri, objIri);
    Optional<SequenceResource> optRes = iSequenceDao.findByAnnotationIriEquals(objIri);
    if(optRes.isPresent()){
      SequenceResource res = optRes.get();
      log.trace("Deleting resource {}.", res);
      iSequenceDao.delete(res);
      log.trace("Resource successfully deleted.");
    }
  }

  @Override
  public int countElementsInSeq(String modelIri, String seqIri){
    log.trace("Entering countElementsInSeq({}, {}).", modelIri, seqIri);
    int cnt = iSequenceDao.findBySequenceIriEquals(seqIri).size();
    log.trace("Returning sequence size {}", cnt);
    return cnt;
  }

  @Override
  public void writeObjectToDatabase(WapObject wapObject) {
    org.eclipse.rdf4j.model.Model dataset = wapObject.getDataset();

    Model jenaModel = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
    for (org.eclipse.rdf4j.model.Statement rdf4jStatement: dataset) {
      org.eclipse.rdf4j.model.Resource subject = rdf4jStatement.getSubject();
      IRI predicate = rdf4jStatement.getPredicate();
      Value object = rdf4jStatement.getObject();

      org.apache.jena.rdf.model.Statement jenaStatement = jenaModel.createStatement(
              jenaModel.createResource(subject.stringValue()),
              jenaModel.createProperty(predicate.stringValue()),
              jenaModel.createTypedLiteral(object.stringValue())
      );

      jenaModel.add(jenaStatement);
    }

    String iriString = wapObject.getIriString();
    dataBase.addNamedModel(iriString, jenaModel);
    // Model returnValue = dataBase.getNamedModel(iriString);
  }

  @Override
  public List<String> getRangeOfObjectIrisFromSeq(String containerIri, String seqIri, int firstIndex, int lastIndex){
    log.trace("Entering getRangeOfObjectIrisFromSeq({}, {}, {}, {}).", containerIri, seqIri, firstIndex, lastIndex);

    int pageSize = wapServerConfig.getPageSize();
    int pageNr = firstIndex / pageSize;
    log.debug("Converted firstIndex {} and lastIndex {} to page {} using pageSize {}.", firstIndex, lastIndex, pageNr, pageSize);
    Page<SequenceResource> page = iSequenceDao.findBySequenceIriEquals(seqIri, PageRequest.of(pageNr, pageSize));
    log.trace("Obtaining resource IRIs from page.");
    List<String> retValue = new ArrayList<>();
    page.getContent().forEach((r) -> {
      retValue.add(r.getAnnotationIri());
    });
    log.trace("Returning {} resource IRI(s).", retValue.size());
    return retValue;
  }

  @Override
  public List<String> getAllObjectIrisOfSeq(String modelIri, String seqIri){
    log.trace("Entering getAllObjectIrisOfSeq({}, {},).", modelIri, seqIri);
    return getRangeOfObjectIrisFromSeq(modelIri, seqIri, 0, Integer.MAX_VALUE);
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
  public void emptySeq(String modelIri, String seqIri){
    log.trace("Entering emptySeq({}, {},).", modelIri, seqIri);
    List<SequenceResource> res = iSequenceDao.findBySequenceIriEquals(seqIri);
    log.trace("Removing {} resource IRI(s).", res.size());
    iSequenceDao.deleteAll(res);
  }
}
