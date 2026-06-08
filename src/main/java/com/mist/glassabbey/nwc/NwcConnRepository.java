package com.mist.glassabbey.nwc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NwcConnRepository extends JpaRepository<NwcConn, UUID> {
    @Query("""
    SELECT n FROM NwcConn n
    WHERE n.creator.id = :creatorId
    AND n.isPrimary = true
    AND n.isActive = true
    """)
    Optional<NwcConn> findPrimaryByGalleryCreatorId(@Param("creatorId") UUID creatorId);

    // demote all primary connections for a creator
    @Modifying
    @Query("UPDATE NwcConn n SET n.isPrimary = false WHERE n.creator.id = :creatorId")
    void demotePrimary(@Param("creatorId") UUID creatorId);

    // find by id + creator ownership check
    Optional<NwcConn> findByIdAndCreatorId(UUID id, UUID creatorId);

    // list all for creator
    List<NwcConn> findByCreatorIdOrderByCreatedAtDesc(UUID creatorId);
}
