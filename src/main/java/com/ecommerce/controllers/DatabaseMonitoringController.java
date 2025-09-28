package com.ecommerce.controllers;

import com.ecommerce.services.DatabasePerformanceMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/monitoring")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DatabaseMonitoringController {

    private final DatabasePerformanceMonitoringService monitoringService;

    /**
     * Endpoint pour récupérer toutes les statistiques Hibernate
     */
    @GetMapping("/hibernate/stats")
    public ResponseEntity<Map<String, Object>> getHibernateStatistics() {
        Map<String, Object> stats = monitoringService.getHibernateStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Endpoint pour les requêtes lentes
     */
    @GetMapping("/hibernate/slow-queries")
    public ResponseEntity<Map<String, Object>> getSlowQueries() {
        Map<String, Object> slowQueries = monitoringService.getSlowQueries();
        return ResponseEntity.ok(slowQueries);
    }

    /**
     * Endpoint pour les ratios de performance du cache
     */
    @GetMapping("/hibernate/cache-performance")
    public ResponseEntity<Map<String, Double>> getCachePerformance() {
        Map<String, Double> ratios = monitoringService.getCachePerformanceRatios();
        return ResponseEntity.ok(ratios);
    }

    /**
     * Endpoint pour le health check des performances
     */
    @GetMapping("/hibernate/health")
    public ResponseEntity<Map<String, Object>> getDatabaseHealth() {
        Map<String, Object> health = monitoringService.getDatabaseHealth();
        return ResponseEntity.ok(health);
    }

    /**
     * Endpoint pour reset les statistiques
     */
    @PostMapping("/hibernate/reset-stats")
    public ResponseEntity<String> resetStatistics() {
        monitoringService.resetStatistics();
        return ResponseEntity.ok("Statistiques Hibernate remises à zéro");
    }

    /**
     * Endpoint pour forcer un log du résumé de performance
     */
    @PostMapping("/hibernate/log-summary")
    public ResponseEntity<String> logPerformanceSummary() {
        monitoringService.logPerformanceSummary();
        return ResponseEntity.ok("Résumé de performance loggé");
    }
}
