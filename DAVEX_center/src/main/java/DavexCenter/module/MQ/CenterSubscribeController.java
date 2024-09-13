package DavexCenter.module.MQ;


import DavexBase.common.Body;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController // @RestController的作用等同于@Controller + @ResponseBody。
// 相当于@Controller+@ResponseBody两个注解的结合，返回json数据不需要在方法前面加@ResponseBody注解了，但使用@RestController这个注解，就不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RequestMapping("/center/rabbitMQ")
public class CenterSubscribeController {

    class AgentMessageListener {
        public void handleMessage(String message) {
            System.out.println("Received message: " + message);
        }
    }

    @Autowired
    CenterSubscribeService centerSubscribeService;

    @PostMapping("/subscribe")
    public Body<String> subscribe(@RequestParam("centerId") String centerId,
                                  @RequestParam("queueName") String queueName,
                                  @RequestParam("routingKey") String routingKey){
        return centerSubscribeService.subscribeToCenter(centerId, queueName, routingKey, new AgentMessageListener());
    }

    @PostMapping("/unsubscribe")
    public Body<String> unsubscribe(@RequestParam("queueName") String queueName){
        return centerSubscribeService.unsubscribe(queueName);
    }

}
