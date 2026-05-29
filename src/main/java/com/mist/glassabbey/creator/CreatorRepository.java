package com.mist.glassabbey.creator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreatorRepository extends JpaRepository<Creator, UUID> {
    Optional<Creator> findByPubkey(String pubkey);

    @Query("""
        select distinct c
        from Creator c
        left join fetch c.galleries g
        where c.id = :id
    """)
    Optional<Creator> findCreatorById(UUID id);
}
