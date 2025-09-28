package com.ecommerce.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabasePerformanceMonitoringService {

    private final EntityManagerFactory entityManagerFactory;

    /**
     * Récupère les statistiques Hibernate complètes de manière sécurisée
     * Basé sur les bonnes pratiques de Vlad Mihalcea
     */
    public Map<String, Object> getHibernateStatistics() {
        try {
            SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
            Statistics stats = sessionFactory.getStatistics();

            if (!stats.isStatisticsEnabled()) {
                log.warn("Les statistiques Hibernate ne sont pas activées");
                return Map.of("statisticsEnabled", false);
            }

            Map<String, Object> statisticsMap = new HashMap<>();

            // Statistiques des requêtes - conversion sécurisée
            statisticsMap.put("queryExecutionCount", safeLongValue(stats.getQueryExecutionCount()));
            statisticsMap.put("queryExecutionMaxTime", safeLongValue(stats.getQueryExecutionMaxTime()));
            statisticsMap.put("queryExecutionMaxTimeQueryString", safeStringValue(stats.getQueryExecutionMaxTimeQueryString()));
            statisticsMap.put("queryCacheHitCount", safeLongValue(stats.getQueryCacheHitCount()));
            statisticsMap.put("queryCacheMissCount", safeLongValue(stats.getQueryCacheMissCount()));
            statisticsMap.put("queryCachePutCount", safeLongValue(stats.getQueryCachePutCount()));

            // Statistiques des entités
            statisticsMap.put("entityLoadCount", safeLongValue(stats.getEntityLoadCount()));
            statisticsMap.put("entityInsertCount", safeLongValue(stats.getEntityInsertCount()));
            statisticsMap.put("entityUpdateCount", safeLongValue(stats.getEntityUpdateCount()));
            statisticsMap.put("entityDeleteCount", safeLongValue(stats.getEntityDeleteCount()));

            // Statistiques du cache de second niveau
            statisticsMap.put("secondLevelCacheHitCount", safeLongValue(stats.getSecondLevelCacheHitCount()));
            statisticsMap.put("secondLevelCacheMissCount", safeLongValue(stats.getSecondLevelCacheMissCount()));
            statisticsMap.put("secondLevelCachePutCount", safeLongValue(stats.getSecondLevelCachePutCount()));

            // Statistiques des connexions
            statisticsMap.put("connectCount", safeLongValue(stats.getConnectCount()));
            statisticsMap.put("prepareStatementCount", safeLongValue(stats.getPrepareStatementCount()));
            statisticsMap.put("closeStatementCount", safeLongValue(stats.getCloseStatementCount()));

            // Sessions
            statisticsMap.put("sessionOpenCount", safeLongValue(stats.getSessionOpenCount()));
            statisticsMap.put("sessionCloseCount", safeLongValue(stats.getSessionCloseCount()));

            // Transactions
            statisticsMap.put("transactionCount", safeLongValue(stats.getTransactionCount()));
            statisticsMap.put("successfulTransactionCount", safeLongValue(stats.getSuccessfulTransactionCount()));

            statisticsMap.put("statisticsEnabled", true);
            return statisticsMap;

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques Hibernate", e);
            return Map.of("error", e.getMessage(), "statisticsEnabled", false);
        }
    }

    /**
     * Récupère les requêtes les plus lentes de manière sécurisée
     */
    public Map<String, Object> getSlowQueries() {
        try {
            SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
            Statistics stats = sessionFactory.getStatistics();

            if (!stats.isStatisticsEnabled()) {
                return Map.of("statisticsEnabled", false);
            }

            Map<String, Object> slowQueries = new HashMap<>();
            long maxQueryTime = safeLongValue(stats.getQueryExecutionMaxTime());
            String slowestQuery = safeStringValue(stats.getQueryExecutionMaxTimeQueryString());

            slowQueries.put("maxQueryTime", maxQueryTime);
            slowQueries.put("slowestQuery", slowestQuery);
            slowQueries.put("statisticsEnabled", true);

            // Log si requête très lente détectée
            if (maxQueryTime > 2000L) { // Plus de 2 secondes
                log.warn("Requête très lente détectée: {}ms - Query: {}", maxQueryTime, slowestQuery);
            }

            return slowQueries;

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des requêtes lentes", e);
            return Map.of("error", e.getMessage(), "statisticsEnabled", false);
        }
    }

    /**
     * Calcule les ratios de performance du cache de manière sécurisée
     */
    public Map<String, Double> getCachePerformanceRatios() {
        try {
            SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
            Statistics stats = sessionFactory.getStatistics();

            if (!stats.isStatisticsEnabled()) {
                return Map.of();
            }

            Map<String, Double> ratios = new HashMap<>();

            // Ratio cache de requêtes
            long queryCacheHits = safeLongValue(stats.getQueryCacheHitCount());
            long queryCacheMisses = safeLongValue(stats.getQueryCacheMissCount());
            long totalQueryCacheRequests = queryCacheHits + queryCacheMisses;

            if (totalQueryCacheRequests > 0) {
                double queryCacheHitRatio = (double) queryCacheHits / totalQueryCacheRequests * 100.0;
                ratios.put("queryCacheHitRatio", Math.round(queryCacheHitRatio * 100.0) / 100.0);
            }

            // Ratio cache de second niveau
            long secondLevelCacheHits = safeLongValue(stats.getSecondLevelCacheHitCount());
            long secondLevelCacheMisses = safeLongValue(stats.getSecondLevelCacheMissCount());
            long totalSecondLevelCacheRequests = secondLevelCacheHits + secondLevelCacheMisses;

            if (totalSecondLevelCacheRequests > 0) {
                double secondLevelCacheHitRatio = (double) secondLevelCacheHits / totalSecondLevelCacheRequests * 100.0;
                ratios.put("secondLevelCacheHitRatio", Math.round(secondLevelCacheHitRatio * 100.0) / 100.0);
            }

            return ratios;

        } catch (Exception e) {
            log.error("Erreur lors du calcul des ratios de cache", e);
            return Map.of();
        }
    }

    /**
     * Reset des statistiques Hibernate de manière sécurisée
     */
    public void resetStatistics() {
        try {
            SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
            Statistics stats = sessionFactory.getStatistics();

            if (stats.isStatisticsEnabled()) {
                stats.clear();
                log.info("Statistiques Hibernate remises à zéro");
            } else {
                log.warn("Impossible de remettre à zéro - statistiques non activées");
            }
        } catch (Exception e) {
            log.error("Erreur lors du reset des statistiques", e);
        }
    }

    /**
     * Vérification de la santé des performances de base de données
     */
    public Map<String, Object> getDatabaseHealth() {
        try {
            Map<String, Object> stats = getHibernateStatistics();

            if (Boolean.FALSE.equals(stats.get("statisticsEnabled"))) {
                return Map.of(
                    "status", "DOWN",
                    "reason", "Statistiques Hibernate non activées"
                );
            }

            Map<String, Double> ratios = getCachePerformanceRatios();

            // Vérifier si performance acceptable
            Object maxQueryTimeObj = stats.get("queryExecutionMaxTime");
            long maxQueryTime = maxQueryTimeObj instanceof Long ? (Long) maxQueryTimeObj : 0L;
            boolean isHealthy = maxQueryTime < 5000L; // Moins de 5 secondes

            Map<String, Object> healthStatus = new HashMap<>();
            healthStatus.put("status", isHealthy ? "UP" : "DOWN");
            healthStatus.put("hibernateStats", stats);
            healthStatus.put("cacheRatios", ratios);
            healthStatus.put("slowQueries", getSlowQueries());

            return healthStatus;

        } catch (Exception e) {
            log.error("Erreur lors de la vérification des statistiques Hibernate", e);
            return Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            );
        }
    }

    /**
     * Log un résumé des performances de manière sécurisée
     */
    public void logPerformanceSummary() {
        try {
            Map<String, Object> stats = getHibernateStatistics();

            if (Boolean.FALSE.equals(stats.get("statisticsEnabled"))) {
                log.warn("Impossible de logger le résumé - statistiques non activées");
                return;
            }

            Map<String, Double> ratios = getCachePerformanceRatios();

            log.info("=== HIBERNATE PERFORMANCE SUMMARY ===");
            log.info("Requêtes exécutées: {}", stats.get("queryExecutionCount"));
            log.info("Temps max requête: {}ms", stats.get("queryExecutionMaxTime"));
            log.info("Ratio cache requêtes: {}%", ratios.getOrDefault("queryCacheHitRatio", 0.0));
            log.info("Ratio cache second niveau: {}%", ratios.getOrDefault("secondLevelCacheHitRatio", 0.0));
            log.info("Entités chargées: {}", stats.get("entityLoadCount"));
            log.info("Sessions ouvertes: {}", stats.get("sessionOpenCount"));
            log.info("=====================================");
        } catch (Exception e) {
            log.error("Erreur lors du logging du résumé de performance", e);
        }
    }

    // Méthodes utilitaires pour la conversion sécurisée des types
    private long safeLongValue(long value) {
        return Math.max(0L, value);
    }

    private String safeStringValue(String value) {
        return value != null ? value : "N/A";
    }
}
