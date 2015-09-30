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
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.Transformer;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import sun.misc.Regexp;
import uk.soton.service.mediation.Alignment;
import uk.soton.service.mediation.EntityTranslationService;
import uk.soton.service.mediation.EntityTranslationServiceImpl;
import uk.soton.service.mediation.JenaAlignment;
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

	private static final Logger log = Logger.getLogger( Mediator.class.getName() );

	/**
	 * The particular jenaAlignment that holds the actual translation rules to apply
	 * for this specific mediator.
	 */
	private JenaAlignment jenaAlignment = null;
	
	/**
	 * Generate a unique id for each mediator, based on the edoalIRI.
	 */
	static private int counter = 0;
	private String edoalIRI, id;
	
	/**
	 * At least for printing purposes it is handy to have available a jena RDF model   
	 */
	// private Model RDFmodel = null;
	// private org.semanticweb.owl.align.Alignment owlAlignment = null;
	
	/**
	 * The mediator is based on an EDOAL Alignment(s). This represent that very EDOAL alignment.
	 * TODO Facilitate more than one single EDOAL alginment.
	 * 
	 */
	//private EDOAL edoalAlignment = null;

	/**
	 * The alignment that is used as source for all operations has been
	 * implemented as just another semanticweb alignment type. However, it is
	 * constructed from an EDOAL alignment.
	 */
	private EDOALAlignment edoalAlignment = null;
	
	/**
	 * An alignment can provide translations for SQL-like commands as well: 
	 * CONSTRUCT, ASK, and DESCRIBE; the SELECT statement is part of the message that 
	 * is being translated 
	 */
	private List<Query> constructs;
	
	/**
	 * Constructor - creates the mediator object and initialises its processing
	 * queue
	 */
	public Mediator(EDOALAlignment edoal) {
		if (edoal == (EDOALAlignment)null) {
			throw new NullPointerException("Cannot create mediator without edoal alignment");
		} else {
			constructs = new ArrayList<Query>();
			edoalAlignment = edoal;
			edoalIRI = edoal.getExtension(Namespace.ALIGNMENT.uri, Annotations.ID);
			// TODO perform a regex to select only the 'onto1 dash onto2' part of the IRI 
			id = edoalIRI + "_mediator" + ++Mediator.counter;
		}
	}
	
	/**
	 * @param jenaAlignment the jenaAlignment to set
	 */
	public void setJenaAlignment(JenaAlignment ja) {
		this.jenaAlignment = ja;
	}

	public boolean addAllConstruct(List<Query> c) {
		return constructs.addAll(c);
	}

	/**
	 * Mediate between the two data elements, i.e., translate the left hand one
	 * into the the right hand one.
	 * 
	 * TODO extend the application of other languages than SPARQL, e.g. SPIN.
	 * Assume SPARQL for the moment
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
			EntityTranslationService ets = new EntityTranslationServiceImpl();
			Transform translation = new EntityTranslation(ets, this.jenaAlignment);
			Op translated = Transformer.transform(translation, op);
			rhe = ExtendedOpAsQuery.asQuery(translated);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Couldn't translate the query: "  + lhe + "\n", e);
			e.printStackTrace();
		}
		return rhe;
	}

	/**
	 * Mediate between the two data elements, i.e., translate the left hand one
	 * into the the right hand one.
	 * 
	 * Transformation by rewriteQuery was not yet implemented in the old
	 * align.java that this jenaAlignment package currently makes use of.
	 * TODO upgrade to most recent Alignment API
	 * 
	 * @param filename
	 * @return String translated query
	 * @throws FileNotFoundException, IOException 
	 */
	public Query translate(File file) throws FileNotFoundException, IOException {
		log.log(Level.INFO, "Need to Ugrade to Alignment API 4.7 in order to apply BasisAlignment.rewriteQuery()\nWill translate in functional identical old way.");
		// Transform query
		//
		
		//
		Query transformedQuery = null;
		//BasicAlignment al = (BasicAlignment) this.mediation;
		//Properties parameters = new Properties();
		//
		// Transformation by method rewriteQuery was not yet implemented in the old
		// align.java that this mediation package currently makes use of.
		// TODO upgrade to most recent Alignment API
		//
		// transformedQuery = al.rewriteQuery( queryString, parameters );

		// Translate by reading file and transforming string into query, then translate(query)
		try {
			transformedQuery = translate(readFile(file));
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "Query file does not exist", e);
			e.printStackTrace();
		}
		
		return transformedQuery;
	}

	/**
	 * 
	 */
	public String toString() {
		return getId();
	}
	
	/**
	 * Show the jenaAlignment as RDF graph model
	 * @param format, e.g., "Turtle"
	 * @return A formatted string representing the RDF graph model
	 */
	public String asModel(String format) {
		StringWriter w = new StringWriter();
		Model model = ((JenaAlignment)jenaAlignment).getModel();
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
	 * getEdoalId
	 * @return String the <id> field from the EDOAL alignment 
	 */
	public String getEdoalId() {
		return this.edoalIRI;
	}
	
	/**
	 * Getters
	 * @return EDOAL the edoal alignment that this mediator is working from
	 */
	public EDOALAlignment getEdoalAlignment() {
		return this.edoalAlignment;
	}
	
	/**
	 * Getters
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

}
