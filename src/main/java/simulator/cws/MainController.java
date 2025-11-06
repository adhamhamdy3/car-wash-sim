package simulator.cws;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import simulator.cws.models.ServiceStation;
import simulator.cws.ui.CarCard;
import simulator.cws.ui.PumpCard;
import simulator.cws.utlils.CarObserver;
import simulator.cws.utlils.PumpObserver;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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

    private final Map<Integer, PumpCard> pumpCards = new HashMap<>();

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
            pumpCards.clear();

            for (int i = 1; i <= numPumps; i++) {
                PumpCard pumpCard = new PumpCard(i, speedSpinner.getValue());

                // Save references
                pumpCards.put(i, pumpCard);
                pumpsContainer.getChildren().add(pumpCard);
            }
        });
    }

    void createCarCard(int carId) {
        Platform.runLater(() -> {
            CarCard carCard = new CarCard(carId);

            // Add the car box to the FlowPane queueContainer
            queueContainer.getChildren().add(carCard);
        });
    }

    // When a pump starts servicing a car
    public void startServiceVisual(int pumpId, int carId) {
        Platform.runLater(() -> {
            removeCarCard(carId);

            PumpCard pumpCard = pumpCards.get(pumpId);
            if (pumpCard == null) return;
            pumpCard.setLightColor("red");
            pumpCard.startCD(speedSpinner.getValue());
            pumpCard.setCarImage(carId);

        });
    }

    // When a pump finishes servicing a car
    public void finishServiceVisual(int pumpId) {
        Platform.runLater(() -> {
            PumpCard pumpCard = pumpCards.get(pumpId);
            if (pumpCard == null) return;

            pumpCard.setLightColor("green");
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
            startServiceVisual(pumpId, carId);
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