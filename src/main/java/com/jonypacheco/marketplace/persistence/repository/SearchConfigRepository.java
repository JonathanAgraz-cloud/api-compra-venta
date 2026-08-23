package com.jonypacheco.marketplace.persistence.repository;

import com.jonypacheco.marketplace.persistence.domain.entity.SearchConfig;
import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchConfigRepository extends JpaRepository<SearchConfig, Long> {

    List<SearchConfig> findByActivoTrue();

    List<SearchConfig> findByZonaAndActivoTrue(ZonaMerida zona);
}
