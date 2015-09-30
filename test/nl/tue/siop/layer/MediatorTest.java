/**
 * 
 */
package nl.tue.siop.layer;

import static org.junit.Assert.*;
//import static org.junit.matchers.JUnitMatchers.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;

//import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import org.hamcrest.core.Is;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import org.semanticweb.owl.align.AlignmentException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.shared.DoesNotExistException;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import uk.soton.service.mediation.Alignment;
import uk.soton.service.mediation.JenaAlignment;
import uk.soton.service.mediation.edoal.EDOALMediator;




/**
 * @author Paul Brandt <paul@brandt.name>
 *
 */
public class MediatorTest {

	Mediator m;
	EDOALAlignment ea;
	File alF = null, toF = null, fmF = null;
	@Before
	public void setUp() throws Exception {
		File f = new File("./test/nl/tue/siop/layer/test1.json");
		setConfig(f);
		
		// init parser
		AlignmentParser ap = new AlignmentParser(0);
		ap.initAlignment(null);
		// init edoal alignment
		this.ea = new EDOALAlignment();
		String s = new String(Files.readAllBytes(Paths.get(alF.getAbsolutePath())));
		this.ea = (EDOALAlignment) ap.parseString(s);
	}

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
	 * Test method for {@link nl.tue.siop.layer.MediatorFactory#Mediator(java.lang.Object)}.
	 * @throws AlignmentException 
	 */
	
	@Rule
	public ExpectedException e = ExpectedException.none();
	
	@Test
	public void testNullPointerExceptionConstructorMediator() {
		//String sourceFile = "fileDoesNotExist.xml";
		e.expect(NullPointerException.class);
		e.expectMessage(org.hamcrest.CoreMatchers.containsString("Cannot create mediator without"));
		
		this.m = new Mediator((EDOALAlignment) null);
	}

	
	/**
	 * Test method for {@link nl.tue.siop.layer.MediatorFactory#Mediator(java.lang.Object)}.
	 * @throws AlignmentException 
	 */
	@Test
	public void testConstructorMediator() {
		this.m = new Mediator(this.ea);
		assertNotNull("Creating mediator", this.m);
		assertThat("Mediator is populated with correct edoalId", this.m.getEdoalId(), is("http://oms.omwg.org/ontoA-ontoB/"));
		assertEquals("Mediator is populated with correct edoal alignmnet", this.m.getEdoalAlignment(),this.ea);
		assertThat("Mediator is populated with correct id", this.m.getId(), containsString("ontoA-ontoB/_mediator"));
	}

	
	/**
	 * Test method for {@link nl.tue.siop.layer.MediatorFactory#generateMediation()}.
	 * @throws Exception 
	 */
	@Test
	public void testTranslate() throws Exception {
		this.m = new Mediator(this.ea);
		assertNotNull("Could generate alignment", this.m);
		
		JenaAlignment ja = (JenaAlignment) EDOALMediator.mediate(ea);
		this.m.setJenaAlignment(ja);
		
		Query qTo 			= this.m.readFile(toF);
		Query qFrom 		= this.m.readFile(fmF);
		Query transQTo 		= this.m.translate(qTo);
		Query transQFrom 	= this.m.translate(qFrom);
		assertEquals("Translate(dataTo) equals dataFrom", qFrom, transQTo);
		assertEquals("Translate(dataFrom) equals dataTo", qTo, transQFrom);
	}

}
