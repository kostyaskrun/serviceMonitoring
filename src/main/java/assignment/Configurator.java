package assignment;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

public class Configurator {

    private Map<String, ServiceConfiguration> configuration = new HashMap<>();

    private static Configurator instance = null;

    private Configurator() {}

    //Singleton method
    static Configurator getInstance() {
        if (isNull(instance)) {
            instance = new Configurator();
        }
        return instance;
    }

    ServiceConfiguration getConfiguration(String name) {
        return configuration.get(name);
    }

    void addConfiguration(ServiceConfiguration serviceConfiguration) {
        configuration.put(serviceConfiguration.getName(), serviceConfiguration);
    }

    public void removeConfiguration(String name) {
        configuration.remove(name);
    }

    void reset() {
        configuration.clear();
    }

    Iterable<String> getServiceNames() {
        return configuration.keySet();
    }
}