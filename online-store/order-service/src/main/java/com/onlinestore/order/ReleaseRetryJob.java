package com.onlinestore.order; // same package as the other order classes

import org.springframework.scheduling.annotation.Scheduled; // runs a method on a timer
import org.springframework.stereotype.Component; // marks this class as a Spring bean

// DIFFICULTY: background work. It runs on its own thread, outside any web request, so errors must be handled here.
@Component // Spring creates one instance and starts its scheduled method
public class ReleaseRetryJob { // keeps retrying failed releases until they work

    static final int MAX_ATTEMPTS = 5; // after this many failures we stop and leave the row for a human to inspect

    private final PendingReleaseRepository repository; // finds and deletes retry tasks
    private final ProductClient productClient; // the call that may fail

    public ReleaseRetryJob(PendingReleaseRepository repository, ProductClient productClient) { // Spring injects both
        this.repository = repository; // keep the repository
        this.productClient = productClient; // keep the client
    } // end of constructor

    @Scheduled(fixedDelay = 30_000) // wait 30 s AFTER the previous run ends, so runs never overlap
    public void retryFailedReleases() { // one pass over all open tasks
        for (PendingRelease task : repository.findByAttemptsLessThan(MAX_ATTEMPTS)) { // skip tasks that gave up
            try { // one broken task must not stop the others
                productClient.release(task.getProductId(), task.getQuantity()); // try the original call again
                repository.delete(task); // success: the task is done, remove it
            } catch (RuntimeException e) { // product-service is still down or refused
                task.recordFailedAttempt(); // remember that this try failed
                repository.save(task); // persist the new attempt count
            } // end of try/catch
        } // end of loop
    } // end of retryFailedReleases
} // end of class
