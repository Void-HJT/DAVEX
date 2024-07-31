package DveAgent.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import DveAgent.info.CompileParameter;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CompileParameterListTypeHandler extends BaseTypeHandler<List<CompileParameter>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(CompileParameter.class, new CompileParameterDeserializer());
        objectMapper.registerModule(module);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<CompileParameter> parameters, JdbcType jdbcType)
            throws SQLException {
        try {
            String jsonString = objectMapper.writeValueAsString(parameters);
            ps.setString(i, jsonString);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting List<Parameter> to JSON", e);
        }
    }

    @Override
    public List<CompileParameter> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String jsonString = rs.getString(columnName);
        return parseParameters(jsonString);
    }

    @Override
    public List<CompileParameter> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String jsonString = rs.getString(columnIndex);
        return parseParameters(jsonString);
    }

    @Override
    public List<CompileParameter> getNullableResult(java.sql.CallableStatement cs, int columnIndex)
            throws SQLException {
        String jsonString = cs.getString(columnIndex);
        return parseParameters(jsonString);
    }

    private List<CompileParameter> parseParameters(String jsonString) throws SQLException {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, new TypeReference<List<CompileParameter>>() {
            });
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting JSON to List<Parameter>", e);
        }
    }
}
