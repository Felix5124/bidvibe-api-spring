package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.dto.bid.ProxyBidResponse;
import com.bidvibe.bidvibeapispring.dto.bid.SetProxyBidRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Facade cho nghiệp vụ Proxy Bid.
 * Logic thực tế nằm trong {@link BidService} để đảm bảo tính nhất quán
 * khi chạy Proxy Engine ngay sau mỗi manual bid.
 */
@Service
@RequiredArgsConstructor
public class ProxyBidService {

    private final BidService bidService;

    /**
     * Đặt hoặc cập nhật hạn mức Proxy Bid cho một auction.
     * Nếu đã có proxy bid đang active → cập nhật maxAmount.
     */
    @Transactional
    public ProxyBidResponse setProxyBid(UUID userId, SetProxyBidRequest req) {
        return bidService.setProxyBid(userId, req);
    }

    /** Huỷ Proxy Bid hiện tại (đặt isActive = false). */
    @Transactional
    public void cancelProxyBid(UUID userId, UUID auctionId) {
        bidService.cancelProxyBid(userId, auctionId);
    }
}
