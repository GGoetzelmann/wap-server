package edu.kit.scc.dem.wapsrv.model.rdf;

import org.eclipse.rdf4j.model.Model;

/**
 * Base interface for RdfTransactions
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface RdfTransactionExecuter {
   /**
    * Execute template for the Lambda Function.
    *
    * @param ds
    *           the Dataset to be read or write
    */
   void execute(Model ds);
}
