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

    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void expireTimedOutConfirmations() {
        LocalDateTime now = LocalDateTime.now();

        List<MatchResult> expiredMatches = matchResultRepository
                .findExpiredPartialConfirmations(now);

        if (expiredMatches.isEmpty()) {
            return;
        }

        int expiredCount = 0;
        for (MatchResult match : expiredMatches) {
            try {
                match.setStatus(MatchStatus.EXPIRED);
                match.setExpiredAt(now);

                if (match.getDonorConfirmed() && !match.getRecipientConfirmed()) {
                    match.setExpiryReason("RECIPIENT_DID_NOT_CONFIRM_WITHIN_48_HOURS");
                } else if (match.getRecipientConfirmed() && !match.getDonorConfirmed()) {
                    match.setExpiryReason("DONOR_DID_NOT_CONFIRM_WITHIN_48_HOURS");
                }

                matchResultRepository.save(match);
                expiredCount++;

                checkAndResetToPending(match);

            } catch (Exception e) {
                log.error("Failed to expire match {}: {}", match.getId(), e.getMessage());
            }
        }
    }

    private void checkAndResetToPending(MatchResult expiredMatch) {
        List<MatchResult> donationMatches = matchResultRepository
                .findByDonationIdAndStatusIn(
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
                }
            });
        }

        List<MatchResult> requestMatches = matchResultRepository
                .findByReceiveRequestIdAndStatusIn(
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
                }
            });
        }
    }
}
