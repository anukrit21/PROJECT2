package com.demoApp.authentication.service.impl;

import com.demoApp.authentication.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@demoapp.com}")
    private String fromEmail;

    @Value("${app.url:https://demoapp.com}")
    private String appUrl;

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Password Reset Request</h2>
                    <p>Hello,</p>
                    <p>You have requested to reset your password. Please click the link below:</p>
                    <p><a href="%s">Reset Password</a></p>
                    <p>If you did not request this, please ignore this email.</p>
                    <p>Thanks,<br/>DemoApp Team</p>
                </body>
                </html>
                """, resetLink);

        sendEmail(to, "DemoApp - Password Reset Request", htmlContent);
    }

    @Override
    public void sendAccountLockedEmail(String to, String username) {
        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Account Locked</h2>
                    <p>Hello %s,</p>
                    <p>Your account has been locked due to multiple failed login attempts.</p>
                    <p>To unlock your account, please reset your password:</p>
                    <p><a href="%s/reset-password">Reset Password</a></p>
                    <p>Thanks,<br/>DemoApp Team</p>
                </body>
                </html>
                """, username, appUrl);

        sendEmail(to, "DemoApp - Account Locked", htmlContent);
    }

    @Override
    public void sendMfaSetupEmail(String to, String setupLink) {
        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>MFA Setup</h2>
                    <p>Hello,</p>
                    <p>Click the link below to set up Multi-Factor Authentication:</p>
                    <p><a href="%s">Setup MFA</a></p>
                    <p>This link will expire in 15 minutes.</p>
                    <p>Thanks,<br/>DemoApp Team</p>
                </body>
                </html>
                """, setupLink);

        sendEmail(to, "DemoApp - Multi-Factor Authentication Setup", htmlContent);
    }

    @Override
    public void sendOAuthRegistrationEmail(String to, String username) {
        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Welcome to DemoApp!</h2>
                    <p>Hello %s,</p>
                    <p>Your account has been successfully created.</p>
                    <p>You can now login using your OAuth provider:</p>
                    <p><a href="%s/login">Login</a></p>
                    <p>Thanks,<br/>DemoApp Team</p>
                </body>
                </html>
                """, username, appUrl);

        sendEmail(to, "Welcome to DemoApp", htmlContent);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}