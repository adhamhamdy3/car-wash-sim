package simulator.cws;

public interface PumpObserver {
    void onCarLogins(int pumpId, String carTag);
    void onCarBeginsService(int pumpId, String carTag);
    void onCarFinishesService(int pumpId, String carTag);
    void onException(String message);
}
