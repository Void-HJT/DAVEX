package DavexCenter.module.auth.service;

import DavexBase.common.AuthTokenCache;
import DavexBase.entity.Keycloak;
import DavexBase.info.TokenResult;
import DavexBase.mapper.KeycloakMapper;
import DavexBase.service.auth.KeycloakService;
import DavexBase.service.auth.TokenValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakAdminService {

    @Value("${my.keycloak_username}")
    private String adminUsername;

    @Value("${my.keycloak_password}")
    private String adminPassword;

    @Value("${my.keycloak_id}")
    private String keycloakId;

    @Autowired
    private AuthTokenCache authTokenCache;

    @Autowired
    private KeycloakMapper keycloakMapper;

    @Autowired
    private TokenValidationService tokenValidationService;

    public boolean addUser(String newUsername, String newPassword) throws Exception {
        Keycloak keycloak = keycloakMapper.selectById(keycloakId);
        if(keycloak==null){
            throw new RuntimeException("keycloak no found");
        }

        TokenResult adminToken = authTokenCache.getToken("admin");
        if (adminToken==null){
            adminToken = tokenValidationService.getToken(adminUsername,adminPassword,"admin");
        }
        RestTemplate restTemplate = new RestTemplate();
        String addUserUrl = keycloak.getServerUrl() + "/admin/realms/" + keycloak.getRealm() + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> userRequest = new HashMap<>();
        userRequest.put("username", newUsername);
        userRequest.put("enabled", true);
        userRequest.put("credentials", List.of(Map.of("type", "password", "value", newPassword, "temporary", false)));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userRequest, headers);
        ResponseEntity<Void> response = restTemplate.postForEntity(addUserUrl, request, Void.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            return true;
        } else {
            throw new RuntimeException("Failed to add user: " + response.getStatusCode());
        }
    }

    public boolean deleteUser(String username) throws Exception {
        Keycloak keycloak = keycloakMapper.selectById(keycloakId);
        if (keycloak == null) {
            throw new RuntimeException("Keycloak configuration not found");
        }

        // 获取管理员Token
        TokenResult adminToken = authTokenCache.getToken("admin");
        if (adminToken == null) {
            adminToken = tokenValidationService.getToken(adminUsername, adminPassword, "admin");
        }

        RestTemplate restTemplate = new RestTemplate();

        // 查询用户ID
        String findUserUrl = keycloak.getServerUrl() + "/admin/realms/" + keycloak.getRealm() + "/users?username=" + username;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken.getAccessToken());

        HttpEntity<Void> findUserRequest = new HttpEntity<>(headers);
        ResponseEntity<List> findUserResponse = restTemplate.exchange(findUserUrl, HttpMethod.GET, findUserRequest, List.class);

        if (findUserResponse.getStatusCode() != HttpStatus.OK || findUserResponse.getBody() == null || findUserResponse.getBody().isEmpty()) {
            throw new RuntimeException("User not found: " + username);
        }

        // 假设查询返回的用户列表中第一个是匹配的用户
        Map<String, Object> user = (Map<String, Object>) findUserResponse.getBody().get(0);
        String userId = (String) user.get("id");

        // 删除用户
        String deleteUserUrl = keycloak.getServerUrl() + "/admin/realms/" + keycloak.getRealm() + "/users/" + userId;
        HttpEntity<Void> deleteUserRequest = new HttpEntity<>(headers);
        ResponseEntity<Void> deleteUserResponse = restTemplate.exchange(deleteUserUrl, HttpMethod.DELETE, deleteUserRequest, Void.class);

        if (deleteUserResponse.getStatusCode() == HttpStatus.NO_CONTENT) {
            return true;
        } else {
            throw new RuntimeException("Failed to delete user: " + username);
        }
    }



}
