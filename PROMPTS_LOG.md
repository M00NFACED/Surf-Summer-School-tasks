# Журнал взаимодействия с ИИ

## Проект

- Название: Surf
- Заказчик: Скалодром «Вертикаль»
- Текущий этап: День 1 — анализ, требования к MVP, схема данных
- Скоуп: клиентское мобильное приложение и Client API
- Источник требований: `01-analysis/0-customer-brief/customer-brief.md`

## Формат записи

Каждая сессия содержит дату, запрос, принятые решения, созданные или изменённые артефакты и открытые вопросы.

## Сессии

### 2026-09-24 — Сессия 01

- Запрос: подготовить структуру рабочего пространства, шаблоны анализа и материалы первичного элицитирования.
- Принятые решения: использовать только роль Клиента; внешние админка, интерфейс инструктора и бэкенд считать готовыми black-box системами; онлайн-оплату не включать в MVP.
- Артефакты: `01-analysis/templates/_SCREEN_TEMPLATE.md`, `01-analysis/templates/_LOGIC_TEMPLATE.md`, `01-analysis/1-elicitation/customer-questions.md`, `01-analysis/1-elicitation/domain-description.md`.
- Открытые вопросы: подтвердить правила отмены, лимиты групп, модель проката и порядок уведомлений при профилактике.

### 2026-09-24 — Сессия 02

- Запрос: декомпозировать канонические требования к MVP по бизнес-, функциональным и нефункциональным требованиям, историям, use cases и границам поставки.
- Принятые решения: закрепить 14 бизнес-требований BR-001…BR-014; добавить авторизацию по телефону и SMS; задать окно отмены 2 часа, бронь на одного человека и оплату на месте; обработать HTTP 409 без фантомной брони; считать онлайн-оплату Won't.
- Артефакты: `01-analysis/2-requirements/01-business-requirements.md`, `02-functional-requirements.md`, `03-non-functional-requirements.md`, `04-user-stories.md`, `05-use-cases.md`, `06-constraints-and-scope.md`, `README.md`.
- Трассируемость: в `README.md` добавлена матрица `BR → US → UC → FR → NFR`; use cases содержат предусловия, Happy Path, альтернативы и потоки исключений.
- Открытые вопросы: контрактные названия методов Client API и точный внешний поставщик SMS/Push будут уточнены на этапе API и дизайна.

### 2026-09-24 — Сессия 03

- Запрос: подготовить архитектурный план мобильного клиента, реестр экранов, дизайн-бриф, концептуальную и логическую модель данных, sequence-схемы и OpenAPI-контракт Client API.
- Принятые решения: зафиксировать 8 экранов `SCR-001…SCR-008`; описать push deep link в детали брони; хранить источник истины в существующем backend; использовать `BookingEquipment` как связь many-to-many; закрепить `HTTP 201` для успешной брони, `HTTP 409` для конкуренции и `HTTP 400` для нарушения окна отмены; phone validation `+7XXXXXXXXXX`; payment method только `on_site`.
- Артефакты: `01-analysis/3-design-brief/screen-registry.md`, `01-analysis/3-design-brief/navigation.md`, `01-analysis/3-design-brief/design-brief.md`, `01-analysis/4-design/data-model.md`, `01-analysis/4-design/api-sequence.md`, `01-analysis/api/openapi.yaml`.
- Проверка: OpenAPI YAML успешно разобран; в спецификации 9 paths и 31 schemas, все обязательные endpoints и `ErrorResponse` присутствуют.
- Открытые вопросы: фактический deployment URL Client API, поставщики SMS/Push и окончательные тексты ошибок будут уточнены перед интеграционной реализацией.

### 2026-09-24 — Сессия 04

