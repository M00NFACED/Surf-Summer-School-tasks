# QA Test Report — MVP

## 1. Метаданные

- Проект: Surf / клиент «Вертикаль»
- Этап: QA и стабилизация MVP
- Дата проверки: 2026-09-25
- Commit: `fix(client): fix slot and instructor navigation ids, persist auth token and add QA test artifacts`
- Область: `SCR-001`…`SCR-005`, auth/schedule/booking, persistence и theme
- Источник требований: `01-analysis/2-requirements/`, `01-analysis/5-mobile-app-spec/`, `01-analysis/api/openapi.yaml`

## 2. Сводка

| Bug ID | Severity | Краткое описание | Статус |
|---|---|---|---|
| BUG-001 | High | Крэш прогрессивного форматирования номера и нестабильный cursor | Resolved |
| BUG-002 | Critical | Необработанный `SocketException`/сетевой сбой на Main | Resolved |
| BUG-003 | Critical | `Некорректный идентификатор слота/инструктора` из-за слишком строгой UUID-валидации | Resolved |
| BUG-004 | Critical | Сессия терялась при перезапуске приложения | Resolved |
| BUG-005 | Medium | Белый текст на белом фоне в системной тёмной теме | Resolved |

## 3. Отчёты по дефектам

### BUG-001 — Маска ввода телефона

- Severity: High
- Сценарий: ввести 1–10 цифр номера, удалить цифры и вставить значение с префиксом `7`/`8`.
- Воспроизведение до исправления: progressive formatter вызывал `substring` с фиксированным конечным индексом; промежуточная длина могла выйти за границу.
- Исправление: безопасные `minOf`-срезы для каждого сегмента; `OffsetMapping` клампит границы и проверяет длины 0–10; добавлены unit-тесты.
- Проверка: `PhoneNumberValidatorTest`, `PhoneNumberVisualTransformationTest`, `AuthViewModelTest`.
- Статус: **Resolved**.

### BUG-002 — SocketException и сетевой сбой

- Severity: Critical
- Сценарий: запустить OTP, расписание или загрузку карточки без доступного backend.
- Воспроизведение до исправления: исключение из suspend-вызова доходило до `Dispatchers.Main` и завершало UI-job.
- Исправление: безопасные `try/catch` с пробросом `CancellationException`, перевод остальных сетевых ошибок в состояние с сообщением `Не удалось подключиться к серверу. Проверьте соединение.`; auth repository возвращает `Result.failure`.
- Проверка: `AuthViewModelTest`, `ScheduleViewModelTest`, `BookingViewModelTest`, shared build.
- Статус: **Resolved**.

### BUG-003 — Идентификаторы слота и инструктора

- Severity: Critical
- Сценарий: открыть карточку из `GET /slots` или выбрать инструктора из DTO.
- Воспроизведение до исправления: клиент использовал regex, ограничивающий UUID версией 1–5 и RFC variant 8–B; backend fixtures содержат канонические UUID с version/variant `0`, поэтому UI показывал `Некорректный идентификатор...`.
- Исправление: общий `UuidValidator` принимает любой canonical 8-4-4-4-12 hex UUID, trimming внешних пробелов и отбрасывая malformed/quoted значения; slot/instructor IDs нормализуются в use cases и presentation; path строится через Ktor URL segments.
- Проверка: `UuidValidatorTest`, `GetScheduleUseCaseTest`, `GetSlotDetailsUseCaseTest`, `CreateBookingUseCaseTest`, `ScheduleQueryBuilderTest`, `BookingViewModelTest`.
- Статус: **Resolved**.

### BUG-004 — Persistent auth token

- Severity: Critical
- Сценарий: успешно подтвердить OTP, закрыть и перезапустить приложение.
- Воспроизведение до исправления: `App` использовал `InMemoryTokenStorage`, поэтому после перезапуска требовался новый SMS.
- Исправление: Android использует `SharedPreferencesSettings`, desktop — `PreferencesSettings`; оба передают `SettingsTokenStorage` в `App`; `restoreSession` открывает расписание при непустом сохранённом токене, logout очищает ключ.
- Проверка: `SettingsTokenStorageTest`, Android debug build, desktop compile.
- Статус: **Resolved**.

### BUG-005 — Системная тёмная тема

- Severity: Medium
- Сценарий: включить системную тёмную тему и открыть расписание, ввод телефона и OTP.
- Воспроизведение до исправления: root shell не выбирал semantic dark color scheme; текст и фон могли использовать светлую пару.
- Исправление: `App` выбирает `darkColorScheme`/`lightColorScheme`; Scaffold/Surface используют `MaterialTheme.colorScheme.background`, остальные UI-компоненты — семантические цвета без `Color.White`/`Color.Black`.
- Проверка: статический grep исходников, shared lint и Android debug build.
- Статус: **Resolved**.

## 4. Проверки

| Проверка | Результат |
|---|---|
| `.\gradlew :client:shared:jvmTest` | Passed |
| `.\gradlew :client:androidApp:assembleDebug` | Passed |
| `.\gradlew :client:shared:build` | Passed |
| `.\gradlew :client:desktopApp:compileKotlinDesktop` | Passed |
| Ручной запуск на физическом Android/iOS и два параллельных HTTP POST | Pending в внешнем QA-окружении |

## 5. Residual risks

- Реальный OTP/SMS-провайдер и production HTTPS endpoint не подключены в локальной сборке.
- Device-level проверка dark theme, focus movement и persistent storage требует запуска на эмуляторе/устройстве.
- Race-тест HTTP 409 должен выполняться против источника истины; client не создаёт локальное подтверждение при конфликте.
