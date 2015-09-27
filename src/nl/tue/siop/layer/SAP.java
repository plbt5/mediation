/**
 * 
 */
package nl.tue.siop.layer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owl.align.AlignmentException;

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
import uk.soton.service.mediation.edoal.EDOALQueryGenerator;

/**
 * @author brandtp
 *
 */
public class SAP {

	private static final Logger log = Logger.getLogger(SAP.class.getName());

	/**
	 * Every SAP creates its own mediator and protocol components to work with
	 */
	private Mediator m;
	private Protocol p;

	/**
	 * @return the protocol handler 
	 * TODO this should not be necessary (and hence should be removed) when the protocol handler is duplicated into communicating peers
	 */
	public Protocol getP() {
		return p;
	}

	public SAP() {
		try {
			this.p = new Protocol();
			this.m = null;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Couldn't create a new protocol instance", e);
		}
	}

	/**
	 * Create a new mediator that will translate according to the EDOAL
	 * alignment
	 * 
	 * @param file
	 *            the file that carries the EDOAL alignment TODO pass an EDOAL
	 *            object as opposed to a file that needs to be read first TODO
	 *            the mediator should be able to handle more than one EDOAL
	 *            alignment
	 * @throws IOException 
	 */
	public void addEDOALALignment(File edoalFile) throws UnsupportedOperationException, AlignmentException, IOException {
		if (this.m != null) {
			throw new UnsupportedOperationException(
					"Not Supported Yet: Attempt to add another EDOAL alignment to existing mediator");
		} else {
			String edoalString = Utilities.readFile(edoalFile, StandardCharsets.UTF_8);
			MediatorFactory mf = new MediatorFactory(edoalString);
			//MediatorFactory mf = new MediatorFactory(edoalFile);
			this.m = mf.createMediator();
			
			Alignment mediation = m.getMediation();

			// Add the CONSTRUCT queries
			this.m.addAllConstruct(constructQuery(mediation));
		}
	}

	/**
	 * Create CONSTRUCT queries from alignment and add them to the mediator
	 */

	private List<Query> constructQuery(Alignment patterns) {
		List<Query> constructs = EDOALQueryGenerator.generateQueriesFromAlignment(patterns);
		System.out.println("CONSTRUCT queries from alignment\n");
		for (Query q : constructs) {
			System.out.println("<----------------------->");
			System.out.println(q.toString());
		}
		return constructs;
	}

	/**
	 * Send a statement, expressed in one's own native language, to the other
	 * application. This implies the application of a mediation, e.g.,
	 * transformation, into its equivalent statement however now expressed in
	 * the native language of the other application.
	 * 
	 * @param String
	 *            s The statement
	 * @return True if delivered, False otherwise. TODO Abstract into Statement
	 *         <type> or something
	 */
	public boolean send(String s) {
		Query tq = null;
		Query sq = QueryFactory.create(s, Syntax.syntaxARQ);
		tq = this.m.translate(sq);

		return this.p.send(tq.toString());
	}

	/**
	 * Check whether a statement was received from the other application. Since
	 * the other application acts a slave, it sends statement expressed in its
	 * own language. Receiving data therefore implies an inverse mediation,
	 * e.g., transformation, into its equivalent statement expressed in our
	 * language.
	 * 
	 * @return Query A statement in our language.
	 */
	public Query receive() {
		Query sq = QueryFactory.create(p.receive(), Syntax.syntaxARQ);
		Query tq = this.m.translate(sq);
		return tq;
	}

	/**
	 * Show the mediation rules that are currently applied. Use both the console
	 * and the logger as output target.
	 */

	public void showMediation() {
		String medString = this.m.toString();
		log.log(Level.INFO, this.m.toString() + " mediates according to alignment: \n" + m.getEdoalAlignment().asAxioms());
		return;
	}
}
