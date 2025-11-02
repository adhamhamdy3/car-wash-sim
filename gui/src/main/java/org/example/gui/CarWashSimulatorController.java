package org.example.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
            return;
        }

        int carId = carCounter.getAndIncrement();
        Car c = new Car(carId, queue, mutex, empty, full, this);
        c.start();
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
                totalArrived++;
                totalWaiting++;
            }
            if (message.contains("begins service")) {
                totalWaiting--;
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