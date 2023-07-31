package edu.kit.scc.dem.wapsrv.model.rdf;

import edu.kit.scc.dem.wapsrv.exceptions.NotAnAnnotationException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.FormattableObject;
import edu.kit.scc.dem.wapsrv.model.WapObject;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.AnnoVocab;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

import java.util.NoSuchElementException;

/**
 * The RdfAnnotation implementation with RDF commons as data backend
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class RdfAnnotation extends RdfWapObject implements Annotation {
   /**
    * Creates a new RdfAnnotation object using the given parameters
    * 
    * @param dataset
    *                   The data set used as data backend
    * @param rdfBackend
    *                   The RDF backend
    */
   public RdfAnnotation(Model dataset, RdfBackend rdfBackend) {
      super(dataset, rdfBackend);
      Resource iri = getIriForType(AnnoVocab.annotation);
      if (iri == null) {
         // no rdf:type info for Annotation found -> Not Annotation
         throw new NotAnAnnotationException();
      }
   }

   @Override
   public String getContainerIri() {
      return WapObject.getParentContainerIriString(getIriString());
   }

   @Override
   public boolean hasTarget() {
      return hasProperty(AnnoVocab.target);
   }

   @Override
   public Type getType() {
      return FormattableObject.Type.ANNOTATION;
   }
}
