package DveAgent.module.test;

import DveAgent.common.R;
import DveAgent.module.auth.service.AgentWebClientService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.bind.annotation.RequestBody;

@RestController // @RestController的作用等同于@Controller + @ResponseBody。
// 相当于@Controller+@ResponseBody两个注解的结合，返回json数据不需要在方法前面加@ResponseBody注解了，但使用@RestController这个注解，就不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RequestMapping("/test")
public class TestController {

    private AgentWebClientService webClientService;

    public TestController(AgentWebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @RequestMapping("/hello")
    public String hello() {
        return "hello swagger";
    }

    @PostMapping("/message")
    public R<?> message() {
        return R.success("123", "123");
    }

    @RequestMapping("webClient")
    public String postMethodName() {
        try {
            WebClient webClient = webClientService.agent2CenterWebClient(2);
            return webClient.get().uri("/test/hello").retrieve().bodyToMono(String.class).block();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

}