# DNS Query Record Demo - R2DBC + H2

这是一个完整的响应式CRUD Demo，展示了Spring Boot 4.0 + WebFlux + R2DBC + H2的正确使用方式。

## 技术栈

- **Spring Boot 4.0.0** - 最新Spring框架
- **Spring WebFlux** - 响应式Web框架（非阻塞）
- **Spring Data R2DBC** - 响应式数据库访问（非阻塞）
- **H2 Database** - 内存数据库
- **Lombok** - 简化代码
- **Java 21** - 最新LTS版本

## 项目结构

```
com.npc2048.dns/
├── controller/
│   └── DnsQueryRecordController.java    # REST API控制器
├── service/
│   └── DnsQueryRecordService.java       # 业务逻辑层
├── repository/
│   └── DnsQueryRecordRepository.java    # 数据访问层（响应式）
└── model/
    └── DnsQueryRecord.java              # 实体类
```

## 快速开始

### 1. 安装依赖

```bash
cd dns-backend
mvn clean install
```

### 2. 启动应用

```bash
mvn spring-boot:run
```

服务会在 `http://localhost:8080` 启动。

### 3. 测试API

#### 查询所有记录（已有3条测试数据）

```bash
curl http://localhost:8080/api/dns-records
```

#### 创建新记录

```bash
curl -X POST http://localhost:8080/api/dns-records \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "baidu.com",
    "queryType": "A",
    "responseIp": "39.156.66.10",
    "cacheHit": false,
    "responseTimeMs": 28
  }'
```

#### 根据ID查询

```bash
curl http://localhost:8080/api/dns-records/1
```

#### 根据域名查询

```bash
curl http://localhost:8080/api/dns-records/domain/google.com
```

#### 查询缓存命中的记录

```bash
curl http://localhost:8080/api/dns-records/cache-hit?value=true
```

#### 更新记录

```bash
curl -X PUT http://localhost:8080/api/dns-records/1 \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "example.com",
    "queryType": "AAAA",
    "responseIp": "2606:2800:220:1:248:1893:25c8:1946",
    "cacheHit": true,
    "responseTimeMs": 10
  }'
```

#### 删除记录

```bash
curl -X DELETE http://localhost:8080/api/dns-records/1
```

#### 获取缓存命中率统计

```bash
curl http://localhost:8080/api/dns-records/stats/cache-hit-rate
```

#### 实时流式推送（SSE）

```bash
curl -N http://localhost:8080/api/dns-records/stream
```

## API文档

### 端点列表

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/dns-records` | 获取所有记录 |
| GET | `/api/dns-records/{id}` | 根据ID获取记录 |
| GET | `/api/dns-records/domain/{domain}` | 根据域名查询 |
| GET | `/api/dns-records/cache-hit?value=true` | 按缓存命中状态查询 |
| POST | `/api/dns-records` | 创建新记录 |
| PUT | `/api/dns-records/{id}` | 更新记录 |
| DELETE | `/api/dns-records/{id}` | 删除记录 |
| GET | `/api/dns-records/stats/cache-hit-rate` | 获取缓存命中率 |
| GET | `/api/dns-records/stream` | SSE实时流（演示响应式推送） |

## 响应式编程要点

### ✅ 正确做法：非阻塞响应式链

```java
// Controller
public Mono<DnsQueryRecord> getRecord(Long id) {
    return service.getRecordById(id);  // 返回Mono，不阻塞
}

// Service
public Mono<DnsQueryRecord> getRecordById(Long id) {
    return repository.findById(id)
        .doOnNext(record -> log.debug("Found: {}", record))
        .switchIfEmpty(Mono.empty());  // 响应式错误处理
}
```

### ❌ 错误做法：阻塞操作

```java
// ❌ 不要这样做！
public DnsQueryRecord getRecord(Long id) {
    return repository.findById(id).block();  // block()会阻塞！
}

// ❌ 不要在响应式流中使用Thread.sleep()
public Mono<String> bad() {
    Thread.sleep(1000);  // 阻塞整个线程！
    return Mono.just("result");
}
```

## H2 数据库控制台

访问 http://localhost:8080/h2-console

连接参数：
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: (留空)

## 数据库表结构

```sql
CREATE TABLE dns_query_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    domain VARCHAR(255) NOT NULL,
    query_type VARCHAR(10) NOT NULL,
    response_ip VARCHAR(50),
    cache_hit BOOLEAN DEFAULT FALSE,
    query_time TIMESTAMP NOT NULL,
    response_time_ms INTEGER
);
```

## 核心依赖

```xml
<!-- 响应式数据库访问 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-r2dbc</artifactId>
</dependency>

<!-- H2 R2DBC 驱动 -->
<dependency>
    <groupId>io.r2dbc</groupId>
    <artifactId>r2dbc-h2</artifactId>
</dependency>

<!-- WebFlux 响应式Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

## 为什么不用JPA？

**JPA是阻塞的！** 它会破坏WebFlux的非阻塞模型。

- ❌ `spring-boot-starter-data-jpa` - 阻塞式数据库访问
- ✅ `spring-boot-starter-data-r2dbc` - 非阻塞响应式访问

| 技术 | 模型 | 返回类型 | 适用场景 |
|------|------|----------|----------|
| JPA | 阻塞 | `List<T>`, `T` | Spring MVC |
| R2DBC | 响应式 | `Flux<T>`, `Mono<T>` | Spring WebFlux |

## 日志输出

启动后你会看到类似的日志：

```
2024-01-01 10:00:00.123  INFO 12345 --- [main] c.n.d.DnsApplication : Starting DnsApplication
2024-01-01 10:00:01.456  INFO 12345 --- [main] o.s.b.w.r.c.ReactorResourceFactory : Started reactor netty resources
2024-01-01 10:00:02.789  INFO 12345 --- [main] c.n.d.DnsApplication : Started DnsApplication in 3.5 seconds
```

## 性能特点

- **全异步非阻塞**: 所有数据库操作都是非阻塞的
- **高并发支持**: 基于Reactor Netty，单线程处理大量请求
- **背压支持**: Flux/Mono自动处理数据流背压
- **资源高效**: 不需要为每个请求分配线程

## 进一步学习

- [Spring Data R2DBC文档](https://docs.spring.io/spring-data/r2dbc/docs/current/reference/html/)
- [Project Reactor文档](https://projectreactor.io/docs/core/release/reference/)
- [WebFlux文档](https://docs.spring.io/spring-framework/reference/web/webflux.html)

---

**记住：在WebFlux中，永远不要调用`.block()`！**
