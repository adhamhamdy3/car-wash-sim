package simulator.cws;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController implements CarObserver, PumpObserver {
    @FXML private Label queueStatusLabel;
    @FXML private TextField capacityField;
    @FXML private TextField pumpsField;
    @FXML private Button startBtn;
    @FXML private Button stopBtn;
    @FXML private Button resetBtn;
    @FXML private Button addCarButton;
    @FXML private Button clearLogBtn;
    @FXML private TextArea logArea;
    @FXML private Spinner<Integer> speedSpinner;

    // Stats
    @FXML private Label arrivedLabel;
    @FXML private Label servicedLabel;
    @FXML private Label waitingLabel;

    // Cars queue
    @FXML private FlowPane queueContainer;

    // Pumps cards
    @FXML private FlowPane pumpsContainer;

    // Service Station
    private ServiceStation station;

    private final Map<Integer, VBox> pumpCards = new HashMap<>();

    @FXML
    public void initialize() {
        // Hook up buttons
        startBtn.setOnAction(e -> startSimulation());
        addCarButton.setOnAction(e -> addCar());
        resetBtn.setOnAction(e -> resetSimulation());
        stopBtn.setOnAction(e -> stopSimulation());
        clearLogBtn.setOnAction(e -> clearLog());

        speedSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2));
    }

    void setupPumpCards(int numPumps) {
        Platform.runLater(() -> {
            pumpsContainer.getChildren().clear();
            for (int i = 1; i <= numPumps; i++) {
                VBox pumpCard = new VBox(10);
                pumpCard.setStyle("-fx-border-color: #23ce6b; -fx-border-radius: 8;-fx-border-width: 1; -fx-padding: 10; -fx-alignment: center; -fx-background-color: rgba(254,246,239,0.88);");
                pumpCard.setPrefWidth(180);

                // === Top bar: Light + Label ===
                HBox topBar = new HBox(10);
                topBar.setStyle("-fx-alignment: center-left; -fx-end-margin: 10;");
                ImageView light = new ImageView(new Image(getClass().getResource("/simulator/cws/assets/green.png").toExternalForm()));
                light.setFitWidth(57);
                light.setFitHeight(24);
                HBox.setMargin(light, new Insets(0, 0, 0, 7));
                Label countDownlabel = new Label(i + "s");
                topBar.getChildren().addAll(light, countDownlabel);

                // === Middle row: Pump + Car ===
                HBox serviceRow = new HBox(10);
                serviceRow.setStyle("-fx-alignment: center;");

                // Pump image
                ImageView pumpImage = new ImageView(new Image(getClass().getResource("/simulator/cws/assets/pump.png").toExternalForm()));
                pumpImage.setFitWidth(100);
                pumpImage.setFitHeight(100);

                Label pumplabel = new Label("Pump " + i);
                pumplabel.setStyle("-fx-alignment: left;");
                // Placeholder (empty) car box — we’ll fill it later when a car arrives
                VBox carBox = new VBox(5);
                carBox.setStyle("-fx-alignment: center;");
                carBox.setPrefWidth(90);

                serviceRow.getChildren().addAll(pumpImage, carBox);

                // === Add everything to the card ===
                pumpCard.getChildren().addAll(topBar, serviceRow, pumplabel);

                // Save references
                pumpCards.put(i, pumpCard);
                pumpsContainer.getChildren().add(pumpCard);
            }
        });
    }

    void createCarCard(int carId) {
        Platform.runLater(() -> {
            // Create a VBox to hold the car image and label
            VBox carBox = new VBox(5);
            carBox.setAlignment(javafx.geometry.Pos.CENTER);
            carBox.setUserData(carId);


            int imageNumber = (carId - 1) % 8 + 1;
            String imagePath = "/simulator/cws/assets/" + imageNumber + ".png";

            // Load the car image
            ImageView carImage = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
            carImage.setFitWidth(110);
            carImage.setFitHeight(110);

            // Label under the car image (C1, C2, ...)
            Text carLabel = new Text("C" + carId);

            carBox.getChildren().addAll(carImage, carLabel);

            // Add the car box to the FlowPane queueContainer
            queueContainer.getChildren().add(carBox);
        });
    }

    // When a pump starts servicing a car
    public void startServiceVisual(int pumpId, String carTag) {
        Platform.runLater(() -> {
            if(!queueContainer.getChildren().isEmpty()){
                queueContainer.getChildren().removeFirst();
            }
            VBox pumpCard = pumpCards.get(pumpId);
            if (pumpCard == null) return;

            // Top bar: set red light
            HBox topBar = (HBox) pumpCard.getChildren().get(0);
            ImageView light = (ImageView) topBar.getChildren().get(0);
            light.setImage(new Image(getClass().getResource("/simulator/cws/assets/red.png").toExternalForm()));
            pumpCard.setStyle("-fx-border-color: #d90707; -fx-border-radius: 6; -fx-border-width: 2; " +
                    "-fx-padding: 10; -fx-alignment: center; -fx-background-color: rgba(254,246,239,0.88);");
            // Middle row: find carBox
            HBox serviceRow = (HBox) pumpCard.getChildren().get(1);
            VBox carBox = (VBox) serviceRow.getChildren().get(1);

            // Clear previous car visuals (if any)
            carBox.getChildren().clear();

            // Determine image path based on car number
            String carNum = carTag.replaceAll("[^0-9]", "");
            int carIndex = ((Integer.parseInt(carNum) - 1) % 8) + 1;
            String imagePath = "/simulator/cws/assets/" + carIndex + ".png";

            // Load car image
            Image carImage;
            try {
                carImage = new Image(getClass().getResource(imagePath).toExternalForm());
            } catch (Exception e) {
                carImage = new Image(getClass().getResource("/simulator/cws/assets/1.png").toExternalForm());
            }

            ImageView carImageView = new ImageView(carImage);
            carImageView.setFitWidth(100);
            carImageView.setFitHeight(100);

            Label carLabel = new Label(carTag);
            carLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

            carBox.getChildren().addAll(carImageView, carLabel);
        });
    }

    // When a pump finishes servicing a car
    public void finishServiceVisual(int pumpId) {
        Platform.runLater(() -> {
            VBox pumpCard = pumpCards.get(pumpId);
            if (pumpCard == null) return;

            // Turn light back to green
            HBox topBar = (HBox) pumpCard.getChildren().get(0);
            ImageView light = (ImageView) topBar.getChildren().get(0);
            light.setImage(new Image(getClass().getResource("/simulator/cws/assets/green.png").toExternalForm()));
            pumpCard.setStyle("-fx-border-color: #23ce6b; -fx-border-radius: 6; -fx-border-width: 2; -fx-padding: 10; -fx-alignment: center; -fx-background-color: rgba(254,246,239,0.88);");
            // Clear the car box
            HBox serviceRow = (HBox) pumpCard.getChildren().get(1);
            VBox carBox = (VBox) serviceRow.getChildren().get(1);
            carBox.getChildren().clear();
        });
    }

    private void startSimulation() {
        try {
            int waitingAreaSize = Integer.parseInt(capacityField.getText());
            int numPumps = Integer.parseInt(pumpsField.getText());

            station = new ServiceStation(waitingAreaSize, numPumps);
            station.startSimulation(speedSpinner.getValue(), this);

            log("Simulation started: Waiting area capacity " + waitingAreaSize + ", Pumps = " + numPumps +
                    " and Service speed = " + speedSpinner.getValue() + "s");

            setupPumpCards(numPumps);

            startBtn.setDisable(true);
            stopBtn.setDisable(false);
            speedSpinner.setDisable(true);
        } catch (NumberFormatException e) {
            log("Please enter valid numbers for capacity and pumps.");
        }
    }

    private void addCar() {
        if (!station.isRunning()) {
            log("Start the simulation first!");
            return;
        }

        station.addCar(this);
    }

    private void stopSimulation() {
        station.stopSimulation();

        log("Simulation stopped manually.");

        stopBtn.setDisable(true);
        addCarButton.setDisable(true);
    }

    private void resetSimulation() {
        station.reset();

        logArea.clear();
        queueContainer.getChildren().clear();
        pumpsContainer.getChildren().clear();
        startBtn.setDisable(false);
        addCarButton.setDisable(false);
        stopBtn.setDisable(true);
        speedSpinner.setDisable(false);

        log("Simulation reset.");
    }

    private void clearLog(){
        logArea.clear();
    }

    // Helper to safely log from any thread
    public void log(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));

            // TODO: CHANGE THIS LOGIC
            if(station.getWaitingCars() >= station.getWaitingAreaSize()) {
                addCarButton.setDisable(true);
                 logArea.appendText("Reached maximum capacity.");
                return;
            } else {
                addCarButton.setDisable(false);
            }

            logArea.appendText("[" + timestamp + "] " + message + "\n");

            // Update labels
            queueStatusLabel.setText("Queue: " + station.getWaitingCars() + "/" + station.getWaitingAreaSize());
            arrivedLabel.setText("Total cars arrived: " + station.getCarCounter());
            servicedLabel.setText("Cars serviced: " + station.getServicedCars());
            waitingLabel.setText("Cars waiting: " + station.getWaitingCars());
        });
    }

    private void removeCarCard(int carId) {
        queueContainer.getChildren().removeIf(node -> {
            Object id = node.getUserData();
            return id != null && id.equals(carId);
        });
    }

    @Override
    public void onCarLogins(int pumpId, int carId) {
        Platform.runLater(() -> {
            log("P" + pumpId + ": C" + carId + " login");
            removeCarCard(carId);
            startServiceVisual(pumpId, "C" + carId);
        });
    }

    @Override
    public void onCarBeginsService(int pumpId, int carId) {
        Platform.runLater(() -> {
            log("P" + pumpId + ": C" + carId + " begins service at Bay " + pumpId);
        });
    }

    @Override
    public void onCarFinishesService(int pumpId, int carId) {
        Platform.runLater(() -> {
            log("P" + pumpId + ": C" + carId + " finishes service");
            log("P" + pumpId + ": Bay " + pumpId + " is now free");

            removeCarCard(carId); // double check
            finishServiceVisual(pumpId);
        });
    }

    @Override
    public void onCarArrives(int carId) {
        Platform.runLater(() -> log("C" + carId + " arrived"));
    }

    @Override
    public void onCarEntersQueue(int carId) {
        Platform.runLater(() -> {
            log("C" + carId + " entered the queue");
             createCarCard(carId);
        });
    }

    @Override
    public void onException(String message) {
        Platform.runLater(() -> log(message));
    }
}