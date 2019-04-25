package assignment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MonitorServer extends Thread {

    //The monitor should not poll any service more frequently than once a second
    private static final int MAIN_THREAD_INTERVAL_MILLISECONDS = 1000;
    private boolean isThreadRunning = true;
    private Logger logger;

    Configurator configurator = Configurator.getInstance();

    MonitorServer() {
        logger = Logger.getLogger(this.getClass().getSimpleName());
        logger.log(Level.INFO, String.format("Monitoring Server is enabled at %s", System.currentTimeMillis()));
    }

    private ExecutorService threadPool = Executors.newCachedThreadPool();

    public void run() {
        try {
            while (isThreadRunning) {
                for (String serviceName : configurator.getServiceNames()) {
                    ServiceConfiguration configuration = configurator.getConfiguration(serviceName);
                    if (!isTimeToRun(configuration)) {
                        continue;
                    }

                    threadPool.submit(new MonitorWorker(serviceName));
                }

                Thread.sleep(MAIN_THREAD_INTERVAL_MILLISECONDS);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception occurred in run method: ", e);
        }

        logger.log(Level.INFO, String.format("Monitoring Server is disabled at %s", System.currentTimeMillis()));
    }

    void stopServer() {
        isThreadRunning = false;
    }

    private boolean isTimeToRun(ServiceConfiguration configuration) {
        long timestamp = System.currentTimeMillis();
        return configuration.isRunning()
                && !configuration.isInOutage(timestamp)
                && (timestamp >= configuration.getLastRunTimestamp() + configuration.getQueryInterval() * 1000);
    }


}
