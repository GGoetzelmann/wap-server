package edu.kit.scc.dem.wapsrv.repository.util;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.*;

public class QueryBuilder {

    public static Query appendQueryForPropertyValue(Query query, String property, String value) {

        query.setPrefix("oa", "http://www.w3.org/ns/oa#");
        query.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

        ElementGroup body = (ElementGroup) query.getQueryPattern();
        ElementNamedGraph graph = (ElementNamedGraph) body.getElements().get(0);
        ElementGroup graphBody = (ElementGroup) graph.getElement();

        switch (property) {
            case "target": {
                Var sub = Var.alloc("s1");
                Var sub2 = Var.alloc("s2");
                //value may be target itself or source
                ElementTriplesBlock triplesBlock1 = new ElementTriplesBlock();
                triplesBlock1.addTriple(Triple.create(sub, NodeFactory.createURI("http://www.w3.org/ns/oa#hasTarget"), NodeFactory.createURI(value)));

                ElementTriplesBlock triplesBlock2 = new ElementTriplesBlock();
                triplesBlock2.addTriple(Triple.create(sub2, NodeFactory.createURI("http://www.w3.org/ns/oa#hasSource"), NodeFactory.createURI(value)));
                triplesBlock2.addTriple(Triple.create(sub, NodeFactory.createURI("http://www.w3.org/ns/oa#hasTarget"), sub2));


                ElementUnion union = new ElementUnion();
                union.addElement(triplesBlock1);
                union.addElement(triplesBlock2);

                graphBody.addElement(union);

                return query;
            }
            case "body": {
                Var sub = Var.alloc("s3");
                Var sub2 = Var.alloc("s4");
                //value may be body itself or value
                ElementTriplesBlock triplesBlock1 = new ElementTriplesBlock();
                triplesBlock1.addTriple(Triple.create(sub, NodeFactory.createURI("http://www.w3.org/ns/oa#hasBody"), NodeFactory.createURI(value)));

                ElementTriplesBlock triplesBlock2 = new ElementTriplesBlock();
                triplesBlock2.addTriple(Triple.create(sub2, NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#value"), NodeFactory.createLiteral(value)));
                triplesBlock2.addTriple(Triple.create(sub, NodeFactory.createURI("http://www.w3.org/ns/oa#hasBody"), sub2));

                ElementUnion union = new ElementUnion();
                union.addElement(triplesBlock1);
                union.addElement(triplesBlock2);

                graphBody.addElement(union);
                return query;
            }
            case "selector": { //TODO: currently only for value, not for an external selector
                Var sub = Var.alloc("s5");
                Var sub2 = Var.alloc("s6");
                //value may be value
                ElementTriplesBlock triplesBlock1 = new ElementTriplesBlock();
                triplesBlock1.addTriple(Triple.create(sub2, NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#value"), NodeFactory.createLiteral(value)));
                triplesBlock1.addTriple(Triple.create(sub, NodeFactory.createURI("http://www.w3.org/ns/oa#hasSelector"), sub2));

                graphBody.addElement(triplesBlock1);
                return query;
            }
            case "creator": {
                Var sub = Var.alloc("s7");
                Var sub2 = Var.alloc("s8");
                query.setPrefix("foaf", "http://xmlns.com/foaf/0.1/");
                //value may be creator itself or foaf:name
                //value may be body itself or value
                ElementTriplesBlock triplesBlock1 = new ElementTriplesBlock();
                triplesBlock1.addTriple(Triple.create(sub, NodeFactory.createURI("http://purl.org/dc/terms/creator"), NodeFactory.createURI(value)));

                ElementTriplesBlock triplesBlock2 = new ElementTriplesBlock();
                triplesBlock2.addTriple(Triple.create(sub2, NodeFactory.createURI("http://xmlns.com/foaf/0.1/name"), NodeFactory.createLiteral(value)));
                triplesBlock2.addTriple(Triple.create(sub, NodeFactory.createURI("http://purl.org/dc/terms/creator"), sub2));

                ElementUnion union = new ElementUnion();
                union.addElement(triplesBlock1);
                union.addElement(triplesBlock2);

                graphBody.addElement(union);
                return query;
            }
            default:
                throw new IllegalArgumentException("queried property not implemented yet");
        }
    }

    public static Query buildBasicQuery() {
        Query query = QueryFactory.make();
        query.setQuerySelectType();
        query.setDistinct(true);

        ElementGroup body = new ElementGroup();
        Var g = Var.alloc("g");
        ElementNamedGraph graph = new ElementNamedGraph(g, new ElementGroup());

        body.addElement(graph);
        query.setQueryPattern(body);
        query.addResultVar(g);
        return query;
    }
}
