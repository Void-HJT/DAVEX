package DavexCenter.module.test;

import DavexBase.common.Body;
import DavexBase.common.R;
import DavexBase.service.auth.CenterWebClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController // @RestController的作用等同于@Controller + @ResponseBody。
// 相当于@Controller+@ResponseBody两个注解的结合，返回json数据不需要在方法前面加@ResponseBody注解了，但使用@RestController这个注解，就不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RequestMapping("/test")
public class TestController {
    @Autowired
    CenterWebClientService centerWebClientService;

    @RequestMapping("/hello")
    public String hello() {
        return "hello swagger";
    }

    @PostMapping("/message")
    public R<?> messaage() {
        return R.success("123", "123");
    }

    @PostMapping("/sendMessage")
    public String sendMessage(@RequestParam("target_id") String target_id,
                              @RequestParam("username") String username,
                              @RequestParam("password") String password,
                              @RequestParam("authId")String authId){

        WebClient webclient = null;
        try {
            webclient = centerWebClientService.center2AgentWebClientInAuth(target_id,username,password,authId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String ans = webclient.post()
                .uri(uriBuilder -> uriBuilder.path("/test/hello").build())
                .retrieve()
                .bodyToMono(String.class).block();
        return  ans;
    }
}
