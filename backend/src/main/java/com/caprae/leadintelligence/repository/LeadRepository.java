package com.caprae.leadintelligence.repository;

import com.caprae.leadintelligence.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long>, JpaSpecificationExecutor<Lead> {

    Optional<Lead> findByCompanyNameIgnoreCase(String companyName);

    boolean existsByCompanyNameIgnoreCase(String companyName);
}
