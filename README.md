# SyntecxHub ToDo App 📋

A full-featured Android ToDo application built with **Kotlin** during my internship at **SyntecxHub**.

---

## ✨ Features

### 🔐 Authentication
- Email & Password login / register
- Guest mode with limited features
- Session management with SharedPreferences

### ✅ Task Management
- Add, Edit, Delete tasks
- Mark tasks as complete / incomplete
- Swipe to delete with Undo (Snackbar restore)
- Priority levels — High 🔴, Medium 🟠, Low 🟢
- Priority colour stripe on each task card for instant visual scanning
- Categories — General, Work, Personal, Shopping, Health, Study, Finance
- Filter tasks by status — All / Active / Done (Home screen chips)

### ⏰ Reminders
- Full calendar date picker
- 12-hour & 24-hour time format (user preference)
- Exact alarm notifications via AlarmManager
- Heads-up push notifications
- Reminder auto-cancelled on task completion

### 📊 Statistics
- Overall completion progress (large % banner + progress bar)
- Total / Done / Pending summary cards
- Priority breakdown with individual progress bars
- Category breakdown with dynamic rows, progress bars, and done/total badges
- Motivational progress messages

### 🔍 Search & Filter
- Real-time task search (title + description)
- Filter by priority chip — All / High / Medium / Low
- Combined text + priority filter applied simultaneously
- Live result count display

### 👤 Profile
- User avatar with initials
- Dark mode toggle 🌙
- Time format preference (12 hr / 24 hr)
- Mini stats snapshot (total, done, pending)
- Logout with confirmation

### 🔒 Guest Restrictions
- Reminders, Stats, Search, and Profile locked for guests
- Friendly "Login to unlock" dialogs

---

## 🎨 UI Highlights

- **Material Design 3** throughout — cards, chips, outlined buttons, extended FAB
- **Full dark mode** support via `values-night/` resource qualifiers
- Priority colour stripes on task cards (red / orange / green)
- Completed tasks visually dimmed (alpha + strikethrough)
- WCAG AA-compliant text contrast across all screens
- API 24+ compatible — no `paddingHorizontal` / `marginHorizontal` XML attributes

---

## 🏗️ Architecture

```
MVVM + Repository Pattern
├── Model      → Room Database (Task, User entities — schema v3)
├── ViewModel  → TaskViewModel (StateFlow, activityViewModels)
├── Repository → TaskRepository, UserRepository
└── View       → Activities, Fragments (ViewBinding), ListAdapter (DiffUtil)
```

---

## 🛠️ Tech Stack

| Technology | Usage |
|---|---|
| **Kotlin** | Primary language |
| **Room DB v3** | Local data persistence with migrations |
| **MVVM** | Architecture pattern |
| **StateFlow + Coroutines** | Reactive UI & async operations |
| **AlarmManager** | Exact reminder scheduling |
| **ViewBinding** | Type-safe view access |
| **Material Design 3** | UI components & theming |
| **SharedPreferences** | Session, dark mode & time format settings |
| **ListAdapter + DiffUtil** | Efficient RecyclerView updates |

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

2. Open in **Android Studio Hedgehog** or later

3. Sync Gradle

4. Run on emulator or physical device (**min SDK 24**, target SDK 34)

---

## 👨‍💻 Developer

**Jatin Agarwal**
Internship Project — SyntecxHub
Android App Development

---

## 📄 License

This project was built as part of an internship assignment at SyntecxHub.
