package simulator.cws;

public interface PumpObserver {
    void onCarLogins(int pumpId, int carId);
    void onCarBeginsService(int pumpId, int carId);
    void onCarFinishesService(int pumpId, int carId);
    void onException(String message);
}
