package DavexBase.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "garnet")
public class GarnetProperties {
    private boolean enabled;
    private String containerID;
    private String inputPath;
    private String outputPath;
    private String mpcPath;

}
