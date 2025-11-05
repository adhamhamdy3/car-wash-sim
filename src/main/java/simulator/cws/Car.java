package simulator.cws;

import java.util.Queue;
import java.util.function.Consumer;

public class Car extends Thread {
    private final int id;
    private final Queue<String> queue;
    private final Semaphore mutex, empty, full;
    private final String carTag;
    private final Consumer<String> logCallback;



    public Car(int id, Queue<String> queue, Semaphore mutex, Semaphore empty, Semaphore full, Consumer<String> logCallback) {
        this.id = id;
        this.queue = queue;
        this.mutex = mutex;
        this.empty = empty;
        this.full = full;
        this.logCallback = logCallback;

        this.carTag = "C" + this.id;
    }

    @Override
    public void run() {
        try {
            logCallback.accept(carTag + " arrived");

            empty.acquire(); // wait for empty space
            mutex.acquire(); // lock access to queue

            queue.add(carTag);
            logCallback.accept(carTag + " entered the queue");

            mutex.release(); // release access
            full.release(); // signal that an item is available

        } catch (InterruptedException e) {
            logCallback.accept(carTag + " interrupted and leaving the station...");
            Thread.currentThread().interrupt(); // restore interrupt status
        } catch (Exception e) {
            logCallback.accept(carTag + " encountered an error: " + e.getMessage());
        }
    }
}