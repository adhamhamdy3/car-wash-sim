package simulator.cws;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Pump extends Thread {
    private int id;
    private Queue<Car> queue;
    private Semaphore mutex, empty, full, pumps;
    private int pumpSpeed; // in seconds

    private List<PumpObserver> observers;

    public Pump(int id, Queue<Car> queue, Semaphore mutex,
                Semaphore empty, Semaphore full, Semaphore pumps,
                int pumpSpeed) {
        this.id = id;
        this.queue = queue;
        this.mutex = mutex;
        this.empty = empty;
        this.full = full;
        this.pumps = pumps;
        this.pumpSpeed = pumpSpeed;
        this.observers = new ArrayList<>();
    }

    public void addObserver(PumpObserver observer) {
        observers.add(observer);
    }

    private void notifyOnCarLogins(int carId) {
        for (PumpObserver observer : observers) {
            observer.onCarLogins(id, carId);
        }
    }

    private void notifyOnCarBeginsService(int carId) {
        for (PumpObserver observer : observers) {
            observer.onCarBeginsService(id, carId);
        }
    }

    private void notifyOnCarFinishesService(int carId) {
        for (PumpObserver observer : observers) {
            observer.onCarFinishesService(id, carId);
        }
    }

    private void notifyOnException(String message) {
        for (PumpObserver observer : observers) {
            observer.onException(message);
        }
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                // wait for a car to be available
                full.acquire();

                mutex.acquire();

                if (queue.isEmpty()) {
                    mutex.release();
                    continue;
                }

                Car car = queue.remove();
                notifyOnCarLogins(car.getCarId());
                mutex.release();
                empty.release();

                // acquire a pump bay
                pumps.acquire();

                notifyOnCarBeginsService(car.getCarId());
                Thread.sleep(pumpSpeed * 1000L);
                notifyOnCarFinishesService(car.getCarId());

                pumps.release();
            }
        } catch (InterruptedException e) {
            notifyOnException(getTag() + " shutting down...");
            Thread.currentThread().interrupt();
        }
    }

    public int getPumpId() {
        return id;
    }

    public String getTag() {
        return "P" + id;
    }
}
