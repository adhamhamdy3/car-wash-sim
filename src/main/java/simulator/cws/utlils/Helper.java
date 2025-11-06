package simulator.cws.utlils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Helper {
    public ImageView getCarImageView(int carId) {
        int carIndex = ((carId - 1) % 8) + 1;
        String imagePath = "/simulator/cws/assets/" + carIndex + ".png";

        // Load car image
        Image carImage;
        try {
            carImage = new Image(getClass().getResource(imagePath).toExternalForm());
        } catch (Exception e) {
            carImage = new Image(getClass().getResource("/simulator/cws/assets/1.png").toExternalForm());
        }

        return new ImageView(carImage);
    }
}
