/**
 * 
 */
package nl.tue.siop.layer;

/**
 * A Utility class that implements a pair of things. Used here to 
 * keep a forward and backward mediation together.
 * @author Paul Brandt <paul@brandt.name>
 *
 */

public class Pair<Fwd, Bwd> {
    private Fwd f;
    private Bwd b;
    public Pair(Fwd f, Bwd b){
        this.f = f;
        this.b = b;
    }
    public Fwd getFwd(){ return f; }
    public Bwd getBwd(){ return b; }
    public void setFwd(Fwd f){ this.f = f; }
    public void setBwd(Bwd b){ this.b = b; }
}
