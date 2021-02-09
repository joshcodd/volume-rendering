package models;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Represents a ct head scan viewer and the algorithms that can be carried out on them.
 *
 * @author Josh Codd
 */
public class CTHeadViewer {
    private final int TOP_WIDTH;
    private final int TOP_HEIGHT;
    private final int FRONT_WIDTH;
    private final int FRONT_HEIGHT;
    private final int SIDE_WIDTH;
    private final int SIDE_HEIGHT;
    private final double LIGHT_SOURCE_Y = 20;
    private final double LIGHT_SOURCE_Z = 256;
    private final Volume ctHead;

    private double opacity = 0.12;
    private boolean isGradient = false;
    private boolean isGradientInterpolation = false;
    private double lightSourceX = 83;

    /**
     * Creates a CT viewer.
     * @param volume The volume to use/display.
     */
    public CTHeadViewer(Volume volume) {
        this.ctHead = volume;
        this.TOP_WIDTH = volume.getCT_x_axis();
        this.TOP_HEIGHT = volume.getCT_y_axis();
        this.FRONT_WIDTH = volume.getCT_x_axis();
        this.FRONT_HEIGHT = volume.getCT_z_axis();
        this.SIDE_WIDTH = volume.getCT_x_axis();
        this.SIDE_HEIGHT = volume.getCT_z_axis();
    }

