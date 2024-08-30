package DavexCenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import DavexBase.info.Parameter;
import DavexBase.serializer.ParameterDeserializer;
import DavexBase.serializer.ParameterSerializer;

@Configuration
public class JacksonConfig {

    @Bean
    public Module parameterModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(new ParameterSerializer());
        module.addDeserializer(Parameter.class, new ParameterDeserializer());
        return module;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(parameterModule());
        // JSON支持注释
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        // JSON支持尾逗号
        mapper.configure(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature(), true);
        return mapper;
    }
}
