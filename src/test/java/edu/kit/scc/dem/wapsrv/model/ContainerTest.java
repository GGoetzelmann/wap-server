/**
 * 
 */
package edu.kit.scc.dem.wapsrv.model;

import static org.junit.jupiter.api.Assertions.*;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfModelFactory;
import edu.kit.scc.dem.wapsrv.repository.jena.JenaRdfBackend;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests the interface Container
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {JenaRdfBackend.class, RdfModelFactory.class})
@ActiveProfiles("test")
class ContainerTest {
   @Autowired
   private ModelFactory modelFactory;

   /**
    * Test to annotation seq IRI.
    */
   @Test
   final void testToAnnotationSeqIri() {
      String iri = "http://www.example.org/container2/";
      IRI node = SimpleValueFactory.getInstance().createIRI(iri);
      IRI newIri = Container.toAnnotationSeqIri(node);
      //TODO: Changed assertion, check if correct
      assertEquals(newIri.toString(), iri + "#annotations", "The IRI equals not the expected string");
   }

   /**
    * Test to annotation seq IRI string.
    */
   @Test
   final void testToAnnotationSeqIriString() {
      String iri = "http://www.example.org/container2/";
      String newIri = Container.toAnnotationSeqIriString(iri);
      assertEquals(newIri.toString(), iri + "#annotations", "The IRI equals not the expected string");
   }

   /**
    * Test to container seq IRI.
    */
   @Test
   final void testToContainerSeqIri() {
      String iri = "http://www.example.org/container2/";
      IRI node = SimpleValueFactory.getInstance().createIRI(iri);
      IRI newIri = Container.toContainerSeqIri(node);
      //Changed assertion, does it matter for usage?
      assertEquals(newIri.stringValue(), iri + "#containers", "The IRI equals not the expected string");
   }

   /**
    * Test to container seq IRI string.
    */
   @Test
   final void testToContainerSeqIriString() {
      String iri = "http://www.example.org/container2/";
      String newIri = Container.toContainerSeqIriString(iri);
      assertEquals(newIri.toString(), iri + "#containers", "The IRI equals not the expected string");
   }
}
