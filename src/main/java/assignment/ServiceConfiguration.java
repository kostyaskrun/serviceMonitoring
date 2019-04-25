package assignment;

import java.util.ArrayList;
import java.util.List;

public class ServiceConfiguration {
    private String host;
    private int port;
    private String name;

    private boolean isRunning;
    private int queryInterval;
    private int graceInterval;

    private long lastRunTimestamp;

    private long outageStartTime;
    private long outageEndTime;

    private ServiceState serviceState = new ServiceState();

    private List<IServiceListener> listeners = new ArrayList<>();

    ServiceConfiguration(String host,
                         int port,
                         String name,
                         boolean isRunning,
                         int queryInterval,
                         int graceInterval) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.isRunning = isRunning;
        this.queryInterval = queryInterval;
        this.graceInterval = graceInterval;
    }

    void setOutageStartTime(long outageStartTime) {
        this.outageStartTime = outageStartTime;
    }

    void setOutageEndTime(long outageEndTime) {
        this.outageEndTime = outageEndTime;
    }

    String getHost() {
        return host;
    }

    void setHost(String host) {
        this.host = host;
    }

    int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    void setQueryInterval(int queryInterval) {
        this.queryInterval = queryInterval;
    }

    int getGraceInterval() {
        return graceInterval;
    }

    void setGraceInterval(int graceInterval) {
        this.graceInterval = graceInterval;
    }

    long getLastRunTimestamp() {
        return lastRunTimestamp;
    }

    void setLastRunTimestamp(long lastRunTimestamp) {
        this.lastRunTimestamp = lastRunTimestamp;
    }

    ServiceState getServiceState() {
        return serviceState;
    }

    /**
     * If the grace time is less than the polling frequency, the monitor should schedule extra checks of the service.
     */
    synchronized int getQueryInterval() {
        if (graceInterval < queryInterval) {
            return graceInterval;
        } else {
            return queryInterval;
        }
    }

    synchronized boolean isInOutage(final long currentTimestamp) {
        if(outageStartTime > 0 && outageEndTime > 0) {
            return currentTimestamp >= outageStartTime && currentTimestamp <= outageEndTime;
        }
        return false;
    }

    synchronized void addListener(IServiceListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    synchronized void removeListener(IServiceListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    synchronized void notifyListenersThatServiceIsUp(long timestamp) {
        for (IServiceListener listener : listeners) {
            listener.serviceUp(name, timestamp);
        }
    }

    synchronized void notifyListenersThatServiceIsDown(long timestamp) {
        for (IServiceListener listener : listeners) {
            listener.serviceDown(name, timestamp);
        }
    }
}
