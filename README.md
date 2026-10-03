# 五金店 · 工具配件与开单销售

五金店经营工具、紧固件、电工、建材等商品，支持客户分级（普通 / 批发）、开单结算（步骤条：选客 → 选型 → 确认 → 完成，结算自动扣减库存，批发享 9 折）、售后维修 / 退换 / 作废（退换回滚库存）。

## 启动

```bash
sh start.sh     # 启动后端 + MySQL + 前端（docker compose）
sh stop.sh      # 停止
```

- 后端接口：`/api/products`、`/api/customers`、`/api/sales-orders`、`/api/after-sales`
- 前端访问：http://localhost:8080 （nginx 反代 `/api`）
