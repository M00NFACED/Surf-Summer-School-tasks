# Скалодром «Вертикаль» — Мобильный клиент и Client API

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose_Multiplatform-1.7.3-blue.svg)](https://www.jetbrains.com/compose-multiplatform/)
[![Architecture](https://img.shields.io/badge/Architecture-Clean_Architecture-green.svg)](#архитектура-проекта)
[![Tests](https://img.shields.io/badge/Tests-91_passed-brightgreen.svg)](#тестирование-и-верификация)
[![Backend](https://img.shields.io/badge/Backend-Go_1.23_%2F_PostgreSQL_15-00ADD8.svg)](https://golang.org)

Мультиплатформенный клиент (Android и Desktop) на Compose Multiplatform и изолированный локальный Client API для записи на тренировки, аренды экипировки и отзывов об инструкторе.

Проект построен по принципам **Clean Architecture** и спецификации **OpenAPI 3.0.3**. Приоритеты: детерминированность бизнес-правил, устойчивость к сетевым сбоям, отсутствие локальных «фантомных» записей при конкуренции за место.

---

## Ключевые возможности MVP

1. **Авторизация по номеру телефона (OTP)** — `SCR-001`, `SCR-002`
   - Маска ввода с прогрессивным форматированием, безопасной работой `OffsetMapping` и валидацией национального формата.
   - Ввод кода из шести цифр с автоматическим переходом фокуса, корректной обработкой `Backspace` и таймером повторной отправки.
   - Персистентное хранение сессии: `SettingsTokenStorage` поверх `SharedPreferences` (Android) и `Preferences` (Desktop), восстановление сессии при перезапуске.

2. **Расписание и офлайн-кэш** — `SCR-003`
   - Слоты на 7 дней по умолчанию, фильтры по дате, формату, инструктору и наличию свободных мест в шторке `ModalBottomSheet`.
   - Фильтр дат с `DatePickerDialog`, чипами быстрого выбора и форматом `ДД.ММ.ГГГГ`; поддерживается одна граница периода.
   - Stale-While-Revalidate: скелетон показывается только при первом запуске с пустым списком, при возврате и pull-to-refresh список остаётся на экране и обновляется в фоне.
   - При недоступности сети показывается помеченный кэш, а не пустое состояние.

3. **Карточка слота и оформление брони** — `SCR-004`, `SCR-005`
   - Инвариант «одна бронь — один человек» (BR-005): в запросе нет количества мест, в интерфейсе нет группового степпера.
   - Раздельный выбор снаряжения (BR-006): скальники и страховочная система, для каждого компонента `own` или `rental` с чипами размеров и остатка.
   - Оплата только на месте, `payment_method = on_site` (BR-011).
   - Конкуренция за место обрабатывается по `HTTP 409` (`SLOT_FULL`, `BOOKING_EXISTS`): источник истины обновляется, локальная подтверждённая бронь не создаётся (BR-013).

4. **Мои записи, отмена и отзыв** — `SCR-006`, `SCR-007`, `SCR-008`
   - Разделение списка на «Предстоящие» и «Прошедшие» с карточками и состояниями Loading, Empty, Error, Offline, Forbidden.
   - Клиентская отмена не позднее чем за 2 часа до старта по серверному `cancel_deadline` (BR-007); после дедлайна кнопка заблокирована, шторка подтверждения объясняет правило.
   - Оценка инструктора от 1 до 5 звёзд ровно один раз и только для завершённых записей без оценки (BR-009), отправка `POST /bookings/{id}/rating`; после успеха бронь помечается как оценённая, кнопка исчезает.

5. **Дизайн-система «Волна» и UX**
   - Фирменная изумрудно-бирюзовая палитра (`#389F82` в светлой теме) с полноценной тёмной темой Material 3 без дефолтных фиолетовых цветов.
   - Процедурный векторный артворк `WaveRouteArtwork`: два стиля сцены (утренние валуны для новичкового болдеринга, вечерние вершины для трасс с верёвкой) и детерминированная вариация по идентификатору слота.
   - Плавающая навигация-пилюля на корневых экранах, скрываемая на детальных экранах.
   - Мультиплатформенный `PlatformBackHandler`: системная кнопка «Назад» возвращает по иерархии экранов и закрывает приложение только с корня расписания.
   - Информационные диалоги профиля: правила клуба, контакты поддержки, версия приложения.

---

## Технологический стек

### Клиент

| Компонент | Версия / решение |
|---|---|
| Язык | Kotlin 2.0.21, JVM target 17 |
| UI | Compose Multiplatform 1.7.3, Material 3, Material Icons |
| Сеть | Ktor Client 2.3.12 (OkHttp — Android, CIO — Desktop), kotlinx.serialization 1.7.3 |
| Даты | kotlinx-datetime 0.6.1 |
| Асинхронность | kotlinx-coroutines 1.9.0, StateFlow |
| Хранилище | multiplatform-settings 1.2.0 |
| Сборка | Gradle 8.10.2, Android Gradle Plugin 8.5.2 |

Целевые платформы модуля `client/shared`: `androidTarget` и `jvm()` (Desktop). Общий код лежит в `commonMain`, платформенные реализации — в `androidMain` и `jvmMain`.

### Бэкенд и инфраструктура

| Компонент | Версия / решение |
|---|---|
| API Server | Go 1.23, стандартная библиотека `net/http`, `lib/pq` |
| База данных | PostgreSQL 15 (образ `postgres:15-alpine`) с CHECK-констрейнтами и индексами |
| Контейнеризация | Docker и Docker Compose, healthcheck `pg_isready` |
| Миграции и фикстуры | `backend/migrations/*.sql`, `backend/seed/001_seed.sql` |
| Контракт | OpenAPI 3.0.3: 9 paths, 31 schema (`01-analysis/api/openapi.yaml`) |

Реализованные маршруты backend: `GET /health`, `POST /auth/request-code`, `POST /auth/verify-code`, `GET /slots`, `GET /slots/{id}`, `POST /bookings`, `GET /bookings/my`, `POST /bookings/{id}/cancel`, `POST /bookings/{id}/rating`, `POST /devices/push-token`.

---

## Архитектура проекта

```
Surf/
├── 01-analysis/                  # инженерная аналитика и спецификации
│   ├── 0-customer-brief/         # исходный бриф заказчика
│   ├── 1-elicitation/            # протоколы уточняющих вопросов
│   ├── 2-requirements/           # BR, FR, NFR, use cases, ограничения скоупа
│   ├── 3-design-brief/           # навигационный граф, реестр экранов
│   ├── 4-design/                 # ERD и sequence-диаграммы
│   ├── 5-mobile-app-spec/        # спецификации экранов SCR-001…SCR-008
│   ├── 6-testing/                # матрица проверок и QA-отчёт
│   ├── api/openapi.yaml          # канонический контракт Client API
│   └── templates/                # шаблоны документов аналитики
├── backend/                      # изолированный локальный Client API
│   ├── cmd/server/main.go        # точка входа HTTP-сервера
│   ├── internal/httpapi/         # обработчики и middleware
│   ├── internal/store/           # доступ к данным и транзакции
│   ├── migrations/               # DDL, ограничения и индексы
│   ├── seed/                     # детерминированные фикстуры
│   ├── compose.yaml              # PostgreSQL 15 + Go API
│   └── Dockerfile
├── client/                       # клиент Compose Multiplatform
│   ├── shared/                   # общая бизнес-логика, сеть, UI
│   │   └── src/commonMain/kotlin/org/example/client/
│   │       ├── core/             # network, storage, theme, ui, validation, navigation
│   │       └── features/         # auth, schedule, booking, my_bookings, review, profile, navigation
│   ├── androidApp/               # Android-раннер: MainActivity, манифест, тема, иконки
│   └── desktopApp/               # Desktop-раннер для быстрого предпросмотра
├── AGENTS.md                     # правила работы над проектом
├── IMPLEMENTATION_PLAN.md        # план реализации по шагам
├── PROMPTS_LOG.md                # журнал итераций и решений
└── build.gradle.kts, settings.gradle.kts
```

### Слои клиента

Внутри каждой фичи используется package-by-feature со строгим разделением ответственности:

- `presentation/` — Compose UI, ViewModel, UI State, навигационные события.
- `domain/` — доменные модели, правила и UseCases без зависимостей от Ktor и Compose.
- `data/` — Ktor API, DTO, мапперы, кэш и реализации репозиториев.

Зависимости направлены внутрь: `presentation` обращается к `domain`, а `data` реализует интерфейсы, объявленные в `domain`. Доменный слой не знает о DTO, HTTP, Android и Compose. Общие примитивы дизайн-системы вынесены в `core/ui`, а контрактный слой DTO не дублируется между фичами.

---

## Быстрый старт

Требуется JDK 17 и Docker. Значения по умолчанию для Android-приложения: `http://127.0.0.1:8080`.

### 1. Локальный Client API

```bash
cd backend
# задайте POSTGRES_PASSWORD в окружении или в backend/.env (шаблон — backend/.env.example)
docker compose up -d --build

# проверка доступности
curl http://127.0.0.1:8080/health
```

Ожидается HTTP 200. PostgreSQL поднимается с healthcheck, миграции и фикстуры применяются автоматически.

### 2. Android-клиент

Для физического устройства по USB пробрасывается порт:

```bash
adb reverse tcp:8080 tcp:8080
./gradlew :client:androidApp:installDebug
```

Адрес API задаётся свойством Gradle:

```bash
# официальный эмулятор Android Studio
./gradlew :client:androidApp:installDebug -PapiBaseUrl=http://10.0.2.2:8080

# локальный backend с другого устройства в сети
./gradlew :client:androidApp:installDebug -PapiBaseUrl=http://192.168.1.10:8080
```

Cleartext-трафик разрешён только для `10.0.2.2`, `127.0.0.1` и `localhost` в `network_security_config.xml`.

### 3. Desktop-клиент

```bash
./gradlew :client:desktopApp:run
```

---

## Тестирование и верификация

Автоматизировано 91 unit-тест в 27 классах: валидаторы телефона и UUID, use cases авторизации, расписания, бронирования, отмены и оценки, мапперы DTO, репозитории, навигация, тема и геометрия артворка.

```bash
# unit-тесты общего модуля
./gradlew :client:shared:jvmTest

# сборка debug APK
./gradlew :client:androidApp:assembleDebug

# полная проверка компиляции обоих раннеров
./gradlew :client:shared:build :client:desktopApp:compileKotlinDesktop
```

Документы тестирования:

- `01-analysis/6-testing/test-matrix.md` — матрица проверок: Smoke, Boundary, Concurrency, Deadlines, Offline Cache, регрессия дизайн-системы.
- `01-analysis/6-testing/test-report.md` — отчёт о дефектах BUG-001…BUG-005 и сквозной аудит соответствия скоупа с матрицами BR, FR и NFR.

### Известные ограничения

- Unit-тесты не заменяют UI-тесты: состояния Loading, Empty, Error, Offline и Forbidden, а также Bottom Sheet проверяются вручную.
- Contract-тесты против запущенного Client API и race-тест двух параллельных запросов на последнее место выполняются во внешнем QA-окружении.
- Push-уведомления: контракт и backend реализуют `POST /devices/push-token`, но регистрация устройства в клиенте не реализована.
- Онлайн-комментарий к оценке не входит в MVP (`SCR-008`, раздел 6), поэтому в запрос уходит только `score`.
- Точные цвета, шрифты и логотип не сверены пиксельно с Figma: файл макета недоступен для прямого доступа, палитра и метрики заданы по переданным скриншотам.

---

## Git Workflow и AI-ассистирование

Работа ведётся атомарными коммитами в стиле Conventional Commits:

- `feat(scope)` — новые функции и экраны по спецификациям;
- `fix(scope)` — точечное устранение дефектов по результатам тестирования;
- `style(scope)` — доработка дизайн-системы и иконок;
- `chore(scope)` — инфраструктура, сборка, документация.

`PROMPTS_LOG.md` хранит полный журнал итераций: запрос, принятые решения, зафиксированные расхождения между макетом и бизнес-правилами, список артефактов и результаты проверок. Правила работы над проектом и критерии готовности заданы в `AGENTS.md`.
