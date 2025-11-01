import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class ServiceStation {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter waiting area capacity (1–10): ");
        int waitingAreaSize = scanner.nextInt();

        System.out.print("Enter number of service bays (pumps): ");
        int numPumps = scanner.nextInt();

        Queue<String> queue = new LinkedList<>();

        Semaphore mutex = new Semaphore(1);
        Semaphore empty = new Semaphore(waitingAreaSize);
        Semaphore full = new Semaphore(0);
        Semaphore pumps = new Semaphore(numPumps);

        // start pumps
        for (int i = 1; i <= numPumps; i++) {
            new Pump(i, queue, mutex, empty, full, pumps).start();
        }

        // generate cars
        int carId = 1;
        while (carId <= 3) { // simulate 3 cars
            new Car(carId, queue, mutex, empty, full).start();
            carId++;
            try {
                Thread.sleep((int) (Math.random() * 1500));
            } catch (InterruptedException e) {}
        }
    }
}