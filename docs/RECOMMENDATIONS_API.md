# Recommendation API Documentation

This document provides detailed information about the recommendation system API endpoints available in the DemoApp.

## Base URLs

- **Production API**: `http://localhost:8080/api`
- **Mock Server**: `http://localhost:3000/api`

## Authentication

The recommendation endpoints are publicly accessible without authentication.

## API Endpoints

### 1. Basic Recommendation Endpoints

#### 1.1 Get Mess Recommendations

Returns a list of recommended mess facilities based on category and cuisine preferences.

- **URL**: `/recommendations/messes`
- **Method**: `GET`
- **Query Parameters**:
  - `category` (optional): Food category (e.g., "Breakfast", "Lunch", "Dinner", "All")
  - `cuisine` (optional): Cuisine type (e.g., "North Indian", "South Indian", "Continental", "All")
- **Example**: `GET /api/recommendations/messes?category=Dinner&cuisine=North%20Indian`

**Success Response (200 OK)**:
```json
[
  {
    "messId": 1,
    "messName": "Royal Dining",
    "category": "Dinner",
    "cuisine": "North Indian",
    "score": 4.8,
    "address": "123 Food Street, Bangalore 560001",
    "location": "Bangalore",
    "recommendedItems": ["Butter Chicken", "Naan", "Paneer Tikka"]
  }
]
```

#### 1.2 Test Recommendations Endpoint

Returns sample recommendation data for testing purposes.

- **URL**: `/recommendations/test`
- **Method**: `GET`
- **Example**: `GET /api/recommendations/test`

**Success Response (200 OK)**:
```json
[
  {
    "messId": 1,
    "messName": "Hotel Malabar",
    "category": "Dinner",
    "cuisine": "North Indian",
    "score": 4.8,
    "address": "123 Main Street, Bangalore",
    "location": "Bangalore",
    "recommendedItems": ["Butter Chicken", "Naan", "Paneer Tikka"]
  },
  // ... additional recommendations
]
```

### 2. Enhanced Recommendation Endpoints

#### 2.1 Get Enhanced Mess Recommendations

Returns a list of recommended mess facilities with additional menu and campus information.

- **URL**: `/enhanced-recommendations/messes`
- **Method**: `GET`
- **Query Parameters**:
  - `category` (optional): Food category (e.g., "Breakfast", "Lunch", "Dinner", "All")
  - `cuisine` (optional): Cuisine type (e.g., "North Indian", "South Indian", "Continental", "All")
  - `location` (optional): Location (e.g., "Bangalore", "Mumbai", "All")
- **Example**: `GET /api/enhanced-recommendations/messes?category=Dinner&cuisine=North%20Indian&location=Bangalore`

**Success Response (200 OK)**:
```json
[
  {
    "messId": 1,
    "messName": "Royal Dining",
    "category": "Dinner",
    "cuisine": "North Indian",
    "score": 4.8,
    "address": "123 Food Street, Bangalore 560001",
    "location": "Bangalore",
    "recommendedItems": ["Butter Chicken", "Naan", "Paneer Tikka"],
    "menuItems": [
      {
        "itemId": 101,
        "name": "Butter Chicken",
        "description": "Creamy tomato-based curry with tender chicken pieces",
        "price": 250.0,
        "category": "Main Course",
        "cuisine": "North Indian",
        "isVegetarian": false,
        "isAvailable": true
      },
      // ... additional menu items
    ],
    "campusInfo": [
      {
        "campusId": 1,
        "name": "Main Campus",
        "location": "Bangalore",
        "address": "123 University Road, Bangalore 560001",
        "contactNumber": "+91-9876543210",
        "email": "info@demoapp.edu"
      }
    ]
  }
]
```

#### 2.2 Enhanced Test Recommendations Endpoint

Returns sample enhanced recommendation data for testing purposes.

- **URL**: `/enhanced-recommendations/test`
- **Method**: `GET`
- **Example**: `GET /api/enhanced-recommendations/test`

**Success Response (200 OK)**:
```json
[
  {
    "messId": 1,
    "messName": "Royal Dining",
    "category": "Dinner",
    "cuisine": "North Indian",
    "score": 4.8,
    "address": "123 Food Street, Bangalore 560001",
    "location": "Bangalore",
    "recommendedItems": ["Butter Chicken", "Naan", "Paneer Tikka"],
    "menuItems": [
      // ... menu items
    ],
    "campusInfo": [
      // ... campus information
    ]
  }
]
```

#### 2.3 Get Popular Menu Items

Returns a list of popular menu items across all mess facilities.

- **URL**: `/enhanced-recommendations/menu-items/popular`
- **Method**: `GET`
- **Example**: `GET /api/enhanced-recommendations/menu-items/popular`

**Success Response (200 OK)**:
```json
[
  {
    "itemId": 101,
    "name": "Butter Chicken",
    "description": "Creamy tomato-based curry with tender chicken pieces",
    "price": 250.0,
    "category": "Main Course",
    "cuisine": "North Indian",
    "messId": 1,
    "messName": "Royal Dining",
    "rating": 4.8,
    "orderCount": 250,
    "isVegetarian": false
  },
  // ... additional popular items
]
```

