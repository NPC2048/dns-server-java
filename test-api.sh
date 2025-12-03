#!/bin/bash
# DNS Query Record Demo API 测试脚本

BASE_URL="http://localhost:5380/api/dns-records"

echo "========================================="
echo "DNS Query Record Demo - API 测试"
echo "========================================="
echo ""

# 1. 获取所有记录
echo "1️⃣  获取所有记录"
echo "curl $BASE_URL"
curl -s $BASE_URL | jq .
echo -e "\n"

# 2. 创建新记录
echo "2️⃣  创建新记录 (baidu.com)"
echo "curl -X POST $BASE_URL -H 'Content-Type: application/json' -d '{...}'"
curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "baidu.com",
    "queryType": "A",
    "responseIp": "39.156.66.10",
    "cacheHit": false,
    "responseTimeMs": 28
  }' | jq .
echo -e "\n"

# 3. 查询ID为1的记录
echo "3️⃣  根据ID查询 (ID=1)"
echo "curl $BASE_URL/1"
curl -s $BASE_URL/1 | jq .
echo -e "\n"

# 4. 根据域名查询
echo "4️⃣  根据域名查询 (google.com)"
echo "curl $BASE_URL/domain/google.com"
curl -s $BASE_URL/domain/google.com | jq .
echo -e "\n"

# 5. 查询缓存命中的记录
echo "5️⃣  查询缓存命中的记录"
echo "curl $BASE_URL/cache-hit?value=true"
curl -s "$BASE_URL/cache-hit?value=true" | jq .
echo -e "\n"

# 6. 获取缓存命中率统计
echo "6️⃣  获取缓存命中率统计"
echo "curl $BASE_URL/stats/cache-hit-rate"
curl -s $BASE_URL/stats/cache-hit-rate | jq .
echo -e "\n"

# 7. 更新记录
echo "7️⃣  更新记录 (ID=1)"
echo "curl -X PUT $BASE_URL/1 -H 'Content-Type: application/json' -d '{...}'"
curl -s -X PUT $BASE_URL/1 \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "example-updated.com",
    "queryType": "AAAA",
    "responseIp": "2606:2800:220:1:248:1893:25c8:1946",
    "cacheHit": true,
    "responseTimeMs": 10
  }' | jq .
echo -e "\n"

# 8. 删除记录
echo "8️⃣  删除记录 (ID=2)"
echo "curl -X DELETE $BASE_URL/2"
curl -s -X DELETE $BASE_URL/2
echo "已删除记录ID=2"
echo -e "\n"

# 9. SSE流式推送（只显示前5条）
echo "9️⃣  SSE实时流（前5条，Ctrl+C停止）"
echo "curl -N $BASE_URL/stream"
echo "提示：这会持续推送数据，按Ctrl+C停止..."
curl -N -s $BASE_URL/stream | head -n 20

echo ""
echo "========================================="
echo "测试完成！"
echo "========================================="
