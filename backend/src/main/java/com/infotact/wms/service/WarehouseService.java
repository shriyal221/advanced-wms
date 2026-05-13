package com.infotact.wms.service;

import com.infotact.wms.api.dto.AisleRequest;
import com.infotact.wms.api.dto.AisleResponse;
import com.infotact.wms.api.dto.StorageBinRequest;
import com.infotact.wms.api.dto.StorageBinResponse;
import com.infotact.wms.api.dto.WarehouseRequest;
import com.infotact.wms.api.dto.WarehouseResponse;
import com.infotact.wms.api.dto.ZoneRequest;
import com.infotact.wms.api.dto.ZoneResponse;
import com.infotact.wms.domain.Aisle;
import com.infotact.wms.domain.StorageBin;
import com.infotact.wms.domain.Warehouse;
import com.infotact.wms.domain.Zone;
import com.infotact.wms.exception.DuplicateResourceException;
import com.infotact.wms.exception.ResourceNotFoundException;
import com.infotact.wms.repository.AisleRepository;
import com.infotact.wms.repository.StorageBinRepository;
import com.infotact.wms.repository.WarehouseRepository;
import com.infotact.wms.repository.ZoneRepository;
import com.infotact.wms.repository.AppUserRepository;
import com.infotact.wms.domain.AppUser;
import com.infotact.wms.domain.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final ZoneRepository zoneRepository;
    private final AisleRepository aisleRepository;
    private final StorageBinRepository storageBinRepository;
    private final AppUserRepository appUserRepository;

    public WarehouseService(
        WarehouseRepository warehouseRepository,
        ZoneRepository zoneRepository,
        AisleRepository aisleRepository,
        StorageBinRepository storageBinRepository,
        AppUserRepository appUserRepository
    ) {
        this.warehouseRepository = warehouseRepository;
        this.zoneRepository = zoneRepository;
        this.aisleRepository = aisleRepository;
        this.storageBinRepository = storageBinRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        String code = request.code().trim().toUpperCase();
        if (warehouseRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Warehouse code already exists: " + code);
        }
        Warehouse warehouse = new Warehouse(code, request.name().trim(), request.address());
        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> listWarehouses() {
        AppUser currentUser = getCurrentUser();
        return warehouseRepository.findAll(Sort.by("code"))
            .stream()
            .filter(w -> {
                if (currentUser != null && currentUser.getRole() == Role.OPERATOR && currentUser.getWarehouse() != null) {
                    return w.getId().equals(currentUser.getWarehouse().getId());
                }
                return true;
            })
            .map(WarehouseResponse::from)
            .toList();
    }

    @Transactional
    public ZoneResponse createZone(ZoneRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + request.warehouseId()));
        Zone zone = new Zone(request.code().trim().toUpperCase(), request.name().trim(), warehouse);
        return ZoneResponse.from(zoneRepository.save(zone));
    }

    @Transactional(readOnly = true)
    public List<ZoneResponse> listZones() {
        return zoneRepository.findAll(Sort.by("code"))
            .stream()
            .map(ZoneResponse::from)
            .toList();
    }

    @Transactional
    public AisleResponse createAisle(AisleRequest request) {
        Zone zone = zoneRepository.findById(request.zoneId())
            .orElseThrow(() -> new ResourceNotFoundException("Zone not found: " + request.zoneId()));
        Aisle aisle = new Aisle(request.code().trim().toUpperCase(), zone);
        return AisleResponse.from(aisleRepository.save(aisle));
    }

    @Transactional(readOnly = true)
    public List<AisleResponse> listAisles() {
        return aisleRepository.findAll(Sort.by("code"))
            .stream()
            .map(AisleResponse::from)
            .toList();
    }

    @Transactional
    public StorageBinResponse createBin(StorageBinRequest request) {
        String code = request.code().trim().toUpperCase();
        if (storageBinRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Storage bin code already exists: " + code);
        }
        Aisle aisle = aisleRepository.findById(request.aisleId())
            .orElseThrow(() -> new ResourceNotFoundException("Aisle not found: " + request.aisleId()));
        StorageBin bin = new StorageBin(code, request.capacity(), aisle);
        return StorageBinResponse.from(storageBinRepository.save(bin));
    }

    @Transactional(readOnly = true)
    public List<StorageBinResponse> listBins() {
        AppUser currentUser = getCurrentUser();
        return storageBinRepository.findAllWithLocation()
            .stream()
            .filter(bin -> {
                if (currentUser != null && currentUser.getRole() == Role.OPERATOR && currentUser.getWarehouse() != null) {
                    return bin.getAisle().getZone().getWarehouse().getId().equals(currentUser.getWarehouse().getId());
                }
                return true;
            })
            .map(StorageBinResponse::from)
            .toList();
    }

    private AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            return appUserRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
}
