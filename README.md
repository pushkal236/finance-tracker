# Finance Tracker (Java)

Finance tracker with MongoDB. Supports CLI, JavaFX Desktop, and Web (React + Spring Boot).

## Prerequisites
- Java 17+
- Node.js 18+ (for React dev server)
- MongoDB (local or Atlas)
- Optional: MongoDB Compass

## Configure Mongo
PowerShell example:
```powershell
$env:MONGODB_URI = "mongodb://localhost:27017"
$env:MONGODB_DB = "finance_tracker"
```

## Run Web (React + Spring Boot)
Terminal 1 (backend):
```powershell
mvn -q -DskipTests spring-boot:run -Dspring-boot.run.mainClass=com.financetracker.web.WebApplication
```
Terminal 2 (frontend):
```powershell
cd frontend
npm install
npm run dev
```
Open http://localhost:5173

## Run Desktop (JavaFX)
```powershell
mvn -q -DskipTests compile
mvn -q -DskipTests exec:java -Dexec.mainClass=com.financetracker.ui.MainApp
```

## Run CLI
```powershell
mvn -q -DskipTests exec:java -Dexec.mainClass=com.financetracker.App
```

## API Endpoints
- GET `/api/transactions?from=yyyy-MM-dd&to=yyyy-MM-dd`
- POST `/api/transactions` { date, amount, type, category, note }
- GET `/api/balance`
- GET `/api/reports/monthly?year=YYYY&month=MM`

## View data in Compass
- Connect using `MONGODB_URI`
- Open database `MONGODB_DB`
- Collection: `transactions`

## Features
- Add income/expense
- List transactions by date range
- Dashboard: balance + monthly report + expenses by category

## Project Structure
- `src/main/java/com/financetracker/ui/MainApp.java`: JavaFX UI
- `src/main/java/com/financetracker/App.java`: CLI
- `src/main/java/com/financetracker/model/*`: Domain models
- `src/main/java/com/financetracker/repo/*`: MongoDB database and repository
- `src/main/java/com/financetracker/service/*`: Business logic
