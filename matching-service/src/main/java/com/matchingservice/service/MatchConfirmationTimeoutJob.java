package com.matchingservice.service;

import com.matchingservice.enums.MatchStatus;
import com.matchingservice.model.MatchResult;
import com.matchingservice.repository.MatchResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchConfirmationTimeoutJob {

    private final MatchResultRepository matchResultRepository;

    /**
     * Run every 15 minutes to check for expired confirmations
     */
    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void expireTimedOutConfirmations() {
        LocalDateTime now = LocalDateTime.now();

        System.out.println("========================================");
        System.out.println("Running confirmation timeout check at: " + now);

        List<MatchResult> matches = matchResultRepository.findAll();

        int expiredCount = 0;
        for (MatchResult match : matches) {
            if (match.getStatus() != MatchStatus.DONOR_CONFIRMED &&
                    match.getStatus() != MatchStatus.RECIPIENT_CONFIRMED) {
                continue;
            }

            if (match.getConfirmationExpiresAt() != null &&
                    now.isAfter(match.getConfirmationExpiresAt())) {

                match.setStatus(MatchStatus.EXPIRED);
                match.setExpiredAt(now);

                if (match.getDonorConfirmed() && !match.getRecipientConfirmed()) {
                    match.setExpiryReason("RECIPIENT_DID_NOT_CONFIRM_WITHIN_48_HOURS");
                    System.out.println("Expired match " + match.getId() +
                            " - Recipient did not confirm");
                } else if (match.getRecipientConfirmed() && !match.getDonorConfirmed()) {
                    match.setExpiryReason("DONOR_DID_NOT_CONFIRM_WITHIN_48_HOURS");
                    System.out.println("Expired match " + match.getId() +
                            " - Donor did not confirm");
                }

                matchResultRepository.save(match);
                expiredCount++;
            }
        }

        System.out.println("Expired " + expiredCount + " matches due to confirmation timeout");
        System.out.println("========================================");
    }
}
