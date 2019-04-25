package assignment;

import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MonitorWorker implements Runnable {
    private ServiceConfiguration configuration;
    private Logger logger;

    private void connected() {
        logger.log(Level.INFO, String.format("Connection established for service: %s", configuration.getName()));
        long timestamp = System.currentTimeMillis();
        configuration.setLastRunTimestamp(timestamp);
        boolean isStateChanged = configuration.getServiceState().markAsRunning(timestamp, configuration.getGraceInterval());
        if (isStateChanged) {
            configuration.notifyListenersThatServiceIsUp(timestamp);
        }
    }

    private void notConnected() {
        logger.log(Level.INFO, String.format("Connection not established for service: %s", configuration.getName()));
        long timestamp = System.currentTimeMillis();
        configuration.setLastRunTimestamp(timestamp);
        boolean isStateChanged = configuration.getServiceState().markAsNotRunning(timestamp, configuration.getGraceInterval());
        if (isStateChanged) {
            configuration.notifyListenersThatServiceIsDown(timestamp);
        }
    }

    public void run() {
        try (Socket socket = new Socket(configuration.getHost(), configuration.getPort())) {
            if (socket.isConnected()) {
                connected();
            } else {
                notConnected();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception occurred in tcp connection method: ", e);
            notConnected();
        }
    }

    MonitorWorker(String name) {
        logger = Logger.getLogger(this.getClass().getSimpleName());
        configuration = Configurator.getInstance().getConfiguration(name);
    }
}