import java.util.Queue;

public class Car extends Thread {
    private int id;
    private Queue<String> queue;
    private Semaphore mutex, empty, full;
    private String carTag;

    public Car(int id, Queue<String> queue, Semaphore mutex, Semaphore empty, Semaphore full) {
        this.id = id;
        this.queue = queue;
        this.mutex = mutex;
        this.empty = empty;
        this.full = full;
        this.carTag = "C" + this.id;
    }

    @Override
    public void run() {
        try {
            System.out.println(carTag + " arrived");

            empty.acquire(); // wait for empty space
            mutex.acquire(); // lock access to queue

            queue.add(carTag);
            System.out.println(carTag + " entered the queue");

            mutex.release(); // release access
            full.release(); // signal that an item is available

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
