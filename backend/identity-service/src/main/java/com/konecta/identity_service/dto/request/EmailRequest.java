package com.konecta.identity_service.dto.request;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String recipient;
    private String subject;
    private String content;
}