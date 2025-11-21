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
}
