/**
 * 
 */
package nl.tue.siop.layer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;

/**
 * @author brandtp
 *
 */
public class demo {

	private static final Logger log = Logger.getLogger(demo.class.getName());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String configFile = "./resources/nl/test1.json";
		String alignFileName;
		String dataAFileName;
		String dataBFileName;

		if (args.length > 1) {
			configFile = args[1];
		}

		JSONParser parser = new JSONParser();

		try {
			// read the json file
			File init = new File(configFile);
			if (init.exists() && !init.isDirectory()) {
				Object obj = parser.parse(new FileReader(configFile));
				JSONObject jsonObject = (JSONObject) obj;

				// get a String from the JSON object
				alignFileName = (String) jsonObject.get("align");
				dataAFileName = (String) jsonObject.get("dataA");
				dataBFileName = (String) jsonObject.get("dataB");

				System.out.println("align: " + alignFileName);
				System.out.println("dataA: " + dataAFileName);
				System.out.println("dataB: " + dataBFileName);

				// Access the SAP
				File alignFile = new File(alignFileName);
				if (alignFile.exists() && !alignFile.isDirectory()) {
					// Simulate a SAP init call from the alignment
					SAP sap = new SAP();
					try {
						sap.addEDOALALignment(alignFile);
					} catch (UnsupportedOperationException e) {
						log.log(Level.SEVERE, "SAP: Cannot add EDOAL alignment: " + alignFile);
					}
					sap.showMediation();

					// Simulate a SAP call to send message

					String qryString = null;
					try {
						qryString = Utilities.readFile(dataAFileName, StandardCharsets.UTF_8);
					} catch (IOException e) {
						log.log(Level.SEVERE, "SAP: Cannot read from: " + dataAFileName);
						e.printStackTrace();
					}
					System.out.println("----> Original query: [\n" + qryString + "\n]\n");
					if (!sap.send(qryString)) {
						log.log(Level.SEVERE, "SAP: Cannot send query: " + qryString);
					}

					// Simulate the SAP call from the other app to receive its
					// data
					// TODO call SAP.receive

				} else {
					log.log(Level.SEVERE, "EDOAL alignment file does not exist: " + alignFile);
				}

			} else {
				log.log(Level.SEVERE, "Demo init file does not exist:" + configFile);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
