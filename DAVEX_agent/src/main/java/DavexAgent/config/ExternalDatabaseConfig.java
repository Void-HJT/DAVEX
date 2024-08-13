package DavexAgent.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class ExternalDatabaseConfig {

    @Bean
    public ExternalDatabaseProperties externalDatabasePropertiesBean() throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(ExternalDatabaseProperties.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        ClassPathResource resource = new ClassPathResource("external-databases.xml");
        try (InputStream is = resource.getInputStream()) {
            return (ExternalDatabaseProperties) unmarshaller.unmarshal(is);
        }
    }
}
