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
	 * TODO Duplicate Protocol Handlers to represent being each others peer
	 */
	private String messageR = null;
				
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
		System.out.println("Sending foreign message:\n" + msg + "\n");
		return true;
	}
	
	public String receive() {
		System.out.println("Receiving foreign message:\n" + this.messageR + "\n");
		return this.messageR;
	}
}
