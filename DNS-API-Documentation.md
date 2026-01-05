# DNS 管理系统 API 接口文档

## 概述

本文档描述了 DNS 管理系统的所有 API 接口，包括配置管理、查询记录、缓存管理、认证等功能。这些接口设计为技术栈无关，可用于 Java、Go、Python 等任何后端实现。

## 基础信息

- **基础 URL**: `http://localhost:5381/api`（可配置）
- **认证方式**: Bearer Token
- **响应格式**: 统一的 JSON 格式
- **内容类型**: `application/json`

## 统一响应格式

所有接口都使用统一的响应格式：

```json
{
  "code": 200,        // 状态码，200表示成功，500表示业务错误
  "msg": "success",   // 响应消息
  "data": null        // 响应数据，成功时包含具体数据，失败时为null
}
```

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 201 | 创建成功 |
| 204 | 删除成功 |
| 401 | 未认证 |
| 403 | 权限不足 |
| 500 | 业务错误 |

## 认证接口

### 1. 用户登录

**接口**: `POST /auth/login`
**描述**: 用户登录认证，返回 token 和用户信息

**请求体**:
```json
{
  "username": "admin",
  "password": "password123"
}
```

**响应**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "sa-token-xxx",
    "username": "admin",
    "isAdmin": true
  }
}
```

### 2. 用户登出

**接口**: `POST /auth/logout`
**描述**: 用户登出，清除 token
**需要认证**: 是

**响应**:
```json
{
  "code": 200,
  "msg": "登出成功"
}
```

### 3. 获取当前用户信息

**接口**: `GET /auth/current`
**描述**: 获取当前登录用户的信息
**需要认证**: 是

**请求头**:
```
Authorization: Bearer sa-token-xxx
```

**响应**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "sa-token-xxx",
    "username": "admin",
    "isAdmin": true
  }
}
```

### 4. 健康检查

**接口**: `GET /auth/health`
**描述**: 认证服务健康检查，无需鉴权

**响应**:
```json
{
  "code": 200,
  "msg": "认证服务正常"
}
```

## DNS 配置管理

### 1. 获取 DNS 配置

**接口**: `GET /dns/config`
**描述**: 获取当前 DNS 服务器所有配置信息
**需要认证**: 否

**响应**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "upstreamDns": [
      {
        "address": "8.8.8.8",
        "port": 53,
        "timeout": 5000,
        "useProxy": false,
        "enabled": true,
        "priority": 1
      }
    ],
    "defaultTimeout": 5000,
    "retryCount": 3,
    "cacheMaxSize": 10000,
    "cacheMaxWeight": 10485760,
    "cacheCapacityMode": "ENTRIES",
    "cacheRebuildStrategy": "CLEAR",
    "cacheDefaultTtl": 300,
    "listenPort": 5354,
    "cacheEnabled": true,
    "queryLogEnabled": true
  }
}
```

### 2. 更新 DNS 配置

**接口**: `PUT /dns/config`
**描述**: 更新 DNS 服务器配置，需要管理员权限
**需要认证**: 是，且需要管理员权限

**请求体**:
```json
{
  "upstreamDns": [
    {
      "address": "8.8.8.8",
      "port": 53,
      "timeout": 5000,
      "useProxy": false,
      "enabled": true,
      "priority": 1
    }
  ],
  "cacheMaxSize": 20000,
  "listenPort": 5354,
  "cacheEnabled": true,
  "queryLogEnabled": true
}
```

**响应**:
```json
{
  "code": 200,
  "msg": "配置更新成功"
}
```

### 3. 重置配置为默认值

**接口**: `POST /dns/config/reset`
**描述**: 将 DNS 配置重置为默认值
**需要认证**: 是，且需要管理员权限

**响应**:
```json
{
  "code": 200,
  "msg": "配置已重置为默认值"
}
```

### 4. 重新加载配置

**接口**: `POST /dns/config/reload`
**描述**: 重新加载 DNS 服务器配置
**需要认证**: 是，且需要管理员权限

**响应**:
```json
{
  "code": 200,
  "msg": "配置已重新加载"
}
```

### 5. 查询域名（测试用）

**接口**: `POST /dns/query`
**描述**: 手动查询 DNS 域名，用于测试 DNS 服务
**需要认证**: 否

**请求体**:
```json
{
  "domain": "google.com"
}
```

**响应**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "domain": "google.com",
    "ipAddresses": ["142.250.191.78", "142.250.191.106"],
    "ttl": 300,
    "cacheHit": false,
    "responseTimeMs": 25,
    "queryTime": "2024-01-06T10:30:00",
    "queryType": "A",
    "success": true,
    "errorMessage": null
  }
}
```

