package com.bidvibe.bidvibeapispring.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Request body gửi tin nhắn Chat Live (trong phòng đấu giá)
 * hoặc Chat P2P (thương lượng Chợ Đen).
 *
 * <ul>
 *   <li>Chat Live:  auctionId != null, receiverId == null</li>
 *   <li>Chat P2P:   receiverId != null, auctionId == null</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
public class SendMessageRequest {

    /** ID phòng đấu giá (Chat Live). */
    private UUID auctionId;

    /** ID người nhận (Chat P2P). */
    private UUID receiverId;

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    @Size(max = 1000, message = "Nội dung không được quá 1000 ký tự")
    private String content;
}
