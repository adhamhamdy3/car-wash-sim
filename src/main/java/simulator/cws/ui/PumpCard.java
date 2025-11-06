package simulator.cws.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import simulator.cws.utlils.Helper;

public class PumpCard extends VBox {
    private int pumpId;

    private ImageView lightImage;
    private ImageView pumpImage;
    private ImageView carImage;
    private HBox topBar;
    private HBox serviceRow;
    private VBox carBox;
    private Label pumpLabel;
    private Label countDownlabel;

    private Helper helper;

    public PumpCard(int pumpId) {
        super(10);
        helper = new Helper();

        this.pumpId = pumpId;

        setStyle("-fx-border-color: #23ce6b; -fx-border-radius: 8;-fx-border-width: 1; -fx-padding: 10; -fx-alignment: center; -fx-background-color: rgba(254,246,239,0.88);");
        setPrefWidth(180);

        // === Top bar: Light + Label ===
        topBar = new HBox(10);
        topBar.setStyle("-fx-alignment: center-left; -fx-end-margin: 10;");

        lightImage = new ImageView(new Image(getClass().getResource("/simulator/cws/assets/green.png").toExternalForm()));
        lightImage.setFitWidth(57);
        lightImage.setFitHeight(24);
        HBox.setMargin(lightImage, new Insets(0, 0, 0, 7));

        countDownlabel = new Label(pumpId + "s");
        topBar.getChildren().addAll(lightImage, countDownlabel);

        // === Middle row: Pump + Car ===
        serviceRow = new HBox(10);
        serviceRow.setStyle("-fx-alignment: center;");

        // Pump image
        pumpImage = new ImageView(new Image(getClass().getResource("/simulator/cws/assets/pump.png").toExternalForm()));
        pumpImage.setFitWidth(100);
        pumpImage.setFitHeight(100);

        carBox = new VBox(5);
        carBox.setStyle("-fx-alignment: center;");
        carBox.setPrefWidth(90);

        serviceRow.getChildren().addAll(pumpImage, carBox);

        pumpLabel = new Label("Pump " + pumpLabel);
        pumpLabel.setStyle("-fx-alignment: left;");

        // === Add everything to the card ===
        this.getChildren().addAll(topBar, serviceRow, pumpLabel);
    }

    public void setLightColor(String color) {
        if (color.equals("red")) {
            // Top bar: set red light
            lightImage.setImage(new Image(getClass().getResource("/simulator/cws/assets/red.png").toExternalForm()));
            this.setStyle("-fx-border-color: #d90707; -fx-border-radius: 6; -fx-border-width: 2; " +
                    "-fx-padding: 10; -fx-alignment: center; -fx-background-color: rgba(254,246,239,0.88);");

        } else if (color.equals("green")) {
            // Turn light back to green
            lightImage.setImage(new Image(getClass().getResource("/simulator/cws/assets/green.png").toExternalForm()));
            this.setStyle("-fx-border-color: #23ce6b; -fx-border-radius: 6; -fx-border-width: 2; -fx-padding: 10; -fx-alignment: center; -fx-background-color: rgba(254,246,239,0.88);");
        }

        // Clear previous car visuals (if any)
        carBox.getChildren().clear();
    }

    public void setCarImage(int carId) {
        ImageView carImageView = helper.getCarImageView(carId);

        carImageView.setFitWidth(100);
        carImageView.setFitHeight(100);

        Label carLabel = new Label("C" + carId);
        carLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        carBox.getChildren().addAll(carImageView, carLabel);
    }
}


