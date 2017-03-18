/**
 * Created by benjamin on 4/13/16.
 */
public class Utilities {

    /**
     * formula taken from http://stackoverflow.com/a/22153181
     * @param fracx - the fractional x value
     * @param fracy - the fractional y value
     * @param negxposy - the point closest to the x axis
     * @param posxposy - the point furthest to the origin
     * @param negxnegy - the point closest to the origin
     * @param posxnegy - the point closest to the y axis
     * @return - the interpolated value
     */
    public static float bilinearInterpolation(
            float fracx, float fracy,
            float negxposy, float posxposy, float negxnegy, float posxnegy){

        return  (1 - fracx) *
                ((1 - fracy) * negxnegy +
                fracy * negxposy) +
                fracx *
                ((1 - fracy) * posxnegy +
                fracy * posxposy);
    }

    /**
     * mathematical mod for java == a % b
     * @param a
     * @param b
     */
    public static int mod(int a, int b){
        return (a < 0) ? (b + a) : (a % b);
    }
}
