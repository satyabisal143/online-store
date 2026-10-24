package com.onlinestore.order; // same package as the other order classes

import org.springframework.data.jpa.repository.JpaRepository; // gives save, findAll, delete, ...

import java.util.List; // list interface

public interface PendingReleaseRepository extends JpaRepository<PendingRelease, Long> { // Spring generates the implementation

    List<PendingRelease> findByAttemptsLessThan(int maxAttempts); // derived query: only tasks that are still worth retrying
} // end of interface
