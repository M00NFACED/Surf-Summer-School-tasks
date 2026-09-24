# Sequence-схемы Client API

## 1. Метаданные

| Поле | Значение |
|---|---|
| Документ | `API-SEQUENCE-001` |
| Назначение | Показать взаимодействие приложения, Client API, backend и внешних сервисов |
| Сценарии | SMS-вход, бронирование, отмена, отмена скалодромом |
| Источник | BR-001, BR-004, BR-007, BR-008, BR-013; FR-001, FR-004, FR-006, FR-008, FR-009, FR-011 |
| Статус | MVP baseline |

## 2. Вход по SMS

```mermaid
sequenceDiagram
    autonumber
    actor Client as Клиент
    participant App as Mobile App
    participant API as Client API
    participant SMS as SMS Provider
    participant Auth as Backend Auth

    Client->>App: Открывает SCR-001 и вводит +7XXXXXXXXXX
    App->>App: Проверяет формат телефона
    App->>API: POST /auth/request-code
    API->>SMS: Отправляет SMS-код
    SMS-->>Client: Код доставлен
    API-->>App: 202 Accepted
    Client->>App: Вводит код на SCR-002
    App->>API: POST /auth/verify-code
    API->>Auth: Проверяет телефон и одноразовый код

    alt Код действителен
        Auth-->>API: Клиент и сессия подтверждены
        API-->>App: 200 TokenResponse (Bearer token)
        App->>App: Сохраняет сессию
        App-->>Client: Открывает SCR-003
    else Код неверный или истёк
        Auth-->>API: Отказ верификации
        API-->>App: 400/401 ErrorResponse
        App-->>Client: Показывает ошибку и возможность повторить
    end
```

## 3. Создание брони с выбором снаряжения

```mermaid
sequenceDiagram
    autonumber
    actor Client as Клиент
    participant App as Mobile App
    participant API as Client API
    participant Backend as Existing Backend

    Client->>App: Выбирает слот на SCR-003/SCR-004
    App->>API: GET /slots/{id}
    API->>Backend: Получает актуальные места и прокат
    Backend-->>API: SlotDetail + EquipmentOption[]
    API-->>App: 200 TrainingSlot
    Client->>App: Открывает SCR-005 и выбирает своё / прокат
    App->>App: Проверяет выбор и отсутствие сети
    App->>API: POST /bookings (slot_id, equipment, payment_method=on_site)
    API->>Backend: Атомарная проверка capacity и rental-фонда

    alt Место доступно
        Backend-->>API: Бронь зафиксирована
        API-->>App: 201 Created Booking
        App->>API: GET /bookings/my
        API-->>App: 200 MyBookingsResponse
        App-->>Client: Показывает «Подтверждена» и «Оплата на месте»
        App->>Client: Переход к SCR-007
    else Последнее место занято
        Backend-->>API: Конфликт атомарной проверки
        API-->>App: 409 Conflict ErrorResponse
        App-->>Client: «Место только что занято», фантомная бронь не создаётся
        App->>API: GET /slots/{id} для обновления остатка
    end
```

## 4. Клиентская отмена брони

```mermaid
sequenceDiagram
    autonumber
    actor Client as Клиент
    participant App as Mobile App
    participant API as Client API
    participant Backend as Existing Backend

    Client->>App: Открывает SCR-007 и нажимает «Отменить»
    App->>App: Показывает дату, время и подтверждение
    Client->>App: Подтверждает отмену
    App->>API: POST /bookings/{id}/cancel
    API->>Backend: Проверяет владельца, статус и серверное время

    alt now <= starts_at - 2 hours
        Backend-->>API: Статус cancelled_by_client
        API-->>App: 200 OK Booking
        App-->>Client: Показывает «Отменена клиентом»
    else now > starts_at - 2 hours
        Backend-->>API: CANCELLATION_DEADLINE_PASSED
        API-->>App: 400 Bad Request ErrorResponse
        App-->>Client: Показывает ограничение и оставляет бронь активной
    end
```

## 5. Отмена слота скалодромом и push

```mermaid
sequenceDiagram
    autonumber
    actor Client as Клиент
    participant App as Mobile App
    participant API as Client API
    participant Backend as Existing Backend
    participant Push as Push Provider

    Backend->>API: Событие slot.cancelled (reason, slot_id)
    API->>Backend: Обновляет статус слота и связанных Booking
    Backend-->>API: Booking.status = cancelled_by_venue
    API->>Push: Отправляет событие для устройств клиента
    Push-->>App: Push: «Тренировка отменена скалодромом», booking_id
    App->>API: GET /bookings/my при открытии deep link
    API-->>App: Бронь с причиной и статусом «Отменена скалодромом»
    App-->>Client: Открывает SCR-007

    alt booking_id валиден
        App-->>Client: Показывает статус, причину и блокировку повторной записи
    else booking_id отсутствует или устарел
        App-->>Client: Открывает SCR-006 и просит выбрать запись
    end
```

## 6. Правила обработки результатов

| Результат | Поведение приложения | Трассируемость |
|---|---|---|
| `201 Created` на `/bookings` | Создать только одну подтверждённую запись, показать оплату на месте | FR-004, FR-006, NFR-002, NFR-003 |
| `409 Conflict` | Не добавлять локальную бронь, обновить слот, показать конкуренцию | FR-004, NFR-003, INV-002 |
| `200 OK` на отмену | Перерисовать детали и список из ответа API | FR-008, BR-007 |
| `400 Bad Request` по дедлайну | Оставить броню без изменения, показать срок 2 часа | FR-008, BR-007, INV-005 |
| Push от отмены | Не удалять бронь, открыть SCR-007, показать причину | FR-009, FR-011, BR-008, BR-010 |
| Ошибка сети на записи | Показать Offline и не создавать Pending-бронь | NFR-005, FR-006 |

## 7. Ответственность участников

- **Mobile App:** собирает ввод, показывает состояния, блокирует повторные команды и обрабатывает ответы.
- **Client API:** формирует клиентский контракт, проверяет токен и передаёт атомарный результат backend.
- **Existing Backend:** хранит состояние, выполняет atomic capacity check и является источником истины.
- **SMS/Push Provider:** доставляет внешний сигнал; ошибка внешнего сервиса не подтверждает бизнес-операцию.
