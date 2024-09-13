package DavexCenter.module.MQ;


import DavexBase.common.Body;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/center/rabbitMQ")
public class CenterPublishController {
    @Autowired
    CenterPublishService centerPublishService;

    /**
     * 创建 application-queue 并绑定到 application-exchange
     */
    @PostMapping("/createFanoutQueue")
    public Body<String> createFanoutQueue(@RequestParam("queueName") String queueName,
                              @RequestParam("exchangeName") String exchangeName) {

        // 创建队列并绑定到交换机
       return centerPublishService.addQueueAndBindToFanout(queueName, exchangeName);
    }

    @PostMapping("/createDirectQueue")
    public Body<String> createDirectQueue(@RequestParam("queueName") String queueName,
                                @RequestParam("exchangeName") String exchangeName,
                              @RequestParam("routingKey") String routingKey) {
        // 创建队列并绑定到交换机
        return centerPublishService.addQueueAndBindToDirect(queueName,exchangeName,routingKey);
    }

    @PostMapping("/createTopicQueue")
    public Body<String> createTopicQueue(@RequestParam("queueName") String queueName,
                                    @RequestParam("exchangeName") String exchangeName,
                                    @RequestParam("routingKey") String routingKey) {

        // 创建队列并绑定到交换机
        return  centerPublishService.addQueueAndBindToTopic(queueName,exchangeName,routingKey);
    }

    /**
     * 发布消息到 application-queue
     */
    @PostMapping("/publishMessageToFanout")
    public Body<String> publishMessageToFanout(@RequestParam("exchangeName")String exchangeName,
                                               @RequestParam("message") String message) {
        // 发布消息到指定交换机和路由键
        Body<String> response = centerPublishService.publishMessageToFanout(exchangeName, message);
        return response;
    }

    @PostMapping("/publishMessageToDirect")
    public Body<String> publishMessageToDirect(@RequestParam("exchangeName")String exchangeName,
                                               @RequestParam("routingKey")String routingKey,
                                               @RequestParam("message") String message) {
        // 发布消息到指定交换机和路由键
        Body<String> response = centerPublishService.publishMessageToDirect(exchangeName,routingKey,message);
        return response;
    }

    @PostMapping("/publishMessageToTopic")
    public Body<String> publishMessageToTopic(@RequestParam("exchangeName")String exchangeName,
                                              @RequestParam("routingKey")String routingKey,
                                              @RequestParam("message") String message) {
        // 发布消息到指定交换机和路由键
        Body<String> response = centerPublishService.publishMessageToTopic(exchangeName,routingKey,message);
        return response;
    }

    @PostMapping("/getExchangeList")
    public Body<List<String>> getExchangeList(){
        return centerPublishService.listExchanges();
    }

    @PostMapping("/getQueue")
    public Body<List<String>> getQueue(@RequestParam("exchangeName") String exchangeName){
        return centerPublishService.listExchangeQueues(exchangeName);
    }

}
