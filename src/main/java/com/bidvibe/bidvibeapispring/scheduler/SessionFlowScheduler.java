package com.bidvibe.bidvibeapispring.scheduler;

import com.bidvibe.bidvibeapispring.entity.AuctionSession;
import com.bidvibe.bidvibeapispring.service.AuctionService;
import com.bidvibe.bidvibeapispring.service.AuctionSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionFlowScheduler {

    private final AuctionSessionService sessionService;
    private final AuctionService auctionService;

    @Scheduled(fixedRate = 1000)
    public void continueSequentialFlow() {
        Instant now = Instant.now();

        sessionService.getSessionsByStatus(AuctionSession.Status.SCHEDULED)
                .forEach(session -> {
                    if (session.getStartTime() == null || session.getStartTime().isAfter(now)) {
                        return;
                    }

                    try {
                        sessionService.activateSession(session.getId());
                        auctionService.startFirstWaitingAuction(session.getId());
                    } catch (Exception ex) {
                        log.warn("[SessionFlowScheduler] Failed to auto-start session {}: {}", session.getId(), ex.getMessage());
                    }
                });

        sessionService.getSessionsByStatus(AuctionSession.Status.ACTIVE)
                .forEach(session -> {
                    try {
                        auctionService.startNextWaitingAuctionOrComplete(session.getId());
                    } catch (Exception ex) {
                        log.warn("[SessionFlowScheduler] Failed to advance session {}: {}", session.getId(), ex.getMessage());
                    }
                });
    }
}
