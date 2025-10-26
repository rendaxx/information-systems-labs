
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

Спецоперации
1. Средний пробег по всем маршрутам
2. Маршруты, которые начинаются и заканчиваются в заданный период времени
3. Все маршруты которые посещают заданную RetailPoint
4. Топ-N самых посещаемых RetailPoint
5. Топ-N ближайших точек к заданной