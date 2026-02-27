package com.bidvibe.bidvibeapispring.dto.auction;

import java.time.Instant;
import java.util.UUID;

import com.bidvibe.bidvibeapispring.entity.AuctionSession;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response trả về thông tin một phiên đấu giá lớn (AuctionSession).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionSessionResponse {

    private UUID id;
    private String title;
    private AuctionSession.Type type;
    private Instant startTime;
    private AuctionSession.Status status;

    // ------------------------------------------------------------------
    // Mapper helper
    // ------------------------------------------------------------------

    public static AuctionSessionResponse from(AuctionSession session) {
        return AuctionSessionResponse.builder()
                .id(session.getId())
                .title(session.getTitle())
                .type(session.getType())
                .startTime(session.getStartTime())
                .status(session.getStatus())
                .build();
    }
}
