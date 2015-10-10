/**
 * 
 */
package nl.tue.siop.layer;

import java.io.InputStream;
import java.util.logging.Logger;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

/**
 * @author brandtp
 *
 */
public class Protocol {

	private static final Logger log = Logger.getLogger(Protocol.class.getName());

	/**
	 * A temporary solution to have a peer communicator available to send foreign stuff to and
	 * have it query its foreign ontology
	 */
	static private Peer peer = null;

	/**
	 * 
	 */
	public Protocol() {
		// Nothing to do, yet
		Protocol.peer = new Peer();
	}

	public Boolean send(Query msg) {
		System.out.println("Sending foreign message:\n" + msg.toString() + "\n");
		return peer.receiveQ(msg.toString());
	}

	public ResultSet receiveA() {
		ResultSet results = Protocol.peer.receiveA();
		
		System.out.println("Receiving foreign results to query: ");
//		ResultSetFormatter.out(System.out, results);
//		System.out.println();
		// List<String> vars = results.getResultVars();
		// System.out.println("vars: " + vars);
		return results;
	}
	
	public String receiveQ() {
		String m = Protocol.peer.getQuery();
		System.out.println("Receiving foreign message:\n" + m + "\n");
		return m;
	}

	/**
	 * Initialise the communicating peer with the stuff it needs to respond with valid 
	 * messages and data.
	 * @param qs the query as string, in terms of ontoB, that it will send to application B
	 * @param fn the name of the file containing the ontology, including instances, that application B will use
	 * @return the success of reading and parsing the ontology file
	 */
	public Boolean initPeer(String qs, String fn) {
		Protocol.peer.setQuery(qs);
		return (!Protocol.peer.setModel(fn).isEmpty());
	}

	/**
	 * This class represents the peer entity with which the protocol is
	 * communicating. It contains the minimal parts that are required to
	 * implement an ontology and query it.
	 */
	static private class Peer {

		/**
		 * This represents a QueryB message that the peer is to send to use, and hence
		 * is to be 'received' by the Protocol. It is therefore represented in ontoB.
		 * Temporarily until the protocol gets communication counterpart instances. 
		 * 
		 * TODO Duplicate Protocol Handlers to represent being each others peer
		 */
		private String ontoBQueryString = null;
		
		/**
		 * The message that has been received last by the peer 
		 */
		private String msgReceived = null;

		/**
		 * Until a real peer entity exists, this class also implements the ontoB
		 * model that can be queried with a translated query from ontoA. The
		 * file that holds ontoB is specified in the demo script text#.json
		 */
		static private OntModel ontoB = null;

		/**
		 * The query that application B will send to application A needs to be
		 * set first.  
		 * @param String query 
		 *            the messageR to set
		 */
		public void setQuery(String qs) {
			this.ontoBQueryString = qs;
		}

		/**
		 * @return ResultSet the results of querying the ontology with the received query, or null
		 * if no query has been received yet.
		 */
		public ResultSet receiveA() {
			if (msgReceived == null) return (ResultSet)null;
			// query the ontology, and return the query results
			return queryModel(msgReceived);
		}

		/**
		 * Receive the query (in terms of ontoB) that application A wants to ask to application B
		 * @param msg
		 * @return
		 */
		public Boolean receiveQ(String msg) {
			msgReceived = msg;
			return true;
		}

		/**
		 * Simulate application B sending a request to query ontoA.
		 * @return String query represented in ontoB
		 */
		public String getQuery() {
			return this.ontoBQueryString;
		}

		/**
		 * @param String
		 *            s - the name of the file containting ontoB
		 */
		public OntModel setModel(String s) {
			if (s == null) {
				return Peer.ontoB;
			}
			Peer.ontoB = createOnto(s);
			return Peer.ontoB;
		}

		/**
		 * For as long as there is no real application with a reasoning engine,
		 * this is the furthest location that such engine can be instantiated.
		 */
		private OntModel createOnto(String ontoBName) {

			// create an empty model
			OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF);

			// use the FileManager to find the input file

			InputStream in = FileManager.get().open(ontoBName);
			if (in == null) {
				throw new IllegalArgumentException("File: " + ontoBName + " not found");
			}

			// read the RDF/XML file
			model.read(in, "RDF/XML");

			// write it to standard out
			model.write(System.out);

			return model;
		}
	
		
		private ResultSet queryModel(String qs) {
			Query query = QueryFactory.create(qs);
			ResultSet results = null;
			QueryExecution qexec = QueryExecutionFactory.create(query, Peer.ontoB);
			results = qexec.execSelect();
			
			return results;
		}
	}
}
