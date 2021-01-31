package models;
import java.io.*;

public class Volume {
    private short[][][] volume;
    private short min, max;
    private final int CT_x_axis;
    private final int CT_y_axis;
    private final int CT_z_axis;

    public Volume(int x, int y, int z) {
        this.CT_x_axis = x;
        this.CT_y_axis = y;
        this.CT_z_axis = z;
    }

    //Function to read in the cthead data set
    public void ReadData(String filename, Boolean isCorrectEndian) throws IOException {
        //File name is hardcoded here - much nicer to have a dialog to select it and capture the size from the user
        File file = new File(filename);
        //Read the data quickly via a buffer (in C++ you can just do a single fread - I couldn't find if there is an equivalent in Java)
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        int i, j, k; //loop through the 3D data set

        min=Short.MAX_VALUE; max=Short.MIN_VALUE; //set to extreme values
        short read; //value read in
        int b1, b2; //data is wrong Endian (check wikipedia) for Java so we need to swap the bytes around

        volume = new short[CT_z_axis][CT_y_axis][CT_x_axis]; //allocate the memory - note this is fixed for this data set
        //loop through the data reading it in
        for (k=0; k<CT_z_axis; k++) {
            for (j=0; j<CT_y_axis; j++) {
                for (i=0; i<CT_x_axis; i++) {
                    //because the Endianess is wrong, it needs to be read byte at a time and swapped
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
        //(i.e. there are 3366 levels of grey (we are trying to display on 256 levels of grey)
        //therefore histogram equalization would be a good thing
        //maybe put your histogram equalization code here to set up the mapping array
    }

    public short getVoxel(int x, int y, int z) {
        return volume[x][y][z];
    }

    public short[][][] getVolume() {
        return volume;
    }

    public void setVolume(short[][][] volume) {
        this.volume = volume;
    }

    public short getMin() {
        return min;
    }

    public void setMin(short min) {
        this.min = min;
    }

    public short getMax() {
        return max;
    }

    public void setMax(short max) {
        this.max = max;
    }

    public int getCT_x_axis() {
        return CT_x_axis;
    }

    public int getCT_y_axis() {
        return CT_y_axis;
    }

    public int getCT_z_axis() {
        return CT_z_axis;
    }
}
