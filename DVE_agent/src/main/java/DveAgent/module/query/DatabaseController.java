package DveAgent.module.query;


import DveBase.common.Body;
import DveBase.entity.OutsideDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // @RestController的作用等同于@Controller + @ResponseBody。
// 相当于@Controller+@ResponseBody两个注解的结合，返回json数据不需要在方法前面加@ResponseBody注解了，但使用@RestController这个注解，就不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RequestMapping("/query/database")
public class DatabaseController {
    @Autowired
    DatabaseService databaseService;

    @PostMapping("/addDatabase")
    public Body<String> addDatabase(@RequestBody OutsideDatabase outsideDatabase){
        return databaseService.addDatabase(outsideDatabase);
    }
}
