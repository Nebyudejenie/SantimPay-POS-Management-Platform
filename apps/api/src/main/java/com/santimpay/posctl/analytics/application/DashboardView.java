package com.santimpay.posctl.analytics.application;

/**
 * Executive dashboard read model — a projection assembled from across modules' read-only data
 * (counts that feed the CEO/Operations dashboards in docs/09). This is the CQRS read side: it never
 * mutates and is the only place analytics reads other schemas, via dedicated read-only queries.
 */
public record DashboardView(
        long totalMerchants,
        long activeMerchants,
        long devicesInStock,
        long devicesDeployed,
        long devicesFaulty,
        long deploymentsToday,
        long openTasks,
        long pendingKyc) {}
