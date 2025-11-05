package simulator.cws;

import java.util.Queue;
import java.util.function.Consumer;

public class Pump extends Thread {
    private final int id;
    private final Queue<String> queue;
    private final Semaphore mutex, empty, full, pumps;
    private final Consumer<String> logCallback;

    private final String pumpTag;

    public Pump(int id, Queue<String> queue, Semaphore mutex,
                Semaphore empty, Semaphore full, Semaphore pumps,
                Consumer<String> logCallback) {
        this.id = id;
        this.queue = queue;
        this.mutex = mutex;
        this.empty = empty;
        this.full = full;
        this.pumps = pumps;
        this.logCallback = logCallback;
        this.pumpTag = "Pump " + id;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                // wait until there is at least one car
                full.acquire();

                mutex.acquire();
                if (queue.isEmpty()) {
                    // another pump might have taken it already
                    mutex.release();
                    continue;
                }

                String car = queue.remove();
                logCallback.accept(pumpTag + ": " + car + " login");

                mutex.release();
                pumps.acquire(); // acquire a service bay

                logCallback.accept(pumpTag + ": " + car + " begins service at Bay " + id);
                Thread.sleep((int) (Math.random() * 2000) + 1000); // simulate service time

                logCallback.accept(pumpTag + ": " + car + " finishes service");
                logCallback.accept(pumpTag + ": Bay " + id + " is now free");

                pumps.release(); // release the bay
                empty.release(); // signal empty space in queue
            }
        } catch (InterruptedException e) {
            logCallback.accept(pumpTag + " shutting down...");
            Thread.currentThread().interrupt();
        }
    }
}
