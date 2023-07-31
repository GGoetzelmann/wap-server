package edu.kit.scc.dem.wapsrv.model.rdf;

import java.text.SimpleDateFormat;
import java.util.*;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.DynamicModel;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import org.eclipse.rdf4j.model.vocabulary.XSD;

/**
 * Utility class with common operations on graphs and datasets
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class RdfUtilities {
   /**
    * Renames an existing IRI to a new one. The IRI can either be in a subject or an object position of a statement
    * 
    * @param sourceGraph
    *                    The graph to rename the IRI in
    * @param oldIdIri
    *                    The old IRI
    * @param newIri
    *                    The new IRI
    */
   public static void renameNodeIri(final Model sourceGraph, IRI oldIdIri, IRI newIri) {
      SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
      // Find all the triples containing the IRI

      Map<Statement, Statement> statementsToUpdate = new HashMap<>();
      for (Statement statement : sourceGraph.filter(oldIdIri, null, null)) {
         Resource subject = statement.getSubject();
         Value objectValue = statement.getObject();

         Resource newSubject = subject;
         Value newObject = objectValue;
         if (subject.isIRI()) {
            newSubject = subject.equals(oldIdIri) ? newIri : subject;
         }

         if (objectValue.isIRI()) {
            newObject = objectValue.equals(oldIdIri) ? newIri : objectValue;
         }

         if (!subject.equals(newSubject) || !objectValue.equals(newObject)) {
            Statement updatedStatement = valueFactory.createStatement(newSubject, statement.getPredicate(), newObject);
            statementsToUpdate.put(statement, updatedStatement);
         }


      }

      for (Map.Entry<Statement, Statement> entry : statementsToUpdate.entrySet()) {
         Statement oldStatement = entry.getKey();
         Statement newStatement = entry.getValue();

         sourceGraph.add(newStatement);
         sourceGraph.remove(oldStatement);
      }

   }

   public static void BNodesToIRI(final Model sourceGraph, BNode oldNode, IRI newIri) {
      //TODO: Refactor duplicate code
      SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
      // Find all the triples containing the IRI

      Map<Statement, Statement> statementsToUpdate = new HashMap<>();
      for (Statement statement : sourceGraph.filter(oldNode, null, null)) {
         Resource subject = statement.getSubject();
         Value objectValue = statement.getObject();

         Resource newSubject = subject;
         Value newObject = objectValue;
         if (subject.isBNode()) {
            newSubject = subject.equals(oldNode) ? newIri : subject;
         }

         if (objectValue.isBNode()) {
            newObject = objectValue.equals(oldNode) ? newIri : objectValue;
         }

         if (!subject.equals(newSubject) || !objectValue.equals(newObject)) {
            Statement updatedStatement = valueFactory.createStatement(newSubject, statement.getPredicate(), newObject);
            statementsToUpdate.put(statement, updatedStatement);
         }


      }

      for (Map.Entry<Statement, Statement> entry : statementsToUpdate.entrySet()) {
         Statement oldStatement = entry.getKey();
         Statement newStatement = entry.getValue();

         sourceGraph.add(newStatement);
         sourceGraph.remove(oldStatement);
      }
   }

   /**
    * Clones a graph object with the help of a given RDF factory
    * 
    * @param  sourceGraph
    *                     The graph to clone
    * @param  factory
    *                     The RDF factory
    * @return             The cloned graph
    */
   public static Model clone(Model sourceGraph) {
      Model targetGraph = new LinkedHashModel();
      // Find all the triples containing the IRI
      Iterator<Statement> it = sourceGraph.iterator();
      it.forEachRemaining(targetGraph::add);
      return targetGraph;
   }

   /**
    * Clones a data set object with the help of a given RDF factory.
    *
    * @param  sourceDataset
    *                       The data set to clone
    * @param  factory
    *                       The RDF factory
    * @return               the dataset

   public static Model clone(Model sourceDataset) {
      throw new InternalServerException("Not yet implemented : RdfUtilities.clone()");
   }
    */

   /**
    * Converts nString to string. nStrings come from the RDF Store and include enclosing characters like
    * quotes(&lt;,&gt;) and quotes ("). This enclosing characters will be removed.
    *
    * @param  nString
    *                 the nString
    * @return         the stripped string
    */
   public static String nStringToString(String nString) {
      String firstChar = nString.substring(0, 1);
      String lastChar = nString.substring(nString.length() - 1, nString.length());
      if ((firstChar.equals(lastChar) && firstChar.equals("\"")) || (firstChar.equals("<") && lastChar.equals(">"))) {
         return nString.substring(1, nString.length() - 1);
      }
      return nString;
   }

   /**
    * Get the whole subgraph denoted by the given root as a new dataset
    * 
    * @param  dataset
    *                 dataset to extract the data from
    * @param  rdf
    *                 the RDF Backend to be used
    * @param  rootIri
    *                 the IRI of the root of the subGraph to extract
    * @return         a Dataset With only the Graph of the rootIRI included.
    */
   public static Model getSubDataset(Model dataset, Resource rootIri) {
      Model subset = new LinkedHashModel();
      List<Resource> termsToCheck = new Vector<Resource>();
      Set<Resource> termsChecked = new HashSet<Resource>();
      termsToCheck.add(rootIri);
      for (int n = 0; n < termsToCheck.size(); n++) {
         Resource iri = termsToCheck.get(n);
         termsChecked.add(iri);
         //Iterable<Statement> iterator = dataset.iterate(null, iri, null, null);
         for (Statement quad : dataset.filter(iri, null, null)) {
            // System.out.println(quad);
            subset.add(quad);
            if (quad.getObject() instanceof Resource) {
               if (!termsChecked.contains(quad.getObject())) {
                  termsToCheck.add((Resource) quad.getObject());
               }
            }
         }
      }
      return subset;
   }

   /**
    * Generates a Rdf literal from a calendar object.
    *
    * @param  calendar
    *                  the calendar object
    * @param  rdf
    *                  the RDF Backend to be used
    * @return          the RDF literal
    */
   public static Literal rdfLiteralFromCalendar(Calendar calendar) {
      SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
      String dateTimeString = sdf.format(calendar.getTime());

      Literal timedate = valueFactory.createLiteral(dateTimeString, XSD.DATETIME);
      return timedate;
   }
}
