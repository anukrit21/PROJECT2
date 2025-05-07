package com.demoApp.mess.ml.recommendation;

import org.ejml.data.DMatrixRMaj;
import org.ejml.simple.SimpleMatrix;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;

@Component
public class MessRecommendationModel {
    private static final Logger logger = LoggerFactory.getLogger(MessRecommendationModel.class);

    private List<MenuItemDto> menuItems;
    private List<MessDto> messes;
    private List<RatingDto> ratings;
    private Map<Integer, String> messIdToName;
    private Map<String, List<MenuItemDto>> categoryAndCuisineToMenu;
    private Map<String, Map<Integer, Double>> userItemRatings;
    private SimpleMatrix similarityMatrix;
    private List<String> foodItems;
    private Map<String, Integer> foodItemIndices;

    private final com.demoApp.mess.config.RecommendationConfig config;

    public MessRecommendationModel(com.demoApp.mess.config.RecommendationConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void initialize() {
        try {
            loadDatasets();
            buildCategoryAndCuisineMap();
            buildUserItemRatings();
            createSimilarityMatrix();
            logger.info("Mess recommendation model initialized successfully");
        } catch (IOException e) {
            logger.error("Failed to initialize recommendation model: {}", e.getMessage(), e);
            // Initialize with empty data to prevent NullPointerExceptions
            menuItems = Collections.emptyList();
            messes = Collections.emptyList();
            ratings = Collections.emptyList();
            messIdToName = Collections.emptyMap();
            categoryAndCuisineToMenu = Collections.emptyMap();
            userItemRatings = Collections.emptyMap();
            foodItems = Collections.emptyList();
            foodItemIndices = Collections.emptyMap();
            
            // Create empty similarity matrix
            double[][] emptyMatrix = new double[0][0];
            similarityMatrix = new SimpleMatrix(new DMatrixRMaj(emptyMatrix));
        }
    }

    private void loadDatasets() throws IOException {
        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        messIdToName = new HashMap<>();
        messes = new ArrayList<>();
        try {
            // Use the config to get the file path
            ClassPathResource messResource = new ClassPathResource(config.getMessMasterFile());
            if (messResource.exists()) {
                try (Reader reader = new InputStreamReader(messResource.getInputStream());
                     CSVParser csvParser = new CSVParser(reader, csvFormat)) {
                    for (CSVRecord record : csvParser) {
                        int messId = Integer.parseInt(record.get("Mess_id"));
                        String messName = record.get("Mess_name");
                        messIdToName.put(messId, messName);
                        messes.add(new MessDto(messId, messName));
                    }
                }
            } else {
                logger.warn("Mess master file not found at {}. Using default data.", config.getMessMasterFile());
                // Add some default data
                messIdToName.put(1, "Default Mess");
                messes.add(new MessDto(1, "Default Mess"));
            }
        } catch (Exception e) {
            logger.error("Error loading mess data: {}", e.getMessage(), e);
            // Add fallback data
            messIdToName.put(1, "Default Mess");
            messes.add(new MessDto(1, "Default Mess"));
        }

        menuItems = new ArrayList<>();
        try {
            // Use the config to get the file path
            ClassPathResource menuResource = new ClassPathResource(config.getMenuMessFile());
            if (menuResource.exists()) {
                try (Reader reader = new InputStreamReader(menuResource.getInputStream());
                     CSVParser csvParser = new CSVParser(reader, csvFormat)) {
                    for (CSVRecord record : csvParser) {
                        int messId = Integer.parseInt(record.get("Mess_id"));
                        String foodItem = record.get("Food_item");
                        String category = record.get("Category");
                        String cuisine = record.get("Cuisine");
                        menuItems.add(new MenuItemDto(messId, foodItem, category, cuisine));
                    }
                }
            } else {
                logger.warn("Menu file not found at {}. Using default data.", config.getMenuMessFile());
                // Add some default data
                menuItems.add(new MenuItemDto(1, "Default Food", "All", "All"));
            }
        } catch (Exception e) {
            logger.error("Error loading menu data: {}", e.getMessage(), e);
            // Add fallback data
            menuItems.add(new MenuItemDto(1, "Default Food", "All", "All"));
        }

        ratings = new ArrayList<>();
        try {
            // Use the config to get the file path
            ClassPathResource ratingsResource = new ClassPathResource(config.getRatingsFile());
            if (ratingsResource.exists()) {
                try (Reader reader = new InputStreamReader(ratingsResource.getInputStream());
                     CSVParser csvParser = new CSVParser(reader, csvFormat)) {
                    for (CSVRecord record : csvParser) {
                        int messId = Integer.parseInt(record.get("Mess_id"));
                        String foodItem = record.get("Food_item");
                        String category = record.get("Category");
                        String cuisine = record.get("Cuisine");
                        double rating = Double.parseDouble(record.get("Rating"));
                        int orderCount = Integer.parseInt(record.get("Order_count"));
                        ratings.add(new RatingDto(messId, foodItem, category, cuisine, rating, orderCount));
                    }
                }
            } else {
                logger.warn("Ratings file not found at {}. Using default data.", config.getRatingsFile());
                // Add some default data
                ratings.add(new RatingDto(1, "Default Food", "All", "All", 4.0, 10));
            }
        } catch (Exception e) {
            logger.error("Error loading ratings data: {}", e.getMessage(), e);
            // Add fallback data
            ratings.add(new RatingDto(1, "Default Food", "All", "All", 4.0, 10));
        }

        logger.info("Loaded {} messes, {} menu items, and {} ratings", messes.size(), menuItems.size(), ratings.size());
    }

    private void buildCategoryAndCuisineMap() {
        categoryAndCuisineToMenu = menuItems.stream()
                .collect(Collectors.groupingBy(
                        menuItem -> menuItem.getCategory().toLowerCase() + "_" + menuItem.getCuisine().toLowerCase()
                ));
        logger.info("Built category and cuisine map with {} combinations", categoryAndCuisineToMenu.size());
    }

    private void buildUserItemRatings() {
        userItemRatings = new HashMap<>();
        Map<Integer, Double> ratingsMap = new HashMap<>();
        for (RatingDto rating : ratings) {
            ratingsMap.put(rating.getMessId(), rating.getRating());
        }
        userItemRatings.put("user", ratingsMap);
        logger.info("Built user-item ratings matrix");
    }

    private void createSimilarityMatrix() {
        foodItems = ratings.stream()
                .map(RatingDto::getFoodItem)
                .distinct()
                .collect(Collectors.toList());

        foodItemIndices = new HashMap<>();
        for (int i = 0; i < foodItems.size(); i++) {
            foodItemIndices.put(foodItems.get(i), i);
        }

        double[][] itemFeatures = new double[foodItems.size()][messes.size()];
        for (RatingDto rating : ratings) {
            int foodItemIndex = foodItemIndices.get(rating.getFoodItem());
            int messIndex = messes.indexOf(new MessDto(rating.getMessId(), ""));
            if (messIndex >= 0) {
                itemFeatures[foodItemIndex][messIndex] = rating.getRating();
            }
        }

        double[][] similarityData = new double[foodItems.size()][foodItems.size()];
        for (int i = 0; i < foodItems.size(); i++) {
            for (int j = i; j < foodItems.size(); j++) {
                double[] itemI = itemFeatures[i];
                double[] itemJ = itemFeatures[j];
                double distance = EuclideanDistance.d(itemI, itemJ);
                double similarity = 1.0 / (1.0 + distance);
                similarityData[i][j] = similarity;
                similarityData[j][i] = similarity;
            }
        }

        similarityMatrix = new SimpleMatrix(new DMatrixRMaj(similarityData));
        logger.info("Created item similarity matrix of size {}x{}", foodItems.size(), foodItems.size());
    }

    public RecommendationResponse recommendMesses(String category, String cuisine) {
        // Use default values from config if null or empty
        String effectiveCategory = (category == null || category.isEmpty()) ? 
            config.getDefaultCategory() : category;
        String effectiveCuisine = (cuisine == null || cuisine.isEmpty()) ? 
            config.getDefaultCuisine() : cuisine;
            
        logger.info("Generating recommendations for category={} and cuisine={}", effectiveCategory, effectiveCuisine);
        
        String key = effectiveCategory.toLowerCase() + "_" + effectiveCuisine.toLowerCase();
        List<MenuItemDto> filteredMenu = categoryAndCuisineToMenu.getOrDefault(key, Collections.emptyList());
        
        if (filteredMenu.isEmpty() && !"all_all".equals(key.toLowerCase())) {
            // Try with All category if specific category has no items
            String allKey = "all_" + effectiveCuisine.toLowerCase();
            filteredMenu = categoryAndCuisineToMenu.getOrDefault(allKey, Collections.emptyList());
            
            // If still empty, try with All cuisine
            if (filteredMenu.isEmpty()) {
                String categoryAllKey = effectiveCategory.toLowerCase() + "_all";
                filteredMenu = categoryAndCuisineToMenu.getOrDefault(categoryAllKey, Collections.emptyList());
                
                // If still empty, use all_all
                if (filteredMenu.isEmpty()) {
                    filteredMenu = categoryAndCuisineToMenu.getOrDefault("all_all", Collections.emptyList());
                }
            }
        }
        
        if (filteredMenu.isEmpty()) {
            logger.info("No items available for this category and cuisine");
            return new RecommendationResponse(
                Collections.emptyList(),
                Collections.singletonList(new UnrecommendedMessDto("No items available for this category and cuisine today."))
            );
        }
        
        // Find rated menu items for this category and cuisine
        List<RatingDto> ratedItems = ratings.stream()
            .filter(r -> (r.getCategory().equalsIgnoreCase(effectiveCategory) || "All".equalsIgnoreCase(effectiveCategory))
                   && (r.getCuisine().equalsIgnoreCase(effectiveCuisine) || "All".equalsIgnoreCase(effectiveCuisine)))
            .collect(Collectors.toList());
        
        // Group rated items by mess and calculate average ratings
        Map<Integer, List<RatingDto>> ratedItemsByMess = ratedItems.stream()
            .collect(Collectors.groupingBy(RatingDto::getMessId));
        
        List<RecommendedMessDto> recommended = new ArrayList<>();
        List<UnrecommendedMessDto> unrecommended = new ArrayList<>();
        
        // First add direct recommendations based on ratings
        for (Map.Entry<Integer, List<RatingDto>> entry : ratedItemsByMess.entrySet()) {
            int messId = entry.getKey();
            List<RatingDto> messRatings = entry.getValue();
            
            double avgRating = messRatings.stream().mapToDouble(RatingDto::getRating).average().orElse(0);
            double avgOrderCount = messRatings.stream().mapToDouble(RatingDto::getOrderCount).average().orElse(0);
            double score = (avgRating * 0.7) + (avgOrderCount * 0.3 / 10);
            
            List<String> foodItems = messRatings.stream()
                .<String>map(RatingDto::getFoodItem)
                .collect(Collectors.toList());
            
            String messName = messIdToName.getOrDefault(messId, "Unknown Mess");
            
            // Use the threshold from config
            if (score >= config.getMinScoreThreshold()) {
                recommended.add(new RecommendedMessDto(
                    messId,
                    messName,
                    Math.round(avgRating * 100) / 100.0,
                    String.join("\n", foodItems)
                ));
            } else {
                unrecommended.add(new UnrecommendedMessDto(
                    messId,
                    messName,
                    Math.round(avgRating * 100) / 100.0,
                    String.join("\n", foodItems)
                ));
            }
        }
        
        // Add collaborative filtering recommendations for unrated items
        Set<Integer> recommendedMessIds = recommended.stream()
            .<Integer>map(RecommendedMessDto::getMessId)
            .collect(Collectors.toSet());
        
        // Find unrated menu items
        List<MenuItemDto> unratedItems = filteredMenu.stream()
            .filter(menuItem -> ratedItems.stream()
                .noneMatch(ratingDto -> 
                    ratingDto.getMessId() == menuItem.getMessId() && 
                    ratingDto.getFoodItem().equals(menuItem.getFoodItem())))
            .collect(Collectors.toList());
        
        // For each unrated item, predict rating using item similarity
        for (MenuItemDto unratedItem : unratedItems) {
            if (recommendedMessIds.contains(unratedItem.getMessId())) {
                continue; // Skip if mess is already recommended
            }
            
            Double predictedRating = predictRating(unratedItem.getFoodItem());
            if (predictedRating != null && predictedRating >= config.getMinScoreThreshold() + 0.2) {
                String messName = messIdToName.getOrDefault(unratedItem.getMessId(), "Unknown Mess");
                
                recommended.add(new RecommendedMessDto(
                    unratedItem.getMessId(),
                    messName,
                    String.format("Recommended by CF (%.2f)", predictedRating),
                    unratedItem.getFoodItem()
                ));
                
                recommendedMessIds.add(unratedItem.getMessId());
            }
        }
        
        // Add remaining unrated items to unrecommended
        for (MenuItemDto unratedItem : unratedItems) {
            if (recommendedMessIds.contains(unratedItem.getMessId())) {
                continue; // Skip if mess is already recommended
            }
            
            String messName = messIdToName.getOrDefault(unratedItem.getMessId(), "Unknown Mess");
            
            unrecommended.add(new UnrecommendedMessDto(
                unratedItem.getMessId(),
                messName,
                "No Rating",
                unratedItem.getFoodItem()
            ));
        }
        
        // If no recommendations, add a message
        if (recommended.isEmpty()) {
            logger.info("No recommendations found for category: {}, cuisine: {}", effectiveCategory, effectiveCuisine);
            return new RecommendationResponse(
                Collections.emptyList(),
                Collections.singletonList(new UnrecommendedMessDto(
                    "We couldn't find any recommendations that match your criteria at this time. " +
                    "Please try different filters or check back later."))
            );
        }
        
        if (unrecommended.isEmpty()) {
            unrecommended.add(new UnrecommendedMessDto("No unrecommended messes found!"));
        }
        
        // Sort recommendations by rating (highest first)
        recommended.sort(Comparator.comparing(
            r -> r.getRating() instanceof Number ? ((Number) r.getRating()).doubleValue() : 0.0,
            Comparator.reverseOrder()
        ));
        
        logger.info("Generated {} recommendations and {} unrecommended messes", 
            recommended.size(), unrecommended.size());
        
        return new RecommendationResponse(recommended, unrecommended);
    }
    
    private Double predictRating(String foodItem) {
        if (!foodItemIndices.containsKey(foodItem)) {
            logger.warn("Food item '{}' not found in the dataset", foodItem);
            return null;
        }
        
        int foodItemIndex = foodItemIndices.get(foodItem);
        
        // Use our custom Matrix class for better performance
        double[] similarities = new double[foodItems.size()];
        double[] ratings = new double[foodItems.size()];
        
        for (int i = 0; i < foodItems.size(); i++) {
            similarities[i] = similarityMatrix.get(foodItemIndex, i);
            
            // Get the average rating for this food item
            String otherFoodItem = foodItems.get(i);
            double avgRating = this.ratings.stream()
                .filter(r -> r.getFoodItem().equals(otherFoodItem))
                .mapToDouble(RatingDto::getRating)
                .average()
                .orElse(0.0);
                
            ratings[i] = avgRating;
        }
        
        // Create Matrix objects
        Matrix similarityVector = new Matrix(new double[][] { similarities });
        Matrix ratingVector = new Matrix(new double[][] { ratings });
        
        // Calculate the weighted sum using matrices
        Matrix transposedSimilarities = similarityVector.transpose();
        Matrix weightedRatings = ratingVector.multiply(transposedSimilarities);
        
        // Get the sum of similarities
        double sumSimilarities = 0.0;
        for (double sim : similarities) {
            sumSimilarities += sim;
        }
        
        // Avoid division by zero
        if (sumSimilarities == 0.0) {
            return null;
        }
        
        // Get the predicted rating
        double predictedRating = weightedRatings.get(0, 0) / sumSimilarities;
        
        logger.debug("Predicted rating for '{}': {}", foodItem, predictedRating);
        return predictedRating;
    }

    public static class MenuItemDto {
        private int messId;
        private String foodItem;
        private String category;
        private String cuisine;

        public MenuItemDto(int messId, String foodItem, String category, String cuisine) {
            this.messId = messId;
            this.foodItem = foodItem;
            this.category = category;
            this.cuisine = cuisine;
        }

        public int getMessId() { return messId; }
        public String getFoodItem() { return foodItem; }
        public String getCategory() { return category; }
        public String getCuisine() { return cuisine; }
    }

    public static class MessDto {
        private int messId;
        private String messName;

        public MessDto(int messId, String messName) {
            this.messId = messId;
            this.messName = messName;
        }

        public int getMessId() { return messId; }
        public String getMessName() { return messName; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof MessDto)) return false;
            MessDto other = (MessDto) obj;
            return this.messId == other.messId;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(messId);
        }
    }

    public static class RatingDto {
        private int messId;
        private String foodItem;
        private String category;
        private String cuisine;
        private double rating;
        private int orderCount;

        public RatingDto(int messId, String foodItem, String category, String cuisine, double rating, int orderCount) {
            this.messId = messId;
            this.foodItem = foodItem;
            this.category = category;
            this.cuisine = cuisine;
            this.rating = rating;
            this.orderCount = orderCount;
        }

        public int getMessId() { return messId; }
        public String getFoodItem() { return foodItem; }
        public String getCategory() { return category; }
        public String getCuisine() { return cuisine; }
        public double getRating() { return rating; }
        public int getOrderCount() { return orderCount; }
    }

    public static class RecommendedMessDto {
        private int messId;
        private String messName;
        private Object rating;
        private String recommendedItems;

        public RecommendedMessDto(int messId, String messName, Object rating, String recommendedItems) {
            this.messId = messId;
            this.messName = messName;
            this.rating = rating;
            this.recommendedItems = recommendedItems;
        }

        public int getMessId() { return messId; }
        public String getMessName() { return messName; }
        public Object getRating() { return rating; }
        public String getRecommendedItems() { return recommendedItems; }
    }

    public static class UnrecommendedMessDto {
        private int messId;
        private String messName;
        private Object reason;
        private String availableItems;

        public UnrecommendedMessDto(String reason) {
            this.reason = reason;
        }

        public UnrecommendedMessDto(int messId, String messName, Object reason, String availableItems) {
            this.messId = messId;
            this.messName = messName;
            this.reason = reason;
            this.availableItems = availableItems;
        }

        public int getMessId() { return messId; }
        public String getMessName() { return messName; }
        public Object getReason() { return reason; }
        public String getAvailableItems() { return availableItems; }
    }

    public static class RecommendationResponse {
        private List<RecommendedMessDto> recommended;
        private List<UnrecommendedMessDto> unrecommended;

        public RecommendationResponse(List<RecommendedMessDto> recommended, List<UnrecommendedMessDto> unrecommended) {
            this.recommended = recommended;
            this.unrecommended = unrecommended;
        }

        public List<RecommendedMessDto> getRecommended() { return recommended; }
        public List<UnrecommendedMessDto> getUnrecommended() { return unrecommended; }
    }
}
