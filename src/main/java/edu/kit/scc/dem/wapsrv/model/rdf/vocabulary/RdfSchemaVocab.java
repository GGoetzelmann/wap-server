package edu.kit.scc.dem.wapsrv.model.rdf.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * This class provides the vocabulary for RDF schema (https://www.w3.org/2000/01/rdf-schema#) this class currently only
 * implements the needed "label"
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class RdfSchemaVocab {
   /**
    * label
    */
   public static IRI label = buildIri("label");
   private static final String ROOT = "http://www.w3.org/2000/01/rdf-schema#";

   private static IRI buildIri(String property) {

      return SimpleValueFactory.getInstance().createIRI(ROOT + property);
   }
}
