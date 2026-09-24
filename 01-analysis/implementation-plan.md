# План реализации MVP Surf

## 1. Метаданные

| Поле | Значение |
|---|---|
| Документ | `IMPLEMENTATION-PLAN-001` |
| Назначение | Пошаговая реализация клиентского MVP тремя feature-батчами |
| Клиент | Compose Multiplatform, Kotlin |
| API | Client API по `01-analysis/api/openapi.yaml` |
| Локальная инфраструктура | Go + PostgreSQL в Docker для разработки и тестов |
| Источник экранов | `01-analysis/3-design-brief/screen-registry.md` и `01-analysis/5-mobile-app-spec/` |
| Статус | План реализации |
| Ограничение | Backend production остаётся внешней black-box системой |

## 2. Цели и правила выполнения

- Реализовать только клиентское мобильное приложение Клиента и контракт Client API.
- Следовать `AGENTS.md`, OpenAPI, BR/FR/NFR и спецификациям `SCR-001…SCR-008`.
- Перед каждым этапом сформулировать план файлов, зависимостей и проверок.
- Каждый этап завершать тестами, lint/typecheck при наличии и атомарным коммитом.
- Локальный Go/PostgreSQL backend нужен только для воспроизводимой разработки и контрактных тестов.
- Не реализовывать админку, интерфейс инструктора, онлайн-оплату или управление расписанием в клиенте.

## 3. Карта feature-батчей

| Этап | Feature | Экраны | API | Ключевые правила |
|---|---|---|---|---|
| 1 | Инфраструктура и локальный backend | — | Контракт целиком | Docker, PostgreSQL, OpenAPI, Ktor |
| 2 | `auth` | `SCR-001`, `SCR-002` | `/auth/request-code`, `/auth/verify-code` | E.164, OTP 6 цифр, Bearer |
| 3 | `schedule` | `SCR-003` | `GET /slots` | 7 дней, фильтры, Offline-кэш |
| 4 | `booking` | `SCR-004`, `SCR-005` | `GET /slots/{id}`, `POST /bookings` | 8/16, 1 человек, rental, `409`, `on_site` |

## 4. Этап 1 — Инфраструктура и локальный backend

### 4.1. Каркас репозитория

- [ ] Создать Compose Multiplatform-модули `client`, `androidApp`, `desktopApp` и `local-backend`.
- [ ] Настроить Gradle Kotlin, Compose Multiplatform, Ktor Client, Ktor serialization и общие source sets.
- [ ] Создать `client/src/commonMain/kotlin/org/example/client/` с корневой навигацией и DI.
- [ ] Создать package-by-feature каталоги `auth`, `schedule`, `booking` с `presentation/`, `domain/`, `data/`.
- [ ] Создать `androidApp/src/main/res/xml/network_security_config.xml` с cleartext только для `10.0.2.2`, `127.0.0.1`, `localhost`.
- [ ] Вынести base URL в debug/local конфигурацию: Emulator `http://10.0.2.2:8080`, desktop/local `http://127.0.0.1:8080`.
- [ ] Добавить `.env.example` с именами переменных без секретов; реальные credentials исключить из Git.

### 4.2. OpenAPI и локальный backend

- [ ] Проверить `01-analysis/api/openapi.yaml` как единственный контракт и зафиксировать его версию в CI.
- [ ] Поднять Docker Compose с сервисами `api` и `postgres`, healthcheck и отдельным volume для БД.
- [ ] Реализовать в Go каркасы всех операций контракта или тестовые адаптеры с теми же путями, DTO и кодами ответов.
- [ ] Создать PostgreSQL migrations для Client, Instructor, TrainingSlot, EquipmentOption, Booking, Rating и NotificationDevice.
- [ ] Добавить fixtures для двух форматов, инструкторов, 8/16 вместимости, проката и свободных мест.
- [ ] Реализовать атомарную проверку capacity и rental-фонда в локальном backend для теста 409.
- [ ] Добавить проверку ограничения одной брони на клиента/слот и запрета бронирования отменённого слота.
- [ ] Добавить `/health` вне клиентского контракта только как инфраструктурный endpoint.

### 4.3. Общий Ktor-клиент и кэш

- [ ] Настроить `HttpClient` с `Logging` уровня `HEADERS/BODY` только в debug и редактированием чувствительных значений.
- [ ] Подключить `ContentNegotiation` с JSON и `ignoreUnknownKeys = true`.
- [ ] Создать DTO-слой строго по OpenAPI и общий mapper ошибок `ErrorResponse`.
- [ ] Добавить auth interceptor для Bearer и interceptor отмены/повторного submit.
- [ ] Создать cache abstraction для read-only расписания с признаком stale/offline.
- [ ] Добавить unit и contract smoke tests на запуск клиента, base URL и чтение схемы.

