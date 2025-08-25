# Architecture

## 1. Introduction
The **Homeal** application is an Android **offline-first** app designed to generate recipes based on the ingredients available.  
It does not rely on any external server: all data is stored and accessed locally.

---

## 2. Technical choices
- **Database**: SQLite on the server and locally
- **Architecture pattern**: **MVVM** (Model View ViewModel).  
- **Data**:
  - General recipes (shipped statically with the app).  
  - Personal recipes (created by the user).  
  - Saved recipes (favorites).  
  - OpenFoodFacts data (provided statically with the app).  

---

## 3. Logical architecture

### 3.1 View (UI Tabs)
- **Calendar Tab**: displays daily meals, user can assign recipes.  
- **Fridge Tab**: displays fridge ingredients, can be updated manually or by scanning.  
- **Shopping List Tab**: lists items to buy, auto-updates when scanned.  
- **Scan Tab**: scans product barcodes, adds them to fridge or removes them from shopping list.  
- **Settings Tab**: app preferences and user options.  

### 3.2 Controllers
- Handle user interactions from each tab.  
- Communicate with the server to fetch or update data and local db.

---

## 4. Key points

- **Static and dynamic data**:  
  - Base data (global recipes + OpenFoodFacts) is on the server through a rest API.  
  - User data (personal recipes, saved recipes, fridge, shopping list) is stored in dedicated tables lcally

---

## 5. Implementation Details
- **Language**: Java/Kotlin
- **Minimum SDK**: Android API 21+ (Android 5.0)
- **Permissions**: Camera (scanning), Storage (data)

---
