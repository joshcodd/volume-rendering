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
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        min=Short.MAX_VALUE; max=Short.MIN_VALUE;
        short read;
        int b1, b2;
        volume = new short[CT_z_axis][CT_y_axis][CT_x_axis];

        for (int k = 0; k < CT_z_axis; k++) {
//            for (int i = 0; i < 3416; i++){
//                in.readByte();
//            }
            for (int j = 0; j < CT_y_axis; j++) {
                for (int i=0; i < CT_x_axis; i++) {
                    b1 = ((int) in.readByte()) & 0xff;
                    b2 = ((int) in.readByte()) & 0xff;
                    //swap bytes
                    if (!isCorrectEndian) {
                        read = (short) ((b2 << 8) | b1);
                    } else {
                        read = (short) ((b1 << 8) | b2);
                    }

                    if (read < min) min = read;
                    if (read > max) max = read;
                    volume[k][j][i]=read;
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
