package DveAgent.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import DveAgent.info.CompileParameter;

import java.io.IOException;

public class CompileParameterDeserializer extends StdDeserializer<CompileParameter> {

    public CompileParameterDeserializer() {
        this(null);
    }

    public CompileParameterDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public CompileParameter deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String name = node.get("name").asText();
        String description = node.get("description").asText();
        CompileParameter.CompileParameterType parameterType = CompileParameter.CompileParameterType
                .valueOf(node.get("parameterType").asText());
        Object value = null;

        if (parameterType == CompileParameter.CompileParameterType.ARG) {
            value = node.get("value").intValue();
        } else if (parameterType == CompileParameter.CompileParameterType.FLAG) {
            value = node.get("value").asText();
        }
        Boolean required = node.get("required").asBoolean();

        return new CompileParameter(name, parameterType, value, description, required);
    }
}
