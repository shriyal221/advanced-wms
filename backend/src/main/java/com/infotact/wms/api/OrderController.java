package com.infotact.wms.api;

import com.infotact.wms.api.dto.OrderCreateRequest;
import com.infotact.wms.api.dto.OrderResponse;
import com.infotact.wms.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> list() {
        return orderService.list();
    }

    @PostMapping
    public OrderResponse create(@Valid @RequestBody OrderCreateRequest request) {
        return orderService.create(request);
    }

    @PostMapping("/{id}/start-picking")
    public OrderResponse startPicking(@PathVariable Long id) {
        return orderService.startPicking(id);
    }

    @PostMapping("/{id}/pack")
    public OrderResponse pack(@PathVariable Long id) {
        return orderService.pack(id);
    }

    @PostMapping("/{id}/ship")
    public OrderResponse ship(@PathVariable Long id) {
        return orderService.ship(id);
    }
}
