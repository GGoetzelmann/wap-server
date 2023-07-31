package edu.kit.scc.dem.wapsrv.model.rdf;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import edu.kit.scc.dem.wapsrv.exceptions.FormatNotAvailableException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.AnnotationList;
import edu.kit.scc.dem.wapsrv.model.FormattableObject;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * The RdfAnnotationList implementation
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class RdfAnnotationList implements AnnotationList {
   /**
    * List of annotations in this annotation list
    */
   private final List<Annotation> annotations = new Vector<Annotation>();
   /**
    * The RdfBackend
    */
   private final RdfBackend rdfBackend;
   /**
    * The container IRI
    */
   private IRI containerIri;
   /**
    * The container ETag
    */
   private String containerEtag;

   /**
    * Creates a new RdfAnnotationList object using the given parameters
    * 
    * @param rdfBackend
    *                   The RDF backend
    */
   public RdfAnnotationList(RdfBackend rdfBackend) {
      this.rdfBackend = rdfBackend;
   }

   @Override
   public List<Annotation> getAnnotations() {
      return annotations;
   }

   @Override
   public String toString(Format format) throws FormatNotAvailableException {
      Model dataset = createUnionDataset();
      return rdfBackend.getOutput(dataset, format);
   }

   /**
    * Creates a dataset combining all annotations into one
    * 
    * @return The combined dataset
    */
   private Model createUnionDataset() {
      Model combinedDataset = new LinkedHashModel();
      for (Annotation anno : annotations) {
         // Maybe there is need to add a quad that links the different annotations, maybe not.
         for (Statement quad : anno.getDataset()) {
            combinedDataset.add(quad);
         }
      }
      return combinedDataset;
   }

   @Override
   public IRI getContainerIri() {
      return containerIri;
   }

   @Override
   public void setContainerIri(IRI containerIri) {
      this.containerIri = containerIri;
   }

   @Override
   public String getContainerEtag() {
      return containerEtag;
   }

   @Override
   public void setContainerEtag(String containerEtag) {
      this.containerEtag = containerEtag;
   }

   @Override
   public void addAnnotation(Annotation anno) {
      annotations.add(anno);
   }

   @Override
   public Iterator<Annotation> iterator() {
      return annotations.iterator();
   }

   @Override
   public void setContainerIri(String containerIri) {
      setContainerIri(SimpleValueFactory.getInstance().createIRI(containerIri));
   }

   @Override
   public Type getType() {
      return FormattableObject.Type.ANNOTATION;
   }
}
