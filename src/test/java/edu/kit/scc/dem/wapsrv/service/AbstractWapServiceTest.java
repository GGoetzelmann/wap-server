package edu.kit.scc.dem.wapsrv.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Calendar;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.EmptyModel;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.EtagFactory;
import edu.kit.scc.dem.wapsrv.app.EtagFactoryMock;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.app.WapServerConfigMock;
import edu.kit.scc.dem.wapsrv.model.Container;
import edu.kit.scc.dem.wapsrv.model.ModelFactory;
import edu.kit.scc.dem.wapsrv.model.Page;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfUtilities;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.DcTermsVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.LdpVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfSchemaVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.WapVocab;
import edu.kit.scc.dem.wapsrv.model.validators.JsonLdValidator;
import edu.kit.scc.dem.wapsrv.model.validators.Validator;
import edu.kit.scc.dem.wapsrv.model.validators.ValidatorRegistry;
import edu.kit.scc.dem.wapsrv.repository.CollectedRepository;
import edu.kit.scc.dem.wapsrv.repository.RepositoryMock;
import edu.kit.scc.dem.wapsrv.repository.jena.JenaRepository;
import edu.kit.scc.dem.wapsrv.testscommon.ModelFactoryMock;
import org.springframework.context.annotation.ComponentScan;

/**
 * Tests the class AbstractWapService
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ContainerServiceImpl.class, RepositoryMock.class, ModelFactoryMock.class,
  EtagFactoryMock.class, JsonLdProfileRegistry.class, ValidatorRegistry.class, WapServerConfigMock.class,
  JsonLdValidator.class, Validator.class})
@ComponentScan(basePackages = "edu.kit.scc.dem.wapsrv.repository")
@Configuration
@ActiveProfiles("test")
class AbstractWapServiceTest{

  private static final String ROOT_IRI = "http://www.example.org/wap/";
  private AbstractWapService wapObjectService;
  @Autowired
  private CollectedRepository[] wapObjectRepository;
  @SuppressWarnings("unused")
  @Autowired
  private JsonLdProfileRegistry jsonLdProfileRegistry;
  @Autowired
  private WapServerConfig wapServerConfigMock;
  @Autowired
  private ModelFactory modelFactoryMock;
  @SuppressWarnings("unused")
  @Autowired
  private ValidatorRegistry validatorRegistry;
  @SuppressWarnings("unused")
  @Autowired
  private EtagFactory etagFactoryMock;
  private Model dataset;

  @Autowired
  private void setWapService(ContainerServiceImpl containerService){
    this.wapObjectService = containerService;
  }

  //TODO: Check and update
  /**
   * Setup test.
   */
  @BeforeEach
  /**
  void setupTest(){
    simpleRDF = new SimpleRDF();
    dataset = simpleRDF.createDataset();
    IRI root = simpleRDF.createIRI(ROOT_IRI);
    dataset.add(root, root, RdfVocab.type, LdpVocab.basicContainer);
    dataset.add(root, root, DcTermsVocab.modified,
            RdfUtilities.rdfLiteralFromCalendar(Calendar.getInstance(), simpleRDF));
    dataset.add(root, root, RdfSchemaVocab.label, simpleRDF.createLiteral("label"));
    dataset.add(root, root, WapVocab.etag, simpleRDF.createLiteral("test etag"));
    when(wapObjectRepository[0].getRdf()).thenReturn(simpleRDF);
    when(wapObjectRepository[0].getTransactionDataset()).thenReturn(dataset);
    when(wapObjectRepository[0].getWapObject(ROOT_IRI)).thenReturn(dataset);
    when(modelFactoryMock.createContainer(any(Dataset.class), any(boolean.class), any(boolean.class)))
            .thenReturn(mock(Container.class));
    when(wapServerConfigMock.getPageSize()).thenReturn(10);
    Page pageMock = mock(Page.class);
    when(modelFactoryMock.createPage(any(Dataset.class), eq(ROOT_IRI), eq(0), any(boolean.class), any(boolean.class),
            any(int.class), any(String.class), eq("label"))).thenReturn(pageMock);
    when(pageMock.getContainerIri()).thenReturn(ROOT_IRI);
    Dataset pageDataset = simpleRDF.createDataset();
    pageDataset.getGraph().add(root, WapVocab.deleted, WapVocab.deleted);
    when(pageMock.getDataset()).thenReturn(pageDataset);
  }*/

  void setupTest(){
    ValueFactory valueFactory = SimpleValueFactory.getInstance();

    // Create an RDF model to hold the triples
    dataset = new LinkedHashModel();

    IRI root = valueFactory.createIRI(ROOT_IRI);
    dataset.add(root, RdfVocab.type, LdpVocab.basicContainer);
    dataset.add(root, DcTermsVocab.modified,
            RdfUtilities.rdfLiteralFromCalendar(Calendar.getInstance()));
    dataset.add(root, RdfSchemaVocab.label, valueFactory.createLiteral("label"));
    dataset.add(root, WapVocab.etag, valueFactory.createLiteral("test etag"));
    when(wapObjectRepository[0].getTransactionDataset()).thenReturn(dataset);
    when(wapObjectRepository[0].getWapObject(ROOT_IRI)).thenReturn(dataset);
    when(modelFactoryMock.createContainer(any(Model.class), any(boolean.class), any(boolean.class)))
            .thenReturn(mock(Container.class));
    when(wapServerConfigMock.getPageSize()).thenReturn(10);
    Page pageMock = mock(Page.class);
    when(modelFactoryMock.createPage(any(Model.class), eq(ROOT_IRI), eq(0), any(boolean.class), any(boolean.class),
            any(int.class), any(String.class), eq("label"))).thenReturn(pageMock);
    when(pageMock.getContainerIri()).thenReturn(ROOT_IRI);
    Model pageDataset = new LinkedHashModel();
    pageDataset.add(root, WapVocab.deleted, WapVocab.deleted);
    when(pageMock.getDataset()).thenReturn(pageDataset);
  }

