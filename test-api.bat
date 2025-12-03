@echo off
REM DNS Query Record Demo API 测试脚本 (Windows版本)

SET BASE_URL=http://localhost:5380/api/dns-records

echo =========================================
echo DNS Query Record Demo - API 测试
echo =========================================
echo.

REM 1. 获取所有记录
echo 1. 获取所有记录
echo curl %BASE_URL%
curl -s %BASE_URL%
echo.
echo.

REM 2. 创建新记录
echo 2. 创建新记录 (baidu.com)
curl -s -X POST %BASE_URL% ^
  -H "Content-Type: application/json" ^
  -d "{\"domain\":\"baidu.com\",\"queryType\":\"A\",\"responseIp\":\"39.156.66.10\",\"cacheHit\":false,\"responseTimeMs\":28}"
echo.
echo.

REM 3. 查询ID为1的记录
echo 3. 根据ID查询 (ID=1)
echo curl %BASE_URL%/1
curl -s %BASE_URL%/1
echo.
echo.

REM 4. 根据域名查询
echo 4. 根据域名查询 (google.com)
echo curl %BASE_URL%/domain/google.com
curl -s %BASE_URL%/domain/google.com
echo.
echo.

REM 5. 查询缓存命中的记录
echo 5. 查询缓存命中的记录
echo curl %BASE_URL%/cache-hit?value=true
curl -s "%BASE_URL%/cache-hit?value=true"
echo.
echo.

REM 6. 获取缓存命中率统计
echo 6. 获取缓存命中率统计
echo curl %BASE_URL%/stats/cache-hit-rate
curl -s %BASE_URL%/stats/cache-hit-rate
echo.
echo.

REM 7. 更新记录
echo 7. 更新记录 (ID=1)
curl -s -X PUT %BASE_URL%/1 ^
  -H "Content-Type: application/json" ^
  -d "{\"domain\":\"example-updated.com\",\"queryType\":\"AAAA\",\"responseIp\":\"2606:2800:220:1:248:1893:25c8:1946\",\"cacheHit\":true,\"responseTimeMs\":10}"
echo.
echo.

REM 8. 删除记录
echo 8. 删除记录 (ID=2)
echo curl -X DELETE %BASE_URL%/2
curl -s -X DELETE %BASE_URL%/2
echo 已删除记录ID=2
echo.
echo.

echo =========================================
echo 测试完成！
echo =========================================
pause
