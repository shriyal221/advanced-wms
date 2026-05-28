package com.infotact.wms.config;

import com.infotact.wms.domain.Aisle;
import com.infotact.wms.domain.AppUser;
import com.infotact.wms.domain.Product;
import com.infotact.wms.domain.ProductCategory;
import com.infotact.wms.domain.PurchaseOrder;
import com.infotact.wms.domain.PurchaseOrderItem;
import com.infotact.wms.domain.Role;
import com.infotact.wms.domain.StorageBin;
import com.infotact.wms.domain.Supplier;
import com.infotact.wms.domain.Warehouse;
import com.infotact.wms.domain.Zone;
import com.infotact.wms.repository.AisleRepository;
import com.infotact.wms.repository.AppUserRepository;
import com.infotact.wms.repository.ProductCategoryRepository;
import com.infotact.wms.repository.ProductRepository;
import com.infotact.wms.repository.PurchaseOrderRepository;
import com.infotact.wms.repository.StorageBinRepository;
import com.infotact.wms.repository.SupplierRepository;
import com.infotact.wms.repository.WarehouseRepository;
import com.infotact.wms.repository.ZoneRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
        PurchaseOrderRepository purchaseOrderRepository,
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

            AppUser adminUser = userRepository.findByUsername("admin").orElse(null);
            if (adminUser == null) {
                userRepository.save(new AppUser("admin", passwordEncoder.encode(adminPassword), Role.ADMIN, "Warehouse Admin", "admin@infotact.local", "+91 90000 00001", warehouse));
            } else {
                adminUser.updateProfile("Warehouse Admin", "admin@infotact.local", "+91 90000 00001", Role.ADMIN, "ACTIVE", warehouse);
                adminUser.changePassword(passwordEncoder.encode(adminPassword));
                userRepository.save(adminUser);
            }

            AppUser operatorUser = userRepository.findByUsername("operator").orElse(null);
            if (operatorUser == null) {
                userRepository.save(new AppUser("operator", passwordEncoder.encode(operatorPassword), Role.OPERATOR, "Floor Operator", "operator@infotact.local", "+91 90000 00002", warehouse));
            } else {
                operatorUser.updateProfile("Floor Operator", "operator@infotact.local", "+91 90000 00002", Role.OPERATOR, "ACTIVE", warehouse);
                operatorUser.changePassword(passwordEncoder.encode(operatorPassword));
                userRepository.save(operatorUser);
            }

            AppUser shriyalUser = userRepository.findByUsername("shriyal").orElse(null);
            if (shriyalUser == null) {
                userRepository.save(new AppUser("shriyal", passwordEncoder.encode(adminPassword), Role.ADMIN, "Shriyal Admin", "shriyal@infotact.local", "+91 90000 00005", warehouse));
            } else {
                shriyalUser.updateProfile("Shriyal Admin", "shriyal@infotact.local", "+91 90000 00005", Role.ADMIN, "ACTIVE", warehouse);
                shriyalUser.changePassword(passwordEncoder.encode(adminPassword));
                userRepository.save(shriyalUser);
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

            // Add 20 Professional Products
            String[][] professionalProducts = {
                {"SKU-TK-100", "Industrial Tool Kit", "BC-TK-100", "Professional grade tool kit for industrial maintenance and repair operations.", "5", "10", "12500", "15.5", "Industrial Tools"},
                {"SKU-PR-500", "Heavy Duty Pallet Rack", "BC-PR-500", "High-capacity steel storage rack designed for heavy industrial pallets.", "20", "5", "28500", "120.0", "Warehouse Equipment"},
                {"SKU-EF-1000", "Electric Forklift", "BC-EF-1000", "Advanced electric forklift with 3000kg lifting capacity and fast charging.", "100", "2", "1250000", "3200.0", "Material Handling"},
                {"SKU-SG-200", "Safety Goggles Pro", "BC-SG-200", "Anti-fog, scratch-resistant professional safety goggles for floor workers.", "1", "50", "850", "0.25", "Safety Gear"},
                {"SKU-AC-50", "Industrial Air Compressor", "BC-AC-50", "Powerful 50L air compressor for pneumatic tools and machinery.", "15", "5", "42000", "35.0", "Industrial Tools"},
                {"SKU-HPT-25", "Hydraulic Pallet Jack", "BC-HPT-25", "Heavy-duty hydraulic hand pallet truck with 2500kg capacity.", "40", "8", "22000", "85.0", "Material Handling"},
                {"SKU-SB-100", "Safety Barrier Guard", "BC-SB-100", "High-visibility steel safety barrier for protecting personnel and equipment.", "25", "10", "9500", "25.0", "Safety Gear"},
                {"SKU-DWS-300", "Digital Floor Scale", "BC-DWS-300", "Industrial digital floor scale with 300kg capacity and precision sensors.", "12", "4", "15500", "18.0", "Warehouse Equipment"},
                {"SKU-IVF-24", "High-Velocity Fan", "BC-IVF-24", "24-inch industrial-grade high-velocity fan for warehouse ventilation.", "8", "10", "8900", "14.0", "Warehouse Equipment"},
                {"SKU-FE-009", "ABC Fire Extinguisher", "BC-FE-009", "Professional 9kg ABC dry powder fire extinguisher for industrial safety.", "6", "20", "4200", "16.0", "Safety Gear"},
                {"SKU-WH-HM-01", "Hard Hat Pro", "BC-HM-01", "High-impact resistant hard hat with adjustable suspension.", "1", "100", "1200", "0.4", "Safety Gear"},
                {"SKU-WH-GV-02", "Heavy Duty Gloves", "BC-GV-02", "Cut-resistant safety gloves for material handling.", "1", "200", "450", "0.1", "Safety Gear"},
                {"SKU-WH-BW-03", "Bubble Wrap Roll", "BC-BW-03", "100m x 1m industrial bubble wrap for packaging fragile items.", "5", "50", "1800", "2.5", "Packing Supplies"},
                {"SKU-WH-ST-04", "Stretch Film", "BC-ST-04", "500mm x 300m clear stretch film for pallet wrapping.", "2", "150", "650", "1.8", "Packing Supplies"},
                {"SKU-WH-CT-05", "Corrugated Boxes Large", "BC-CT-05", "Double-wall corrugated shipping boxes 24x24x24 inches.", "5", "500", "120", "0.5", "Packing Supplies"},
                {"SKU-WH-PT-06", "Packaging Tape", "BC-PT-06", "Strong adhesive clear packaging tape, 2 inches x 50m.", "1", "1000", "45", "0.2", "Packing Supplies"},
                {"SKU-WH-TD-07", "Tape Dispenser Gun", "BC-TD-07", "Ergonomic handheld tape dispenser for 2-inch packaging tape.", "2", "30", "350", "0.5", "Warehouse Equipment"},
                {"SKU-WH-LD-08", "Loading Dock Bumper", "BC-LD-08", "Heavy-duty rubber bumper for loading docks.", "10", "20", "4500", "15.0", "Warehouse Equipment"},
                {"SKU-WH-MS-09", "Magnetic Sweeper", "BC-MS-09", "24-inch push-type magnetic sweeper for collecting metal debris.", "8", "5", "3200", "12.0", "Warehouse Equipment"},
                {"SKU-WH-FL-10", "LED Flood Light", "BC-FL-10", "100W industrial LED flood light for warehouse illumination.", "3", "40", "2800", "2.2", "Warehouse Equipment"}
            };

            for (String[] pData : professionalProducts) {
                if (!productRepository.existsBySku(pData[0])) {
                    final String catName = pData[8];
                    ProductCategory category = productCategoryRepository.findAllWithDetails()
                        .stream()
                        .filter(c -> c.getName().equalsIgnoreCase(catName) && c.getWarehouse().getId().equals(warehouse.getId()))
                        .findFirst()
                        .orElseGet(() -> productCategoryRepository.save(new ProductCategory(catName, warehouse, null, zone)));
                    
                    productRepository.save(new Product(
                        pData[0], pData[1], pData[2], pData[3],
                        Integer.parseInt(pData[4]),
                        new BigDecimal(pData[5]),
                        new BigDecimal(pData[6]),
                        Double.parseDouble(pData[7]),
                        category, warehouse
                    ));
                }
            }
            if (!supplierRepository.existsByNameIgnoreCase("Infotact Supply Co.")) {
                supplierRepository.save(new Supplier("Infotact Supply Co.", "Bengaluru, Karnataka", "supply@infotact.local", "+91 90000 00003"));
            }

            if (purchaseOrderRepository.count() == 0) {
                Supplier supplier = supplierRepository.findAll().get(0);
                Product labels = productRepository.findBySku("SKU-LABEL-100").orElseThrow();
                Product scanners = productRepository.findBySku("SKU-SCANNER-200").orElseThrow();

                // Order 1: Ordered
                PurchaseOrder po1 = new PurchaseOrder(supplier, warehouse, Instant.now().plus(7, ChronoUnit.DAYS));
                po1.addItem(new PurchaseOrderItem(labels, 500));
                po1.addItem(new PurchaseOrderItem(scanners, 10));
                purchaseOrderRepository.save(po1);

                // Order 2: Received
                PurchaseOrder po2 = new PurchaseOrder(supplier, warehouse, Instant.now().minus(2, ChronoUnit.DAYS));
                po2.addItem(new PurchaseOrderItem(labels, 1000));
                po2.markReceived();
                purchaseOrderRepository.save(po2);
            }
        };
    }
}
