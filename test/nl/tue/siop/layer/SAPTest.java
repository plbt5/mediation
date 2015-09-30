/**
 * 
 */
package nl.tue.siop.layer;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
//import java.nio.file.NoSuchFileException;
import java.util.Collection;

//import org.hamcrest.core.Is;
import static org.hamcrest.CoreMatchers.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.semanticweb.owl.align.AlignmentException;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author brandtp
 *
 */
@RunWith(Parameterized.class)
public class SAPTest {
	private File inputFile;
	private Boolean expectedResult;
	
	private SAP sap;
	private File alF = null, toF = null, fmF = null;
	/**
	 * @throws java.lang.Exception
	 */
//	@Before
//	public void setUp() throws Exception {
//		sap = new SAP();
//		setConfig(inputFile);
//	}

	private void setConfig(File f) {
		JSONParser parser = new JSONParser();
		// read the json file
		if (f.exists() && !f.isDirectory()) {
			Object obj = null;
			try {
				obj = parser.parse(new FileReader(f));
			} catch (IOException | ParseException e) {
				fail("Cannot read test configuration file: " + f.getAbsolutePath());
			}
			JSONObject jsonObject = (JSONObject) obj;

			// get a String from the JSON object
			
			try {
				alF = new File((String) jsonObject.get("align"));
				toF = new File((String) jsonObject.get("dataTo"));
				fmF = new File((String) jsonObject.get("dataFrom"));
			} catch (NullPointerException e) {
				fail("Cannot open all configuration files from " + f.getAbsolutePath());
			}
		} else {
			fail("Test configuration file does not exist: " + f.getAbsolutePath());
		}
		if (!alF.exists() || alF.isDirectory()) {
			fail("Cannot open alignment file " + alF.getAbsolutePath());
		}
		if (!toF.exists() || toF.isDirectory()) {
			fail("Cannot open dataTo file " + toF.getAbsolutePath());
		}
		if (!fmF.exists() || fmF.isDirectory()) {
			fail("Cannot open dataFrom file " + fmF.getAbsolutePath());
		}
	}

	/**
	 * Since this test is run as many times as there are different test data files,
	 * a constructor is required to initialise the test with the relevant test data.
	 * Every time a testrunner triggers, it will pass the arguments from parameters
	 * that are defined in the dataFiles method (since that method has been annotated
	 * with '@Parameters'.
	 * TODO Rework this to see if and how the expectedResults can play a role. 
	 */
	public SAPTest (String f, Boolean b) {
		this.inputFile = new File(f);
		this.expectedResult = b;
		sap = new SAP();
		setConfig(inputFile);
	}
	
	/**
	 * This dataFile method, since it has been annotated with '@Parameters',
	 * is called every time the testrunner triggers, and returns a Collection of Objects
	 * (as Array) as the test data set, that is subsequently used by the constructor to
	 * initialise the test scenario.
	 * TODO Add other test scenarios by adding new test#.json's and test data files.
	 * @return
	 */
	@Parameters
	public static Collection<Object[]> dataFiles() {
		return Arrays.asList(new Object[][] { 
			{ "./test/nl/tue/siop/layer/test1.json", true } 
		});
	}

	@Rule
	public ExpectedException thrown1 = ExpectedException.none();
	/**
	 * Test method for {@link nl.tue.siop.layer.SAP#addEDOALALignment(java.lang.String)}.
	 * @throws IOException 
	 * @throws AlignmentException 
	 * @throws UnsupportedOperationException 
	 */
	@Test
	public void testAddTwoAlignmentsAddEDOALALignment() throws Exception {
		thrown1.expect(UnsupportedOperationException.class);
	    thrown1.expectMessage("add another EDOAL");
		this.sap.addEDOALALignment(alF);
		this.sap.addEDOALALignment(alF);
		fail("No Exception Thrown: Despite Adding More Alignments To One Mediator");
	}

	@Rule
	public ExpectedException thrown2 = ExpectedException.none();
	/**
	 * Test method for {@link nl.tue.siop.layer.SAP#addEDOALALignment(java.lang.String)}.
	 */
	@Test
	public void testEmptyALignFileAddEDOALALignment() throws Exception {
		thrown2.expect(IOException.class);
		thrown2.expectMessage("Alignment file does not exist");
		this.sap.addEDOALALignment((File)null);
		fail("No Exception Thrown Despite Empty Alignment File Descriptor");
	}
	
	/**
	 * Test method for {@link nl.tue.siop.layer.SAP#addEDOALALignment(java.lang.String)}.
	 */
	@Test
	public void testAssertCreatedMediatorAddEDOALALignment() throws Exception {
		this.sap.addEDOALALignment(alF);
		assertThat("EDAOL alignment has been parsed", this.sap.getMediator().getEdoalId(), is("http://oms.omwg.org/ontoA-ontoB/")); 
	}

	/**
	 * Test method for {@link nl.tue.siop.layer.SAP#addEDOALALignment(java.lang.String)}.
	 */
	@Test
	public void testAssertCreatedJenaAddEDOALALignment() throws Exception {
		this.sap.addEDOALALignment(alF);
		assertThat("Jena alignment has correct source ontology", this.sap.getMediator().getJenaAlignment().getSourceOntologyURIs().iterator().next(), is("http://tutorial.topbraid.com/ontoA#"));
		assertThat("Jena alignment has correct target ontology", this.sap.getMediator().getJenaAlignment().getTargetOntologyURIs().iterator().next(), is("http://tutorial.topbraid.com/ontoB#"));
		assertTrue("Jena alignment has more than 0 match patterns", this.sap.getMediator().getJenaAlignment().getPatterns().size() > 0);
	}

}
