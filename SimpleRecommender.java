package com.securefile.task4;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SimpleRecommender - user-based collaborative filtering using cosine similarity.
 * No external libraries required — suitable for demo & unit tests.
 */
public class SimpleRecommender {

    // Cosine similarity between two users' rating maps.
    public static double cosineSimilarity(Map<String, Double> a, Map<String, Double> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return 0.0;

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (double va : a.values()) normA += va * va;
        for (double vb : b.values()) normB += vb * vb;

        for (String item : a.keySet()) {
            if (b.containsKey(item)) {
                dot += a.get(item) * b.get(item);
            }
        }

        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Recommend topN items for targetUser from ratings.
     * ratings: Map<userId, Map<itemId, rating>>
     */
    public static List<Map.Entry<String, Double>> recommend(
            Map<String, Map<String, Double>> ratings,
            String targetUser,
            int topN) {

        if (!ratings.containsKey(targetUser)) {
            throw new IllegalArgumentException("Unknown user: " + targetUser);
        }

        Map<String, Double> targetRatings = ratings.get(targetUser);

        // compute similarity between target and all other users
        Map<String, Double> sims = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> e : ratings.entrySet()) {
            String user = e.getKey();
            if (user.equals(targetUser)) continue;
            double sim = cosineSimilarity(targetRatings, e.getValue());
            sims.put(user, sim);
        }

        // candidate items = items other users rated but target hasn't
        Set<String> candidates = new HashSet<>();
        for (Map.Entry<String, Map<String, Double>> e : ratings.entrySet()) {
            String user = e.getKey();
            if (user.equals(targetUser)) continue;
            for (String item : e.getValue().keySet()) {
                if (!targetRatings.containsKey(item)) candidates.add(item);
            }
        }

        // compute weighted scores for candidates
        Map<String, Double> scores = new HashMap<>();
        for (String item : candidates) {
            double num = 0.0;
            double den = 0.0;
            for (Map.Entry<String, Map<String, Double>> e : ratings.entrySet()) {
                String user = e.getKey();
                if (user.equals(targetUser)) continue;
                Map<String, Double> userRatings = e.getValue();
                if (!userRatings.containsKey(item)) continue;
                double sim = sims.getOrDefault(user, 0.0);
                num += sim * userRatings.get(item);
                den += Math.abs(sim);
            }
            double score = (den == 0.0) ? 0.0 : (num / den);
            scores.put(item, score);
        }

        // return topN candidates sorted by score desc
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(Math.max(0, topN))
                .collect(Collectors.toList());
    }

    // Demo main with in-memory sample data
    public static void main(String[] args) {
        Map<String, Map<String, Double>> ratings = new HashMap<>();

        ratings.put("alice", Map.of(
                "maths-puzzle-app", 5.0,
                "secure-file-storage", 3.0,
                "e-learning-app", 4.0
        ));

        ratings.put("bob", Map.of(
                "maths-puzzle-app", 4.0,
                "pharmacy-management", 5.0,
                "secure-file-storage", 2.5
        ));

        ratings.put("carol", Map.of(
                "secure-file-storage", 4.5,
                "e-learning-app", 4.0,
                "pharmacy-management", 3.0
        ));

        ratings.put("dave", Map.of(
                "maths-puzzle-app", 2.0,
                "e-learning-app", 3.5,
                "new-item-x", 5.0
        ));

        // Recommend for alice
        List<Map.Entry<String, Double>> recs = recommend(ratings, "alice", 5);
        System.out.println("Recommendations for alice:");
        if (recs.isEmpty()) {
            System.out.println(" (no recommendations)");
        } else {
            for (var r : recs) {
                System.out.printf("  %s -> score: %.4f%n", r.getKey(), r.getValue());
            }
        }
    }
}
