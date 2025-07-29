package com.matchingservice.repository;

import com.matchingservice.enums.MatchStatus;
import com.matchingservice.model.MatchingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchingRecordRepository extends JpaRepository<Long, MatchingRecord> {

}