package DveAgent.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import DveAgent.info.Parameter;

public class ParameterSerializer extends StdSerializer<Parameter> {

    public ParameterSerializer() {
        this(null);
    }

    public ParameterSerializer(Class<Parameter> t) {
        super(t);
    }

    @Override
    public void serialize(Parameter parameter, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();
        gen.writeStringField("name", parameter.getName());
        switch (parameter.getParameterType()) {
            case POS:
                gen.writeStringField("parameterType", "POS");
                gen.writeNumberField("posORflag", (int) parameter.getPosORflag());
                break;
            case FLAG:
            default:
                gen.writeStringField("parameterType", "FLAG");
                gen.writeStringField("posORflag", (String) parameter.getPosORflag());
                break;
        }
        switch (parameter.getLimitType()) {
            case NUM:
                gen.writeStringField("limitType", "NUM");
                gen.writeObjectField("limit", (Parameter.NUMLimit<?>) parameter.getLimit());
                break;
            case STRING:
            default:
                gen.writeStringField("limitType", "STRING");
                gen.writeObjectField("limit", (Parameter.STRINGLimit) parameter.getLimit());
                break;
            case ENUM:
                gen.writeStringField("limitType", "ENUM");
                gen.writeObjectField("limit", (Parameter.ENUMLimit) parameter.getLimit());
                break;
        }
        gen.writeBooleanField("required", parameter.getRequired());
        gen.writeStringField("description", parameter.getDescription());
        gen.writeEndObject();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Parameter> handledType() {
        return (Class<Parameter>) (Class<?>) Parameter.class;
    }
}