    /**
     * Draws the specified slice of the CAT scan to screen.
     * @param image The image to write to.
     * @param slice The slice to display.
     * @param view The direction of CAT scan to view. Options are front, side or top.
     */
    public void drawSlice(WritableImage image, int slice, String view) {
        //Get image dimensions, and declare loop variables
        int w = (int) image.getWidth(), h = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        double col;
        short datum;

        //Try to always use j for loops in y, and i for loops in x
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                //at this point (i,j) is a single pixel in the image
                //here you would need to do something to (i,j) if the image size
                //does not match the slice size (e.g. during an image resizing operation
                //If you don't do this, your j,i could be outside the array bounds
                //In the framework, the image is 256x256 and the data set slices are 256x256
                //so I don't do anything - this also leaves you something to do for the assignment
                datum = getVoxel(view, i, j, slice);
                //calculate the colour by performing a mapping from [min,max] -> 0 to 1 (float)
                //Java setColor uses float values from 0 to 1 rather than 0-255 bytes for colour
                col = (((float) datum - (float) ctHead.getMin()) / ((float) (ctHead.getMax() - ctHead.getMin())));
                image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
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
    public short getVoxel(String view, int x, int y, int z) {
        if (view.equals("top")) {
            return ctHead.getVoxel(z, y, x);
        } else if (view.equals("side")) {
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
    public double getLighting(int x, int y, int ray, Vector gradient) {
        Vector lightSourcePosition = new Vector(lightSourceX, LIGHT_SOURCE_Y, LIGHT_SOURCE_Z);
        Vector lightDirection = lightSourcePosition.subtract(new Vector(x, y, ray));
        lightDirection.normalize();
        gradient.normalize();
        return Math.max(0, gradient.dotProduct(lightDirection));
    }

    /**
     * Calculates the gradient for the current voxel at integer positions.
     * @param i The x location of voxel.
     * @param j The y location of voxel.
     * @param ray The z/ray location of voxel.
     * @param rayLength Maximum length of the ray.
     * @param view The scan direction. i.e top, front or side
     * @return The gradient of the specified voxel
     */
    public Vector calculateGradient(int i, int j, int ray, int rayLength, String view, int w, int h) {
        double x;
        double y;
        double z;

        if (i > 0 && i < (w - 1)) {
            double x1 = getVoxel(view, i - 1, j, ray);
            double x2 = getVoxel(view, i + 1, j, ray);
            x = x2 - x1;
        } else if (i <= 0) {
            double x2 = getVoxel(view, i + 1, j, ray);
            x = x2 - getVoxel(view, i, j, ray);
        } else {
            double x1 = getVoxel(view, i - 1, j, ray);
            x = getVoxel(view, i, j, ray) - x1;
        }

        if (j > 0 && j < (h - 1)) {
            double y1 = getVoxel(view, i, j - 1, ray);
            double y2 = getVoxel(view, i, j + 1, ray);
            y = y2 - y1;
        } else if (j <= 0) {
            double y2 = getVoxel(view, i, j + 1, ray);
            y = y2 - getVoxel(view, i, j, ray);
        } else {
            double y1 = getVoxel(view, i, j - 1, ray);
            y = getVoxel(view, i, j, ray) - y1;
        }

        if (ray > 0 && ray < (rayLength - 1)) {
            double z1 = getVoxel(view, i, j, ray - 1);
            double z2 = getVoxel(view, i, j, ray + 1);
            z = z2 - z1;
        } else if (ray <= 0) {
            double z2 = getVoxel(view, i, j, ray + 1);
            z = z2 - getVoxel(view, i, j, ray);
        } else {
            double z1 = getVoxel(view, i, j, ray - 1);
            z = getVoxel(view, i, j, ray) - z1;
        }
        return new Vector(x, y, z);
    }

    /**
     * Calculates the gradient for the current voxel of a non integer position.
     * @param i The x location of voxel.
     * @param j The y location of voxel.
     * @param ray The exact z/ray location of voxel.
     * @param rayLength Maximum length of the ray.
     * @param view The scan direction. i.e top, front or side
     * @return The gradient of the specified voxel
     */
    public Vector calculateGradient(int i, int j, double ray, int rayLength, String view, int w, int h) {
        double x;
        double y;
        double z;

        if (i > 0 && i < (w - 1)) {
            double x1 = getVoxelInterpolation(view, i - 1, j, ray);
            double x2 = getVoxelInterpolation(view, i + 1, j, ray);
            x = x2 - x1;
        } else if (i <= 0) {
            double x2 = getVoxelInterpolation(view, i + 1, j, ray);
            x = x2 - getVoxelInterpolation(view, i, j, ray);
        } else {
            double x1 = getVoxelInterpolation(view, i - 1, j, ray);
            x = getVoxelInterpolation(view, i, j, ray) - x1;
        }

        if (j > 0 && j < (h - 1)) {
            double y1 = getVoxelInterpolation(view, i, j - 1, ray);
            double y2 = getVoxelInterpolation(view, i, j + 1, ray);
            y = y2 - y1;
        } else if (j <= 0) {
            double y2 = getVoxelInterpolation(view, i, j + 1, ray);
            y = y2 - getVoxelInterpolation(view, i, j, ray);
        } else {
            double y1 = getVoxelInterpolation(view, i, j - 1, ray);
            y = getVoxelInterpolation(view, i, j, ray) - y1;
        }

        if (ray > 0 && ray < (rayLength - 1)) {
            double z1 = getVoxelInterpolation(view, i, j, ray - 1);
            double z2 = getVoxelInterpolation(view, i, j, ray + 1);
            z = z2 - z1;
        } else if (ray <= 0) {
            double z2 = getVoxelInterpolation(view, i, j, ray + 1);
            z = z2 - getVoxelInterpolation(view, i, j, ray);
        } else {
            double z1 = getVoxelInterpolation(view, i, j, ray - 1);
            z = getVoxelInterpolation(view, i, j, ray) - z1;
        }
        return new Vector(x, y, z);
    }

    /**
     * Gets the voxel at a non integer position.
     * @param view The direction viewing the ct image from.
     * @param x The x value to get.
     * @param y The y value to get.
     * @param z The non integer z value to get.
     * @return Voxel at non-integer position.
     */
    public double getVoxelInterpolation(String view, int x, int y, double z) {
        int z1 = (int) Math.floor(z);
        int z2 = (int) Math.ceil(z);
        short v1 = getVoxel(view, x, y, z1);
        short v2 = getVoxel(view, x, y, z2);
        return linearInterpolationColour(v1, v2, z1, z, z2);
    }

    /**
     * Calculates the voxel at a non-integer position.
     * @param v1 The voxel at the previous integer position.
     * @param v2 The voxel at the following integer position.
     * @param x1 The integer position before.
     * @param x The non-integer position to find.
     * @param x2 The integer position after.
     * @return The voxel value at non integer position X.
     */
    public double linearInterpolationColour(Short v1, Short v2, int x1, double x, int x2) {
        double dividend = x - x1;
        double divisor = x2 - x1;
        double quotient = dividend / divisor;
        return (v1 + (v2 - v1) * quotient);
    }

    /**
     * Calculates the non-integer position of a specified value within two integer positions.
     * @param v The value to get the position of.
     * @param v1 The value at the previous integer position.
     * @param v2 The value at the following integer position.
     * @param x1 The previous integer position.
     * @param x2 The following integer position.
     * @return The non integer position of the specified value.
     */
    public double linearInterpolationPosition(int v, int v1, int v2, int x1, int x2) {
        double dividend = v - v1;
        double divisor = v2 - v1;
        double quotient = dividend / divisor;
        return x1 + (x2 - x1) * quotient;
    }

    /**
     * Performs volume rendering on the specified image/scan.
     * @param image The image to write to.
     * @param view The direction to view the scan/dataset from. i.e front, side or top.
     */
    public void volumeRender(WritableImage image, String view) {
        int w = (int) image.getWidth(), h = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        int rayLength = (view.equals("top")) ? ctHead.getCT_z_axis() : ctHead.getCT_x_axis();

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                double alphaAccum = 1;
                double redAccum = 0;
                double greenAccum = 0;
                double blueAccum = 0;
                boolean hitBone = false;
                double L = 1;

                for (int ray = 0; ray < (rayLength - 1) && !hitBone; ray++) {
                    short currentVoxel = getVoxel(view, i, j, ray);
                    if (currentVoxel > 400 && isGradient) {
                        Vector gradient;
                        if (isGradientInterpolation) {
                            double actualIntersection;
                            int prevRay = (ray > 0) ? (ray - 1) : ray;
                            short prevVoxel = getVoxel(view, i, j, prevRay);
                            actualIntersection =
                                    linearInterpolationPosition(400, prevVoxel, currentVoxel, prevRay, ray);
                            gradient = calculateGradient(i, j, actualIntersection, rayLength, view, w, h);
                        } else {
                            gradient = calculateGradient(i, j, ray, rayLength, view, w, h);
                        }

                        L = getLighting(i, j, ray, gradient);
                        hitBone = true;
                    }

                    if (!isGradient || hitBone) {
                        double[] colour = transferFunction(currentVoxel);
                        double sigma = colour[3];
                        redAccum = Math.min(redAccum + (alphaAccum * sigma * L * colour[0]), 1);
                        greenAccum = Math.min(greenAccum + (alphaAccum * sigma * L * colour[1]), 1);
                        blueAccum = Math.min(blueAccum + (alphaAccum * sigma * L * colour[2]), 1);
                        alphaAccum = alphaAccum * (1 - sigma);
                    }
                }

                image_writer.setColor(i, j, Color.color(redAccum, greenAccum, blueAccum, 1.0));
            }//column
        }//row
    }

    /**
     * The default transfer function for the default dataset. Calculates pixel colour from a
     * voxel.
     * @param voxel The voxel to get RGB value for.
     * @return The RGB and opacity value for the pixel.
     */
    private double[] transferFunction(short voxel) {
        double R, G, B, O;
        if ((voxel > -299) && (voxel < 50)) {
            R = 1.0;
            G = 0.79;
            B = 0.6;
            O = opacity;
        } else if (voxel > 300) {
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
        return new double[]{R, G, B, O};
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
     * Sets if gradient shading should be used.
     * @param isGradient If gradient shading should be used.
     */
    public void setGradientShading(boolean isGradient) {
        this.isGradient = isGradient;
    }

    /**
     * Gets if gradient shading should be used.
     * @return If gradient shading should be used.
     */
    public boolean getGradientShading() {
        return this.isGradient;
    }

    /**
     * Sets if gradient shading interpolation should be used.
     * @param isGradientInterpolation If interpolation should be used.
     */
    public void setGradientInterpolation(boolean isGradientInterpolation) {
        this.isGradientInterpolation = isGradientInterpolation;
    }

    /**
     * Gets if gradient shading should be used.
     * @return If gradient shading should be used.
     */
    public boolean getGradientInterpolation() {
        return this.isGradientInterpolation;
    }

    /**
     * Gets the width of the top image.
     * @return The width of the top image.
     */
    public int getTop_width() {
        return TOP_WIDTH;
    }

    /**
     * Gets the height of the top image.
     * @return The height of the top image.
     */
    public int getTop_height() {
        return TOP_HEIGHT;
    }

    /**
     * Gets the width of the front image.
     * @return The width of the front image.
     */
    public int getFront_width() {
        return FRONT_WIDTH;
    }

    /**
     * Gets the height of the front image.
     * @return The height of the front image.
     */
    public int getFront_height() {
        return FRONT_HEIGHT;
    }

    /**
     * Gets the width of the side image.
     * @return The width of the side image.
     */
    public int getSide_width() {
        return SIDE_WIDTH;
    }

    /**
     * Gets the height of the side image.
     * @return The height of the side image.
     */
    public int getSide_height() {
        return SIDE_HEIGHT;
    }

    /**
     * Sets the location of the light source along the X axis.
     * @param position The location along the X axis.
     */
    public void setLightSourceX(int position) {
        this.lightSourceX = position;
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