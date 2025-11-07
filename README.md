# Car Wash Simulator

A multithreaded Java simulation that models a busy car wash and gas station, demonstrating the Producer–Consumer problem using semaphores, mutex locks, and a bounded queue to coordinate car arrivals and pump operations.

## Overview

The simulation represents a service station where multiple pumps (consumers) work concurrently to serve arriving cars (producers). Each car enters a shared waiting area and is assigned to an available pump when possible. Synchronization is handled using semaphores and a mutex lock to avoid race conditions when accessing shared resources.

The GUI, built with JavaFX, visually reflects the real-time state of the simulation. Cars can be added dynamically, and the interface continuously updates to display the system’s status and activity log during execution.

## Screenshot

![Car Wash Simulator GUI](https://github.com/user-attachments/assets/f0a3aeb8-643d-4996-b0b7-5d70bbf277ee)

## Multithreading Logic

- **Producers (Cars):** Each car is represented as a thread that attempts to enter the waiting queue. If the queue is full, it waits until space becomes available.
- **Consumers (Pumps):** Each pump runs as a separate thread that waits for cars to arrive. When a car becomes available, the pump acquires the necessary semaphores and begins servicing the car.
- **Semaphores and Mutex:**  
  - `full` and `empty` semaphores control access to the bounded queue.  
  - A `mutex` ensures exclusive access to shared data structures during queue operations.  
  - A `pumps` semaphore limits the number of cars being serviced simultaneously.

This design guarantees correct synchronization, preventing deadlocks and maintaining consistent state across threads.

## How to Run

### Prerequisites
- **JDK 23** or later
- **Maven 3.9+**
- Internet connection (for downloading dependencies)

### Installation & Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/adhamhamdy3/car-wash-sim.git
   cd car-wash-sim
   ```

2. Run the simulation using Maven:
   ```bash
   mvn clean javafx:run
   ```

### Alternative: Running via IDE
1. Open the project in your Java IDE (e.g., IntelliJ IDEA)
2. Configure the JavaFX SDK in your project settings
3. Run the `Main` class to start the simulation
