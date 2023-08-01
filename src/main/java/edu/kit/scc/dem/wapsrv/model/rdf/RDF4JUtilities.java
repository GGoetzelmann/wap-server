package edu.kit.scc.dem.wapsrv.model.rdf;

import edu.kit.scc.dem.wapsrv.exceptions.FormatException;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.repository.jena.RDF4JFormatMapper;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

public class RDF4JUtilities {

    //Assume that not every use case really needs the bnode hashmap
    public static org.apache.jena.rdf.model.Statement toJenaStatement(Statement rdf4jstatement) {
        return toJenaStatement(rdf4jstatement, new HashMap<String, Resource>());
    }
    public static org.apache.jena.rdf.model.Statement toJenaStatement(Statement rdf4jstatement, HashMap<String, Resource> bnodeMapping) {

        Model jenaModel = ModelFactory.createDefaultModel();
        Resource subject;
        if(rdf4jstatement.getSubject().isIRI()) {
             subject = jenaModel.createResource(rdf4jstatement.getSubject().stringValue());
        } else {
            if(bnodeMapping.containsKey(rdf4jstatement.getSubject().stringValue())) {
                subject = bnodeMapping.get(rdf4jstatement.getSubject().stringValue());
            } else {
                subject = jenaModel.createResource();
                bnodeMapping.put(rdf4jstatement.getSubject().stringValue(), subject);
            }
        }
        Property predicate = jenaModel.createProperty(rdf4jstatement.getPredicate().stringValue());
        RDFNode object;

        if (rdf4jstatement.getObject() instanceof org.eclipse.rdf4j.model.Literal) {
            String value =  rdf4jstatement.getObject().stringValue();
            IRI rdf4jDataType = ((org.eclipse.rdf4j.model.Literal) rdf4jstatement.getObject()).getDatatype();
            RDFDatatype dataType = TypeMapper.getInstance().getSafeTypeByName(rdf4jDataType.stringValue());

            object = jenaModel.createTypedLiteral(value, dataType);
        } else {
            if(rdf4jstatement.getObject().isIRI()) {
                object = jenaModel.createResource(rdf4jstatement.getObject().stringValue());
            } else {
                if(bnodeMapping.containsKey(rdf4jstatement.getObject().stringValue())) {
                    object = bnodeMapping.get(rdf4jstatement.getObject().stringValue());
                } else {
                    object = jenaModel.createResource();
                    bnodeMapping.put(rdf4jstatement.getObject().stringValue(), (Resource) object);
                }
            }
        }

        org.apache.jena.rdf.model.Statement jenaStatement = new StatementImpl(subject, predicate, object);
        return jenaStatement;
    }

    public static Statement toRDF4JStatement(org.apache.jena.rdf.model.Statement jenaStatement) {
        return toRDF4JStatement(jenaStatement, new HashMap<Resource, org.eclipse.rdf4j.model.Resource>());
    }
    //TODO: Same thing as the other way round. Bnode mapping needed?
    public static Statement toRDF4JStatement(org.apache.jena.rdf.model.Statement jenaStatement, HashMap<Resource, org.eclipse.rdf4j.model.Resource> bnodeMapping) {

        org.eclipse.rdf4j.model.Resource subject;
        Resource jenaSubject = jenaStatement.getSubject().asResource();
        if (!jenaSubject.isAnon()) {
            subject = SimpleValueFactory.getInstance().createIRI(jenaSubject.getURI());
        } else {
            if(bnodeMapping.containsKey(jenaSubject)){
                subject = bnodeMapping.get(jenaSubject);
            } else {
                subject = SimpleValueFactory.getInstance().createBNode();
                bnodeMapping.put(jenaSubject, subject);
            }

        }
        IRI predicate = SimpleValueFactory.getInstance().createIRI(jenaStatement.getPredicate().getURI());
        Value object;

        if (jenaStatement.getObject().isLiteral()) {
            Literal literal = jenaStatement.getObject().asLiteral();
            object = SimpleValueFactory.getInstance().createLiteral(literal.getString(), SimpleValueFactory.getInstance().createIRI(literal.getDatatypeURI()));
        } else {
            Resource jenaobject = jenaStatement.getObject().asResource();
            if (!jenaobject.isAnon()) {
                object = SimpleValueFactory.getInstance().createIRI(jenaobject.getURI());
            } else {
                if(bnodeMapping.containsKey(jenaobject)){
                    object = bnodeMapping.get(jenaobject);
                } else {
                    object = SimpleValueFactory.getInstance().createBNode();
                    bnodeMapping.put(jenaobject, (org.eclipse.rdf4j.model.Resource) object);
                }
            }

        }

        return SimpleValueFactory.getInstance().createStatement(subject, predicate, object);
    }

    public static org.eclipse.rdf4j.model.Model fromJSONObject(JSONObject jsobject) {
        //TODO: rdf4j does not seem to accept types that are not IRIs (which might be technically a correct implementation). Might be better to somehow rely on the jsonld library to convert to rdf first?
        //TODO: Fail correctly in case of illegalargumentexception
        String jsonString = jsobject.toString();
        //jsonString = jsonLdProfileRegistry.expandJsonLd(jsonString);
        StringReader jsonReader = new StringReader(jsonString);

        RDFFormat rdf4jFormat = RDF4JFormatMapper.map(Format.JSON_LD);
        try {
            org.eclipse.rdf4j.model.Model model =  Rio.parse(jsonReader, rdf4jFormat);
            return model;
        } catch (RDFParseException rex) {
            throw new FormatException(rex.getMessage(), rex);
        } catch (RDFHandlerException rex) {
            throw new FormatException(rex.getMessage(), rex);
        } catch (IOException rex) {
            throw new FormatException(rex.getMessage(), rex);
        }
    }
}
