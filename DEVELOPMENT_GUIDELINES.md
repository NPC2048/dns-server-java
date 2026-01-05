# DNS Server 项目开发规范

## 1. 实体类规范

### 1.1 命名规范
- **实体类后缀**：实体类不需要 `Entity` 后缀，直接使用业务含义名称，如 [ConfigEntity](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/model/entity/ConfigEntity.java#L10-L40)、[QueryRecord](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/model/entity/QueryRecord.java#L10-L65)、[CacheEntity](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/model/entity/CacheEntity.java#L10-L47)、[StatsEntity](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/model/entity/StatsEntity.java#L10-L35)
- **表名对应**：实体类必须与数据库表名保持一致，通过 `@Table(name = "表名")` 注解指定
  - [ConfigEntity](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/model/entity/ConfigEntity.java#L10-L40) 对应 `dns_config` 表
  - [QueryRecord](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/model/entity/QueryRecord.java#L10-L65) 对应 `dns_queries` 表
  - [CacheEntity](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/model/entity/CacheEntity.java#L10-L47) 对应 `dns_cache` 表
  - [StatsEntity](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/model/entity/StatsEntity.java#L10-L35) 对应 `dns_statistics` 表
- **特殊情况**：目前项目中部分实体类保留了 `Entity` 后缀（如 [ConfigEntity](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/model/entity/ConfigEntity.java#L10-L40)、[CacheEntity](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/model/entity/CacheEntity.java#L10-L47)、[StatsEntity](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/model/entity/StatsEntity.java#L10-L35)），为保持一致性，新实体类可沿用此命名方式

### 1.2 结构规范
- **必备注解**：实体类必须包含以下注解
  - `@Entity`：标识为JPA实体
  - `@Table(name = "表名")`：指定对应的数据库表名
  - `@Data`：Lombok注解，生成getter/setter方法
  - `@Builder`：Lombok注解，提供构建器模式
  - `@NoArgsConstructor`：Lombok注解，生成无参构造函数
  - `@AllArgsConstructor`：Lombok注解，生成全参构造函数

### 1.3 字段规范
- **字段注释**：每个字段必须有中文注释说明其用途
- **数据库映射**：使用 `@Column` 注解明确指定数据库列名和属性
- **主键定义**：使用 `@Id` 和 `@GeneratedValue` 注解定义主键
- **字段长度**：对字符串类型字段使用 `length` 属性限制长度
- **数据类型**：遵循数据库字段类型映射规范

## 2. 控制器规范

### 2.1 命名规范
- **后缀**：控制器类必须以 `Controller` 后缀结尾
- **包位置**：所有控制器放在 `com.npc2048.dns.controller` 包下
- **API前缀**：管理类API使用 `/api` 前缀，如 `/api/dns-records`；业务API使用业务相关前缀，如 `/dns`、`/auth`

### 2.2 结构规范
- **必备注解**：
  - `@RestController`：REST风格控制器
  - `@RequestMapping`：指定基础路径
  - `@RequiredArgsConstructor`：Lombok注解，自动注入final字段依赖
  - `@Slf4j`：日志注解
- **权限控制**：根据需要添加 `@SaCheckLogin` 或 `@SaCheckRole` 注解
- **返回格式**：统一使用 `SaResult` 作为返回类型

### 2.3 接口规范
- **日志记录**：在关键操作前后记录日志
- **异常处理**：使用try-catch捕获异常并返回错误信息
- **路径命名**：使用小写加中划线风格，如 `/api/dns-records`
- **注释规范**：每个接口方法必须有JavaDoc注释，说明功能、参数和返回值
- **HTTP方法**：合理使用GET/POST/PUT/DELETE等方法

## 3. 服务类规范

### 3.1 命名规范
- **后缀**：服务类必须以 `Service` 后缀结尾
- **包位置**：所有服务类放在 `com.npc2048.dns.service` 包下
- **实现类**：实现类放在 `impl` 子包中，如 `com.npc2048.dns.service.impl`
- **接口命名**：接口与实现类使用相同名称，实现类以 `Impl` 或具体实现名称结尾

### 3.2 结构规范
- **必备注解**：
  - `@Service`：标识为Spring服务
  - `@RequiredArgsConstructor`：Lombok注解，自动注入依赖
  - `@Slf4j`：日志注解
- **事务管理**：对数据库操作方法使用 `@Transactional` 注解
- **方法命名**：使用语义化的方法名，如 `create`、`update`、`delete`、`getBy` 等

## 4. 数据访问层规范

### 4.1 命名规范
- **后缀**：Repository接口必须以 `Repository` 后缀结尾
- **包位置**：所有Repository放在 `com.npc2048.dns.repository.h2` 包下
- **命名规则**：Repository名称应与对应实体类一致，如 [ConfigEntityRepository](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/repository/h2/ConfigEntityRepository.java#L6-L11)、[DnsQueryRecordRepository](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/repository/h2/DnsQueryRecordRepository.java#L13-L53)

### 4.2 结构规范
- **继承规范**：继承 `JpaRepository<实体类, 主键类型>` 接口
- **注解**：使用 `@Repository` 注解标识
- **自定义方法**：遵循Spring Data JPA命名规范，如 `findBy`、`save`、`deleteBy` 等

## 5. DTO类规范

### 5.1 命名规范
- **位置**：DTO类放在 `com.npc2048.dns.model.dto` 包下
- **后缀**：通常以 `Request`、`Response`、`DTO` 等结尾
- **命名原则**：使用语义化命名，清晰表达用途，如 [LoginRequest](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/model/dto/LoginRequest.java#L9-L14)、[LoginResponse](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/model/dto/LoginResponse.java#L9-L32)

### 5.2 结构规范
- **注解**：使用 `@Data` 注解生成getter/setter
- **验证**：根据需要添加验证注解
- **构造函数**：使用 `@NoArgsConstructor` 和 `@AllArgsConstructor` 生成构造函数

## 6. 配置类规范

### 6.1 命名规范
- **后缀**：配置类以 `Config` 后缀结尾
- **包位置**：所有配置类放在 `com.npc2048.dns.config` 包下
- **常量类**：常量类命名为 `Constants`，包含所有项目常量

### 6.2 结构规范
- **必备注解**：
  - `@Configuration`：标识为配置类
  - `@Data`：Lombok注解
  - `@ConfigurationProperties(prefix = "前缀")`：配置属性绑定
  - `@Component`：将配置类注册为Spring组件
- **常量类结构**：使用 `public static final` 定义常量，私有构造函数防止实例化

## 7. 模型类规范

### 7.1 命名规范
- **普通模型类**：放在 `com.npc2048.dns.model` 包下，如 [DnsQueryResult](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/model/DnsQueryResult.java#L14-L66)、[UpstreamDnsConfig](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/model/UpstreamDnsConfig.java#L10-L87)、[CacheEntry](file:///G/Projects/dns-server/dns-backend/src/main/java/com/npc2048/dns/model/CacheEntry.java#L7-L22)
- **DTO类**：放在 `com.npc2048.dns.model.dto` 包下
- **实体类**：放在 `com.npc2048.dns.model.entity` 包下

### 7.2 结构规范
- **普通模型类**：使用 `@Data`、`@Builder`、`@NoArgsConstructor`、`@AllArgsConstructor` 注解
- **访问修饰符**：常量使用 `public static final` 修饰
- **命名风格**：常量名使用大写字母加下划线，如 `CONFIG_KEY_UPSTREAM_DNS`

## 8. 异常处理规范

### 8.1 全局异常处理
- **位置**：全局异常处理器放在 `com.npc2048.dns.exception` 包下
- **注解**：使用 `@ControllerAdvice` 和 `@ExceptionHandler` 注解
- **返回格式**：统一返回 `SaResult` 格式

## 9. 代码风格规范

### 9.1 注释规范
- **类注释**：每个类必须有JavaDoc注释，包含作者信息
- **字段注释**：每个字段必须有中文注释
- **方法注释**：公共方法必须有注释说明功能、参数和返回值

### 9.2 日志规范
- **日志级别**：合理使用 `debug`、`info`、`warn`、`error` 级别
- **日志内容**：记录关键操作、错误信息和性能指标

### 9.3 代码格式
- **缩进**：使用4个空格进行缩进
- **行长度**：单行代码长度不超过120个字符
- **命名**：变量和方法使用驼峰命名法，类名使用帕斯卡命名法

## 10. 依赖注入规范

### 10.1 构造器注入
- **推荐方式**：优先使用构造器注入（通过 `@RequiredArgsConstructor`）
- **避免字段注入**：不推荐使用 `@Autowired` 直接注入字段

### 10.2 依赖管理
- **最小化原则**：只注入必要的依赖
- **接口依赖**：优先依赖抽象而不是具体实现