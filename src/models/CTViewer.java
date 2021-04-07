package models;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.util.stream.IntStream;

/**
 * Represents a ct scan viewer and the algorithms that can be carried out on them.
 * @author Josh Codd
 */
public class CTViewer {
    private final int TOP_WIDTH;
    private final int TOP_HEIGHT;
    private final int FRONT_WIDTH;
    private final int FRONT_HEIGHT;
    private final int SIDE_WIDTH;
    private final int SIDE_HEIGHT;
    private final double BONE_VALUE = 400;
    private final Volume ctScan;
    private double opacity = 0.12;
    private boolean isGradient = false;
    private boolean isGradientInterpolation = false;
    private double lightSourceX = 83;

    /**
     * Creates a CT viewer.
     * @param volume The volume to use/display.
     */
    public CTViewer(Volume volume) {
        this.ctScan = volume;
        this.TOP_WIDTH = volume.getCT_x_axis();
        this.TOP_HEIGHT = volume.getCT_y_axis();
        this.FRONT_WIDTH = volume.getCT_x_axis();
        this.FRONT_HEIGHT = volume.getCT_z_axis();
        this.SIDE_WIDTH = volume.getCT_x_axis();
        this.SIDE_HEIGHT = volume.getCT_z_axis();
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
            return ctScan.getVoxel(z, y, x);
        } else if (view.equals("side")) {
            return ctScan.getVoxel(y, x, z);
        } else {
            return ctScan.getVoxel(y, z, x);
        }
    }

    /**
     * Draws the specified slice of the CAT scan to screen.
     * @param image The image to write to.
     * @param view The direction of CAT scan to view. Options are front, side or top.
     * @param slice The slice to display.
     */
    public void drawSlice(WritableImage image, String view, int slice) {
        int width = (int) image.getWidth(), height = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        double colour;
        short voxel;

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                voxel = getVoxel(view, i, j, slice);
                colour = (((float) voxel - (float) ctScan.getMin()) / ((float) (ctScan.getMax() - ctScan.getMin())));
                colour = Math.max(colour, 0);
                image_writer.setColor(i, j, Color.color(colour, colour, colour, 1.0));
            } // column loop
        } // row loop
    }

    /**
     * Performs maximum intensity projection on the specified image/scan.
     * @param image The image to write to.
     * @param view The direction to view the scan/dataset from. i.e front, side or top.
     */
    public void maximumIntensityProjection(WritableImage image, String view) {
        PixelWriter image_writer = image.getPixelWriter();
        int width = (int) image.getWidth(), height = (int) image.getHeight();
        int depth = (view.equals("top")) ? ctScan.getCT_z_axis() : ctScan.getCT_x_axis();

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                short maximum = ctScan.getMin();
                for (int k = 0; k < depth; k++) {
                    short currentVoxel = getVoxel(view, i, j, k);
                    if (currentVoxel > maximum){
                        maximum = currentVoxel;
                    }
                }
                float colour = (((float) maximum - (float) ctScan.getMin()) / ((float) (ctScan.getMax() - ctScan.getMin())));
                image_writer.setColor(i, j, Color.color(colour, colour, colour, 1.0));
            }//column
        }//row
    }

    /**
     * Performs volume rendering on the specified image/scan.
     * @param image The image to write to.
     * @param view The direction to view the scan/dataset from. i.e front, side or top.
     */
    public WritableImage volumeRender(WritableImage image, String view, String transferFunction) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage image1 = new WritableImage(width, height);
        PixelWriter writer = image1.getPixelWriter();
        int depth = (view.equals("top")) ? ctScan.getCT_z_axis() : ctScan.getCT_x_axis();

        IntStream.range(0, height).parallel().forEach(j -> {
            IntStream.range(0, width).parallel().forEach(i -> {
                double alphaAccum = 1;
                double redAccum = 0;
                double greenAccum = 0;
                double blueAccum = 0;
                boolean hitBone = false;
                double L = 1;

                for (int k = 0; k < depth && !hitBone; k++) {
                    short currentVoxel = getVoxel(view, i, j, k);
                    if (currentVoxel >= BONE_VALUE && isGradient) {
                        L = getDiffuseLighting(view, i, j, k, currentVoxel, height, width, depth);
                        hitBone = true;
                    }

                    if (!isGradient || hitBone) {
                        final int RED = 0;
                        final int GREEN = 1;
                        final int BLUE = 2;
                        double[] colour = getTransferFunction(currentVoxel, transferFunction);
                        double sigma = colour[3];
                        redAccum = Math.min(Math.max(redAccum + (alphaAccum * sigma * L * colour[RED]), 0), 1);
                        greenAccum = Math.min(Math.max(greenAccum + (alphaAccum * sigma * L * colour[GREEN]),0), 1);
                        blueAccum = Math.min(Math.max(blueAccum + (alphaAccum * sigma * L * colour[BLUE]),0), 1);
                        alphaAccum = alphaAccum * (1 - sigma);
                    }
                }
                writer.setColor(i, j, Color.color(redAccum, greenAccum, blueAccum, 1));
            });
        });
        return image1;
    }

    /**
     * Calculates the diffuse lighting/shading of a pixel.
     * @param x The x axis location of the pixel.
     * @param y The y axis location of the pixel.
     * @param z The z or ray depth location of the pixel.
     * @param currentVoxel The voxel at the current position.
     * @param height The height of the image.
     * @param width The width of the image.
     * @param depth Maximum depth of the ray.
     * @return The lighting value for the specified pixel.
     */
    public double getDiffuseLighting(String view, int x, int y, int z, int currentVoxel, int height, int width, int depth) {
        Vector surfaceNormal = getSurfaceNormal(view, x, y, z, height, width, depth);
        Vector intersection = new Vector(x, y, z);

        if (isGradientInterpolation && currentVoxel != BONE_VALUE && z > 0) {
            int prevRay = z - 1;
            short prevVoxel = getVoxel(view, x, y, prevRay);
            double exactZ =
                    linearInterpolationPosition(BONE_VALUE, prevVoxel, currentVoxel, prevRay, z);
            surfaceNormal = getSurfaceNormal(view, x, y, exactZ, height, width, depth);
            intersection.setC(exactZ);
        }

        final double LIGHT_SOURCE_Y = (double) ctScan.getCT_z_axis() / 4;
        final double LIGHT_SOURCE_Z = ctScan.getCT_x_axis();
        Vector lightSourcePosition = new Vector(lightSourceX, LIGHT_SOURCE_Y, LIGHT_SOURCE_Z);
        Vector lightDirection = lightSourcePosition.subtract(intersection);
        lightDirection.normalize();
        surfaceNormal.normalize();
        return Math.max(0, surfaceNormal.dotProduct(lightDirection));
    }

    /**
     * Calculates and returns the an estimate of the gradient/slope at the specified position, in which the z
     * axis is an integer. This calculation uses both central, forward and backward differance.
     * @param view The direction to view the scan/dataset from. i.e front, side or top.
     * @param current The voxel at the position to find the slope for.
     * @param x1 The x axis position of a voxel before the current.
     * @param y1 The y axis position of a voxel before the current.
     * @param z1 The integer z axis position of a voxel before the current.
     * @param x2 The x axis position of a voxel after the current.
     * @param y2 The y axis position of a voxel after the current.
     * @param z2 The integer z axis position of a voxel after the current.
     * @param min The minimum value a altered axis could be. (Normally 0).
     * @param max The maximum value a altered axis could be. (Normally axis length - 1)
     * @param i The axis you are altering.
     * @return The gradient calculated for the specified position.
     */
    public double getGradient(String view, double current, int x1, int y1, int z1, int x2,
                              int y2, int z2, int min, int max, int i) {
        if (i > min && i < (max - 1)) {
            double prev = getVoxel(view, x1, y1, z1);
            double next = getVoxel(view, x2, y2, z2);
            return next - prev;
        } else if (i <= min) {
            double next = getVoxel(view, x2, y2, z2);
            return next - current;
        } else {
            double prev = getVoxel(view, x1, y1, z1);
            return current - prev;
        }
    }

    /**
     * Calculates and returns the an estimate of the gradient/slope at the specified position, in which the z
     * axis is a non integer position. This calculation uses both central, forward and backward differance.
     * @param view The direction to view the scan/dataset from. i.e front, side or top.
     * @param current The voxel at the position to find the slope for.
     * @param x1 The x axis position of a voxel before the current.
     * @param y1 The y axis position of a voxel before the current.
     * @param z1 The non-integer z axis position of a voxel before the current.
     * @param x2 The x axis position of a voxel after the current.
     * @param y2 The y axis position of a voxel after the current.
     * @param z2 The non-integer z axis position of a voxel after the current.
     * @param min The minimum value a altered axis could be. (Normally 0).
     * @param max The maximum value a altered axis could be. (Normally axis length - 1)
     * @param i The axis you are altering.
     * @return The gradient calculated for the specified position.
     */
    public double getGradient(String view, double current, int x1, int y1, double z1, int x2,
                              int y2, double z2, int min, int max, double i) {
        if (i > min && i < (max - 1)) {
            double prev = getRealVoxel(view, x1, y1, z1);
            double next = getRealVoxel(view, x2, y2, z2);
            return next - prev;
        } else if (i <= min) {
            double next = getRealVoxel(view, x2, y2, z2);
            return next - current;
        } else {
            double prev = getRealVoxel(view, x1, y1, z1);
            return current - prev;
        }
    }

    /**
     * Calculates the surface normal for the current voxel at all integer positions.
     * @param view The scan direction. i.e top, front or side
     * @param x The x location of voxel.
     * @param y The y location of voxel.
     * @param z The z/ray location of voxel.
     * @param height The height of the image.
     * @param width The width of the image.
     * @param depth Maximum depth of the ray.
     * @return The surface normal of the specified voxel
     */
    public Vector getSurfaceNormal(String view, int x, int y, int z, int height, int width, int depth) {
        double xGradient, yGradient, zGradient;
        short currentVoxel = getVoxel(view, x, y, z);
        xGradient = getGradient(view, currentVoxel, x-1, y, z, x+1, y, z,0,(width-1),x);
        yGradient = getGradient(view, currentVoxel, x, y-1, z, x, y+1, z,0,height-1,y);
        zGradient = getGradient(view, currentVoxel, x, y, z-1, x, y, z+1,0,depth-1, z);
        return new Vector(xGradient, yGradient, zGradient);
    }

    /**
     * Calculates the surface normal for the current voxel of a non integer position z.
     * @param view The scan direction. i.e top, front or side
     * @param x The x location of voxel.
     * @param y The y location of voxel.
     * @param z The exact z/ray location of voxel.
     * @param height The height of the image.
     * @param width The width of the image.
     * @param depth Maximum depth of the ray.
     * @return The surface normal of the specified voxel
     */
    public Vector getSurfaceNormal(String view, int x, int y, double z, int height, int width, int depth) {
        double xGradient, yGradient, zGradient;
        double currentVoxel = getRealVoxel(view, x, y, z);
        xGradient = getGradient(view, currentVoxel,x-1, y, z, x+1, y, z, 0, width-1, x);
        yGradient = getGradient(view, currentVoxel, x, y-1, z, x,y+1, z,0,height-1, y);
        zGradient = getGradient(view, currentVoxel, x, y, z-1, x, y,z+1,1,depth-2, z);
        return new Vector(xGradient, yGradient, zGradient);
    }

    /**
     * Gets the voxel at a non integer position z.
     * @param view The direction viewing the ct image from.
     * @param x The x value to get.
     * @param y The y value to get.
     * @param z The non integer z value to get.
     * @return Voxel at non-integer position.
     */
    public double getRealVoxel(String view, int x, int y, double z) {
        int z1 = (int) Math.floor(z);
        int z2 = (int) Math.ceil(z);
        short v1 = getVoxel(view, x, y, z1);
        short v2 = getVoxel(view, x, y, z2);
        return linearInterpolationVoxel(v1, v2, z1, z, z2);
    }

    /**
     * Calculates the voxel at a non-integer position along a single axis.
     * @param v1 The voxel at the previous integer position.
     * @param v2 The voxel at the following integer position.
     * @param x1 The integer position before.
     * @param x The non-integer position to find.
     * @param x2 The integer position after.
     * @return The voxel value at non integer position X.
     */
    public double linearInterpolationVoxel(double v1, double v2, double x1, double x, double x2) {
        return (v1 + (v2 - v1) * ((x - x1)/(x2 - x1)));
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
    public double linearInterpolationPosition(double v, double v1, double v2, int x1, int x2) {
        return x1 + (x2 - x1) * ((v - v1)/(v2 - v1));
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
     * The second transfer function for the visible human dataset. Calculates pixel colour from a
     * voxel.
     * @param voxel The voxel to get RGB value for.
     * @return The RGB and opacity value for the pixel.
     */
    private double[] transferFunctionTwo(short voxel) {
        double R, G, B, O;
        if ((voxel > -299) && (voxel < 50)) {
            R = 1.0;
            G = 0.79;
            B = 0.6;
            O = opacity;
        } else {
            R = 0;
            G = 0;
            B = 0;
            O = 0;
        }
        return new double[]{R, G, B, O};
    }

    /**
     * Handles which transfer function to use based on the name passed in.
     * @param voxel The voxel to use in the transfer function.
     * @param tfName The name of the transfer function to use.
     * @return The result from specified TF.
     */
    private double[] getTransferFunction(short voxel, String tfName){
        if (tfName.equals("TF1")){
            return transferFunction(voxel);
        } else {
            return transferFunctionTwo(voxel);
        }
    }

    /**
     * Gets the volume of this viewer.
     * @return The volume.
     */
    public Volume getCtScan() {
        return ctScan;
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
}
