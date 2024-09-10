package edu.kit.scc.dem.wapsrv.repository.util;

import edu.kit.scc.dem.wapsrv.service.restext.QueryCollectionService;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class QueryBuilderTest {

    private DatasetGraph testGraph;

    private final String graphURIString = "http://example.org/anno26";

    @BeforeEach
    public void setup() {
        // JSON-LD string
        String jsonld = """
                {
                  "@context": "http://www.w3.org/ns/anno.jsonld",
                  "id": "http://example.org/anno26",
                  "type": "Annotation",
                  "creator": {
                      "id": "http://example.org/user1",
                      "type": "Person",
                      "name": "My Pseudonym",
                      "nickname": "pseudo",
                      "email_sha1": "58bad08927902ff9307b621c54716dcc5083e339"
                  },
                  "body": [
                      "http://example.org/road1",
                      {
                        "id": "http://example.org/road2",
                        "value": "find me",
                        "creator": "http://example.org/bodyCreator"
                      }
                  ],
                  "target": {
                    "source": "http://example.org/map1",
                    "selector": {
                      "id": "http://example.org/svg1",
                      "type": "SvgSelector",
                      "value": "<svg>...</svg>"
                    }
                  }
                }
                """;

        // Create a named graph
        testGraph = DatasetGraphFactory.create();
        Model model = ModelFactory.createDefaultModel();
        RDFParser.create().source(new StringReader(jsonld)).lang(RDFFormat.JSONLD.getLang()).parse(model.getGraph());
        testGraph.addGraph(NodeFactory.createURI(graphURIString), model.getGraph());
    }

    @Test
    public void testBuildBasicQuery() {
        Query query = QueryBuilder.buildBasicQuery();

        assertThat(query.toString()).contains("SELECT DISTINCT  ?g");
    }

    /**
     * Test query building for matching of target source with input json-ld
     */
    @Test
    public void testAppendQueryForTargetSource() {
        Query query = QueryBuilder.buildBasicQuery();
        Query appendedQuery = QueryBuilder.appendQueryForPropertyValue(query, "target", "http://example.org/map1");

        // Execute the query
        try (QueryExecution qexec = QueryExecutionFactory.create(appendedQuery, DatasetFactory.wrap(testGraph))) {
            ResultSet results = qexec.execSelect();
            assertTrue(results.hasNext(), "query returned no results");
            RDFNode resultGraphName = results.nextSolution().get("g"); //one named graph, one matching graph
            assertEquals(graphURIString, resultGraphName.toString());
        }
    }

    /**
     * Test query building for target source not matching the input json-ld
     */
    @Test
    public void testAppendQueryForTargetNoMatch() {
        Query query = QueryBuilder.buildBasicQuery();
        Query appendedQuery = QueryBuilder.appendQueryForPropertyValue(query, "target", "http://example.org/map2");

        // Execute the query
        try (QueryExecution qexec = QueryExecutionFactory.create(appendedQuery, DatasetFactory.wrap(testGraph))) {
            ResultSet results = qexec.execSelect();
            assertFalse(results.hasNext()); //one named graph, no matching graph
        }
    }

    /**
     * Test query building for matching of body value with input json-ld
     */
    @Test
    public void testAppendQueryForBodyValue() {
        Query query = QueryBuilder.buildBasicQuery();
        Query appendedQuery = QueryBuilder.appendQueryForPropertyValue(query, "body", "find me");

        // Execute the query
        try (QueryExecution qexec = QueryExecutionFactory.create(appendedQuery, DatasetFactory.wrap(testGraph))) {
            ResultSet results = qexec.execSelect();
            assertTrue(results.hasNext(), "query returned no results");
            RDFNode resultGraphName = results.nextSolution().get("g"); //one named graph, one matching graph
            assertEquals(graphURIString, resultGraphName.toString());
        }
    }

    /**
     * Test query building for annotation creator name matching the input json-ld
     */
    @Test
    public void testAppendQueryForCreatorName() {
        Query query = QueryBuilder.buildBasicQuery();
        Query appendedQuery = QueryBuilder.appendQueryForPropertyValue(query, "creator", "My Pseudonym");

        // Execute the query
        try (QueryExecution qexec = QueryExecutionFactory.create(appendedQuery, DatasetFactory.wrap(testGraph))) {
            ResultSet results = qexec.execSelect();
            assertTrue(results.hasNext(), "query returned no results");
            RDFNode resultGraphName = results.nextSolution().get("g"); //one named graph, one matching graph
            assertEquals(graphURIString, resultGraphName.toString());
        }
    }

    /**
     * Test query building for body creator matching the input json-ld
     */
    @Test
    public void testAppendQueryForCreatorID() {
        Query query = QueryBuilder.buildBasicQuery();
        Query appendedQuery = QueryBuilder.appendQueryForPropertyValue(query, "creator", "http://example.org/bodyCreator");

        // Execute the query
        try (QueryExecution qexec = QueryExecutionFactory.create(appendedQuery, DatasetFactory.wrap(testGraph))) {
            ResultSet results = qexec.execSelect();
            assertTrue(results.hasNext(), "query returned no results");
            RDFNode resultGraphName = results.nextSolution().get("g"); //one named graph, one matching graph
            assertEquals(graphURIString, resultGraphName.toString());
        }
    }

    /**
     * Test query building with multiple match patterns (target and selector match)
     */
    @Test
    public void testAppendMultipleQueries() {
        Query query = QueryBuilder.buildBasicQuery();
        Query appendedQuery = QueryBuilder.appendQueryForPropertyValue(query, "target", "http://example.org/map1");
        Query appendedQuery2 = QueryBuilder.appendQueryForPropertyValue(appendedQuery, "selector", "<svg>...</svg>");

        // Execute the query
        try (QueryExecution qexec = QueryExecutionFactory.create(appendedQuery2, DatasetFactory.wrap(testGraph))) {
            ResultSet results = qexec.execSelect();
            assertTrue(results.hasNext(), "query returned no results");
            RDFNode resultGraphName = results.nextSolution().get("g"); //one named graph, one matching graph
            assertEquals(graphURIString, resultGraphName.toString());
        }
    }

    @Test
    public void testAppendContainsTarget() {
        Query query = QueryBuilder.buildBasicQuery();
        Query appendedQuery = QueryBuilder.appendQueryForPropertyValue(query, "target", "http://example.org/", QueryCollectionService.MatchType.CONTAINS);

        // Execute the query
        try (QueryExecution qexec = QueryExecutionFactory.create(appendedQuery, DatasetFactory.wrap(testGraph))) {
            ResultSet results = qexec.execSelect();
            assertTrue(results.hasNext(), "query returned no results");
            RDFNode resultGraphName = results.nextSolution().get("g"); //one named graph, one matching graph
            assertEquals(graphURIString, resultGraphName.toString());
        }
    }
}
