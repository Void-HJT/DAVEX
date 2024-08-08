package DveAgent.module.query;


import DveAgent.config.ExternalDatabaseProperties;
import DveBase.common.Body;
import DveBase.entity.OutsideDatabase;
import DveBase.entity.OutsideDatabaseTable;
import DveBase.mapper.DatabaseMapper;
import DveBase.mapper.DatabaseTableMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.Optional;

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
}


