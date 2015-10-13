/**
 * 
 */
package nl.tue.siop.layer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceF;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.sun.xml.internal.bind.v2.runtime.property.PropertyFactory;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import sun.misc.Regexp;
import uk.soton.service.mediation.Alignment;
import uk.soton.service.mediation.EntityTranslationService;
import uk.soton.service.mediation.EntityTranslationService.BGPTranslationResult;
import uk.soton.service.mediation.EntityTranslationServiceImpl;
import uk.soton.service.mediation.JenaAlignment;
import uk.soton.service.mediation.RDFVocabulary;
import uk.soton.service.mediation.STriple;
import uk.soton.service.mediation.STripleList;
import uk.soton.service.mediation.algebra.EntityTranslation;
import uk.soton.service.mediation.algebra.ExtendedOpAsQuery;
import uk.soton.service.mediation.edoal.EDOALQueryGenerator;

/**
 * The Mediator performs the translation of data instances of one ontology, into
 * data instances of another ontology. And vice versa. In that process it
 * preserves the semantics as has been specified by the Alignment that it has
 * been generated from (see the MeditiatorGenerator class).<br/>
 *
 * @author Paul Brandt <paul@brandt.name>
 *
 */
public class Mediator {

	private static final Logger log = Logger.getLogger(Mediator.class.getName());

	/**
	 * The particular jenaAlignment that holds the actual translation rules to
	 * apply for this specific mediator.
	 */
	private JenaAlignment jenaAlignment = null;

	/**
	 * The protocol handler that is being used to transfer the data to the peer
	 * communication entity
	 */
	private Protocol protocolHandler;

	/**
	 * Generate a unique id for each mediator, based on the edoalIRI.
	 */
	static private int counter = 0;
	private String edoalIRI, id;

	/**
	 * At least for printing purposes it is handy to have available a jena RDF
	 * model
	 */
	// private Model RDFmodel = null;
	// private org.semanticweb.owl.align.Alignment owlAlignment = null;

	/**
	 * The mediator is based on an EDOAL Alignment(s). This represent that very
	 * EDOAL alignment. TODO Facilitate more than one single EDOAL alginment.
	 * 
	 */
	// private EDOAL edoalAlignment = null;

	/**
	 * The alignment that is used as source for all operations has been
	 * implemented as just another semanticweb alignment type. However, it is
	 * constructed from an EDOAL alignment.
	 */
	private EDOALAlignment edoalAlignment = null;

	/**
	 * An alignment can provide translations for SQL-like commands as well:
	 * CONSTRUCT, ASK, and DESCRIBE; the SELECT statement is part of the message
	 * that is being translated
	 */
	private List<Query> constructs;

	/**
	 * Constructor - creates the mediator object and initialises its processing
	 * queue
	 */
	public Mediator(EDOALAlignment edoal) {
		if (edoal == (EDOALAlignment) null) {
			throw new NullPointerException("Cannot create mediator without edoal alignment");
		} else {
			constructs = new ArrayList<Query>();
			edoalAlignment = edoal;
			protocolHandler = new Protocol();
			edoalIRI = edoal.getExtension(Namespace.ALIGNMENT.uri, Annotations.ID);
			// TODO perform a regex to select only the 'onto1 dash onto2' part
			// of the IRI
			id = edoalIRI + "_mediator" + ++Mediator.counter;
		}
	}

	/**
	 * @param jenaAlignment
	 *            the jenaAlignment to set
	 */
	public void setJenaAlignment(JenaAlignment ja) {
		this.jenaAlignment = ja;
	}

	/**
	 * @return the protocol handler TODO this should not be necessary (and hence
	 *         should be removed) when the protocol handler is duplicated into
	 *         communicating peers
	 */
	public Protocol getPH() {
		return protocolHandler;
	}

	/**
	 * Setter for Construct queries
	 * 
	 * @return Boolean - success or failure of setter operation
	 */
	public Boolean setConstructs() {
		if (jenaAlignment == null) {
			return false;
		} else
			return createConstructQueries(jenaAlignment);
	}

