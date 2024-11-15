package DavexAgent.module.auth.controller;


import DavexBase.common.AuthTokenCache;
import DavexBase.common.R;
import DavexBase.entity.Keycloak;
import DavexBase.entity.KeycloakCredentials;
import DavexBase.service.auth.KeycloakService;
import DavexBase.service.auth.TokenValidationService;
import DavexBase.info.TokenResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    TokenValidationService tokenValidationService;

    @Autowired
    KeycloakService keycloakService;

    @Autowired
    AuthTokenCache authTokenCache;

    @PostMapping("/getToken")
    public TokenResult getToken(@RequestParam String username,
                                @RequestParam String password,
                                @RequestParam String authId){
        return tokenValidationService.getToken(username,password,authId);
    }

    @PostMapping("/isTokenExpired")
    public boolean isTokenExpired(@RequestParam String authId){
        return tokenValidationService.isTokenExpired(authId);
    }

    @PostMapping("/updatePublicKey")
    public void updatePublicKey(@RequestParam String username,
                                @RequestParam String password,
                                @RequestParam String authId){
        try {
            tokenValidationService.updatePublicKey(username,password,authId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/getTokenFromCache")
    public TokenResult getTokenFromCache(@RequestParam("KeycloakUrl") String KeycloakUrl)
    {
        TokenResult tokenResult = authTokenCache.getToken(KeycloakUrl);
        return tokenResult;
    }

    @PostMapping("/deleteCache")
    public boolean deleteCache(){
        authTokenCache.clearCache();
        return true;
    }

    @PostMapping("/addKeycloak")
    public R<String> addKeycloak(@RequestBody Keycloak keycloak){
        return keycloakService.addKeycloak(keycloak);
    }

    @PostMapping("/updateKeycloak")
    public R<String> updateKeycloak(@RequestBody Keycloak keycloak){
        return keycloakService.updateKeycloak(keycloak);
    }

    @PostMapping("/deleteKeycloak")
    public R<String> deleteKeycloak(@RequestParam("authId") String authId){
        return keycloakService.deleteKeycloak(authId);
    }

    @PostMapping("/getKeycloak")
    public R<Keycloak> getKeycloak(@RequestParam("authId") String authId){
        return keycloakService.getKeycloak(authId);
    }

    @PostMapping("/addKeycloakCredentials")
    public R<String> addKeycloakCredentials(@RequestBody KeycloakCredentials keycloakCredentials){
        return keycloakService.addKeycloakCredentials(keycloakCredentials);
    }

    @PostMapping("/updateKeycloakCredentials")
    public R<String> updateKeycloakCredentials(@RequestBody KeycloakCredentials keycloakCredentials){
        return keycloakService.updateKeycloakCredentials(keycloakCredentials);
    }

    @PostMapping("/deleteKeycloakCredentials")
    public R<String> deleteKeycloakCredentials(@RequestParam("targetId") String targetId){
        return keycloakService.deleteKeycloakCredentials(targetId);
    }

    @PostMapping("/getKeycloakCredentials")
    public R<KeycloakCredentials> getKeycloakCredentials(@RequestParam("targetId") String targetId){
        return keycloakService.getKeycloakCredentials(targetId);
    }


}
