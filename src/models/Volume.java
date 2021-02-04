package models;
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
     * @throws IOException If file ends prematurely/wrong size volume.
     */
    public void ReadData(String filename, Boolean isCorrectEndian) throws IOException {
        File file = new File(filename);
        //Read the data quickly via a buffer.
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        int i, j, k; //loop through the 3D data set
        min=Short.MAX_VALUE; max=Short.MIN_VALUE; //set to extreme values
        short read;
        int b1, b2; //data is wrong Endian (check wikipedia) for Java so we need to swap the bytes around

        volume = new short[CT_z_axis][CT_y_axis][CT_x_axis]; //allocate the memory - note this is fixed for this data set
        //loop through the data reading it in
        for (k=0; k<CT_z_axis; k++) {
            for (j=0; j<CT_y_axis; j++) {
                for (i=0; i<CT_x_axis; i++) {
                    //because the Endian is wrong, it needs to be read byte at a time and swapped
                    b1 = ((int) in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    b2 = ((int) in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types

                    if (!isCorrectEndian) {
                        read = (short) ((b2 << 8) | b1); //and swizzle the bytes around
                    } else {
                        read = (short) ((b1 << 8) | b2); //and swizzle the bytes around
                    }

                    if (read < min) min = read; //update the minimum
                    if (read > max) max = read; //update the maximum
                    volume[k][j][i]=read; //put the short into memory (in C++ you can replace all this code with one fread)
                }
            }
        }
        System.out.println(min+" "+max); //diagnostic - for CThead this should be -1117, 2248
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
}
