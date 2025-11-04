package com.rendaxx.labs.mappers.api;

import com.rendaxx.labs.api.v1.model.PageDriverApiDto;
import com.rendaxx.labs.api.v1.model.PageOrderApiDto;
import com.rendaxx.labs.api.v1.model.PageRetailPointApiDto;
import com.rendaxx.labs.api.v1.model.PageRouteApiDto;
import com.rendaxx.labs.api.v1.model.PageRoutePointApiDto;
import com.rendaxx.labs.api.v1.model.PageVehicleApiDto;
import com.rendaxx.labs.dtos.DriverDto;
import com.rendaxx.labs.dtos.OrderDto;
import com.rendaxx.labs.dtos.RetailPointDto;
import com.rendaxx.labs.dtos.RouteDto;
import com.rendaxx.labs.dtos.RoutePointDto;
import com.rendaxx.labs.dtos.VehicleDto;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PageResponseMapper {

    private final OrderApiMapper orderApiMapper;
    private final DriverApiMapper driverApiMapper;
    private final VehicleApiMapper vehicleApiMapper;
    private final RetailPointApiMapper retailPointApiMapper;
    private final RoutePointApiMapper routePointApiMapper;
    private final RouteApiMapper routeApiMapper;

    public PageOrderApiDto toOrderPage(Page<OrderDto> page) {
        PageOrderApiDto dto = new PageOrderApiDto();
        dto.setContent(orderApiMapper.toApi(page.getContent()));
        applyMetadata(page,
                dto::setPage,
                dto::setSize,
                dto::setTotalElements,
                dto::setTotalPages,
                dto::setSort,
                dto::setFirst,
                dto::setLast,
                dto::setEmpty);
        return dto;
    }

    public PageDriverApiDto toDriverPage(Page<DriverDto> page) {
        PageDriverApiDto dto = new PageDriverApiDto();
        dto.setContent(driverApiMapper.toApi(page.getContent()));
        applyMetadata(page,
                dto::setPage,
                dto::setSize,
                dto::setTotalElements,
                dto::setTotalPages,
                dto::setSort,
                dto::setFirst,
                dto::setLast,
                dto::setEmpty);
        return dto;
    }

    public PageVehicleApiDto toVehiclePage(Page<VehicleDto> page) {
        PageVehicleApiDto dto = new PageVehicleApiDto();
        dto.setContent(vehicleApiMapper.toApi(page.getContent()));
        applyMetadata(page,
                dto::setPage,
                dto::setSize,
                dto::setTotalElements,
                dto::setTotalPages,
                dto::setSort,
                dto::setFirst,
                dto::setLast,
                dto::setEmpty);
        return dto;
    }

    public PageRetailPointApiDto toRetailPointPage(Page<RetailPointDto> page) {
        PageRetailPointApiDto dto = new PageRetailPointApiDto();
        dto.setContent(retailPointApiMapper.toApi(page.getContent()));
        applyMetadata(page,
                dto::setPage,
                dto::setSize,
                dto::setTotalElements,
                dto::setTotalPages,
                dto::setSort,
                dto::setFirst,
                dto::setLast,
                dto::setEmpty);
        return dto;
    }

    public PageRoutePointApiDto toRoutePointPage(Page<RoutePointDto> page) {
        PageRoutePointApiDto dto = new PageRoutePointApiDto();
        dto.setContent(routePointApiMapper.toApi(page.getContent()));
        applyMetadata(page,
                dto::setPage,
                dto::setSize,
                dto::setTotalElements,
                dto::setTotalPages,
                dto::setSort,
                dto::setFirst,
                dto::setLast,
                dto::setEmpty);
        return dto;
    }

    public PageRouteApiDto toRoutePage(Page<RouteDto> page) {
        PageRouteApiDto dto = new PageRouteApiDto();
        dto.setContent(routeApiMapper.toApi(page.getContent()));
        applyMetadata(page,
                dto::setPage,
                dto::setSize,
                dto::setTotalElements,
                dto::setTotalPages,
                dto::setSort,
                dto::setFirst,
                dto::setLast,
                dto::setEmpty);
        return dto;
    }

    private void applyMetadata(
            Page<?> source,
            Consumer<Integer> pageSetter,
            Consumer<Integer> sizeSetter,
            Consumer<Long> totalElementsSetter,
            Consumer<Integer> totalPagesSetter,
            Consumer<List<String>> sortSetter,
            Consumer<Boolean> firstSetter,
            Consumer<Boolean> lastSetter,
            Consumer<Boolean> emptySetter
    ) {
        pageSetter.accept(source.getNumber());
        sizeSetter.accept(source.getSize());
        totalElementsSetter.accept(source.getTotalElements());
        totalPagesSetter.accept(source.getTotalPages());
        sortSetter.accept(toSortStrings(source.getSort()));
        firstSetter.accept(source.isFirst());
        lastSetter.accept(source.isLast());
        emptySetter.accept(source.isEmpty());
    }

    private List<String> toSortStrings(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return List.of();
        }
        return sort.stream()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .collect(Collectors.toList());
    }
}
