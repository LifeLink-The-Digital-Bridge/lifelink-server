package com.matchingservice.service;

import com.matchingservice.enums.MatchStatus;
import com.matchingservice.enums.DonationStatus;
import com.matchingservice.enums.RequestStatus;
import com.matchingservice.model.MatchResult;
import com.matchingservice.repository.MatchResultRepository;
import com.matchingservice.repository.donor.DonationRepository;
import com.matchingservice.repository.recipient.ReceiveRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchConfirmationTimeoutJob {

    private final MatchResultRepository matchResultRepository;
    private final DonationRepository donationRepository;
    private final ReceiveRequestRepository receiveRequestRepository;

    @Value("${match.confirmation.timeout.minutes:2880}")
    private int confirmationTimeoutMinutes;

    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void expireTimedOutConfirmations() {
        log.info("Running match confirmation timeout check with timeout: {} minutes", confirmationTimeoutMinutes);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeoutThreshold = now.minusMinutes(confirmationTimeoutMinutes);

        List<MatchResult> expiredMatches = matchResultRepository.findExpiredMatches(
                List.of(
                        MatchStatus.PENDING,
                        MatchStatus.DONOR_CONFIRMED,
                        MatchStatus.RECIPIENT_CONFIRMED
                ),
                timeoutThreshold
        );

        if (expiredMatches.isEmpty()) {
            log.debug("No expired matches found");
            return;
        }

        log.info("Found {} expired matches to process", expiredMatches.size());

        int expiredCount = 0;
        for (MatchResult match : expiredMatches) {
            try {
                if (match.getDonorConfirmed() && match.getRecipientConfirmed()) {
                    log.debug("Skipping match {} - both parties already confirmed", match.getId());
                    continue;
                }

                match.setStatus(MatchStatus.EXPIRED);
                match.setExpiredAt(now);

                if (match.getDonorConfirmed() && !match.getRecipientConfirmed()) {
                    match.setExpiryReason("RECIPIENT_DID_NOT_CONFIRM_WITHIN_" + confirmationTimeoutMinutes + "_MINUTES");
                    log.info("Match {} expired: Recipient did not confirm within {} minutes",
                            match.getId(), confirmationTimeoutMinutes);
                } else if (match.getRecipientConfirmed() && !match.getDonorConfirmed()) {
                    match.setExpiryReason("DONOR_DID_NOT_CONFIRM_WITHIN_" + confirmationTimeoutMinutes + "_MINUTES");
                    log.info("Match {} expired: Donor did not confirm within {} minutes",
                            match.getId(), confirmationTimeoutMinutes);
                } else {
                    match.setExpiryReason("BOTH_PARTIES_DID_NOT_CONFIRM_WITHIN_" + confirmationTimeoutMinutes + "_MINUTES");
                    log.info("Match {} expired: Neither party confirmed within {} minutes",
                            match.getId(), confirmationTimeoutMinutes);
                }

                matchResultRepository.save(match);
                expiredCount++;

                checkAndResetToPending(match);

            } catch (Exception e) {
                log.error("Failed to expire match {}: {}", match.getId(), e.getMessage(), e);
            }
        }

        log.info("Successfully expired {} matches", expiredCount);
    }

    private void checkAndResetToPending(MatchResult expiredMatch) {
        List<MatchResult> donationMatches = matchResultRepository.findByDonationIdAndStatusIn(
                expiredMatch.getDonationId(),
                List.of(
                        MatchStatus.PENDING,
                        MatchStatus.DONOR_CONFIRMED,
                        MatchStatus.RECIPIENT_CONFIRMED,
                        MatchStatus.CONFIRMED
                )
        );

        if (donationMatches.isEmpty()) {
            donationRepository.findById(expiredMatch.getDonationId()).ifPresent(donation -> {
                if (donation.getStatus() == DonationStatus.MATCHED) {
                    donation.setStatus(DonationStatus.PENDING);
                    donationRepository.save(donation);
                    log.info("Reset donation {} status to PENDING (was MATCHED)", expiredMatch.getDonationId());
                }
            });
        }

        List<MatchResult> requestMatches = matchResultRepository.findByReceiveRequestIdAndStatusIn(
                expiredMatch.getReceiveRequestId(),
                List.of(
                        MatchStatus.PENDING,
                        MatchStatus.DONOR_CONFIRMED,
                        MatchStatus.RECIPIENT_CONFIRMED,
                        MatchStatus.CONFIRMED
                )
        );

        if (requestMatches.isEmpty()) {
            receiveRequestRepository.findById(expiredMatch.getReceiveRequestId()).ifPresent(request -> {
                if (request.getStatus() == RequestStatus.MATCHED) {
                    request.setStatus(RequestStatus.PENDING);
                    receiveRequestRepository.save(request);
                    log.info("Reset receive request {} status to PENDING (was MATCHED)", expiredMatch.getReceiveRequestId());
                }
            });
        }
    }
}
