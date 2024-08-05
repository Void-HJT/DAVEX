package DveAgent.common;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import DveBase.common.My;
import DveBase.entity.Agent;

@Component
public class MyAgent extends My {

    private Agent agent;

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    @Override
    public void init() throws Exception {
        super.init();
        agent = new Agent();
        agent.setUid(id);
        agent.setName(name);
        agent.setIp(ip);
        agent.setPort(port);
        agent.setDescription(description);
        agent.setLastUpdated(LocalDateTime.now());
    }
}
