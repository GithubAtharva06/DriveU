# DriveU Backend Service

Technical documentation for the DriveU backend application, a RESTful ride-hailing service built with Spring Boot and PostgreSQL.

---

## Table of Contents

- [1. Technology Stack](#1-technology-stack)
- [2. Architecture](#2-architecture)
- [3. Project Structure](#3-project-structure)
- [4. Domain Model](#4-domain-model)
  - [Entities](#entities)
  - [Enumerations](#enumerations)
- [5. Ride Lifecycle & State Machine](#5-ride-lifecycle--state-machine)
  - [State Transitions](#state-transitions)
  - [Cancellation Rules](#cancellation-rules)
- [6. Ride Dispatch & Offer System](#6-ride-dispatch--offer-system)
  - [Nearby Discovery & Filtering](#nearby-discovery--filtering)
  - [Offer Expiry & Concurrency Resolution](#offer-expiry--concurrency-resolution)
  - [Dynamic Out-of-Range Invalidation](#dynamic-out-of-range-invalidation)
- [7. Driver Location Tracking](#7-driver-location-tracking)
  - [Geospatial Math](#geospatial-math)
- [8. Validation](#8-validation)
- [9. Exception Handling](#9-exception-handling)
  - [Error Envelope Schema](#error-envelope-schema)
  - [HTTP Status Code Mappings](#http-status-code-mappings)
- [10. API Reference](#10-api-reference)
  - [Driver Endpoints (`/drivers`)](#driver-endpoints-drivers)
  - [Passenger Endpoints (`/passengers`)](#passenger-endpoints-passengers)
  - [Admin Endpoints (`/admins`)](#admin-endpoints-admins)
  - [Ride Endpoints (`/rides`)](#ride-endpoints-rides)
  - [Location Endpoints (`/locations`)](#location-endpoints-locations)
  - [Ride Offer Endpoints (`/ride-offers`)](#ride-offer-endpoints-ride-offers)
- [11. Example Requests & Responses](#11-example-requests--responses)
- [12. Configuration](#12-configuration)
- [13. Local Development](#13-local-development)
- [14. Current Implementation Status](#14-current-implementation-status)

---

## 1. Technology Stack

- **Runtime**: Java 25
- **Framework**: Spring Boot 4.1.1 (`spring-boot-starter-web`)
- **Data Access**: Spring Data JPA / Hibernate (`spring-boot-starter-data-jpa`)
- **Database**: PostgreSQL (Driver: `org.postgresql:postgresql`, runtime scope)
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **Boilerplate Reduction**: Project Lombok (`org.projectlombok:lombok`)
- **Developer Tools**: Spring Boot DevTools 4.1.0 (`spring-boot-devtools`)
- **Primary Identifiers**: UUIDv4 (`java.util.UUID`) across all primary and foreign keys
- **API Style**: Synchronous RESTful JSON over HTTP
- **Build System**: Apache Maven (`mvnw` wrapper provided)

---

## 2. Architecture

The application implements a classical layered architecture with clear boundaries between HTTP handling, business logic, persistence, and database storage:

```
+--------------------------------------------------------------------+
|                         HTTP Clients / Consumers                   |
+--------------------------------------------------------------------+
                                   │
                              JSON │ HTTP Requests
                                   ▼
+--------------------------------------------------------------------+
| Controller Layer (`com.driveu.driverapp.controller`)               |
| - Endpoints, path mapping, HTTP status handling                   |
| - DTO binding and Jakarta Validation (`@Valid`)                   |
+--------------------------------------------------------------------+
                                   │
                        DTOs / IDs │
                                   ▼
+--------------------------------------------------------------------+
| Service Layer (`com.driveu.driverapp.service`)                     |
| - Domain business rules, state transition validation               |
| - Geospatial calculations (Haversine formula)                      |
| - Concurrency resolution, transactional boundaries (`@Transactional`)|
+--------------------------------------------------------------------+
                                   │
                         Entity    │ JPA Queries
                                   ▼
+--------------------------------------------------------------------+
| Repository Layer (`com.driveu.driverapp.repository`)               |
| - Spring Data JPA Repositories (`JpaRepository<T, UUID>`)          |
| - Derived query methods and database existence checks              |
+--------------------------------------------------------------------+
                                   │
                                   ▼
+--------------------------------------------------------------------+
| Database Layer                                                     |
| - PostgreSQL Relational Database                                   |
+--------------------------------------------------------------------+
```

Cross-cutting components:
- **DTOs (`com.driveu.driverapp.dto`)**: Separate inbound command structures (`Request/`) from outbound representations (`Response/`) to prevent leaking internal database schemas.
- **Exception Interceptor (`com.driveu.driverapp.exception`)**: Centralized `@RestControllerAdvice` catching domain, validation, and system exceptions to produce uniform JSON error responses.

---

## 3. Project Structure

```
driverapp/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/driveu/driverapp/
    │   │   ├── DriverappApplication.java          # Spring Boot main class
    │   │   ├── controller/                        # REST Controllers
    │   │   │   ├── AdminController.java
    │   │   │   ├── DriverController.java
    │   │   │   ├── DriverLocationController.java
    │   │   │   ├── PassengerController.java
    │   │   │   ├── RideController.java
    │   │   │   └── RideOfferController.java
    │   │   ├── dto/
    │   │   │   ├── Request/                       # Inbound request bodies
    │   │   │   │   ├── AdminRequest.java
    │   │   │   │   ├── AvailabilityUpdateRequest.java
    │   │   │   │   ├── DriverRequest.java
    │   │   │   │   ├── LocationUpdateRequest.java
    │   │   │   │   ├── PassengerRequest.java
    │   │   │   │   ├── RideOfferRequest.java
    │   │   │   │   ├── RideRequest.java
    │   │   │   │   └── RideStatusUpdateRequest.java
    │   │   │   └── Response/                      # Outbound response bodies
    │   │   │       ├── AdminResponse.java
    │   │   │       ├── DriverLocationResponse.java
    │   │   │       ├── DriverResponse.java
    │   │   │       ├── NearbyDriverResponse.java
    │   │   │       ├── PassengerResponse.java
    │   │   │       ├── RideOfferResponse.java
    │   │   │       └── RideResponse.java
    │   │   ├── entities/                          # JPA Entities & Enums
    │   │   │   ├── Admin.java
    │   │   │   ├── Driver.java
    │   │   │   ├── DriverLocation.java
    │   │   │   ├── OfferStatus.java
    │   │   │   ├── Passenger.java
    │   │   │   ├── Ride.java
    │   │   │   ├── RideOffer.java
    │   │   │   ├── RideStatus.java
    │   │   │   └── StatusCheck.java
    │   │   ├── exception/                         # Error handling components
    │   │   │   ├── BusinessException.java
    │   │   │   ├── DuplicateResourceException.java
    │   │   │   ├── ErrorResponse.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── ResourceNotFoundException.java
    │   │   ├── repository/                        # Spring Data Repositories
    │   │   │   ├── AdminRepository.java
    │   │   │   ├── DriverLocationRepository.java
    │   │   │   ├── DriverRepository.java
    │   │   │   ├── PassengerRepository.java
    │   │   │   ├── RideOfferRepository.java
    │   │   │   └── RideRepository.java
    │   │   └── service/                           # Business logic implementations
    │   │       ├── AdminService.java
    │   │       ├── DriverLocationService.java
    │   │       ├── DriverService.java
    │   │       ├── PassengerService.java
    │   │       ├── RideOfferService.java
    │   │       └── RideService.java
    │   └── resources/
    │       └── application.properties             # Spring configuration file
    └── test/
        └── java/com/driveu/driverapp/
            └── DriverappApplicationTests.java     # Basic test context
```

---

## 4. Domain Model

### Entities

| Entity | Primary Key | Key Attributes & Relations | Description |
|---|---|---|---|
| **`Driver`** | `UUID id` | `phoneNo` (unique), `userName`, `email` (unique), `password`, `licenseNumber` (unique), `status` (`StatusCheck`), `createdAt` | Driver account record. Defaults to `status = OFFLINE`. |
| **`Passenger`** | `UUID id` | `phoneNo` (unique), `userName`, `email` (unique), `password`, `createdAt`, `active` (boolean) | Passenger account record. Tracks whether the passenger account is active. |
| **`Admin`** | `UUID id` | `phoneNo` (unique), `userName`, `email` (unique), `password`, `createdAt` | System administrator account record. |
| **`DriverLocation`** | `UUID locationId` | `driver` (`OneToOne`, foreign key `driver_id`, unique), `latitude`, `longitude`, `available` (boolean), `updatedAt` | Geographic coordinate storage for a driver. |
| **`Ride`** | `UUID rideId` | `passenger` (`ManyToOne`), `driver` (`ManyToOne`), `rideStatus` (`RideStatus`), `pickupLocation`, `dropLocation`, `pickupLatitude`, `pickupLongitude`, `dropLatitude`, `dropLongitude`, `fare`, `pickupAt`, `dropOffAt` | Core trip entity holding routing coordinates, assignment, and status. |
| **`RideOffer`** | `UUID offerId` | `ride` (`ManyToOne`), `driver` (`ManyToOne`), `offerStatus` (`OfferStatus`), `createdAt`, `expiresAt` | Time-bound offer dispatched to a nearby driver for a requested ride. |

### Enumerations

- **`RideStatus`**: `REQUESTED`, `ACCEPTED`, `ARRIVING`, `ARRIVED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`
- **`OfferStatus`**: `PENDING`, `ACCEPTED`, `DECLINED`, `EXPIRED`, `RIDE_BOOKED`, `CANCELLED`
- **`StatusCheck`**: `ONLINE`, `OFFLINE`

---

## 5. Ride Lifecycle & State Machine

The ride lifecycle is managed by `RideService` as an explicit unidirectional state machine.

```
       [POST /rides]
             │
             ▼
       +-----------+
       | REQUESTED | ──(Cancel by Passenger)──► +-----------+
       +-----------+                            | CANCELLED |
             │                                  +-----------+
    (Driver Accepts)                                  ▲
             │                                        │
             ▼                                        │
       +-----------+                                  │
       | ACCEPTED  | ──(Cancel by Passenger)──────────┤
       +-----------+                                  │
             │                                        │
      (Driver Updates)                                │
             │                                        │
             ▼                                        │
       +-----------+                                  │
       | ARRIVING  | ──(Cancel by Passenger)──────────┤
       +-----------+                                  │
             │                                        │
      (Driver Updates)                                │
             │                                        │
             ▼                                        │
       +-----------+                                  │
       |  ARRIVED  | ──(Cancel by Passenger)──────────┘
       +-----------+
             │
     (Trip Starts: records pickupAt)
             │
             ▼
       +-------------+
       | IN_PROGRESS |   [Cancellation Prohibited]
       +-------------+
             │
      (Trip Ends: records dropOffAt)
             │
             ▼
       +-----------+
       | COMPLETED |     [Terminal State]
       +-----------+
```

### State Transitions

1. **`REQUESTED`**:
   - Created via `POST /rides`.
   - Preconditions: Passenger must exist, `passenger.active == true`, and passenger must not have an active ride in `[REQUESTED, ACCEPTED, ARRIVING, ARRIVED, IN_PROGRESS]`.
2. **`ACCEPTED`**:
   - Reached when a driver accepts via `PATCH /rides/{rideId}/accept` or accepts a dispatched offer via `PATCH /ride-offers/{offerId}/accept/{driverId}`.
   - Preconditions: Ride must be in `REQUESTED` status, driver must have `status == ONLINE`, and driver must not have an active ride in `[ACCEPTED, ARRIVING, ARRIVED, IN_PROGRESS]`.
3. **`ARRIVING`**:
   - Reached via `PATCH /rides/{rideId}/status?driverId={id}` with body `{"rideStatus": "ARRIVING"}`.
   - Precondition: Valid only from `ACCEPTED`. Driver ID must match assigned driver.
4. **`ARRIVED`**:
   - Reached via status update endpoint with body `{"rideStatus": "ARRIVED"}`.
   - Precondition: Valid only from `ARRIVING`. Driver ID must match assigned driver.
5. **`IN_PROGRESS`**:
   - Reached via status update endpoint with body `{"rideStatus": "IN_PROGRESS"}`.
   - Precondition: Valid only from `ARRIVED`. Driver ID must match assigned driver.
   - Mutation: Automatically assigns `pickupAt = LocalDateTime.now()`. Rejects if `pickupAt` is already set.
6. **`COMPLETED`**:
   - Reached via status update endpoint with body `{"rideStatus": "COMPLETED"}`.
   - Precondition: Valid only from `IN_PROGRESS`. Requires `pickupAt` to be present.
   - Mutation: Automatically assigns `dropOffAt = LocalDateTime.now()`.

### Cancellation Rules

- Handled via `PATCH /rides/{rideId}/cancel?passengerId={id}`.
- Allowed only if `ride.passenger.id == passengerId`.
- Allowed exclusively when current status is `REQUESTED`, `ACCEPTED`, `ARRIVING`, or `ARRIVED`.
- Attempting cancellation when status is `IN_PROGRESS`, `COMPLETED`, or `CANCELLED` throws a `BusinessException`.

---

## 6. Ride Dispatch & Offer System

The ride dispatch workflow broadcasts time-limited offers to nearby online drivers:

### Nearby Discovery & Filtering

1. Invoked via `POST /ride-offers/generate/{rideId}`.
2. Validates that the ride is in `REQUESTED` status.
3. Loads all records from `driver_location`.
4. Filters out any driver where `driver.status != ONLINE`.
5. Calculates the Haversine distance between driver coordinates and ride `pickupLatitude` / `pickupLongitude`.
6. Enforces a maximum radius threshold of **`2.0 km`**.
7. Sorts candidate drivers ascending by distance.
8. Excludes drivers who already have an offer for this ride via `rideOfferRepository.existsByRide_RideIdAndDriver_Id`.
9. Persists a new `RideOffer` per eligible driver with:
   - `offerStatus = PENDING`
   - `createdAt = now`
   - `expiresAt = now + 1 minute`

### Offer Expiry & Concurrency Resolution

When a driver accepts an offer via `PATCH /ride-offers/{offerId}/accept/{driverId}`:
1. Validates offer ownership: `offer.driver.id == driverId`.
2. Validates offer status: must be `PENDING`.
3. Verifies time validity: if `now >= offer.expiresAt`, transitions offer to `EXPIRED` and throws `BusinessException`.
4. Verifies ride status: ride must still be `REQUESTED`.
5. Verifies driver status: driver must still be `ONLINE`.
6. Verifies driver proximity: recalculates distance to pickup; if `> 2.0 km`, marks offer `EXPIRED` and throws `BusinessException`.
7. Verifies active rides: driver cannot have an existing active ride (`ACCEPTED`, `ARRIVING`, `ARRIVED`, `IN_PROGRESS`).
8. Transitions ride to `ACCEPTED` and assigns driver to ride.
9. Transitions current offer to `ACCEPTED`.
10. **Closes competing offers**: Finds all other `PENDING` offers for that `rideId` and updates their status to **`RIDE_BOOKED`**, preventing duplicate acceptance.

### Dynamic Out-of-Range Invalidation

Inside `DriverLocationService.updateDriverLocation`, every coordinate update triggers:
- Query for all `PENDING` offers belonging to the updating driver.
- For each offer, if `now >= offer.expiresAt` OR distance to pickup exceeds `2.0 km`, the offer status is immediately updated to `EXPIRED`.

---

## 7. Driver Location Tracking

Driver coordinates are maintained in the `driver_location` table:

- **Entity Structure**:
  - `locationId` (UUID, primary key)
  - `driver` (Driver, unique foreign key)
  - `latitude` (Double, range: `[-90.0, 90.0]`)
  - `longitude` (Double, range: `[-180.0, 180.0]`)
  - `available` (Boolean, defaults to `false`)
  - `updatedAt` (LocalDateTime)

### Geospatial Math

Distances are computed in Java using the spherical Haversine formula with mean Earth radius $R = 6371.0\text{ km}$:

$$\Delta\phi = \text{radians}(\text{lat}_2 - \text{lat}_1), \quad \Delta\lambda = \text{radians}(\text{lon}_2 - \text{lon}_1)$$

$$a = \sin^2\left(\frac{\Delta\phi}{2}\right) + \cos(\text{radians}(\text{lat}_1)) \cdot \cos(\text{radians}(\text{lat}_2)) \cdot \sin^2\left(\frac{\Delta\lambda}{2}\right)$$

$$c = 2 \cdot \text{atan2}\left(\sqrt{a}, \sqrt{1 - a}\right)$$

$$d = R \cdot c$$

---

## 8. Validation

Requests are validated using Jakarta Validation constraints (`@Valid`):

- **Indian Phone Number Format**:
  - `@Pattern(regexp = "^(?:\\+91|91)?[6-9]\\d{9}$")`
  - Validates 10-digit Indian numbers with optional `+91` or `91` prefixes.
  - Applied to `DriverRequest`, `PassengerRequest`, `AdminRequest`.
- **Password Complexity**:
  - `@Size(min = 8, max = 100)`
  - `@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*]).{8,}$")`
  - Requires at least one uppercase letter, one lowercase letter, one digit, and one special character.
  - Applied to `DriverRequest`, `PassengerRequest`, `AdminRequest`.
- **Email**:
  - `@Email` and `@NotBlank` on `DriverRequest`, `PassengerRequest`, `AdminRequest`.
- **Geographic Coordinates**:
  - Latitude: `@NotNull`, `@DecimalMin("-90.0")`, `@DecimalMax("90.0")`
  - Longitude: `@NotNull`, `@DecimalMin("-180.0")`, `@DecimalMax("180.0")`
  - Applied to `LocationUpdateRequest` and coordinate fields in `RideRequest`.
- **Required Fields**:
  - `@NotBlank` for strings, `@NotNull` for UUIDs and enum values (`RideStatusUpdateRequest`, `RideRequest`, `RideOfferRequest`).

---

## 9. Exception Handling

Centralized exception handling is implemented in `GlobalExceptionHandler` using `@RestControllerAdvice`.

### Error Envelope Schema

All errors produce a consistent JSON payload:

```json
{
  "status": 400,
  "message": "Ride is no longer available",
  "timestamp": "2026-09-23T12:00:00.000000"
}
```

### HTTP Status Code Mappings

| Exception Class | HTTP Status | Handling Logic |
|---|---|---|
| `ResourceNotFoundException` | **404 Not Found** | Returns exception message indicating missing entity ID. |
| `DuplicateResourceException` | **409 Conflict** | Returns conflict message when phone, email, or license already exists. |
| `BusinessException` | **400 Bad Request** | Returns domain rule rejection message (e.g. invalid status transitions, out-of-range accept, driver offline). |
| `MethodArgumentNotValidException` | **400 Bad Request** | Extracts the first failed field error formatted as `<field>: <message>`. |
| `ConstraintViolationException` | **400 Bad Request** | Returns entity constraint violation details. |
| `Exception` (catch-all) | **500 Internal Server Error** | Returns `"An unexpected error occurred"`. |

---

## 10. API Reference

### Driver Endpoints (`/drivers`)

| Method | Endpoint | Request Body / Params | Response Status | Description |
|---|---|---|---|---|
| `POST` | `/drivers` | Body: `DriverRequest` | `201 Created` | Register a new driver. |
| `GET` | `/drivers/{id}` | Path: `id` (UUID) | `200 OK` | Fetch driver by ID. |
| `GET` | `/drivers` | - | `200 OK` | List all drivers. |
| `PUT` | `/drivers/{id}` | Path: `id` (UUID), Body: `DriverRequest` | `200 OK` | Update driver profile. |
| `PATCH` | `/drivers/{id}/deactivate` | Path: `id` (UUID) | `204 No Content` | Set driver status to `OFFLINE`. |
| `PATCH` | `/drivers/{id}/online` | Path: `id` (UUID) | `204 No Content` | Set driver status to `ONLINE`. |
| `PATCH` | `/drivers/{id}/offline` | Path: `id` (UUID) | `204 No Content` | Set driver status to `OFFLINE`. |
| `GET` | `/drivers/{id}/availability` | Path: `id` (UUID) | `200 OK` | Returns boolean (`true` if `ONLINE`). |
| `DELETE` | `/drivers/{id}` | Path: `id` (UUID) | `204 No Content` | Delete driver by ID. |

### Passenger Endpoints (`/passengers`)

| Method | Endpoint | Request Body / Params | Response Status | Description |
|---|---|---|---|---|
| `POST` | `/passengers` | Body: `PassengerRequest` | `201 Created` | Register a new passenger. |
| `GET` | `/passengers/{id}` | Path: `id` (UUID) | `200 OK` | Fetch passenger by ID. |
| `GET` | `/passengers` | - | `200 OK` | List all passengers. |
| `PUT` | `/passengers/{id}` | Path: `id` (UUID), Body: `PassengerRequest` | `200 OK` | Update passenger profile. |
| `PATCH` | `/passengers/{id}/deactivate` | Path: `id` (UUID) | `204 No Content` | Set `active = false`. |
| `PATCH` | `/passengers/{id}/reactivate` | Path: `id` (UUID) | `204 No Content` | Set `active = true`. |
| `DELETE` | `/passengers/{id}` | Path: `id` (UUID) | `204 No Content` | Delete passenger by ID. |

### Admin Endpoints (`/admins`)

| Method | Endpoint | Request Body / Params | Response Status | Description |
|---|---|---|---|---|
| `POST` | `/admins` | Body: `AdminRequest` | `201 Created` | Register an admin. |
| `GET` | `/admins/{id}` | Path: `id` (UUID) | `200 OK` | Fetch admin by ID. |
| `GET` | `/admins` | - | `200 OK` | List all admins. |
| `PUT` | `/admins/{id}` | Path: `id` (UUID), Body: `AdminRequest` | `200 OK` | Update admin. |
| `DELETE` | `/admins/{id}` | Path: `id` (UUID) | `204 No Content` | Delete admin by ID. |
| `GET` | `/admins/passengers` | - | `200 OK` | Admin view: list all passengers. |
| `GET` | `/admins/passengers/{id}` | Path: `id` (UUID) | `200 OK` | Admin view: fetch passenger by ID. |
| `PATCH` | `/admins/passengers/{id}/deactivate` | Path: `id` (UUID) | `204 No Content` | Admin deactivates passenger. |
| `DELETE` | `/admins/passengers/{id}` | Path: `id` (UUID) | `204 No Content` | Admin deletes passenger. |
| `DELETE` | `/admins/passengers` | Body: `List<UUID>` | `204 No Content` | Admin batch-deletes passengers. |
| `GET` | `/admins/drivers` | - | `200 OK` | Admin view: list all drivers. |
| `GET` | `/admins/drivers/{id}` | Path: `id` (UUID) | `200 OK` | Admin view: fetch driver by ID. |
| `PATCH` | `/admins/drivers/{id}/deactivate` | Path: `id` (UUID) | `204 No Content` | Admin deactivates driver (`OFFLINE`). |
| `DELETE` | `/admins/drivers/{id}` | Path: `id` (UUID) | `204 No Content` | Admin deletes driver. |
| `DELETE` | `/admins/drivers` | Body: `List<UUID>` | `204 No Content` | Admin batch-deletes drivers. |

### Ride Endpoints (`/rides`)

| Method | Endpoint | Request Body / Params | Response Status | Description |
|---|---|---|---|---|
| `POST` | `/rides` | Body: `RideRequest` | `201 Created` | Create new ride in `REQUESTED` status. |
| `GET` | `/rides/{rideId}` | Path: `rideId` (UUID) | `200 OK` | Fetch ride by ID. |
| `GET` | `/rides` | - | `200 OK` | List all rides. |
| `GET` | `/rides/passenger/{passengerId}` | Path: `passengerId` (UUID) | `200 OK` | List rides for a passenger. |
| `GET` | `/rides/driver/{driverId}` | Path: `driverId` (UUID) | `200 OK` | List rides for a driver. |
| `PATCH` | `/rides/{rideId}/accept` | Path: `rideId` (UUID), Query: `driverId` (UUID) | `200 OK` | Assign driver directly to ride (`ACCEPTED`). |
| `PATCH` | `/rides/{rideId}/cancel` | Path: `rideId` (UUID), Query: `passengerId` (UUID) | `200 OK` | Passenger cancels ride (`CANCELLED`). |
| `PATCH` | `/rides/{rideId}/status` | Path: `rideId` (UUID), Query: `driverId` (UUID), Body: `RideStatusUpdateRequest` | `200 OK` | Update ride status (`ARRIVING`, `ARRIVED`, `IN_PROGRESS`, `COMPLETED`). |

### Location Endpoints (`/locations`)

| Method | Endpoint | Request Body / Params | Response Status | Description |
|---|---|---|---|---|
| `PUT` | `/locations/driver/{driverId}` | Path: `driverId` (UUID), Body: `LocationUpdateRequest` | `200 OK` | Ingest driver coordinates; invalidates out-of-range offers. |
| `GET` | `/locations/nearby` | Query: `latitude` (Double), `longitude` (Double) | `200 OK` | Returns online drivers within 2.0 km, sorted nearest first. |

### Ride Offer Endpoints (`/ride-offers`)

| Method | Endpoint | Request Body / Params | Response Status | Description |
|---|---|---|---|---|
| `POST` | `/ride-offers/generate/{rideId}` | Path: `rideId` (UUID) | `201 Created` | Broadcast offers (1-min expiry) to online drivers within 2 km. |
| `GET` | `/ride-offers/{offerId}` | Path: `offerId` (UUID) | `200 OK` | Fetch offer by ID. |
| `GET` | `/ride-offers/ride/{rideId}` | Path: `rideId` (UUID) | `200 OK` | List all offers generated for a ride. |
| `GET` | `/ride-offers/driver/{driverId}` | Path: `driverId` (UUID) | `200 OK` | List all offers received by a driver. |
| `PATCH` | `/ride-offers/{offerId}/accept/{driverId}` | Path: `offerId` (UUID), `driverId` (UUID) | `200 OK` | Driver accepts offer; closes competing offers as `RIDE_BOOKED`. |
| `PATCH` | `/ride-offers/{offerId}/decline/{driverId}` | Path: `offerId` (UUID), `driverId` (UUID) | `200 OK` | Driver declines offer (marked `DECLINED`). |

---

## 11. Example Requests & Responses

### 1. Driver Registration (`POST /drivers`)

**Request**:
```json
POST /drivers
Content-Type: application/json

{
  "phoneNo": "+919876543210",
  "userName": "rajesh_kumar",
  "email": "rajesh.kumar@example.com",
  "password": "StrongPassword@123",
  "licenseNumber": "KA0120200001234"
}
```

**Response (`201 Created`)**:
```json
{
  "id": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "phoneNo": "+919876543210",
  "userName": "rajesh_kumar",
  "email": "rajesh.kumar@example.com",
  "licenseNumber": "KA0120200001234",
  "status": "OFFLINE",
  "createdAt": "2026-09-23T10:15:30"
}
```

---

### 2. Location Update (`PUT /locations/driver/{driverId}`)

**Request**:
```json
PUT /locations/driver/a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d
Content-Type: application/json

{
  "latitude": 12.9352,
  "longitude": 77.6245
}
```

**Response (`200 OK`)**:
```json
{
  "locationId": "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e",
  "driverId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "latitude": 12.9352,
  "longitude": 77.6245,
  "updatedAt": "2026-09-23T10:20:00"
}
```

---

### 3. Ride Creation (`POST /rides`)

**Request**:
```json
POST /rides
Content-Type: application/json

{
  "passengerId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "pickupLocation": "Koramangala 5th Block, Bengaluru",
  "pickupLatitude": 12.9352,
  "pickupLongitude": 77.6245,
  "dropLocation": "Indiranagar 100 Feet Road, Bengaluru",
  "dropLatitude": 12.9716,
  "dropLongitude": 77.6412
}
```

**Response (`201 Created`)**:
```json
{
  "rideId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "passengerId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "driverId": null,
  "rideStatus": "REQUESTED",
  "pickupLocation": "Koramangala 5th Block, Bengaluru",
  "pickupLatitude": 12.9352,
  "pickupLongitude": 77.6245,
  "dropLocation": "Indiranagar 100 Feet Road, Bengaluru",
  "dropLatitude": 12.9716,
  "dropLongitude": 77.6412,
  "fare": null,
  "pickupAt": null,
  "dropOffAt": null
}
```

---

### 4. Ride Offer Generation (`POST /ride-offers/generate/{rideId}`)

**Request**:
```json
POST /ride-offers/generate/7c9e6679-7425-40de-944b-e07fc1f90ae7
```

**Response (`201 Created`)**:
```json
[
  {
    "offerId": "9d8e7f6a-5b4c-3d2e-1f0a-9b8c7d6e5f4a",
    "rideId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "driverId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    "offerStatus": "PENDING",
    "createdAt": "2026-09-23T10:25:00",
    "expiresAt": "2026-09-23T10:26:00"
  }
]
```

---

### 5. Ride Status Update (`PATCH /rides/{rideId}/status?driverId={driverId}`)

**Request**:
```json
PATCH /rides/7c9e6679-7425-40de-944b-e07fc1f90ae7/status?driverId=a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d
Content-Type: application/json

{
  "rideStatus": "ARRIVING"
}
```

**Response (`200 OK`)**:
```json
{
  "rideId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "passengerId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "driverId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "rideStatus": "ARRIVING",
  "pickupLocation": "Koramangala 5th Block, Bengaluru",
  "pickupLatitude": 12.9352,
  "pickupLongitude": 77.6245,
  "dropLocation": "Indiranagar 100 Feet Road, Bengaluru",
  "dropLatitude": 12.9716,
  "dropLongitude": 77.6412,
  "fare": null,
  "pickupAt": null,
  "dropOffAt": null
}
```

---

### 6. Error Response (`400 Bad Request`)

**Response (`400 Bad Request`)**:
```json
{
  "status": 400,
  "message": "Driver must be online to accept a ride",
  "timestamp": "2026-09-23T10:30:15.123456"
}
```

---

## 12. Configuration

Application configuration is declared in `src/main/resources/application.properties`:

```properties
spring.application.name=driverapp

spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### PostgreSQL Configuration & Environment Variables

The datasource properties reference external environment variables. The application relies on standard Spring property substitution; it does not parse `.env` files automatically.

The following variables must be set in the host environment or terminal session:

| Environment Variable | Description | Example / Placeholder |
|---|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost:5432/driveu` |
| `SPRING_DATASOURCE_USERNAME` | Database user | `YOUR_DB_USERNAME` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `YOUR_DB_PASSWORD` |

---

## 13. Local Development

### Prerequisites

- **Java Development Kit (JDK) 25**
- **PostgreSQL 14+**
- **Git**

### Database Setup

Create the target PostgreSQL database:

```sql
CREATE DATABASE driveu;
```

### Build & Run

1. Export the environment variables:
   ```bash
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/driveu
   export SPRING_DATASOURCE_USERNAME=YOUR_DB_USERNAME
   export SPRING_DATASOURCE_PASSWORD=YOUR_DB_PASSWORD
   ```

2. Compile the project with the included Maven wrapper:
   ```bash
   ./mvnw clean compile
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

4. The application starts on port `8080` by default.

---

## 14. Current Implementation Status

| Capability | Status | Implementation Details |
|---|---|---|
| **Driver Management** | Implemented | Registration, profile updates, online/offline status toggles, availability check. |
| **Passenger Management** | Implemented | Registration, profile updates, active status toggling. |
| **Admin Operations** | Implemented | Admin CRUD, passenger/driver deactivation, single & batch deletion. |
| **Location Ingestion** | Implemented | Coordinate storage, bounding validation, Haversine proximity computation. |
| **Proximity Dispatch (2 km)** | Implemented | Discovery of online drivers within 2.0 km, sorted by distance. |
| **1-Minute Ride Offers** | Implemented | Time-limited offers, auto-closure of competing offers as `RIDE_BOOKED`. |
| **Deterministic State Machine** | Implemented | State transitions `REQUESTED` through `COMPLETED`, cancellation constraints. |
| **Exception Handling** | Implemented | Centralized `@RestControllerAdvice` mapping exceptions to standard JSON envelopes. |
| **Spring Security & JWT** | Planned | *Not implemented.* Endpoints are unauthenticated; no security filters or tokens. |
| **Password Hashing (BCrypt)** | Planned | *Not implemented.* Passwords are currently stored in plain text. |
| **Database Migrations (Flyway)** | Planned | *Not implemented.* Schema generation is currently handled via `hibernate.ddl-auto=update`. |
| **In-Memory Caching (Redis)** | Planned | *Not implemented.* Driver coordinates are read from and written to PostgreSQL. |
| **Real-Time Push (WebSockets)** | Planned | *Not implemented.* All driver/passenger interactions require HTTP polling. |
| **Payment Gateway Integration** | Planned | *Not implemented.* The `fare` field exists on `Ride`, but no billing logic or gateways are integrated. |
| **Ratings & Feedback** | Planned | *Not implemented.* No rating entities or review submission endpoints exist. |
| **Docker & CI/CD** | Planned | *Not implemented.* No Dockerfile, docker-compose, or CI/CD pipelines are configured. |
| **PostGIS Integration** | Planned | *Not implemented.* Geospatial filtering currently computes distances in-memory using Java. |
