/**
 * 
 */
package nl.tue.siop.layer;

import java.io.File;
import java.io.FileReader;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * @author brandtp
 *
 */
public class demo {

	private static final Logger log = Logger.getLogger( demo.class.getName() );
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String configFile = "./resources/nl/test1.json";
		String alignFile;
		String dataAFile;
		String dataBFile;

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
				alignFile = (String) jsonObject.get("align");
				dataAFile = (String) jsonObject.get("dataA");
				dataBFile = (String) jsonObject.get("dataB");
	
				System.out.println("align: " + alignFile);
				System.out.println("dataA: " + dataAFile);
				System.out.println("dataB: " + dataBFile);
				
				File align = new File(alignFile);
				if (align.exists() && !align.isDirectory()) {
					// Simulate a SAP init call from the alignment
					SAP sap = new SAP();
					sap.addEDOALALignment("file:" + alignFile);
					
					// Simulate a SAP call to send message
					// TODO call SAP.send
					
					// Simulate the SAP call from the other app to receive its data
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
