# SyntecxHub ToDo App 📋

A full-featured Android ToDo application built with **Kotlin** during my internship at **SyntecxHub**.

---

## ✨ Features

### 🔐 Authentication
- Email & Password login/register
- Guest mode with limited features
- Session management with SharedPreferences

### ✅ Task Management
- Add, Edit, Delete tasks
- Mark tasks as complete/incomplete
- Swipe to delete with Undo
- Priority levels — High, Medium, Low
- Categories — General, Work, Personal, Shopping, Health, Study, Finance

### ⏰ Reminders
- Full calendar date picker
- 12-hour & 24-hour time format (user preference)
- Exact alarm notifications via AlarmManager
- Heads-up push notifications

### 📊 Statistics
- Total / Done / Pending task counts
- Priority breakdown with progress bars
- Category breakdown
- Motivational progress messages

### 🔍 Search & Filter
- Real-time task search
- Filter by priority (High / Medium / Low)

### 👤 Profile
- User avatar with initials
- Dark mode toggle 🌙
- Time format preference (12hr / 24hr)
- Logout with confirmation

### 🔒 Guest Restrictions
- Reminders, Stats, Search, Profile locked for guests
- Friendly "Login to unlock" dialogs

---

## 🏗️ Architecture

```
MVVM + Repository Pattern
├── Model    → Room Database (Task, User entities)
├── ViewModel → TaskViewModel, StateFlow
├── Repository → TaskRepository, UserRepository
└── View     → Activities, Fragments, Adapters
```

---

## 🛠️ Tech Stack

| Technology | Usage |
|---|---|
| **Kotlin** | Primary language |
| **Room DB** | Local data persistence |
| **MVVM** | Architecture pattern |
| **StateFlow** | Reactive UI updates |
| **Coroutines** | Async operations |
| **AlarmManager** | Exact reminder scheduling |
| **ViewBinding** | View access |
| **Material3** | UI components |
| **SharedPreferences** | Session & settings |

---

## 📁 Project Structure

```
syntecxhub_todolist/
├── auth/
│   ├── SplashActivity.kt
│   ├── LoginActivity.kt
│   └── RegisterActivity.kt
├── data/
│   ├── model/   → Task.kt, User.kt
│   ├── db/      → AppDatabase.kt, TaskDao.kt, UserDao.kt
│   └── repo/    → TaskRepository.kt, UserRepository.kt
├── ui/
│   ├── home/    → MainActivity, HomeFragment, TaskViewModel, TaskAdapter
│   ├── search/  → SearchFragment
│   ├── stats/   → StatsFragment
│   ├── profile/ → ProfileFragment
│   └── task/    → AddEditTaskActivity
└── utils/
    ├── SessionManager.kt
    ├── NotificationHelper.kt
    ├── ReminderReceiver.kt
    ├── ReminderScheduler.kt
    ├── Extensions.kt
    └── Constants.kt
```

---

## 🚀 Getting Started

1. Clone the repository
```bash
git clone https://github.com/jatin-agarwal-01/Syntecxhub_ToDoListApp.git
```

2. Open in **Android Studio**

3. Sync Gradle

4. Run on emulator or physical device (min SDK 24)

---

## 👨‍💻 Developer

**Jatin Agarwal**
Internship Project — SyntecxHub
Android App Development

---

## 📄 License

This project was built as part of an internship assignment at SyntecxHub.
