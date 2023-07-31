package edu.kit.scc.dem.wapsrv.testscommon;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.kit.scc.dem.wapsrv.model.rdf.RDF4JUtilities;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb2.TDB2Factory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.github.jsonldjava.core.DocumentLoader;
import com.github.jsonldjava.core.JsonLdConsts;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfUtilities;
import edu.kit.scc.dem.wapsrv.repository.jena.JenaRdfBackend;
import edu.kit.scc.dem.wapsrv.testsbenchmark.WapServerProfiling;
import org.apache.jena.sparql.core.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ApacheCommonsRdfTests
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@Tag("old")
public class ApacheCommonsRdfTests {

    private static final Logger logger = LoggerFactory.getLogger(ApacheCommonsRdfTests.class);

    private static final int SPEED_TEST_COUNT = 100;

    /**
     * Test context load.
     */
    @Test
    public void contextLoads() {
    }

    /**
     * Setup tests.
     */
    @BeforeAll
    static void setupTests() {
        System.setProperty(DocumentLoader.DISALLOW_REMOTE_CONTEXT_LOADING, "false");
    }

    /**
     * Teardown tests.
     */
    @AfterAll
    static void teardownTests() {
        System.setProperty(DocumentLoader.DISALLOW_REMOTE_CONTEXT_LOADING, "false");
    }

    //TODO: Continue removing apache commons (jsonld)
    /**
     * Test RDF simple annotation.
     *
     * @throws JsonLdError A JSON LD error exception
     * @throws JsonGenerationException A JSON generation exception
     * @throws IOException A I/O exception

    @Test
    public void rdfSimpleAnnotation() throws JsonLdError, JsonGenerationException {
        JenaRdfBackend rdfLib = new JenaRdfBackend();
        Model dataset = rdfLib.readFromFile("src/main/resources/testdata/PAGE2017XML_Tristrant_VD16T1963-008.jsonld",
                Format.JSON_LD);
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        IRI type = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/oa#Annotation");
        IRI created = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/terms/created");
        Literal createdString = SimpleValueFactory.getInstance().createLiteral("2018-06-5T00:23:00Z");
        IRI oldIdIri = null;
        for (Statement t : dataset.filter(null, iri, type)) {
            Log.info(this, t.getSubject().stringValue() + " , " + t.getPredicate().stringValue() + " , "
                    + t.getObject().stringValue());
            oldIdIri = (IRI) t.getSubject();
            if (dataset.contains(t.getSubject(), created, null)) {
                Log.info(this, "the created triple for this annotation already exists, DELETING...");
                dataset.remove(t.getSubject(), created, null);
            }
            dataset.add(t.getSubject(), created, createdString);
        }
        IRI newIdIri = SimpleValueFactory.getInstance().createIRI("http://wapserver.dem.scc.kit.edu/tristrant/anno1");
        RdfUtilities.renameNodeIri(dataset, oldIdIri, newIdIri);
        logger.trace(rdfLib.getOutput(dataset, Format.JSON_LD));
        JsonLdRDF jsonLdRdf = new JsonLdRDF();
        JsonLdGraph jsonLdGraph = jsonLdRdf.createGraph();
        for (Statement t : dataset) {
            Triple tr = (Triple) RDF4JUtilities.toJenaStatement(t);
            jsonLdGraph.add(tr);
        }
        final JsonLdOptions options = new JsonLdOptions();
        options.format = JsonLdConsts.APPLICATION_NQUADS;
        options.setCompactArrays(true);
        logger.trace("Before compact");
        try {
            logger.trace(JsonUtils.toPrettyString(jsonLdGraph));
        } catch (IOException e) {
            logger.trace(e.getMessage());
        }
        final List<String> newContexts = new LinkedList<>();
        newContexts.add("http://www.w3.org/ns/anno.jsonld");
        final Map<String, Object> compacted = JsonLdProcessor.compact(jsonLdGraph, newContexts, options);
        logger.trace("\n\nAfter compact:");
        try {
            logger.trace(JsonUtils.toPrettyString(compacted));
        } catch (IOException e) {
            logger.trace(e.getMessage());
        }
    }
     */

    //TODO: Look into what it does
    /**
     * JENA backed speed test.
     *
     * @throws FileNotFoundException File not found exception

    @Test
    public void jenaBackedSpeedTest() throws FileNotFoundException {
        JenaRDF factory = new JenaRDF();
        JenaGraph sourceGraph = factory.createGraph();
        sourceGraph.asJenaModel().read("src/main/resources/testdata/PAGE2017XML_Tristrant_VD16T1963-008.jsonld");
        IRI typeAnnotation = factory.createIRI("http://www.w3.org/ns/oa#Annotation");
        IRI rdfType = factory.createIRI(org.apache.jena.vocabulary.RDF.type.getURI());
        long timeStartPrep = System.currentTimeMillis();
        Iterable<Triple> it = sourceGraph.iterate(null, rdfType, typeAnnotation);
        BlankNodeOrIRI node = null;
        for (Triple t : it) {
            node = t.getSubject();
            Log.info(this, "Anno ROOT: " + t.getSubject().ntriplesString() + " Object: " + t.getObject().ntriplesString());
        }
        // fill a List with models to add
        List<JenaGraph> modelList = new ArrayList<JenaGraph>();
        for (int i = 0; i < SPEED_TEST_COUNT; i++) {
            Graph newGraph = RdfUtilities.clone(sourceGraph);
            RdfUtilities.renameNodeIri(newGraph, node,
                    factory.createIRI("http://wapserver.dem.scc.kit.edu/tristrant/anno" + i));
            modelList.add((JenaGraph) newGraph);
        }
        long durationPrep = System.currentTimeMillis() - timeStartPrep;
        Log.info(this, "---------- Prepare of " + SPEED_TEST_COUNT + " Annos in millis: " + durationPrep);
        org.apache.jena.query.Dataset ds = TDB2Factory.connectDataset("temp/tdb2/test.tdb");
        ((Transactional)ds).begin();
        ds.getDefaultModel().removeAll();
        ds.commit();
        ds.end();
        long timeStart = System.currentTimeMillis();
        ds.begin(ReadWrite.WRITE);
        for (int i = 0; i < SPEED_TEST_COUNT; i++) {
            ds.getDefaultModel().add(modelList.get(i).asJenaModel());
        }
        ds.commit();
        ds.end();
        long duration = System.currentTimeMillis() - timeStart;
        Log.info(this, "---------- Write " + SPEED_TEST_COUNT + " Annos to Database millis: " + duration);
        Model targetModel = ModelFactory.createDefaultModel();
        Model model2 = ds.getDefaultModel();
        ds.begin(ReadWrite.READ);
        Log.info(this, "Triples # in DB: " + model2.listStatements().toList().size());
        ds.end();
        long timeStartRead = System.currentTimeMillis();
        ds.begin(ReadWrite.READ);
        for (int i = 0; i < SPEED_TEST_COUNT; i++) {
            Model foundModel = model2.getResource("http://wapserver.dem.scc.kit.edu/tristrant/anno" + i).getModel();
            targetModel.add(foundModel);
        }
        ds.end();
        long durationRead = System.currentTimeMillis() - timeStartRead;
        Log.info(this, "---------- read " + SPEED_TEST_COUNT + " Annos from Database millis: " + durationRead);
    }
     */
}
