import java.util.Map;
import java.util.Queue;
import java.util.Random;

public class Pump extends Thread {
    private int id;
    private Queue<String> queue;
    private Semaphore mutex, empty, full, pumps;
    private String pumpTag;

    public Pump(int id, Queue<String> queue, Semaphore mutex, Semaphore empty, Semaphore full, Semaphore pumps) {
        this.id = id;
        this.queue = queue;
        this.mutex = mutex;
        this.empty = empty;
        this.full = full;
        this.pumps = pumps;

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
                System.out.println(pumpTag + ": " + car + " login");

                mutex.release();
                pumps.acquire(); // acquire a service bay

                System.out.println(pumpTag + ": " + car + " begins service at Bay " + id);
                Thread.sleep((int) (Math.random() * 2000) + 1000); // simulate work

                System.out.println(pumpTag + ": " + car + " finishes service");
                System.out.println(pumpTag + ": Bay " + id + " is now free");

                pumps.release(); // release the bay
                empty.release(); // signal empty space in queue
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
