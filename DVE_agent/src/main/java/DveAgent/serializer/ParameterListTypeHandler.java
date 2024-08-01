package DveAgent.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import DveAgent.info.Parameter;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ParameterListTypeHandler extends BaseTypeHandler<List<Parameter>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Parameter.class, new ParameterDeserializer());
        objectMapper.registerModule(module);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Parameter> parameters, JdbcType jdbcType)
            throws SQLException {
        try {
            String jsonString = objectMapper.writeValueAsString(parameters);
            ps.setString(i, jsonString);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting List<Parameter> to JSON", e);
        }
    }

    @Override
    public List<Parameter> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String jsonString = rs.getString(columnName);
        return parseParameters(jsonString);
    }

    @Override
    public List<Parameter> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String jsonString = rs.getString(columnIndex);
        return parseParameters(jsonString);
    }

    @Override
    public List<Parameter> getNullableResult(java.sql.CallableStatement cs, int columnIndex)
            throws SQLException {
        String jsonString = cs.getString(columnIndex);
        return parseParameters(jsonString);
    }

    private List<Parameter> parseParameters(String jsonString) throws SQLException {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, new TypeReference<List<Parameter>>() {
            });
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting JSON to List<Parameter>", e);
        }
    }
}
