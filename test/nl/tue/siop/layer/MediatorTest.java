/**
 * 
 */
package nl.tue.siop.layer;

import static org.junit.Assert.*;
//import static org.junit.matchers.JUnitMatchers.*;

//import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import org.semanticweb.owl.align.AlignmentException;

import com.hp.hpl.jena.shared.DoesNotExistException;




/**
 * @author Paul Brandt <paul@brandt.name>
 *
 */
public class MediatorTest {

	/**
	 * Test method for {@link nl.tue.siop.layer.MediatorGenerator#Mediator(java.lang.Object)}.
	 * @throws AlignmentException 
	 */
	
	@Rule
	public ExpectedException e = ExpectedException.none();
	
	@Test
	public void testMediatorException() {
		String sourceFile = "file:fileDoesNotExist.xml";
		e.expect(UnsupportedOperationException.class);
		e.expectMessage(org.hamcrest.CoreMatchers.containsString("MediatorGenerator"));
		
		new MediatorGenerator(sourceFile);
	}
	
	/**
	 * Test method for {@link nl.tue.siop.layer.MediatorGenerator#Mediator(java.lang.Object)}.
	 * @throws AlignmentException 
	 */
	@Test
	public void testMediator() {
		String sourceFile = "file:resources/nl/myAlign.xml";
		MediatorGenerator m = null;
		
		try {
			m = new MediatorGenerator(sourceFile);
			// m2 = new MediatorGenerator("uri:resources/nl/myAlign.xml");
			// m3 = new MediatorGenerator( );
			// m4 = new MediatorGenerator( );
		} catch (UnsupportedOperationException e) {
			fail("Parsing as String serialisation failed for: " + sourceFile);
			e.printStackTrace();
		}
		assertNotNull("Success: parse EDOAL alignment as String: " + sourceFile, m);
		// fail("ToDo: Parsing URI serialisation, not yet implemented");
		// fail("ToDo: Parsing Reader serialisation, not yet implemented");
		// fail("ToDo: Parsing InputStream serialisation, not yet implemented");
	}

	
	/**
	 * Test method for {@link nl.tue.siop.layer.MediatorGenerator#generateMediation()}.
	 */
	@Test
	public void testGenerateMediation() {
		String sourceFile = "file:resources/nl/myAlign.xml";
		MediatorGenerator m = null;
		
		try {
			m = new MediatorGenerator(sourceFile);
			m.generateMediation();
		} catch (AlignmentException e) {
			fail("Generating mediation from alignment");
			e.printStackTrace();
		}
		assertNotNull("Could generate alignment", m.toString());
	}

}
