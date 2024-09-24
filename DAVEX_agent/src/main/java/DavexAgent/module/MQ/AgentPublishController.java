package DavexAgent.module.MQ;

import DavexBase.common.Body;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/agent/rabbitMQ")
public class AgentPublishController {
    @Autowired
    AgentPublishService agentPublishService;

    @PostMapping("/createQueue")
    public Body<String> createQueue(@RequestParam("centerId") String centerId,
                                    @RequestParam("exchangeName") String exchangeName,
                                    @RequestParam("queueName") String queueName){
        return agentPublishService.createAndBindFanoutExchange(centerId,exchangeName,queueName);
    }

    @PostMapping("/sendMessage")
    public Body<String> sendMessage(@RequestParam("centerId") String centerId,
                                    @RequestParam("exchangeName") String exchangeName,
                                    @RequestParam("message") String message){
        return agentPublishService.sendMessageToFanoutExchange(centerId,exchangeName,message);
    }

    @PostMapping("/destroyConnection")
    public Body<String> destroyConnection(@RequestParam("queueName") String queueName){
        return  agentPublishService.destroyConnection(queueName);
    }

}
