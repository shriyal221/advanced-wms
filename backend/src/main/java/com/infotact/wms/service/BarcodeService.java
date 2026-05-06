package com.infotact.wms.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.infotact.wms.domain.Product;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BarcodeService {
    private final ProductService productService;

    public BarcodeService(ProductService productService) {
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public byte[] qrCodeForProduct(Long productId) {
        Product product = productService.getProduct(productId);
        String payload = "SKU:" + product.getSku() + "|NAME:" + product.getName();
        try {
            BitMatrix matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 280, 280);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return output.toByteArray();
        } catch (WriterException | IOException exception) {
            throw new IllegalStateException("Unable to generate QR code for product " + product.getSku(), exception);
        }
    }
}
