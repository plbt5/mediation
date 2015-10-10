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

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;

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
		String configFile = "./resources/nl/test2.json";
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
				dataAFileName = (String) jsonObject.get("dataTo");
				dataBFileName = (String) jsonObject.get("dataFrom");

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
						log.log(Level.SEVERE, "Cannot add EDOAL alignment: " + alignFile);
					}
					// Show the results
					sap.showMediation();

					// Simulate a SAP call to send message
					// 		First get the message (in own language) to send
					
					String qryString = null;
					try {
						qryString = Utilities.readFile(dataAFileName, StandardCharsets.UTF_8);
					} catch (IOException e) {
						log.log(Level.SEVERE, "Cannot read from: " + dataAFileName);
						e.printStackTrace();
					} catch (NullPointerException e) {
						log.log(Level.SEVERE, "DataTo file does not exist: " + dataAFileName);
					}
					
					// 		Also, initialise the peer communicator
					
					if (!sap.getMediator().getPH().initPeer(qryString,sap.getMediator().getOnto2()) ) {
						log.log(Level.SEVERE, "Cannot read onto2");
					};
					
					// 		Secondly, send the message
					System.out.println("----> Original query: [\n" + qryString + "\n]\n");
					if (!sap.sendQ(qryString)) {
						log.log(Level.SEVERE, "Cannot send query: " + qryString);
					} else {
						//	Thirdly, await the response
						String queryResult = sap.receiveA();
						if (queryResult == null) {
							log.log(Level.SEVERE, "Cannot receive answer to query.");
						}
					}
					
					// Simulate the receipt of a message
					
					File dataBFile = new File(dataBFileName);
					if (dataBFile.exists() && !dataBFile.isDirectory()) {
						// Simulate the SAP call from the other app to receive its
						// data
						//
						// Firstly, initialise the simulation, e.g.
						// put the 'received' query from the file into the protocol's receipt message
						qryString = null;
						try {
							qryString = Utilities.readFile(dataBFileName, StandardCharsets.UTF_8);
						} catch (IOException e) {
							log.log(Level.SEVERE, "Cannot read from: " + dataBFileName);
							e.printStackTrace();
						}
						
						// Secondly, as an application, get the received message
						Query rcvdQry = sap.receiveQ();
						if (rcvdQry != null) {
							System.out.println("----> Received native query: [\n" + rcvdQry + "\n]\n");
						} else {
							log.log(Level.SEVERE, "Cannot receive query.");
						} 
					} else {
						log.log(Level.INFO, "Cannot demonstrate inverse translation due to absence of dataBFile.");
					}

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