## DNS 查询记录管理

### 1. 创建 DNS 查询记录

**接口**: `POST /api/dns-records`
**描述**: 手动创建一条 DNS 查询记录
**需要认证**: 否

**请求体**:
```json
{
  "domain": "example.com",
  "queryType": "A",
  "responseIp": "93.184.216.34",
  "cacheHit": false,
  "queryTime": 1704541800000,
  "responseTimeMs": 45
}
```

**响应**:
```json
{
  "code": 201,
  "msg": "success",
  "data": {
    "id": 1,
    "domain": "example.com",
    "queryType": "A",
    "responseIp": "93.184.216.34",
    "cacheHit": false,
    "queryTime": 1704541800000,
    "responseTimeMs": 45
  }
}
```

### 2. 分页获取所有 DNS 查询记录

**接口**: `GET /api/dns-records`
**描述**: 分页查询所有 DNS 查询记录
**需要认证**: 否

**查询参数**:
- `page` (int, default=0): 页码
- `size` (int, default=20): 每页大小

**响应**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "domain": "example.com",
        "queryType": "A",
        "responseIp": "93.184.216.34",
        "cacheHit": false,
        "queryTime": 1704541800000,
        "responseTimeMs": 45
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "currentPage": 0,
    "pageSize": 20
  }
}
```

### 3. 根据ID获取记录

**接口**: `GET /api/dns-records/{id}`
**描述**: 根据ID获取单条 DNS 查询记录
**需要认证**: 否

**路径参数**:
- `id` (Long): 记录ID

**响应**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "domain": "example.com",
    "queryType": "A",
    "responseIp": "93.184.216.34",
    "cacheHit": false,
    "queryTime": 1704541800000,
    "responseTimeMs": 45
  }
}
```

### 4. 根据域名查询记录

**接口**: `GET /api/dns-records/domain/{domain}`
**描述**: 根据域名分页查询 DNS 查询记录
**需要认证**: 否

**路径参数**:
- `domain` (String): 域名

**查询参数**:
- `page` (int, default=0): 页码
- `size` (int, default=20): 每页大小

### 5. 根据缓存命中状态查询记录

**接口**: `GET /api/dns-records/cache-hit`
**描述**: 根据缓存命中状态分页查询 DNS 查询记录
**需要认证**: 否

**查询参数**:
- `value` (Boolean): 缓存命中状态 (true/false)
- `page` (int, default=0): 页码
- `size` (int, default=20): 每页大小

### 6. 获取缓存命中率统计

**接口**: `GET /api/dns-records/stats/cache-hit-rate`
**描述**: 计算并返回 DNS 查询的缓存命中率
**需要认证**: 否

**响应**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "cacheHitRate": 0.85,
    "description": "85.00%"
  }
}
```

### 7. 删除 DNS 查询记录

**接口**: `DELETE /api/dns-records/{id}`
**描述**: 删除指定ID的 DNS 查询记录
**需要认证**: 否

**路径参数**:
- `id` (Long): 记录ID

**响应**:
```json
{
  "code": 200,
  "msg": "记录删除成功"
}
```

## 缓存管理

### 1. 获取缓存详情

**接口**: `GET /cache/detail`
**描述**: 获取 DNS 缓存的详细统计信息
**需要认证**: 否

**响应**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "hits": 1250,
    "misses": 250,
    "size": 800,
    "hitRate": 0.8333
  }
}
```

### 2. 清空缓存

**接口**: `DELETE /dns/cache`
**描述**: 清空 DNS 缓存
**需要认证**: 是，且需要管理员权限

**响应**:
```json
{
  "code": 200,
  "msg": "缓存已清空"
}
```

## 系统管理

### 1. 获取鉴权配置

**接口**: `GET /api/manage/auth-config`
**描述**: 获取系统鉴权配置
**需要认证**: 是

