package DavexBase.service.query;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

import DavexBase.common.*;
import DavexBase.service.blockchain.UpChainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;

import DavexBase.common.Body;
import DavexBase.common.ExternalDatabaseProperties;
import DavexBase.common.R;
import DavexBase.entity.OutsideDatabase;
import DavexBase.entity.OutsideDatabaseTable;
import DavexBase.info.QueryRequest;
import DavexBase.mapper.DatabaseMapper;
import DavexBase.mapper.DatabaseTableMapper;
import DavexBase.service.auth.AgentWebClientService;
import DavexBase.service.auth.CenterWebClientService;

import static DavexBase.common.UUIDGenerator.generateUUID;

@Service
public class DatabaseService {

    @Autowired
    private DatabaseMapper databaseMapper;

    @Autowired
    private DatabaseTableMapper databaseTableMapper;

    @Autowired
    private AgentWebClientService agentWebClientService;

    @Autowired
    private CenterWebClientService centerWebClientService;

    @Autowired
    private ExternalDatabaseProperties externalDatabasePropertiesBean;

    @Autowired
    private My my;

    @Autowired
    private UpChainService upChainService;

    // 定义 SQL 注入关键词和模式
    private static final String[] SQL_INJECTION_KEYWORDS = { "DROP", "DELETE", "UNION", "SLEEP", "--", "' OR '1'='1" };
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile("([';]+|(--)+)", Pattern.CASE_INSENSITIVE);

    public Body<String> addDatabase(OutsideDatabase database) {

        databaseMapper.insert(database);
        populateDatabaseTables(database);
        return Body.success("", "test");
    }

    public void populateDatabaseTables(OutsideDatabase database) {
        Optional<ExternalDatabaseProperties.DatabaseConfig> dbConfig = getExternalDatabaseConfig(database.getName());

        if (dbConfig.isPresent()) {
            try (Connection connection = DriverManager.getConnection(
                    dbConfig.get().getUrl(), dbConfig.get().getUsername(), dbConfig.get().getPassword())) {

                DatabaseMetaData metaData = connection.getMetaData();

                // 指定要查询的数据库名称
                String catalog = connection.getCatalog();
                System.out.println(catalog);

                // 获取所有表
                ResultSet tables = metaData.getTables(catalog, null, "%", new String[] { "TABLE" });
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    String tableDescription = tables.getString("REMARKS");

                    // 获取表结构信息
                    ResultSet columns = metaData.getColumns(null, null, tableName, "%");
                    JSONArray schemaArray = new JSONArray();
                    while (columns.next()) {
                        JSONObject columnObject = new JSONObject();
                        columnObject.put("column_name", columns.getString("COLUMN_NAME"));
                        columnObject.put("column_type", columns.getString("TYPE_NAME"));
                        columnObject.put("column_size", columns.getInt("COLUMN_SIZE"));
                        schemaArray.add(columnObject);
                    }

                    OutsideDatabaseTable databaseTable = new OutsideDatabaseTable();
                    databaseTable.setAgentId(database.getAgentId());
                    databaseTable.setOutsideDatabaseId(database.getUid());
                    databaseTable.setName(tableName);
                    databaseTable.setDescription(tableDescription);

                    // 将JSONArray转换为字符串
                    String jsonString = schemaArray.toString();
                    // 移除所有反斜杠
                    String formattedJsonString = jsonString.replace("\\", "");

                    databaseTable.setSchemaExample(formattedJsonString);
                    databaseTable.setExample(""); // 示例数据根据需要填写

                    databaseTableMapper.insert(databaseTable);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // 处理异常
            }
        }
    }

    private Optional<ExternalDatabaseProperties.DatabaseConfig> getExternalDatabaseConfig(String dbName) {
        return externalDatabasePropertiesBean.getDatabases().stream()
                .filter(db -> db.getName().equalsIgnoreCase(dbName))
                .findFirst();
    }

    public Body<List<OutsideDatabaseTable>> getTable(Long databaseId) {
        LambdaQueryWrapper<OutsideDatabaseTable> queryWrapper = Wrappers.<OutsideDatabaseTable>lambdaQuery()
                .eq(OutsideDatabaseTable::getOutsideDatabaseId, databaseId);
        List<OutsideDatabaseTable> outsideDatabaseTables = databaseTableMapper.selectList(queryWrapper);
        return Body.success(outsideDatabaseTables, "返回成功");
    }

