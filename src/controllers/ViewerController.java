package controllers;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import models.CTHeadViewer;

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

    private CTHeadViewer ctHead;

    WritableImage top_image;
    WritableImage front_image;
    WritableImage side_image;


    public void draw() {
        top_image = new WritableImage(ctHead.getTop_width(), ctHead.getTop_height());
        front_image = new WritableImage(ctHead.getFront_width(), ctHead.getFront_height());
        side_image = new WritableImage(ctHead.getSide_width(), ctHead.getSide_height());

        firstView.setImage(top_image);
        secondView.setImage(front_image);
        thirdView.setImage(side_image);

        firstViewSlider.setMax(ctHead.getCtHead().getCT_z_axis() - 1);
        secondViewSlider.setMax(ctHead.getCtHead().getCT_y_axis() - 1);
        thirdViewSlider.setMax(ctHead.getCtHead().getCT_y_axis() - 1);

        midSlideButton.setOnAction(event -> {
            ctHead.drawSlice(top_image, 76, "top");
            ctHead.drawSlice(side_image, 76, "side");
            ctHead.drawSlice(front_image, 76, "front");
        });

        volumeRenderButton.setOnAction(event -> {
            ctHead.volumeRender(side_image, "side");
            ctHead.volumeRender(top_image, "top");
            ctHead.volumeRender(front_image, "front");

            Background blackBG = new Background(new BackgroundFill(new Color(0.035,0.035,0.035,1.0), CornerRadii.EMPTY, Insets.EMPTY));
            firstViewBackground.setBackground(blackBG);
            secondViewBackground.setBackground(blackBG);
            thirdViewBackground.setBackground(blackBG);
        });

        firstViewSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                ctHead.drawSlice(top_image, newValue.intValue(), "top"));

        secondViewSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                ctHead.drawSlice(front_image, newValue.intValue(), "front"));

        thirdViewSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                ctHead.drawSlice(side_image, newValue.intValue(), "side"));

        opacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ctHead.setOpacity((double) (newValue)/100.0);
            volumeRenderButton.fire();
        });

        midSlideButton.fire();
    }

    public void setCTHeadViewer (CTHeadViewer ctHead) {
        this.ctHead = ctHead;
    }
}
