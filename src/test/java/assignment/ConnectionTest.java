package assignment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static java.util.Objects.nonNull;
import static org.junit.Assert.assertEquals;

public class ConnectionTest {

    private final String SERVICE_NAME = "name";
    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    private MonitorServer server;

    private IServiceListener testListener = new IServiceListener() {
        @Override
        public void serviceUp(String name, long timestamp) {
            ServiceConfiguration configuration = server.configurator.getConfiguration(name);
            logger.info((String.format("Test Listener --- Name: %s, Host: %s, Port: %s, is Up at time: %s",
                    configuration.getName(),
                    configuration.getHost(),
                    configuration.getPort(),
                    String.valueOf(timestamp))));
        }

        @Override
        public void serviceDown(String name, long timestamp) {
            ServiceConfiguration configuration = server.configurator.getConfiguration(name);
            logger.info((String.format("Test Listener --- Name: %s, Host: %s, Port: %s, is DOWN at time: %s",
                    configuration.getName(),
                    configuration.getHost(),
                    configuration.getPort(),
                    String.valueOf(timestamp))));
        }
    };

    @Before
    public void setUp() {
        server = new MonitorServer();
    }

    @After
    public void tearDown() {
        if (nonNull(server)) {
            server.configurator.reset();
            server.stopServer();
            server = null;
        }
    }

    @Test
    public void testServerOutageBehavior() throws Exception {
        final long timestamp = System.currentTimeMillis();
        ServiceConfiguration configuration = new ServiceConfiguration("www.google.com", 80, SERVICE_NAME, true, 1, 3);
        configuration.setOutageStartTime(timestamp);
        configuration.setOutageEndTime(timestamp + 10 * 1000);
        server.configurator.addConfiguration(configuration);
        ServiceState serviceState = server.configurator.getConfiguration(SERVICE_NAME).getServiceState();

        //add listeners
        for (String name : server.configurator.getServiceNames()) {
            ServiceConfiguration sc = server.configurator.getConfiguration(name);
            sc.addListener(testListener);
        }

        server.start();

        //check if server state hasn't changed
        assertEquals(State.UNKNOWN, serviceState.getState());

        //sleep amount should be bigger than outage period, just to be sure that outage time is over
        Thread.sleep(11 * 1000);

        assertEquals(State.RUNNING, serviceState.getState());
    }

    @Test
    public void testTcpConnectionWithCorrectAddress() throws Exception {
        ServiceConfiguration configuration = new ServiceConfiguration("www.google.com", 80, SERVICE_NAME, true, 1, 3);
        server.configurator.addConfiguration(configuration);
        ServiceState serviceState = server.configurator.getConfiguration(SERVICE_NAME).getServiceState();

        //add listeners
        for (String name : server.configurator.getServiceNames()) {
            ServiceConfiguration sc = server.configurator.getConfiguration(name);
            sc.addListener(testListener);
        }

        //check if server has status Unknown
        assertEquals(State.UNKNOWN, serviceState.getState());

        server.start();

        Thread.sleep(10 * 1000);

        //check if server state has been changed to Running
        assertEquals(State.RUNNING, serviceState.getState());
    }

    @Test
    public void testTcpConnectionWithWrongAddress() throws Exception {
        ServiceConfiguration configuration = new ServiceConfiguration("www.randomWrongAddress.com", 80, SERVICE_NAME, true, 1, 3);
        server.configurator.addConfiguration(configuration);
        ServiceState serviceState = server.configurator.getConfiguration(SERVICE_NAME).getServiceState();

        //add listeners
        for (String name : server.configurator.getServiceNames()) {
            ServiceConfiguration sc = server.configurator.getConfiguration(name);
            sc.addListener(testListener);
        }

        //check if server has status Unknown
        assertEquals(State.UNKNOWN, serviceState.getState());

        server.start();

        Thread.sleep(10 * 1000);

        //check if server state has been changed to Not_Running
        assertEquals(State.NOT_RUNNING, serviceState.getState());
    }

    @Test
    public void testChangeHostAddressOfExistingService() throws Exception {
        ServiceConfiguration configuration = new ServiceConfiguration("www.google.com", 80, SERVICE_NAME, true, 1, 3);
        server.configurator.addConfiguration(configuration);
        ServiceState serviceState = server.configurator.getConfiguration(SERVICE_NAME).getServiceState();

        //add listeners
        for (String name : server.configurator.getServiceNames()) {
            ServiceConfiguration sc = server.configurator.getConfiguration(name);
            sc.addListener(testListener);
        }

        //check if server has status Unknown
        assertEquals(State.UNKNOWN, serviceState.getState());

        server.start();

        Thread.sleep(10 * 1000);

        //check if server state has been changed to Running
        assertEquals(State.RUNNING, serviceState.getState());

        ServiceConfiguration sc = server.configurator.getConfiguration(SERVICE_NAME);
        sc.setHost("www.randomWrongAddress.com");
        server.configurator.addConfiguration(sc);

        Thread.sleep(10 * 1000);

        //check if server state has been changed to Not_Running
        assertEquals(State.NOT_RUNNING, serviceState.getState());
    }

    @Test(expected = IllegalStateException.class)
    public void testStopServerThatAlreadyStopped() {
        ServiceConfiguration configuration = new ServiceConfiguration("www.first-service.com", 80, SERVICE_NAME, true, 1, 3);
        server.configurator.addConfiguration(configuration);
        ServiceConfiguration serviceConfiguration = server.configurator.getConfiguration(SERVICE_NAME);
        ServiceState serviceState = serviceConfiguration.getServiceState();

        //check if service has Unknown status
        assertEquals(serviceState.getState(), State.UNKNOWN);

        //mark Service as Not_Running
        serviceState.markAsNotRunning(System.currentTimeMillis(), 0);

        //check if service status has been changed to Not_Running
        assertEquals(serviceState.getState(), State.NOT_RUNNING);

        //Try to mark service as Not_Running again
        serviceConfiguration.getServiceState().markAsNotRunning(System.currentTimeMillis(), 0);
    }

    @Test(expected = IllegalStateException.class)
    public void testRunServerThatAlreadyRunning() {
        ServiceConfiguration configuration = new ServiceConfiguration("www.first-service.com", 80, SERVICE_NAME, true, 1, 3);
        server.configurator.addConfiguration(configuration);
        ServiceConfiguration serviceConfiguration = server.configurator.getConfiguration(SERVICE_NAME);

        ServiceState serviceState = serviceConfiguration.getServiceState();

        //check if service has Unknown status
        assertEquals(serviceState.getState(), State.UNKNOWN);

        //mark Service as Running
        serviceState.markAsRunning(System.currentTimeMillis(), 0);

        //check if service status has been changed to Running
        assertEquals(serviceState.getState(), State.RUNNING);

        //Try to mark service as Running again
        serviceConfiguration.getServiceState().markAsRunning(System.currentTimeMillis(), 0);
    }

    @Test
    public void testGraceThreshold() {
        ServiceConfiguration configuration = new ServiceConfiguration("www.first-service.com", 80, SERVICE_NAME, true, 1, 3);
        configuration.setQueryInterval(10);
        configuration.setGraceInterval(5);
        assertEquals(configuration.getQueryInterval(), 5);
    }
}
