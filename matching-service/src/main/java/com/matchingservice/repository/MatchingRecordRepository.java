package com.matchingservice.repository;

import com.matchingservice.model.donor.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchingRecordRepository extends JpaRepository<MatchResult, Long> {

}