package com.example.mailsend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MailSendByOfficeResponse {

    private OfficeResponse office;
    private List<MailSendResponse> mailSends;
}
