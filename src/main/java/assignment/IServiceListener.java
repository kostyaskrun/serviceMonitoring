package assignment;

public interface IServiceListener {
    void serviceUp(String name, long timestamp);
    void serviceDown(String name, long timestamp);
}
