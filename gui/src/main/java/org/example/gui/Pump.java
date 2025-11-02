package org.example.gui;

import java.util.Queue;

public class Pump extends Thread {
    private int id;
    private Queue<String> queue;
    private Semaphore mutex, empty, full, pumps;
    private String pumpTag;
    private CarWashSimulatorController controller;

    public Pump(int id, Queue<String> queue, Semaphore mutex, Semaphore empty, Semaphore full, Semaphore pumps, CarWashSimulatorController controller) {
        this.id = id;
        this.queue = queue;
        this.mutex = mutex;
        this.empty = empty;
        this.full = full;
        this.pumps = pumps;
        this.controller = controller;

        this.pumpTag = "Pump " + this.id;
    }

    @Override
    public void run() {
        try {
            while (true) {
                full.acquire(); // wait for cars
                mutex.acquire(); // lock queue access

                if (queue.isEmpty()) {
                    mutex.release();
                    continue;
                }

                String car = queue.remove();
                controller.log(pumpTag + ": " + car + " login");

                mutex.release();
                pumps.acquire(); // acquire a service bay

                controller.log(pumpTag + ": " + car + " begins service at Bay " + id);
                Thread.sleep((int) (Math.random() * 2000) + 2500); // simulate work

                controller.log(pumpTag + ": " + car + " finishes service");
                controller.log(pumpTag + ": Bay " + id + " is now free");

                pumps.release(); // release the bay
                empty.release(); // signal empty space in queue
            }

        } catch (InterruptedException e) {
            controller.log(pumpTag + " shutting down...");
        }
    }
}