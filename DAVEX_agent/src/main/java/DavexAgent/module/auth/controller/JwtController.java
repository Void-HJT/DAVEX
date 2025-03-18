package DavexAgent.module.auth.controller;

import DavexAgent.module.auth.service.AuthService;
import DavexBase.entity.JwtMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class JwtController {
    @Autowired
    AuthService authService;

    @PostMapping("/getJwt")
    public ResponseEntity<String> getJwt(@RequestParam("centerId")String centerId,
                                         @RequestParam("agentId")String agentId,
                                         @RequestParam("password")String password){
        boolean is = authService.getJwt(centerId,agentId,password);
        return is?ResponseEntity.ok("get jwt")
                : ResponseEntity.status(401).body("can not get jwt");
    }

    @PostMapping("/checkJwt")
    public ResponseEntity<String> checkJwt(@RequestParam("centerId")String centerId){
        boolean is = authService.checkJwt(centerId);
        return is?ResponseEntity.ok("valid jwt")
                : ResponseEntity.status(401).body("invalid jwt");
    }

    @PostMapping("/refreshJwt")
    public ResponseEntity<String> refreshJwt(@RequestParam("centerId")String centerId){
        boolean is = authService.refreshJwt(centerId);
        return is?ResponseEntity.ok("jwt is refreshed")
                : ResponseEntity.status(401).body("jwt refresh error");
    }

    @PostMapping("/revokeJwt")
    public ResponseEntity<String> revokeJwt(@RequestParam("centerId")String centerId){
        boolean is = authService.revokeJwt(centerId);
        return is?ResponseEntity.ok("jwt is revoked")
                : ResponseEntity.status(401).body("jwt revoke error");
    }

    @PostMapping("/getAllJwt")
    public java.util.Set<java.util.Map.Entry<String, String>> getAllJwt(){
        return authService.getAllJwt();
    }

    @PostMapping("/getAllMeta")
    public java.util.Set<java.util.Map.Entry<String, JwtMetadata>> getAllMeta(){
        return authService.getAllMeta();
    }

}
