/**
 * 
 */
package nl.tue.siop.layer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owl.align.AlignmentException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import uk.soton.service.mediation.Alignment;
import uk.soton.service.mediation.edoal.EDOALQueryGenerator;

/**
 * The Service Access Point (SAP) represents the main point of access to the applications for access to 
 * the semantic services that the SIOp layer provides. The SIOp layer makes use of an underlying communication stack 
 * to exchange data between application peers. Based on an EDOAL alignment, the main service of the SIOp layer is
 * to enable loose coupled semantic between the communicating peers. This means that each peer can exchange data in its
 * native semantics; the SIOp layer provides for the required translation between semantic representations of each
 * application. The translation of the data itself is being performed by the mediator, while the exchange of the data is performed 
 * by the protocol handlers. The SAP provides for the generation and instantiation of the mediator.  
 * 
 * @author Paul Brandt <paul@brandt.name>
 *
 *
 */
public class SAP {

	private static final Logger log = Logger.getLogger(SAP.class.getName());

	/**
	 * Every SAP creates its own mediator and protocol components to work with.
	 * TODO SAP shouldn't contain a protocol, that should be left to the mediator
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
	
	/** 
	 * @return the mediator this SAP is working with
	 * 
	 */
	public Mediator getMediator() {
		return m;
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
	 * @param File
	 *            the file that carries the EDOAL alignment TODO pass an EDOAL
	 *            object as opposed to a file that needs to be read first TODO
	 *            the mediator should be able to handle more than one EDOAL
	 *            alignment
	 * @throws IOException when files do not exist or are inaccessible
	 * @throws UnsupportedOperationException when attempt is made to add more than one EDOAL alignment
	 * @throws AlignmentException 
	 */
	public void addEDOALALignment(File edoalFile) throws UnsupportedOperationException, IOException {
		if (this.m != null) {
			throw new UnsupportedOperationException(
					"Not Supported Yet: Cannot add another EDOAL alignment to existing mediator");
		} else if (edoalFile == (File)null) {
			throw new IOException("Alignment file does not exist");
		} else {
			AlignmentParser ap = new AlignmentParser(0);
			ap.initAlignment(null);
			EDOALAlignment ea = new EDOALAlignment();
			try{
				String s = new String(Files.readAllBytes(Paths.get(edoalFile.getAbsolutePath())));
				ea = (EDOALAlignment) ap.parseString(s);
			}catch(IOException e){
				log.log(Level.SEVERE, "Cannot find alignment file: " + edoalFile.getCanonicalPath(), e);
			}catch (AlignmentException e1){
				log.log(Level.SEVERE, "Cannot parse alignment file: " + edoalFile.getCanonicalPath(), e1);
			} 
						
			try {
				this.m = new MediatorFactory().createMediator(ea);
			} catch (NullPointerException e) {
				log.log(Level.SEVERE, "Cannot create mediator from: " + ea.getExtension(Namespace.ALIGNMENT.uri, Annotations.ID), e);
				e.printStackTrace();
			}

			// Add the CONSTRUCT queries
			Alignment ja = m.getJenaAlignment();
			this.m.addAllConstruct(constructQuery(ja));
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
		log.log(Level.INFO, this.m.toString() + " mediates according to alignment: \n" + m.asAxioms());
		log.log(Level.INFO, this.m.asModel("Turtle"));
		return;
	}
}
