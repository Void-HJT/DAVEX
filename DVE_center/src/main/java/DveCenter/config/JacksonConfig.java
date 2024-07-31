package DveCenter.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import DveAgent.info.CompileParameter;
import DveAgent.serializer.PairDeserializer;
import DveAgent.serializer.PairSerializer;
import DveAgent.serializer.CompileParameterDeserializer;
import DveAgent.serializer.CompileParameterSerializer;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Module pairModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(new PairSerializer());
        module.addDeserializer(Pair.class, new PairDeserializer());
        return module;
    }

    @Bean
    public Module parameterModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(new CompileParameterSerializer());
        module.addDeserializer(CompileParameter.class, new CompileParameterDeserializer());
        return module;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(pairModule());
        mapper.registerModule(parameterModule());
        return mapper;
    }
}
