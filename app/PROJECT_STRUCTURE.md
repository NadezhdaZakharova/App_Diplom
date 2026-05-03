# PROJECT_STRUCTURE

Документ фиксирует текущую архитектуру проекта `App_Diplom` в формате  
`слой -> файлы -> ответственность -> зависимости`.

## 1. Архитектурная схема

Проект организован по слоям:

- `ui` (presentation): экраны и состояние интерфейса на `Jetpack Compose`.
- `domain`: бизнес-правила, use case-сценарии, контракты доступа к данным.
- `data`: локальное хранилище (`Room`), реализации репозиториев, сенсоры.
- `app/worker`: композиция зависимостей и фоновые задачи.
- `core`: общие утилиты.

Поток зависимостей:

`ui -> domain (usecase/repository contracts) -> data (repository impl + room)`

`worker -> data/domain`  
`app container -> data/domain`

---

## 2. Таблица по слоям

| Слой | Файлы/разделы | Ответственность | Основные зависимости |
|---|---|---|---|
| `core` | `core/DateUtils.kt` | Единые функции работы с датами (текущая дата, неделя и т.д.) | Kotlin stdlib, `java.time` |
| `domain` (engine) | `domain/GamificationEngine.kt`, `domain/GamificationConstants.kt`, `domain/GamificationSeeds.kt`, `domain/GamificationStreaks.kt`, `domain/PlayerProgress.kt` | Ядро геймификации: XP, уровни, streak, достижения, weekly challenge | Доменные/локальные модели, `java.time` |
| `domain/model` | `DailyStats.kt`, `PlayerProfile.kt`, `Achievement.kt`, `WeeklyChallenge.kt`, `AppUserMode.kt`, `Exercise.kt`, `WorkoutExercise.kt`, `StudentRewardsStats.kt` | Чистые модели предметной области | Без Android-зависимостей |
| `domain/repository` | `ActivityRepository.kt`, `GamificationRepository.kt`, `TrainingRepository.kt` | Контракты доступа к данным для use case и ViewModel | `kotlinx.coroutines.flow`, domain models |
| `domain/usecase` | `AddStepsUseCase.kt`, `BootstrapGameUseCase.kt` | Прикладные сценарии бизнес-логики | `domain/repository` |
| `data/local` | `DiplomDatabase.kt`, `DiplomDao.kt`, все `*Entity.kt` | Room-схема, DAO-запросы и таблицы хранения | Room (`androidx.room`) |
| `data/repository` | `ActivityRepositoryImpl.kt`, `GamificationRepositoryImpl.kt`, `TrainingRepositoryImpl.kt` | Реализация repository-контрактов через Room и маппинг entity <-> domain | `data/local`, `domain`, coroutines Flow, JSON |
| `data/sensor` | `StepCounterManager.kt` | Интеграция с шагомером/сенсорами Android | Android Sensor API |
| `ui` (state/app shell) | `MainViewModel.kt`, `MainUiState.kt`, `DiplomApp.kt`, `AppDestinations.kt`, `UserModeUi.kt`, `UiStrings.kt` | Управление состоянием UI, orchestration use case/repositories, состояние импорта/экспорта тренировки | Compose, Lifecycle ViewModel, Coroutines Flow |
| `ui/screens` | `TrainingScreens.kt`, `RewardsScreen.kt` | Экранная логика и визуальные сценарии пользователя; экспорт JSON, системная отправка `ACTION_SEND`, пауза/возобновление и подтверждаемое закрытие сессии тренировки | Compose Material3, `ui` state/models |
| `ui/components` | `AccessibleTextButton.kt` | Переиспользуемые UI-компоненты | Compose |
| `ui/theme` | `Color.kt`, `Theme.kt`, `Type.kt` | Дизайн-система приложения (цвета, типографика, тема) | Compose Material3 |
| `app` | `MainActivity.kt`, `DiplomApplication.kt` | Android entry point, Hilt `@HiltAndroidApp`, `@AndroidEntryPoint`, lifecycle-логика шагомера и запуск UI | AndroidX Activity, Hilt, WorkManager |
| `di` | `di/DatabaseModule.kt`, `di/RepositoryModule.kt` | Hilt-модули: `Room`, привязка интерфейсов репозиториев | Hilt, Room |
| `work` | `work/DailyWorkScheduler.kt` | Планирование периодического `WorkManager` | WorkManager, `@ApplicationContext` |
| `worker` | `worker/DailyRecalculateWorker.kt` | Периодический фоновый пересчет геймификации | WorkManager, Room, repository impl |
| `tests` | `src/test/.../ExampleUnitTest.kt`, `src/androidTest/.../ExampleInstrumentedTest.kt` | Базовые unit/instrumented проверки | JUnit, AndroidX Test |

---

## 3. Разделение ответственности (коротко)

### UI слой (`ui`)
- Не хранит данные напрямую в БД.
- Работает с состоянием через `StateFlow`.
- Делегирует бизнес-операции в use case/repository.

### Domain слой (`domain`)
- Содержит бизнес-правила и контракты.
- Не зависит от Compose, Room и Android UI.
- Является центральным местом для правил расчета прогресса и достижений.

### Data слой (`data`)
- Отвечает за хранение и извлечение данных.
- Инкапсулирует Room/DAO и детали сериализации.
- Предоставляет реализацию контрактов из `domain/repository`.

### Infrastructure (`app`, `worker`)
- Инициализирует зависимости.
- Выполняет фоновые задачи и планирование через WorkManager.

---

## 4. Границы зависимостей (важно для защиты)

- `ui` знает про `domain` и интерфейсы репозиториев, но не про детали SQL.
- `domain` не знает про Android UI и Compose.
- `data` знает про `domain` и реализует его контракты.
- `worker` использует `data/domain` для фоновой синхронизации/пересчета.

Это соответствует базовой идее разделения слоев и упрощает сопровождение, тестирование и дальнейший переход к полноценной Clean Architecture.

---

## 5. Текущие технические примечания

- DI через **Hilt** (`DiplomApplication`, модули `di/*`, `@HiltViewModel`).
- Используется актуальный **Hilt** под AGP 9 с **новым DSL** по умолчанию (`android.newDsl` не отключается).
- Переход между разделами реализован через состояние (`NavigationSuiteScaffold`), без `NavHost`.
- Отправка сформированной тренировки в другие приложения через `Intent.ACTION_SEND` из `TrainingScreens.kt`.
- Входящие deep link/share-target фильтры из `AndroidManifest` удалены (по текущему scope проекта).
- Сетевой слой (`Retrofit`) в текущей версии еще не внедрен.

Эти пункты можно использовать как roadmap для следующего этапа развития проекта.
