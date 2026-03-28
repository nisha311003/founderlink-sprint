package com.founderlink.messagingService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageRequest {

    @NotNull(message = "Receiver ID is mandatory")
    private Long receiverId;

    @NotBlank(message = "Receiver email is mandatory")
    private String receiverEmail;

    @NotBlank(message = "Message content is mandatory")
    private String content;
}
