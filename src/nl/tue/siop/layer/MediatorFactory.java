/**
 * MediatorFactory.java 
 *
 * Copyright (C) University of Eindhoven, Eindhoven, The Entherlands, 2015
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package nl.tue.siop.layer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owl.align.AlignmentException;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.Transformer;

import fr.inrialpes.exmo.align.impl.edoal.Transformation;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import uk.soton.service.mediation.Alignment;
import uk.soton.service.mediation.EntityTranslationService;
import uk.soton.service.mediation.EntityTranslationServiceImpl;
import uk.soton.service.mediation.JenaAlignment;
import uk.soton.service.mediation.RewritingRule;
import uk.soton.service.mediation.algebra.EntityTranslation;
import uk.soton.service.mediation.algebra.ExtendedOpAsQuery;
import uk.soton.service.mediation.edoal.EDOALMediator;

/**
 * The Class MediatorFactory generates the alignment-specific details that conform to the selected 
 * EDOAL alignment. The generated details are applied by a new instance of the Mediator. 
 * data that belongs to, and can be retrieved from one application ontology, into data that belongs to 
 * another application ontology. In that process it preserves the semantics as has been specified by 
 * the Alignment that it has been generated from (see the MeditiationGenerator class).<br/>
 *
 * The MediatorFactory Class provides the following features:<br/>
 * // TODO wat zijn die features dan?
 * 
 * @author Paul Brandt <paul@brandt.name>
 *
 */
public class MediatorFactory {

	private static final Logger log = Logger.getLogger( MediatorFactory.class.getName() );
	/**
	 * A MediatorFactory is the result of an alignment. This is the EDOAL alignment that it originates from.
	 */	
	private EDOAL edoal = null;

	/**
	 * An mediator transforms data between two ontologies. Those tranformations are specified by this mediation
	 * that is generated from the EDOAL alignment between the ontologies. 
	 * TODO: Support multiple data languages for a single alignment
	 * 		Mediations are also dependent on the language that represents the ontology data. We assume 
	 * 		RDF pattern rewriting rules. On extension to other languages, multiple mediations will emerge, hence 
	 * 		Class uk.soton.service.mediation.Alignment need to become overloaded with other constructions to host
	 * 		other type of rewriting rules. An even better solution would be to abstract the different types of 
	 * 		Alignments away in a Mediation Class.
	 */	
	private Alignment mediation = null;
	private AlignmentParser parser = null;
	
	/**
	 * At least for printing purposes it is handy to have available a jena RDF model   
	 */
	private Model RDFmodel = null;
	
	private org.semanticweb.owl.align.Alignment al = null;
	
	/**
	 * The MediatorFactory class constructor parses and stores the EDOAL alignment to apply.
	 * @param object of type: String, URI, Reader or InputStream; containing the EDOAL alignment
	 * @throws AlignmentException the alignment exception
	 */
	public MediatorFactory(Object o) throws UnsupportedOperationException {
		try {
			// Read and parse the EDOAL alignment. Several serialisations for alignments exists
			// hence we need to consider them all and cast to the correct type.
			
			if ( o instanceof String) this.edoal = new EDOAL((String) o); 
			else {
				log.log(Level.WARNING,	"Class not handled yet:" + o.getClass());
				throw new UnsupportedOperationException("MediatorFactory: Cannot yet handle EDOAL serialisations of type " + o.getClass());
			}
			
			//if ( o instanceof String) this.edoal.align = this.edoal.parser.parseString((String) o); 
			//if ( o instanceof URI ) this.edoal.align = this.edoal.parser.parse( (URI) o );
			//if ( o instanceof Reader ) this.edoal.align = this.edoal.parser.parse((Reader) o);
			//if ( o instanceof InputStream ) this.edoal.align = this.edoal.parser.parse((InputStream) o);
			//else Logger.getAnonymousLogger().log(Level.WARNING,	"Class not handled yet:" + o.getClass());
		} catch (AlignmentException e) {
			log.log(Level.SEVERE, "Couldn't load the EDOAL alignment:", e);
		}
	}
	
	
	/**
	 * Generates the transformation rewriting rules from their definition in the alignment specification.
	 * @param language (optional) - the language to represent the data that the rules rewrite; assumed RDF
	 * @return Transformation - the rewriting rules that are generated
	 * @throws AlignmentException the alignment exception
	 * TODO: Generate the mediation for other data languages than RDF (e.g., SPIN?)
	 */
	public Alignment generateMediation() throws AlignmentException {
		try {
			this.mediation = EDOALMediator.mediate(this.edoal.align);
		} catch (AlignmentException e) {
			log.log(Level.SEVERE, "Couldn't generate the proper rewriting rules for this mediation:", e);
		}
		return this.mediation;
	}

	/**
	 * @return the mediation
	 */
	public Alignment getMediation() {
		return this.mediation;
	}

	/**
	 * @return the printable format of the mediation
	 */
	public String toString() {
		int indent = 4;
		this.RDFmodel = ((JenaAlignment)this.mediation).getModel();
		String syntax = "N-TRIPLE";
		StringWriter rdf = new StringWriter();
		StringWriter rdf2 = new StringWriter();
		RDFmodel.write(rdf, syntax);
		String s = "Mediation:\n";
		Hashtable<Triple, List<Triple>> p = this.mediation.getPatterns();

		System.out.println("keys:\n");
		Enumeration <Triple>e = p.keys();
		while (e.hasMoreElements()) {
			System.out.println(e.nextElement());
		}
		System.out.println("TrplList:\n");
		Enumeration <List<Triple>>e1 = p.elements();
		while (e1.hasMoreElements()) {
			System.out.println(e1.nextElement());
		}

		System.out.println("Full map:\n");
		System.out.println(p);
		
		String t = rdf.toString();
		s.concat(t);
		s.concat("\n === \n");
		return s.concat(String.format("%" + indent + "s", this.edoal.toString()));
		//return s.concat(this.edoal.toString());
	}

}
