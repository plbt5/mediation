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

import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
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
 * The Class MediatorFactory generates Mediators. 
 *
 * The MediatorFactory Class provides the following features:<br/>
 * (i) Creating a mediator from an EDOAL alignment; (ii) generating a corresponding Jena alignment from it   
 * 
 * TODO: Support multiple data languages for a single alignment
 * 		Mediations are also dependent on the language that represents the ontology data. We assume 
 * 		RDF pattern rewriting rules for this moment. On extension to other languages, multiple mediations will emerge, hence 
 * 		Class uk.soton.service.mediation.Alignment need to become overloaded with other constructions to host
 * 		other type of rewriting rules. An even better solution would be to abstract the different types of 
 * 		Alignments away in a Mediation Class.
 * 
 * @author Paul Brandt <paul@brandt.name>
 *
 */
public class MediatorFactory {

	private static final Logger log = Logger.getLogger( MediatorFactory.class.getName() );
	
	/**
	 * The MediatorFactory class creates a mediator, specifically tailored to the alignment
	 * of choice. 
	 * @param File The file that contains the EDOAL alignment
	 */
	
	public MediatorFactory() {
	}

	
	/**
	 * Create a mediator.
	 * We assume mediation of RDF-statements for this moment. However, the EDOAL alignment
	 * provides for room to extend this to other data formats as well. That is to say, for
	 * this to happen, the correspondence specification needs adoption:
	 * <edoal><map><Cell><entity1> now is implicitly defined as rdf:resource=''
	 * @return
	 */
	public Mediator createMediator(EDOALAlignment ea) throws NullPointerException {
		Mediator m = new Mediator(ea);
		try {
			m.setJenaAlignment(generateMediation(ea));
		} catch (AlignmentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (m.getJenaAlignment() == null) {
			throw new NullPointerException("Couldn't generate mediation");
		}
//		MediatorFactory.mediators.add(m);
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
	private JenaAlignment generateMediation(EDOALAlignment ea) throws AlignmentException {
		JenaAlignment alignment = null;
		try {
			alignment = (JenaAlignment) EDOALMediator.mediate(ea);
		} catch (AlignmentException e) {
			log.log(Level.SEVERE, "Couldn't generate the proper rewriting rules for this mediation: " +
					ea.toString(), e);
		}
		return alignment;
	}
	

}