	/**
	 * Mediate a query, i.e., translate the elements of the query statement by finding each pattern
	 * that is translatable, e.g., the left hand side, into its specification from the alignment, i.e.,
	 * the right hand side.
	 * 
	 * TODO Currently the SPARQL algebra assumes EQ during translation. This has
	 * to change to actively use the entity relation to create other
	 * translations than EQ only.
	 * 
	 * @param LHE
	 *            - the left hand data element that is taken as input
	 * @return RHE - the right hand data element as result of the translation
	 */
	public Query translate(Query lhe) {
		Query rhe = null;
		// Assume SPARQL
		try {
			Op op = Algebra.compile(lhe);
//			System.out.println("LHS as ARQ Algebra:");
//			System.out.println(op.toString());

			EntityTranslationService ets = new EntityTranslationServiceImpl();
			Transform translation = new EntityTranslation(ets, this.jenaAlignment);

			Op translated = Transformer.transform(translation, op);
//			System.out.println("Translated LHS as ARQ Algebra:");
//			System.out.println(translated.toString());

			rhe = ExtendedOpAsQuery.asQuery(translated);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Couldn't translate the query: " + lhe + "\n", e);
			e.printStackTrace();
		}
		return rhe;
	}
	
	/**
	 * Mediate between the two data elements, i.e., translate the left hand one
	 * into the the right hand one.
	 * 
	 * Transformation by rewriteQuery was not yet implemented in the old
	 * align.java that this jenaAlignment package currently makes use of. TODO
	 * upgrade to most recent Alignment API
	 * 
	 * @param filename
	 * @return String translated query
	 * @throws FileNotFoundException,
	 *             IOException
	 */
	public Query translate(File file) throws FileNotFoundException, IOException {
		log.log(Level.INFO,
				"Need to Ugrade to Alignment API 4.7 in order to apply BasisAlignment.rewriteQuery()\nWill translate in functional identical old way.");
				// Transform query
				//

		//
		Query transformedQuery = null;
		// BasicAlignment al = (BasicAlignment) this.mediation;
		// Properties parameters = new Properties();
		//
		// Transformation by method rewriteQuery was not yet implemented in the
		// old
		// align.java that this mediation package currently makes use of.
		// TODO upgrade to most recent Alignment API
		//
		// transformedQuery = al.rewriteQuery( queryString, parameters );

		// Translate by reading file and transforming string into query, then
		// translate(query)
		try {
			transformedQuery = translate(readFile(file));
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "Query file does not exist", e);
			e.printStackTrace();
		}

