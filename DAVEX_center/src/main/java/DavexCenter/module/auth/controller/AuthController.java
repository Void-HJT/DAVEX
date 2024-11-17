package DavexCenter.module.auth.controller;


import DavexBase.common.AuthTokenCache;
import DavexBase.common.R;
import DavexBase.entity.Keycloak;
import DavexBase.entity.KeycloakCredentials;
import DavexBase.info.TokenResult;
import DavexBase.service.auth.KeycloakService;
import DavexBase.service.auth.TokenValidationService;
import DavexCenter.module.auth.service.KeycloakAdminService;
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
    KeycloakAdminService keycloakAdminService;

    @Autowired
    AuthTokenCache authTokenCache;

    @PostMapping("/getToken")
    public TokenResult getToken(@RequestParam("username") String username,
                                @RequestParam("password") String password,
                                @RequestParam("authId") String authId){
        return tokenValidationService.getToken(username,password,authId);
    }

    @PostMapping("/isTokenExpired")
    public boolean isTokenExpired(@RequestParam("authId") String authId){
        return tokenValidationService.isTokenExpired(authId);
    }

    @PostMapping("/updatePublicKey")
    public boolean updatePublicKey(@RequestParam("username") String username,
                                   @RequestParam("password") String password,
                                   @RequestParam("authId") String authId){
        try {
            return tokenValidationService.updatePublicKey(username,password,authId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/logout")
    public boolean logout(@RequestParam("authId") String authId){
        try {
            return tokenValidationService.logout(authId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/getTokenFromCache")
    public TokenResult getTokenFromCache(@RequestParam("keycloakUrl") String keycloakUrl)
    {
        TokenResult tokenResult = authTokenCache.getToken(keycloakUrl);
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

    @PostMapping("/addUser")
    public boolean addUser(@RequestParam("username") String username,
                           @RequestParam("password") String password){
        try {
            return keycloakAdminService.addUser(username,password);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/deleteUser")
    public boolean deleteUser(@RequestParam("username")String username){
        try {
            return keycloakAdminService.deleteUser(username);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



}


