package com.notification.service;

import com.notification.kafka.event.donor_events.*;
import com.notification.kafka.event.recipient_events.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendDonationCreatedEmail(String toEmail, String userName, DonationEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Donation Registered Successfully - LifeLink");
            helper.setFrom("lifelink590@gmail.com");

            String htmlContent = buildDonationCreatedEmailTemplate(userName, event);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Donation created email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send donation created email to: {}", toEmail, e);
        }
    }

    public void sendRequestCreatedEmail(String toEmail, String userName, ReceiveRequestEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Request Registered Successfully - LifeLink");
            helper.setFrom("lifelink590@gmail.com");

            String htmlContent = buildRequestCreatedEmailTemplate(userName, event);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Request created email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send request created email to: {}", toEmail, e);
        }
    }

    public void sendDonationCancelledEmail(String toEmail, String userName, DonationCancelledEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Donation Cancelled - LifeLink");
            helper.setFrom("lifelink590@gmail.com");

            String htmlContent = buildDonationCancelledEmailTemplate(userName, event);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Donation cancelled email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send donation cancelled email to: {}", toEmail, e);
        }
    }

    public void sendRequestCancelledEmail(String toEmail, String userName, RequestCancelledEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Request Cancelled - LifeLink");
            helper.setFrom("lifelink590@gmail.com");

            String htmlContent = buildRequestCancelledEmailTemplate(userName, event);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Request cancelled email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send request cancelled email to: {}", toEmail, e);
        }
    }

    private String buildDonationCreatedEmailTemplate(String userName, DonationEvent event) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .info-box { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #667eea; border-radius: 5px; }
                        .footer { text-align: center; margin-top: 20px; color: #888; font-size: 12px; }
                        h1 { margin: 0; font-size: 28px; }
                        .label { font-weight: bold; color: #667eea; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 Donation Registered!</h1>
                        </div>
                        <div class="content">
                            <p>Dear <strong>%s</strong>,</p>
                            <p>Thank you for your generous contribution! Your donation has been successfully registered in our system.</p>
                            
                            <div class="info-box">
                                <p><span class="label">Donation ID:</span> %s</p>
                                <p><span class="label">Donation Type:</span> %s</p>
                                <p><span class="label">Date:</span> %s</p>
                                <p><span class="label">Status:</span> %s</p>
                            </div>
                            
                            <p>We will notify you once a match is found. Your selfless act can save lives!</p>
                            <p>Thank you for being a hero! ❤️</p>
                            
                            <p>Best regards,<br><strong>The LifeLink Team</strong></p>
                        </div>
                        <div class="footer">
                            <p>© 2025 LifeLink - The Digital Bridge. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                userName,
                event.getDonationId(),
                event.getDonationType(),
                event.getDonationDate(),
                event.getStatus()
        );
    }

    private String buildRequestCreatedEmailTemplate(String userName, ReceiveRequestEvent event) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .info-box { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #f5576c; border-radius: 5px; }
                        .footer { text-align: center; margin-top: 20px; color: #888; font-size: 12px; }
                        h1 { margin: 0; font-size: 28px; }
                        .label { font-weight: bold; color: #f5576c; }
                        .urgency { background: #fff3cd; padding: 10px; border-radius: 5px; margin: 15px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>📋 Request Registered!</h1>
                        </div>
                        <div class="content">
                            <p>Dear <strong>%s</strong>,</p>
                            <p>Your request has been successfully registered in our system. We are actively searching for a suitable match.</p>
                            
                            <div class="info-box">
                                <p><span class="label">Request ID:</span> %s</p>
                                <p><span class="label">Request Type:</span> %s</p>
                                <p><span class="label">Date:</span> %s</p>
                                <p><span class="label">Urgency Level:</span> %s</p>
                                <p><span class="label">Status:</span> %s</p>
                            </div>
                            
                            <div class="urgency">
                                <strong>⚠️ Note:</strong> We prioritize requests based on urgency level and will notify you immediately when a match is found.
                            </div>
                            
                            <p>Stay strong! We're working hard to find you a match. 💪</p>
                            
                            <p>Best regards,<br><strong>The LifeLink Team</strong></p>
                        </div>
                        <div class="footer">
                            <p>© 2025 LifeLink - The Digital Bridge. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                userName,
                event.getReceiveRequestId(),
                event.getRequestType(),
                event.getRequestDate(),
                event.getUrgencyLevel(),
                event.getStatus()
        );
    }

    private String buildDonationCancelledEmailTemplate(String userName, DonationCancelledEvent event) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #6c757d; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .info-box { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #6c757d; border-radius: 5px; }
                        .footer { text-align: center; margin-top: 20px; color: #888; font-size: 12px; }
                        h1 { margin: 0; font-size: 28px; }
                        .label { font-weight: bold; color: #6c757d; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Donation Cancelled</h1>
                        </div>
                        <div class="content">
                            <p>Dear <strong>%s</strong>,</p>
                            <p>This is to confirm that your donation has been cancelled as requested.</p>
                            
                            <div class="info-box">
                                <p><span class="label">Donation ID:</span> %s</p>
                                <p><span class="label">Cancelled At:</span> %s</p>
                                <p><span class="label">Reason:</span> %s</p>
                            </div>
                            
                            <p>We understand that circumstances change. If you'd like to donate again in the future, we'd love to have you back!</p>
                            
                            <p>Best regards,<br><strong>The LifeLink Team</strong></p>
                        </div>
                        <div class="footer">
                            <p>© 2025 LifeLink - The Digital Bridge. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                userName,
                event.getDonationId(),
                event.getCancelledAt(),
                event.getCancellationReason()
        );
    }

    private String buildRequestCancelledEmailTemplate(String userName, RequestCancelledEvent event) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #6c757d; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .info-box { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #6c757d; border-radius: 5px; }
                        .footer { text-align: center; margin-top: 20px; color: #888; font-size: 12px; }
                        h1 { margin: 0; font-size: 28px; }
                        .label { font-weight: bold; color: #6c757d; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Request Cancelled</h1>
                        </div>
                        <div class="content">
                            <p>Dear <strong>%s</strong>,</p>
                            <p>This is to confirm that your request has been cancelled as requested.</p>
                            
                            <div class="info-box">
                                <p><span class="label">Request ID:</span> %s</p>
                                <p><span class="label">Cancelled At:</span> %s</p>
                                <p><span class="label">Reason:</span> %s</p>
                            </div>
                            
                            <p>If you need assistance in the future, please don't hesitate to create a new request. We're here to help!</p>
                            
                            <p>Best regards,<br><strong>The LifeLink Team</strong></p>
                        </div>
                        <div class="footer">
                            <p>© 2025 LifeLink - The Digital Bridge. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                userName,
                event.getRequestId(),
                event.getCancelledAt(),
                event.getCancellationReason()
        );
    }
    public void sendMatchFoundEmail(String toEmail, String userName, String otherUserName, String otherUserProfileLink, 
                                    com.notification.kafka.event.MatchFoundEvent event, boolean isDonor,
                                    com.notification.dto.DonationDTO donation, com.notification.dto.ReceiveRequestDTO request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Match Found! - LifeLink");
            helper.setFrom("lifelink590@gmail.com");

            String htmlContent = buildMatchFoundEmailTemplate(userName, otherUserName, otherUserProfileLink, event, isDonor, donation, request);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Match found email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send match found email to: {}", toEmail, e);
        }
    }

    private String buildMatchFoundEmailTemplate(String userName, String otherUserName, String otherUserProfileLink, 
                                                com.notification.kafka.event.MatchFoundEvent event, boolean isDonor,
                                                com.notification.dto.DonationDTO donation, com.notification.dto.ReceiveRequestDTO request) {
        String roleText = isDonor ? "Recipient" : "Donor";
        String actionText = isDonor ? "Please review their profile and confirm if you wish to proceed." : "The donor has been notified. Please wait for their confirmation.";
        
        String specificType = "";
        String itemType = isDonor ? "donation" : "request";
        
        if (isDonor && donation != null && donation.getDonationType() != null) {
            switch (donation.getDonationType()) {
                case BLOOD:
                    specificType = donation.getBloodType() != null ? donation.getBloodType().replace("_POSITIVE", "+").replace("_NEGATIVE", "-") : "";
                    break;
                case ORGAN:
                    specificType = donation.getOrganType() != null ? donation.getOrganType().replace("_", " ") : "";
                    break;
                case TISSUE:
                    specificType = donation.getTissueType() != null ? donation.getTissueType().replace("_", " ") : "";
                    break;
                case STEM_CELL:
                    specificType = donation.getStemCellType() != null ? donation.getStemCellType().replace("_", " ") : "";
                    break;
            }
            itemType = donation.getDonationType().toString() + " " + itemType;
        } else if (!isDonor && request != null && request.getRequestType() != null) {
             switch (request.getRequestType()) {
                case BLOOD:
                    specificType = request.getRequestedBloodType() != null ? request.getRequestedBloodType().replace("_POSITIVE", "+").replace("_NEGATIVE", "-") : "";
                    break;
                case ORGAN:
                    specificType = request.getRequestedOrgan() != null ? request.getRequestedOrgan().replace("_", " ") : "";
                    break;
                case TISSUE:
                    specificType = request.getRequestedTissue() != null ? request.getRequestedTissue().replace("_", " ") : "";
                    break;
                case STEM_CELL:
                    specificType = request.getRequestedStemCellType() != null ? request.getRequestedStemCellType().replace("_", " ") : "";
                    break;
            }
            itemType = request.getRequestType().toString() + " " + itemType;
        }
        
        // Capitalize first letter of each word
        if (!specificType.isEmpty() && !specificType.contains("+") && !specificType.contains("-")) {
             String[] words = specificType.toLowerCase().split(" ");
             StringBuilder sb = new StringBuilder();
             for (String word : words) {
                 if (word.length() > 0) {
                     sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
                 }
             }
             specificType = sb.toString().trim();
        }

        String typeDescription = (specificType.isEmpty() ? "" : specificType + " ") + itemType;

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #00b09b 0%%, #96c93d 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .info-box { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #00b09b; border-radius: 5px; }
                        .footer { text-align: center; margin-top: 20px; color: #888; font-size: 12px; }
                        h1 { margin: 0; font-size: 28px; }
                        .label { font-weight: bold; color: #00b09b; }
                        .btn { display: inline-block; padding: 10px 20px; background-color: #00b09b; color: white; text-decoration: none; border-radius: 5px; margin-top: 15px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>✨ Match Found!</h1>
                        </div>
                        <div class="content">
                            <p>Dear <strong>%s</strong>,</p>
                            <p>Great news! We have found a potential match for your <strong>%s</strong>.</p>
                            
                            <div class="info-box">
                                <p><span class="label">Match ID:</span> %s</p>
                                <p><span class="label">Matched With:</span> %s (%s)</p>
                                <p><span class="label">Matched At:</span> %s</p>
                                <p><span class="label">Compatibility Score:</span> %.1f%%</p>
                            </div>
                            
                            <p>%s</p>
                            
                            <a href="http://localhost:3000%s" class="btn">View %s Profile</a>
                            
                            <p>Best regards,<br><strong>The LifeLink Team</strong></p>
                        </div>
                        <div class="footer">
                            <p>© 2025 LifeLink - The Digital Bridge. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                userName,
                typeDescription,
                event.getMatchId(),
                otherUserName,
                roleText,
                event.getMatchedAt(),
                event.getCompatibilityScore() * 100,
                actionText,
                otherUserProfileLink,
                roleText
        );
    }
}
