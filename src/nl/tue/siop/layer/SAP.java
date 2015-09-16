/**
 * 
 */
package nl.tue.siop.layer;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owl.align.AlignmentException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;

import uk.soton.service.mediation.Alignment;

/**
 * @author brandtp
 *
 */
public class SAP {

	private static final Logger log = Logger.getLogger( SAP.class.getName() );
	
	/**
	 * Every SAP creates its own mediator and protocol components to work with
	 */
	private Mediator m;
	private Protocol p;

	public SAP() {
		try {
			this.p = new Protocol();
			this.m = null;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Couldn't create a new protocol instance", e);
		}
	}
	
	/**
	 * Create a new mediator that will translate according to the EDOAL alignment
	 * @param file the file that carries the EDOAL alignment
	 * TODO pass an EDOAL object as opposed to a file that needs to be read first
	 * TODO the mediator should be able to handle more than one EDOAL alignment
	 */
	public void addEDOALALignment (String file) throws UnsupportedOperationException {
		if (this.m != null) {
			throw new UnsupportedOperationException("Not Supported Yet: Attempt to add another EDOAL alignment to existing mediator");
		} else {
			Alignment a = null;
			try {
				MediatorGenerator mg = new MediatorGenerator(file);
				a = mg.generateMediation();
				this.m = new Mediator(a);
			} catch (AlignmentException e) {
				log.log(Level.SEVERE, "Couldn't load the alignment from: " + file, e);
			}
		}
	}
	
	
	public boolean send(String s) {
		Query tq = null;
		Query sq = QueryFactory.create(s, Syntax.syntaxARQ);
		tq = this.m.translate(sq);
		return this.p.send(tq.toString());
	}
	
	public Query receive() {
		Query sq = QueryFactory.create(p.receive(), Syntax.syntaxARQ);
		return sq;
	}
	
}
