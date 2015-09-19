/**
 * 
 */
package nl.tue.siop.layer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;

import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.impl.edoal.Transformation;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import uk.soton.service.mediation.JenaAlignment;
import uk.soton.service.mediation.edoal.EDOALQueryGenerator;

/**
 * The EDOAL Class provides the following features:<br/>
 * 1. <b>Construction</b> of entities from other entities can be expressed
 * through algebraic operations. In this way, shallowness of ontologies can be
 * overcome. // TODO Example: <br/>
 * 2. <b>Restrictions</b> can be expressed on entities in order to narrow their
 * scope. Narrowing the scope of an entity makes it possible to more precisely
 * owlAlgnmt this entity with the corresponding one in the other ontology. <br/>
 * 3. <b>Transformations</b> of property values can be specified. Property
 * values using different encodings or units can be aligned using
 * transformations.
 * 
 * The EDOAL alignment builds on the Alignment format [1].
 * 
 * [1] J. Euzenat, “Alignment infrastructure for ontology mediation and other
 * applications”, in 1st ICSOC international workshop on Mediation in semantic
 * web services, 2005, pp. 81-95.
 * 
 * @author Paul Brandt <paul@brandt.name>
 *
 */
public class EDOAL {

	private static final Logger log = Logger.getLogger(EDOAL.class.getName());

	/**
	 * The alignment that is used as source for all operations has been
	 * implemented as just another semanticweb alignment type. However, it is
	 * constructed from an EDOAL alignment.
	 */
	private EDOALAlignment edoalAlignment;

	/**
	 * In order to load an EDOAL alignment, it needs to be read and parsed. This
	 * is that parser.
	 */
	private AlignmentParser parser;

	/**
	 * 
	 */
	public EDOAL() {
		this.edoalAlignment = null;
		this.parser = new AlignmentParser(0);
		this.parser.initAlignment(null);
	}

	/**
	 * Constructor that immediately parses an EDOAL alignment.
	 * 
	 * @param s
	 *            the relative path/to/EDOAL/alignment.xml.
	 * @throws AlignmentException
	 *             on problems with the parsing
	 */
	public EDOAL(String s) throws AlignmentException {
		this(); // Call the base constructor to set the generic stuff
		this.edoalAlignment = (EDOALAlignment) this.parser.parseString(s);
	}

	/**
	 * @return the printable format of an EDOAL object
	 */
	public String toString() {
		StringBuilder s = new StringBuilder(256);
		String o1 = null, o2 = null, l = null, t = null;
		Integer n = 0;
		try {
			o1 = this.edoalAlignment.getOntology1URI().toString();
			o2 = this.edoalAlignment.getOntology2URI().toString();
			l = this.edoalAlignment.getLevel();
			t = this.edoalAlignment.getType();
			n = this.edoalAlignment.nbCells();
		} catch (Exception e) {
			log.log(Level.WARNING, "Couldn't generate the proper string for org.semanticweb.owl.align.Alignment ", e);
			e.printStackTrace();
		}
		s.append("Ont1 : ").append(o1).append("\n");
		s.append("Ont2 : ").append(o2).append("\n");
		s.append("Level: ").append(l).append("\n");
		s.append("Type : ").append(t).append("\n");
		s.append("Total: ").append(t).append(" correspondences\n");
		s.append("\n=====\n").append(this.parser.toString());

		return s.toString();
	}

	/**
	 * Display the EDOAL alignment as a set of axioms
	 */
	public String Display() {
		PrintWriter writer;
		StringWriter stringWriter = new StringWriter();
		String s = "\n++via writer++++\n";
		try {
			writer = new PrintWriter(stringWriter, true);
			AlignmentVisitor renderer = new RDFRendererVisitor(writer);
			if (this.edoalAlignment != null) {
				this.edoalAlignment.render(renderer);
			} else {
				log.log(Level.WARNING, "Couldn't render because of NULL edoalAlignment");
			}
			// TODO Apparently, this is not the way to operate a writer in order to produce strings
			s += stringWriter.toString() + "\n++++++\n";
			
			writer.flush();
			writer.close();
		} catch (AlignmentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

	/**
	 * @return the edoalAlignment
	 */
	public EDOALAlignment getAlignment() {
		return edoalAlignment;
	}

	/**
	 * @return the parser
	 */
	public AlignmentParser getParser() {
		return parser;
	}
	
	public String getName() {
		return this.edoalAlignment.getXNamespaces().getProperty("xml:base", "Fucked!");
	}

}
