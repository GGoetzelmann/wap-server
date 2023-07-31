package edu.kit.scc.dem.wapsrv.repository;

import edu.kit.scc.dem.wapsrv.model.WapObject;
import org.eclipse.rdf4j.model.Model;

/**
 * Provides the functions for all the necessary operations in the database that could not have been handled in RDF. The
 * red:seq operations for example are not implemented in apache.commons.rdf so we are using the functions of the
 * rdf-backend here.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface WapObjectRepository extends TransactionRepository {
   /**
    * Adds the element to rdf:seq.
    *
    * @param modelIri
    *                 the graph/model IRI
    * @param seqIri
    *                 the sequence IRI
    * @param objIri
    *                 the objects IRI to be put into the rdf:seq
    */
   void addElementToRdfSeq(String modelIri, String seqIri, String objIri);

   /**
    * Removes the element from rdf:seq.
    *
    * @param modelIri
    *                 the graph/model IRI
    * @param seqIri
    *                 the sequence IRI
    * @param objIri
    *                 the objects IRI to be removed from the rdf:seq
    */
   void removeElementFromRdfSeq(String modelIri, String seqIri, String objIri);

   /**
    * Count elements in rdf:seq.
    *
    * @param  modelIri
    *                  the graph/model IRI
    * @param  seqIri
    *                  the sequence IRI
    * @return          the count of elements in the rdf:seq
    */
   int countElementsInSeq(String modelIri, String seqIri);

   /**
    * Write the RDF data of a WapObject to database.
    *
    * @param wapObject
    *                  the WapObject
    */
   void writeObjectToDatabase(WapObject wapObject);

   /**
    * Gets the WapObject from the Database
    *
    * @param  iri
    *             the IRI of the WapObject to read
    * @return     the WapObject
    */
   Model getWapObject(String iri);

   void updateTriple(String modelIri, org.eclipse.rdf4j.model.Statement oldStatement, org.eclipse.rdf4j.model.Statement newStatement);

   void addTriple(String modelIri, org.eclipse.rdf4j.model.Statement statement);

   void removeTriple(String modelIri, org.eclipse.rdf4j.model.Statement statement);

   void removeAll(String iri);
}
