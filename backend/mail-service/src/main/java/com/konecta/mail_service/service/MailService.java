package com.konecta.mail_service.service;

import com.konecta.mail_service.dto.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(EmailRequest request) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Konecta Password Reset OTP</title>
                    <style>
                        /* Modern styles with inverted color scheme */
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; margin: 0; padding: 0; background-color: #ffffff; color: #333; }
                        .container { max-width: 600px; margin: 40px auto; padding: 30px; background-color: #2A01CD; border-radius: 12px; box-shadow: 0 6px 15px rgba(0, 0, 0, 0.2); color: #A6B7FF; border: 1px solid #A6B7FF; }
                        .header { font-size: 24px; font-weight: 700; text-align: center; margin-bottom: 30px; color: #F0FA00; border-bottom: 3px solid #A6B7FF; padding-bottom: 15px; }
                        .otp-block { display: block; width: fit-content; margin: 25px auto; padding: 15px 35px; font-size: 40px; font-weight: 800; letter-spacing: 6px; color: #2A01CD; background-color: #FFF; border-radius: 8px; text-align: center; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.5); }
                        .expiry-time {  font-weight: 700; white-space: nowrap; }
                        .footer { font-size: 14px; margin-top: 30px; padding-top: 20px; color: #A6B7FF; text-align: center; border-top: 1px solid #A6B7FF; }
                        p { margin-bottom: 20px; }
                        a { color: #F0FA00; }
                    </style>
                </head>
                <body style="background-color: #ffffff;">
                    <div class="container" style="max-width: 600px; margin: 40px auto; padding: 30px; background-color: #2A01CD; border-radius: 12px; box-shadow: 0 6px 15px rgba(0, 0, 0, 0.2); color: #A6B7FF; border: 1px solid #A6B7FF;">
                        <div class="header" style="color: #F0FA00;">
                            ${subject}
                        </div>
                        <p>Hello Dear Employee,</p>
                        ${content}
                        <p> Thank you,<br>Konecta Team</p>
                        <div class="footer">
                            If you have any questions, please <a href="mailto:erp.konecta@gmail.com" style="color: #F0FA00; text-decoration: none;">contact us</a>.
                        </div>
                    </div>
                </body>
                </html>
        """;
        html =html.replace("${subject}", request.getSubject())
                .replace("${content}", request.getContent());

        helper.setTo(request.getRecipient());
        helper.setSubject(request.getSubject());
        helper.setText(html, true);
        mailSender.send(mimeMessage);
    }
}
