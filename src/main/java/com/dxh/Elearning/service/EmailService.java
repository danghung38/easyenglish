package com.dxh.Elearning.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-SERVICE")
public class EmailService {

    private final WebClient brevoWebClient;

    @Value("${spring.brevo.api-key}")
    private String apiKey;

    @Value("${spring.brevo.from-email}")
    private String fromEmail;

    @Value("${spring.brevo.from-name}")
    private String fromName;

    public void sendVerificationEmail(String to, String name, String otp) throws IOException {
        Map<String, Object> body = Map.of(
                "sender", Map.of("email", fromEmail, "name", fromName),
                "to", List.of(Map.of("email", to)),
                "subject", "Verify your EasyEnglish account",
                "htmlContent", buildOtpHtml(name, otp)
        );
        send(body, "OTP");
    }

    public void sendResetPasswordEmail(String to, String name, String resetCode) throws IOException {
        Map<String, Object> body = Map.of(
                "sender", Map.of("email", fromEmail, "name", fromName),
                "to", List.of(Map.of("email", to)),
                "subject", "Reset your EasyEnglish password",
                "htmlContent", buildResetPasswordHtml(name, resetCode)
        );
        send(body, "ResetPassword");
    }

    private void send(Map<String, Object> body, String type) throws IOException {
        try {
            brevoWebClient.post()
                    .uri("/smtp/email")
                    .header("api-key", apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), response ->
                            response.bodyToMono(String.class)
                                    .defaultIfEmpty("No response body")
                                    .flatMap(error -> Mono.error(new IOException(
                                            "Brevo rejected " + type + " email: " + error))))
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(10))
                    .block();
            log.info("[{}] email sent successfully", type);
        } catch (Exception e) {
            log.error("[{}] email failed: {}", type, e.getMessage());
            throw new IOException("Brevo email request failed", e);
        }
    }

    private String buildOtpHtml(String name, String otp) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head><meta charset="UTF-8"/></head>
            <body style="margin:0;padding:0;background:#f0f4f8;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%" cellpadding="0" cellspacing="0" style="padding:40px 0;">
                <tr><td align="center">
                  <table width="560" cellpadding="0" cellspacing="0"
                    style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                    <tr>
                      <td style="background:linear-gradient(135deg,#667eea,#764ba2);padding:40px;text-align:center;">
                        <div style="font-size:36px;">&#127891;</div>
                        <h1 style="color:#fff;margin:8px 0 0;font-size:24px;">EasyEnglish</h1>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:40px;">
                        <h2 style="color:#1a202c;margin:0 0 8px;">Hello, {{name}}! &#128075;</h2>
                        <p style="color:#718096;font-size:15px;line-height:1.6;margin:0 0 28px;">
                          Enter the OTP below to verify your EasyEnglish account.
                        </p>
                        <div style="background:#f7f5ff;border:2px dashed #764ba2;border-radius:12px;
                                    padding:28px;text-align:center;margin-bottom:24px;">
                          <p style="margin:0 0 8px;color:#9a8abf;font-size:12px;
                                    text-transform:uppercase;letter-spacing:2px;">Verification code</p>
                          <div style="font-size:42px;font-weight:800;letter-spacing:12px;
                                      color:#667eea;font-family:'Courier New',monospace;">{{otp}}</div>
                        </div>
                        <div style="background:#fff8e1;border-left:4px solid #f6ad55;
                                    border-radius:6px;padding:14px 18px;">
                          <p style="margin:0;color:#92400e;font-size:14px;">
                            &#9201; This code expires in <strong>30 minutes</strong>.
                            Never share it with anyone.
                          </p>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td style="background:#f7fafc;padding:20px 40px;border-top:1px solid #e2e8f0;text-align:center;">
                        <p style="margin:0;color:#cbd5e0;font-size:12px;">&#169; 2026 EasyEnglish</p>
                      </td>
                    </tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """
                .replace("{{name}}", safe(name))
                .replace("{{otp}}", safe(otp));
    }

    private String buildResetPasswordHtml(String name, String resetCode) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head><meta charset="UTF-8"/></head>
            <body style="margin:0;padding:0;background:#f0f4f8;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%" cellpadding="0" cellspacing="0" style="padding:40px 0;">
                <tr><td align="center">
                  <table width="560" cellpadding="0" cellspacing="0"
                    style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                    <tr>
                      <td style="background:linear-gradient(135deg,#f093fb,#f5576c);padding:40px;text-align:center;">
                        <div style="font-size:36px;">&#128274;</div>
                        <h1 style="color:#fff;margin:8px 0 0;font-size:24px;">EasyEnglish</h1>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:40px;">
                        <h2 style="color:#1a202c;margin:0 0 8px;">Reset your password</h2>
                        <p style="color:#718096;font-size:15px;line-height:1.6;margin:0 0 28px;">
                          Hello <strong>{{name}}</strong>, use this code to reset your password.
                        </p>
                        <div style="background:#fff5f5;border:2px dashed #f5576c;border-radius:12px;
                                    padding:28px;text-align:center;margin-bottom:24px;">
                          <p style="margin:0 0 8px;color:#c53030;font-size:12px;
                                    text-transform:uppercase;letter-spacing:2px;">Reset code</p>
                          <div style="font-size:42px;font-weight:800;letter-spacing:12px;
                                      color:#f5576c;font-family:'Courier New',monospace;">{{resetCode}}</div>
                        </div>
                        <div style="background:#fff8e1;border-left:4px solid #f6ad55;
                                    border-radius:6px;padding:14px 18px;margin-bottom:12px;">
                          <p style="margin:0;color:#92400e;font-size:14px;">
                            &#9201; This code expires in <strong>30 minutes</strong>.
                          </p>
                        </div>
                        <div style="background:#ebf8ff;border-left:4px solid #63b3ed;
                                    border-radius:6px;padding:14px 18px;">
                          <p style="margin:0;color:#2c5282;font-size:14px;">
                            &#128737; If you did not request a password reset, you can safely ignore this email.
                          </p>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td style="background:#f7fafc;padding:20px 40px;border-top:1px solid #e2e8f0;text-align:center;">
                        <p style="margin:0;color:#cbd5e0;font-size:12px;">&#169; 2026 EasyEnglish</p>
                      </td>
                    </tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """
                .replace("{{name}}", safe(name))
                .replace("{{resetCode}}", safe(resetCode));
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
