/**
 * 
 */
package nl.tue.siop.layer;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

/**
 * @author brandtp
 *
 */
public class EntityLanguage {

	private Query q = null;
	private String s = null;
	
	/**
	 * @return
	 */
	public Object toSparql() {
		Query query = QueryFactory.create(this.s);
		return null;
	}

}
