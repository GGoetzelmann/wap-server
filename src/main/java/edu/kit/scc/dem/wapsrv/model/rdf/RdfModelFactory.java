package edu.kit.scc.dem.wapsrv.model.rdf;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import edu.kit.scc.dem.wapsrv.exceptions.NotAnAnnotationException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.AnnotationList;
import edu.kit.scc.dem.wapsrv.model.Container;
import edu.kit.scc.dem.wapsrv.model.ModelFactory;
import edu.kit.scc.dem.wapsrv.model.Page;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.AnnoVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfVocab;

/**
 * The RdfModelFactory implementation. Relies on an actual {@link RDF} implementation.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@Component
public class RdfModelFactory implements ModelFactory {
   /**
    * The used RDF backend
    */
   private RdfBackend rdfBackend;

   /**
    * Sets the RDF backend via autowire
    * 
    * @param rdfBackend
    *                   The RDF backend
    */
   @Autowired
   private void setRdfBackend(RdfBackend rdfBackend) {
      this.rdfBackend = rdfBackend;
      //this.rdf = rdfBackend.getRdf();
   }

   @Override
   public AnnotationList createAnnotationList(String rawAnnotation, Format format) {
      AnnotationList annotationList = new RdfAnnotationList(rdfBackend);
      Model dataset = rdfBackend.readFromString(rawAnnotation, format);
      Set<Resource> annotationIris = new HashSet<Resource>();
      Model iterator = dataset.filter(null, RdfVocab.type, AnnoVocab.annotation);
      for (Statement statement : iterator) {
         annotationIris.add(statement.getSubject());
      }
      if (annotationIris.isEmpty()) {
         throw new NotAnAnnotationException();
      }
      for (Resource iri : annotationIris) {
         Model annoDataset = RdfUtilities.getSubDataset(dataset, iri);
         annotationList.addAnnotation(new RdfAnnotation(annoDataset, rdfBackend));
      }
      return annotationList;
   }

   @Override
   public Annotation createAnnotation(Model dataSet) {
      return new RdfAnnotation(dataSet, rdfBackend);
   }

   @Override
   public Annotation createAnnotation(String rawAnnotation, Format format) {
      Annotation annotation = new RdfAnnotation(rdfBackend.readFromString(rawAnnotation, format), rdfBackend);
      return annotation;
   }

   @Override
   public boolean isValidInputFormat(Format format) {
      return rdfBackend.isValidInputFormat(format);
   }

   @Override
   public Page createPage(Model dataset, String containerIri, int pageNr, boolean preferIrisOnly, boolean isEmbedded,
         int annoTotalCount, String modified, String label) {
      return new RdfPage(dataset, containerIri, pageNr, preferIrisOnly, isEmbedded, annoTotalCount, modified, label,
            rdfBackend);
   }

   @Override
   public Container createContainer(Model dataset, boolean preferMinimalContainer, boolean preferIrisOnly) {
      return new RdfOutputContainer(dataset, preferMinimalContainer, preferIrisOnly, rdfBackend);
   }

   @Override
   public Container createContainer(String rawContainer, Format format, String newContainerIri) {
      IRI containerIri = SimpleValueFactory.getInstance().createIRI(newContainerIri);
      RdfContainer container
            = new RdfContainer(rdfBackend.readFromString(rawContainer, format), rdfBackend, containerIri);
      return container;
   }

   @Override
   public String convertFormat(String rawString, Format srcFormat, Format destFormat) {
      Model dataset = rdfBackend.readFromString(rawString, srcFormat);
      return rdfBackend.getOutput(dataset, destFormat);
   }

   @Override
   public Container createContainer(Model dataset) {
      // Default is preferMinimalContainer and irisOnly
      return createContainer(dataset, true, true);
   }
}
