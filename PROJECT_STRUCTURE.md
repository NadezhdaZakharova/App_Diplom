# PROJECT_STRUCTURE

Документ фиксирует текущую архитектуру проекта `App_Diplom` в формате  
`слой -> файлы -> ответственность -> зависимости`.

## 1. Архитектурная схема

Проект организован по слоям:

- `ui` (presentation): Compose-экраны, состояние интерфейса, пользовательские сценарии.
- `domain`: бизнес-правила, use case, контракты репозиториев, модели.
- `data`: Room-хранилище, DAO, реализации репозиториев, сенсорные источники.
- `app/di/work/worker`: entry point приложения, DI через Hilt, фоновая обработка.
- `core`: общие утилиты.

Поток зависимостей:

`ui -> domain/repository interfaces -> data implementations -> Room`

`worker -> repositories -> data/local`

---

## 2. Таблица по слоям

| Слой | Файлы/разделы | Ответственность | Основные зависимости |
|---|---|---|---|
| `core` | `core/DateUtils.kt` | Утилиты дат (today/week start и т.д.) | Kotlin stdlib, `java.time` |
| `domain` (engine) | `domain/GamificationEngine.kt`, `domain/GamificationConstants.kt`, `domain/GamificationSeeds.kt`, `domain/GamificationStreaks.kt`, `domain/PlayerProgress.kt` | Ядро геймификации: XP, уровни, streak, weekly challenge, достижения | Доменные/локальные модели, `java.time` |
| `domain/model` | `DailyStats.kt`, `PlayerProfile.kt`, `Achievement.kt`, `WeeklyChallenge.kt`, `AppUserMode.kt`, `Exercise.kt`, `WorkoutExercise.kt`, `StudentRewardsStats.kt` | Чистые предметные модели | Без Android-зависимостей |
| `domain/repository` | `ActivityRepository.kt`, `GamificationRepository.kt`, `TrainingRepository.kt` | Контракты доступа к данным для `ViewModel`/use case | `kotlinx.coroutines.flow`, domain models |
| `domain/usecase` | `AddStepsUseCase.kt`, `BootstrapGameUseCase.kt` | Прикладные бизнес-сценарии | `domain/repository` |
| `data/local` | `DiplomDatabase.kt`, `DiplomDao.kt`, `*Entity.kt` | Схема Room БД, таблицы и запросы DAO | Room (`androidx.room`) |
| `data/repository` | `ActivityRepositoryImpl.kt`, `GamificationRepositoryImpl.kt`, `TrainingRepositoryImpl.kt` | Реализация контрактов репозиториев, маппинг entity <-> domain, JSON импорт/экспорт тренировки | `data/local`, `domain`, coroutines Flow, JSON |
| `data/sensor` | `StepCounterManager.kt` | Интеграция с шагомером Android | Android Sensor API |
| `ui` (state/app shell) | `MainViewModel.kt`, `MainUiState.kt`, `DiplomApp.kt`, `AppDestinations.kt`, `UserModeUi.kt`, `UiStrings.kt` | Оркестрация состояния и навигации, действия пользователя, связка экранов с репозиториями | Compose, Lifecycle ViewModel, Coroutines Flow |
| `ui/screens` | `TrainingScreens.kt`, `RewardsScreen.kt` | Экранная логика тренировок/наград, таймер с паузой/выходом, импорт/экспорт и отправка тренировки | Compose Material3 |
| `ui/components` | `AccessibleTextButton.kt` | Переиспользуемые UI-компоненты | Compose |
| `ui/theme` | `Color.kt`, `Theme.kt`, `Type.kt` | Тема и дизайн-система | Compose Material3 |
| `app` | `MainActivity.kt`, `DiplomApplication.kt` | Entry point приложения, запуск UI, lifecycle-логика шагомера, инициализация Hilt | AndroidX Activity, Hilt |
| `di` | `di/DatabaseModule.kt`, `di/RepositoryModule.kt` | Hilt-модули зависимостей (Room + repositories) | Hilt, Room |
| `work` | `work/DailyWorkScheduler.kt` | Планирование periodic задач | WorkManager |
| `worker` | `worker/DailyRecalculateWorker.kt` | Фоновый пересчет геймификации | WorkManager, repositories |
| `tests` | `src/test/...`, `src/androidTest/...` | Unit/instrumented тесты | JUnit, AndroidX Test |

---

## 3. Разделение ответственности

### UI слой (`ui`)
- Работает с `StateFlow` и событиями пользователя.
- Не содержит SQL/DAO-логики.
- Вызывает методы `ViewModel`, дальше — репозитории/use case.

### Domain слой (`domain`)
- Содержит бизнес-правила и интерфейсы.
- Не зависит от Android UI, Compose и Room.

### Data слой (`data`)
- Отвечает за хранение и выдачу данных.
- Инкапсулирует Room/DAO и детали JSON.
- Реализует интерфейсы из `domain/repository`.

### Infrastructure (`app`, `di`, `work`, `worker`)
- Инициализирует зависимости (Hilt).
- Управляет background-процессами через WorkManager.

---

## 4. Границы зависимостей

- `ui` знает только про модели/контракты и `ViewModel`.
- `domain` не знает про Android framework.
- `data` знает про `domain` и Android persistence layer.
- `worker` использует репозитории и не зависит от UI.

Это упрощает поддержку, тестирование и дальнейшее расширение.

---

## 5. Текущие технические примечания

- DI через **Hilt** (`DiplomApplication`, `di/*`, `@HiltViewModel`).
- Сборка на AGP 9 + Compose plugin + KSP.
- Навигация реализована состоянием (`NavigationSuiteScaffold`) без `NavHost`.
- Входящие deep link/share-target фильтры из `AndroidManifest` удалены (по текущему scope проекта).
- Отправка сформированной тренировки наружу через `Intent.ACTION_SEND` сохранена.
- В экране сессии тренировки есть:
  - пауза/возобновление таймера;
  - подтверждаемое закрытие режима с предупреждением о сбросе прогресса.
- Сетевой слой (`Retrofit`) в проект не внедрен.
