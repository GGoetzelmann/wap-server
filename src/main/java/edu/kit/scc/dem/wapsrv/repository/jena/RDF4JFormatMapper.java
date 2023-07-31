package edu.kit.scc.dem.wapsrv.repository.jena;

import edu.kit.scc.dem.wapsrv.model.formats.Format;
import org.apache.jena.riot.Lang;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.util.Hashtable;
import java.util.Map;

/**
 * This is the central point where are formats that should be usable by Jena are registered. Both incoming and outgoing
 * formats must be registered here.
 * <p>
 * Attention: This only indicates triple store support. Depending on the configuration of the server additional
 * validators may be needed for input formats and Formatters for output formats.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public final class RDF4JFormatMapper {
   /**
    * The single instance
    */
   private static final RDF4JFormatMapper INSTANCE = new RDF4JFormatMapper();
   /**
    * The map containing the registered mappings
    */
   private Map<Format, RDFFormat> format2lang = new Hashtable<Format, RDFFormat>();

   /**
    * Constructs the Jena format mapper singleton.
    */
   private RDF4JFormatMapper() {
      format2lang.put(Format.JSON_LD, RDFFormat.JSONLD);
      format2lang.put(Format.TURTLE, RDFFormat.TURTLE);
      format2lang.put(Format.RDF_XML, RDFFormat.RDFXML);
      format2lang.put(Format.NQUADS, RDFFormat.NQUADS);
      format2lang.put(Format.NTRIPLES, RDFFormat.NTRIPLES);
      format2lang.put(Format.RDF_JSON, RDFFormat.RDFJSON);
   }

   /**
    * Maps the given format to the corresponding Jena Lang
    * 
    * @param  format
    *                The format to map
    * @return        The corresponding Jena lang, null if format unsupported or format==null
    */
   public static RDFFormat map(Format format) {
      if (format == null)
         return null;
      return INSTANCE.format2lang.get(format);
   }
}
