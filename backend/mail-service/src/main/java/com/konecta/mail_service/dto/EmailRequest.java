package com.konecta.mail_service.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailRequest {
    private String recipient;
    private String subject;
    private String content;
}