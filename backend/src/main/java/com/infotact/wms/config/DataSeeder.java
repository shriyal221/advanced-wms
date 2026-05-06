package com.infotact.wms.config;

import com.infotact.wms.domain.Aisle;
import com.infotact.wms.domain.AppUser;
import com.infotact.wms.domain.Product;
import com.infotact.wms.domain.ProductCategory;
import com.infotact.wms.domain.Role;
import com.infotact.wms.domain.StorageBin;
import com.infotact.wms.domain.Supplier;
import com.infotact.wms.domain.Warehouse;
import com.infotact.wms.domain.Zone;
import com.infotact.wms.repository.AisleRepository;
import com.infotact.wms.repository.AppUserRepository;
import com.infotact.wms.repository.ProductCategoryRepository;
import com.infotact.wms.repository.ProductRepository;
import com.infotact.wms.repository.StorageBinRepository;
import com.infotact.wms.repository.SupplierRepository;
import com.infotact.wms.repository.WarehouseRepository;
import com.infotact.wms.repository.ZoneRepository;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(
        AppUserRepository userRepository,
        WarehouseRepository warehouseRepository,
        ZoneRepository zoneRepository,
        AisleRepository aisleRepository,
        StorageBinRepository storageBinRepository,
        ProductRepository productRepository,
        ProductCategoryRepository productCategoryRepository,
        SupplierRepository supplierRepository,
        PasswordEncoder passwordEncoder,
        @Value("${seed.admin-password}") String adminPassword,
        @Value("${seed.operator-password}") String operatorPassword
    ) {
        return args -> {
            Warehouse warehouse;
            Zone zone;
            if (!warehouseRepository.existsByCode("BLR-01")) {
                warehouse = warehouseRepository.save(new Warehouse("BLR-01", "Bengaluru Fulfillment Hub", "Peenya Industrial Area, Bengaluru"));
                zone = zoneRepository.save(new Zone("AMBIENT", "Ambient Storage", warehouse));
                Aisle aisleA = aisleRepository.save(new Aisle("A-01", zone));
                Aisle aisleB = aisleRepository.save(new Aisle("A-02", zone));
                storageBinRepository.save(new StorageBin("BIN-A01-001", 500, aisleA));
                storageBinRepository.save(new StorageBin("BIN-A01-002", 350, aisleA));
                storageBinRepository.save(new StorageBin("BIN-A02-001", 250, aisleB));
            } else {
                warehouse = warehouseRepository.findByCode("BLR-01").orElseThrow();
                zone = zoneRepository.findFirstByCode("AMBIENT").orElse(null);
            }

            String[][] extraWarehouses = {
                {"MUM-01", "Mumbai Logistics Center", "Andheri East, Mumbai"},
                {"DEL-01", "Delhi Distribution Node", "Okhla Industrial Estate, New Delhi"},
                {"CHE-01", "Chennai Cargo Hub", "Guindy, Chennai"},
                {"HYD-01", "Hyderabad Warehouse", "HITEC City, Hyderabad"},
                {"PUN-01", "Pune Storage Facility", "Hinjawadi, Pune"},
                {"KOL-01", "Kolkata Transit Point", "Salt Lake City, Kolkata"},
                {"AMD-01", "Ahmedabad Fulfillment", "Sanand, Ahmedabad"},
                {"JAI-01", "Jaipur Depot", "Sitapura Industrial Area, Jaipur"},
                {"LKO-01", "Lucknow Base", "Gomti Nagar, Lucknow"}
            };
            for (int i = 0; i < extraWarehouses.length; i++) {
                String[] wh = extraWarehouses[i];
                if (!warehouseRepository.existsByCode(wh[0])) {
                    Warehouse w = warehouseRepository.save(new Warehouse(wh[0], wh[1], wh[2]));
                    Zone z = zoneRepository.save(new Zone("MAIN-" + wh[0], "Main Zone", w));
                    Aisle a = aisleRepository.save(new Aisle("A-" + wh[0], z));
                    storageBinRepository.save(new StorageBin("BIN-" + wh[0], 500 + (i * 50), a));
                }
            }

            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(new AppUser("admin", passwordEncoder.encode(adminPassword), Role.ADMIN, "Warehouse Admin", "admin@infotact.local", "+91 90000 00001", warehouse));
            }
            if (!userRepository.existsByUsername("operator")) {
                userRepository.save(new AppUser("operator", passwordEncoder.encode(operatorPassword), Role.OPERATOR, "Floor Operator", "operator@infotact.local", "+91 90000 00002", warehouse));
            }

            ProductCategory suppliesCategory = null;
            if (!productCategoryRepository.existsByNameIgnoreCaseAndWarehouseId("Packing Supplies", warehouse.getId())) {
                suppliesCategory = productCategoryRepository.save(new ProductCategory("Packing Supplies", warehouse, null, zone));
            } else {
                suppliesCategory = productCategoryRepository.findAllWithDetails()
                    .stream()
                    .filter(category -> category.getName().equalsIgnoreCase("Packing Supplies") && category.getWarehouse().getId().equals(warehouse.getId()))
                    .findFirst()
                    .orElse(null);
            }
            if (!productRepository.existsBySku("SKU-LABEL-100")) {
                productRepository.save(new Product(
                    "SKU-LABEL-100",
                    "Thermal Shipping Labels",
                    "BC-LABEL-100",
                    "4x6 adhesive shipping labels for outbound packing.",
                    1,
                    BigDecimal.valueOf(100),
                    BigDecimal.valueOf(299),
                    0.1,
                    suppliesCategory,
                    warehouse
                ));
            }
            if (!productRepository.existsBySku("SKU-SCANNER-200")) {
                productRepository.save(new Product(
                    "SKU-SCANNER-200",
                    "Handheld Barcode Scanner",
                    "BC-SCANNER-200",
                    "Wireless barcode scanner used by floor operators.",
                    5,
                    BigDecimal.valueOf(20),
                    BigDecimal.valueOf(4999),
                    0.6,
                    suppliesCategory,
                    warehouse
                ));
            }
            if (!supplierRepository.existsByNameIgnoreCase("Infotact Supply Co.")) {
                supplierRepository.save(new Supplier("Infotact Supply Co.", "Bengaluru, Karnataka", "supply@infotact.local", "+91 90000 00003"));
            }
        };
    }
}
