/**
 * 
 */
package nl.tue.siop.layer;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import uk.soton.service.mediation.JenaAlignment;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author brandtp
 *
 */
@RunWith(Parameterized.class)
public class SAPTest {
	private String hasRelationProp = "http://ecs.soton.ac.uk/om.owl#hasRelation";
	private File fIn;
	private File fER;
	private SAP sap;
	private File alF = null, toF = null, fmF = null;
	private String edoalIRI = null, sourceOntIRI = null, targetOntIRI = null, alRelation = null;
	private Integer totalPatterns = new Integer(0);
	private Resource testResource = (Resource) null;
	private Model testModel = ModelFactory.createDefaultModel();
	private String relationURI = null;
	/**
	 * @throws java.lang.Exception
	 */
//	@Before
//	public void setUp() throws Exception {
//		sap = new SAP();
//		setConfig(fIn);
//	}

	private void setConfig() {
		JSONParser parser = new JSONParser();
		// read the json file
		if (this.fIn.exists() && !fIn.isDirectory()) {
			Object obj = null;
			try {
				obj = parser.parse(new FileReader(fIn));
			} catch (IOException | ParseException e) {
				fail("Cannot read test configuration file: " + fIn.getAbsolutePath() + "\n" + e);
			}
			JSONObject jsonObject = (JSONObject) obj;

			// get a String from the JSON object
			
			try {
				alF = new File((String) jsonObject.get("align"));
				toF = new File((String) jsonObject.get("dataTo"));
				fmF = new File((String) jsonObject.get("dataFrom"));
			} catch (NullPointerException e) {
				fail("Cannot open all configuration files from " + fIn.getAbsolutePath() + "\n" + e);
			}
		} else {
			fail("Test configuration file does not exist: " + fIn.getAbsolutePath());
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
		
		// read the json file
		if (fER.exists() && !fER.isDirectory()) {
			Object obj = null;
			try {
				obj = parser.parse(new FileReader(fER));
			} catch (FileNotFoundException e) {
				fail("Cannot find test configuration file: " + fER.getAbsolutePath() + "\n" + e);
			} catch (ParseException e) {
				fail("Cannot parse test configuration file: " + fER.getAbsolutePath() + "\n" + e);
			} catch (IOException e) {
				fail("Cannot read test configuration file: " + fER.getAbsolutePath() + "\n" + e);
			}
			JSONObject jsonObject = (JSONObject) obj;

			// get a String from the JSON object
			
			try {
				edoalIRI = (String) jsonObject.get("edoalIRI");
				sourceOntIRI = (String) jsonObject.get("sourceOntIRI");
				targetOntIRI = (String) jsonObject.get("targetOntIRI");
				totalPatterns = Integer.parseInt(jsonObject.get("totalPatterns").toString());
				switch ((String) jsonObject.get("alRelation")) {
				case "=": // Equivalence
					testResource = testModel.createResource("http://ecs.soton.ac.uk/om.owl#EQ");
					break;
				case "%": // Non equivalence
					testResource = testModel.createResource("http://ecs.soton.ac.uk/om.owl#NEQ");
					break;
				case "<": // SubsumedBy
					testResource = testModel.createResource("http://ecs.soton.ac.uk/om.owl#LT");
					break;
				case ">": // Subsumes
					testResource = testModel.createResource("http://ecs.soton.ac.uk/om.owl#GT");
					break;
				case "~>": // Non transitive implication
					testResource = testModel.createResource("http://ecs.soton.ac.uk/om.owl#NTI");
					break;
				case "InstanceOf":
					testResource = testModel.createResource("http://ecs.soton.ac.uk/om.owl#IO");
					break;
				case "HasInstance":
					testResource = testModel.createResource("http://ecs.soton.ac.uk/om.owl#HI");
					break;
				default:
					fail("Cannot get alRelation from " + fER.getAbsolutePath());
					break;
				}
			} catch (NullPointerException e) {
				fail("Cannot read expected results from " + fER.getAbsolutePath() + "\n" + e);
			} catch(NumberFormatException e) {
				fail("Cannot get totalPatterns from expected results pattern: should be \"5\"" + "\n" + e);
			}
		} else {
			fail("Test expected results file does not exist: " + fER.getAbsolutePath());
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
	public SAPTest (String fIn, String fER) {
		this.fIn = new File(fIn);
		this.fER = new File(fER);
		sap = new SAP();
		setConfig();
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
	public static Collection<String[]> dataFiles() {
		return Arrays.asList(new String[][] { 
			{ "./resources/nl/test1.json", "./resources/nl/test1ExpectedResults.json" }, 
			{ "./resources/nl/test2.json", "./resources/nl/test2ExpectedResults.json" } 
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
		assertThat("EDAOL alignment has been parsed", this.sap.getMediator().getEdoalId(), containsString(edoalIRI)); 
	}

	/**
	 * Test method for {@link nl.tue.siop.layer.SAP#addEDOALALignment(java.lang.String)}.
	 */
	@Test
	public void testAssertCreatedJenaAddEDOALALignment() throws Exception {
		this.sap.addEDOALALignment(alF);
		assertThat("Jena alignment fails on source ontology", this.sap.getMediator().getJenaAlignment().getSourceOntologyURIs().iterator().next(), is(sourceOntIRI));
		assertThat("Jena alignment fails on target ontology", this.sap.getMediator().getJenaAlignment().getTargetOntologyURIs().iterator().next(), is(targetOntIRI));
		assertThat("Jena alignment fails to match correct number of patterns", this.sap.getMediator().getJenaAlignment().getPatterns().size(),  is(totalPatterns));
		Property prop = ((JenaAlignment)this.sap.getMediator().getJenaAlignment()).getModel().getProperty(hasRelationProp);
		ResIterator ri = ((JenaAlignment)this.sap.getMediator().getJenaAlignment()).getModel().listSubjectsWithProperty(prop);
		assertTrue("Jena alignment fails: no hasRelation", ri.hasNext());
	    while (ri.hasNext()) {
	    	assertThat("Jena alignment fails on alignment relation", ri.nextResource().getProperty(prop).getResource(), is(testResource));
	    }
	}

}
