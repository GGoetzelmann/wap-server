package edu.kit.scc.dem.wapsrv.repository;

import edu.kit.scc.dem.wapsrv.model.Annotation;
import org.apache.jena.query.Dataset;

import java.util.List;
import java.util.Map;

/**
 * The annotation repository is the interface to the storage database. It is used by the annotation service for all
 * load/store operations. there are currently no operations required here any more. The interface is kept for possible
 * later usage.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface AnnotationRepository extends WapObjectRepository {
   /**
    * Get the dataset of the repository.
    * 
    * @return The dataset
    */
   Dataset getDataBase();

   /**
    * get all annotations matching the value of the provided WADM property (target, body, selector, ...)
    * Annotations to be returned iff they match ALL provided property value pairs
    * @param PropertyValues Map of properties and their matching values.
    * @return Dataset of all matching annotations
    */
   List<Annotation> getAnnotationsByWADMPropertyValues(Map<String, String> PropertyValues);
}
