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

## 6. Сквозной аудит соответствия скоупа

Дата аудита: 2026-09-25. Проверены бриф (`01-analysis/0-customer-brief/customer-brief.md`), `BR-001..014`, `FR-001..011`, `NFR-001..007`, `01-analysis/api/openapi.yaml` и `SCR-001..008` против кода клиента.

### 6.1 Матрица BR → код → тесты

| BR | Требование | Реализация в клиенте | Покрытие |
|---|---|---|---|
| BR-001 | Самостоятельная запись клиента | `features/auth/*`, `features/booking/*`, `App.kt` | `AuthUseCasesTest`, `AuthViewModelTest` |
| BR-002 | Информация о слоте | `TrainingSlotSummary`, `SlotDetailContent`, `ScheduleCard` | `ScheduleMapperTest`, `GetSlotDetailsUseCaseTest` |
| BR-003 | Горизонт расписания | `GetScheduleUseCase` (7 дней по умолчанию), `ScheduleQueryBuilder` | `GetScheduleUseCaseTest`, `ScheduleQueryBuilderTest` |
| BR-004 | Вместимость групп 8/16 | `TrainingFormat.maxCapacity`; слоты создаёт только backend, клиент не создаёт и не меняет слоты | `TrainingFormatCapacityTest` |
| BR-005 | Бронь на одного человека | `CreateBookingRequest` без `places`, `BookingScreen` без группового степпера, текст «Одна бронь — один человек» | `CreateBookingUseCaseTest`, `BookingMapperTest` |
| BR-006 | Своё снаряжение или прокат | `EquipmentPicker` (скальники) и `EquipmentPicker` (страховочная система) раздельно, `EquipmentSelection.Own/Rental` | `BookingMapperTest`, `CreateBookingUseCaseTest` |
| BR-007 | Окно отмены 2 часа | `CancelBookingUseCase` сверяет `cancel_deadline` с `now`, `MyBookingDetailsScreen` блокирует кнопку | `MyBookingsUseCasesTest` |
| BR-008 | Отмена скалодромом | `SlotStatus.CANCELLED`, `BookingStatus.CANCELLED_BY_VENUE`, вывод причины в UI | `MyBookingsMapperTest` |
| BR-009 | Оценка инструктора | DTO `RatingDto` присутствует, пользовательский поток `SCR-008` не реализован | Не покрыто |
| BR-010 | Push-уведомления | Регистрация push-токена в клиенте не реализована | Не покрыто |
| BR-011 | Оплата на месте | `PaymentMethod.ON_SITE` единственный вариант, `BookingMapper` жёстко пишет `on_site` | `BookingMapperTest` |
| BR-012 | Ограниченная роль клиента | Приложение только читает и создаёт бронь, административных сценариев нет | Статический аудит |
| BR-013 | Источник истины, защита от конкуренции | `BookingRepositoryImpl` маппит 409 в `SlotFullException`/`DuplicateBookingException`, локальная `confirmed` не создаётся | `BookingRepositoryImplTest`, `BookingViewModelTest` |
| BR-014 | Доступность данных через Client API | Все DTO сгенерированы по `openapi.yaml`, собственных полей и кодов ошибок нет | `ScheduleMapperTest`, `BookingMapperTest`, `MyBookingsMapperTest` |

### 6.2 Матрица FR → код → тесты

