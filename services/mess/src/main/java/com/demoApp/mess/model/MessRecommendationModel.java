package com.demoApp.mess.model;

import java.util.List;

public class MessRecommendationModel {

    // Recommendation class holds individual recommendation data
    public static class Recommendation {
        private String messId;
        private String category;
        private String cuisine;
        private Object rating;  // Could be String or Number, depending on your requirements
        
        // Getters and setters
        public String getMessId() {
            return messId;
        }

        public void setMessId(String messId) {
            this.messId = messId;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getCuisine() {
            return cuisine;
        }

        public void setCuisine(String cuisine) {
            this.cuisine = cuisine;
        }

        public Object getRating() {
            return rating;
        }

        public void setRating(Object rating) {
            this.rating = rating;
        }
    }

    // RecommendationResponse class holds a list of recommendations
    public static class RecommendationResponse {
        private List<Recommendation> recommendations;

        // Getter and setter for recommendations list
        public List<Recommendation> getRecommendations() {
            return recommendations;
        }

        public void setRecommendations(List<Recommendation> recommendations) {
            this.recommendations = recommendations;
        }
    }

    // The recommendMesses method is where we implement the logic to recommend messes
    public RecommendationResponse recommendMesses(String category, String cuisine) {
        // Implement logic to generate the recommendations based on category and cuisine
        // This is a sample implementation, replace it with your actual logic
        
        // For now, we'll just return a sample list of recommendations.
        List<Recommendation> sampleRecommendations = List.of(
            new Recommendation() {{
                setMessId("1");
                setCategory(category);
                setCuisine(cuisine);
                setRating("4.5");
            }},
            new Recommendation() {{
                setMessId("2");
                setCategory(category);
                setCuisine(cuisine);
                setRating("4.0");
            }}
        );

        // Return a RecommendationResponse containing the sample recommendations
        RecommendationResponse response = new RecommendationResponse();
        response.setRecommendations(sampleRecommendations);

        return response;
    }
}
