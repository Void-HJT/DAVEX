package DveAgent.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import DveAgent.info.CompileParameter;

public class CompileParameterSerializer extends StdSerializer<CompileParameter> {

    public CompileParameterSerializer() {
        this(null);
    }

    public CompileParameterSerializer(Class<CompileParameter> t) {
        super(t);
    }

    @Override
    public void serialize(CompileParameter parameter, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();
        gen.writeStringField("name", parameter.getName());
        if (parameter.getParameterType() == CompileParameter.CompileParameterType.ARG) {
            gen.writeStringField("parameterType", "ARG");
            gen.writeNumberField("value", (Integer) parameter.getValue());
        } else if (parameter.getParameterType() == CompileParameter.CompileParameterType.FLAG) {
            gen.writeStringField("parameterType", "FLAG");
            gen.writeStringField("value", (String) parameter.getValue());
        }
        gen.writeBooleanField("required", parameter.getRequired());
        gen.writeStringField("description", parameter.getDescription());
        gen.writeEndObject();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<CompileParameter> handledType() {
        return (Class<CompileParameter>) (Class<?>) CompileParameter.class;
    }
}
