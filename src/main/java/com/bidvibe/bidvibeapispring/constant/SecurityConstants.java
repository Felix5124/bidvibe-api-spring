package com.bidvibe.bidvibeapispring.constant;

/**
 * Hằng số liên quan đến bảo mật, JWT và CORS.
 * Dùng chung cho SecurityConfig và JwtAuthFilter.
 */
public final class SecurityConstants {

    private SecurityConstants() {}

    // -------------------------------------------------------------------------
    // JWT – Supabase Auth
    // -------------------------------------------------------------------------

    /** Tên header chứa JWT trong mọi request cần xác thực. */
    public static final String AUTH_HEADER = "Authorization";

    /** Prefix bắt buộc đứng trước JWT token. */
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * Claim trong Supabase JWT chứa email của user.
     * Dùng để tra cứu / tạo User trong database.
     */
    public static final String JWT_CLAIM_EMAIL = "email";

    /**
     * Claim chứa UUID của user từ Supabase Auth.
     * Dùng làm sub (subject) của token.
     */
    public static final String JWT_CLAIM_SUB = "sub";

    /**
     * Claim chứa role của user trong Supabase metadata.
     * Ví dụ: "authenticated", "service_role".
     */
    public static final String JWT_CLAIM_ROLE = "role";

    // -------------------------------------------------------------------------
    // URL WHITELIST – Không cần xác thực
    // -------------------------------------------------------------------------

    /**
     * Các đường dẫn hoàn toàn công khai — không giới hạn HTTP method.
     * Dùng cho WebSocket handshake, health check, Swagger docs.
     */
    public static final String[] PUBLIC_PATHS = {
            "/ws/**",                    // WebSocket handshake endpoint
            "/actuator/health",          // Health check
            "/v3/api-docs/**",           // Swagger / OpenAPI docs
            "/swagger-ui/**",
    };

    /**
     * Các API path chỉ cho phép GET không cần xác thực.
     * Tất cả method khác (POST/PUT/DELETE) vẫn yêu cầu JWT.
     */
    public static final String[] PUBLIC_GET_URLS = {
            "/api/sessions/**",          // Xem thông tin phiên đấu giá công khai
            "/api/auctions/**",          // Xem chi tiết auction công khai
            "/api/items/**",             // Xem thông tin vật phẩm công khai
            "/api/market/listings/**",   // Xem danh sách Chợ Đen công khai
            "/api/analytics/**",         // Xem biểu đồ giá & thống kê công khai
    };

    /**
     * Các URL chỉ ADMIN được phép truy cập.
     * Dùng trong SecurityConfig để cấu hình hasRole("ADMIN").
     */
    public static final String[] ADMIN_URLS = {
            "/api/admin/**",
    };

    // -------------------------------------------------------------------------
    // CORS
    // -------------------------------------------------------------------------

    /** Origin được phép (Frontend React). Cấu hình lại theo môi trường. */
    public static final String[] ALLOWED_ORIGINS = {
            "http://localhost:3000",
            "http://localhost:5173",
    };

    public static final String[] ALLOWED_METHODS = {
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
    };

    public static final String[] ALLOWED_HEADERS = {
            AUTH_HEADER,
            "Content-Type",
            "Accept",
            "X-Requested-With",
    };

    // -------------------------------------------------------------------------
    // WEBSOCKET
    // -------------------------------------------------------------------------

    /** Endpoint WebSocket mà client kết nối tới (STOMP over SockJS). */
    public static final String WS_ENDPOINT = "/ws";
}

