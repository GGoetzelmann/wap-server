package edu.kit.scc.dem.wapsrv.model.rdf;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import org.eclipse.rdf4j.model.*;
import edu.kit.scc.dem.wapsrv.model.WapObject;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.AnnoVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.DcTermsVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.WapVocab;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Basic RdfWapObject implementing all common functionality of Annotations and Containers.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public abstract class RdfWapObject implements WapObject {
   /**
    * The RdfBackend
    */
   protected final RdfBackend rdfBackend;
   /**
    * The data set used as internal model
    */
   protected final Model dataset;
   /** The IRI of the WapObject */
   protected Resource iri;
   private String etag;

   /**
    * Creates a new RdfWapObject object using the given parameters
    * 
    * @param dataset
    *                   The data set used as data backend
    * @param rdfBackend
    *                   The RDF backend
    */

   public RdfWapObject(Model dataset, RdfBackend rdfBackend) {
      if (dataset.size() == 0) {
         throw new WapException("No triples in dataset, WAP object cannot be created", 500);
      }
      this.dataset = dataset;
      this.rdfBackend = rdfBackend;

      Model typeStatements = dataset.filter(null, RdfVocab.type, null);
      //Get the IRI from the statements. potentially dangerous to do so
      //TODO: move somewhere and make more robust?
      for (Statement tStatement : typeStatements) {
         if(tStatement.getObject().isIRI() && tStatement.getObject().equals(RdfVocab.seq)) continue;
         if(tStatement.getObject().isIRI() && tStatement.getObject().stringValue().equals("http://www.w3.org/ns/activitystreams#OrderedCollectionPage")) continue;
         Resource typeSubject = tStatement.getSubject();
         //Triple is describing a type and the subject is 'top level' - not object of any triple.
         if(!dataset.filter(null, null, typeSubject).stream().anyMatch(element -> true)) {
            this.iri = typeSubject;
            break;
         }
      }
      // Extract ETag and remove from data set.
      Optional<? extends Statement> etagTriple = dataset.filter(null, WapVocab.etag, null).stream().findFirst();
      if (etagTriple.isPresent()) {
         etag = RdfUtilities.nStringToString(etagTriple.get().getObject().stringValue());
         Resource etagSubject = etagTriple.get().getSubject();
         dataset.remove(etagSubject, WapVocab.etag, null);
      } else {
         // Set eTag null -> to be handled in Repository
         etag = null;
      }
   }

   /**
    * Gets the IRI for the given type.
    *
    * @param  type
    *              the IRI representation of the rdf:type
    * @return      the IRI of the node declaring the given rdf:type
    */
   public Resource getIriForType(IRI type) {
      Optional<? extends Statement> triple = dataset.filter(null, RdfVocab.type, type).stream().findFirst();
      if (!triple.isPresent()) {
         // Not a valid WapObject of the defined Type
         return null;
      }
      return triple.get().getSubject();
   }

   @Override
   public Model getDataset() {
      return dataset;
   }

   @Override
   public String toString(Format format) {
      String serialized = rdfBackend.getOutput(getDataset(), format);
      return serialized;
   }

   @Override
   public String getEtagQuoted() {
      return "\"" + getEtag() + "\"";
   }

   @Override
   public String getEtag() {
      return etag;
   }

   @Override
   public void setEtag(String etag) {
      this.etag = etag;
   }

   @Override
   public IRI getIri() {
      if (iri.isIRI()) {
         return (IRI) iri;
      }
      if (iri.isBNode()) {
         return SimpleValueFactory.getInstance().createIRI("_:" + ((BNode) iri).getID());
      }
      return null;
   }

   //TODO: iri handling is not smooth anymore
   @Override
   public void setIri(IRI newIri, boolean copyVia) {

      if (!iri.isBNode()) {
         //check can only happen if IRI is not currently a bnode id
         if (newIri.equals((IRI) iri))
            return;
         IRI oldIRI = (IRI) iri;
         RdfUtilities.renameNodeIri(dataset, oldIRI, newIri);
         iri = newIri;
         if (copyVia) {
            dataset.add(iri, AnnoVocab.via, oldIRI);
         }
      } else {
         RdfUtilities.BNodesToIRI(dataset, (BNode) iri, newIri);
      }


      iri = newIri;
   }

   @Override
   public void setIri(String iri) {
      setIri(SimpleValueFactory.getInstance().createIRI(iri));
   }

   @Override
   public void setIri(String iri, boolean copyVia) {
      setIri(SimpleValueFactory.getInstance().createIRI(iri), copyVia);
   }

   @Override
   public void setIri(IRI iri) {
      setIri(iri, true);
   }

   @Override
   public void setCreated() {
      if (!dataset.contains(iri, DcTermsVocab.created, null)) {
         Calendar calendar = Calendar.getInstance();
         Literal timedate = RdfUtilities.rdfLiteralFromCalendar(calendar);
         dataset.add(iri, DcTermsVocab.created, timedate);
      }
   }

   @Override
   public String getValue(IRI propertyName) {
      Optional<? extends Statement> triple = dataset.filter(iri, propertyName, null).stream().findFirst();
      if (triple.isPresent()) {
         return RdfUtilities.nStringToString(triple.get().getObject().stringValue());
      }
      return null;
   }

   @Override
   public List<String> getValues(IRI propertyName) {
      Vector<String> values = new Vector<>();
      dataset.filter(iri, propertyName, null).forEach(t -> {
         values.add(RdfUtilities.nStringToString(t.getObject().stringValue()));
      });
      return values;
   }

   @Override
   public boolean isDeleted() {
      return hasProperty(WapVocab.deleted);
   }
}
