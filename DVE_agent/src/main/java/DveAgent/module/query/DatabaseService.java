package DveAgent.module.query;


import DveAgent.config.ExternalDatabaseProperties;
import DveBase.common.Body;
import DveBase.entity.OutsideDatabase;
import DveBase.entity.OutsideDatabaseTable;
import DveBase.info.QueryRequest;
import DveBase.mapper.DatabaseMapper;
import DveBase.mapper.DatabaseTableMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;


import java.sql.*;
import java.util.*;

@Service
public class DatabaseService {

    @Autowired
    private DatabaseMapper databaseMapper;

    @Autowired
    private DatabaseTableMapper databaseTableMapper;

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

                // 获取所有表
                ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
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
                    databaseTable.setSchemaExample(schemaArray.toString());
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

    public String executeQuery(QueryRequest request,Long databaseId) {
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
                //返回
                return results.toString();

            } catch (SQLException e) {
                e.printStackTrace();
                // 处理异常，返回错误信息或抛出自定义异常
                return "Error executing query: " + e.getMessage();
            }
        }
        else{
            return "External database configuration not found.";
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
                sql.append("`").append(column).append("` = '").append(value).append("' AND ");
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


