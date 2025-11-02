package simulator.cws;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class ServiceStation {
    private final Queue<String> queue;
    private final Semaphore mutex;
    private final Semaphore empty;
    private final Semaphore full;
    private final Semaphore pumps;
    private final int numPumps;
    private final Consumer<String> logCallback;

    private int carCounter = 0;
    private boolean running = false;

    public ServiceStation(int waitingAreaSize, int numPumps, Consumer<String> logCallback) {
        this.queue = new LinkedList<>();
        this.mutex = new Semaphore(1);
        this.empty = new Semaphore(waitingAreaSize);
        this.full = new Semaphore(0);
        this.pumps = new Semaphore(numPumps);
        this.numPumps = numPumps;
        this.logCallback = logCallback;
    }

    // Called from controller when user clicks "Start Simulation"
    public void startSimulation() {
        if (running) return;
        running = true;

        logCallback.accept("Simulation started: Waiting area capacity " + empty.availablePermits() +
                ", Pumps = " + numPumps);

        for (int i = 1; i <= numPumps; i++) {
            Pump pump = new Pump(i, queue, mutex, empty, full, pumps, (CarWashSimulatorController) logCallback);
            pump.start();
        }
    }

    // Called from controller when user clicks "Add Car"
    public void addCar() {
        if (!running) {
            logCallback.accept("Please start the simulation first!");
            return;
        }

        carCounter++;
        String carTag = "C" + carCounter;
        logCallback.accept(carTag + " arrived");

        Car car = new Car(carCounter, queue, mutex, empty, full, (CarWashSimulatorController) logCallback);
        car.start();
    }

    public void stopSimulation() {
        running = false;
        logCallback.accept("Simulation stopped.");
    }
}