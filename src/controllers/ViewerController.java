package controllers;
import javafx.fxml.FXML;
import javafx.scene.LightBase;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.CTHeadViewer;
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

    private Stage stage;
    private CTHeadViewer ctHead;

    WritableImage top_image;
    WritableImage front_image;
    WritableImage side_image;

    /**
     * Initialises UI elements to be ready for display.
     */
    public void init() {
        top_image = new WritableImage(ctHead.getTop_width(), ctHead.getTop_height());
        front_image = new WritableImage(ctHead.getFront_width(), ctHead.getFront_height());
        side_image = new WritableImage(ctHead.getSide_width(), ctHead.getSide_height());

        Menu menu = new Menu(stage);
        menuPane.getChildren().add(menu.getRoot());
        menu.getController().setStage(stage);
        menu.getRoot().setVisible(false);

        firstView.setImage(top_image);
        secondView.setImage(front_image);
        thirdView.setImage(side_image);

        firstViewSlider.setMax(ctHead.getCtHead().getCT_z_axis() - 1);
        secondViewSlider.setMax(ctHead.getCtHead().getCT_y_axis() - 1);
        thirdViewSlider.setMax(ctHead.getCtHead().getCT_y_axis() - 1);

        midSlideButton.setOnAction(event -> {
            firstViewSlider.valueProperty().setValue(75);
            secondViewSlider.valueProperty().setValue(75);
            thirdViewSlider.valueProperty().setValue(75);

            firstViewSlider.valueProperty().setValue(76);
            secondViewSlider.valueProperty().setValue(76);
            thirdViewSlider.valueProperty().setValue(76);
        });

        volumeRenderButton.setOnAction(event -> {
            ctHead.volumeRender(side_image, "side");
            ctHead.volumeRender(top_image, "top");
            ctHead.volumeRender(front_image, "front");
        });

        firstViewSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ctHead.drawSlice(top_image, newValue.intValue(), "top");
            sliderValueStyle(firstViewSlider);
        });

        secondViewSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ctHead.drawSlice(front_image, newValue.intValue(), "front");
            sliderValueStyle(secondViewSlider);
        });

        thirdViewSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ctHead.drawSlice(side_image, newValue.intValue(), "side");
            sliderValueStyle(thirdViewSlider);
        });

        opacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ctHead.setOpacity((double) (newValue)/100.0);
            volumeRenderButton.fire();
            sliderValueStyle(opacitySlider);
        });

        lightSource.valueProperty().addListener((observable, oldValue, newValue) -> {
            ctHead.setLightSourceX(newValue.intValue());
            volumeRenderButton.fire();
            sliderValueStyle(lightSource);
        });

        gradientButton.setOnAction( e -> {
            ctHead.toggleGradient();
            volumeRenderButton.fire();
        });

        openFileButton.setOnAction(e -> menu.getRoot().setVisible(!menu.getRoot().isVisible()));
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
    public void setCTHeadViewer (CTHeadViewer ctHead) {
        this.ctHead = ctHead;
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
     * Sets the stage of the application.
     * @param stage The stage to set.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
