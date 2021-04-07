package models;
import java.lang.*;
import java.io.*;

/**
 * Represents a volume. That is, a 3D data set.
 * @author Josh Codd.
 */
public class Volume {
    private short[][][] volume;
    private short min, max;
    private final int CT_x_axis;
    private final int CT_y_axis;
    private final int CT_z_axis;

    /**
     * Creates a volume of specified size.
     * @param x The length of the x axis.
     * @param y The length of the y axis.
     * @param z The length of the z axis.
     */
    public Volume(int x, int y, int z) {
        this.CT_x_axis = x;
        this.CT_y_axis = y;
        this.CT_z_axis = z;
    }

    /**
     * Populates the volume with data from file.
     * @param filename The name of the file to read from.
     * @param isCorrectEndian If the file is in the correct endian or not.
     * @param isVH If the volume is the VH project and therefore needs re-sampling.
     * @throws IOException If file ends prematurely/wrong size volume.
     */
    public void ReadData(String filename, boolean isCorrectEndian, boolean isVH) throws IOException {
        File file = new File(filename);
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        min=Short.MAX_VALUE; max=Short.MIN_VALUE;
        short read;
        int b1, b2;
        volume = new short[CT_z_axis][CT_y_axis][CT_x_axis];

        for (int k = 0; k < CT_z_axis; k++) {
            for (int j = 0; j < CT_y_axis; j++) {
                for (int i = 0; i < CT_x_axis; i++) {
                    b1 = ((int) in.readByte()) & 0xff;
                    b2 = ((int) in.readByte()) & 0xff;

                    //swap bytes
                    if (!isCorrectEndian) {
                        read = (short) ((b2 << 8) | b1);
                    } else {
                        read = (short) (b1 << 8 | b2);
                    }

                    if (read < min) min = read;
                    if (read > max) max = read;
                    volume[k][j][i] = read;
                }
            }
        }

        if (isVH) { //Re-sample if user selected file is visible human project.
            resampleVisibleHuman();
        }
    }

    /**
     * Get the voxel at specified position in volume.
     * @param x The x location.
     * @param y The y location.
     * @param z The z location.
     * @return The voxel at that location.
     */
    public short getVoxel(int x, int y, int z) {
        return volume[x][y][z];
    }

    /**
     * Gets the minimum value in the volume.
     * @return The minimum value.
     */
    public short getMin() {
        return min;
    }

    /**
     * Gets the maximum value in the volume.
     * @return The maximum value.
     */
    public short getMax() {
        return max;
    }

    /**
     * Gets the length of the volumes X axis.
     * @return The volumes X axis length.
     */
    public int getCT_x_axis() {
        return CT_x_axis;
    }

    /**
     * Gets the length of the volumes Y axis.
     * @return The volumes Y axis length.
     */
    public int getCT_y_axis() {
        return CT_y_axis;
    }

    /**
     * Gets the length of the volumes Z axis.
     * @return The volumes Z axis length.
     */
    public int getCT_z_axis() {
        return CT_z_axis;
    }

    /**
     * Re-samples and therefore fixes the female visible human data set.
     * Due to the cadaver being moved around, there are some issues with the dataset. This
     * Fixes those issues by re-sampling.
     */
    private void resampleVisibleHuman(){
        for (int j = 0; j < 209; j++) {
            short[][] temp = resizeMatrix(volume[j], CT_x_axis, CT_y_axis, (int)(CT_x_axis /1.9),
                    (int)(CT_y_axis/1.9));
            volume[j] = centreContent(temp, CT_x_axis, CT_y_axis);
        }

        for (int j = 209; j < 227; j++) {
            short[][] temp = resizeMatrix(volume[j], CT_x_axis, CT_y_axis, (int)(CT_x_axis /1.3),
                    (int)(CT_y_axis/1.3));
            volume[j] = centreContent(temp, CT_x_axis, CT_y_axis);
        }

        for (int j = 227; j < 249; j++) {
            short[][] temp = resizeMatrix(volume[j], CT_x_axis, CT_y_axis, (int)(CT_x_axis /1.1),
                    (int)(CT_y_axis/1.1));
            volume[j] = centreContent(temp, CT_x_axis, CT_y_axis);
        }

        if (CT_z_axis > 1117) {
            for (int j = 1106; j < 1110; j++) {
                short[][] temp = resizeMatrix(volume[j], CT_x_axis, CT_y_axis, (int) (CT_x_axis / 1.3),
                        (int) (CT_y_axis / 1.3));
                volume[j] = centreContent(temp, CT_x_axis, CT_y_axis);
            }

            for (int j = 1117; j < CT_z_axis; j++) {
                short[][] temp = resizeMatrix(volume[j], CT_x_axis, CT_y_axis, (int) (CT_x_axis / 1.3),
                        (int) (CT_y_axis / 1.3));
                volume[j] = centreContent(temp, CT_x_axis, CT_y_axis);
            }
        }
    }

    /**
     * Resizes a matrix to a specified size.
     * Uses nearest neighbor.
     * @param matrix The matrix to resize.
     * @param width The width of the matrix to resize.
     * @param height The height of the matrix to resize.
     * @param resizeW The width to resize to.
     * @param resizeH The height to resize to.
     * @return Th matrix of the new size.
     */
    private short[][] resizeMatrix(short[][] matrix, int width, int height, int resizeW, int resizeH) {
        short[][] resized = createMatrix(resizeW, resizeH);
        for (int x = 0; x < resizeW; x++) {
            for (int y = 0; y < resizeH; y++) {
                int nearestX = (int) (x * (float) width  / resizeW);
                int nearestY = (int) (y * (float) height / resizeH);
                resized[x][y] = matrix[nearestX][nearestY];
            }
        }
        return resized;
    }

    /**
     * Centres a matrix within a larger container matrix of specified size.
     * @param content The smaller matrix to centre.
     * @param containerWidth The width of the container.
     * @param containerHeight The height of the container.
     * @return The new matrix.
     */
    private short[][] centreContent(short[][] content, int containerWidth, int containerHeight) {
        short[][] output = createMatrix(containerWidth, containerHeight);
        int length = content.length;
        int centreBegins = ((getCT_x_axis() - content.length) / 2);
        int xCentre = 0;
        for ( int x = 0; x < containerWidth; x++) {
            int yCentre = 0;
            for ( int y = 0; y < containerHeight; y++) {
                if (y >  centreBegins && y < centreBegins + length ){
                    output[x][y] = content[xCentre][yCentre];
                    yCentre++;
                }
            }
            if (x > centreBegins && x < centreBegins + length){
                xCentre++;
            }
        }
        return output;
    }

    /**
     * Creates a matrix of a specified size, and sets all values to default to
     * -1024, the hounsfield value of air.
     * @param width the width of the matrix to create.
     * @param height the height of the matrix to create.
     * @return a matrix of specified height.
     */
    private short[][] createMatrix (int width, int height){
        short[][] matrix = new short[width][height];
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                matrix[x][y] = -1024;
            }
        }
        return matrix;
    }
}
