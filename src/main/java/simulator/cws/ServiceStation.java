package simulator.cws;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ServiceStation {
    private Queue<Car> queue;
    private Semaphore mutex, empty, full, pumps;

    private List<Pump> pumpsList;
    private List<Car> carsList;
    private int waitingAreaSize;

    private int numPumps;
    private int carCounter = 0;

    private volatile boolean running = false;

    public ServiceStation(int waitingAreaSize, int numPumps) {
        if (waitingAreaSize < 0) {
            throw new IllegalArgumentException("waitingAreaSize must be >= 0");
        }
        if (numPumps <= 0) {
            throw new IllegalArgumentException("numPumps must be > 0");
        }

        this.queue = new LinkedList<>();
        this.mutex = new Semaphore(1);
        this.empty = new Semaphore(waitingAreaSize);
        this.full = new Semaphore(0);
        this.pumps = new Semaphore(numPumps);

        this.pumpsList = new ArrayList<>();
        this.carsList = new ArrayList<>();

        this.numPumps = numPumps;
        this.waitingAreaSize = waitingAreaSize;
    }

    // Called from controller when user clicks "Start Simulation"
    public void startSimulation(int pumpSpeed, PumpObserver pumpObserver) {
        running = true;

        // create and start pump threads
        for (int i = 1; i <= numPumps; i++) {
            Pump pump = new Pump(i, queue, mutex, empty, full, pumps, pumpSpeed);
            pump.addObserver(pumpObserver);
            pumpsList.add(pump);
            pump.start();
        }
    }

    // Called from controller when user clicks "Add Car"
    public void addCar(CarObserver carObserver) {
        carCounter++;

        Car car = new Car(carCounter, queue, mutex, empty, full);
        car.addObserver(carObserver);
        carsList.add(car);
        car.start();
    }

    // Stop simulation and try to stop threads cleanly
    public void stopSimulation() {
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
    }

    public void reset() {
        stopSimulation();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        queue.clear();
        pumpsList.clear();
        carsList.clear();

        carCounter = 0;
        running = false;

        mutex = new Semaphore(1);
        empty = new Semaphore(waitingAreaSize);
        full = new Semaphore(0);
        pumps = new Semaphore(numPumps);
    }

    public boolean isRunning() {
        return running;
    }

    public int getWaitingAreaSize() {
        return waitingAreaSize;
    }

    public int getCarCounter() {
        return carCounter;
    }

    public int getWaitingCars() {
        return queue.size();
    }

    public int getServicedCars() {
        return numPumps - pumps.availablePermits();
    }
}
