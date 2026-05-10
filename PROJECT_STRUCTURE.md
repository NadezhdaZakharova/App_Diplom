# PROJECT_STRUCTURE

**Каноническое описание структуры каталогов и слоёв** — актуальная карта модулей и файлов.  
Краткое описание назначения приложения и пользовательских сценариев — в **`PROJECT_DESCRIPTION.md`**.

Документ в формате: `слой → файлы → ответственность → зависимости`.

---

## Оглавление

1. [Архитектурная схема](#1-архитектурная-схема)  
2. [Таблица по слоям](#2-таблица-по-слоям)  
3. [Разделение ответственности](#3-разделение-ответственности)  
4. [Границы зависимостей](#4-границы-зависимостей)  
5. [Технические примечания](#5-технические-примечания)

---

## 1. Архитектурная схема

**Gradle-модули** (физическое разбиение):

| Модуль | Содержимое | Зависимости |
|--------|------------|-------------|
| **`:domain`** | JVM Kotlin: `domain/*`, `core/DateUtils`, модели `MainUiCoreState` / `CoreMainUiInputs`, **use case** (в т.ч. **`ObserveCoreMainUiInputsUseCase`** — единая подписка на `Flow` для главного экрана) | Coroutines, `javax.inject` |
| **`:data`** | Android Library: Room, DAO, реализации репозиториев, сенсор, уведомления, импорт, Hilt-модули `DatabaseModule` / `RepositoryModule` / `StepMilestoneNotifierModule`, ресурсы строк для уведомлений модуля | `:domain`, Room, Hilt, Work (через app при необходимости) |
| **`:app`** | Compose UI, `MainActivity`, `MainViewModel`, WorkManager worker/scheduler, `BootstrapGameUseCaseEntryPoint`, манифест, `res` приложения | `:domain`, `:data` |

Поток зависимостей:

`:app` → `:domain` + `:data`  `:data` → `:domain`  `:domain` без Android и без `:data`

---

## 2. Таблица по слоям (пакеты внутри модулей)

| Слой | Файлы / разделы | Ответственность | Основные зависимости |
|------|-----------------|-----------------|----------------------|
| `core` (`:domain`) | `core/DateUtils.kt` | Утилиты дат | `java.time` |
| `domain` (engine) | `GamificationEngine.kt`, `GamificationConstants.kt`, … | Ядро геймификации | Модели домена |
| `domain` | `StudentRewardsCalculator.kt`, `ExerciseTitleKeys.kt`, … | Награды, ключи, контракты | Без Android |
| `domain/model` | `DailyStats.kt`, … | Модели | — |
| `domain/repository` | интерфейсы репозиториев | Контракты | Flow |
| `domain/usecase` | все `*UseCase`, в т.ч. **`ObserveCoreMainUiInputsUseCase`** | Команды и **наблюдение** данных для UI; **MainViewModel** не держит репозиториев | `domain/repository` |
| `domain` | `MainUiCoreState.kt` | DTO потока `CoreMainUiInputs` для сборки `MainUiState` | Модели |
| `data/local` | `DiplomDatabase.kt` (v**9**), миграции, DAO, entities | Room | KSP, схемы в **`data/schemas`** |
| `data/repository` | `*RepositoryImpl.kt`, JSON, парсеры | Реализации | `:domain`, Room |
| `data/notification` | `AndroidStepMilestoneNotifier.kt` | Уведомления (`com.example.diplom.data.R`) | Android |
| `data/importing`, `data/sensor` | препроцессор, шагомер | Импорт, сенсор | Android |
| `data/di` | `DatabaseModule`, `RepositoryModule`, `StepMilestoneNotifierModule` | Hilt-связки | `:domain` |
| `ui` (`:app`) | `MainViewModel.kt`, `MainUiState.kt`, … | UI, только use case + **`ObserveCoreMainUiInputsUseCase()`** для потока состояния | Compose, Hilt |
| `ui/screens` | `TrainingScreen.kt`, `WorkoutSessionScreen.kt`, `TrainingUiPrimitives.kt`, `TrainingExerciseCards.kt`, `TrainerTrainingSection.kt`, `StudentTrainingSection.kt`, `TrainerJsonExportBlock.kt`, `TrainingTypes.kt`, `TrainingDurationValidation.kt`, `RewardsScreen.kt` | Тренировки: у ученика аккордеон «Самостоятельная» / «От тренера» (одна секция открыта или обе свёрнуты); сворачиваемая карточка **цели по шагам** и краткое **«Сохранено»** после применения; тренер — план и экспорт. Награды: **встроенная карточка** предложения конвертировать шаги в тренировку **над блоком «Активность»**; баннер при достижении цели; **линейный прогресс по дням** относительно текущей `dailyGoal`. Сессия — таймер, пауза | Compose Material3 |
| `ui/components` | `AccessibleTextButton.kt` | Переиспользуемые элементы | Compose |
| `ui/theme` | `Color.kt`, `Theme.kt`, `Type.kt` | Тема | Material 3 |
| корень пакета | `MainActivity.kt`, `DiplomApplication.kt` | Entry point, share-intent JSON, шаги → ViewModel | Activity, Hilt |
| `di` (`:app`) | `BootstrapGameUseCaseEntryPoint.kt` | Hilt **EntryPoint** для **BootstrapGameUseCase** из `DailyRecalculateWorker` | Hilt |
| `work` / `worker` (`:app`) | `DailyWorkScheduler.kt`, `DailyRecalculateWorker.kt` | WorkManager | `:data` / use case через EntryPoint |
| `tests` | `:domain` — `GamificationEngineTest`; `:data` — `TrainerWorkoutImportPreprocessorImplTest`, `TrainerWorkoutPlannedSecondsParserTest` (тот же модуль, что и реализация); `:app` — при необходимости лёгкий `ExampleUnitTest`, **`androidTest`** / `SmokeInstrumentedTest` | Unit / instrumented | JUnit, Compose UI Test |

---

## 3. Разделение ответственности

### UI (`ui`)

- Работает с `StateFlow` и событиями пользователя.
- Не содержит SQL / DAO.
- Вызывает методы `ViewModel` → **только use case** (в т.ч. **`ObserveCoreMainUiInputsUseCase`** для объединённого `Flow` состояния).

### Domain (`domain`)

- Бизнес-правила и интерфейсы.
- Не зависит от Android UI, Compose и Room.
- `StepMilestoneNotifier` — контракт; реализация в `data/notification`.

### Data (`data`)

- Хранение и выдача данных, Room / DAO, детали JSON.
- Реализует интерфейсы `domain/repository`.

### Infrastructure (`:app`: `MainActivity`, `DiplomApplication`, `di`, `work`, `worker`)

- Точка входа приложения и Hilt **EntryPoint** для Worker.
- Фоновые задачи через WorkManager.
- Привязки Room / репозиториев к интерфейсам — в модуле **`:data`** (`data/di`).

---

## 4. Границы зависимостей

- `ui` знает модели / контракты и `ViewModel`.
- `domain` не знает Android framework (кроме типов в контрактах при необходимости).
- `data` знает `domain` и Android (persistence, notifications).
- `worker` не зависит от UI.

---

## 5. Технические примечания

- **Core library desugaring** (`desugar_jdk_libs`): `java.time` на **minSdk 24**.
- DI: **Hilt** (`DiplomApplication`, `di/*`, `@HiltViewModel`).
- Сборка: AGP + Compose Compiler + **KSP** (Room).
- Навигация: **`NavigationSuiteScaffold`**, без `NavHost`.
- Room: **`fallbackToDestructiveMigration(false)`**, цепочка миграций в `DiplomDatabaseMigrations.kt`, версия БД **9**.
- **Share Target** в `MainActivity`: приём JSON через `ACTION_SEND` (`text/plain`).
- Строки: в **`:app`** — UI (`values/strings.xml`, `values-en/strings.xml`); тексты **канала и уведомлений по шагам** — только в **`:data`** (`data/src/main/res/...`), без дублирования в приложении.
- Модуль **`:data`**: отдельный **`consumer-rules.pro`** не подключается; при обфускации release — **`app/proguard-rules.pro`**.
- Разрешения: `ACTIVITY_RECOGNITION`, `POST_NOTIFICATIONS` (API 33+).
- Лаунчер: `AndroidManifest` указывает `@mipmap/ic_launcher` / `ic_launcher_round`; в исходниках — adaptive **API 26+**, для более старых API иконки **генерируются при сборке** (webp в APK).
- Каталог **`.kotlin/`** в корне репозитория — служебный кэш Kotlin/IDE (в репозиторий обычно не коммитится); пустые пакеты в `src` после удаления классов на диске не оставлять — иначе они «висят» в дереве проекта.
- **Git**: в корне — **`.gitattributes`** (для исходников и конфигов в основном **LF**; для `*.bat` — **CRLF**; для **`.idea/`** — **`-text`**, чтобы не навешивать глобальное `eol=lf` на XML студии). В **`.gitignore`** указано **`**/build/`**, чтобы каталоги сборки всех модулей (`app/build`, `data/build`, …) не попадали в индекс и не ломали **`git status`** на Windows из‑за слишком длинных путей внутри `build/.transforms/`. В этом репозитории локально задано **`core.autocrlf=false`**, чтобы не дублировать нормализацию с атрибутами. При смене правил окончаний строк один раз можно выполнить **`git add --renormalize .`** и зафиксировать отдельным коммитом.