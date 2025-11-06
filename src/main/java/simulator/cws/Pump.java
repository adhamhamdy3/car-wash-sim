package simulator.cws;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Pump extends Thread {
    private final int id;
    private final Queue<String> queue;
    private final Semaphore mutex, empty, full, pumps;
    private final int pumpSpeed; // in seconds

    private final List<PumpObserver> observers;

    private final String pumpTag;

    public Pump(int id, Queue<String> queue, Semaphore mutex,
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

        this.pumpTag = "Pump " + id;
    }

    public void addObserver(PumpObserver observer) {
        observers.add(observer);
    }

    private void notifyOnCarLogins(String carTag) {
        for (PumpObserver observer : observers) {
            observer.onCarLogins(id, carTag);
        }
    }

    private void notifyOnCarBeginsService(String carTag) {
        for (PumpObserver observer : observers) {
            observer.onCarBeginsService(id, carTag);
        }
    }

    private void notifyOnCarFinishesService(String carTag) {
        for (PumpObserver observer : observers) {
            observer.onCarFinishesService(id, carTag);
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
                // wait until there is at least one car
                full.acquire();
                mutex.acquire();

                if (queue.isEmpty()) {
                    // another pump might have taken it already
                    mutex.release();
                    continue;
                }

                String car = queue.remove();
                notifyOnCarLogins(car);

                mutex.release();
                pumps.acquire(); // acquire a service bay

                notifyOnCarBeginsService(car);

                Thread.sleep( pumpSpeed * 1000L); // simulate service time

                notifyOnCarFinishesService(car);

                pumps.release(); // release the bay
                empty.release(); // signal empty space in queue
            }
        } catch (InterruptedException e) {
            notifyOnException(pumpTag + " shutting down...");
            Thread.currentThread().interrupt();
        }
    }
}
