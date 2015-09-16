/**
 * 
 */
package nl.tue.siop.layer;

/**
 * @author brandtp
 *
 */
public class Protocol {

	/**
	 * This represents a message that will be 'received';
	 * Temporarily until the protocol becomes communicating twin instances.
	 * 
	 */
	private String messageR = 
				"PREFIX ex:	<http://example.org/demo#>\n" +
				"PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
				"PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n" +
				"SELECT ?person\n" +
				"WHERE {\n" +
				"    ?person a ex:Person .\n" +
				"    ?person ex:age ?age .\n" +
				"    FILTER (?age > 18) .\n" +
				"}";
				
	/**
	 * @param messageR the messageR to set
	 */
	public void setMessageR(String messageR) {
		this.messageR = messageR;
	}

	/**
	 * 
	 */
	public Protocol() {
		// TODO Auto-generated constructor stub
	}
	
	public boolean send(String msg) {
		System.out.println("Sending message:\n" + msg);
		return true;
	}
	
	public String receive() {
		return this.messageR;
	}
}