- Запрос: подготовить детальные спецификации всех восьми экранов мобильного приложения Клиента на основе шаблона, реестра, дизайн-брифа и OpenAPI-контракта.
- Принятые решения: заполнить в каждом файле метаданные, цель, UI-компоненты с типами и валидацией, состояния Initial/Loading/Success/Empty/Error/Offline/Forbidden, API-вызовы, правила, Given-When-Then AC и трассируемость BR/FR/NFR/US/UC; не добавлять TODO или сокращённые ссылки на другие спецификации.
- Артефакты: `01-analysis/5-mobile-app-spec/SCR-001_Вход_по_телефону.md`, `SCR-002_Ввод_SMS_кода.md`, `SCR-003_Расписание_тренировок.md`, `SCR-004_Карточка_слота.md`, `SCR-005_Оформление_брони.md`, `SCR-006_Мои_записи.md`, `SCR-007_Детали_брони.md`, `SCR-008_Оценка_инструктора.md`.
- Проверка: все 8 файлов содержат 8 секций шаблона, полный набор состояний, AC Given-When-Then и связи с API; обязательные endpoints и статусы 201/400/401/409 отражены.
- Статус: аналитический этап Дня 1 завершён. Следующий этап — дизайн и реализация по утверждённым спецификациям.

### 2026-09-24 — Сессия 05

- Запрос: Настройка правил разработки AGENTS.md и плана реализации.
- Принятые решения: OpenAPI закреплён как единственный контракт; клиент ограничен ролью Клиента и тремя feature-батчами; локальный Go/PostgreSQL backend используется только для разработки и тестов; production backend остаётся black-box; зафиксированы package-by-feature, Compose Multiplatform, Ktor, base URL и Android network security правила.
- Артефакты: `AGENTS.md`, `IMPLEMENTATION_PLAN.md`.
- План: Stage 1 — инфраструктура и Docker backend; Stage 2 — OTP (`SCR-001`, `SCR-002`); Stage 3 — расписание и Offline-кэш (`SCR-003`); Stage 4 — детали слота и бронирование с rental и `409` (`SCR-004`, `SCR-005`).
- Проверка: обязательные секции AGENTS и чек-листы этапов созданы; правила BR-004, BR-005, BR-006, BR-007, BR-011 и traceability BR/FR/NFR/SCR/UC отражены.
- Статус: правила агента и план реализации готовы к использованию на следующем этапе.

### 2026-09-24 — Сессия 06

- Запрос: Реализация инфраструктуры, Docker backend и network security.
- Принятые решения: перенести план в `IMPLEMENTATION_PLAN.md`; реализовать локальный Go/PostgreSQL backend по OpenAPI; хранить OTP в памяти и печатать шестизначный код в stdout; применять транзакционную блокировку слота и rental-опций; не добавлять онлайн-оплату.
- Артефакты: `IMPLEMENTATION_PLAN.md`, `backend/compose.yaml`, `backend/Dockerfile`, `backend/go.mod`, `backend/go.sum`, `backend/cmd/server/`, `backend/internal/`, `backend/migrations/`, `backend/seed/`, `client/androidApp/src/main/AndroidManifest.xml`, `client/androidApp/src/main/res/xml/network_security_config.xml`.
- Проверка: `go vet ./...` и `go test ./...` прошли; `docker compose config --no-interpolate` прошёл; SQL static checks подтвердили 7 таблиц, 8/16 constraints, 5 инструкторов и 7-дневные fixtures; handler test подтвердил HTTP 202 и OTP stdout-лог.
- Runtime: Docker Desktop Linux engine возвращает HTTP 500; контейнеры не запущены, миграции в БД и curl `/health`/`POST /auth/request-code` фактически не выполнены. Повторить после восстановления daemon.

### 2026-09-25 — Сессия 07

