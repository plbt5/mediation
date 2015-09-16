/**
 * 
 */
package nl.tue.siop.layer;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author brandtp
 *
 */
public class SAPTest {
	SAP sap;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.sap = new SAP();
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	/**
	 * Test method for {@link nl.tue.siop.layer.SAP#addEDOALALignment(java.lang.String)}.
	 */
	@Test
	public void testAddEDOALALignment() {
		String alignFile = "file:resources/nl/myAlign.xml";
		thrown.expect(UnsupportedOperationException.class);
	    thrown.expectMessage("add another EDOAL");
		this.sap.addEDOALALignment(alignFile);
		this.sap.addEDOALALignment(alignFile);
		fail("No Exception Thrown: Despite Adding More Alignments To One Mediator");
	}

}
