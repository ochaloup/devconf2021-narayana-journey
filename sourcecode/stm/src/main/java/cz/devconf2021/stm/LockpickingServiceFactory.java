package cz.devconf2021.stm;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.stm.Container;

@ApplicationScoped
class LockpickingServiceFactory {
    private LockpickingTransactionalService flightServiceProxy;

    private void initFlightServiceFactory() {
        Container<LockpickingTransactionalService> container = new Container<>();
        flightServiceProxy = container.create(new FlightServiceImpl());
    }

    LockpickingTransactionalService getInstance() {
        if (flightServiceProxy == null) {
            initFlightServiceFactory();
        }
        return flightServiceProxy;
    }
}
