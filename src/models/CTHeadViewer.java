package models;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class CTHeadViewer {
    private final Volume ctHead;
    double opacity = 0.12;
    //Good practice: Define your top view, front view and side view images (get the height and width correct)
    //Here's the top view - looking down on the top of the head (each slice we are looking at is CT_x_axis x CT_y_axis)
    private final int Top_width;
    private final int Top_height;

    //Here's the front view - looking at the front (nose) of the head (each slice we are looking at is CT_x_axis x CT_z_axis)
    private final int Front_width;
    private final int Front_height;

    //and you do the other (side view) - looking at the ear of the head
    private final int side_width;
    private final int side_height;

    public CTHeadViewer(Volume volume) {
        this.ctHead = volume;
        this.Top_width = volume.getCT_x_axis();
        this.Top_height = volume.getCT_y_axis();

        this.Front_width = volume.getCT_x_axis();
        this.Front_height = volume.getCT_z_axis();

        this.side_width = volume.getCT_x_axis();
        this.side_height = volume.getCT_z_axis();
    }

    /*
       This function shows how to carry out an operation on an image.
       It obtains the dimensions of the image, and then loops through
       the image carrying out the copying of a slice of data into the
       image.
   */
    public void drawSlice(WritableImage image, int slice, String view) {
        //Get image dimensions, and declare loop variables
        int w=(int) image.getWidth(), h=(int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        double col;
        short datum;
        //Shows how to loop through each pixel and colour
        //Try to always use j for loops in y, and i for loops in x
        //as this makes the code more readable
        for (int j=0; j<h; j++) {
            for (int i=0; i<w; i++) {
                //at this point (i,j) is a single pixel in the image
                //here you would need to do something to (i,j) if the image size
                //does not match the slice size (e.g. during an image resizing operation
                //If you don't do this, your j,i could be outside the array bounds
                //In the framework, the image is 256x256 and the data set slices are 256x256
                //so I don't do anything - this also leaves you something to do for the assignment
                datum = getView(view, i , j, slice); //get values from slice 76 (change this in your assignment)
                //calculate the colour by performing a mapping from [min,max] -> 0 to 1 (float)
                //Java setColor uses float values from 0 to 1 rather than 0-255 bytes for colour
                col=(((float)datum-(float)ctHead.getMin())/((float)(ctHead.getMax()-ctHead.getMin())));
                image_writer.setColor(i, j, Color.color(col,col,col, 1.0));
            } // column loop
        } // row loop
    }

    public short getView(String view, int x, int y, int z){
        if (view.equals("top")){
            return ctHead.getVoxel(z, y, x);
        } else if (view.equals("side")){
            return ctHead.getVoxel(y, x, z);
        } else {
            return ctHead.getVoxel(y, z, x);
        }
    }

    public void volumeRender(WritableImage image, String view){
        int w=(int) image.getWidth(), h=(int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        int rayLength = (view.equals("top")) ? ctHead.getCT_z_axis() : ctHead.getCT_x_axis();

        for (int j = 0; j<h; j++) {
            for (int i = 0; i < w; i++) {
                double aAccum = 1;
                double cRAccum = 0;
                double cGAccum = 0;
                double cBAccum = 0;
                double L = 1;

                for (int ray = 0; ray < rayLength; ray++) {
                    short currentVoxel = getView(view, i, j, ray);
                    double[] c = transferFunction(currentVoxel);

                    cRAccum = Math.min(cRAccum + (aAccum * c[3] * L * c[0]), 1);
                    cGAccum = Math.min(cGAccum + (aAccum * c[3] * L * c[1]), 1);
                    cBAccum = Math.min(cBAccum + (aAccum * c[3] * L * c[2]), 1);
                    aAccum = aAccum * (1 - c[3]);
                }

                double opacity = 1 - aAccum;
                image_writer.setColor(i, j, Color.color(cRAccum, cGAccum, cBAccum, opacity));
            }
        }
    }

    private double[] transferFunction(short datum){
        double R, G, B, O;
        if ((datum > -299) && (datum < 50)){
            R = 1.0;
            G = 0.79;
            B = 0.6;
            O = opacity;
        } else  if (datum > 300){
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

    public Volume getCtHead() {
        return ctHead;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public int getTop_width() {
        return Top_width;
    }

    public int getTop_height() {
        return Top_height;
    }

    public int getFront_width() {
        return Front_width;
    }

    public int getFront_height() {
        return Front_height;
    }

    public int getSide_width() {
        return side_width;
    }

    public int getSide_height() {
        return side_height;
    }

}