### 4.4. Gate этапа

- [ ] `docker compose up` поднимает API и PostgreSQL без ручной подготовки данных.
- [ ] Контрактные smoke-тесты проходят для `200`, `201`, `400`, `409`.
- [ ] Android build собирается с `network_security_config.xml`.
- [ ] Desktop build запускается с `127.0.0.1:8080`.
- [ ] В коде нет feature-логики в `App.kt` и нет ручных дублей DTO.

## 5. Этап 2 — Feature 1: авторизация OTP

### 5.1. Структура

- [ ] Создать `features/auth/presentation/`, `features/auth/domain/`, `features/auth/data/`.
- [ ] Создать отдельные файлы для `PhoneEntryScreen`, `OtpEntryScreen`, ViewModel и UI State.
- [ ] Создать `RequestCodeUseCase`, `VerifyCodeUseCase`, телефонную валидацию и auth repository.
- [ ] Создать отдельные DTO `RequestCodeRequest`, `RequestCodeResponse`, `VerifyCodeRequest`, `TokenResponse` по OpenAPI.
- [ ] Создать token storage abstraction с безопасным хранением вне исходного кода.

### 5.2. Сценарий SCR-001 / SCR-002

- [ ] В `SCR-001` реализовать маску `+7 (___) ___-__-__` и нормализацию `+7XXXXXXXXXX`.
- [ ] Заблокировать CTA для пустого/невалидного номера.
- [ ] Вызвать `POST /auth/request-code`, обработать 202, 400, 429 и 503.
- [ ] В `SCR-002` реализовать шесть цифровых полей, numeric input и одноразовую отправку.
- [ ] Хранить cooldown по `retry_after`, не обходить его и не создавать локальный токен.
- [ ] Вызвать `POST /auth/verify-code`, обработать 200, 400 и 401.
- [ ] Сохранить Bearer-токен только после успешного ответа и открыть расписание.
- [ ] Реализовать Loading, Empty, Error, Offline и Forbidden для обоих экранов.

### 5.3. Проверки feature

- [ ] Unit-тесты на E.164, пустой ввод, неверный OTP и cooldown.
- [ ] UI-тесты на переходы `SCR-001 → SCR-002 → SCR-003`.
- [ ] Проверить отсутствие токена, OTP и полного телефона в debug-логах.
- [ ] Проверить обработку истёкшей сессии без потери безопасного pending deep link.

### 5.4. Gate feature

- [ ] `UC-001` проходит по acceptance criteria.
- [ ] `SCR-001` и `SCR-002` соответствуют соответствующим спецификациям.
- [ ] Auth feature не содержит Compose в `data/` и Ktor в `domain/`.

## 6. Этап 3 — Feature 2: расписание и оффлайн-кэш

### 6.1. Структура и API

- [ ] Создать `features/schedule/presentation/`, `features/schedule/domain/`, `features/schedule/data/`.
- [ ] Создать отдельные файлы для Schedule Screen, slot card, ViewModel, UI State, UseCases и repository.
- [ ] Создать DTO `SlotListResponse`, `TrainingSlotSummary`, `Instructor` по OpenAPI.
- [ ] Реализовать `GET /slots` с параметрами `from`, `to`, `format`, `instructor_id`.
- [ ] Маппить формат `novice_bouldering` как «Новичковый болдеринг», `rope_routes` как «Трассы с верёвкой».
- [ ] Применять дефолтный период ближайших 7 дней, если API не получил период.
- [ ] Отменять устаревший UI-request при быстрой смене фильтров.

### 6.2. UX и кэш

- [ ] Отображать Skeleton 3–4 карточек во время загрузки.
- [ ] Добавить фильтры даты, формата, инструктора и сброс фильтров.
- [ ] Показывать карточку с датой, временем, форматом, инструктором и свободными местами.
- [ ] Показывать Empty State «Пока нет доступных тренировок» при нулевом `items`.
- [ ] Сохранять последний успешный расписание read-only и маркировать его как неактуальное Offline.
- [ ] Не смешивать Error и Empty; при отсутствии сети без кэша показывать Error.
- [ ] Не показывать слоты `cancelled` как доступные к бронированию.

### 6.3. Проверки feature

- [ ] Unit-тесты на построение query и фильтрацию в domain-модели.
- [ ] Contract-тесты на 200, 400, 401 и 503.
- [ ] UI-тесты Skeleton, Success, Empty, Error и Offline.
- [ ] Проверить измеримое открытие расписания не дольше 2 секунд на согласованном тестовом профиле.
- [ ] Проверить, что фильтр инструктора использует UUID, а неизвестные поля ответа не ломают разбор.

### 6.4. Gate feature