- Запрос: реализовать Feature 1 авторизации OTP для Compose Multiplatform-клиента, настроить Ktor/токен-хранилище и проверить локальный Docker backend; отметить выполненные пункты секций 4 и 5 плана.
- Принятые решения: добавить отдельный KMP-модуль `client/shared`; использовать Compose compiler plugin Kotlin 2.0.21; оставить auth DTO в `data/`, доменную валидацию и `RequestCodeUseCase`/`VerifyCodeUseCase` без Compose/Ktor; хранить токен через `TokenStorage`; фильтровать auth-запросы из debug-логов; разрешить cleartext только для доменов из `network_security_config.xml`.
- Артефакты: `settings.gradle.kts`, `client/shared/build.gradle.kts`, `client/shared/src/commonMain/kotlin/org/example/client/core/`, `features/auth/`, `features/schedule/presentation/ScheduleShellScreen.kt`, `App.kt`, `client/shared/src/commonTest/`, `client/androidApp/src/main/AndroidManifest.xml`, `client/androidApp/src/main/res/xml/network_security_config.xml`, `IMPLEMENTATION_PLAN.md`.
- Проверка: `:client:shared:compileKotlinJvm` и `:client:shared:jvmTest` проходят; unit-тесты телефона, пустого ввода и OTP проходят; Docker-контейнеры `backend` и `postgres` healthy; `GET /health` вернул `200`, `POST /auth/request-code` вернул `202` с `retry_after=60`; `go vet ./...` и `go test ./...` прошли.
- Ограничения: Android и desktop Gradle-модули ещё не подключены, поэтому их build и UI/integration tests остаются pending; в плане отмечены только фактически проверенные пункты.

### 2026-09-25 — Сессия 08

- Запрос: реализовать Feature 2 «Расписание тренировок, фильтрация и оффлайн-кэш» для `SCR-003`, подключить экран после OTP, logout и обновить Section 6 плана.
- Принятые решения: использовать `GET /slots` с Bearer-токеном и query `from`, `to`, `format`, `instructor_id`; период по умолчанию — ближайшие 7 дней; `InstructorSummary` использует точные поля OpenAPI-схемы `Instructor`; кэш хранит последний успешный snapshot, время сохранения и Offline-флаг; 401 завершает клиентскую сессию, 503/сетевая ошибка используют кэш; фильтры debounce 300 мс с отменой предыдущей job.
- Трассируемость: `BR-001`, `BR-002`, `BR-003`, `BR-014`, `FR-002`, `NFR-001`, `NFR-005`, `NFR-007`, `SCR-003`, `UC-002`.
- Артефакты: DTO и repository/data в `client/shared/src/commonMain/kotlin/org/example/client/features/schedule/data/`, domain-модели и `GetScheduleUseCase` в `features/schedule/domain/`, UI-компоненты и `ScheduleViewModel` в `features/schedule/presentation/`, `App.kt`, `ScheduleTestFixtures.kt`, schedule unit-тесты, `IMPLEMENTATION_PLAN.md`.
- Проверка: `:client:shared:compileKotlinJvm`, `:client:shared:jvmTest` и `:client:shared:build` проходят; unit-тесты покрывают query, 7-дневный период, DTO-маппинг, 0 мест/cancelled и offline fallback; локальный smoke `OTP → Bearer → GET /slots` вернул 21 слот; `go vet ./...` и `go test ./...` прошли.
- Ограничения: отдельные contract/UI/performance tests и Android/desktop target ещё не подключены; визуальная проверка и gate acceptance остаются pending.

### 2026-09-25 — Сессия 09

