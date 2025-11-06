package simulator.cws.utlils;

public interface CarObserver {
    void onCarArrives(int carId);
    void onCarEntersQueue(int carId);
    void onException(String message);
}
