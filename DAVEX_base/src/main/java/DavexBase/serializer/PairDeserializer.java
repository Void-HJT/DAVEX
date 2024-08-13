package DavexBase.serializer;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;


public class PairDeserializer extends JsonDeserializer<Pair<Long, Long>> {
    @Override
    public Pair<Long, Long> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        p.nextToken(); // Start array
        Long left = p.getLongValue();
        p.nextToken(); // Next element
        Long right = p.getLongValue();
        p.nextToken(); // End array
        return Pair.of(left, right);
    }
}
