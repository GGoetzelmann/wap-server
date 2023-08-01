package edu.kit.scc.dem.wapsrv.model.rdf;


import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.model.Container;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.AsVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.LdpVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfVocab;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XSD;

/**
 * The class is used to generate the correct output representation of the Container. It extends the Container class and
 * overrides functions to generate the dataset for the output
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class RdfOutputContainer extends RdfContainer {
   /**
    * Instantiates a new RDF output container.
    *
    * @param dataset
    *                               the dataset with the RDF data from the database
    * @param preferMinimalContainer
    *                               true, if prefer minimal container was requested
    * @param preferIrisOnly
    *                               true, if prefer iris only was requested
    * @param rdfBackend
    *                               the RDF backend to be used
    */
   public RdfOutputContainer(Model dataset, boolean preferMinimalContainer, boolean preferIrisOnly,
                             RdfBackend rdfBackend) {
      super(dataset, preferMinimalContainer, preferIrisOnly, rdfBackend);
      if (preferIrisOnly) {
         setIri(getIriString() + "?iris=1", false);
      } else {
         setIri(getIriString() + "?iris=0", false);
      }
   }

   /*
    * (non-Javadoc)
    * @see edu.kit.scc.dem.wapsrv.model.rdf.RdfWapObject#getDataset()
    */
   @Override
   public Model getDataset() {
      // Because the sequence head is also included we need to substract 1
      long annoCount = dataset.filter(Container.toAnnotationSeqIri(getIri()), null, null).stream().count() - 1;
      Literal annoCountLiteral
            = SimpleValueFactory.getInstance().createLiteral(String.valueOf(annoCount), XSD.NON_NEGATIVE_INTEGER);
      dataset.add(getIri(), AsVocab.totalItems, annoCountLiteral);
      // Don't show first and last if there are no Annotation, hence no page.
      if (annoCount != 0) {
         int pageCount
               = (int) (Math.floor((annoCount - 1.0f) / (WapServerConfig.getInstance().getPageSize() + 0.0f)) + 1);
         IRI firstIri = SimpleValueFactory.getInstance().createIRI(getIriString() + "&page=0");
         int lastPage = pageCount == 0 ? 0 : pageCount - 1;
         IRI lastIri = SimpleValueFactory.getInstance().createIRI(getIriString() + "&page=" + lastPage);
         dataset.add(getIri(), AsVocab.first, firstIri);
         dataset.add(getIri(), AsVocab.last, lastIri);
      }
      // Do not add contains in minimalContainer
      if (!preferMinimalContainer) {
         // Add subcontainer in contains according to the sequence
         dataset.filter(Container.toContainerSeqIri(getIri()), null, null).forEach(t -> {
            if (!t.getObject().equals(RdfVocab.seq)) {
               dataset.add(getIri(), LdpVocab.contains, t.getObject());
            }
         });
      }
      // Delete sequence for container
      dataset.remove(Container.toContainerSeqIri(getIri()), null, null);
      // Delete sequence for annotations
      dataset.remove(Container.toAnnotationSeqIri(getIri()), null, null);
      return super.getDataset();
   }
}
