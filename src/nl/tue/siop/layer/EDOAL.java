/**
 * 
 */
package nl.tue.siop.layer;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;

import com.hp.hpl.jena.rdf.model.Model;

import fr.inrialpes.exmo.align.impl.edoal.Transformation;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import uk.soton.service.mediation.JenaAlignment;

/**
 * The EDOAL Class provides the following features:<br/>
 * 1. <b>Construction</b> of entities from other entities can be expressed through
 * 		algebraic operations. In this way, shallowness of ontologies can be overcome.
 * 		// TODO Example: <br/>
 * 2. <b>Restrictions</b> can be expressed on entities in order to narrow their scope. Narrowing the 
 * 		scope of an entity makes it possible to more precisely align this entity with the corresponding 
 * 		one in the other ontology. <br/>
 * 3. <b>Transformations</b> of property values can be specified. Property values using different encodings
 * 		or units can be aligned using transformations.    
 * 
 * The EDOAL alignment builds on the Alignment format [1]. 
 * 
 * [1] J. Euzenat, “Alignment infrastructure for ontology mediation and other applications”, in 
 *     1st ICSOC international workshop on Mediation in semantic web services, 2005, pp. 81-95.
 *     
 * @author Paul Brandt <paul@brandt.name>
 *
 */
public class EDOAL {

	private static final Logger log = Logger.getLogger( EDOAL.class.getName() );
	
	/**
	 * The alignment that is used as source for all operations has been implemented as 
	 * just another semanticweb alignment type. However, it is constructed from an EDOAL alignment.
	 */
	public Alignment align;
	
	/**
	 * In order to load an EDOAL alignment, it needs to be read and parsed. This is that parser.
	 */
	public AlignmentParser parser;
	

	/**
	 * 
	 */
	public EDOAL() {
		this.align = null;
		this.parser = new AlignmentParser(0);
		this.parser.initAlignment(null);
	}	

	
	/**
	 * Constructor that immediately parses an EDOAL alignment.
	 * @param s the relative path/to/EDOAL/alignment.xml.
	 * @throws AlignmentException on problems with the parsing
	 */
	public EDOAL(String s) throws AlignmentException {
		this();  // Call the base constructor to set the generic stuff
		this.align = this.parser.parse(s);
	}

	/**
	 * @return the printable format of an EDOAL object
	 */
	public String toString() {
		StringBuilder s = new StringBuilder(256);
		String o1 = null, o2 = null, l = null, t = null;
		try {
			o1 = this.align.getOntology1URI().toString();
			o2 = this.align.getOntology2URI().toString();
			l  = this.align.getLevel();
			t  = this.align.getType();
		} catch (AlignmentException e) {
			log.log(Level.WARNING, "Couldn't generate the proper string for org.semanticweb.owl.align.Alignment ", e);
			e.printStackTrace();
		}
		s.append("Ont1 : ").append(o1).append("\n");
		s.append("Ont2 : ").append(o2).append("\n");
		s.append("Level: ").append(l).append("\n");
		s.append("Type : ").append(t).append("\n");
		s.append("\n=====\n").append(this.parser.toString());

		return s.toString();
	}	
}
