package models;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

/**
 * Represents a ct head scan viewer and the algorithms that can be carried out on them.
 * @author Josh Codd
 */
public class CTHeadViewer {
    private final double CT_BACKGROUND_COLOUR = 0.043;
    private final double TRANSPARANT = 0;
    private final Volume ctHead;
    private double opacity = 0.12;
    private boolean isGradient = false;

    //TEMPORARY LIGHT SOURCE VARIABLES
    public double light1 = 50;
    public double light2 = 50;
    public double light3 = 50;

    private final int Top_width;
    private final int Top_height;
    private final int Front_width;
    private final int Front_height;
    private final int side_width;
    private final int side_height;


    /**
     * Creates a CT viewer.
     * @param volume The volume to use/display.
     */
    public CTHeadViewer(Volume volume) {
        this.ctHead = volume;
        this.Top_width = volume.getCT_x_axis();
        this.Top_height = volume.getCT_y_axis();

        this.Front_width = volume.getCT_x_axis();
        this.Front_height = volume.getCT_z_axis();

        this.side_width = volume.getCT_x_axis();
        this.side_height = volume.getCT_z_axis();
    }

    /**
     * Draws the specified slice of the CAT scan to screen.
     * @param image The image to write to.
     * @param slice The slice to display.
     * @param view The direction of CAT scan to view. Options are front, side or top.
     */
    public void drawSlice(WritableImage image, int slice, String view) {
        //Get image dimensions, and declare loop variables
        int w=(int) image.getWidth(), h=(int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        double col;
        short datum;
        //Shows how to loop through each pixel and colour
        //Try to always use j for loops in y, and i for loops in x
        for (int j=0; j<h; j++) {
            for (int i=0; i<w; i++) {
                //at this point (i,j) is a single pixel in the image
                //here you would need to do something to (i,j) if the image size
                //does not match the slice size (e.g. during an image resizing operation
                //If you don't do this, your j,i could be outside the array bounds
                //In the framework, the image is 256x256 and the data set slices are 256x256
                //so I don't do anything - this also leaves you something to do for the assignment
                datum = getView(view, i , j, slice);
                //calculate the colour by performing a mapping from [min,max] -> 0 to 1 (float)
                //Java setColor uses float values from 0 to 1 rather than 0-255 bytes for colour
                col=(((float)datum-(float)ctHead.getMin())/((float)(ctHead.getMax()-ctHead.getMin())));
                image_writer.setColor(i, j, Color.color(col,col,col, 1.0));
            } // column loop
        } // row loop
    }

    /**
     *Gets the correct voxel for the direction/view you want to see.
     * @param view The view/direction to display e.g top, side or front
     * @param x The x value to get.
     * @param y The y value to get.
     * @param z The z value to get.
     * @return The correct voxel.
     */
    public short getView(String view, int x, int y, int z){
        if (view.equals("top")){
            return ctHead.getVoxel(z, y, x);
        } else if (view.equals("side")){
            return ctHead.getVoxel(y, x, z);
        } else {
            return ctHead.getVoxel(y, z, x);
        }
    }

    /**
     * Calculates the lighting/shading of a pixel.
     * @param y The y axis location of the pixel.
     * @param x The x axis location of the pixel.
     * @param ray The z or ray depth location of the pixel.
     * @param gradient The vector of the gradient of the data.
     * @return The lighting value for said pixel.
     */
    public double getLighting(int x, int y, int ray, Vector gradient){
        Vector lightDirection = new Vector(light1 - x, light2 - y, light3 - ray);
        lightDirection.normalize();
        gradient.normalize();
        return Math.max(0, gradient.dotProduct(lightDirection));
    }


    /**
     * Calculates the gradient for the current voxel.
     * @param i The x location of voxel.
     * @param j The y location of voxel.
     * @param z The z/ray location of voxel.
     * @param rayLength Maximum length of the ray.
     * @param view The scan direction. i.e top, front or side
     * @return The gradient of the specified voxel
     */
    public Vector calculateGradient(int i, int j, int z, int rayLength, String view){
        Vector x1y1 = new Vector((i - 1), j, 0);
        Vector x2y2= new Vector((i + 1), j, 0);
        int width = view.equals("top") ? getTop_width() : getSide_width();

        if (i > 0 && i < (width - 1)) {
            for (int ray = 0; ray < rayLength; ray++) {
                short currentVoxel = getView(view, (i - 1), j, ray);
                if (currentVoxel >= 300) {
                    x1y1.setC(ray);
                    ray = rayLength;
                }
            }

            for (int ray = 0; ray < rayLength; ray++) {
                short currentVoxel = getView(view, (i + 1), j, ray);
                if (currentVoxel >= 300) {
                    x2y2.setC(ray);
                    ray = rayLength;
                }
            }
        }

        if (x1y1.getC() != 0 && x2y2.getC() != 0) { //central difference
            return new Vector((x2y2.getA() - x1y1.getA()), (x2y2.getB() - x1y1.getB()), (x2y2.getC() - x1y1.getC()));
        } else if (x1y1.getC() == 0){ //forward difference
            return new Vector((x2y2.getA() - i), (x2y2.getB() - j), (x2y2.getC() - z));
        } else { //backwards difference
            return new Vector((i - x1y1.getA()), (j - x1y1.getB()), (z - x1y1.getC()));
        }
    }

    /**
     * Performs volume rendering on the specified image/scan.
     * @param image The image to write to.
     * @param view The direction to view the scan/dataset from. i.e front, side or top.
     */
    public void volumeRender(WritableImage image, String view){
        int w=(int) image.getWidth(), h=(int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        int rayLength = (view.equals("top")) ? ctHead.getCT_z_axis() : ctHead.getCT_x_axis();

        for (int j = 0; j<h; j++) {
            for (int i = 0; i < w; i++) {
                double alphaAccum = 1;
                double redAccum = 0;
                double greenAccum = 0;
                double blueAccum = 0;
                double L = isGradient ? 0 : 1;

                for (int ray = 0; ray < rayLength; ray++) {
                    short currentVoxel = getView(view, i, j, ray);
                    if (currentVoxel >= 300  && isGradient){
                        Vector gradient = calculateGradient(i,j,ray,rayLength,view);
                        L = getLighting(i, j, ray, gradient);
                        ray = rayLength;
                    }

                    //Compositing accumulation.
                    double[] colour = transferFunction(currentVoxel);
                    double sigma = colour[3];
                    redAccum = Math.min(redAccum + (alphaAccum * sigma * L * colour[0]), 1);
                    greenAccum = Math.min(greenAccum + (alphaAccum * sigma * L * colour[1]), 1);
                    blueAccum = Math.min(blueAccum + (alphaAccum * sigma * L * colour[2]), 1);
                    alphaAccum = alphaAccum * (1 - sigma);
                }

                //Composite final black background.
                redAccum = redAccum + (alphaAccum * CT_BACKGROUND_COLOUR * L * TRANSPARANT);
                greenAccum = greenAccum + (alphaAccum * CT_BACKGROUND_COLOUR * L * TRANSPARANT);
                blueAccum = blueAccum + (alphaAccum * CT_BACKGROUND_COLOUR * L * TRANSPARANT);
                alphaAccum = alphaAccum * (TRANSPARANT);

                double opacity = 1 - alphaAccum;
                image_writer.setColor(i, j, Color.color(redAccum, greenAccum, blueAccum, opacity));
            }//column
        }//row
//        System.out.println(light1 + " " + light2 + " " + light3);
    }

    /**
     * The default transfer function for the default dataset. Calculates pixel colour from a
     * voxel.
     * @param voxel The voxel to get RGB value for.
     * @return The RGB and opacity value for the pixel.
     */
    private double[] transferFunction(short voxel){
        double R, G, B, O;
        if ((voxel > -299) && (voxel < 50)){
            R = 1.0;
            G = 0.79;
            B = 0.6;
            O = opacity;
        } else  if (voxel > 300){
            R = 1;
            G = 1;
            B = 1;
            O = 0.8;
        } else {
            R = 0;
            G = 0;
            B = 0;
            O = 0;
        }
        return new double[]{R,G,B,O};
    }

    /**
     * Gets the volume of this viewer.
     * @return The volume.
     */
    public Volume getCtHead() {
        return ctHead;
    }

    /**
     * Sets the opacity of the skin for the transfer function.
     * @param opacity The opacity of the skin.
     */
    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    /**
     * Toggles gradient shading.
     */
    public void toggleGradient() {
        this.isGradient = !isGradient;
        System.out.println(isGradient);
    }

    /**
     * Gets the width of the top image.
     * @return The width of the top image.
     */
    public int getTop_width() {
        return Top_width;
    }

    /**
     * Gets the height of the top image.
     * @return The height of the top image.
     */
    public int getTop_height() {
        return Top_height;
    }

    /**
     * Gets the width of the front image.
     * @return The width of the front image.
     */
    public int getFront_width() {
        return Front_width;
    }

    /**
     * Gets the height of the front image.
     * @return The height of the front image.
     */
    public int getFront_height() {
        return Front_height;
    }

    /**
     * Gets the width of the side image.
     * @return The width of the side image.
     */
    public int getSide_width() {
        return side_width;
    }

    /**
     * Gets the height of the side image.
     * @return The height of the side image.
     */
    public int getSide_height() {
        return side_height;
    }


//CODE FOR POTENTIALLY ROTATING HEAD 45 DEGREES
//    public void drawSlice45(WritableImage image, int slice, String view) {
//        //Get image dimensions, and declare loop variables
//        int w=(int) image.getWidth(), h=(int) image.getHeight();
//        PixelWriter image_writer = image.getPixelWriter();
//
//        double iCenter = w/2;
//        double jCenter = h/2;
//        double kCenter = (iCenter <= jCenter) ? iCenter : jCenter;
//
//        double col;
//        short datum;
//
//        double maxX = Double.MIN_VALUE;
//        double minX = Double.MAX_VALUE;
//
//        double maxZ = Double.MIN_VALUE;
//        double minZ = Double.MAX_VALUE;
//
//        for (int j=0; j<h; j++) {
//            for (int i=0; i<w; i++) {
//
//                int oldX = i;
//                int oldY = j;
//                int oldZ = slice;
//
////                for (int r = 0; r < w; r++){
////                    if (Math.round(i * Math.cos(45) + (r) * Math.sin(45)) == slice) {
////                        double newX = i * Math.cos(45) + (r) * Math.sin(45);
////                        double newz = i * Math.sin(45) + (r) * Math.cos(45);
////                        int z = (int) Math.round(newz);
////                        int x = (int) Math.round(newX);
////
////                        if (x < w && z < w && x > -1 && z > -1) {
////                            datum = getView("side", x, j, z);
////                            col = (((float) datum - (float) ctHead.getMin()) / ((float) (ctHead.getMax() - ctHead.getMin())));
////                            image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
////                        }
////                        //break;
////                    }
////                }
//
//               double newX1 = i * Math.cos(45) + (slice) * Math.sin(45);
//               double newz1 = i * Math.sin(45) + (slice) * Math.cos(45);
//
//                System.out.println(slice + " " + newz1);
//
//                int z = (int) Math.round(newz1);
//                int x = (int) Math.round(newX1);
//
//                if (x > maxX){
//                    maxX = x;
//                }
//                if (z > maxZ){
//                    maxZ = z;
//                }
//                if (x < minX){
//                    minX = x;
//                }
//                if (z < minZ){
//                    minZ = z;
//                }
//
//                if (x < w && z < w && x > -1 && z > -1) {
//                    datum = getView("side", x, j, z);
//                    col = (((float) datum - (float) ctHead.getMin()) / ((float) (ctHead.getMax() - ctHead.getMin())));
//                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
//                }
//            } // column loop
//        } // row loop
//
//        System.out.println(minX + " " + maxX);
//
//        System.out.println(minZ + " " + maxZ);
//    }
}