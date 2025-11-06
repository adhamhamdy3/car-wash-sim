package simulator.cws;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Car extends Thread {
    private int id;
    private Queue<Car> queue;
    private Semaphore mutex, empty, full;

    private List<CarObserver> observers;

    public Car(int id, Queue<Car> queue, Semaphore mutex, Semaphore empty, Semaphore full) {
        this.id = id;
        this.queue = queue;
        this.mutex = mutex;
        this.empty = empty;
        this.full = full;
        this.observers = new ArrayList<>();
    }

    public void addObserver(CarObserver observer) {
        observers.add(observer);
    }

    private void notifyOnCarArrives(int carId) {
        for (CarObserver o : observers) o.onCarArrives(carId);
    }

    private void notifyOnCarEntersQueue(int carId) {
        for (CarObserver o : observers) o.onCarEntersQueue(carId);
    }

    private void notifyOnException(String message) {
        for (CarObserver o : observers) o.onException(message);
    }

    @Override
    public void run() {
        try {
            notifyOnCarArrives(id);

            empty.acquire(); // wait for empty space
            mutex.acquire(); // lock access to queue

            queue.add(this);
            notifyOnCarEntersQueue(id);

            mutex.release(); // release access
            full.release(); // signal that an item is available

        } catch (InterruptedException e) {
            notifyOnException("C" + id + " interrupted and leaving the station...");
            Thread.currentThread().interrupt(); // restore interrupt status
        } catch (Exception e) {
            notifyOnException("C" + id + " encountered an error: " + e.getMessage());
        }
    }

    public int getCarId() {
        return id;
    }

    public String getTag() {
        return "C" + id;
    }
}