# ИС Лаба 1

## Что сделал
| Фича | Детали                                                                           |
| --- |----------------------------------------------------------------------------------|
| ✅ Spring Boot 3 | Стартеры Data JPA, Validation, Web и WebSocket для realtime-обновлений           |
| ✅ Геостек | Flyway + PostgreSQL/PostGIS, Hibernate Spatial и JTS для пространственных данных |
| ✅ OpenAPI-first | `specs/openapi/openapi.yml` генерирует Java API и TypeScript SDK                 |
| ✅ Маппинги | MapStruct, Lombok и annotation processors собирают DTO/Entity                    |
| ✅ Тестирование | JUnit 5 + Testcontainers и Spring Boot test-starters                             |
| ✅ Контейнеризация | Docker Compose разворачивает backend, frontend и PostGIS                         |
| ✅ Monorepo JS | pnpm workspace с веб-приложением и пакетом `@rendaxx/api-ts`                     |
| ✅ Code Style | Spotless, Checkstyle и dependency-management следят за единым стилем             |
| ❌ Деплой на Helios | Пострис там отсутствует, поэтому автоматический деплой невозможен                |
| ✅ Деплой на свой сервер | GitHub CI + Docker workflow выкатывает сервис на собственный хост                |

## Быстрый старт
| Действие | Команда | Комментарий |
| --- | --- | --- |
| Собрать все модули | `./gradlew build` | Java 17, включает Spotless/Checkstyle |
| Запустить backend | `./gradlew :apps:backend:bootRun` | Требуется PostgreSQL (см. Docker ниже) |
| Прогнать тесты | `./gradlew test` | Testcontainers поднимет Postgres автоматически |
| Поднять PostGIS локально | `docker compose up -d db` | Использует `postgis/postgis:16-3.4` |
| Настроить web-workspace | `pnpm install` | Устанавливает генератор TypeScript SDK |

> Конфигурация по умолчанию хранится в `src/main/resources/application.yaml`. Для переопределения подключите `SPRING_DATASOURCE_*` / `SPRING_FLYWAY_*` или переменные из `.env`.

## OpenAPI и SDK
- **Java API** (`libs/api-java`): задача `openApiGenerate` (плагин `org.openapi.generator`) преобразует `specs/openapi/openapi.yml` в интерфейсы `com.rendaxx.labs.api.v1.*`. Генерация запускается перед `compileJava`, вручную можно вызвать `./gradlew :libs:api-java:openApiGenerate`.
- **TypeScript SDK** (`libs/api-ts`):
  ```bash
  pnpm install          # однократно устанавливает инструменты
  pnpm run gen:api      # обновляет libs/api-ts/src/generated
  ```
  Пакет `@rendaxx/api-ts` реэкспортирует сгенерированный код через `libs/api-ts/src/index.ts`. Перед использованием вызовите `createConfiguration` из `generated/runtime`, чтобы передать `VITE_API_BASE_URL` или другой origin.

## Docker окружение
`docker-compose.yaml` разворачивает три сервиса:
1. `app` — Spring Boot backend (порт `PUBLIC_PORT:8080`).
2. `web` — фронтенд-образ (порт `WEB_PUBLIC_PORT:80`).
3. `db` — PostGIS `postgis/postgis:16-3.4` с volume `information-systems-labs-postgres` и healthcheck.

Все переменные берутся из `.env`. По умолчанию наружу открыт только backend/web; порт 5432 рекомендуется проксировать вручную при необходимости.

## Структура репозитория
- `apps/backend` — основной Spring Boot сервис.
- `libs/api-java` — Java API, сгенерированный из OpenAPI.
- `libs/api-ts` — TypeScript SDK (`@rendaxx/api-ts`).
- `apps/web` — клиентское приложение (использует SDK).
- `specs/openapi/openapi.yml` — единый контракт.
- `src/main/java/com/rendaxx/labs/**` — доменные объекты, репозитории, сервисы, мапперы и валидации.
- `src/main/resources/db/migration` — миграции Flyway (`V{n}__description.sql`).

## Задание лабораторной
```java
public class Route {
    private Long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private List<RoutePoint> routePoints; // не нулл, не пустой
    private Vehicle vehicle; // может быть нулл (если пока не назначили)
    private LocalDateTime creationTime; // дата-время создания
    private LocalDateTime plannedStartTime; // время начала маршрута
    private LocalDateTime plannedEndTime; // время конца маршрута
    private BigDecimal mileageInKm; // пробег (в км)
}

public class RoutePoint { // точка маршрута
    private Long id; // больше 0, уникальное, генерируется
    private Route route; // не нулл
    private RetailPoint retailPoint; // не нулл
    private OperationType operationType; // не нулл
    private List<Order> orders; // заказы которые надо увезти/привезти на эту точку, не пустой не нулл
    private LocalDateTime plannedStartTime; // время приезда на точку
    private LocalDateTime plannedEndTime; // время убытия с точки
    Integer orderNumber; // порядковый номер точки в маршруте, больше равно 0, не нулл
}

public class RetailPoint {
    private Long id; // больше 0, уникальное, генерируется
    private String name; // уникальное, не нулл не пустое
    private String address; // уникальное, не нулл не пустое
    private Coordinates coordinates; // координаты точки, не нулл не пустое (можно ещё прикрутить интеграцию с сервисом поиска координат по адресу :) )
    private PointType type; // тип точки не нулл
    private String timezone; // часовой пояс точки, не нулл не пустое и ваще валидная таймзона
}

public class Order {
    private Long id; // больше 0, уникальное, генерируется
    private String goodsType; // не нулл, не пустое
    private Integer minTemperature; //может быть нулл
    private Integer maxTemperature; //может быть нулл
    double volumeInCubicMeters; // больше 0
    double weightInKg; // больше 0
}

public class Vehicle {
    private Long id; // больше 0, уникальное, генерируется
    private Driver driver; // может быть нулл
    private String gosNumber; //номер машины, не нулл не пустое уникальное
    private double tonnageInTons; // больше 0
    private double bodyHeightInMeters; // больше 0
    private double bodyWidthInMeters; // больше 0
    private double bodyLengthInCubicMeters; // больше 0
}

public class Driver {
    private Long id; // больше 0, уникальное, генерируется
    private String firstName; // не пустое не нулл
    private String middleName; // не пустое не нулл
    private String lastName; // может быть нулл
    private String passport; // паспортные данные, уникальные, не нулл не пустые
}


public enum OperationType {
    LOAD,
    UNLOAD,
    VISIT
}

public enum PointType {
    SHOP,
    WAREHOUSE,
    GARAGE
}
```

Спецоперации
1. Средний пробег по всем маршрутам
2. Маршруты, которые начинаются и заканчиваются в заданный период времени
3. Все маршруты которые посещают заданную RetailPoint
4. Топ-N самых посещаемых RetailPoint
5. Топ-N ближайших точек к заданной
