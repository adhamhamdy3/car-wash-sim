package simulator.cws;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

public class CarWashSimulatorController {

    @FXML private TextField capacityField;
    @FXML private TextField pumpsField;
    @FXML private Button startBtn;
    @FXML private Button stopBtn;
    @FXML private Button resetBtn;
    @FXML private Button addCarButton; // "Add Car"
    @FXML private Button clearLogBtn;
    @FXML private TextArea logArea;
    // the stats part
    @FXML private Label arrivedLabel;
    @FXML private Label servicedLabel;
    @FXML private Label waitingLabel;
    private int totalArrived = 0;
    private int totalServiced = 0;
    private int totalWaiting = 0;
    private Queue<Integer> waitingQueue = new LinkedList<>();
    private Queue<String> queue;
    private Semaphore mutex, empty, full, pumps;

    private int waitingAreaSize;
    private int numPumps;

    private final AtomicInteger carCounter = new AtomicInteger(1);
    private boolean simulationRunning = false;

    private List<Pump> pumpThreads = new ArrayList<>();
    //cars queue
    private int carImageIndex = 1;
    @FXML private FlowPane queueContainer;

    //pumps cards
    @FXML private FlowPane pumpsContainer;

    @FXML
    public void initialize() {
        // Hook up buttons
        startBtn.setOnAction(e -> startSimulation());
        addCarButton.setOnAction(e -> addCar());
        resetBtn.setOnAction(e -> resetSimulation());
        stopBtn.setOnAction(e -> stopSimulation());
        clearLogBtn.setOnAction(e -> clearLog());
    }

    private void startSimulation() {
        try {
            waitingAreaSize = Integer.parseInt(capacityField.getText());
            numPumps = Integer.parseInt(pumpsField.getText());
        } catch (NumberFormatException e) {
            log("Please enter valid numbers for capacity and pumps.");
            return;
        }

        queue = new LinkedList<>();
        mutex = new Semaphore(1);
        empty = new Semaphore(waitingAreaSize);
        full = new Semaphore(0);
        pumps = new Semaphore(numPumps);

        simulationRunning = true;
        log("Simulation started with waiting capacity " + waitingAreaSize + " and " + numPumps + " pumps.");

        //visual changes to pump cards
        Platform.runLater(() -> {
            pumpsContainer.getChildren().clear();
            for (int i = 1; i <= numPumps; i++) {
                VBox pumpCard = new VBox(5);
                pumpCard.getStyleClass().add("pump-card");
                pumpCard.setPrefWidth(210);
                pumpCard.setPrefHeight(150);
                pumpCard.setStyle("-fx-alignment: center; -fx-background-color: #f7f7f7; -fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 10; -fx-background-radius: 10;");

                // Pump image
                ImageView pumpImage = new ImageView(new Image(getClass().getResource("/simulator/cws/assets/pump.png").toExternalForm()));
                pumpImage.setFitWidth(80);
                pumpImage.setFitHeight(80);

                // Light indicator (start as green)
                ImageView lightImage = new ImageView(new Image(getClass().getResource("/simulator/cws/assets/green.png").toExternalForm()));
                lightImage.setFitWidth(48);
                lightImage.setFitHeight(19);

                // Countdown label
                Label countdownLabel = new Label("0s");
                countdownLabel.getStyleClass().add("countdown-label");

                // Pump label
                Label pumpLabel = new Label("Pump " + i);
                pumpLabel.getStyleClass().add("pump-label");

                // Layout: light + countdown on same row
                HBox topBar = new HBox(5, lightImage, countdownLabel);
                topBar.setStyle("-fx-alignment: center;");

                pumpCard.getChildren().addAll(topBar, pumpImage, pumpLabel);
                pumpsContainer.getChildren().add(pumpCard);
            }
        });
        // start pump threads
        for (int i = 1; i <= numPumps; i++) {
            Pump p = new Pump(i, queue, mutex, empty, full, pumps, this);
            pumpThreads.add(p);
            p.start();
        }

        startBtn.setDisable(true);
        addCarButton.setDisable(false);
        stopBtn.setDisable(false);
    }

    private void addCar() {
        if (!simulationRunning) {
            log("Start the simulation first!");
        }
        else if(totalWaiting >= waitingAreaSize) {
            addCarButton.setDisable(true);
            log("Reached maximum capacity.");
        }
        else{
            int carId = carCounter.getAndIncrement();
            Car c = new Car(carId, queue, mutex, empty, full, this);
            c.start();
            Platform.runLater(() -> {
                // Create a VBox to hold the car image and label
                VBox carBox = new VBox(5);
                carBox.setAlignment(javafx.geometry.Pos.CENTER);

                // Cycle through 1.png–8.png for car images
                int imageNumber = (carImageIndex - 1) % 8 + 1;
                String imagePath = "/simulator/cws/assets/" + imageNumber + ".png";
                carImageIndex++;

                // Load the car image
                ImageView carImage = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
                carImage.setFitWidth(90);
                carImage.setFitHeight(90);

                // Label under the car image (C1, C2, ...)
                Text carLabel = new Text("C" + carId);

                carBox.getChildren().addAll(carImage, carLabel);

                // Add the car box to the FlowPane queueContainer
                queueContainer.getChildren().add(carBox);
            });
        }

    }

    private void stopSimulation() {
        simulationRunning = false;
        for (Pump p : pumpThreads) {
            p.interrupt();
        }
        log("Simulation stopped manually.");
        stopBtn.setDisable(true);
        addCarButton.setDisable(true);
    }

    private void resetSimulation() {
        simulationRunning = false;
        queue = null;
        pumpThreads.clear();
        logArea.clear();
        queueContainer.getChildren().clear();
        carCounter.set(1);
        startBtn.setDisable(false);
        addCarButton.setDisable(true);
        stopBtn.setDisable(true);
        totalArrived = 0;
        totalServiced = 0;
        totalWaiting = 0;
        log("Simulation reset.");
    }
    private void clearLog(){
        logArea.clear();
    }
    // Helper to safely log from any thread
    public void log(String message) {
        Platform.runLater(() -> {
            logArea.appendText("• " + message + "\n");

            if (message.contains("arrived")) {
                if(totalWaiting == waitingAreaSize){
                    addCarButton.setDisable(true);
                }
                totalArrived++;
                totalWaiting++;
            }
            if (message.contains("begins service")) {
                totalWaiting--;
                if(!queueContainer.getChildren().isEmpty()){
                    queueContainer.getChildren().removeFirst();
                }
                if(totalWaiting < waitingAreaSize){
                    addCarButton.setDisable(false);
                }
            }
            if (message.contains("finishes service")) {
                totalServiced++;
            }

            arrivedLabel.setText("Total cars arrived: " + totalArrived);
            servicedLabel.setText("Cars serviced: " + totalServiced);
            waitingLabel.setText("Cars waiting: " + totalWaiting);
        });
    }

}