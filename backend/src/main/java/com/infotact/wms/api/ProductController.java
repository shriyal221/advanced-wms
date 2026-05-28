package com.infotact.wms.api;

import com.infotact.wms.api.dto.ProductRequest;
import com.infotact.wms.api.dto.ProductResponse;
import com.infotact.wms.service.BarcodeService;
import com.infotact.wms.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final BarcodeService barcodeService;

    public ProductController(ProductService productService, BarcodeService barcodeService) {
        this.productService = productService;
        this.barcodeService = barcodeService;
    }

    @GetMapping
    public List<ProductResponse> list() {
        return productService.list();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @GetMapping(value = "/{id}/barcode", produces = MediaType.IMAGE_PNG_VALUE)
    @SuppressWarnings("null")
    public ResponseEntity<byte[]> barcode(@PathVariable Long id) {
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(barcodeService.qrCodeForProduct(id));
    }
}
