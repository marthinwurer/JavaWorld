/**
 * Created by benjamin on 4/13/16.
 */
public enum DisplayState {
    height_and_water, steepness, shadows;

    private static DisplayState[] vals = values();

    public DisplayState next(){
        return vals[(this.ordinal() +1 ) % vals.length];
    }
}
