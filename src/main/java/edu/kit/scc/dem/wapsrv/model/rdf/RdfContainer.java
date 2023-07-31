package edu.kit.scc.dem.wapsrv.model.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import edu.kit.scc.dem.wapsrv.exceptions.InvalidContainerException;
import edu.kit.scc.dem.wapsrv.model.Container;
import edu.kit.scc.dem.wapsrv.model.FormattableObject;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.AsVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.LdpVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfSchemaVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfVocab;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.hibernate.mapping.SimpleValue;

/**
 * Implements a Container with RDF commons as data backend
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class RdfContainer extends RdfWapObject implements Container {
   /** prefer minimal container was requested */
   protected boolean preferMinimalContainer;
   /** prefer iris only was requested */
   protected boolean preferIrisOnly;

   /**
    * Creates a new RdfContainer object using the given parameters results in preferMinimalContainer = true
    * 
    * @param dataset
    *                        The data set used as data backend
    * @param rdfBackend
    *                        The RDF backend
    * @param newContainerIri
    *                        The new target IRI of the Container
    */
   public RdfContainer(Model dataset, RdfBackend rdfBackend, IRI newContainerIri) {
      this(dataset, true, true, rdfBackend, newContainerIri);
   }

   /**
    * Creates a new RdfContainer object using the given parameters
    * 
    * @param dataset
    *                               The data set used as data backend
    * @param preferMinimalContainer
    *                               true, if preferMinimalContainer was requested
    * @param preferIrisOnly
    *                               true, if only IRIs was requested
    * @param rdfBackend
    *                               The RDF backend
    * @param newContainerIri
    *                               The new target IRI of the Container, null if no renaming should be done
    */
   public RdfContainer(Model dataset, boolean preferMinimalContainer, boolean preferIrisOnly, RdfBackend rdfBackend,
         IRI newContainerIri) {
      super(dataset, rdfBackend);
      // init object
      this.preferMinimalContainer = preferMinimalContainer;
      this.preferIrisOnly = preferIrisOnly;
      Resource iri = getIriForType(LdpVocab.basicContainer);
      Resource iriAnnotationCollection = getIriForType(AsVocab.orderedCollection);
      if (iri == null | iriAnnotationCollection == null) {
         throw new InvalidContainerException(
               "The given data does not represent a valid container, type is missing or does not match: "
                     + "A Container has to be an ldp:BasicContainer AND an AnnotationCollection. "
                     + "Another reason for this could be that the ID was not a valid URI/IRI.");
      }
      // rename the container here, because the generation of the RDF:seq names does not work with BlankNodes
      if (newContainerIri != null) {
         // call the super to not try to rename not existing RDF:seq names
         super.setIri(newContainerIri, true);
      }
      // If the RDF.sequence entry for the container does not exist, it will be added here.
      if (!dataset.contains(Container.toContainerSeqIri((IRI) getIri()), RdfVocab.type, RdfVocab.seq)) {
         dataset.add(Container.toContainerSeqIri((IRI) getIri()), RdfVocab.type, RdfVocab.seq);
      }
      // If the RDF.sequence entry for the container does not exist, it will be added here.
      if (!dataset.contains(Container.toAnnotationSeqIri((IRI) getIri()), RdfVocab.type, RdfVocab.seq)) {
         dataset.add(Container.toAnnotationSeqIri((IRI) getIri()), RdfVocab.type, RdfVocab.seq);
      }
      // DON'T put things to just change the container for output here. use the RdfOutputContainer class for that.
   }

   /**
    * Creates a new RdfContainer object using the given parameters
    * 
    * @param dataset
    *                               The data set used as data backend
    * @param preferMinimalContainer
    *                               true, if preferMinimalContainer was requested
    * @param preferIrisOnly
    *                               true, if only IRIs was requested
    * @param rdfBackend
    *                               The RDF backend
    */
   public RdfContainer(Model dataset, boolean preferMinimalContainer, boolean preferIrisOnly, RdfBackend rdfBackend) {
      this(dataset, preferMinimalContainer, preferIrisOnly, rdfBackend, null);
   }

   /*
    * @see edu.kit.scc.dem.wapsrv.model.Container#getLabel()
    */
   @Override
   public String getLabel() {
      return getValue(RdfSchemaVocab.label);
   }

   /*
    * @see edu.kit.scc.dem.wapsrv.model.Container#createDefaultLabel()
    */
   @Override
   public void createDefaultLabel() {
      String label = getValue(RdfSchemaVocab.label);
      Literal labelLiteral;
      if (label == null) {
         labelLiteral = SimpleValueFactory.getInstance().createLiteral(getIriString());
      } else {
         labelLiteral = SimpleValueFactory.getInstance().createLiteral(label);
      }
      dataset.add(iri, RdfSchemaVocab.label, labelLiteral);
   }

   /*
    * @see edu.kit.scc.dem.wapsrv.model.Container#isMinimalContainer()
    */
   @Override
   public boolean isMinimalContainer() {
      return preferMinimalContainer;
   }

   /*
    * (non-Javadoc)
    * @see edu.kit.scc.dem.wapsrv.model.rdf.RdfWapObject#setIri(org.apache.commons.rdf.api.IRI)
    */
   @Override
   public void setIri(IRI newIri, boolean copyVia) {
      RdfUtilities.renameNodeIri(dataset, Container.toContainerSeqIri(getIri()), Container.toContainerSeqIri(newIri));
      RdfUtilities.renameNodeIri(dataset, Container.toAnnotationSeqIri(getIri()), Container.toAnnotationSeqIri(newIri));
      super.setIri(newIri, copyVia);
   }

   @Override
   public Type getType() {
      return FormattableObject.Type.CONTAINER;
   }
}