- Запрос: реализовать Feature 3 «Детали слота, оформление брони с прокатом и обработка 409 Conflict» для `SCR-004`/`SCR-005`, подключить сквозную навигацию и выполнить Docker smoke.
- Принятые решения: `GET /slots/{id}` и `POST /bookings` используют Bearer-токен; `CreateBookingRequest` не содержит количества людей или `client_id`; для скальников и системы валидируются отдельные `own/rental` selections и доступные option IDs; `payment_method` фиксирован как `on_site`; HTTP 201 создаёт только подтверждение, HTTP 409 преобразуется в `SlotFullException`, обновляет детали и не создаёт локальную бронь; повторный submit блокируется.
- Трассируемость: `BR-004`, `BR-005`, `BR-006`, `BR-011`, `BR-013`, `BR-014`, `FR-003`, `FR-004`, `FR-005`, `FR-006`, `NFR-002`, `NFR-003`, `NFR-005`, `NFR-006`, `SCR-004`, `SCR-005`, `UC-002`, `UC-003`.
- Артефакты: booking DTO/data/repository/mapper в `features/booking/data/`, domain models/use cases в `features/booking/domain/`, `SlotDetailScreen`, `BookingScreen`, `EquipmentPicker`, `BookingViewModel` и state в `features/booking/presentation/`, navigation в `App.kt`, booking unit-тесты, `IMPLEMENTATION_PLAN.md`.
- Проверка: `:client:shared:jvmTest` и `:client:shared:build` проходят; unit-тесты проверяют `on_site`, отсутствие participant quantity, обязательное снаряжение и rental availability; Docker smoke `OTP → GET /slots (200) → GET /slots/{id} (200) → POST /bookings (201)` вернул `confirmed` и `payment_method=on_site`; `go vet ./...` и `go test ./...` прошли.
- Ограничения: отдельные contract/UI/race/performance tests и Android/desktop target остаются pending; `GET /bookings/my` для неоднозначного сетевого ответа ещё не реализован.

### 2026-09-25 — Сессия 10

- Запрос: добавить Gradle Wrapper 8.10.2 и модуль `:client:desktopApp` для интерактивного запуска UI на Windows без Android Studio.
- Принятые решения: централизовать Kotlin/Compose plugin versions в корневом `build.gradle.kts` с `apply false`; использовать `jvm("desktop")`, `compose.desktop.currentOs` и `MainKt`; окно запускать в мобильных пропорциях 400×800 dp.
- Артефакты: `gradlew`, `gradlew.bat`, `gradle/wrapper/`, `build.gradle.kts`, `client/desktopApp/build.gradle.kts`, `client/desktopApp/src/desktopMain/kotlin/org/example/client/Main.kt`, `settings.gradle.kts`, `IMPLEMENTATION_PLAN.md`.
- Проверка: `.\gradlew :client:desktopApp:compileKotlinDesktop` проходит; wrapper загружает Gradle 8.10.2; общий `:client:shared:build` проверен после подключения модуля.
- Ограничения: интерактивное окно не запускалось автоматически; для просмотра используйте команду из итогового отчёта.

### 2026-09-25 — Сессия 11

- Запрос: настроить `:client:androidApp` для Android Studio, добавить Android target в shared и `MainActivity`.
- Принятые решения: использовать AGP 8.5.2, Kotlin Android 2.0.21, compile/target SDK 34 и Java/Kotlin JVM target 17; shared подключает `androidTarget()` и OkHttp engine, desktop сохраняет CIO; включён `android.useAndroidX=true`.
- Артефакты: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `client/shared/build.gradle.kts`, `client/androidApp/build.gradle.kts`, `client/androidApp/src/main/kotlin/org/example/client/MainActivity.kt`, `AndroidManifest.xml`, `IMPLEMENTATION_PLAN.md`.
- Проверка: `.\gradlew :client:androidApp:assembleDebug` проходит; `:client:shared:build` и `:client:desktopApp:compileKotlinDesktop` проходят; Android debug APK собран.
- Ограничения: запуск на эмуляторе/устройстве не выполнялся; для Android Studio требуется локальный SDK и JBR/JDK 17.

### 2026-09-25 — Сессия 12