    public Body<byte[]> executeQuery(QueryRequest request, Long databaseId, Boolean chainMaker, String requestHash, String requestId) throws Exception {
        // 遍历请求中的所有条件并检查是否存在 SQL 注入
        for (Object condition : request.getConditions().values()) {
            if (condition instanceof String && isSqlInjectionSuspected((String) condition)) {
                return Body.error("查询指令存在不合法内容，可能包含注入攻击。");
            }
        }

        List<Object> params = new ArrayList<>();
        String sql = buildSqlFromRequest(request, params);  // 调用改进后的 buildSqlFromRequest 方法
        System.out.println("Generated SQL: " + sql);  // 调试输出生成的 SQL 语句

        // 获取数据库配置
        LambdaQueryWrapper<OutsideDatabase> queryWrapper = Wrappers.<OutsideDatabase>lambdaQuery()
                .eq(OutsideDatabase::getUid, databaseId);
        OutsideDatabase database = databaseMapper.selectOne(queryWrapper);

        Optional<ExternalDatabaseProperties.DatabaseConfig> dbConfig = getExternalDatabaseConfig(database.getName());
        if (dbConfig.isPresent()) {
            try (Connection connection = DriverManager.getConnection(
                    dbConfig.get().getUrl(), dbConfig.get().getUsername(), dbConfig.get().getPassword());
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                // 设置参数
                for (int i = 0; i < params.size(); i++) {
                    preparedStatement.setObject(i + 1, params.get(i));
                }

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    // 获取结果集的元数据
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    // 将查询结果存入 List<Map<String, Object>> 中
                    List<Map<String, Object>> results = new ArrayList<>();
                    while (resultSet.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            row.put(columnName, resultSet.getObject(i));
                        }
                        results.add(row);
                    }

                    // 将结果序列化为 JSON
                    ObjectMapper mapper = new ObjectMapper();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    mapper.writeValue(out, results);
                    byte[] jsonData = out.toByteArray();

                    //========================上链模块========================
//                    if(chainMaker){
//                        String responseMsgJson = new String(jsonData, StandardCharsets.UTF_8);
//                        //生成responseID
//                        String responseId = generateUUID("response", "query", my.getId());
//                        //将响应进行上链操作
//                        ContractResponse responseResponse = upChainService.responseUpChain(requestHash,responseMsgJson,responseId,requestId,my.getId(),"agent");
//                    }
                    //========================上链结束========================

                    // 返回查询结果
                    return Body.success(jsonData, "查询成功");

                } catch (IOException e) {
                    e.printStackTrace();
                    return Body.error("Error serializing results: " + e.getMessage());
                }

            } catch (SQLException e) {
                if (isSqlInjectionError(e)) {
                    return Body.error("查询指令存在不合法内容，可能包含注入攻击。");
                } else {
                    e.printStackTrace();
                    return Body.error("Error executing query: " + e.getMessage());
                }
            }

        } else {
            return Body.error("External database configuration not found");
        }
    }

    public Body<String> query2Agent(QueryRequest request, String applicationId, String agentId, Long databaseId) {

        try {
            //========================上链模块：将查询请求进行上链操作========================
//            String centerId = my.getId();
//            String requestId = generateUUID("request", "query", centerId);
//            String requestMsg = "{" +
//                    "\"application\": \"" + applicationId + "\", " +
//                    "\"center\": \"" + centerId + "\", " +
//                    "\"database\": \"" + databaseId + "\", " +
//                    "\"agentId\": \"" + agentId + "\"" +
//                    "}";
//            String fileDescription = centerId + "has a query task related to the external database"+ databaseId + "that the " + agentId + "is connected to.";
//
//            ContractResponse respectResponse = upChainService.requestUpChain(requestId,fileDescription,requestMsg,centerId,"center");
//            Map<String, Object> resultMap = (Map<String, Object>) respectResponse.getData();
//            String requestHash = resultMap.get("sharing_setting_hash").toString();
            //========================上链模块结束========================

            Body<byte[]> response = centerWebClientService.center2AgentWebClient(agentId).post()
                    .uri(uriBuilder -> uriBuilder.path("/query/database/locateQuery")
                            .queryParam("databaseId", databaseId)
                            //========================上链时需要传递的参数========================
//                            .queryParam("chainMaker", true)
//                            .queryParam("requestHash", requestHash)
//                            .queryParam("requestId", requestId)
                            //========================上链时需要传递的参数结束========================
                            .build())
                    .bodyValue(request) // 将请求体设置为QueryRequest
                    .retrieve() // 准备接收响应
                    .bodyToMono(new ParameterizedTypeReference<Body<byte[]>>() {
                    }) // 指定返回类型
                    .block(); // 阻塞等待响应并获取结果

            // 从Body对象中提取data
            byte[] jsonData = response.getData();

            // 构建文件名
            String fileName = "application_" + applicationId + "_" + "database_" + databaseId + "_" + "table_"
                    + request.getTableName() + "_query_results.json";
            MultipartFile multipartFile = new MockMultipartFile("file", fileName, "application/json", jsonData);

            String hash;
            try {
                // 获取文件的byte信息
                byte[] uploadBytes = multipartFile.getBytes();
                // 拿到一个SHA-256转换器
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                byte[] digest = sha256.digest(uploadBytes);
                hash = new BigInteger(1, digest).toString(16);
            } catch (Exception e) {
                return Body.error("文件计算hash失败" + e.getMessage());
            }
            // 发送文件
            try {
                MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
                multipartBody.add("file", multipartFile.getResource()); // 这里的 "file" 是服务端期望的文件字段名


                String centerId = my.getId();
                R<String> res = agentWebClientService.agent2CenterWebClient(centerId).post()
                        .uri(UriBuilder -> UriBuilder.path("/queryFile/saveQuery").queryParam("hash", hash)
                                .queryParam("applicationId", applicationId).build())
                        .contentType(MediaType.MULTIPART_FORM_DATA).body(BodyInserters.fromMultipartData(multipartBody))
                        .retrieve().bodyToMono(new ParameterizedTypeReference<R<String>>() {
                        }).block();
                // 记录结果消息
                String content;
                if (res.getBody().getCode() == 1) {
                    content = String.format("查询任务完成\n代理: %s\n数据库id: %s\n表名: %s",
                            agentId, databaseId, request.getTableName());
                } else {
                    content = String.format("查询任务完成\n代理: %s\n数据库id: %s\n表名: %s\n错误信息: %s",
                            agentId, databaseId, request.getTableName(), res.getBody().getMessage());
                }
                agentWebClientService.agent2CenterWebClient(centerId).post()
                        .uri(uriBuilder -> uriBuilder.path("/notification/set")
                                .queryParam("appID", applicationId)
                                .queryParam("title", "查询任务结束")
                                .queryParam("content", content)
                                .queryParam("taskID", "")
                                .queryParam("code", res.getBody().getCode())
                                .queryParam("type", "query").build())
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<String>() {
                        }).block();
            } catch (Exception e) {
                e.printStackTrace();
                return Body.error("发送失败");
            }
            // 返回封装的结果
            String result = new String(multipartFile.getBytes(), StandardCharsets.UTF_8);
            return Body.success("发送成功" + result);
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error("Query failed due to exception: " + e.getMessage());
        }
    }

    //修改后避免直接拼接SQL语句
    private String buildSqlFromRequest(QueryRequest request, List<Object> params) {
        StringBuilder sql = new StringBuilder("SELECT ");

        // 选择要查询的列
        if (request.getColumns() == null || request.getColumns().isEmpty()) {
            sql.append("*");
        } else {
            sql.append(String.join(", ", request.getColumns()));
        }

        sql.append(" FROM `").append(request.getTableName()).append("`");

        // 添加查询条件
        if (request.getConditions() != null && !request.getConditions().isEmpty()) {
            sql.append(" WHERE ");
            request.getConditions().forEach((column, value) -> {
                if (value instanceof Map) {
                    Map<String, Object> conditionMap = (Map<String, Object>) value;
                    String operator = (String) conditionMap.get("operator");
                    Object conditionValue = conditionMap.get("value");
                    sql.append("`").append(column).append("` ")
                            .append(operator).append(" ? AND ");
                    params.add(conditionValue); // 添加参数
                } else {
                    sql.append("`").append(column).append("` = ? AND ");
                    params.add(value); // 添加参数
                }
            });
            // 移除最后一个 " AND "
            sql.setLength(sql.length() - 5);
        }

        // 添加排序条件
        if (request.getOrderBy() != null && !request.getOrderBy().isEmpty()) {
            sql.append(" ORDER BY ");
            request.getOrderBy().forEach((column, order) -> {
                sql.append("`").append(column).append("` ").append(order).append(", ");
            });
            // 移除最后一个 ", "
            sql.setLength(sql.length() - 2);
        }

        // 添加分页条件
        if (request.getLimit() != null) {
            sql.append(" LIMIT ?");
            params.add(request.getLimit());
        }

        if (request.getOffset() != null) {
            sql.append(" OFFSET ?");
            params.add(request.getOffset());
        }

        return sql.toString();
    }

    // 检查是否存在 SQL 注入的可疑内容
    private boolean isSqlInjectionSuspected(String input) {
        // 关键词检查
        for (String keyword : SQL_INJECTION_KEYWORDS) {
            if (input.toUpperCase().contains(keyword)) {
                return true;
            }
        }
        // 正则表达式检查
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            return true;
        }
        return false;
    }

    // 检查 SQLException 是否可能由 SQL 注入引起
    private boolean isSqlInjectionError(SQLException e) {
        String message = e.getMessage().toLowerCase();
        return message.contains("syntax error") || message.contains("you have an error in your sql syntax");
    }

    public Body<List<OutsideDatabase>> getDatabase() {
        List<OutsideDatabase> outsideDatabases = databaseMapper.selectList(null);
        return Body.success(outsideDatabases, "");
    }
}
