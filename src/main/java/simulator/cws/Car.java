package simulator.cws;

import java.util.Queue;

public class Car extends Thread {
    private int id;
    private Queue<String> queue;
    private Semaphore mutex, empty, full;
    private String carTag;
    private CarWashSimulatorController controller;

    public Car(int id, Queue<String> queue, Semaphore mutex, Semaphore empty, Semaphore full, CarWashSimulatorController controller) {
        this.id = id;
        this.queue = queue;
        this.mutex = mutex;
        this.empty = empty;
        this.full = full;
        this.controller = controller;
        this.carTag = "C" + this.id;
    }

    @Override
    public void run() {
        try {
            controller.log(carTag + " arrived");

            empty.acquire(); // wait for empty space
            mutex.acquire(); // lock access to queue

            queue.add(carTag);
            controller.log(carTag + " entered the queue");

            mutex.release(); // release access
            full.release(); // signal that an item is available

        } catch (Exception e) {
            controller.log(carTag + " encountered an error: " + e.getMessage());
        }
    }
}