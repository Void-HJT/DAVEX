package DavexCenter.module.MQ;

import DavexBase.common.Body;
import DavexBase.service.MQ.PublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/center/rabbitMQ")
public class PublishController {
    @Autowired
    PublishService publishService;

    @PostMapping("/createQueue")
    public Body<String> createQueue(@RequestParam("centerId") String centerId,
                                    @RequestParam("exchangeName") String exchangeName,
                                    @RequestParam("queueName") String queueName){
        return publishService.createAndBindFanoutExchange(centerId,exchangeName,queueName);
    }

    @PostMapping("/sendMessage")
    public Body<String> sendMessage(@RequestParam("centerId") String centerId,
                                    @RequestParam("exchangeName") String exchangeName,
                                    @RequestParam("message") String message){
        return publishService.sendMessageToFanoutExchange(centerId,exchangeName,message);
    }

    @PostMapping("/destroyConnection")
    public Body<String> destroyConnection(@RequestParam("queueName") String queueName){
        return  publishService.destroyConnection(queueName);
    }

}
