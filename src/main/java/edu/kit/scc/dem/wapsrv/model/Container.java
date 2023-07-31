package edu.kit.scc.dem.wapsrv.model;

import edu.kit.scc.dem.wapsrv.model.rdf.RdfUtilities;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * The container interface defines all methods special to containers. These address root containers, labels and other
 * container-only metadata, page information and the number of contained annotations.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface Container extends WapObject {
   /**
    * Gets the human readable label of the container
    * 
    * @return The label
    */
   String getLabel();

   /**
    * Creates a default label value (=name of the container) if none exists
    */
   void createDefaultLabel();

   /**
    * Checks if PreferMinimalContainer preference is set
    * 
    * @return True if PreferMinimalContainer
    */
   boolean isMinimalContainer();

   /**
    * Generates the IRI for the rdf:seq of a container holding the annotations.
    *
    * @param  iri
    *             the IRI of the container
    * @return     the IRI of the rdf:seq for Annotations
    */
   static IRI toAnnotationSeqIri(IRI iri) {
      return SimpleValueFactory.getInstance().createIRI(toAnnotationSeqIriString(RdfUtilities.nStringToString(iri.stringValue())));
   }

   /**
    * Generates the IRI String for the rdf:seq of a container holding the annotations.
    *
    * @param  containerIriString
    *                            the IRI of the container
    * @return                    the IRI String of the rdf:seq for Annotations
    */
   static String toAnnotationSeqIriString(String containerIriString) {
      return containerIriString + "#annotations";
   }

   /**
    * Generates the IRI for the rdf:seq of a container holding the subcontainers.
    *
    * @param  iri
    *             the IRI of the container
    * @return     the IRI of the rdf:seq for Subcontainers
    */
   static IRI toContainerSeqIri(IRI iri) {
      return SimpleValueFactory.getInstance().createIRI(toContainerSeqIriString(RdfUtilities.nStringToString(iri.stringValue())));
   }

   /**
    * Generates the IRI String for the rdf:seq of a container holding the subcontainers.
    *
    * @param  containerIriString
    *                            the IRI of the container
    * @return                    the IRI String of the rdf:seq for Subcontainers
    */
   static String toContainerSeqIriString(String containerIriString) {
      return containerIriString + "#containers";
   }
}
