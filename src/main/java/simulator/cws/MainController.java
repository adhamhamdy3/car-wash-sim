package simulator.cws;

import javafx.application.Platform;
import javafx.fxml.FXML;
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
import java.util.List;

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

    private List<VBox> pumpCards = new ArrayList<>();

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
        // visual changes to pump cards
        Platform.runLater(() -> {
            pumpsContainer.getChildren().clear();
            pumpCards.clear();

            for (int i = 1; i <= numPumps; i++) {
                VBox pumpCard = new VBox(5);
                pumpCard.getStyleClass().add("pump-card");
                pumpCard.setPrefWidth(210);
                pumpCard.setPrefHeight(150);
                pumpCard.setStyle("-fx-alignment: center; -fx-background-color: #f7f7f7; "
                        + "-fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 10; "
                        + "-fx-background-radius: 10;");

                // Pump image
                ImageView pumpImage = new ImageView(
                        new Image(getClass().getResource("/simulator/cws/assets/pump.png").toExternalForm()));
                pumpImage.setFitWidth(80);
                pumpImage.setFitHeight(80);

                // Light indicator (start as green)
                ImageView lightImage = new ImageView(
                        new Image(getClass().getResource("/simulator/cws/assets/green.png").toExternalForm()));
                lightImage.setFitWidth(48);
                lightImage.setFitHeight(19);

                // Countdown label
                Label countdownLabel = new Label(speedSpinner.getValue().toString() + "s");
                countdownLabel.getStyleClass().add("countdown-label");

                // Pump label
                Label pumpLabel = new Label("Pump " + i);
                pumpLabel.getStyleClass().add("pump-label");

                // Layout: light + countdown on same row
                HBox topBar = new HBox(5, lightImage, countdownLabel);
                topBar.setStyle("-fx-alignment: center;");

                pumpCard.getChildren().addAll(topBar, pumpImage, pumpLabel);

                pumpsContainer.getChildren().add(pumpCard);
                pumpCards.add(pumpCard); // save for later reference
            }
        });

    }

    void createCarCard() {
        Platform.runLater(() -> {
            // Create a VBox to hold the car image and label
            VBox carBox = new VBox(5);
            carBox.setAlignment(javafx.geometry.Pos.CENTER);

            // Cycle through 1.png–8.png for car images
            int imageNumber = (station.getCarCounter()) % 8 + 1;
            String imagePath = "/simulator/cws/assets/" + imageNumber + ".png";

            // Load the car image
            ImageView carImage = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
            carImage.setFitWidth(90);
            carImage.setFitHeight(90);

            // Label under the car image (C1, C2, ...)
            Text carLabel = new Text("C" + station.getCarCounter());

            carBox.getChildren().addAll(carImage, carLabel);

            // Add the car box to the FlowPane queueContainer
            queueContainer.getChildren().add(carBox);
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
//        createCarCard();
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

    @Override
    public void onCarLogins(int pumpId, int carId) {
        Platform.runLater(() -> {
            log("P" + pumpId + ": C" + carId + " login");

            // Modify queueContainer safely on the FX thread
            if (!queueContainer.getChildren().isEmpty()) {
                queueContainer.getChildren().removeFirst();
            }
        });
    }

    @Override
    public void onCarBeginsService(int pumpId, int carId) {
        Platform.runLater(() ->
                log("P" + pumpId + ": C" + carId + " begins service at Bay " + pumpId)
        );
    }

    @Override
    public void onCarFinishesService(int pumpId, int carId) {
        Platform.runLater(() -> {
            log("P" + pumpId + ": C" + carId + " finishes service");
            log("P" + pumpId + ": Bay " + pumpId + " is now free");
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
             createCarCard();
        });
    }

    @Override
    public void onException(String message) {
        Platform.runLater(() -> log(message));
    }
}