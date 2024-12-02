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

import java.util.ArrayList;
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

    //添加用户
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

    //删除用户
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

    //列出用户
    public List<Map<String, Object>> getAllUsersWithRoles() throws Exception {
        Keycloak keycloak = keycloakMapper.selectById(keycloakId);
        if (keycloak == null) {
            throw new RuntimeException("Keycloak configuration not found");
        }

        // 获取管理员Token
        TokenResult adminToken = authTokenCache.getToken("admin");
        if (adminToken == null) {
            adminToken = tokenValidationService.getToken(adminUsername, adminPassword, "admin");
        }
        else {
            //验证该token是否过期
            String isExpired = tokenValidationService.checkTokenStatus("admin");
            if(!isExpired.equals("Token is valid")){
                if (isExpired.equals("Token expired but Refresh Token is valid"))
                {
                    //如果refreshToken没过期则更新
                    try {
                        tokenValidationService.updateToken("admin");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if(isExpired.equals("Token expired and Refresh Token is invalid"))
                {
                    //如果过期重新申请
                    adminToken = tokenValidationService.getToken(adminUsername, adminPassword, "admin");
                }
            }
        }

        RestTemplate restTemplate = new RestTemplate();

        // 获取用户列表
        String usersUrl = keycloak.getServerUrl() + "/admin/realms/" + keycloak.getRealm() + "/users";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken.getAccessToken());

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<List> usersResponse = restTemplate.exchange(usersUrl, HttpMethod.GET, request, List.class);

        if (usersResponse.getStatusCode() != HttpStatus.OK || usersResponse.getBody() == null) {
            throw new RuntimeException("Failed to retrieve users list");
        }

        List<Map<String, Object>> users = usersResponse.getBody();
        List<Map<String, Object>> usersWithRoles = new ArrayList<>();

        // 遍历用户列表，获取每个用户的角色
        for (Map<String, Object> user : users) {
            String userId = (String) user.get("id");
            String username = (String) user.get("username");

            // 获取用户的所有角色（直接角色和客户端角色）
            String roleMappingsUrl = keycloak.getServerUrl() + "/admin/realms/" + keycloak.getRealm() + "/users/" + userId + "/role-mappings";
            ResponseEntity<Map> roleMappingsResponse = restTemplate.exchange(roleMappingsUrl, HttpMethod.GET, request, Map.class);

            if (roleMappingsResponse.getStatusCode() != HttpStatus.OK || roleMappingsResponse.getBody() == null) {
                throw new RuntimeException("Failed to retrieve role mappings for user: " + username);
            }

            Map<String, Object> roleMappings = roleMappingsResponse.getBody();

            // 收集所有角色
            List<String> roles = new ArrayList<>();

            // Realm级角色
            if (roleMappings.containsKey("realmMappings")) {
                List<Map<String, Object>> realmMappings = (List<Map<String, Object>>) roleMappings.get("realmMappings");
                for (Map<String, Object> realmRole : realmMappings) {
                    roles.add((String) realmRole.get("name"));
                }
            }

            // 客户端角色（如果需要，可以进一步处理）
            if (roleMappings.containsKey("clientMappings")) {
                Map<String, Map<String, Object>> clientMappings = (Map<String, Map<String, Object>>) roleMappings.get("clientMappings");
                for (Map.Entry<String, Map<String, Object>> entry : clientMappings.entrySet()) {
                    String clientId = entry.getKey();
                    List<Map<String, Object>> clientRoles = (List<Map<String, Object>>) entry.getValue().get("mappings");
                    for (Map<String, Object> clientRole : clientRoles) {
                        roles.add(clientId + ":" + clientRole.get("name"));
                    }
                }
            }

            // 构造用户与角色的映射信息
            Map<String, Object> userWithRoles = new HashMap<>();
            userWithRoles.put("username", username);
            userWithRoles.put("roles", roles);

            usersWithRoles.add(userWithRoles);
        }

        return usersWithRoles;
    }



}