- Запрос: исправить маску ввода телефона и обработать сетевой крэш `SocketException`; сделать базовый URL Android настраиваемым для `127.0.0.1` через `adb reverse` и `10.0.2.2` на эмуляторе.
- Принятые решения: поле хранит только 10 национальных цифр, отбрасывает ведущие `7`/`8` и визуально форматируется как `+7 (XXX) XXX-XX-XX`; API получает E.164; `CancellationException` пробрасывается, остальные ошибки переводятся в состояние с сообщением `Не удалось подключиться к серверу. Проверьте соединение.`; Android debug URL задаётся свойством `apiBaseUrl` с default `127.0.0.1`, desktop явно использует localhost.
- Трассируемость: `BR-001`, `BR-003`, `BR-004`, `BR-005`, `BR-013`, `FR-001`, `FR-002`, `FR-004`, `NFR-003`, `NFR-004`, `NFR-005`, `SCR-001`, `SCR-003`, `SCR-004`, `SCR-005`.
- Артефакты: `PhoneNumberValidator.kt`, `PhoneEntryScreen.kt`, `PhoneNumberVisualTransformation.kt`, `NetworkConfig.kt`, `NetworkError.kt`, `AuthRepositoryImpl.kt`, `AuthViewModel.kt`, `ScheduleViewModel.kt`, `BookingViewModel.kt`, Android/desktop entry points, тесты масок, NetworkConfig и ViewModel.
- Проверка: `:client:shared:build` проходит; `:client:shared:jvmTest` проходит; `:client:desktopApp:compileKotlinDesktop` проходит; `:client:androidApp:assembleDebug` проходит с default URL и с `-PapiBaseUrl=http://10.0.2.2:8080`; `git diff --check` проходит.
- Ограничения: запуск UI на физическом устройстве/эмуляторе и ручная проверка `adb reverse` не выполнялись; для физического Android требуется проброс порта, для эмулятора можно передать `apiBaseUrl=http://10.0.2.2:8080`.
- Нумерация: запись продолжает журнал как Сессия 12, поскольку Сессии 10 и 11 уже заняты desktop и Android.

### 2026-09-25 — Сессия 13

- Запрос: исправить `substring index out of bounds` в прогрессивном формате телефона и сброс экрана авторизации; проверить desktop tests и Android debug build.
- Принятые решения: `format` использует безопасные `minOf`-срезы для 0–10 цифр; `OffsetMapping` клампит границы и проверяет все промежуточные длины; `config`, storage и `AuthViewModel` стабилизированы через `remember`; `CodeSent.phone` напрямую передаётся в `OtpVerificationScreen`; OTP-экран переименован из `OtpEntryScreen`.
- Трассируемость: `BR-001`, `FR-001`, `NFR-004`, `SCR-001`, `SCR-002`; OpenAPI не изменялся.
- Артефакты: `PhoneNumberValidator.kt`, `PhoneNumberVisualTransformation.kt`, `PhoneEntryScreen.kt`, `OtpVerificationScreen.kt`, `App.kt`, `SCR-002_Ввод_SMS_кода.md`, тесты форматтера/маски/AuthViewModel.
- Проверка: `:client:shared:jvmTest`, `:client:shared:build`, `:client:desktopApp:compileKotlinDesktop` и `:client:androidApp:assembleDebug` проходят; запрошенная `:client:shared:desktopTest` не запускается, потому что в shared используется target `jvm()` и такой Gradle task отсутствует.
- Ограничения: UI-проверка на устройстве/эмуляторе не выполнялась; `desktopTest` требует отдельного alias/переименования target и не менялся в рамках bugfix.

### 2026-09-25 — Сессия 14