		return transformedQuery;
	}

	/**
	 * Mediate a list of triples, i.e., translate each element of the <s,p,o> triplet when it matches 
	 * a translation pattern in the alignment.
	 * 
	 * @param LHE
	 *            - the list of triples that is taken as input
	 * @return RHE - the mediated list of triples as result of the translation
	 */
	public STripleList translate(STripleList triples) {
		log.log(Level.INFO, "Answer triples:" + triples.toString());

		BGPTranslationResult translatedTriples = null;
		STripleList results = new STripleList();
		try {
			EntityTranslationService ets = new EntityTranslationServiceImpl();
			translatedTriples = ets.getTranslatedTriples(jenaAlignment, triples.asTripleList());

			for (Triple triple : translatedTriples.getTranslatedBGP()) {
				results.add(new STriple(triple.getSubject(), triple.getMatchPredicate(), triple.getMatchObject()));
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Couldn't translate the triples: \n" + triples + "\n", e);
			e.printStackTrace();
		}
		
		log.log(Level.INFO, "Translated triples:" + results.toString());

		return results;
	}
	
	/**
	 * Create SPARQL CONSTRUCT queries based on an EDOAL alignment, in order to
	 * import external data and add them to the mediator.
	 */
	private boolean createConstructQueries(Alignment patterns) {
		List<Query> c = EDOALQueryGenerator.generateQueriesFromAlignment(patterns);
		return constructs.addAll(c);
	}

	/**
	 * Return the name of this mediator
	 * 
	 * @return String - the name of this mediator
	 */
	public String toString() {
		return getId();
	}

	/**
	 * Show the jenaAlignment as RDF graph model
	 * 
	 * @param format,
	 *            e.g., "Turtle"
	 * @return A formatted string representing the RDF graph model
	 */
	public String asModel(String format) {
		StringWriter w = new StringWriter();
		Model model = ((JenaAlignment) jenaAlignment).getModel();
		model.write(w, format);
		return w.toString();
	}

	/**
	 * Get the EDOAL alignment as a set of axioms
	 */
	public String asAxioms() {
		PrintWriter writer;
		String s = null;
		StringWriter stringWriter = new StringWriter();
		try {
			writer = new PrintWriter(stringWriter, true);
			AlignmentVisitor renderer = new RDFRendererVisitor(writer);
			((org.semanticweb.owl.align.Alignment) this.edoalAlignment).render(renderer);
			s += stringWriter.toString();
			writer.flush();
			writer.close();
		} catch (AlignmentException e) {
			log.log(Level.WARNING, "Couldn't render EDOAL alignment into axioms.", e);
		}
		return s;
	}

	/**
	 * getFromEdoal(String l, String s)
	 * 
	 * @param String
	 *            l: the label in the Edoal.xml String s: the sublabel in the
	 *            edoal.xml
	 * @return
	 * @return String: the contents of the label, or null if nothing can be
	 *         found
	 */
	public String getFromEdoal(String uri, String lbl) {
		return this.edoalAlignment.getExtension(uri, lbl);
	}

	/**
	 * getEdoalId
	 * 
	 * @return String the <id> field from the EDOAL alignment
	 */
	public String getEdoalId() {
		return this.edoalIRI;
	}

	/**
	 * Getters
	 * 
	 * @return EDOAL the edoal alignment that this mediator is working from
	 */
	public EDOALAlignment getEdoalAlignment() {
		return this.edoalAlignment;
	}

	/**
	 * Getters
	 * 
	 * @return Mediation the jenaAlignment that this mediator is working with
	 */
	public Alignment getJenaAlignment() {
		return this.jenaAlignment;
	}

	public String getId() {
		return this.id;
	}

	public Query readFile(File f) throws IOException, FileNotFoundException {
		if (f.exists() && !f.isDirectory()) {
			InputStream in = new FileInputStream(f);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			String queryString = "";
			while ((line = reader.readLine()) != null) {
				queryString += line + "\n";
			}
			reader.close();
			return QueryFactory.create(queryString);
		} else {
			throw new FileNotFoundException("Query file does not exist: " + f.toString());
		}
	}

	/**
	 * @return
	 */
	public String getOnto2() {
		return this.edoalAlignment.getFile2().toString();
	}

	/**
	 * Send a statement, expressed in one's own native language, to the other
	 * application. This implies the application of a mediation, e.g.,
	 * transformation, into its equivalent statement however now expressed in
	 * the native language of the other application.
	 * 
	 * @param String
	 *            s The statement
	 * @return True if delivered, False otherwise.
	 * 
	 *         TODO Abstract the string s into Statement <type> or something
	 */
	public Boolean sendQ(String s) {
		if (protocolHandler == null)
			return false;
		Query tq = null;
		Query sq = QueryFactory.create(s, Syntax.syntaxARQ);
		tq = translate(sq);
		if (tq == null)
			return false;
		return this.protocolHandler.send(tq);
	}

	/**
	 * Check whether a statement was received from the other application. Since
	 * the other application acts a slave, it sends statement expressed in its
	 * own language. Receiving data therefore implies an inverse mediation,
	 * e.g., transformation, into its equivalent statement expressed in our
	 * language.
	 * 
	 * @return Query the query statement in our native ontoA language.
	 */
	public Query receiveQ() {
		Query sq = QueryFactory.create(protocolHandler.receiveQ(), Syntax.syntaxARQ);
		Query tq = translate(sq);
		return tq;
	}

	/**
	 * Receive the answer to the previous query from the protocol handler and
	 * translate the answer into terms of ontoA
	 * 
	 * @return the translated answer in native terms (ontoA)
	 */
	public String receiveA() {
		// Receive the answer
		ResultSet results = protocolHandler.receiveA();
		// Represent the answer as a list of triples, by building a new triple list from the answer
		List<String> vars = results.getResultVars();	// The variables that contain the answers
		Model rm = results.getResourceModel();			// The model that contains the details of the original query
		STripleList answerAsTriples = new STripleList();// The answers as triples

		// Build the new triples
		while (results.hasNext()) {				// For each single answer ...
			QuerySolution qs = results.next();	// ... get the answer
			for ( String var : vars) {			// ... for each variable in that answer
												// ... turn actual answer literal into a triple <instance, RDFtype, ontoB:class>
				RDFNode s = qs.get(var);
				Property p = ResourceFactory.createProperty(RDFVocabulary.RDF_TYPE);
				Resource o = (Resource) rm.getProperty((Resource) s, p).getObject();	// Get the ontoB:class
				answerAsTriples.add(new STriple(s.asNode(), p.asNode(), o.asNode()));
			}
		}
		
		// Translate the answer 
		STripleList translatedAnswer = translate(answerAsTriples);
		return translatedAnswer.toString();
	}
}
