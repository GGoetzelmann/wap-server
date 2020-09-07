package edu.kit.scc.dem.wapsrv.repository.jena;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.system.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import edu.kit.scc.dem.wapsrv.exceptions.FormatException;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfBackend;

/**
 * The Jena implementation of the RdfBackend interface.<br>
 * This class is used to generate datasets from Strings and Strings from datasets. It is Jena-dependant.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@Component
public class JenaRdfBackend implements RdfBackend {
   /**
    * The backend for usage in tests
    */
   public static RdfBackend instance;
   /**
    * The logger to use
    */
   private final Logger logger = LoggerFactory.getLogger(this.getClass());
   /**
    * The Jena RDF
    */
   private JenaRDF rdf;

   /**
    * Creates a new Jena RDF Object to use as the RDF backend
    */
   public JenaRdfBackend() {
      JenaSystem.init();
      this.rdf = new JenaRDF();
      logger.info("Jena initialized and RDF Backend instantiated");
      instance = this;
   }

   @Override
   public RDF getRdf() {
      return this.rdf;
   }

   @Override
   public String getOutput(Dataset dataset, Format format) throws WapException {
      Lang lang = JenaFormatMapper.map(format);
      if (lang == null) {
         throw new FormatException("Format " + format + " not supported in jena RDF backend");
      }
      Graph graph = rdf.asJenaGraph(dataset.getGraph());
      StringWriter writer = new StringWriter();
      RDFDataMgr.write(writer, graph, lang);
      // StringWriters do not have to be closed!
      return writer.toString();
   }

   @Override
   public Dataset readFromString(String serialization, final Format format) throws WapException {
      final Lang lang = JenaFormatMapper.map(format);
      if (lang == null) {
         throw new FormatException("Format " + format + " not supported in jena RDF backend");
      }
      ByteArrayInputStream in = new ByteArrayInputStream(serialization.getBytes());
      // org.apache.jena.query.Dataset datasetGraph=null;
      org.apache.jena.sparql.core.DatasetGraph datasetGraph = rdf.createDataset().asJenaDatasetGraph();
      try {
         RDFDataMgr.read(datasetGraph, in, lang);
      } catch (RiotException rex) {
         throw new FormatException(rex.getMessage(), rex);
      }
      // closing byte array input streams is not needed
      return rdf.asDataset(datasetGraph);
   }

   @Override
   public boolean isValidInputFormat(Format format) {
      // we allow only JSON-LD for now
      // return format == Format.JSON_LD;
      return JenaFormatMapper.map(format) != null;
   }
}