- Запрос: выполнить QA и стабилизацию MVP; исправить навигацию slot/instructor IDs, persistent auth token, OTP Backspace и тёмную тему; добавить test matrix/report.
- Принятые решения: UUID validator принимает canonical 8-4-4-4-12 hex независимо от version/variant; IDs нормализуются в presentation/domain/data, path строится через Ktor URL segments; Android использует `SharedPreferencesSettings`, desktop — `PreferencesSettings` через `SettingsTokenStorage`; startup восстанавливает token и открывает расписание; OTP Backspace очищает предыдущую ячейку; root выбирает системную dark/light MaterialTheme scheme.
- Трассируемость: `BR-001`, `BR-003`, `BR-004`, `BR-005`, `BR-007`, `BR-013`, `FR-001`, `FR-002`, `FR-003`, `FR-004`, `FR-005`, `FR-006`, `FR-008`, `NFR-003`, `NFR-004`, `NFR-005`, `NFR-006`, `NFR-007`, `SCR-001`…`SCR-007`; OpenAPI не изменялся.
- Артефакты: `UuidValidator.kt`, `GetScheduleUseCase.kt`, `GetSlotDetailsUseCase.kt`, `CreateBookingUseCase.kt`, `ScheduleViewModel.kt`, `BookingViewModel.kt`, `KtorBookingRemoteDataSource.kt`, `ScheduleQueryBuilder.kt`, `App.kt`, platform entry points, `OtpVerificationScreen.kt`, `ScheduleScreen.kt`, `PhoneEntryScreen.kt`, `01-analysis/6-testing/test-matrix.md`, `01-analysis/6-testing/test-report.md`, unit/storage tests.
- Проверка: `.\gradlew :client:shared:jvmTest` — Passed; `.\gradlew :client:androidApp:assembleDebug` — Passed; `.\gradlew :client:shared:build` и `.\gradlew :client:desktopApp:compileKotlinDesktop` — Passed.
- Ограничения: ручной запуск на физическом устройстве/эмуляторе, реальный SMS и race-тест внешнего backend остаются pending; в test-report device-level пункты явно отмечены.

### 2026-09-25 — Сессия 15

- Запрос: привести интерфейс к Figma «Волна»: фирменная emerald/teal палитра в обеих темах, BottomBar, «Мои записи» с отменой, компактный прокат, удобные даты и понятный duplicate-409.
- Принятые решения: использовать `WaveTheme` с явными `WaveLightColorScheme`/`WaveDarkColorScheme` без Purple-дефолтов; навигацию вынести в `AuthenticatedShell` с сохранением вкладок; переиспользовать booking DTO и добавить `GET /bookings/my`/`POST /bookings/{id}/cancel`; дедлайн отмены брать из `cancel_deadline`; `BOOKING_EXISTS` отображать отдельным CTA перехода в «Мои записи»; rental-опции показывать горизонтальными чипами размера и остатка; date filter поддерживает одну границу, `ДД.ММ.ГГГГ`, quick chips и Material3 DatePickerDialog.
- Трассируемость: `BR-004`, `BR-005`, `BR-006`, `BR-007`, `BR-011`, `BR-013`, `FR-002`, `FR-004`, `FR-005`, `FR-006`, `FR-007`, `FR-008`, `NFR-003`, `NFR-005`, `NFR-006`, `NFR-007`, `SCR-003`, `SCR-005`, `SCR-006`, `SCR-007`, `SCR-008`; OpenAPI не изменялся.
- Артефакты: `WaveTheme.kt`, `WaveBottomBar.kt`, `MainTab.kt`, `AuthenticatedShell.kt`, `features/my_bookings/` (domain/data/presentation), `features/profile/presentation/ProfileScreen.kt`, `MyBookingsResponse.kt`, booking repository/remote methods, `EquipmentPicker.kt`, `ScheduleDateFilterView.kt`, `BookingState.kt`/repository duplicate mapping, `App.kt`, `WaveThemeTest`, `MyBookingsUseCasesTest`, `MyBookingsMapperTest`, `BookingRepositoryImplTest`, `GetScheduleUseCaseTest`, `test-matrix.md`, `PROMPTS_LOG.md`.
- Проверка: `.\gradlew :client:shared:jvmTest` — Passed; `.\gradlew :client:androidApp:assembleDebug` — Passed; `.\gradlew :client:shared:build` — Passed; `.\gradlew :client:desktopApp:compileKotlinDesktop` — Passed.
- Ограничения: Figma-файл недоступен для прямого web-fetch, визуальное соответствие сверено по переданным токенам и описанию; device-level проверка BottomBar/DatePicker/отмены остаётся manual.

