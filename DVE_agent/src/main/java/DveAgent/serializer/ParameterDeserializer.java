package DveAgent.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import DveAgent.info.Parameter;
import DveAgent.info.Parameter.ENUMLimit;
import DveAgent.info.Parameter.NUMLimit;
import DveAgent.info.Parameter.STRINGLimit;

import java.io.IOException;

public class ParameterDeserializer extends StdDeserializer<Parameter> {

    public ParameterDeserializer() {
        this(null);
    }

    public ParameterDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Parameter deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        ObjectMapper mapper = new ObjectMapper();
        String name = node.get("name").asText();
        String description = node.get("description").asText();
        Parameter.ArgumentsType parameterType = Parameter.ArgumentsType
                .valueOf(node.get("parameterType").asText());
        Object posORflag = null;

        if (parameterType == Parameter.ArgumentsType.POS) {
            posORflag = node.get("posORflag").intValue();
        } else if (parameterType == Parameter.ArgumentsType.FLAG) {
            posORflag = node.get("posORflag").asText();
        }

        Parameter.LimitType limitType = Parameter.LimitType
                .valueOf(node.get("limitType").asText());
        Object limit = null;

        switch (limitType) {
            case NUM:
                limit = mapper.treeToValue(node.get("limit"), NUMLimit.class);
                break;
            case ENUM:
                limit = mapper.treeToValue(node.get("limit"), ENUMLimit.class);
                break;
            case STRING:
            default:
                limit = mapper.treeToValue(node.get("limit"), STRINGLimit.class);
                break;
        }

        Boolean required = node.get("required").asBoolean();
        Boolean auto = node.get("auto").asBoolean();
        return new Parameter(name, parameterType, limitType, posORflag, limit, description, required, auto);
    }
}
