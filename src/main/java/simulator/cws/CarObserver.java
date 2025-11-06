package simulator.cws;

public interface CarObserver {
    void onCarArrives(String carTag);
    void onCarEntersQueue(String carTag);
    void onException(String message);
}
