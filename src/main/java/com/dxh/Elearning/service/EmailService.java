package com.dxh.Elearning.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-SERVICE")
public class EmailService {

    private final SendGrid sendGrid;

    @Value("${spring.sendGrid.fromEmail}")
    private String from;

    @Value("${spring.sendGrid.templateId}")
    private String templateId;

    @Value("${spring.sendGrid.resetPasswordId}")
    private String resetPasswordId;

    @Value("${spring.sendGrid.verificationLink}")
    private String verificationLink;

    @Value("${spring.sendGrid.resetPasswordLink:http://localhost:3000/reset/confirm/}")
    private String resetPasswordLink;



    /**
     * Send simple email with sendGrid
     *
     * @param to
     * @param subject
     * @param text
     */
    public void send(String to, String subject, String text) {
        Email fromEmail = new Email(from);
        Email toEmail = new Email(to);

        Content content = new Content("text/plain", text);
        Mail mail = new Mail(fromEmail, subject, toEmail, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);

            if (response.getStatusCode() == 202) {
                log.info("Email sent successfully");
            } else {
                log.error("Email sent failed");
            }
        } catch (IOException e) {
            log.error("Email sent failed, errorMessage={}", e.getMessage());
        }
    }

    /**
     * Send email verification
     *
     * @param to
     * @param name
     */
    public void sendVerificationEmail(String to, String name, String secretKey) throws IOException {
        log.info("Sending verification email for name={}", name);

        Email fromEmail = new Email(from, "Hùng Java");
        Email toEmail = new Email(to);
        String subject = "Xác thực tài khoản";

        // Generate secret code and save to db
//        String secretCode = UUID.randomUUID().toString();
        log.info("secretCode = {}", secretKey);

        // TOD0 save secretCode to db

        // Tạo dynamic template data
        Map<String, String> dynamicTemplateData = new HashMap<>();
        dynamicTemplateData.put("name", name);
        dynamicTemplateData.put("verify_link", verificationLink + "?secretKey=" + secretKey);

        Mail mail = new Mail();
        mail.setFrom(fromEmail);
        mail.setSubject(subject);
        Personalization personalization = new Personalization();
        personalization.addTo(toEmail);

        // Add dynamic template data
        dynamicTemplateData.forEach(personalization::addDynamicTemplateData);

        mail.addPersonalization(personalization);
        mail.setTemplateId(templateId); // Template ID từ SendGrid

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sendGrid.api(request);
        if (response.getStatusCode() == 202) {
            log.info("VerificationToken sent successfully");
        } else {
            log.error("VerificationToken sent failed");
        }
    }


    public void sendResetPasswordEmail(String to, String name, String resetCode) throws IOException {
        log.info("Sending reset password email for name={} to email={}", name, to);

        Email fromEmail = new Email(from, "EngTech");
        Email toEmail = new Email(to);
        String subject = "Reset Your Password";

        Map<String, String> dynamicTemplateData = new HashMap<>();
        dynamicTemplateData.put("name", name);
        dynamicTemplateData.put("reset_code", resetCode);
        dynamicTemplateData.put("reset_link", resetPasswordLink + "?email=" + to + "&token=" + resetCode);

        Mail mail = new Mail();
        mail.setFrom(fromEmail);
        mail.setSubject(subject);
        Personalization personalization = new Personalization();
        personalization.addTo(toEmail);

        // Add dynamic template data
        dynamicTemplateData.forEach(personalization::addDynamicTemplateData);

        mail.addPersonalization(personalization);
        mail.setTemplateId(resetPasswordId); // Template ID từ SendGrid

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sendGrid.api(request);
        if (response.getStatusCode() == 202) {
            log.info("VerificationToken sent successfully");
        } else {
            log.error("VerificationToken sent failed");
        }
    }
}
