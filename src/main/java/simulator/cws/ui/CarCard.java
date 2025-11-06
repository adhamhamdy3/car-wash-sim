package simulator.cws.ui;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class CarCard extends VBox {
    private int carId;
    private ImageView carImage;
    private Text carLabel;

    public CarCard(int carId) {
        super(5);
        // Create a VBox to hold the car image and label
        this.setAlignment(Pos.CENTER);
        this.setUserData(carId);

        int imageNumber = (carId - 1) % 8 + 1;
        String imagePath = "/simulator/cws/assets/" + imageNumber + ".png";

        // Load the car image
        carImage = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
        carImage.setFitWidth(110);
        carImage.setFitHeight(110);

        // Label under the car image (C1, C2, ...)
        carLabel = new Text("C" + carId);

        this.getChildren().addAll(carImage, carLabel);
    }
}
