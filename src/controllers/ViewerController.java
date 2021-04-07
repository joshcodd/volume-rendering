package controllers;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.CTViewer;
import views.Menu;

/**
 * Controller class for the viewer interface.
 * Handles all user input and displays scans.
 * @author Josh Codd.
 */
public class ViewerController {
    @FXML
    public ImageView firstView;
    public ImageView secondView;
    public ImageView thirdView;
    public Slider firstViewSlider;
    public Slider secondViewSlider;
    public Slider thirdViewSlider;
    public Slider opacitySlider;
    public Button volumeRenderButton;
    public Button midSlideButton;
    public StackPane firstViewBackground;
    public StackPane secondViewBackground;
    public StackPane thirdViewBackground;
    public StackPane menuPane;
    public Button openFileButton;
    public Slider lightSource;
    public Button gradientButton;
    public Button gradientInterpolationButton;
    public Button mipButton;
    public VBox volRendMenu;
    public VBox lightMenu;
    public ChoiceBox<String> tfChoice;
    public ScrollPane sc;

    private Stage stage;
    private CTViewer ctViewer;
    private boolean isVolumeRendered = false;
    private boolean isMIP = false;
    private String transferFunction = "TF1";

    WritableImage top_image;
    WritableImage front_image;
    WritableImage side_image;

    /**
     * Initialises UI elements to be ready for display.
     */
    public void init() {
        top_image = new WritableImage(ctViewer.getTop_width(), ctViewer.getTop_height());
        front_image = new WritableImage(ctViewer.getFront_width(), ctViewer.getFront_height());
        side_image = new WritableImage(ctViewer.getSide_width(), ctViewer.getSide_height());

        Menu menu = new Menu(stage);
        menuPane.getChildren().add(menu.getRoot());
        menu.getController().setStage(stage);
        menu.getRoot().setVisible(false);

        firstView.setImage(top_image);
        secondView.setImage(front_image);
        thirdView.setImage(side_image);

        firstViewSlider.setMax(ctViewer.getCtScan().getCT_z_axis() - 1);
        secondViewSlider.setMax(ctViewer.getCtScan().getCT_y_axis() - 1);
        thirdViewSlider.setMax(ctViewer.getCtScan().getCT_y_axis() - 1);

        tfChoice.getItems().add("TF1");
        tfChoice.getItems().add("TF2");
        tfChoice.setValue("TF1");

        tfChoice.setOnAction(event ->  {
            transferFunction = tfChoice.getValue();
            volumeRender();}
        );


        midSlideButton.setOnAction(event -> {
            reset();
            isMIP = false;
            firstViewSlider.valueProperty().setValue(75);
            secondViewSlider.valueProperty().setValue(75);
            thirdViewSlider.valueProperty().setValue(75);

            firstViewSlider.valueProperty().setValue(76);
            secondViewSlider.valueProperty().setValue(76);
            thirdViewSlider.valueProperty().setValue(76);
        });

        volumeRenderButton.setOnAction(event -> {
            if (!isVolumeRendered) {
                isMIP = false;
                volumeRender();
                volRendMenu.setVisible(true);
                volRendMenu.setManaged(true);
                isVolumeRendered = true;
            } else {
                midSlideButton.fire();
            }
        });

        firstViewSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ctViewer.drawSlice(top_image, "top", newValue.intValue());
            sliderValueStyle(firstViewSlider);
            reset();
            isMIP = false;
        });

        secondViewSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ctViewer.drawSlice(front_image,"front", newValue.intValue());
            sliderValueStyle(secondViewSlider);
            reset();
            isMIP = false;
        });

        thirdViewSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ctViewer.drawSlice(side_image, "side", newValue.intValue());
            sliderValueStyle(thirdViewSlider);
            reset();
            isMIP = false;
        });

        opacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ctViewer.setOpacity((double) (newValue)/100.0);
            volumeRender();
            sliderValueStyle(opacitySlider);
        });

        lightSource.valueProperty().addListener((observable, oldValue, newValue) -> {
            ctViewer.setLightSourceX(newValue.intValue());
            volumeRender();
            sliderValueStyle(lightSource);
        });

        gradientButton.setOnAction(e -> {
            ctViewer.setGradientShading(!ctViewer.getGradientShading());
            lightMenu.setVisible(ctViewer.getGradientShading());
            lightMenu.setManaged(true);
            volumeRender();
        });

        gradientInterpolationButton.setOnAction(e -> {
            ctViewer.setGradientInterpolation(!ctViewer.getGradientInterpolation());
            String value = ctViewer.getGradientInterpolation() ? "On" : "Off";
            gradientInterpolationButton.setText("Interpolation: " + value);
            volumeRender();
        });

        mipButton.setOnAction(e -> {
            if (!isMIP) {
                ctViewer.maximumIntensityProjection(top_image, "top");
                ctViewer.maximumIntensityProjection(side_image, "side");
                ctViewer.maximumIntensityProjection(front_image, "front");
                reset();
                isMIP = true;
            } else {
                isMIP = false;
                midSlideButton.fire();
            }
        });

        openFileButton.setOnAction(e -> {
            menu.getRoot().setVisible(!menu.getRoot().isVisible());
            volRendMenu.setManaged(false);
        });
    }

    /**
     * Resets the volume rendering menu.
     */
    public void reset(){
        volRendMenu.setVisible(false);
        volRendMenu.setManaged(false);
        isVolumeRendered = false;
        ctViewer.setGradientShading(false);
        ctViewer.setGradientInterpolation(false);
        gradientInterpolationButton.setText("Interpolation: Off");
        lightMenu.setVisible(false);
        lightMenu.setManaged(false);
    }

    /**
     * Carried out volume rendering on all views.
     */
    public void volumeRender(){
        ctViewer.volumeRender(side_image, "side", transferFunction);
        ctViewer.volumeRender(top_image, "top", transferFunction);
        ctViewer.volumeRender(front_image, "front", transferFunction);
    }

    /**
     * Changes the style of a slider to be a different colour up to the value selected/thumb location.
     * @param slider The slider to update.
     */
    public void sliderValueStyle(Slider slider){
        double value = (slider.getValue() - slider.getMin()) / (slider.getMax() - slider.getMin()) * 100.0 ;
        slider.lookup(".slider .track").setStyle(String.format("-fx-background-color: " +
                        "linear-gradient(to right, #0278D7 0%%, #0278D7 %f%%, #383838 %f%%, #383838 100%%);",
                value, value));
    }

    /**
     * Sets the viewer to use.
     * @param ctHead The viewer to use.
     */
    public void setCTHeadViewer (CTViewer ctHead) {
        this.ctViewer = ctHead;
    }

    /**
     * Gets the slider that sets opacity.
     * @return The opacity slider.
     */
    public Slider getOpacitySlider(){
        return opacitySlider;
    }

    /**
     * Gets the button that displays middle slide.
     * @return The mid slide button.
     */
    public Button getMidSlideButton(){
        return midSlideButton;
    }

    /**
     * Gets the pane in which the menu is to be displayed..
     * @return The menu pane.
     */
    public StackPane getMenuPane() {
        return menuPane;
    }

    /**
     * Gets the pane in which the volume rendering options are to be displayed in.
     * @return The volume rendering menu pane.
     */
    public VBox getVolRendMenu() {
        return volRendMenu;
    }

    /**
     * Sets the stage of the application.
     * @param stage The stage to set.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
