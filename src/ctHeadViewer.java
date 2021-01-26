import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.*;

// OK this is not best practice - maybe you'd like to create
// a volume data class?
// I won't give extra marks for that though.

public class ctHeadViewer extends Application {
    short cthead[][][]; //store the 3D volume data set
    short min, max; //min/max value in the 3D volume data set
    int CT_x_axis = 256;
    int CT_y_axis = 256;
    int CT_z_axis = 113;

    @Override
    public void start(Stage stage) throws FileNotFoundException, IOException {
        stage.setTitle("CThead Viewer");


        ReadData();

        //Good practice: Define your top view, front view and side view images (get the height and width correct)
        //Here's the top view - looking down on the top of the head (each slice we are looking at is CT_x_axis x CT_y_axis)
        int Top_width = CT_x_axis;
        int Top_height = CT_y_axis;

        //Here's the front view - looking at the front (nose) of the head (each slice we are looking at is CT_x_axis x CT_z_axis)
        int Front_width = CT_x_axis;
        int Front_height = CT_z_axis;

        //and you do the other (side view) - looking at the ear of the head
        int side_width = CT_x_axis;
        int side_height = CT_z_axis;

        //We need 3 things to see an image
        //1. We create an image we can write to
        WritableImage top_image = new WritableImage(Top_width, Top_height);
        WritableImage front_image = new WritableImage(Front_width, Front_height);
        WritableImage side_image = new WritableImage(side_width, side_height);

        //2. We create a view of that image
        ImageView TopView = new ImageView(top_image);
        ImageView FrontView = new ImageView(front_image);
        ImageView SideView = new ImageView(side_image);


        Button slice76_button=new Button("slice76"); //an example button to get the slice 76
        //sliders to step through the slices (top and front directions) (remember 113 slices in top direction 0-112)
        Slider Top_slider = new Slider(0, CT_z_axis-1, 0);
        Slider Front_slider = new Slider(0, CT_y_axis-1, 0);
        Slider Side_slider = new Slider(0, CT_y_axis-1, 0);

        slice76_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TopDownSlice(top_image, 76);
                SideSlice(side_image, 76);
                FrontSlice(front_image, 76);
            }
        });

        Top_slider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue <? extends Number >
                                                observable, Number oldValue, Number newValue)
                    {
                        System.out.println(newValue.intValue());
                        TopDownSlice(top_image, newValue.intValue());
                    }
                });

        Front_slider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue <? extends Number >
                                                observable, Number oldValue, Number newValue)
                    {
                        System.out.println(newValue.intValue());
                        FrontSlice(front_image, newValue.intValue());
                    }
                });

        Side_slider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue <? extends Number >
                                                observable, Number oldValue, Number newValue)
                    {
                        System.out.println(newValue.intValue());
                        SideSlice(side_image, newValue.intValue());
                    }
                });

        Button volRendButton = new Button("Volume Render");
        volRendButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

            }
        });

        FlowPane root = new FlowPane();
        root.setVgap(8);
        root.setHgap(4);
//https://examples.javacodegeeks.com/desktop-java/javafx/scene/image-scene/javafx-image-example/




        StackPane FrontPadding = new StackPane(FrontView);
        FrontPadding.setStyle("-fx-padding: 0 0 30 30;");

        StackPane SidePadding = new StackPane(SideView);
        SidePadding.setStyle("-fx-padding: 0 0 0 30;");


        //3. (referring to the 3 things we need to display an image)
        //we need to add it to the flow pane
        root.getChildren().addAll(TopView, new VBox(FrontPadding, SidePadding), slice76_button, volRendButton,Top_slider, Front_slider, Side_slider);

        Scene scene = new Scene(root, 546, 480);
        stage.setScene(scene);
        stage.show();
    }

    //Function to read in the cthead data set
    public void ReadData() throws IOException {
        //File name is hardcoded here - much nicer to have a dialog to select it and capture the size from the user
        File file = new File("CThead");
        //Read the data quickly via a buffer (in C++ you can just do a single fread - I couldn't find if there is an equivalent in Java)
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        int i, j, k; //loop through the 3D data set

        min=Short.MAX_VALUE; max=Short.MIN_VALUE; //set to extreme values
        short read; //value read in
        int b1, b2; //data is wrong Endian (check wikipedia) for Java so we need to swap the bytes around

        cthead = new short[CT_z_axis][CT_y_axis][CT_x_axis]; //allocate the memory - note this is fixed for this data set
        //loop through the data reading it in
        for (k=0; k<CT_z_axis; k++) {
            for (j=0; j<CT_y_axis; j++) {
                for (i=0; i<CT_x_axis; i++) {
                    //because the Endianess is wrong, it needs to be read byte at a time and swapped
                    b1=((int)in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    b2=((int)in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    read=(short)((b2<<8) | b1); //and swizzle the bytes around
                    if (read<min) min=read; //update the minimum
                    if (read>max) max=read; //update the maximum
                    cthead[k][j][i]=read; //put the short into memory (in C++ you can replace all this code with one fread)
                }
            }
        }
        System.out.println(min+" "+max); //diagnostic - for CThead this should be -1117, 2248
        //(i.e. there are 3366 levels of grey (we are trying to display on 256 levels of grey)
        //therefore histogram equalization would be a good thing
        //maybe put your histogram equalization code here to set up the mapping array
    }


    /*
       This function shows how to carry out an operation on an image.
       It obtains the dimensions of the image, and then loops through
       the image carrying out the copying of a slice of data into the
       image.
   */
    public void TopDownSlice(WritableImage image, int slice) {
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
                datum=cthead[slice][j][i]; //get values from slice 76 (change this in your assignment)
                //calculate the colour by performing a mapping from [min,max] -> 0 to 1 (float)
                //Java setColor uses float values from 0 to 1 rather than 0-255 bytes for colour
                col=(((float)datum-(float)min)/((float)(max-min)));
                image_writer.setColor(i, j, Color.color(col,col,col, 1.0));
            } // column loop
        } // row loop
    }

    public void FrontSlice(WritableImage image, int slice) {
        //Get image dimensions, and declare loop variables
        int w=(int) image.getWidth(), h=(int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        double col;
        short datum;

        for (int j=0; j<h; j++) {
            for (int i=0; i<w; i++) {
                datum=cthead[j][slice][i];

                col=(((float)datum-(float)min)/((float)(max-min)));
                image_writer.setColor(i, j, Color.color(col,col,col, 1.0));
            } // column loop
        } // row loop
    }

    public void SideSlice(WritableImage image, int slice) {
        //Get image dimensions, and declare loop variables
        int w=(int) image.getWidth(), h=(int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        double col;
        short datum;

        for (int j=0; j<h; j++) {
            for (int i=0; i<w; i++) {
                datum=cthead[j][i][slice];

                col=(((float)datum-(float)min)/((float)(max-min)));

                image_writer.setColor(i, j, Color.color(col,col,col, 1.0));
            } // column loop
        } // row loop
    }
    
    public static void main(String[] args) {
        launch();
    }

}