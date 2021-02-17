package cz.devconf2021.stm;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/stm")
@RequestScoped
public class LockpickingRestResource {
    ExecutorService executor;

    @ConfigProperty(name = "org.acme.quickstart.stm.threadpool.size")
    int threadPoolSize;;

    @Inject
    LockpickingServiceFactory factory;

    @PostConstruct
    void postConstruct() {
        executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    @PreDestroy
    void preDestroy() {
        executor.shutdown();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public CompletionStage<String> bookingCount() {
        return CompletableFuture.supplyAsync(
                () -> getInfo(factory.getInstance()),
                executor);
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public CompletionStage<String> asynBook() {
        return CompletableFuture.supplyAsync(() -> {
            LockpickingTransactionalService flightService = factory.getInstance();

            flightService.makeBooking("BA123");

            return getInfo(flightService);
        }, executor);
    }

    @POST
    @Path("sync")
    @Produces(MediaType.TEXT_PLAIN)
    public String book() {
        LockpickingTransactionalService flightService = factory.getInstance();

        flightService.makeBooking("BA123");

        return getInfo(flightService);
    }

    private String getInfo(LockpickingTransactionalService flightService) {
        return Thread.currentThread().getName()
                + ":  Booking Count=" + flightService.getNumberOfBookings();
    }
}
