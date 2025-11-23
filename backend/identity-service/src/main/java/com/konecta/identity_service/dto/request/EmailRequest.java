package com.konecta.identity_service.dto.request;


import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest implements Serializable {
    private String recipient;
    private String subject;
    private String content;
}