### 2026-09-25 — Сессия 16

- Запрос: сверить реализацию с присланными фреймами Figma (вход, OTP, расписание, шторка фильтров, карточка слота, оформление, успех, мои записи, детали, отмена, профиль) и привести UI к макету по стилю и структуре.
- Решение по приоритету: макет — референс стиля и структуры, продуктовые правила и Client API имеют приоритет. Макет сделан для другого домена, поэтому его продуктовые механики не переносятся.
- Зафиксированные расхождения макета и проекта: групповое количество мест и «Итого ×N» не переносятся (BR-005, CON-004, в `CreateBookingRequest` нет `places`); «доска для каждого места» заменено раздельным выбором скальников и страховочной системы (BR-006, `EquipmentSelections`); цена тренировки не показывается — в API есть только `EquipmentOption.price`; экран «Как вас зовут?» и правка имени пропущены — нет эндпоинта профиля; фото и карта — заглушки, координат и `image_url` в API нет; текст отмены сформулирован строго по BR-007 и `customer-brief` (отмена не позднее чем за 2 часа, штраф не взимается); фильтр использует форматы из API и инструкторов из загруженных слотов.
- Стиль по макету: teal `#4E9E92` / `#6FBFB2`, карточка `#F1F1F1`, чипы `#EFEFEF`, бейджи `#C9E7C4` и `#F5EFA6`, pill-BottomBar без подписей, заголовки по центру, круглая кнопка «назад», иконка фильтров, `ModalBottomSheet` для фильтров и отмены, сегментированный переключатель «Предстоящие/Прошедшие», BottomBar скрывается на детальных экранах.
- Трассируемость: `BR-002`, `BR-003`, `BR-005`, `BR-006`, `BR-007`, `BR-011`, `BR-014`, `FR-001`…`FR-008`, `NFR-003`, `NFR-005`, `NFR-006`, `NFR-007`, `SCR-001`…`SCR-007`, `CON-004`, `CON-005`; OpenAPI не изменялся, `01-analysis` не менялся.
- Артефакты: `core/theme/WaveColors.kt`, `core/theme/WaveTheme.kt`, `core/ui/WaveTopBar.kt`, `WaveBadge.kt`, `WaveControls.kt`, `WaveFilters.kt`, `WaveSegmentedToggle.kt`, `features/navigation/*`, `features/schedule/presentation/ScheduleFilterSheet.kt`, `ScheduleFilterSections.kt`, `ScheduleDateFilterSection.kt`, `ScheduleCard.kt`, `ScheduleScreen.kt`, `ScheduleDateFormatter.kt`, `ScheduleFilter.kt`, `GetScheduleUseCase.kt`, `features/booking/presentation/SlotDetailContent.kt`, `SlotDetailError.kt`, `BookingSummaryCard.kt`, `BookingSuccessView.kt`, `BookingScreen.kt`, `EquipmentPicker.kt`, `SlotDetailScreen.kt`, `features/my_bookings/presentation/*`, `features/profile/presentation/ProfileScreen.kt`, `features/auth/presentation/PhoneEntryScreen.kt`, `OtpVerificationScreen.kt`, `AuthViewModel.kt`, `App.kt`, тесты `WaveThemeTest`, `GetScheduleUseCaseTest`, `ScheduleDateFormatterTest`.
- Проверка: `.\gradlew :client:shared:jvmTest` — Passed; `.\gradlew :client:shared:build` — Passed; `.\gradlew :client:desktopApp:compileKotlinDesktop` — Passed; `.\gradlew :client:androidApp:assembleDebug` — Passed.
- Ограничения: точные hex и шрифты взяты по скриншотам и требуют пиксельной сверки в Figma; логотип «ВОЛНА» не воспроизводится; device-level проверка шторок и pill-навигации остаётся manual.
