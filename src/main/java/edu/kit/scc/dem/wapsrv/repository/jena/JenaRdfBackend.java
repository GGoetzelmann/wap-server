package edu.kit.scc.dem.wapsrv.repository.jena;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.model.rdf.RDF4JUtilities;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import edu.kit.scc.dem.wapsrv.exceptions.FormatException;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfBackend;

import org.json.JSONTokener;

import static edu.kit.scc.dem.wapsrv.model.rdf.RDF4JUtilities.fromJSONObject;

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

   //@Autowired
   //private JsonLdProfileRegistry jsonLdProfileRegistry;

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

   /**
    * Creates a new Jena RDF Object to use as the RDF backend
    */
   public JenaRdfBackend() {
      JenaSystem.init();
      logger.info("Jena initialized");
      instance = this;
   }

   @Override
   public String getOutput(Model dataset, Format format) throws WapException {
      Lang lang = JenaFormatMapper.map(format);
      if (lang == null) {
         throw new FormatException("Format " + format + " not supported in jena RDF backend");
      }



      //TODO: Why are we doing this? Do we have to operate with JENA functionalities here?
      String contextString = dataset.subjects().stream().findFirst().get().stringValue();
      Dataset jenaDataset = DatasetFactory.create();
      org.apache.jena.rdf.model.Model jenaModel = jenaDataset.getNamedModel(contextString);

      Graph g = GraphFactory.createJenaDefaultGraph();
      HashMap<String, org.apache.jena.rdf.model.Resource> bnodeMapping = new HashMap<>();
      for (Statement rdf4jStatement: dataset) {
         //TODO: how can we make sure that everything is properly parsed here - relying on string values does not seam like a good idea
         org.apache.jena.rdf.model.Statement jenaStatement = RDF4JUtilities.toJenaStatement(rdf4jStatement, bnodeMapping);

         jenaModel.add(jenaStatement);
         g.add(jenaStatement.asTriple());
      }

      StringWriter writer = new StringWriter();
      //TODO: check if it is enough to provide the model - if so, remove dataset creation
      RDFDataMgr.write(writer, g, lang);
      // StringWriters do not have to be closed!
      return writer.toString();
   }

   @Override
   public Model readFromString(String serialization, final Format format) throws WapException {
      final Lang lang = JenaFormatMapper.map(format);
      if (lang == null) {
         throw new FormatException("Format " + format + " not supported in jena RDF backend");
      }
      ByteArrayInputStream in = new ByteArrayInputStream(serialization.getBytes());
      // org.apache.jena.query.Dataset datasetGraph=null;

      /**
      org.apache.jena.rdf.model.Model jenaModel = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
      try {
         RDFDataMgr.read(jenaModel, in, lang);
      } catch (RiotException rex) {
         throw new FormatException(rex.getMessage(), rex);
      }
      // closing byte array input streams is not needed
      return jenaModel;
       */

      //Data can be an array or a single object (might be always be an array) but rdf4j cannot handle arrays


      if(format.equals(Format.JSON_LD)) {
         Object json = new JSONTokener(in).nextValue();
         if (json instanceof JSONObject) {
            Model model = RDF4JUtilities.fromJSONObject((JSONObject) json);
            return model;
         } else if (json instanceof JSONArray) {
            JSONArray jsonarray = (JSONArray) json;
            Model model = new LinkedHashModel();
            for(int i=0;i<jsonarray.length();i++){
               Model currentModel = RDF4JUtilities.fromJSONObject((JSONObject) jsonarray.get(i));
               model.addAll(currentModel);
            }
            return model;
         }

      }


      RDFFormat rdf4jFormat = RDF4JFormatMapper.map(format);
      try {
         Model model =  Rio.parse(in, rdf4jFormat);
         return model;
      } catch (RDFParseException rex) {
         throw new FormatException(rex.getMessage(), rex);
      } catch (RDFHandlerException rex) {
         throw new FormatException(rex.getMessage(), rex);
      } catch (IOException rex) {
         throw new FormatException(rex.getMessage(), rex);
      }

   }

   @Override
   public boolean isValidInputFormat(Format format) {
      // we allow only JSON-LD for now
      // return format == Format.JSON_LD;
      return JenaFormatMapper.map(format) != null;
   }
}