//  /**
//   * Test getting model factory.
//   */
//  @Test
//  final void testGetModelFactory(){
//    // DOTEST write the test for this method
//  }
//
//  /**
//   * Test getting WAP Server configuration.
//   */
//  @Test
//  final void testGetWapServerConfig(){
//    // DOTEST write the test for this method
//  }
//
//  /**
//   * Test getting validator registry.
//   */
//  @Test
//  final void testGetValidatorRegistry(){
//    // DOTEST write the test for this method
//  }
//
//  /**
//   * Test generating an ETAG.
//   */
//  @Test
//  final void testGenerateEtag(){
//    // DOTEST write the test for this method
//  }
//
//  /**
//   * Test if input format is valid.
//   */
//  @Test
//  final void testIsValidInputFormat(){
//    // DOTEST write the test for this method
//  }
//
//  /**
//   * Test if schema is valid.
//   */
//  @Test
//  final void testCheckSchemaValidity(){
//    // DOTEST write the test for this method
//  }
//
//  /**
//   * Test ETAG exists.
//   */
//  @Test
//  final void testCheckEtag(){
//    // DOTEST write the test for this method
//  }
//
//  /**
//   * Test deleting an object.
//   */
//  @Test
//  final void testDeleteObject(){
//    // DOTEST write the test for this method
//  }
//
//  /**
//   * Test to update ETAG on blank node and IRI string.
//   */
//  @Test
//  final void testUpdateEtagBlankNodeOrIRIString(){
//    // DOTEST write the test for this method
//  }
//
//  /**
//   * Test to update an ETAG string.
//   */
//  @Test
//  final void testUpdateEtagStringString(){
//    // DOTEST write the test for this method
//  }
//
//  /**
//   * Test to write a WAP object to DB.
//   */
//  @Test
//  final void testWriteWapObjectToDb(){
//    // DOTEST write the test for this method
//  }
//
//  /**
//   * Test if exists and is not deleted.
//   */
//  @Test
//  final void testCheckExistsAndNotDeleted(){
//    // DOTEST write the test for this method
//  }

  /**
   * Test if an IRI is contained.
   */
  @Test
  final void testContainsIri(){
    wapObjectService.containsIri(ROOT_IRI);
  }

//  /**
//   * Test if an IRI is deleted.
//   */
//  @Test
//  final void testIsIriDeleted(){
//    // DOTEST write the test for this method
//  }
}
