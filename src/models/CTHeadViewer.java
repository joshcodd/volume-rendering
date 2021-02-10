package models;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import sun.lwawt.macosx.CSystemTray;

/**
 * Represents a ct head scan viewer and the algorithms that can be carried out on them.
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
        int w = (int) image.getWidth(), h = (int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        double col;
        short datum;

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                datum = getVoxel(view, i, j, slice);
                col = (((float) datum - (float) ctHead.getMin()) / ((float) (ctHead.getMax() - ctHead.getMin())));
                image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
            } // column loop
        } // row loop
    }

    /**
     *Gets the correct voxel for the direction/view you want to see.
     * @param view The view/direction to display e.g top, side or front
     * @param i The x value to get.
     * @param j The y value to get.
     * @param k The z value to get.
     * @return The correct voxel.
     */
    public short getVoxel(String view, int i, int j, int k) {
        if (view.equals("top")) {
            return ctHead.getVoxel(k, j, i);
        } else if (view.equals("side")) {
            return ctHead.getVoxel(j, i, k);
        } else {
            return ctHead.getVoxel(j, k, i);
        }
    }

    /**
     * Calculates the lighting/shading of a pixel.
     * @param y The y axis location of the pixel.
     * @param x The x axis location of the pixel.
     * @param ray The z or ray depth location of the pixel.
     * @param surfaceNormal The surface normal of the data.
     * @return The lighting value for said pixel.
     */
    public double getLighting(int x, int y, double ray, Vector surfaceNormal) {
        Vector lightSourcePosition = new Vector(lightSourceX, LIGHT_SOURCE_Y, LIGHT_SOURCE_Z);
        Vector lightDirection = lightSourcePosition.subtract(new Vector(x, y, ray));
        lightDirection.normalize();
        surfaceNormal.normalize();
        return Math.max(0, surfaceNormal.dotProduct(lightDirection));
    }

    /**
     * Calculates the surface normal for the current voxel at integer positions.
     * @param i The x location of voxel.
     * @param j The y location of voxel.
     * @param ray The z/ray location of voxel.
     * @param rayLength Maximum length of the ray.
     * @param view The scan direction. i.e top, front or side
     * @return The surface normal of the specified voxel
     */
    public Vector getSurfaceNormal(int i, int j, int ray, int rayLength, String view, int w, int h) {
        double x, y, z;
        short currentVoxel = getVoxel(view, i, j, ray);
        if (i > 0 && i < (w - 1)) {
            double x1 = getVoxel(view, i - 1, j, ray);
            double x2 = getVoxel(view, i + 1, j, ray);
            x = x2 - x1;
        } else if (i <= 0) {
            double x2 = getVoxel(view, i + 1, j, ray);
            x = x2 - currentVoxel;
        } else {
            double x1 = getVoxel(view, i - 1, j, ray);
            x = currentVoxel - x1;
        }

        if (j > 0 && j < (h - 1)) {
            double y1 = getVoxel(view, i, j - 1, ray);
            double y2 = getVoxel(view, i, j + 1, ray);
            y = y2 - y1;
        } else if (j <= 0) {
            double y2 = getVoxel(view, i, j + 1, ray);
            y = y2 - currentVoxel;
        } else {
            double y1 = getVoxel(view, i, j - 1, ray);
            y = currentVoxel - y1;
        }

        if (ray > 0 && ray < (rayLength - 1)) {
            double z1 = getVoxel(view, i, j, ray - 1);
            double z2 = getVoxel(view, i, j, ray + 1);
            z = z2 - z1;
        } else if (ray <= 0) {
            double z2 = getVoxel(view, i, j, ray + 1);
            z = z2 - currentVoxel;
        } else {
            double z1 = getVoxel(view, i, j, ray - 1);
            z = currentVoxel - z1;
        }
        return new Vector(x, y, z);
    }

    /**
     * Calculates the surface normal for the current voxel of a non integer position.
     * @param i The x location of voxel.
     * @param j The y location of voxel.
     * @param ray The exact z/ray location of voxel.
     * @param rayLength Maximum length of the ray.
     * @param view The scan direction. i.e top, front or side
     * @return The surface normal of the specified voxel
     */
    public Vector getSurfaceNormal(int i, int j, double ray, int rayLength, String view, int w, int h) {
        double x, y, z;
        double currentVoxel = getRealVoxel(view, i, j, ray);

        if (i > 0 && i < (w - 1)) {
            double x1 = getRealVoxel(view, i - 1, j, ray);
            double x2 = getRealVoxel(view, i + 1, j, ray);
            x = x2 - x1;
        } else if (i <= 0) {
            double x2 = getRealVoxel(view, i + 1, j, ray);
            x = x2 - currentVoxel;
        } else {
            double x1 = getRealVoxel(view, i - 1, j, ray);
            x = currentVoxel - x1;
        }

        if (j > 0 && j < (h - 1)) {
            double y1 = getRealVoxel(view, i, j - 1, ray);
            double y2 = getRealVoxel(view, i, j + 1, ray);
            y = y2 - y1;
        } else if (j <= 0) {
            double y2 = getRealVoxel(view, i, j + 1, ray);
            y = y2 -currentVoxel;
        } else {
            double y1 = getRealVoxel(view, i, j - 1, ray);
            y = currentVoxel - y1;
        }

        if (ray >= 1 && ray < (rayLength - 2)) {
            double z1 = getRealVoxel(view, i, j, ray - 1.0);
            double z2 = getRealVoxel(view, i, j, ray + 1.0);
            z = z2 - z1;
        } else if (ray <= 1) {
            double z2 = getRealVoxel(view, i, j, ray + 1.0);
            z = z2 - currentVoxel;
        } else {
            double z1 = getRealVoxel(view, i, j, ray - 1.0);
            z = currentVoxel - z1;
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
    public double getRealVoxel(String view, int x, int y, double z) {
        int z1 = (int) Math.floor(z);
        int z2 = (int) Math.ceil(z);
        short v1 = getVoxel(view, x, y, z1);
        short v2 = getVoxel(view, x, y, z2);
        return linearInterpolationVoxel(v1, v2, z1, z, z2);
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
    public double linearInterpolationVoxel(double v1, double v2, double x1, double x, int x2) {
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

                for (int ray = 0; ray < rayLength && !hitBone; ray++) {
                    short currentVoxel = getVoxel(view, i, j, ray);
                    if (currentVoxel >= 400 && isGradient) {
                        Vector surfaceNormal;
                        if (isGradientInterpolation) {
                            double actualIntersection;
                            int prevRay = (ray > 0) ? (ray - 1) : ray;
                            short prevVoxel = getVoxel(view, i, j, prevRay);

                            if (currentVoxel == 400){
                                surfaceNormal = getSurfaceNormal(i, j, ray, rayLength, view, w, h);
                                L = getLighting(i, j, ray, surfaceNormal);
                            } else {
                                actualIntersection =
                                        linearInterpolationPosition(400, prevVoxel, currentVoxel, prevRay, ray);
                                surfaceNormal = getSurfaceNormal(i, j, actualIntersection, rayLength, view, w, h);
                                L = getLighting(i, j, actualIntersection, surfaceNormal);
                            }

                        } else {
                            surfaceNormal = getSurfaceNormal(i, j, ray, rayLength, view, w, h);
                            L = getLighting(i, j, ray, surfaceNormal) ;
                        }
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
                image_writer.setColor(i, j, Color.color(redAccum, greenAccum, blueAccum, 1));
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
}