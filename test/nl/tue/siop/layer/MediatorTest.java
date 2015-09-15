/**
 * 
 */
package nl.tue.siop.layer;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

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
		try {
			e.expect(AlignmentException.class);
			new MediatorGenerator(sourceFile);
			fail("Should have thrown an DoesNotExistException or AlignmentException!");
		} catch (AlignmentException e) {
			assertThat(e.getMessage(), containsString(sourceFile));
		} catch (DoesNotExistException e) {
			assertThat(e.getMessage(), containsString(sourceFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		} catch (AlignmentException e) {
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
