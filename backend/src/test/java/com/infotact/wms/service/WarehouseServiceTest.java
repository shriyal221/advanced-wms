package com.infotact.wms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.infotact.wms.api.dto.AisleRequest;
import com.infotact.wms.api.dto.AisleResponse;
import com.infotact.wms.api.dto.StorageBinRequest;
import com.infotact.wms.api.dto.StorageBinResponse;
import com.infotact.wms.api.dto.WarehouseRequest;
import com.infotact.wms.api.dto.WarehouseResponse;
import com.infotact.wms.api.dto.ZoneRequest;
import com.infotact.wms.api.dto.ZoneResponse;
import com.infotact.wms.exception.DuplicateResourceException;
import com.infotact.wms.exception.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WarehouseServiceTest {

    @Autowired
    private WarehouseService warehouseService;

    @Test
    void createWarehouse_Success() {
        WarehouseRequest request = new WarehouseRequest("TEST-WH-1", "Test Warehouse 1", "123 Test Ave");
        WarehouseResponse response = warehouseService.createWarehouse(request);
        
        assertThat(response).isNotNull();
        assertThat(response.code()).isEqualTo("TEST-WH-1");
        assertThat(response.name()).isEqualTo("Test Warehouse 1");
    }

    @Test
    void createWarehouse_DuplicateCode_ThrowsException() {
        WarehouseRequest request = new WarehouseRequest("TEST-WH-2", "Test 2", "Address");
        warehouseService.createWarehouse(request);

        assertThatThrownBy(() -> warehouseService.createWarehouse(request))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void listWarehouses_ReturnsAll() {
        warehouseService.createWarehouse(new WarehouseRequest("TEST-WH-3A", "WH A", "Addr A"));
        warehouseService.createWarehouse(new WarehouseRequest("TEST-WH-3B", "WH B", "Addr B"));

        List<WarehouseResponse> list = warehouseService.listWarehouses();
        assertThat(list).isNotEmpty();
        assertThat(list).extracting(WarehouseResponse::code)
            .contains("TEST-WH-3A", "TEST-WH-3B");
    }

    @Test
    void createZone_Success() {
        WarehouseResponse wh = warehouseService.createWarehouse(new WarehouseRequest("TEST-WH-4", "Name", "Addr"));
        ZoneRequest request = new ZoneRequest(wh.id(), "ZONE-A", "Zone A");
        
        ZoneResponse response = warehouseService.createZone(request);
        assertThat(response.code()).isEqualTo("ZONE-A");
        assertThat(response.warehouseId()).isEqualTo(wh.id());
    }

    @Test
    void createZone_WarehouseNotFound_ThrowsException() {
        ZoneRequest request = new ZoneRequest(99999L, "ZONE-B", "Zone B");
        
        assertThatThrownBy(() -> warehouseService.createZone(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Warehouse not found");
    }

    @Test
    void listZones_ReturnsAll() {
        WarehouseResponse wh = warehouseService.createWarehouse(new WarehouseRequest("TEST-WH-5", "Name", "Addr"));
        warehouseService.createZone(new ZoneRequest(wh.id(), "ZONE-C1", "C1"));
        warehouseService.createZone(new ZoneRequest(wh.id(), "ZONE-C2", "C2"));

        List<ZoneResponse> list = warehouseService.listZones();
        assertThat(list).extracting(ZoneResponse::code).contains("ZONE-C1", "ZONE-C2");
    }

    @Test
    void createAisle_Success() {
        WarehouseResponse wh = warehouseService.createWarehouse(new WarehouseRequest("TEST-WH-6", "Name", "Addr"));
        ZoneResponse zone = warehouseService.createZone(new ZoneRequest(wh.id(), "ZONE-D", "D"));
        
        AisleRequest request = new AisleRequest(zone.id(), "AISLE-1");
        AisleResponse response = warehouseService.createAisle(request);
        
        assertThat(response.code()).isEqualTo("AISLE-1");
        assertThat(response.zoneId()).isEqualTo(zone.id());
    }

    @Test
    void createAisle_ZoneNotFound_ThrowsException() {
        AisleRequest request = new AisleRequest(99999L, "AISLE-2");
        
        assertThatThrownBy(() -> warehouseService.createAisle(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Zone not found");
    }

    @Test
    void createBin_Success() {
        WarehouseResponse wh = warehouseService.createWarehouse(new WarehouseRequest("TEST-WH-7", "Name", "Addr"));
        ZoneResponse zone = warehouseService.createZone(new ZoneRequest(wh.id(), "ZONE-E", "E"));
        AisleResponse aisle = warehouseService.createAisle(new AisleRequest(zone.id(), "AISLE-3"));
        
        StorageBinRequest request = new StorageBinRequest(aisle.id(), "BIN-1", 100);
        StorageBinResponse response = warehouseService.createBin(request);
        
        assertThat(response.code()).isEqualTo("BIN-1");
        assertThat(response.capacity()).isEqualTo(100);
    }

    @Test
    void createBin_DuplicateCode_ThrowsException() {
        WarehouseResponse wh = warehouseService.createWarehouse(new WarehouseRequest("TEST-WH-8", "Name", "Addr"));
        ZoneResponse zone = warehouseService.createZone(new ZoneRequest(wh.id(), "ZONE-F", "F"));
        AisleResponse aisle = warehouseService.createAisle(new AisleRequest(zone.id(), "AISLE-4"));
        
        StorageBinRequest request = new StorageBinRequest(aisle.id(), "BIN-DUPE", 100);
        warehouseService.createBin(request);
        
        assertThatThrownBy(() -> warehouseService.createBin(request))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("already exists");
    }
}
