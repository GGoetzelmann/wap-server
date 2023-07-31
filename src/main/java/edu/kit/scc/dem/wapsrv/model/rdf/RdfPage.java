package edu.kit.scc.dem.wapsrv.model.rdf;

import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.FormatNotAvailableException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.ContainerPreference;
import edu.kit.scc.dem.wapsrv.model.FormattableObject;
import edu.kit.scc.dem.wapsrv.model.Page;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.AsVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.DcTermsVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfSchemaVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfVocab;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XSD;

/**
 * Implements a Page with RDF commons as data backend
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class RdfPage implements Page {
   /**
    * The RdfBackend
    */
   private final RdfBackend rdfBackend;
   /**
    * The data set used as internal model
    */
   private final Model dataset;
   private IRI iriPreferOnly;
   private IRI iriFull;
   private int pageNr;
   private int itemTotalCount;
   private int itemsPerPage;
   private String containerIri;
   private boolean preferIrisOnly;
   private AsCollection asCollection;

   /**
    * Creates a new RdfPage object using the given parameters
    * 
    * @param dataset
    *                           The data set used as data backend
    * @param containerIriString
    *                           .
    * @param pageNr
    *                           The page number
    * @param preferIrisOnly
    *                           true to list only annotation IRIs, false to embed whole annotations
    * @param isEmbedded
    *                           true if the Page is for embedding into a container (partOf is not created)
    * @param annoTotalCount
    *                           The total count of annotations in this container
    * @param modified
    *                           The modified value of the underlying container
    * @param label
    *                           .
    * @param rdfBackend
    *                           The RDF backend
    */
   public RdfPage(Model dataset, String containerIriString, int pageNr, boolean preferIrisOnly, boolean isEmbedded,
                  int annoTotalCount, String modified, String label, RdfBackend rdfBackend) {
      this.dataset = dataset;
      this.rdfBackend = rdfBackend;
      this.pageNr = pageNr;
      this.itemTotalCount = annoTotalCount;
      this.containerIri = containerIriString;
      this.preferIrisOnly = preferIrisOnly;
      this.itemsPerPage = WapServerConfig.getInstance().getPageSize();
      iriPreferOnly = getIriPreferOnly();
      iriFull = getIriforPage(pageNr);
      // IRI containerIri = rdfBackend.getRdf().createIRI(containerIriString);
      // Put basic information
      dataset.add(iriFull, RdfVocab.type, AsVocab.orderedCollectionPage);
      if (!isEmbedded) {
         // --- part of content start
         dataset.add(iriFull, AsVocab.partOf, iriPreferOnly);
         dataset.add(iriPreferOnly, AsVocab.totalItems,
               SimpleValueFactory.getInstance().createLiteral(String.valueOf(annoTotalCount), XSD.NON_NEGATIVE_INTEGER));
         dataset.add(iriPreferOnly, DcTermsVocab.modified,
                 SimpleValueFactory.getInstance().createLiteral(modified));
         dataset.add(iriPreferOnly, AsVocab.first, getIriforPage(0));
         dataset.add(iriPreferOnly, AsVocab.last, getIriforPage(getPageCount() - 1));
         // label from container
         dataset.add(iriPreferOnly, RdfSchemaVocab.label, SimpleValueFactory.getInstance().createLiteral(label));
         // --- part of content end
      }
      asCollection = new AsCollection(dataset, iriFull);
      // Set startIndex
      dataset.add(iriFull, AsVocab.startIndex, SimpleValueFactory.getInstance()
            .createLiteral(String.valueOf(getFirstAnnotationPosition()), XSD.NON_NEGATIVE_INTEGER));
      // Put next/prev pages
      if (hasNextPage()) {
         dataset.add(iriFull, AsVocab.next, getIriforPage(pageNr + 1));
      }
      if (hasPreviousPage()) {
         dataset.add(iriFull, AsVocab.prev, getIriforPage(pageNr - 1));
      }
   }

   private IRI getIriPreferOnly() {
      return SimpleValueFactory.getInstance().createIRI(containerIri + "?iris=" + (preferIrisOnly ? 1 : 0));
   }

   private IRI getIriforPage(int pageNr) {
      return SimpleValueFactory.getInstance().createIRI(getIriPreferOnly().stringValue() + "&page=" + pageNr);
   }

   @Override
   public String getIri() {
      return iriPreferOnly.stringValue() + "&page=" + pageNr;
   }

   @Override
   public String toString(Format format) throws FormatNotAvailableException {
      return rdfBackend.getOutput(dataset, format);
   }

   @Override
   public int getPageNr() {
      return pageNr;
   }

   @Override
   public String getContainerIri() {
      return containerIri;
   }

   @Override
   public int getContainerPreference() {
      return preferIrisOnly ? ContainerPreference.PREFER_CONTAINED_IRIS
            : ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS;
   }

   @Override
   public String getNextPage() {
      if (!hasNextPage()) {
         return null;
      } else {
         return iriPreferOnly.stringValue() + "&page=" + (pageNr + 1);
      }
   }

   @Override
   public String getPreviousPage() {
      if (!hasPreviousPage()) {
         return null;
      } else {
         return iriPreferOnly.stringValue() + "&page=" + (pageNr - 1);
      }
   }

   @Override
   public int getFirstAnnotationPosition() {
      // This value has to be delivered as part of the page metadata.
      return pageNr * itemsPerPage;
      // Attention, the sequence count from 1, but this counts up from 0 as usual
      // which means external view: annotation 20 (counted from 0) means annotation 21 in the sequence.
   }

   @Override
   public void addAnnotation(Annotation anno) {
      if (preferIrisOnly) {
         throw new IllegalArgumentException("Cannot add the full annotation if preferIriesOnly");
      }
      if (preferIrisOnly) {
         addAnnotationIri(anno.getIriString());
      }
      asCollection.addItem(anno.getIri());
      Model annoDs = anno.getDataset();
      annoDs.filter(null, null, null).stream().forEach(t -> {
         dataset.add(t);
      });
   }

   @Override
   public void addAnnotationIri(String annoIri) {
      if (!preferIrisOnly) {
         throw new IllegalArgumentException("Cannot add the annotation Iri alone  if not preferIriesOnly");
      }
      asCollection.addItem(SimpleValueFactory.getInstance().createIRI(annoIri));
   }

   @Override
   public Type getType() {
      return FormattableObject.Type.PAGE;
   }

   @Override
   public void closeAdding() {
      asCollection.closeCollection();
   }

   private boolean hasNextPage() {
      // 2 pages (0 and 1) ==> 1 no next (1<2-1==false), 0 has next (0<2-1==true)
      return pageNr < getPageCount() - 1;
   }

   private int getPageCount() {
      return (int) Math.floor((itemTotalCount - 1.0) / (itemsPerPage + 0.0f) + 1);
   }

   private boolean hasPreviousPage() {
      return pageNr > 0;
   }

   @Override
   public Model getDataset() {
      return dataset;
   }

   private class AsCollection {
      boolean isFirstItem;
      boolean isClosed;
      BNode lastNode;
      private Model graph;

      public AsCollection(Model graph, IRI iri) {
         this.isFirstItem = true;
         this.isClosed = false;
         this.graph = graph;
         lastNode = SimpleValueFactory.getInstance().createBNode();
         graph.add(iri, AsVocab.items, lastNode);
      }

      public void addItem(Resource blankNodeOrIRI) {
         if (isClosed) {
            throw new RuntimeException("AsCollection is already closed. It is not allowed to add more Items");
         }
         if (!isFirstItem) {
            BNode nextNode = SimpleValueFactory.getInstance().createBNode();
            graph.add(lastNode, RdfVocab.rest, nextNode);
            lastNode = nextNode;
         } else {
            isFirstItem = false;
         }
         graph.add(lastNode, RdfVocab.first, blankNodeOrIRI);
      }

      public void closeCollection() {
         if (!isClosed) {
            graph.add(lastNode, RdfVocab.rest, RdfVocab.nil);
            isClosed = true;
         }
      }
   }
}
