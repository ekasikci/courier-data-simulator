package com.ekasikci.courierdatasimulator.kafka.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ekasikci.courierdatasimulator.kafka.entitiy.Package;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {

    /**
     * Find all non-cancelled packages
     * 
     * @return List of packages where cancelled is not 1
     */
    @Query("SELECT p FROM Package p WHERE p.cancelled = 0 OR p.cancelled IS NULL")
    List<Package> findAllNonCancelled();

    /**
     * Check if a package exists and is not cancelled
     * 
     * @param id Package ID
     * @return true if package exists and is not cancelled
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Package p " +
            "WHERE p.id = :id AND (p.cancelled = 0 OR p.cancelled IS NULL)")
    boolean existsByIdAndNotCancelled(Long id);
}