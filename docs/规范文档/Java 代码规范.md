
<a name="uya0Z"></a>
## 一、编程环境
<a name="ueu3R"></a>
### 1、 代码编译器
统一使用IntelliJ IDEA，最好用的java代码编译器，没有之一

<a name="judWq"></a>
### 2、代码生成器
推荐在IntelliJ IDEA 安装GitHub Copilot，安装后即可通过编写注释来生成大部分的代码，既提高编程效率，又保证代码可读性。<br />GitHub Copilot的申请请参考：[https://zhuanlan.zhihu.com/p/578964972](https://zhuanlan.zhihu.com/p/578964972)

<a name="slide-0"></a>
## 二、编程规范
<a name="slide-1"></a>
### 1、基本原则
通过所有测试：每次修改完代码后，请运行并通过所有的测试<br />尽可能消除重复：重复的代码会导致在进行代码修改时需要进行多处修改，容易遗漏导致Bug<br />尽可能少地使用语法糖：语法糖虽然能让你的代码看起来简洁，但是容易让人看不懂。好的java代码应该能够让不熟悉java特性的人也能轻松读懂。


<a name="slide-2"></a>
### 2、命名规范
项目名称使用UpperCamelCase 风格，缩写部分则全部大写。<br />例如：`FudanDDE `<br />类名称同样使用UpperCamelCase 风格，缩写部分则全部大写。<br />例如：`class OrderService `<br />方法名称、参数名、变量名使用lowerCamelCase 风格<br />例如：`localValue / getHttpMessage() / inputUserId`<br />常量名称使用下划线+大写字母命名<br />例如：`NUMBER_OF_THREAD `
<a name="slide-3"></a>
### 3、TODO/FIXME 规范
`TODO/TBD(to be determined)` 注释一般用来描述已知待改进、待补充的修改点,并且加上作者名称。  `FIXME` 注释一般用来描述已知缺陷，它们都应该有统一风格，方便文本搜索统一处理。如：<br />// TODO <author-name>: 补充XX处理<br />// FIXME <author-name>: XX缺陷
<a name="slide-4"></a>
### 4、方法参数规范
无论是 `controller，service，manager，dao` 亦或是其他的代码，每个方法最多 `3` 个参数，如果超出 `3` 个参数的话，要封装成 `javabean` 对象。  

1. 方便他人调用，降低出错几率。尤其是当参数是同一种类型，仅仅依靠顺序区分，稍有不慎便是灾难性后果，而且排查起来也极其恶心。  
2. 保持代码整洁、清晰度。当一个个方法里充斥着一堆堆参数的时候，再坚强的人，也会身心疲惫。

反例：

```java
/**
* 使用证书加密数据工具方法
*
* @param param
* @param password 加密密码
* @param priCert 私钥
* @param pubCert 公钥
* @return 返回加密后的字符串
*/
public String signEnvelop(JdRequestParam param, String password, String priCert, String pubCert){}
```
<a name="slide-5"></a>
### 5、注释规范
<a name="slide-6"></a>
#### 5-1、注释和代码一样重要
注释除了说明作用、逻辑之外。还有一个很重要的原因：当业务逻辑过于复杂，代码过于庞大的时候，注释就变成了一道道美化环境、分离与整理逻辑思路的路标。这是很重要的一点，它能有效得帮助我们免于陷入代码与业务逻辑的泥沼之中。<br />正例：
```java
/**
* 开始抽奖方法
* 保存中奖信息、奖励用户积分等
* @param luckDrawDTO
* @return ResponseDTO 返回中奖信息
*/
public ResponseDTO<String> startLuckDraw(LuckDrawDTO luckDrawDTO) {
 
    // -------------- 1、校验抽奖活动基本信息 ------------------------
    xxx伪代码一顿操作
 
    // -------------- 2、新增抽奖记录 -------------------------------
    xxx伪代码一顿操作
 
    // -------------- 3、如果需要消耗积分，则扣除钢镚积分 -------------
    xxx伪代码一顿操作
 
    // -------------- 4、获取奖品信息，开始翻滚吧 --------------------
    xxx伪代码一顿操作
 
    return ResponseDTO.succ(luckDrawPrizeVO);
}
```
<a name="slide-7"></a>
#### 5-2、注释和代码的一致性
注释并不是越多越好，当注释过多，维护代码的同时，还需要维护注释，不仅变成了一种负担，也与我们当初添加注释的初衷背道而驰。  <br />首先：大家应该通过清晰的逻辑架构，好的变量命名来提高代码可读性；需要的时候，才辅以注释说明。注释是为了帮助阅读者快速读懂代码，所以要从读者的角度出发，按需注释。注释内容要简洁、明了、无二义性，信息全面且不冗余。<br />其次：无论是修改、复制代码时，都要仔细核对注释内容是否正确。只改代码，不改注释是一种不文明行为，破坏了代码与注释的一致性，会让阅读者迷惑、费解，甚至误解。<br />反例：
```java
// 查询部门
EmployeeDTO employee = employeeDao.listByDeptId(deptId);
```
<a name="slide-8"></a>
#### 5-3、方法注释
方法要尽量通过方法名自解释，不要写无用、信息冗余的方法头，不要写空有格式的方法头注释。<br />方法头注释内容可选，但不限于：功能说明、返回值，用法、算法实现等等。尤其是对外的方法接口声明，其注释，应当将重要、有用的信息表达清楚。<br />正例：

```java
/**
 * 解析转换时间字符串为 LocalDate 时间类
 * 调用前必须校验字符串格式 否则可能造成解析失败的错误异常
 *
 * @param dateStr 必须是 yyyy-MM-dd 格式的字符串
 * @return LocalDate
 */
public static LocalDate parseYMD(String dateStr){}
```
反例：
```java
/**
 * 校验对象
 *
 * @param t
 * @return String
 */
public static <T> String checkObj(T t);
```
反例中出现的问题：
```
方法注释没有说明具体的作用、使用事项。
参数、返回值，空有格式没内容。这是非常重要一点，任何人调用任何方法之前都需要知道方法对参数的要求，以及返回值是什么。
```
<a name="slide-9"></a>
## 三、项目规范
<a name="slide-10"></a>
### 1、代码目录结构
统一的目录结构是所有项目的基础。<br />src                               源码目录<br />|-- common                            各个项目的通用类库<br />|-- config                            项目的配置信息<br />|-- constant                          全局公共常量<br />|-- handler                           全局处理器<br />|-- interceptor                       全局连接器<br />|-- listener                          全局监听器<br />|-- module                            各个业务<br />|-- |--- employee                         员工模块<br />|-- |--- role                             角色模块<br />|-- |--- login                            登录模块<br />|-- third                             三方服务，比如redis, oss，微信sdk等等<br />|-- util                              全局工具类<br />|-- Application.java                  启动类
<a name="slide-11"></a>
### 2、common 目录规范
common 目录用于存放各个项目通用的项目，但是又可以依照项目进行特定的修改。<br />src 源码目录<br />|-- common 各个项目的通用类库<br />|-- |--- anno          通用注解，比如权限，登录等等<br />|-- |--- constant      通用常量，比如 ResponseCodeConst<br />|-- |--- domain        全局的 javabean，比如 BaseEntity,PageParamDTO 等<br />|-- |--- exception     全局异常，如 BusinessException<br />|-- |--- json          json 类库，如 LongJsonDeserializer，LongJsonSerializer<br />|-- |--- swagger       swagger 文档<br />|-- |--- validator     适合各个项目的通用 validator，如 CheckEnum，CheckBigDecimal 等
<a name="slide-12"></a>
### 3、config 目录规范
config 目录用于存放各个项目通用的项目，但是又可以依照项目进行特定的修改。<br />src                               源码目录<br />|-- config                            项目的所有配置信息<br />|-- |--- MvcConfig                    mvc的相关配置，如interceptor,filter等<br />|-- |--- DataSourceConfig             数据库连接池的配置<br />|-- |--- MybatisConfig                mybatis的配置<br />|-- |--- ....                         其他
<a name="slide-13"></a>
### 4、module 目录规范
module 目录里写项目的各个业务，每个业务一个独立的顶级文件夹，在文件里进行 mvc 的相关划分。其中，domain 包里存放 entity, dto, vo，bo 等 javabean 对象<br />src<br />|-- module                         所有业务模块<br />|-- |-- role                          角色模块<br />|-- |-- |--RoleController.java              controller<br />|-- |-- |--RoleConst.java                   role相关的常量<br />|-- |-- |--RoleService.java                 service<br />|-- |-- |--RoleDao.java                     dao<br />|-- |-- |--domain                           domain<br />|-- |-- |-- |-- RoleEntity.java                  表对应实体<br />|-- |-- |-- |-- RoleDTO.java                     dto对象<br />|-- |-- |-- |-- RoleVO.java                      返回对象<br />|-- |-- employee                      员工模块<br />|-- |-- login                         登录模块<br />|-- |-- email                         邮件模块<br />|-- |-- ....                          其他
<a name="slide-14"></a>
### 5、 domain 包中的 javabean 命名规范
1） `javabean` 的整体要求：
```
不得有任何的业务逻辑或者计算
基本数据类型必须使用包装类型（Integer, Double、Boolean 等）
不允许有任何的默认值
每个属性必须添加注释，并且必须使用多行注释。
必须使用 lombok 简化 getter/setter 方法
建议对象使用 lombok 的 @Builder ，@AllArgsConstructor，同时使用这两个注解，简化对象构造方法以及set方法。
```
正例：
```java
@Builder
@NoArgsConstructor
@Data
public class DemoDTO {
 
    private String name;
     
    private Integer age;
}
```
 
```java
// 使用示例：
 
DemoDTO demo = DemoDTO.builder()
                .name("yeqiu")
                .age(66)
                .build();
```
 <br />2）数据对象；`XxxxEntity`，要求：
```
以 Entity 为结尾（阿里是为 DO 为结尾）
Xxxx 与数据库表名保持一致
类中字段要与数据库字段保持一致，不能缺失或者多余
类中的每个字段添加注释，并与数据库注释保持一致
不允许有组合
项目内的日期类型必须统一，建议使用 java.util.Date
```
3）传输对象；`XxxxDTO`，要求：
```
不可以继承自 Entity
DTO 可以继承、组合其他 DTO，VO，BO 等对象
DTO 只能用于前端、RPC 的请求参数
```
3）视图对象；`XxxxVO`，要求：
```
不可继承自 Entity
VO 可以继承、组合其他 DTO，VO，BO 等对象
VO 只能用于返回前端、rpc 的业务数据封装对象
```
4）业务对象 `BO`，要求：
```
不可以继承自 Entity
BO 对象只能用于 service，manager，dao 层，不得用于 controller 层
```
<a name="slide-15"></a>
## 四、MVC 规范
<a name="slide-16"></a>
### 1、整体分层
```
controller 层
service 层
manager 层
dao 层
```
<a name="slide-17"></a>
### 2、 `controller` 层规范
1） 只允许在 method 上添加 `RequestMapping` 注解，不允许加在 class 上（为了方便的查找 url，放到  class不能一次性查找出来）<br />正例：

```java
@RestController
public class DepartmentController {
 
    @GetMapping("/department/list")
    public ResponseDTO<List<DepartmentVO>> listDepartment() {
        return departmentService.listDepartment();
    }
}
```
反例：
```java
@RequestMapping ("/department")
public class DepartmentController {
 
    @GetMapping("/list")
    public ResponseDTO<List<DepartmentVO>> listDepartment() {
        return departmentService.listDepartment();
    }
}
```
2）使用 /业务模块/子模块/动作 命名 url：
> _虽然 Rest 大法好，但是有时并不能一眼根据 url 看出来是什么操作_

正例：

```
GET  /department/get/{id}      查询某个部门详细信息
GET /department/query         复杂查询
POST /department/add           添加部门
POST /department/update        更新部门
POST  /department/delete/{id}   删除部门
```
3）每个方法必须添加 `swagger` 文档注解 `@ApiOperation` ，并填写接口描述信息，描述最后必须加上作者信息 `@author 哪吒` 。<br />正例：

```java
@ApiOperation("更新部门信息 @author 哪吒")
@PostMapping("/department/update")
public ResponseDTO<String> updateDepartment(@Valid @RequestBody DeptUpdateDTO deptUpdateDTO) {
    return departmentService.updateDepartment(deptUpdateDTO);
}
```

4）controller 负责协同和委派业务，充当路由的角色，每个方法要保持简洁：

- 不做任何的业务逻辑操作
- 不做任何的参数、业务校验，参数校验只允许使用@Valid 注解做简单的校验
- 不做任何的数据组合、拼装、赋值等操作

正例：<br />    
```java
@ApiOperation("添加部门 @author 哪吒")
@PostMapping("/department/add")
public ResponseDTO<String> addDepartment(@Valid @RequestBody DepartmentCreateDTO departmentCreateDTO) {
    return departmentService.addDepartment(departmentCreateDTO);
}
```
5）只能在 `controller` 层获取当前请求用户，并传递给 `service` 层。
> _因为获取当前请求用户是从 ThreadLocal 里获取取的，在 service、manager、dao 层极有可能是其他非 request 线程调用，会出现 null 的情况，尽量避免_

    
```java
@ApiOperation("添加员工 @author yandanyang")
@PostMapping("/employee/add")
public ResponseDTO<String> addEmployee(@Valid @RequestBody EmployeeAddDTO employeeAddDTO) {
    LoginTokenBO requestToken = SmartRequestTokenUtil.getRequestUser();
    return employeeService.addEmployee(employeeAddDTO, requestToken);
}
```
<a name="slide-18"></a>
### 3、 `service` 层规范
1）合理拆分 service 文件，如果业务较大，请拆分为多个 service。<br />如订单业务,所有业务都写到 OrderService 中会导致文件过大，故需要进行拆分如下：
```
OrderQueryService 订单查询业务
OrderCreateService 订单新建业务
OrderDeliverService 订单发货业务
OrderValidatorService 订单验证业务
```


2）service是具体的业务处理逻辑服务层，尽量避免将web层某些参数传递到service中。  <br />反例：
```java
public ResponseDTO<String> handlePinganRequest(HttpServletRequest request){
    InputStreamReader inputStreamReader = new InputStreamReader(request.getInputStream(), "GBK");
    BufferedReader reader = new BufferedReader(inputStreamReader);
    StringBuilder sb = new StringBuilder();
    String str;
    while ((str = reader.readLine()) != null) {
        sb.append(str);
    }
    if(!JSON.isValid(msg)){
      return ResponseDTO.wrap(ResponseCodeConst.ERROR_PARAM);
    }
    PinganMsgDTO PinganMsgDTO = JSON.parseObject(msg,PinganMsgDTO.class);
    // 示例结束
}
```
 <br />反例中出现的问题：

- 反例中把 `HttpServletRequest` 传递到service中，是为了获取Request流中的字符信息，然后才是真正的业务处理。按照分层的初衷：将代码、业务逻辑解耦，正确的做法应该是`handlePinganRequest`方法将`String`字符作为参数直接处理业务，将从`Request`中获取字符的操作放入`controller`中。
- 另一个坏处是不方便做单元测试，还得一个`new`一个`HttpServletRequest`并制造一个`InputStream`，然而这样做并不能模拟到真实的业务情景及数据。

<a name="slide-20"></a>
### 4、boolean类型的属性命名规范
> 类中布尔类型的变量，都不要加is，否则部分框架解析会引起序列化错误。反例：定义为基本数据类型 Boolean isDeleted；的属性，它的方法也是 isDeleted()，RPC在反向解析的时候，“以为”对应的属性名称是 deleted，导致属性获取不到，进而抛出异常。

`boolean` 类型的类属性和数据表字段都统一使用 `flag` 结尾。虽然使用 `isDeleted` 从字面语义上更直观，但是比起可能出现的潜在错误，这点牺牲还是值得的。<br />正例：<br />deletedFlag, onlineFlag
<a name="slide-22"></a>
## 五、数据库规范
<a name="slide-23"></a>
### 1 建表规范
表必备三字段：id, create_time, update_time
```java
id 字段 Long 类型，单表自增，自增长度为 1
create_time 字段 datetime 类型，默认值 CURRENT_TIMESTAMP
update_time 字段 datetime 类型，默认值 CURRENT_TIMESTAMP, On update CURRENT_TIMESTAMP
```
<a name="slide-24"></a>
### 2 枚举类表字段注释需要将所有枚举含义进行注释
修改或增加字段的状态描述，必须要及时同步更新注释。  如下表的 `sync_status` 字段 `同步状态 0 未开始 1同步中 2同步成功 3失败`。<br />正例：
```java
CREATE TABLE `t_change_data` (
     `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
     `sync_status` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0' COMMENT '同步状态 0 未开始 1同步中 2同步成功 3失败',
     `sync_time` DATETIME NULL DEFAULT NULL COMMENT '同步时间',
     `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
     `update_time` DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
     PRIMARY KEY (`change_data_id`)
)
```
反例：
```java
CREATE TABLE `t_change_data` (
     `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
     `sync_status` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0' COMMENT '同步状态 ',
     `sync_time` DATETIME NULL DEFAULT NULL COMMENT '同步时间',
     `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
     `update_time` DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
     PRIMARY KEY (`change_data_id`)
)
```
<a name="slide-25"></a>
### 
<a name="slide-26"></a>
## 六、其他
<a name="slide-27"></a>
### 1、代码提交规范
```
提交前应该冷静、仔细检查一下，确保没有加入不应该提交的文件。
提交前应该先编译一次（idea里ctrl+F9），防止出现编译都报错的情况。
提交前先更新pull一次代码，提交前发生冲突要比提交后发生冲突容易解决的多。
提交前检查代码是否格式化，是否符合代码规范，无用的包引入、变量是否清除等等。
提交时检查注释是否准确简洁的表达出了本次提交的内容。
```
<a name="slide-28"></a>
### 2、保持项目整洁
使用git，必须添加 .gitignore 忽略配置文件。  不要提交与项目无关的内容文件：idea配置、target包等。
