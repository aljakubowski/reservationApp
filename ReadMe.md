
## Hotel Booking Integration API (MVP)

This project is an MVP integration for a new API partner handling up to 3 million booking requests per hour (approx. 833 RPS).

### 🚀 How to Run and Use

1. Start the PostgreSQL database using Docker:
   ```sh
   docker compose up -d
   ```

2. Run the Spring Boot application:
   ```sh
   mvn spring-boot:run
   ```

3. The application will automatically populate the database with mock rooms (e.g., ROOM-101 in HOTEL-A) on startup.

4. Access Swagger UI at: http://localhost:8080/swagger-ui.html

5. Available endpoints:

### Availability - Endpoints for checking hotel and room availability.

 1. Get available rooms
**Endpoint:** `GET /api/v1/availability`

   Example Request: Returns a list of rooms available in the given date range for a specific hotel.

```sh
BASE_URL="http://localhost:8080/api/v1/availability"

curl -X GET "$BASE_URL?hotelId=HOTEL-A&checkIn=2026-05-10&checkOut=2026-05-15"
```

### Bookings - Endpoints for managing user bookings.

---
**2. Create a new booking Endpoint: POST /api/v1/bookings**

   Example Request: Creates a hotel room booking. Uses the Idempotency-Key header to safely retry requests without creating duplicate bookings.
```sh
BASE_URL="http://localhost:8080/api/v1/bookings"

REQUEST_BODY='{
"userId": "user-123",
"hotelId": "HOTEL-A",
"roomId": "ROOM-101",
"checkIn": "2026-05-10",
"checkOut": "2026-05-15"
}'

curl -X POST "$BASE_URL" \
-H "Content-Type: application/json" \
-H "Idempotency-Key: uuid-1234-5678-9012" \
-d "$REQUEST_BODY"
```
---
**3. Get user bookings Endpoint: GET /api/v1/bookings**

   Example Request: Returns a paginated list of all active and cancelled bookings for a specific user.


```sh
BASE_URL="http://localhost:8080/api/v1/bookings"

curl -X GET "$BASE_URL?userId=user-123&page=0&size=20"
```
---
**4.  Get booking details Endpoint: GET /api/v1/bookings**

   Example Request: Returns details of a specific booking by its ID. Requires user ID to ensure data privacy.

```sh
BASE_URL="http://localhost:8080/api/v1/bookings/6"

curl -X GET "$BASE_URL?userId=user-123"
```
---
**5. Cancel a booking Endpoint: PATCH /api/v1/bookings/{bookingId}/cancel**

   Example Request: Cancels an existing booking. The room becomes available for other users.


```sh
BASE_URL="http://localhost:8080/api/v1/bookings/6/cancel"

curl -X PATCH "$BASE_URL?userId=user-123"
```

---