| FR | Реализация в клиенте | Статус | Покрытие |
|---|---|---|---|
| FR-001 | `PhoneEntryScreen`, `OtpVerificationScreen`, `PhoneNumberValidator` | Выполнено | `PhoneNumberValidatorTest`, `PhoneNumberVisualTransformationTest`, `AuthUseCasesTest` |
| FR-002 | `ScheduleScreen`, `ScheduleFilterSheet`, кэш `InMemoryScheduleCache` | Выполнено | `GetScheduleUseCaseTest`, `ScheduleRepositoryImplTest`, `ScheduleViewModelTest` |
| FR-003 | `SlotDetailScreen`, `SlotDetailContent` | Выполнено | `GetSlotDetailsUseCaseTest` |
| FR-004 | `CreateBookingUseCase`, `BookingScreen` без группового количества | Выполнено | `CreateBookingUseCaseTest` |
| FR-005 | `EquipmentPicker` с режимами «Своя/Прокат» и чипами размеров | Выполнено | `BookingMapperTest` |
| FR-006 | `BookingRepositoryImpl.createBooking`, `payment_method = on_site` | Выполнено | `BookingMapperTest`, `BookingRepositoryImplTest` |
| FR-007 | `features/my_bookings/*`, `GET /bookings/my` | Выполнено | `MyBookingsMapperTest`, `MyBookingsUseCasesTest` |
| FR-008 | `CancelBookingUseCase`, `CancelBookingSheet` | Выполнено | `MyBookingsUseCasesTest` |
| FR-009 | Отображение статуса и причины отмены залом | Выполнено | `MyBookingsMapperTest` |
| FR-010 | `POST /bookings/{id}/rating` и `SCR-008` | **Не реализовано** | Нет |
| FR-011 | Регистрация push-токена, `POST /devices` | **Не реализовано** | Нет |

### 6.3 NFR

| NFR | Проверка | Статус |
|---|---|---|
| NFR-001 | Открытие расписания ≤2 с; скелетон только при пустом списке, обновление в фоне | Выполнено архитектурно (`ScheduleState.Refreshing`), нужен прогон на устройстве |
| NFR-002 | Отклик бронирования ≤1,5 с | Зависит от backend, измеряется во внешнем QA |
| NFR-003 | 409 без фантомной брони | Выполнено, `BookingRepositoryImplTest` |
| NFR-004 | Телефон только `+7XXXXXXXXXX` | `PhoneNumberValidatorTest` |
| NFR-005 | Кэш помечается, бронь блокируется офлайн | `ScheduleRepositoryImplTest`, `ScheduleOfflineBanner` |
| NFR-006 | Минимальная ширина 360 dp, touch target 48 dp | Выполнено, статический аудит разметки |
| NFR-007 | Empty State расписания | `ScheduleEmptyView`, `GetScheduleUseCaseTest` |

### 6.4 Подтверждение критических ограничений

- **BR-004**: клиент не создаёт слоты; лимиты 8 и 16 зафиксированы в `TrainingFormat.maxCapacity` и в `TrainingSlotSummary.capacity` (1..16) — `TrainingFormatCapacityTest`.
- **BR-005**: в `CreateBookingRequest` отсутствует количество мест, UI не содержит степпера, в форме зафиксирован текст «Одна бронь — один человек» — `CreateBookingUseCaseTest`, `BookingMapperTest`.
- **BR-006**: `EquipmentSelections` требует отдельных полей `shoes` и `harness`, каждое со своим `own`/`rental` — `BookingMapperTest`.
- **BR-007**: отмена блокируется после `cancel_deadline`, в шторке отмены текст «не позднее чем за 2 часа до начала тренировки» — `MyBookingsUseCasesTest`.
- **BR-011**: `PaymentMethod.ON_SITE` — единственное значение, `on_site` не переопределяется UI — `BookingMapperTest`.
- **BR-013**: при 409 клиент не создаёт локальную бронь, обновляет источник истины и предлагает «Обновить» либо переход в «Мои записи» — `BookingRepositoryImplTest`, `BookingViewModelTest`.

### 6.5 Статус покрытия unit-тестами

Автоматизировано 72 unit-теста в 25 классах: auth и ввод номера (13), валидация, сеть и тема (6), расписание, даты и артворк (26), бронирование и «Мои записи» (13), навигация и профиль (14). Отсутствуют UI-тесты на Loading/Empty/Error/Offline/Forbidden, контрактные тесты против запущенного Client API и race-тест 409 — они требуют внешнего backend и помечены Pending.

### 6.6 Обнаруженные пробелы

- `FR-010` (оценка инструктора, `SCR-008`) и `FR-011`/`BR-010` (push-уведомления) не реализованы в клиенте: DTO `RatingDto` и эндпоинты OpenAPI существуют, но UI и регистрация устройства отсутствуют.
- Точные цвета, шрифты и логотип не сверены пиксельно с Figma, так как файл недоступен для прямого доступа.

