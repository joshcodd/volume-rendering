import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.*;

public class ctHeadViewer extends Application {
    Volume ctHead = new Volume(256, 256, 113);
    double opacity = 0.12;

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("CThead Viewer");
        ctHead.ReadData("CThead", false);

        //Good practice: Define your top view, front view and side view images (get the height and width correct)
        //Here's the top view - looking down on the top of the head (each slice we are looking at is CT_x_axis x CT_y_axis)
        int Top_width = ctHead.getCT_x_axis();
        int Top_height = ctHead.getCT_y_axis();

        //Here's the front view - looking at the front (nose) of the head (each slice we are looking at is CT_x_axis x CT_z_axis)
        int Front_width = ctHead.getCT_x_axis();
        int Front_height = ctHead.getCT_z_axis();

        //and you do the other (side view) - looking at the ear of the head
        int side_width = ctHead.getCT_x_axis();
        int side_height = ctHead.getCT_z_axis();

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
        Button volRendButton = new Button("Volume Render");

        //sliders to step through the slices (top and front directions) (remember 113 slices in top direction 0-112)
        Slider Top_slider = new Slider(0, ctHead.getCT_z_axis()-1, 0);
        Slider Front_slider = new Slider(0, ctHead.getCT_y_axis()-1, 0);
        Slider Side_slider = new Slider(0, ctHead.getCT_y_axis()-1, 0);
        Slider opacity_slider = new Slider(0, 100, 12);

        slice76_button.setOnAction(event -> {
            drawSlice(top_image, 76, "top");
            drawSlice(side_image, 76, "side");
            drawSlice(front_image, 76, "front");
        });

        volRendButton.setOnAction(event -> {
            volumeRender(side_image, "side");
            volumeRender(top_image, "top");
            volumeRender(front_image, "front");
        });

        Top_slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue.intValue());
            drawSlice(top_image, newValue.intValue(), "top");
        });

        Front_slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue.intValue());
            drawSlice(front_image, newValue.intValue(), "front");
        });

        Side_slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue.intValue());
            drawSlice(side_image, newValue.intValue(), "side");
        });

        opacity_slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            opacity = (double) (newValue)/100.0;
            volumeRender(side_image, "side");
            volumeRender(top_image, "top");
            volumeRender(front_image, "front");
        });

        FlowPane root = new FlowPane();
        root.setVgap(8);
        root.setHgap(4);

        StackPane FrontPadding = new StackPane(FrontView);
        FrontPadding.setStyle("-fx-padding: 0 0 30 30");
        FrontPadding.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        StackPane SidePadding = new StackPane(SideView);
        SidePadding.setStyle("-fx-padding: 0 0 0 30");
        SidePadding.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        StackPane TopPadding = new StackPane(TopView);
        TopPadding.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        //3. (referring to the 3 things we need to display an image)
        //we need to add it to the flow pane
        root.getChildren().addAll(TopPadding, new VBox(FrontPadding, SidePadding), slice76_button, volRendButton,
                Top_slider, Front_slider, Side_slider, opacity_slider);

        Scene scene = new Scene(root, 546, 480);
        stage.setScene(scene);
        stage.show();
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

    public static void main(String[] args) {
        launch();
    }

}