import java.awt.*;

/**
 * Created by benjamin on 4/11/16.
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

public class Main extends BasicGame {

    public static int power = 8;
    public static final int SCREEN_HEIGHT = 700;
    public static final int SCREEN_WIDTH = 1024;

    public static final int OFFSET_X = SCREEN_WIDTH / 2;
    public static final int OFFSET_Y = 128;

    private int curry = OFFSET_Y;
    private int currx = OFFSET_X;

    private int shadowAngle = 45;
    private static Color shadow = new Color(0.0f,0.0f,0.0f,0.25f);

    public float seaLevel = 0.5f;

    public Random rand = new Random();
    public DiamondSquare fractal;
    private int dimension;


    public Color[][] pixels;
    private Image display;


    public float[][] standingwater;
    public float[][] height;
    public float[][] ownership;

    public boolean[][] shadowmap;


    private DisplayState state;

    private Projection projection;
    private boolean changed = true;

    public Main(String gamename) {
        super(gamename);
        fractal = new DiamondSquare(12345, power);
        height = fractal.data;
        dimension = fractal.width;

        fractal = new DiamondSquare(123456, power);
        ownership = fractal.data;
        pixels = new Color[dimension][dimension];
        state = DisplayState.height_and_water;
        projection = Projection.flat;
        shadowmap = new boolean[dimension][dimension];
    }

    @Override
    public void init(GameContainer gc) throws SlickException {

        buildMap();
        display = new Image(SCREEN_WIDTH, SCREEN_HEIGHT);

    }

    private void buildMap(){
        changed = true;

        if( state == DisplayState.height_and_water){
            for( int ii = 0; ii < dimension; ii++) {
                for (int jj = 0; jj < dimension; jj++) {
                    int val = (int) (256 * height[ii][jj]);
                    int val_s = (int) (128 * height[ii][jj] * height[ii][jj] * height[ii][jj]);
                    pixels[ii][jj] = new Color(0, val, 0);
                    if (height[ii][jj] < seaLevel) {
                        pixels[ii][jj] = new Color(0, 0, height[ii][jj]);
                    }
                }
            }
            calculateShadows();
        }

        else if (state == DisplayState.steepness){
            for( int ii = 1; ii < dimension - 1; ii++) {
                for (int jj = 1; jj < dimension - 1; jj++) {
                    int val = (int) (steepness(ii, jj) * 256 * 10);
                    pixels[ii][jj] = new Color(val, val, val);
                }
            }
        }
        else if( state == DisplayState.shadows){
            int val = 0;
            for( int ii = 1; ii < dimension - 1; ii++) {
                for (int jj = 1; jj < dimension - 1; jj++) {
                    if( shadowmap[ii][jj]){
                       val = 255;
                    }
                    else{
                        val = 0;
                    }
                    pixels[ii][jj] = new Color(val, val, val);
                }
            }

        }
    }


    private void calculateShadows(){

        /*
         * if the next point in the direction is lower than the linear height, then it is shadowed.
         *
         * if it is larger, then stop the ray.
         */
        // calculate the drop per tile
        // TODO: some trig stuff
        float drop = 1.0f / 256.0f + 0.0001f;

        shadowmap = new boolean[dimension][dimension];

        for (int yy = 0; yy < dimension; yy++) {
            for (int xx = 0; xx < dimension; xx++) {

                if( shadowmap[yy][xx]){
                    continue;
                }

                float ray = height[yy][xx];
                int y = yy;
                int x = xx;
                while( true ) {
                    y++;
                    x--;
                    ray -= drop;

                    if (y > 0 && x > 0 && y < dimension && x < dimension && ray > Math.max( height[y][x], seaLevel)) {
                        shadowmap[y][x] = true;
                    } else {
                        break;
                    }
                }
            }
        }


        for (int yy = 0; yy < dimension; yy++) {
            for (int xx = 0; xx < dimension; xx++) {

                if (shadowmap[yy][xx]) {
                    pixels[yy][xx] = pixels[yy][xx].darker();
                }
            }
        }

    }

    @Override
    public void keyPressed(int key, char c) {
        changed = true;
        switch (c) {
            case 'r':
                fractal = new DiamondSquare(rand.nextInt(), power);
                height = fractal.data;
                dimension = fractal.width;
                break;
            case 'w':
                seaLevel += 0.05;
                break;
            case 's':
                seaLevel -= 0.05;
                break;
            case 'n':
                state = state.next();
                break;
            case 'p':
                projection = projection.next();
                break;
        }
        switch (key) {
            case Input.KEY_UP:
                curry++;
                break;
            case Input.KEY_DOWN:
                curry--;
                break;
            case Input.KEY_LEFT:
                currx++;
                break;
            case Input.KEY_RIGHT:
                currx--;
                break;
            case Input.KEY_ESCAPE:
                System.exit(0);
                break;
        }
        buildMap();
    }

    @Override
    public void update(GameContainer gc, int i) throws SlickException {
        Input input = gc.getInput();
        if (input.isKeyDown(Input.KEY_UP)) {
            curry+=5;
            changed = true;
        }
        if (input.isKeyDown(Input.KEY_DOWN)) {
            curry-=5;
            changed = true;
        }
        if (input.isKeyDown(Input.KEY_LEFT)) {
            currx+=5;
            changed = true;
        }
        if (input.isKeyDown(Input.KEY_RIGHT)) {
            currx-=5;
            changed = true;
        }

        // do some movement and distribution
        changed = true;
        float[] mvector = {0.5f, 0.5f};

        // chose a random point
//        int xpoint = rand.nextInt(dimension) + 500;
//        int ypoint = rand.nextInt(dimension) + 500;

//        float[][] next = new float[dimension][dimension];
//        for (int yy = 0; yy < dimension; yy++) {
//            for (int xx = 0; xx < dimension; xx++) {
//
////                if( xx + 500 < xpoint && xx + 550 > xpoint &&
////                        yy + 500 < ypoint && yy + 550 > ypoint) {
////                if (ownership[yy][xx] > .5){
//
//                    float current = height[yy][xx];
//                    next[yy][xx] += current * 0.25f;
//                    next[(yy + 1) % dimension][xx] += current * 0.25f;
//                    next[yy][(xx + 1) % dimension] += current * 0.25f;
//                    next[(yy + 1) % dimension][(xx + 1) % dimension] += current * 0.25f;
////                }
////                else{
////                    next[yy][xx] += height[yy][xx];
////                }
//
//
//            }
//        }
//
//
//        height = next;

//        // talus angle erosion
//        next = new float[dimension][dimension];
//        for (int yy = 0; yy < dimension; yy++) {
//            for (int xx = 0; xx < dimension; xx++) {
//
//                float h = height[yy][xx];
//                float lowest = height[(yy - 1) %dimension][(xx - 1) %dimension];
//                lowest = Math.min(lowest, height[(yy)][Utilities.mod((xx - 1) , dimension)]);
//                lowest = Math.min(lowest, height[(yy + 1 ) %dimension][(xx - 1) %dimension]);
//                lowest = Math.min(lowest, height[(yy - 1) %dimension][xx]);
//                lowest = Math.min(lowest, height[(yy + 1) %dimension][xx]);
//                lowest = Math.min(lowest, height[(yy - 1) %dimension][(xx + 1) %dimension]);
//                lowest = Math.min(lowest, height[yy][(xx + 1) %dimension]);
//                lowest = Math.min(lowest, height[(yy + 1) %dimension][(xx + 1) %dimension]);
//
//                System.out.println(lowest);
//
//            }
//        }
//
//
        buildMap();
    }

    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException {

        if( this.changed == false){
            display.draw(0,0);
            return;
        }
        this.changed = false;

        if ( projection == Projection.flat) {
            for (int ii = 0; ii < dimension; ii++) {
                for (int jj = 0; jj < dimension; jj++) {
                    setPixel(g, pixels[ii][jj], jj, ii);
//                    if( shadowmap[ii][jj]){
//                        setPixel(g, shadow, jj, ii);
//                    }
                }
            }
        }
        else if (projection == Projection.isometric){
            int seaheight = (int) (OFFSET_Y * seaLevel);
            for (int yy = 0; yy < dimension; yy++) {
                for (int xx = 0; xx < dimension; xx++) {

                    int screenx = (xx - yy) * 2 + currx;
                    int screeny = (xx + yy) * 1 + curry;

                    int height = (int) (this.height[yy][xx] * OFFSET_Y);

                    if (height < seaheight){
                        height = seaheight;
                    }

//                    if( shadowmap[yy][xx]){
//                        pixels[yy][xx] = pixels[yy][xx].darker();
////                        drawTower(g, shadow, screenx, screeny, height);
//                    }
                    drawTower(g, pixels[yy][xx], screenx, screeny, height);
                }
            }
        }
        g.copyArea(display, 0, 0);
    }

    public static void main(String[] args) {
        try {
            AppGameContainer appgc;
            appgc = new AppGameContainer(new Main("Simple Slick Game"));
            appgc.setDisplayMode(1024, 700, false);
            appgc.setTargetFrameRate(60);
            appgc.start();

        } catch (SlickException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void setPixel( Graphics g, Color color, int x, int y){
        g.setColor(color);
        g.drawLine(x,y,x,y);
    }

    private void drawTower( Graphics g, Color color, int x, int y, int height){
        g.setColor(color);
        g.drawRect(x,y - height,1,height);
    }
    private float steepness( int x, int y){
        float top = Math.abs(height[y+1][x] - height[y][x]);
        float bot = Math.abs(height[y-1][x] - height[y][x]);
        float left = Math.abs(height[y][x+1] - height[y][x]);
        float right = Math.abs(height[y][x-1] - height[y][x]);
        return Math.max(top, Math.max(bot, Math.max(left, right)));
    }
}
