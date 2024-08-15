package DavexCenter.module.query.controller;

import DavexBase.common.Body;
import DavexBase.entity.OutsideDatabase;
import DavexBase.service.auth.CenterWebClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

@RestController // @RestController的作用等同于@Controller + @ResponseBody。
// 相当于@Controller+@ResponseBody两个注解的结合，返回json数据不需要在方法前面加@ResponseBody注解了，但使用@RestController这个注解，就不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RequestMapping("/query/query")
public class QueryController {
    @Autowired
    CenterWebClientService centerWebClientService;

    @PostMapping("/getDatabase")
    public Body<List<OutsideDatabase>> getDatabase(@RequestParam("agentId") Long agentId){
        List<OutsideDatabase> ans = new LinkedList<>();

        try {
            return   centerWebClientService.center2AgentWebClient(agentId)
                    .post()
                    .uri(UriBuilder -> UriBuilder.path("/query/database/getDatabase").build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Body<List<OutsideDatabase>>>() {})
                    .block();
        } catch (Exception e) {
            return Body.error(ans, e.getLocalizedMessage());
        }
    }
}
