package DavexCenter.module.auth.controller;


import DavexBase.entity.Agent;
import DavexCenter.common.AuthException;
import DavexCenter.module.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class JwtController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Agent agent) {
        String token = authService.login(agent.getUid(), agent.getPassword());
        return ResponseEntity.ok(token);
    }

    // 检查 JWT 有效性
    @PostMapping("/checkJWT")
    public ResponseEntity<?> checkJWT(@RequestHeader("Authorization") String token) {
        boolean isValid = authService.validateToken(token);
        return isValid ? ResponseEntity.ok("Token is valid")
                : ResponseEntity.status(401).body("Invalid token");
    }

    // 刷新 JWT
    @PostMapping("/refreshJWT")
    public ResponseEntity<?> refreshJWT(@RequestHeader("Authorization") String oldToken) {
        try {
            String newToken = authService.refreshToken(oldToken);
            return ResponseEntity.ok(newToken);
        } catch (AuthException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // 吊销 JWT
    @PostMapping("/revokeJWT")
    public ResponseEntity<?> revokeJWT(@RequestHeader("Authorization") String token) {
        authService.revokeToken(token);
        return ResponseEntity.ok("Token revoked");
    }

    // 吊销 JWT
    @PostMapping("/deleteAllRevokedJWT")
    public boolean deleteAllRevokedJWT() {
        return authService.deleteAllRevokedToken();
    }

}
