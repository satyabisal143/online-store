package com.onlinestore.order; // same package as the other order classes

import org.springframework.stereotype.Service; // marks this class as a Spring service bean
import org.springframework.transaction.annotation.Propagation; // controls how a transaction joins an existing one
import org.springframework.transaction.annotation.Transactional; // runs a method in one DB transaction

// DIFFICULTY: this lives in its own bean on purpose. Spring transactions work through a proxy, so a
// REQUIRES_NEW method called from inside the SAME class would silently run in the caller's transaction.
@Service // Spring creates one instance
public class PendingReleaseService { // stores release tasks that must not be lost

    private final PendingReleaseRepository repository; // saves the retry tasks

    public PendingReleaseService(PendingReleaseRepository repository) { // Spring injects the repository
        this.repository = repository; // keep it
    } // end of constructor

    // DIFFICULTY: OrderService.create() rolls back when it fails, which would also erase this row.
    // REQUIRES_NEW suspends that transaction and commits this insert on its own, so the task survives.
    @Transactional(propagation = Propagation.REQUIRES_NEW) // separate transaction, committed immediately
    public void record(Long productId, int quantity) { // remember "give this stock back later"
        repository.save(new PendingRelease(productId, quantity)); // persist the retry task
    } // end of record
} // end of class
