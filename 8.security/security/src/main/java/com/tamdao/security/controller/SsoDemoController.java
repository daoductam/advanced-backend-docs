package com.tamdao.security.controller;

import com.tamdao.security.entity.User;
import com.tamdao.security.hashing.PasswordHasher;
import com.tamdao.security.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class SsoDemoController {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    // SSO Memory Stores
    private final Map<String, String> activeTickets = new ConcurrentHashMap<>(); // ticket -> username
    private final Map<String, String> ssoSessions = new ConcurrentHashMap<>();   // ssoCookie -> username
    
    // App Local Memory Sessions
    private final Map<String, String> app1Sessions = new ConcurrentHashMap<>(); // app1Cookie -> username
    private final Map<String, String> app2Sessions = new ConcurrentHashMap<>(); // app2Cookie -> username

    public SsoDemoController(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    // ======================================================
    // 1. SSO SERVER ENDPOINTS (sso.domain.com)
    // ======================================================

    /**
     * SSO Authorization Endpoint.
     * Decides whether to issue a ticket (if already logged into SSO) or redirect to login.
     */
    @GetMapping("/sso/authorize")
    public ResponseEntity<Void> ssoAuthorize(@RequestParam String redirectUrl,
                                             @CookieValue(value = "SSO_COOKIE", required = false) String ssoCookie) {
        if (ssoCookie != null && ssoSessions.containsKey(ssoCookie)) {
            // User is already logged into SSO Server! Issue a one-time Ticket
            String username = ssoSessions.get(ssoCookie);
            String ticket = "TKT-" + UUID.randomUUID().toString().substring(0, 8);
            activeTickets.put(ticket, username);

            // Redirect back to application callback with the ticket
            String targetUrl = redirectUrl + "?ticket=" + ticket;
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(targetUrl)).build();
        }

        // Not logged in to SSO. Redirect to the SSO login page
        String loginPageUrl = "/api/sso/login-page?redirectUrl=" + redirectUrl;
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(loginPageUrl)).build();
    }

    /**
     * SSO Login Page (mock prompt instructions).
     */
    @GetMapping("/sso/login-page")
    public ResponseEntity<String> ssoLoginPage(@RequestParam String redirectUrl) {
        return ResponseEntity.ok("<html><body>"
                + "<h2>SSO Central Login Server</h2>"
                + "<p>Để đăng nhập, hãy gửi yêu cầu POST đến <b>/api/sso/login-submit</b></p>"
                + "<form action='/api/sso/login-submit' method='POST'>"
                + "  <input type='hidden' name='redirectUrl' value='" + redirectUrl + "' />"
                + "  Tên tài khoản: <input type='text' name='username' required /><br/>"
                + "  Mật khẩu: <input type='password' name='password' required /><br/>"
                + "  <button type='submit'>Đăng nhập</button>"
                + "</form>"
                + "</body></html>");
    }

    /**
     * SSO Submit Login.
     * Sets the SSO Cookie on sso.domain.com and redirects back to target app with a ticket.
     */
    @PostMapping("/sso/login-submit")
    public ResponseEntity<Void> ssoLoginSubmit(@RequestParam String username,
                                               @RequestParam String password,
                                               @RequestParam String redirectUrl,
                                               HttpServletResponse response) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (!passwordHasher.verify(password, user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Create SSO Session
        String ssoCookieValue = "SSO-SESS-" + UUID.randomUUID().toString().substring(0, 8);
        ssoSessions.put(ssoCookieValue, username);

        // Set the SSO Cookie in browser
        Cookie ssoCookie = new Cookie("SSO_COOKIE", ssoCookieValue);
        ssoCookie.setPath("/api/sso"); // Restrict path to simulate SSO subdomain isolation
        response.addCookie(ssoCookie);

        // Generate one-time Ticket
        String ticket = "TKT-" + UUID.randomUUID().toString().substring(0, 8);
        activeTickets.put(ticket, username);

        // Redirect back to application
        String targetUrl = redirectUrl + "?ticket=" + ticket;
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(targetUrl)).build();
    }

    /**
     * SSO Backchannel Ticket Verification Endpoint.
     * Validates and destroys the ticket (one-time use).
     */
    @GetMapping("/sso/verify-ticket")
    public ResponseEntity<?> ssoVerifyTicket(@RequestParam String ticket) {
        if (!activeTickets.containsKey(ticket)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ticket không hợp lệ hoặc đã được sử dụng!");
        }

        // Retrieve and invalidate ticket immediately
        String username = activeTickets.remove(ticket);
        return ResponseEntity.ok(Map.of("username", username, "verified", true));
    }

    // ======================================================
    // 2. APPLICATION 1 (app1.domain.com)
    // ======================================================

    @GetMapping("/app1/dashboard")
    public ResponseEntity<String> app1Dashboard(@CookieValue(value = "APP1_SESSION", required = false) String app1Cookie) {
        if (app1Cookie != null && app1Sessions.containsKey(app1Cookie)) {
            String username = app1Sessions.get(app1Cookie);
            return ResponseEntity.ok("Chào mừng bạn đến APP 1 Dashboard! Đang đăng nhập dưới quyền: " + username);
        }

        // Not logged in locally. Redirect to SSO to authenticate
        String authorizeUrl = "/api/sso/authorize?redirectUrl=/api/app1/callback";
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(authorizeUrl)).build();
    }

    @GetMapping("/app1/callback")
    public ResponseEntity<Void> app1Callback(@RequestParam String ticket, HttpServletResponse response) {
        // App 1 verifies the ticket via backchannel with SSO Server
        if (activeTickets.containsKey(ticket)) {
            String username = activeTickets.remove(ticket); // Simulate verification

            // Create App 1 local session
            String app1CookieValue = "APP1-SESS-" + UUID.randomUUID().toString().substring(0, 8);
            app1Sessions.put(app1CookieValue, username);

            // Set local Cookie
            Cookie localCookie = new Cookie("APP1_SESSION", app1CookieValue);
            localCookie.setPath("/api/app1");
            response.addCookie(localCookie);

            // Redirect back to Dashboard
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/api/app1/dashboard")).build();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    // ======================================================
    // 3. APPLICATION 2 (app2.domain.com)
    // ======================================================

    @GetMapping("/app2/dashboard")
    public ResponseEntity<String> app2Dashboard(@CookieValue(value = "APP2_SESSION", required = false) String app2Cookie) {
        if (app2Cookie != null && app2Sessions.containsKey(app2Cookie)) {
            String username = app2Sessions.get(app2Cookie);
            return ResponseEntity.ok("Chào mừng bạn đến APP 2 Dashboard! Đang đăng nhập dưới quyền: " + username);
        }

        // Not logged in locally. Redirect to SSO to authenticate
        String authorizeUrl = "/api/sso/authorize?redirectUrl=/api/app2/callback";
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(authorizeUrl)).build();
    }

    @GetMapping("/app2/callback")
    public ResponseEntity<Void> app2Callback(@RequestParam String ticket, HttpServletResponse response) {
        // App 2 verifies the ticket via backchannel with SSO Server
        if (activeTickets.containsKey(ticket)) {
            String username = activeTickets.remove(ticket); // Simulate verification

            // Create App 2 local session
            String app2CookieValue = "APP2-SESS-" + UUID.randomUUID().toString().substring(0, 8);
            app2Sessions.put(app2CookieValue, username);

            // Set local Cookie
            Cookie localCookie = new Cookie("APP2_SESSION", app2CookieValue);
            localCookie.setPath("/api/app2");
            response.addCookie(localCookie);

            // Redirect back to Dashboard
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/api/app2/dashboard")).build();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
