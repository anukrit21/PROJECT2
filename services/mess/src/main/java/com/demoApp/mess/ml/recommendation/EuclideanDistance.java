package com.demoApp.mess.ml.recommendation;

public class EuclideanDistance {
    /**
     * Calculates Euclidean distance between two vectors
     * @param vector1 First vector
     * @param vector2 Second vector
     * @return Euclidean distance
     */
    public static double d(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }
        
        double sum = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            double diff = vector1[i] - vector2[i];
            sum += diff * diff;
        }
        
        return Math.sqrt(sum);
    }
}