**响应**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "requireForDnsQueries": true,
    "adminUsers": ["admin", "manager"]
  }
}
```

### 2. 更新鉴权配置

**接口**: `PUT /api/manage/auth-config`
**描述**: 更新系统鉴权配置
**需要认证**: 是，且需要管理员权限

**请求体**:
```json
{
  "requireForDnsQueries": false,
  "adminUsers": ["admin"]
}
```

**响应**:
```json
{
  "code": 200,
  "msg": "配置更新成功"
}
```

### 3. 管理员健康检查

**接口**: `GET /api/manage/health`
**描述**: 管理服务健康检查
**需要认证**: 是

**响应**:
```json
{
  "code": 200,
  "msg": "管理服务正常"
}
```

## 数据类型定义

### UpstreamDnsConfig
```json
{
  "address": "string",        // DNS 服务器地址
  "port": 53,                 // 端口号，可选
  "timeout": 5000,            // 超时时间（毫秒），可选
  "useProxy": false,          // 是否使用代理，可选
  "enabled": true,            // 是否启用，可选
  "priority": 1,              // 优先级（数字越小优先级越高），可选
  "proxyConfig": null         // 代理配置，可选
}
```

### ProxyConfig
```json
{
  "host": "string",           // 代理主机
  "port": 8080,              // 代理端口，可选
  "type": "http",            // 代理类型，可选
  "username": "string",      // 用户名，可选
  "password": "string"       // 密码，可选
}
```

### QueryRecord
```json
{
  "id": 1,                    // 记录ID（创建时无需提供）
  "domain": "string",         // 域名
  "queryType": "string",     // 查询类型（A、AAAA、CNAME等）
  "responseIp": "string",     // 响应IP
  "cacheHit": true,          // 是否缓存命中
  "queryTime": 1704541800000, // 查询时间（时间戳）
  "responseTimeMs": 45       // 响应时间（毫秒）
}
```

### DnsQueryResult
```json
{
  "domain": "string",         // 查询域名
  "ipAddresses": ["string"],  // IP地址列表
  "ttl": 300,                // TTL值
  "cacheHit": true,          // 是否缓存命中
  "responseTimeMs": 25,      // 响应时间（毫秒）
  "queryTime": "2024-01-06T10:30:00", // 查询时间
  "queryType": "A",          // 查询类型
  "success": true,           // 是否成功
  "errorMessage": null       // 错误信息，可选
}
```

### CacheStats
```json
{
  "hits": 1250,              // 缓存命中次数
  "misses": 250,             // 缓存未命中次数
  "size": 800,               // 当前缓存大小
  "hitRate": 0.8333          // 缓存命中率
}
```

## 错误处理

### 常见错误响应

```json
{
  "code": 500,
  "msg": "配置更新失败：上游DNS服务器不可用",
  "data": null
}
```

### 错误码说明

| HTTP状态码 | 业务错误码 | 说明 |
|------------|------------|------|
| 401 | - | 未认证 |
| 403 | - | 权限不足 |
| 500 | 1001 | 配置验证失败 |
| 500 | 1002 | 上游DNS服务器不可用 |
| 500 | 1003 | 缓存操作失败 |
| 500 | 1004 | 数据库操作失败 |

## 使用示例

### JavaScript/TypeScript

```javascript
// 获取DNS配置
async function getDnsConfig() {
  const response = await fetch('/api/dns/config', {
    method: 'GET'
  });
  const result = await response.json();
  if (result.code === 200) {
    return result.data;
  } else {
    throw new Error(result.msg);
  }
}

// 更新DNS配置
async function updateDnsConfig(config) {
  const response = await fetch('/api/dns/config', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(config)
  });
  const result = await response.json();
  if (result.code !== 200) {
    throw new Error(result.msg);
  }
}

// 执行DNS查询
async function queryDomain(domain) {
  const response = await fetch('/api/dns/query', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ domain })
  });
  const result = await response.json();
  if (result.code === 200) {
    return result.data;
  } else {
    throw new Error(result.msg);
  }
}
```

### Python

```python
import requests

