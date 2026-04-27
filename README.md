# Madhu-Marga: Digital Beekeeper's Diary and Hive Health Monitoring System

A complete, production-ready Android app for beekeepers to manage their hives, track inspections, monitor health, and record harvests.

## Features

### Hive Management
- Add, edit, and delete hives with name and type
- View all hives in a dynamic list from Room Database
- Supports multiple hive types: Langstroth, Top Bar, Warre, Flow Hive

### Inspection System
- Record inspections with queen presence, activity level, pest detection, and honey flow
- **Rule-based alert system:**
  - Queen absent → Critical alert
  - Low activity → Warning
  - Pests detected → Danger
  - Low honey flow → Advisory
- Live alert preview while filling inspection form

### Harvest Tracking
- Record honey quantity per hive
- View harvest history per hive
- Dashboard shows total harvest across all hives

### Photo Log
- Upload images from gallery picker
- Store image URIs in Room Database
- Display images per hive in a horizontal scrollable row (LazyRow)
- Delete individual photos

### Profile & Settings
- Edit profile (name, email) with validation
- Toggle notification preferences
- Data persists across app restarts

### Special Features
- **Pro Membership dialog**: Shows "Coming Soon" message
- **Empty state handling**: Informative messages when no data exists
- **Input validation**: All forms validate input before saving

## Architecture

```
UI (Compose) → ViewModel → Repository → Room Database
```

- **MVVM** with clean separation of concerns
- **StateFlow** for reactive state management
- **Repository pattern** for data access
- No business logic in UI layer

## Tech Stack

- **Kotlin** — 100%
- **Jetpack Compose** — UI framework (no XML)
- **Room Database** — Offline-first local storage
- **Navigation Compose** — Screen navigation
- **Coil** — Image loading
- **Material 3** — Design system

## Database Entities

| Entity | Description |
|--------|-------------|
| `Hive` | Hive name, type, creation date |
| `Inspection` | Queen status, activity, pests, honey flow, notes |
| `Harvest` | Quantity in kg, date |
| `HiveImage` | Image URI, hive reference |
| `UserProfile` | Name, email, notification preferences |

## Project Structure

```
com.madhumarga/
├── data/
│   ├── db/
│   │   ├── dao/          # Data Access Objects
│   │   ├── entity/       # Room entities
│   │   └── AppDatabase.kt
│   └── repository/       # Repository layer
├── ui/
│   ├── components/       # Reusable UI components
│   ├── navigation/       # Navigation graph
│   ├── screens/          # Feature screens with ViewModels
│   └── theme/            # Material 3 theme
├── MadhuMargaApp.kt      # Application class
└── MainActivity.kt       # Entry point
```

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| Dashboard | `dashboard` | Overview with stats, alerts, quick actions |
| Hive List | `hive_list` | All hives with edit/delete |
| Add/Edit Hive | `add_hive`, `edit_hive/{id}` | Form for hive management |
| Hive Detail | `hive_detail/{id}` | Photos, inspections, harvests |
| Inspection | `inspection/{id}` | Inspection form with alert preview |
| Harvest | `harvest/{id}` | Record and view harvest history |
| Profile | `profile` | User profile and settings |

## Building

```bash
./gradlew assembleDebug
```

## Requirements

- Android Studio Hedgehog or later
- Minimum SDK: 26 (Android 8.0)
- Target SDK: 34 (Android 14)
- JDK 17
