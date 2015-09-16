/**
 * 
 */
package nl.tue.siop.layer;




import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.Transformer;

import uk.soton.service.mediation.Alignment;
import uk.soton.service.mediation.EntityTranslationService;
import uk.soton.service.mediation.EntityTranslationServiceImpl;
import uk.soton.service.mediation.algebra.EntityTranslation;
import uk.soton.service.mediation.algebra.ExtendedOpAsQuery;

/**
 * The Mediator performs the translation of data instances of one ontology, into data instances of another ontology.
 * And vice versa. In that process it preserves the semantics as has been specified by 
 * the Alignment that it has been generated from (see the MeditiatorGenerator class).<br/>
 *
 * @author brandtp
 *
 */
public class Mediator {

	/**
	 * The particular mediation that holds the actual translation rules to apply for this specific mediator.
	 */
	private Alignment mediation = null;
	
	/**
	 * Constructor - creates the mediator object and initialises its processing queue
	 */
	public Mediator(Alignment m) {
		// TODO Auto-generated constructor stub
		this.mediation = m;
	}
	
	/**
	 * Mediate between the two data elements, i.e., translate the left hand one into the 
	 * the right hand one.
	 * 
	 * TODO extend the application of other languages than SPARQL, e.g. SPIN.
	 * Assume SPARQL for the moment
	 * 
	 * @param LHE - the left hand data element that is taken as input
	 * @return RHE - the right hand data element as result of the translation
	 */
	public Query translate(Query lhe) {
		Query rhe = null;
		// Assume SPARQL
		try {
			Query query = QueryFactory.create(lhe.toString(), Syntax.syntaxARQ);
			Op op = Algebra.compile(query);
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

	/**
	 * Mediate between the two data elements in the opposite direction, i.e., 
	 * translate the right hand one into the left hand one.
	 * @param RHE - the right hand data element that is taken as input
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

	
}
