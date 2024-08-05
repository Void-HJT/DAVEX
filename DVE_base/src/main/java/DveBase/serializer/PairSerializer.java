package DveBase.serializer;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PairSerializer extends JsonSerializer<Pair<Long, Long>> {
    @Override
    public void serialize(Pair<Long, Long> pair, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartArray();
        gen.writeNumber(pair.getLeft());
        gen.writeNumber(pair.getRight());
        gen.writeEndArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Pair<Long, Long>> handledType() {
        return (Class<Pair<Long, Long>>) (Class<?>) Pair.class;
    }
}
