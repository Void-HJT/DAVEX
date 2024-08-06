package DveCenter.common;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import DveBase.common.My;
import DveBase.entity.Center;

@Component
public class MyCenter extends My {

    private Center center;

    @PostConstruct
    public void init() throws Exception {
        center = new Center();
        center.setUid(id);
        center.setName(name);
        center.setIp(ip);
        center.setPort(port);
        center.setDescription(description);
        center.setLastUpdated(LocalDateTime.now());
    }

    public Center getCenter() {
        return center;
    }

    public void setCenter(Center center) {
        this.center = center;
    }

}
