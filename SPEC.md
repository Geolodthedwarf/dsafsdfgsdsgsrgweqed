# LibreLibraria Android - Project Specification

## 1. Project Overview

**Project Name:** LibreLibraria  
**Type:** Android Native Application (Java)  
**Core Functionality:** A comprehensive library management system with offline-first architecture, allowing users to catalog books, manage lending/returning, track reading progress, and synchronize data with PostgreSQL servers.

## 2. Technology Stack & Choices

### Framework & Language
- **Language:** Java 17
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34

### Key Libraries/Dependencies
- **UI:** Material Design Components 1.11.0
- **Local Database:** Room 2.6.1 (SQLite abstraction)
- **Networking:** Retrofit 2.9.0 + OkHttp 4.12.0
- **JSON Parsing:** Gson 2.10.1
- **Async Operations:** RxJava 3.1.8 + RxAndroid 3.0.2
- **Image Loading:** Glide 4.16.0
- **Charts:** MPAndroidChart 3.1.0
- **Barcode Scanning:** ZXing 3.5.3
- **Embedded Server:** NanoHTTPD 2.3.1 (for server mode)
- **Dependency Injection:** Manual DI with Application-scoped singletons

### Architecture Pattern
- **Clean Architecture** with 3 layers:
  - **Presentation Layer:** Activities, Fragments, ViewModels
  - **Domain Layer:** Use Cases, Repository Interfaces
  - **Data Layer:** Repository Implementations, Room DAOs, Remote APIs, Local Data Sources
- **MVVM** for UI components
- **Repository Pattern** for data access
- **Offline-First** with background sync

### State Management
- LiveData for UI state observation
- RxJava for async operations and data streams
- SharedPreferences for user settings

## 3. Feature List

### Core Features
1. **Book Catalog Management**
   - Add, edit, delete books
   - Fields: title, author, ISBN, publisher, year, genre, language, copies, shelf location, tags, description, cover image
   - Search and filter books
   - Barcode scanner for ISBN lookup

2. **Lending/Returning System**
   - Lend books to borrowers
   - Track due dates and calculate late fees
   - Return books with condition notes
   - Lending history

3. **Rating & Review System**
   - 5-star rating with 0.1 precision
   - Written reviews
   - Reading status tracking (Want, Reading, Read, Own, Stopped)

4. **Reading Diary**
   - Add notes per book
   - Date-stamped entries
   - Quote capture

5. **Statistics & Visualization**
   - Total books, available, borrowed counts
   - Genre distribution charts
   - Top-rated books
   - Monthly additions chart
   - Reading status breakdown

6. **Open Library Integration**
   - Auto-fill book details from ISBN
   - Fetch cover images
   - Search by title/author

7. **Data Management**
   - Import/Export library (JSON format)
   - Backup and restore
   - Tag management

8. **Sync Capabilities**
   - Local SQLite database (offline-first)
   - PostgreSQL server synchronization (client mode)
   - Mini HTTP server for peer sync (server mode)
   - Conflict resolution

### Secondary Features
- Dark/Light theme support
- Ukrainian/English localization
- Reading progress tracking
- Multiple library support

## 4. UI/UX Design Direction

### Overall Visual Style
- **Material Design 3** with custom theming
- Clean, card-based layouts
- Smooth animations and transitions
- Accessible design with proper contrast ratios

### Color Scheme
- **Primary:** Indigo (#5c6bc0)
- **Secondary:** Purple (#7c4dff)
- **Surface:** White/Dark based on theme
- **Status Colors:**
  - Available: Green
  - Borrowed: Orange
  - Overdue: Red
  - Reading: Blue

### Layout Approach
- **Bottom Navigation** with 4 main sections:
  1. Dashboard (home, quick stats)
  2. Catalog (book list with search/filter)
  3. Lending (active loans)
  4. More (statistics, settings, diary, tags)
- **Floating Action Button** for quick add actions
- **Pull-to-refresh** for sync operations
- **Modal bottom sheets** for quick actions
- **Full-screen dialogs** for forms

### Navigation Structure
```
MainActivity (Single Activity)
├── DashboardFragment
├── CatalogFragment
│   └── BookDetailActivity
├── LendingFragment
│   ├── LendBookDialog
│   └── ReturnBookDialog
└── MoreFragment
    ├── StatisticsFragment
    ├── DiaryFragment
    ├── TagsFragment
    └── SettingsFragment
```

## 5. Database Schema (Room/PostgreSQL)

### Tables
1. **books** - Main book catalog
2. **borrowers** - Borrower information
3. **loans** - Lending records
4. **ratings** - Book ratings and reviews
5. **diary_entries** - Reading diary notes
6. **tags** - Book tags
7. **audit_log** - Action history
8. **sync_metadata** - Sync tracking

## 6. API Design (for server sync)

### REST Endpoints
- `GET/POST /api/books` - List/Create books
- `GET/PUT/DELETE /api/books/{id}` - CRUD operations
- `POST /api/books/sync` - Bulk sync
- `GET/POST /api/loans` - Lending operations
- `GET /api/statistics` - Dashboard stats

### Sync Protocol
- Last-modified timestamp based
- Conflict resolution: server wins with local backup
- Delta sync for efficiency
