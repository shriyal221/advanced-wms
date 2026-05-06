package com.infotact.wms.api;

import com.infotact.wms.api.dto.ProductCategoryRequest;
import com.infotact.wms.api.dto.ProductCategoryResponse;
import com.infotact.wms.service.ProductCategoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product-categories")
public class ProductCategoryController {
    private final ProductCategoryService productCategoryService;

    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @GetMapping
    public List<ProductCategoryResponse> list() {
        return productCategoryService.list();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProductCategoryResponse create(@Valid @RequestBody ProductCategoryRequest request) {
        return productCategoryService.create(request);
    }
}
