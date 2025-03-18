package DavexCenter.module.auth.controller;


import DavexBase.entity.Agent;
import DavexCenter.common.AuthException;
import DavexCenter.module.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class JwtController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>> login(@RequestBody Agent agent) {
        Map<String,Object> token = authService.login(agent.getUid(), agent.getPassword());
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
    public ResponseEntity<Map<String,Object>> refreshJWT(@RequestHeader("Authorization") String oldToken) {
        try {
            Map<String,Object> newToken = authService.refreshToken(oldToken);
            return ResponseEntity.ok(newToken);
        } catch (AuthException e) {
            //返回一个包含错误信息的 Map，而不是 String
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
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
