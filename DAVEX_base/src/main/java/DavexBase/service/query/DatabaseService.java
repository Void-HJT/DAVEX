package DavexBase.service.query;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import DavexBase.common.R;
import DavexBase.mapper.AgentMapper;
import DavexBase.service.auth.CenterWebClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;

import DavexBase.common.ExternalDatabaseProperties;
import DavexBase.service.auth.AgentWebClientService;
import DavexBase.common.Body;
import DavexBase.entity.OutsideDatabase;
import DavexBase.entity.OutsideDatabaseTable;
import DavexBase.info.QueryRequest;
import DavexBase.mapper.DatabaseMapper;
import DavexBase.mapper.DatabaseTableMapper;
import org.springframework.web.reactive.function.BodyInserters;

@Service
public class DatabaseService {

    @Autowired
    private DatabaseMapper databaseMapper;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private DatabaseTableMapper databaseTableMapper;

    @Autowired
    private AgentWebClientService agentWebClientService;

    @Autowired
    private CenterWebClientService centerWebClientService;

    @Autowired
    private ExternalDatabaseProperties externalDatabasePropertiesBean;


    public Body<String> addDatabase(OutsideDatabase database) {

        databaseMapper.insert(database);
        populateDatabaseTables(database);
        return Body.success("","test");
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
                ResultSet tables = metaData.getTables(catalog, null, "%", new String[]{"TABLE"});
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
                .eq(OutsideDatabaseTable::getOutsideDatabaseId,databaseId);
        List<OutsideDatabaseTable> outsideDatabaseTables = databaseTableMapper.selectList(queryWrapper);
        return Body.success(outsideDatabaseTables,"返回成功");
    }

    public Body<byte[]> executeQuery(QueryRequest request, Long databaseId) {
        String sql = buildSqlFromRequest(request);
        System.out.println(sql);
        LambdaQueryWrapper<OutsideDatabase> queryWrapper = Wrappers.<OutsideDatabase>lambdaQuery()
                .eq(OutsideDatabase::getUid,databaseId);
        OutsideDatabase database = databaseMapper.selectOne(queryWrapper);

        Optional<ExternalDatabaseProperties.DatabaseConfig> dbConfig = getExternalDatabaseConfig(database.getName());
        if (dbConfig.isPresent()) {
            try (Connection connection = DriverManager.getConnection(
                    dbConfig.get().getUrl(), dbConfig.get().getUsername(), dbConfig.get().getPassword());
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {

                // 获取结果集的元数据
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                // 处理结果集，将查询结果存入List<Map<String, Object>>中
                List<Map<String, Object>> results = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        row.put(columnName, resultSet.getObject(i));
                    }
                    results.add(row);
                }

                //将结果序列化为JSON
                ObjectMapper mapper = new ObjectMapper();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    mapper.writeValue(out, results);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                byte[] jsonData = out.toByteArray();

                // 返回查询文件
                return Body.success(jsonData,"查询成功");

            } catch (SQLException e) {
                e.printStackTrace();
                return Body.error("Error executing query: " + e.getMessage());
            }
        }
        else{
            return Body.error("External database configuration not found");
        }
    }

    public Body<String> query2Agent(QueryRequest request, Long applicationId, Long agentId, Long databaseId) {

        try {
            Body<byte[]> response = centerWebClientService.center2AgentWebClient(agentId).post()
                    .uri(uriBuilder -> uriBuilder.path("/query/database/locateQuery")
                            .queryParam("databaseId", databaseId)
                            .build())
                    .bodyValue(request)  // 将请求体设置为QueryRequest
                    .retrieve()  // 准备接收响应
                    .bodyToMono(new ParameterizedTypeReference<Body<byte[]>>() {})  // 指定返回类型
                    .block();  // 阻塞等待响应并获取结果

            // 从Body对象中提取data
            byte[] jsonData = response.getData();

            //构建文件名
            String fileName = "application_"+applicationId+"_"+"database_"+databaseId+"_"+"table_"+request.getTableName()+"_query_results.json";
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

                agentWebClientService.agent2CenterWebClient(1).post()
                        .uri(UriBuilder -> UriBuilder.path("/queryFile/saveQuery").queryParam("hash", hash).queryParam("applicationId",applicationId).build())
                        .contentType(MediaType.MULTIPART_FORM_DATA).body(BodyInserters.fromMultipartData(multipartBody))
                        .retrieve().bodyToMono(new ParameterizedTypeReference<R<String>>() {
                        }).block();
            } catch (Exception e) {
                e.printStackTrace();
                return Body.error("发送失败");
            }
            // 返回封装的结果
            String result = new String(multipartFile.getBytes(), StandardCharsets.UTF_8);
            return Body.success("发送成功"+result);
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error("Query failed due to exception: " + e.getMessage());
        }
    }

    private String buildSqlFromRequest(QueryRequest request) {
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
                            .append(operator).append(" '")
                            .append(conditionValue).append("' AND ");
                } else {
                    sql.append("`").append(column).append("` = '")
                            .append(value).append("' AND ");
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
            sql.append(" LIMIT ").append(request.getLimit());
        }

        if (request.getOffset() != null) {
            sql.append(" OFFSET ").append(request.getOffset());
        }

        return sql.toString();
    }

    public Body<List<OutsideDatabase>> getDatabase() {
        List<OutsideDatabase> outsideDatabases = databaseMapper.selectList(null);
        return Body.success(outsideDatabases,"");
    }
}


