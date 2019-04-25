package assignment;

class ServiceState {

    private long stateTimestamp;
    private State state = State.UNKNOWN;

    State getState() {
        return state;
    }

    /**
     * @param timestamp of the event
     * @param graceInterval grace period
     * @return true if server was disabled, and now it's working on
     */
    boolean markAsRunning(final long timestamp, final int graceInterval) {
        switch (state) {
            case UNKNOWN:
                state = State.RUNNING;
                stateTimestamp = System.currentTimeMillis();
                return false;

            case NOT_RUNNING:
                if (isTimeToChangeStatus(timestamp, graceInterval)) {
                    state = State.RUNNING;
                    stateTimestamp = timestamp;
                    return true;
                }
                return false;

            case RUNNING:
                throw new IllegalStateException("You cannot mark the server as running, because this server is still running");
            default:
                return false;
        }
    }

    /**
     * @param timestamp of the event
     * @param graceInterval grace period
     * @return true if server was enabled, and now it's off
     */
    boolean markAsNotRunning(final long timestamp, final int graceInterval) {
        switch (state) {
            case UNKNOWN:
                state = State.NOT_RUNNING;
                stateTimestamp = System.currentTimeMillis();
                return false;

            case RUNNING:
                if (isTimeToChangeStatus(timestamp, graceInterval)) {
                    state = State.NOT_RUNNING;
                    stateTimestamp = timestamp;
                    return true;
                }
                return false;

            case NOT_RUNNING:
                throw new IllegalStateException("You cannot mark the server as not running, because this server is already stopped");
            default:
                return false;
        }
    }

    private boolean isTimeToChangeStatus(final long timestamp, final int secondsAfter) {
        return stateTimestamp + secondsAfter * 1000 <= timestamp;
    }
}
