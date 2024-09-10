package edu.kit.scc.dem.wapsrv.repository.util;

import edu.kit.scc.dem.wapsrv.service.restext.QueryCollectionService;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.E_StrContains;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueString;
import org.apache.jena.sparql.syntax.*;

public class QueryBuilder {

    public static Query appendQueryForPropertyValue(Query query, String property, String value) {
        return appendQueryForPropertyValue(query, property, value, QueryCollectionService.MatchType.EXACT);
    }

    public static Query appendQueryForPropertyValue(Query query, String property, String value, QueryCollectionService.MatchType matchType) {

        query.setPrefix("oa", "http://www.w3.org/ns/oa#");
        query.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

        ElementGroup body = (ElementGroup) query.getQueryPattern();
        ElementNamedGraph graph = (ElementNamedGraph) body.getElements().get(0);
        ElementGroup graphBody = (ElementGroup) graph.getElement();

        switch (property) {
            case "target": {
                Var sub = Var.alloc("s1");
                Var sub2 = Var.alloc("s2");
                Node matchResource = matchType == QueryCollectionService.MatchType.EXACT
                        ? NodeFactory.createURI(value)
                        : Var.alloc("m1");
                //value may be target itself or source

                //?s1 oa:hasTarget <value> || ?s1 oa:hasTarget ?m1
                ElementTriplesBlock triplesBlock1 = new ElementTriplesBlock();
                triplesBlock1.addTriple(Triple.create(sub, NodeFactory.createURI("http://www.w3.org/ns/oa#hasTarget"), matchResource));

                //?s2 oa:hasSource <value> || ?s2 oa:hasSource ?m1
                //?s1 oa:hasTarget ?s2
                ElementTriplesBlock triplesBlock2 = new ElementTriplesBlock();
                triplesBlock2.addTriple(Triple.create(sub2, NodeFactory.createURI("http://www.w3.org/ns/oa#hasSource"), matchResource));
                triplesBlock2.addTriple(Triple.create(sub, NodeFactory.createURI("http://www.w3.org/ns/oa#hasTarget"), sub2));

                //FILTER CONTAINS(STR(?m1), value)
                if (matchType == QueryCollectionService.MatchType.CONTAINS) {
                    Expr fexpr = new E_StrContains(new E_Str(new ExprVar(matchResource)), new NodeValueString(value));
                    ElementFilter matchFilter = new ElementFilter(fexpr);
                    graphBody.addElement(matchFilter);
                }
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

                Node matchLiteral = matchType == QueryCollectionService.MatchType.EXACT
                        ? NodeFactory.createLiteral(value)
                        : Var.alloc("m3");

                //?s6 rdf:value "value" || ?s6 rdf:value ?m3
                //?s5 oa:hasSelector ?s6
                ElementTriplesBlock triplesBlock1 = new ElementTriplesBlock();
                triplesBlock1.addTriple(Triple.create(sub2, NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#value"), matchLiteral));
                triplesBlock1.addTriple(Triple.create(sub, NodeFactory.createURI("http://www.w3.org/ns/oa#hasSelector"), sub2));

                graphBody.addElement(triplesBlock1);

                //FILTER CONTAINS(STR(?m3), value)
                if (matchType == QueryCollectionService.MatchType.CONTAINS) {
                    Expr fexpr = new E_StrContains(new ExprVar(matchLiteral), new NodeValueString(value));
                    ElementFilter matchFilter = new ElementFilter(fexpr);
                    graphBody.addElement(matchFilter);
                }
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