#### 2.4 Get Mess Facilities by Campus

Returns a list of mess facilities associated with a specific campus.

- **URL**: `/enhanced-recommendations/campus/{campusId}/messes`
- **Method**: `GET`
- **URL Parameters**:
  - `campusId`: ID of the campus
- **Example**: `GET /api/enhanced-recommendations/campus/1/messes`

**Success Response (200 OK)**:
```json
[
  {
    "messId": 1,
    "messName": "Royal Dining",
    "location": "Bangalore",
    "address": "123 Food Street, Bangalore 560001",
    "contactNumber": "+91-9876543211",
    "category": "All",
    "cuisine": "North Indian",
    "rating": 4.8
  },
  // ... additional mess facilities
]
```

## Response Object Schema

### Basic Recommendation Response

| Field             | Type            | Description                                         |
|-------------------|-----------------|-----------------------------------------------------|
| `messId`          | Integer         | Unique identifier for the mess facility             |
| `messName`        | String          | Name of the mess facility                           |
| `category`        | String          | Food category (Breakfast, Lunch, Dinner, All)       |
| `cuisine`         | String          | Cuisine type                                        |
| `score`           | Float           | Recommendation score (0-5)                          |
| `address`         | String          | Physical address of the mess facility               |
| `location`        | String          | City/general location                               |
| `recommendedItems`| Array of String | List of recommended food items from this mess       |

### Enhanced Recommendation Response

Includes all fields from Basic Recommendation Response, plus:

| Field             | Type            | Description                                         |
|-------------------|-----------------|-----------------------------------------------------|
| `menuItems`       | Array of Object | Detailed menu items available at this mess          |
| `campusInfo`      | Array of Object | Campus information associated with this mess        |

### Menu Item Object

| Field             | Type            | Description                                         |
|-------------------|-----------------|-----------------------------------------------------|
| `itemId`          | Integer         | Unique identifier for the menu item                 |
| `name`            | String          | Name of the food item                               |
| `description`     | String          | Description of the food item                        |
| `price`           | Float           | Price of the item                                   |
| `category`        | String          | Food category                                       |
| `cuisine`         | String          | Cuisine type                                        |
| `isVegetarian`    | Boolean         | Whether the item is vegetarian                      |
| `isAvailable`     | Boolean         | Whether the item is currently available             |

### Campus Object

| Field             | Type            | Description                                         |
|-------------------|-----------------|-----------------------------------------------------|
| `campusId`        | Integer         | Unique identifier for the campus                    |
| `name`            | String          | Name of the campus                                  |
| `location`        | String          | Location/city of the campus                         |
| `address`         | String          | Physical address of the campus                      |
| `contactNumber`   | String          | Contact phone number                                |
| `email`           | String          | Contact email address                               |

## Error Handling

The API returns standard HTTP status codes:

- `200 OK`: Successful request
- `400 Bad Request`: Invalid request parameters
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server-side error

Error responses will include an error message and, when applicable, additional details.

## Testing with Mock Data

A mock server is available for testing without the full backend:

1. Start the mock server:
   ```
   cd demoApp-main
   npm install
   node mock-api-server.js
   ```

2. The mock server will be available at http://localhost:3000

3. Test the endpoints using the URLs described above, e.g., http://localhost:3000/api/recommendations/messes

## Integration Examples

### Fetch API (JavaScript)

```javascript
fetch('http://localhost:8080/api/enhanced-recommendations/messes?category=Dinner&cuisine=North%20Indian')
  .then(response => response.json())
  .then(data => console.log(data))
  .catch(error => console.error('Error fetching recommendations:', error));
```

### Axios (JavaScript)

```javascript
import axios from 'axios';

axios.get('http://localhost:8080/api/enhanced-recommendations/messes', {
  params: {
    category: 'Dinner',
    cuisine: 'North Indian',
    location: 'Bangalore'
  }
})
.then(response => console.log(response.data))
.catch(error => console.error('Error fetching recommendations:', error));
```

### Flutter Http Package

```dart
import 'package:http/http.dart' as http;
import 'dart:convert';

Future<List<dynamic>> getEnhancedRecommendations() async {
  final response = await http.get(
    Uri.parse('http://localhost:8080/api/enhanced-recommendations/messes?category=Dinner&cuisine=North%20Indian')
  );

  if (response.statusCode == 200) {
    return jsonDecode(response.body);
  } else {
    throw Exception('Failed to load recommendations');
  }
}
```

## Notes for Frontend Developers

- The API is designed to be flexible - you can omit parameters to get broader results
- Empty response arrays are possible if no recommendations match the criteria
- The `enhanced-recommendations` endpoints provide richer data but may have higher latency
- Use the `/test` endpoints for consistent data during development
- For optimal performance, consider caching recommendations that don't change frequently
- Implement error handling for cases where the recommendation service is unavailable 