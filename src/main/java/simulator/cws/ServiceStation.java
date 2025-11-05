package simulator.cws;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public class ServiceStation {
    private final Queue<String> queue;
    private final Semaphore mutex;
    private final Semaphore empty;
    private final Semaphore full;
    private final Semaphore pumps;

    private final List<Pump> pumpsList;
    private final List<Car> carsList;

    private int numPumps;
    private int waitingAreaSize;
    private int carCounter = 0;

    private final Consumer<String> logCallback;

    private volatile boolean running = false;

    public ServiceStation(int waitingAreaSize, int numPumps, Consumer<String> logCallback) {
        if (waitingAreaSize < 0) {
            throw new IllegalArgumentException("waitingAreaSize must be >= 0");
        }
        if (numPumps <= 0) {
            throw new IllegalArgumentException("numPumps must be > 0");
        }
        if (logCallback == null) {
            throw new IllegalArgumentException("logCallback must not be null");
        }

        this.queue = new LinkedList<>();
        this.mutex = new Semaphore(1);
        this.empty = new Semaphore(waitingAreaSize);
        this.full = new Semaphore(0);
        this.pumps = new Semaphore(numPumps);

        this.pumpsList = new ArrayList<>();
        this.carsList = new ArrayList<>();

        this.numPumps = numPumps;
        this.logCallback = logCallback;
        this.waitingAreaSize = waitingAreaSize;
    }

    // Called from controller when user clicks "Start Simulation"
    public void startSimulation() {
        if (running) {
            logCallback.accept("Simulation already running.");
            return;
        }
        running = true;

        logCallback.accept("Simulation started: Waiting area capacity " + empty.availablePermits()
                + ", Pumps = " + numPumps);

        // create and start pump threads
        for (int i = 1; i <= numPumps; i++) {
            Pump pump = new Pump(i, queue, mutex, empty, full, pumps, logCallback);
            pumpsList.add(pump);
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

        Car car = new Car(carCounter, queue, mutex, empty, full, logCallback);
        carsList.add(car);
        car.start();
    }

    // Stop simulation and try to stop threads cleanly
    public void stopSimulation() {
        if (!running) {
            logCallback.accept("Simulation is not running.");
            return;
        }
        running = false;

        // attempt to stop cars
        for (Car car : carsList) {
            try {
                car.interrupt();
            } catch (Exception ignored) {}
        }

        // attempt to stop pumps
        for (Pump pump : pumpsList) {
            try {
                pump.interrupt();
            } catch (Exception ignored) {}
        }

        logCallback.accept("Simulation stopped.");
    }

    public void setRunning(boolean flag) {
        running = flag;
    }

    public void reset() {
        stopSimulation();
        queue.clear();

        mutex.setPermits(1);
        empty.setPermits(waitingAreaSize);
        full.setPermits(0);
        pumps.setPermits(numPumps);

        pumpsList.clear();
        carsList.clear();

        numPumps = 0;
    }

    // Accessors - return unmodifiable views to avoid external mutation
    public Queue<String> getQueue() {
        return queue;
    }

    public List<Pump> getPumpsList() {
        return Collections.unmodifiableList(pumpsList);
    }

    public List<Car> getCarsList() {
        return Collections.unmodifiableList(carsList);
    }

    public boolean isRunning() {
        return running;
    }

    public int getNumPumps() {
        return numPumps;
    }

    public int getCarCounter() {
        return carCounter;
    }

    public int getWaitingAreaSize() {
        return empty.availablePermits() + full.availablePermits(); // total capacity
    }

    public int getTotalWaiting() {
        return full.availablePermits(); // or track with AtomicInteger
    }
}
