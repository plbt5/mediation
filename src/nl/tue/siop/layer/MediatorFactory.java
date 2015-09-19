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
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
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
import uk.soton.service.mediation.edoal.EDOALQueryGenerator;

/**
 * The Class MediatorFactory manages (generates, deletes?) Mediators. 
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
	
	EDOAL getEdoal() {
		return this.edoal;
	}
	
	/**
	 * The mediators that this factory manages
	 */
	private static List<Mediator> mediators = new ArrayList<Mediator>();

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
	
	/**
	 * At least for printing purposes it is handy to have available a jena RDF model   
	 */
	private Model RDFmodel = null;
	
	//private org.semanticweb.owl.align.Alignment al = null;
	
	/**
	 * The MediatorFactory class creates a mediator, specifically tailored to the alignment
	 * of choice. 
	 */
	
	public MediatorFactory(String edoal) {
		this.edoal = parseEDOAL(edoal);
	}
	
	/**
	 * Every mediation is based on an EDOAL alignment. Since a mediator is build by its factory, the latter
	 * needs to parse at least one EDOAL Alignment. 
	 * 
	 * @param object of type: String, URI, Reader or InputStream; containing the EDOAL alignment
	 * @throws AlignmentException the alignment exception
	 */
	private EDOAL parseEDOAL(Object o) throws UnsupportedOperationException {
		EDOAL edoal = null;
		try {
			// Read and parse the EDOAL alignment. Several serialisations for alignments exists
			// hence we need to consider them all and cast to the correct type.
			
			if ( o instanceof String) edoal = new EDOAL((String) o); 
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
		return edoal;
	}
	
	/**
	 * Create a mediator.
	 * We assume mediation of RDF-statements for this moment. However, the EDOAL alignment
	 * provides for room to extend this to other data formats as well. That is to say, for
	 * this to happen, the correspondence specification needs adoption:
	 * <edoal><map><Cell><entity1> now is implicitly defined as rdf:resource=''
	 * @return
	 */
	public Mediator createMediator() throws NullPointerException {
		Mediator m = new Mediator(this.edoal);
		try {
			m.setMediation(generateMediation());
		} catch (AlignmentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (m.getMediation() == null) {
			throw new NullPointerException("Couldn't generate mediation");
		}
		MediatorFactory.mediators.add(m);
		return m;
	}
	/**
	 * Generates the mediation rewriting rules from their definition in the alignment specification. 
	 * Only when the mediation rules have been generated, it is able to process and transform statements from 
	 * and to the native languages that are used to express the data.
	 * @param language (optional) - the language to represent the data that the rules rewrite; assumed RDF
	 * @return Transformation - the rewriting rules that are generated
	 * @throws AlignmentException the alignment exception
	 * TODO: Generate the mediation rules for other data languages than RDF (e.g., SPIN?)
	 */
	private Alignment generateMediation() throws AlignmentException {
		Alignment mediation = null;
		try {
			mediation = EDOALMediator.mediate(this.edoal.getAlignment());
		} catch (AlignmentException e) {
			log.log(Level.SEVERE, "Couldn't generate the proper rewriting rules for this mediation: " +
					this.edoal.getName(), e);
		}
		return mediation;
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
		this.RDFmodel = ((JenaAlignment)this.mediation).getModel();
		String syntax = "N-TRIPLE";
		StringWriter rdf = new StringWriter();
		RDFmodel.write(rdf, syntax);
		
		String s = "Mediation:\n";
		Hashtable<Triple, List<Triple>> p = this.mediation.getPatterns();

/*		Enumeration <Triple>ek = p.keys();
		Enumeration <List<Triple>>ee = p.elements();
		while (ek.hasMoreElements()) {
			s = s + "Key: "+ ek.nextElement().toString() + "\n";
			s = s + "Elt: "+ ee.nextElement().toString() + "\n";
		}*/
		
		System.out.println("s:\n" + s);
		System.out.println("Full map:\n");
		System.out.println(p);
		
		System.out.println("\nAs OWL axioms:\n");
		s = s + "\nEDOAL Alignment:\n" + this.edoal.toString();
		
		//s = s + "rdf:\n" + rdf.toString() + "\n === \n";
		return s;
		//return s.concat(this.edoal.toString());
	}

}