class DnsApiClient:
    def __init__(self, base_url, token=None):
        self.base_url = base_url
        self.token = token
        self.headers = {'Content-Type': 'application/json'}
        if token:
            self.headers['Authorization'] = f'Bearer {token}'

    def get_config(self):
        response = requests.get(f'{self.base_url}/dns/config', headers=self.headers)
        result = response.json()
        if result['code'] == 200:
            return result['data']
        else:
            raise Exception(result['msg'])

    def update_config(self, config):
        response = requests.put(f'{self.base_url}/dns/config',
                              json=config,
                              headers=self.headers)
        result = response.json()
        if result['code'] != 200:
            raise Exception(result['msg'])

    def query_domain(self, domain):
        response = requests.post(f'{self.base_url}/dns/query',
                                json={'domain': domain},
                                headers=self.headers)
        result = response.json()
        if result['code'] == 200:
            return result['data']
        else:
            raise Exception(result['msg'])
```

### Go

```go
package main

import (
    "bytes"
    "encoding/json"
    "net/http"
)

type DnsApiClient struct {
    BaseURL string
    Token   string
}

type DnsConfig struct {
    UpstreamDns []UpstreamDnsConfig `json:"upstreamDns"`
    // ... 其他配置字段
}

type DnsQueryResult struct {
    Domain         string   `json:"domain"`
    IpAddresses    []string `json:"ipAddresses"`
    TTL            int      `json:"ttl"`
    CacheHit       bool     `json:"cacheHit"`
    ResponseTimeMs int      `json:"responseTimeMs"`
    // ... 其他字段
}

func (c *DnsApiClient) GetConfig() (*DnsConfig, error) {
    resp, err := http.Get(c.BaseURL + "/dns/config")
    if err != nil {
        return nil, err
    }
    defer resp.Body.Close()

    var result struct {
        Code int             `json:"code"`
        Msg  string          `json:"msg"`
        Data DnsConfig       `json:"data"`
    }
    if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
        return nil, err
    }

    if result.Code != 200 {
        return nil, fmt.Errorf(result.Msg)
    }

    return &result.Data, nil
}

func (c *DnsApiClient) UpdateConfig(config *DnsConfig) error {
    jsonData, err := json.Marshal(config)
    if err != nil {
        return err
    }

    req, err := http.NewRequest("PUT", c.BaseURL+"/dns/config", bytes.NewBuffer(jsonData))
    if err != nil {
        return err
    }
    req.Header.Set("Content-Type", "application/json")
    if c.Token != "" {
        req.Header.Set("Authorization", "Bearer "+c.Token)
    }

    resp, err := http.DefaultClient.Do(req)
    if err != nil {
        return err
    }
    defer resp.Body.Close()

    var result struct {
        Code int    `json:"code"`
        Msg  string `json:"msg"`
    }
    if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
        return err
    }

    if result.Code != 200 {
        return fmt.Errorf(result.Msg)
    }

    return nil
}

func (c *DnsApiClient) QueryDomain(domain string) (*DnsQueryResult, error) {
    reqData := map[string]string{"domain": domain}
    jsonData, err := json.Marshal(reqData)
    if err != nil {
        return nil, err
    }

    resp, err := http.Post(c.BaseURL+"/dns/query",
                         "application/json",
                         bytes.NewBuffer(jsonData))
    if err != nil {
        return nil, err
    }
    defer resp.Body.Close()

    var result struct {
        Code int             `json:"code"`
        Msg  string          `json:"msg"`
        Data DnsQueryResult  `json:"data"`
    }
    if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
        return nil, err
    }

    if result.Code != 200 {
        return nil, fmt.Errorf(result.Msg)
    }

    return &result.Data, nil
}
```

## 版本历史

- **v1.0.0** - 初始版本，包含所有基础 API 接口

## 注意事项

1. **认证要求**: 标记为需要认证的接口必须在请求头中携带有效的 Bearer Token
2. **管理员权限**: 标记为需要管理员权限的接口，必须使用具有管理员角色的 Token
3. **分页参数**: 所有分页查询接口都支持 `page` 和 `size` 参数
4. **错误处理**: 建议客户端实现统一的错误处理机制
5. **缓存策略**: 缓存相关的操作可能需要一些时间才能生效
6. **并发安全**: 配置更新操作是原子性的，但建议在更新期间避免频繁的配置读取