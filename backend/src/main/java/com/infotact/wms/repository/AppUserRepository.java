package com.infotact.wms.repository;

import com.infotact.wms.domain.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    @Query("""
        select u
        from AppUser u
        left join fetch u.warehouse
        where u.username = :username
        """)
    Optional<AppUser> findByUsernameWithWarehouse(@Param("username") String username);

    boolean existsByUsername(String username);

    Optional<AppUser> findByEmailIgnoreCase(String email);
}
