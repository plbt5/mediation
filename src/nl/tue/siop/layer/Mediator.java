/**
 * 
 */
package nl.tue.siop.layer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import uk.soton.service.mediation.Alignment;
import uk.soton.service.mediation.EntityTranslationService;
import uk.soton.service.mediation.EntityTranslationServiceImpl;
import uk.soton.service.mediation.algebra.EntityTranslation;
import uk.soton.service.mediation.algebra.ExtendedOpAsQuery;
import uk.soton.service.mediation.edoal.EDOALQueryGenerator;

/**
 * The Mediator performs the translation of data instances of one ontology, into
 * data instances of another ontology. And vice versa. In that process it
 * preserves the semantics as has been specified by the Alignment that it has
 * been generated from (see the MeditiatorGenerator class).<br/>
 *
 * @author brandtp
 *
 */
public class Mediator {

	/**
	 * The particular mediation that holds the actual translation rules to apply
	 * for this specific mediator.
	 */
	private Alignment mediation = null;

	/**
	 * At least for printing purposes it is handy to have available a jena RDF model   
	 */
	private Model RDFmodel = null;
	private org.semanticweb.owl.align.Alignment owlAlignment = null;
	
	/**
	 * The mediator is based on an EDOAL Alignment(s). This represent that very EDOAL alignment.
	 * TODO Facilitate more than one single EDOAL alginment.
	 * 
	 */
	private EDOAL edoalAlignment = null;

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
	public Mediator(EDOAL edoal) {
		constructs = new ArrayList<Query>();
		edoalAlignment = edoal;
	}
	
	/**
	 * @param mediation the mediation to set
	 */
	public void setMediation(Alignment mediation) {
		this.mediation = mediation;
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
			Transform translation = new EntityTranslation(ets, this.mediation);
			Op translated = Transformer.transform(translation, op);
			rhe = ExtendedOpAsQuery.asQuery(translated);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return rhe;
	}

	public String transform(String filename) {
		// Transform query
		String transformedQuery = "Need to Ugrade to Alignment API 4.7 in order to apply BasisAlignment.rewriteQuery() ";
		//BasicAlignment al = (BasicAlignment) this.mediation;

		try {
			InputStream in = new FileInputStream(filename);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			String queryString = "";
			while ((line = reader.readLine()) != null) {
				queryString += line + "\n";
			}
			reader.close();
			
			//Properties parameters = new Properties();
			//
			// Transformation by rewriteQuery was not yet implemented in the old
			// align.java that this
			// mediati0on package currently makes use of.
			// TODO upgrade to most recent Alignment API
			//
			// transformedQuery = al.rewriteQuery( queryString, parameters );
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return transformedQuery;
	}

	/**
	 * Mediate between the two data elements in the opposite direction, i.e.,
	 * translate the right hand one into the left hand one.
	 * 
	 * @param RHE
	 *            - the right hand data element that is taken as input
	 * @return LHE - the left hand data element as result of the translation
	 */
	public Query translateInverse(Query rhe) {
		Query lhe = null;
		try {
			Query query = QueryFactory.create(rhe.toString(), Syntax.syntaxARQ);
			Op op = Algebra.compile(query);
			EntityTranslationService ets = new EntityTranslationServiceImpl();
			Transform translation = new EntityTranslation(ets, this.mediation);
			Op translated = Transformer.transform(translation, op);
			lhe = ExtendedOpAsQuery.asQuery(translated);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lhe;
	}

	/**
	 * Display the EDOAL alignment as a set of axioms
	 */
	public String toString() {
		PrintWriter writer;
		StringWriter stringWriter = new StringWriter();
		String s = this.edoalAlignment.toString();
/*		try {
			writer = new PrintWriter(stringWriter, true);
			AlignmentVisitor renderer = new RDFRendererVisitor(writer);
			
					
			((org.semanticweb.owl.align.Alignment) this.edoalAlignment).render(renderer);
			//this.owlAlignment.render(renderer);
			//this.mediation.render(renderer);
			// TODO Apparently, this is not the way to operate a writer in order to produce strings
			s += stringWriter.toString() + "\n++++++\n";
			
			
			writer.flush();
			writer.close();
		} catch (AlignmentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return s;
	}

	/**
	 * Getters
	 * @return EDOAL the edoal alignment that this mediator is working from
	 */
	EDOAL getEdoalAlignments() {
		return this.edoalAlignment;
	}
	
	/**
	 * Getters
	 * @return Mediation the mediation that this mediator is working with
	 */
	Alignment getMediation() {
		return this.mediation;
	}
}
