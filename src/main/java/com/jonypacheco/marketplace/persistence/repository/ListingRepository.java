package com.jonypacheco.marketplace.persistence.repository;

import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.domain.enums.ListingStatus;
import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    Optional<Listing> findByFacebookId(String facebookId);

    boolean existsByFacebookId(String facebookId);

    List<Listing> findByZonaAndEstado(ZonaMerida zona, ListingStatus estado);
}
