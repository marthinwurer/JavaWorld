/**
 * Created by benjamin on 4/13/16.
 */
public enum Projection {
    flat, isometric;

    private static Projection[] vals = values();

    public Projection next(){
        return vals[(this.ordinal() +1 ) % vals.length];
    }

}
