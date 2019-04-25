package assignment;

public class Main {

    public static void main(String argv[]) {
        final String FIRST_SERVICE = "first";
        final String SECOND_SERVICE = "second";
        final String THIRD_SERVICE = "third";

        MonitorServer server = new MonitorServer();
        server.configurator.addConfiguration(new ServiceConfiguration("www.google.com",80, FIRST_SERVICE, true, 1, 3));
        server.configurator.addConfiguration(new ServiceConfiguration("www.amazon.com",80, SECOND_SERVICE, true, 1, 3));
        server.configurator.addConfiguration(new ServiceConfiguration("www.github.com", 80, THIRD_SERVICE, true, 1, 3));

        server.start();
    }
}