- [ ] `UC-002` проходит по acceptance criteria.
- [ ] `SCR-003` соответствует дизайн-брифу и OpenAPI.
- [ ] Offline-чтение не позволяет создать бронь или изменить данные.

## 7. Этап 4 — Feature 3: детали слота и бронирование

### 7.1. Структура и API

- [ ] Создать `features/booking/presentation/`, `features/booking/domain/`, `features/booking/data/`.
- [ ] Создать отдельные файлы для SlotDetail Screen, Booking Screen, ViewModel, UI State, UseCases и repository.
- [ ] Создать DTO `TrainingSlot`, `EquipmentOption`, `CreateBookingRequest`, `Booking` по OpenAPI.
- [ ] Реализовать `GET /slots/{id}` с полями времени, формата, инструктора, адреса, мест и rental options.
- [ ] Блокировать CTA в `SCR-004` при `available_places = 0` или `status = cancelled`.
- [ ] В `SCR-005` показать одну бронь на клиента без селектора количества людей.
- [ ] Реализовать отдельный выбор скальников `own/rental` и системы `own/rental`.
- [ ] Передавать `payment_method = on_site`; не добавлять платёжный endpoint или токен.

### 7.2. Конкуренция и ошибки

- [ ] Проверять доступность rental option и `available_places` до отправки.
- [ ] Отправлять `POST /bookings` один раз и блокировать повторный submit.
- [ ] Обрабатывать HTTP 201 как единственное подтверждение брони.
- [ ] Обрабатывать HTTP 400 как исправимый запрос без создания локальной брони.
- [ ] Обрабатывать HTTP 401 как истёкшую авторизацию.
- [ ] Обрабатывать HTTP 409 как конкуренцию: обновить слот, показать сообщение и не создавать фантомную бронь.
- [ ] При неоднозначном сетевом ответе сначала сверять `GET /bookings/my`, затем разрешать повтор.
- [ ] При Offline блокировать submit и не создавать `Pending`/`confirmed` локально.

### 7.3. Проверки feature

- [ ] Unit-тесты на 1 booking = 1 client, `own/rental` и недоступный rental.
- [ ] Contract-тесты на 201, 400, 401 и 409.
- [ ] Интеграционный race-тест двух запросов на последнее место.
- [ ] UI-тесты на 201 success, 409 conflict, Offline и 0 мест.
- [ ] Проверить, что сообщение «Оплата на месте» отображается до и после успеха.
- [ ] Проверить, что после 409 карточка не появляется как подтверждённая в локальном состоянии.

### 7.4. Gate feature

- [ ] `UC-003` и `UC-004` проходят по acceptance criteria.
- [ ] `SCR-004` и `SCR-005` соответствуют спецификациям.
- [ ] В production-коде нет зависимости от локального backend implementation details.

## 8. Общий порядок выполнения

- [ ] Сначала реализовать Stage 1 и получить рабочий contract test harness.
- [ ] Затем выполнять Stage 2 → Stage 3 → Stage 4, не объединяя feature-батчи в один commit.
- [ ] После каждого этапа обновлять traceability и прогонять общий quality gate.
- [ ] Не добавлять `SCR-006…SCR-008` в первые три feature-батча без отдельного решения о следующем инкременте; их API-контракт уже зафиксирован.
- [ ] При расхождении API и UI остановить реализацию и исправить канонический источник, а не маскировать расхождение DTO.

## 9. Определение готовности MVP-батча

- [ ] Все FR и AC заявленного feature имеют реализацию и тесты.
- [ ] Все UI-состояния из соответствующей `SCR`-спецификации присутствуют.
- [ ] OpenAPI не изменён неявно; изменения контракта проходят review.
- [ ] Нет секретов, временных endpoint или обходных DTO.
- [ ] Собран Android и desktop target.
- [ ] Lint/typecheck/test-команды завершены успешно.
- [ ] Diff чистый, изменения атомарны, commit соответствует Conventional Commits.

## 10. Трассируемость плана

| Этап | BR | FR | NFR | SCR | UC |
|---|---|---|---|---|---|
| Stage 1 | BR-012, BR-013, BR-014 | Инфраструктурный контракт | NFR-002, NFR-003 | Контракт всех экранов | Инфраструктурная база |
| Stage 2 | BR-001, BR-012 | FR-001 | NFR-004 | SCR-001, SCR-002 | UC-001 |
| Stage 3 | BR-001, BR-002, BR-003, BR-014 | FR-002 | NFR-001, NFR-005, NFR-007 | SCR-003 | UC-002 |
| Stage 4 | BR-004, BR-005, BR-006, BR-011, BR-013, BR-014 | FR-003, FR-004, FR-005, FR-006 | NFR-002, NFR-003, NFR-005, NFR-006 | SCR-004, SCR-005 | UC-002, UC-003 |
