package simulator.cws;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Car extends Thread {
    private final int id;
    private final Queue<String> queue;
    private final Semaphore mutex, empty, full;
    private final String carTag;

    private final List<CarObserver> observers;

    public Car(int id, Queue<String> queue, Semaphore mutex, Semaphore empty, Semaphore full) {
        this.id = id;
        this.queue = queue;
        this.mutex = mutex;
        this.empty = empty;
        this.full = full;
        this.observers = new ArrayList<>();

        this.carTag = "C" + this.id;
    }

    public void addObserver(CarObserver observer) {
        observers.add(observer);
    }

    private void notifyOnCarArrives(String carTag) {
        for (CarObserver o : observers) o.onCarArrives(carTag);
    }

    private void notifyOnCarEntersQueue(String carTag) {
        for (CarObserver o : observers) o.onCarEntersQueue(carTag);
    }

    private void notifyOnException(String message) {
        for (CarObserver o : observers) o.onException(message);
    }

    @Override
    public void run() {
        try {
            notifyOnCarArrives(carTag);

            empty.acquire(); // wait for empty space
            mutex.acquire(); // lock access to queue

            queue.add(carTag);
            notifyOnCarEntersQueue(carTag);

            mutex.release(); // release access
            full.release(); // signal that an item is available

        } catch (InterruptedException e) {
            notifyOnException(carTag + " interrupted and leaving the station...");
            Thread.currentThread().interrupt(); // restore interrupt status
        } catch (Exception e) {
            notifyOnException(carTag + " encountered an error: " + e.getMessage());
        }
    }
}