package com.rendaxx.labs.controller.support;

import com.rendaxx.labs.domain.OperationType;
import com.rendaxx.labs.domain.Order;
import com.rendaxx.labs.domain.PointType;
import com.rendaxx.labs.domain.RetailPoint;
import com.rendaxx.labs.domain.Route;
import com.rendaxx.labs.domain.RoutePoint;
import com.rendaxx.labs.domain.Vehicle;
import com.rendaxx.labs.repository.OrderRepository;
import com.rendaxx.labs.repository.RetailPointRepository;
import com.rendaxx.labs.repository.RouteRepository;
import com.rendaxx.labs.repository.VehicleRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class RouteTestDataFactory {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private final RouteRepository routeRepository;
    private final VehicleRepository vehicleRepository;
    private final RetailPointRepository retailPointRepository;
    private final OrderRepository orderRepository;

    public RouteTestDataFactory(
        RouteRepository routeRepository,
        VehicleRepository vehicleRepository,
        RetailPointRepository retailPointRepository,
        OrderRepository orderRepository
    ) {
        this.routeRepository = routeRepository;
        this.vehicleRepository = vehicleRepository;
        this.retailPointRepository = retailPointRepository;
        this.orderRepository = orderRepository;
    }

    public Route persistRoute(LocalDateTime plannedStart, LocalDateTime plannedEnd, BigDecimal mileage) {
        Vehicle vehicle = vehicleRepository.save(buildVehicle());
        RetailPoint retailPoint = retailPointRepository.save(buildRetailPoint());
        Order order = buildOrder();

        Route route = Route.builder()
            .vehicle(vehicle)
            .creationTime(plannedStart.minusHours(1))
            .plannedStartTime(plannedStart)
            .plannedEndTime(plannedEnd)
            .mileageInKm(mileage)
            .build();

        RoutePoint routePoint = RoutePoint.builder()
            .route(route)
            .retailPoint(retailPoint)
            .operationType(OperationType.LOAD)
            .plannedStartTime(plannedStart)
            .plannedEndTime(plannedEnd)
            .orderNumber(0)
            .build();
        routePoint.getOrders().add(order);
        route.getRoutePoints().add(routePoint);

        return routeRepository.save(route);
    }

    public void cleanDatabase() {
        routeRepository.deleteAll();
        orderRepository.deleteAll();
        retailPointRepository.deleteAll();
        vehicleRepository.deleteAll();
    }

    private Vehicle buildVehicle() {
        return Vehicle.builder()
            .gosNumber("GOS-" + UUID.randomUUID())
            .tonnageInTons(new BigDecimal("5.00"))
            .bodyHeightInMeters(new BigDecimal("2.50"))
            .bodyWidthInMeters(new BigDecimal("2.00"))
            .bodyLengthInCubicMeters(new BigDecimal("12.00"))
            .build();
    }

    private RetailPoint buildRetailPoint() {
        return RetailPoint.builder()
            .name("Retail-" + UUID.randomUUID())
            .address("Address-" + UUID.randomUUID())
            .location(createPoint(37.617494, 55.755825))
            .type(PointType.SHOP)
            .timezone("UTC")
            .build();
    }

    private Order buildOrder() {
        return Order.builder()
            .goodsType("PERISHABLE")
            .minTemperature(2)
            .maxTemperature(6)
            .volumeInCubicMeters(new BigDecimal("1.500"))
            .weightInKg(new BigDecimal("10.000"))
            .build();
    }

    private Point createPoint(double longitude, double latitude) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }
}
