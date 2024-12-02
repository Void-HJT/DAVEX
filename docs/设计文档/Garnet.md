# DAVEX 与 Garnet 集成设计文档

## Garnet 运行流程

隐私计算平台 [`Garnet`](https://github.com/FudanMPL/Garnet) 有两大组件：
1. 编译器组件：将类似于 `Python` 语言的 MPC 代码编译为可供虚拟机解析运行的指令字节码；
2. 虚拟机组件：运行 MPC 指令字节码的程序。

### MPC 的编译

以下是一个 MPC 文件代码的示例：

```Python
table0=sint.Matrix(int(program.args[1]), 1)
table1=sint.Matrix(int(program.args[2]), 1)

for x in table0:
    x.input_from(0) # 从第0方读入数据

for x in table1:
    x.input_from(1) # 从第1方读入数据

merge_table, n = ss_psi_merge(table0, table1)
for x in merge_table:
    print_ln_to(1,"%s",x.reveal_to(1)) # 输出到第1方
```

该 MPC 文件从两方读取数据，通过基于秘密共享的隐私集合求交协议（PSI），计算出两方文件的交集并将结果输出给第1方。

需要注意，MPC 文件在编译时就需要预先确定所有的内存分配，为此对于数据量不定的隐私计算任务，就需要在编译时将数据量的大小传入。假设上述的 PSI 协议两方的数据量分别为：300、500，那么可以通过下面的命令进行编译：

```shell
# 在 Garnet 根目录下
./compile.py /home/nhy/DAVEX/programs/PSI_BASE.mpc 300 500
```

编译成果后，可以在 `Garnet/Programs/Bytecode` 中看到编译好的字节文件 `PSI_BASE-300-500-0.bc`

### 虚拟机的编译

不同的虚拟机对相同的指令会调用不同的底层协议来执行。各个虚拟机所支持的指令、参与方个数、安全参数等都有所不同，需要根据运行场景的不同使用合适的虚拟机。

所有的虚拟机列表写在 `Garnet` 目录下的 `Makefile` 文件中，虚拟机是以 `.x` 作为后缀的二进制程序，可以通过 `make` 命令编译对应的虚拟机：

```shell
# 在 Garnet 根目录下
make semi2k-party.x
```

编译成功后，可以在 `Garnet` 根目录下找到编译好的二进制程序 `Garnet/semi2k-party.x`

### 输入数据的准备

`Garnet` 执行时的输入数据*必须*以特定的规则进行命名：

`<前缀>-P<参与方号>-<线程号>`

例如 `557daa32481d1eaab2af83a2a219c374-P0-0` 表示前缀为 `557daa32481d1eaab2af83a2a219c374` 的第*0*方用于第*0*个线程读取的数据。另外注意，前缀是可以包含目录的。

### 运行

在完成上述的准备后，就可以开始执行具体的任务了。我们已在 `semi2k-party.x` 虚拟机下运行的 PSI 协议为例说明：

```shell
# 在第0方的 Garnet 根目录下，第0方运行
./semi2k-party.x PSI_BASE-300-500 -h 10.176.34.171 -pn 6000 -N 2 -p 0 -IF ~/Garnet/Input/557daa32481d1eaab2af83a2a219c374 -OF ~/Garnet/Output/557daa32481d1eaab2af83a2a219c374
```

下面逐项解释：

- `semi2k-party.x`：使用的虚拟机；
- `PSI_BASE-300-500`：使用的已编译好的字节码，在编译时输入的参数要按顺序通过 `-` 连接在 MPC 文件名后；
- `-h`：协调方节点的地址，`Garnet` 也支持其他的地址方式，这里使用了较为简单的，第0方作为协调方的方法。其余地址方式请参看之前的文档；
- `-pn`：协调方节点的端口；
- `-N`：该任务由几方参与；
- `-p`：本方为第几方；
- `-IF`：输入文件前缀，只需输入前缀即可，不需要输入后面的`-P0-X`；
- `-OF`：输出文件前缀，最终的输出文件名也将按照输入文件的规则命名。

相对应的，第1方也要运行：

```shell
# 在第1方的 Garnet 根目录下，第1方运行
./semi2k-party.x PSI_BASE-300-500 -h 10.176.34.171 -pn 6000 -N 2 -p 1 -IF ~/Garnet/Input/557daa32481d1eaab2af83a2a219c374 -OF ~/Garnet/Output/557daa32481d1eaab2af83a2a219c374
```

输入文件前缀与输出文件前缀理论上并不需要统一，这里将前缀作为了任务号以此达成各方之间的同步。

参与的任务的各方，必须在*1分钟内*全部启动虚拟机，协调方会在指定端口等待连接，其余各方会尝试连接协调方的端口。各虚拟机若在启动1分钟后仍没有足够的参与方加入，会各自停止退出。

## DAVEX 集成

在目前的开发版本中，`DAVEX` 与 `Garnet` 均部署在同一台服务器中，通过使用 `ProcessBuilder` 近似命令行的方式使用 `Garnet`，还没有实现通过 Docker 跨容器访问。

### 配置

`DAVEX` 将 `Garnet` 的路径配置在了`application.yml` 中

```yaml
my:
  id: DAVEX-CXX171
  ip: 10.176.34.171
  name: 检察院
  description: 检察院
  garnet_path: /home/nhy/Garnet # HERE
```

### 数据结构

下面介绍 `DAVEX` 中与 `Garnet` 相关的数据结构。

#### MPC 

```sql
CREATE TABLE `mpc` (
    `uid` varchar(32) NOT NULL,
    `name` varchar(255) DEFAULT NULL,
    `compile_parameters` json DEFAULT NULL,
    `runtime_parameters` json DEFAULT NULL,
    `center_id` varchar(255) DEFAULT NULL,
    `path` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
```

```java
@Data
@TableName(autoResultMap = true)
public class Mpc {
    @TableId(type = IdType.ASSIGN_UUID)
    private String uid;
    private String name;
    @TableField(typeHandler = ParameterListTypeHandler.class)
    private List<Parameter> compileParameters;
    @TableField(typeHandler = ParameterListTypeHandler.class)
    private List<Parameter> runtimeParameters;
    private String centerId;
    @JsonIgnore
    private String path;
    public void setCompileParameters(List<Parameter> parameters) throws Exception {
        Set<Integer> s = new HashSet<>();
        for (Parameter parameter : parameters) {
            if (parameter.getParameterType() != Parameter.ArgumentsType.POS) {
                continue;
            }
            if (s.contains((Integer) parameter.getPosORflag())) {
                throw new Exception("参数重复");
            }
            s.add((Integer) parameter.getPosORflag());
        }
        for (int i = 0; i < s.size(); i++) {
            if (!s.contains(i)) {
                throw new Exception("参数缺失");
            }
        }
        this.compileParameters = parameters;
    }
}
```

上面各字段中具体解释下 `compileParameters` 与 `runtimeParameters`，其余字段含义比较明确。

##### Parameters
 
```java
public class Parameter {
    // 参数类型
    public enum ArgumentsType {
        // 位置参数
        POS,
        // 选项参数
        FLAG,
        // 超参数
        HYPER
    }

    public enum LimitType {
        NUM,
        ENUM,
        STRING,
        AUTO
    }

    public static class NUMLimit<T extends Number> {
        private T min;
        private T max;
        private T defaultValue;

        public NUMLimit() {
        }

        public NUMLimit(T min, T max) {
            this.min = min;
            this.max = max;
        }

        public T getMin() {
            return min;
        }

        public void setMin(T min) {
            this.min = min;
        }

        public T getMax() {
            return max;
        }

        public void setMax(T max) {
            this.max = max;
        }

        public T getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(T defaultValue) {
            this.defaultValue = defaultValue;
        }

    }

    public static class ENUMLimit {
        private String[] values;
        private String defaultValue;

        public ENUMLimit() {
        }

        public ENUMLimit(String[] values, String defaultValue) {
            if (!Arrays.asList(values).contains(defaultValue)) {
                throw new IllegalArgumentException("defaultValue must be in values");
            }
            this.values = values;
            this.defaultValue = defaultValue;
        }

        public String[] getValues() {
            return values;
        }

        public void setValues(String[] values) {
            this.values = values;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    public static class STRINGLimit {
        private String defaultValue;

        public STRINGLimit() {
        }

        public STRINGLimit(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    private String name;
    private ArgumentsType parameterType;
    // 表示位置参数的位置或者选项参数的选项；选项参数时，形如"-R"、"--name"；超参数时，忽略该值
    private Object posORflag;
    private LimitType limitType;
    private Object limit;
    private Boolean required;
    private String description;

    public Parameter() {
    }

    public Parameter(String name, ArgumentsType parameterType, LimitType limitType, Object posORflag,
            Object limit, String description,
            Boolean required) {
        this.name = name;
        this.parameterType = parameterType;
        setPosORflag(posORflag);
        this.limitType = limitType;
        setLimit(limit);
        this.required = required;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArgumentsType getParameterType() {
        return parameterType;
    }

    public void setParameterType(ArgumentsType parameterType) {
        this.parameterType = parameterType;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Object getPosORflag() {
        switch (parameterType) {
            case POS:
                return (Integer) posORflag;
            case FLAG:
            default:
                return (String) posORflag;
        }
    }

    public void setPosORflag(Object posORflag) {
        switch (parameterType) {
            case POS:
                if (posORflag instanceof Integer) {
                    this.posORflag = posORflag;
                } else {
                    throw new IllegalArgumentException("posORflag must be Integer:" + posORflag.toString());
                }
                break;
            case FLAG:
                if (posORflag instanceof String) {
                    this.posORflag = posORflag;
                } else {
                    throw new IllegalArgumentException("posORflag must be String:" + posORflag.toString());
                }
                break;
            case HYPER:
                if (posORflag != null) {
                    throw new IllegalArgumentException("posORflag must be null");
                }
                break;
        }
    }

    public Object getLimit() {
        return limit;
    }

    public void setLimit(Object limit) {
        switch (limitType) {
            case NUM:
                if (!(limit instanceof NUMLimit<?>)) {
                    throw new IllegalArgumentException("limit must be NUMLimit");
                }
                break;

            case ENUM:
                if (!(limit instanceof ENUMLimit)) {
                    throw new IllegalArgumentException("limit must be ENUMLimit");
                }
                break;
            case AUTO:
                if (limit != null) {
                    throw new IllegalArgumentException("limit must be null");
                }
                break;
            case STRING:
            default:
                if (!(limit instanceof STRINGLimit)) {
                    throw new IllegalArgumentException("limit must be STRINGLimit");
                }
                break;
        }
        this.limit = limit;
    }

    public LimitType getLimitType() {
        return limitType;
    }

    public void setLimitType(LimitType limitType) {
        this.limitType = limitType;
    }

    public String getDefaultValue() {
        switch (limitType) {
            case NUM:
                return ((NUMLimit<?>) limit).getDefaultValue().toString();
            case ENUM:
                return ((ENUMLimit) limit).getDefaultValue();
            case STRING:
                return ((STRINGLimit) limit).getDefaultValue();
            default:
            case AUTO:
                return null;
        }
    }
}
``` 

`Parameters` 类，设置目的是为了规范隐私计算任务的参数设置。参数由两个属性构成：参数类型、参数限制。

参数类型分为三类：
1. `POS`：位置参数
2. `FLAG`：选项参数
3. `HYPER`：超参数

以上面使用到的命令行参数为例：

```shell
./compile.py /home/nhy/DAVEX/programs/PSI_BASE.mpc 300 500
```

当中 `300`、`500` 为两个位置参数

```shell
./semi2k-party.x PSI_BASE-300-500 -h 10.176.34.171 -pn 6000 -N 2 -p 1 -IF ~/Garnet/Input/557daa32481d1eaab2af83a2a219c374 -OF ~/Garnet/Output/557daa32481d1eaab2af83a2a219c374
```

`semi2k-party.x` 这里视为超参数；`PSI_BASE-300-500` 这里视为运行程序，不视为参数；其余参数*皆为*选项参数。

| 第二个例子只是为例说明什么是选项参数和超参数，实际使用环节中，虚拟机是作为超参数需要设置写入数据库的，而其余选项参数都是固定的，不需要额外写入数据库。

参数限制分为4类：
1. `NUM`：数字类型；
2. `ENUM`：枚举类型；
3. `STRING`：字符串类型；
4. `AUTO`：自动填写类型。

具体作用原理见代码，不在赘述。

下面是一个 MPC 类的json序列化：
```json
{
  "uid": "PSI_GARNET",
  "centerId": 1, 
  "name": "PSI_GARNET",
  "path": "programs/PSI_BASE.mpc", 
  "compileParameters": [
    {
      "name": "P0_Data", 
      "limit": {}, 
      "required": false, 
      "limitType": "AUTO", 
      "posORflag": 0, 
      "description": "第0方数据", 
      "parameterType": "POS"
    }, 
    {
      "name": "P1_Data", 
      "limit": {}, 
      "required": false, 
      "limitType": "AUTO", 
      "posORflag": 1, 
      "description": "第1方数据", 
      "parameterType": "POS"
    }
  ], 
  "runtimeParameters": [
    {
      "name": "protocol", 
      "limit": {
        "defaultValue": "semi2k-party"
      }, 
      "required": false, 
      "limitType": "STRING", 
      "posORflag": null, 
      "description": "运行虚拟机", 
      "parameterType": "HYPER"
    }, 
    {
      "name": "PK", 
      "limit": {
        "defaultValue": "id"
      }, 
      "required": true, 
      "limitType": "STRING", 
      "posORflag": null, 
      "description": "主键", 
      "parameterType": "HYPER"
    }
  ]
}
```

#### MPCTASK

```sql
CREATE TABLE `mpcTask` (
    `uid` varchar(32) NOT NULL,
    `application_id` varchar(255) DEFAULT NULL,
    `center_id` varchar(255) DEFAULT NULL,
    `mpc_id` varchar(32) DEFAULT NULL,
    `compile_parameters` json DEFAULT NULL,
    `runtime_parameters` json DEFAULT NULL,
    `N` int DEFAULT NULL,
    `part` int DEFAULT NULL,
    `host` varchar(255) DEFAULT NULL,
    `port` int DEFAULT NULL,
    `data_id` varchar(255) DEFAULT NULL,
    `mpc_name` varchar(255) DEFAULT NULL,
    `task_type` enum(
        'GARNET_PSI',
        'GARNET_MPC',
        'GARNET_INFERENCE'
    ) DEFAULT NULL,
    `status` varchar(255) DEFAULT NULL,
    `message` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
```

```java
@Data
@EntityScan
@TableName(value = "mpcTask", autoResultMap = true)
public class MpcTask {

    public enum TaskType {
        GARNET_MPC, GARNET_PSI, GARNET_INFERENCE
    }

    public enum Status {
        INIT, COMPILING, READY, RUNNING, FINISHED, FAILED
    }

    @TableId(type = IdType.ASSIGN_UUID)
    private String uid;
    private String applicationId;
    private String centerId;
    private String mpcId;
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject compileParameters;
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject runtimeParameters;
    private Integer N;
    private Long part;
    private String host;
    private Integer port;
    @JsonInclude(Include.NON_NULL)
    private String dataId;
    @JsonIgnore
    private String mpcName;
    private TaskType taskType;
    private Status status;
    @JsonIgnore
    private String message = "";

    public MpcTask(MpcTask other) {
        this.uid = other.uid;
        this.applicationId = other.applicationId;
        this.centerId = other.centerId;
        this.mpcId = other.mpcId;
        this.compileParameters = other.compileParameters;
        this.runtimeParameters = other.runtimeParameters;
        this.N = other.N;
        this.part = other.part;
        this.host = other.host;
        this.port = other.port;
        this.dataId = other.dataId;
        this.mpcName = other.mpcName;
        this.taskType = other.taskType;
        this.status = other.status;
        this.message = other.message;
    }

    public MpcTask() {
    }
}
```

字段含义较为明确，不再赘述。

下面是一个 MpcTask 类的json序列化：

```json
{
  "partInfo": [{
    "agentID":"DAVEX-CXX171-GXX173",
    "part":1,
    "fileID":"DAVEX-CXX171-GXX173-D2"
  }],
  "applicationId": 0,
  "centerId": 1,
  "compileParameters": {},
  "host": "10.176.34.171",
  "mpcId": "PSI_GARNET",
  "n": 2,
  "part": 0,
  "port": 6000,
  "runtimeParameters": {
    "PK": "案号",
    "protocol" : "semi2k-party"
  },
  "status": "INIT",
  "taskType": "GARNET_PSI",
  "uid": "557daa32481d1eaab2af83a2a219c374"
}
```

### 虚拟机编译

编译虚拟机较为耗时，由于虚拟机的编译与具体的数据无关，`DAVEX` 会在每次启动时提前编译所有的虚拟机，这样在执行具体任务时就不再需要耗时的虚拟机编译环节了。

```java
@EventListener(ApplicationReadyEvent.class)
@Async("customExecutor")
public void init() {
    ProcessBuilder makeBuilder = new ProcessBuilder("make").directory(garnet_directory);
    ProcessBuilder pipBuilder = new ProcessBuilder("pip", "install", "-r", "requirements.txt")
            .directory(garnet_directory);
    ProcessBuilder mkdirInputBuilder = new ProcessBuilder("mkdir", "Input").directory(garnet_directory);
    ProcessBuilder mkdirOutputBuilder = new ProcessBuilder("mkdir", "Output").directory(garnet_directory);
    try {
        makeBuilder.start();
        pipBuilder.start();
        mkdirInputBuilder.start();
        mkdirOutputBuilder.start();
        logger.info("Garnet初始化成功");
    } catch (Exception e) {
        e.printStackTrace();
        logger.info("Garnet初始化失败:" + e.getMessage());
    }
}
```
上面的代码同时也会安装 python 依赖以及创建输入输出文件。

### MPC 编译

MPC 编译涉及到两个步骤：1.上传|下载 MPC 文件；2. 根据参数进行编译。

#### 上传|下载 MPC 文件

将 MPC 文件作为资源，暴露下载接口。

```java
@GetMapping("/select")
public R<Mpc> select(@RequestParam String MpcID) {
    LambdaQueryWrapper<Mpc> queryWrapper = Wrappers.<Mpc>lambdaQuery().eq(Mpc::getUid, MpcID);
    return R.success(mpcMapper.selectOne(queryWrapper), "查询成功");
}

@GetMapping("/download")
public ResponseEntity<Resource> download(@RequestParam String MpcID) throws Exception {
    Path filePath = Paths.get(my.getBase_path()).resolve(mpcMapper.selectById(MpcID).getPath());
    Resource resource = new UrlResource(filePath.toUri());
    if (!resource.exists()) {
        throw new FileNotFoundException("File not found: " + filePath);
    }
    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + filePath.getFileName().toString() + "\"")
            .body(resource);
}
```

上述两个接口分别暴露 MPC 文件的元数据与具体文件。

#### 编译

```java
public void compile(MpcTask mpcTask) throws Exception {
    JSONObject task_parameter = mpcTask.getCompileParameters();
    Mpc mpc = mpcMapper.selectById(mpcTask.getMpcId());
    List<Parameter> mpc_parameters = mpc.getCompileParameters();
    Map<Integer, String> args = new HashMap<>();
    List<String> flags = new ArrayList<>();
    for (Parameter p : mpc_parameters) {
        switch (p.getParameterType()) {
            case POS: {
                String value = task_parameter.getString(p.getName());
                if (value == null && p.getRequired()) {
                    throw new IllegalArgumentException("缺少参数: " + p.getName());
                } else if (value != null) {
                    args.put((Integer) p.getPosORflag(), value);
                } else if (!p.getRequired()) {
                    args.put((Integer) p.getPosORflag(), p.getDefaultValue());
                }
                break;
            }
            case FLAG: {
                String value = task_parameter.getString(p.getName());
                if (value == null && p.getRequired()) {
                    throw new IllegalArgumentException("缺少参数: " + p.getName());
                } else if (value != null) {
                    flags.add((String) p.getPosORflag() + " " + value);
                } else if (!p.getRequired()) {
                    flags.add((String) p.getPosORflag() + " " + p.getDefaultValue());
                }
                break;
            }
            case HYPER:
            default:
                break;
        }
    }
    Path mpc_path = Paths.get(my.getBase_path()).resolve(mpc.getPath());
    String mpc_name = mpc_path.getFileName().toString().split("\\.")[0];
    List<String> command = new ArrayList<>(Arrays.asList("python3", "compile.py", mpc_path.toString()));

    for (int i = 0; i < args.size(); i++) {
        command.add(args.get(i));
        mpc_name += "-" + args.get(i);
    }
    command.addAll(flags);
    ProcessBuilder processBuilder = new ProcessBuilder(command).directory(garnet_directory);
    logger.info("运行命令：" + command.toString());
    try {
        mpcTask.setStatus(MpcTask.Status.COMPILING);
        logger.info(mpcTask.getUid() + ":开始编译");
        mpcTaskMapper.updateById(mpcTask);
        Process process = processBuilder.start();
        int exitcode = process.waitFor();
        if (exitcode != 0) {
            mpcTask.setStatus(MpcTask.Status.FAILED);
            mpcTaskMapper.updateById(mpcTask);
            logger.error(mpcTask.getUid() + ":编译失败");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                StringBuilder errorMsg = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorMsg.append(line).append(System.lineSeparator());
                }
                if (errorMsg.length() > 0) {

                    logger.error(errorMsg.toString());
                    throw new Exception(errorMsg.toString());
                }
            }
        }
        mpcTask.setStatus(MpcTask.Status.READY);
        mpcTask.setMpcName(mpc_name);
        mpcTaskMapper.updateById(mpcTask);
        logger.info(mpcTask.getUid() + ":编译成功");
    } catch (Exception e) {
        mpcTask.setStatus(MpcTask.Status.FAILED);
        mpcTaskMapper.updateById(mpcTask);
        logger.error(mpcTask.getUid() + ":编译失败");
        e.printStackTrace();
    }
}
```

### 程序运行

#### 文件链接

基于 `Garnet` 输入数据命名规则，`DAVEX` 将输入数据根据任务 Uid 软链接到 `Garnet/Input` 文件夹下。

```java
public void link(String path, String prefix, Long part) throws Exception {
    List<String> command = new ArrayList<>(Arrays.asList("ln", "-s", path,
            garnet_directory.getAbsolutePath() + "/Input/" + prefix + "-P" + part + "-0"));
    ProcessBuilder processBuilder = new ProcessBuilder(command).directory(garnet_directory);
    try {
        Process process = processBuilder.start();
        Integer exitcode = process.waitFor();
        if (exitcode != 0) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                StringBuilder errorMsg = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorMsg.append(line).append(System.lineSeparator());
                }
                if (errorMsg.length() > 0) {
                    logger.error(errorMsg.toString());
                    throw new Exception(errorMsg.toString());
                }
            }
        }
    } catch (Exception e) {
        throw e;
    }

}
```

#### 运行

如上所述，参与各方需要在1分钟内启动虚拟机进行运算，为此需要有问询各方是否准备好的机制：

问询接口：
```java
public Boolean ready(String mpcTaskId) throws Exception {
    MpcTask mpcTask = mpcTaskMapper.selectById(mpcTaskId);
    if (mpcTask == null) {
        throw new Exception("任务不存在");
    }
    switch (mpcTask.getStatus()) {
        case READY:
            break;
        case COMPILING:
            return false;
        case RUNNING:
            throw new Exception("任务已在运行中");
        case FAILED:
            throw new Exception("任务失败");
        case FINISHED:
            throw new Exception("任务已完成");
        default:
            throw new Exception("错误");
    }
    LambdaQueryWrapper<MpcTaskAgent> queryWrapper = Wrappers.<MpcTaskAgent>lambdaQuery()
            .eq(MpcTaskAgent::getMpcTaskId, mpcTaskId);
    List<MpcTaskAgent> agents = mpcTaskAgentMapper.selectList(queryWrapper);
    List<R<Boolean>> responses = Flux.fromIterable(agents).flatMap((MpcTaskAgent a) -> {
        try {
            return centerWebClientService.center2AgentWebClient(a.getAgentId()).get()
                    .uri(uriBuilder -> uriBuilder.path("/MpcTasks/ready").queryParam("mpcTaskId", mpcTaskId)
                            .build())
                    .retrieve().bodyToMono(new ParameterizedTypeReference<R<Boolean>>() {
                    });
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }).collectList().block();
    for (R<Boolean> response : responses) {
        if (response.getBody().getCode() == 0) {
            throw new Exception(response.getBody().getMessage());
        }
        if (!response.getBody().getData()) {
            return false;
        }
    }
    return true;
}
```

答应接口：
```java
public Boolean ready(String mpcTaskId) throws Exception {
    return mpcTaskMapper.selectById(mpcTaskId).getStatus() == MpcTask.Status.READY;
}
```

在参与各方全部准备完毕后，协调方向各方发布启动命令，再启动自己的虚拟机。

运行函数：
```java
public void run(MpcTask mpcTask) throws Exception {
    String inputPrefix = garnet_directory.getAbsolutePath() + "/Input/" + mpcTask.getUid();
    String outputPrefix = garnet_directory.getAbsolutePath() + "/Output/" + mpcTask.getUid();
    String protocol = mpcTask.getRuntimeParameters().getString("protocol");
    String mpc_name = mpcTask.getMpcName();
    Long part = mpcTask.getPart();
    List<String> command = new ArrayList<>(Arrays.asList(
            "./" + protocol + ".x",
            "-IF", inputPrefix,
            "-OF", outputPrefix,
            // "-N", mpcTask.getN().toString(),
            "-h", mpcTask.getHost(),
            "-pn", mpcTask.getPort().toString(),
            "-p", part.toString(),
//                "-u",
            mpc_name));
    ProcessBuilder processBuilder = new ProcessBuilder(command).directory(garnet_directory);
    logger.info("运行命令：" + command.toString());
    try {
        mpcTask.setStatus(MpcTask.Status.RUNNING);
        logger.info(mpcTask.getUid() + ":开始运行");
        mpcTaskMapper.updateById(mpcTask);
        Process process = processBuilder.start();
        int exitcode = process.waitFor();
        if (exitcode != 0) {
            mpcTask.setStatus(MpcTask.Status.FAILED);
            mpcTaskMapper.updateById(mpcTask);
            logger.error(mpcTask.getUid() + ":运行失败");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                StringBuilder errorMsg = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorMsg.append(line).append(System.lineSeparator());
                }
                if (errorMsg.length() > 0) {
                    logger.error(errorMsg.toString());
                    throw new Exception(errorMsg.toString());
                }
            }
        }
        mpcTask.setStatus(MpcTask.Status.FINISHED);
        mpcTaskMapper.updateById(mpcTask);
        logger.info(mpcTask.getUid() + ":运行成功");
    } catch (Exception e) {
        mpcTask.setStatus(MpcTask.Status.FAILED);
        mpcTaskMapper.updateById(mpcTask);
        logger.error(mpcTask.getUid() + ":运行失败");
        e.printStackTrace();
        throw e;
    }
}
```
