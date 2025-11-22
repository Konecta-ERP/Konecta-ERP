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
                    <html>
                    <head>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                line-height: 1.6;
                            }
                            .container {
                                max-width: 600px;
                                margin: 0 auto;
                                padding: 20px;
                                border: 1px solid #ddd;
                                border-radius: 8px;
                                color: #A6B7FF;
                                background-color: #2A01CD;
                            }
                            .header {
                                font-size: 18px;
                                font-weight: bold;
                                text-align: center;
                                margin-bottom: 20px;
                                color: #F0FA00;
                            }
                            .footer {
                                font-size: 12px;
                                margin-top: 20px;
                                color: #F0FA00;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">${subject}</div>
                            <p>Hello Dear Employee,</p>
                            ${content}
                            <p>Thank you,<br>
                            Konecta Team</p>
                            <div class="footer">If you have any questions, please contact us.</div>
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
