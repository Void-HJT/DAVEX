package DavexAgent.module.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import DavexAgent.module.task.service.MpcService;
import DavexBase.common.R;
import DavexBase.entity.Mpc;
import DavexBase.mapper.FileMapper;
import DavexBase.mapper.MpcMapper;
import DavexBase.service.auth.AgentWebClientService;
import DavexBase.service.directory.FileFolderService;

@RestController // @RestController的作用等同于@Controller + @ResponseBody。
// 相当于@Controller+@ResponseBody两个注解的结合，返回json数据不需要在方法前面加@ResponseBody注解了，但使用@RestController这个注解，就不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RequestMapping("/test")
public class TestController {

    private AgentWebClientService webClientService;

    @Autowired
    FileMapper fileMapper;

    @Autowired
    FileFolderService fileFolderService;

    @Autowired
    private MpcMapper mpcMapper;

    @Autowired
    MpcService mpcService;

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


    @PostMapping("/json")
    public Mpc postMethodName(@RequestBody Mpc entity) throws Exception {

        mpcMapper.insert(entity);
        return entity;
    }

}