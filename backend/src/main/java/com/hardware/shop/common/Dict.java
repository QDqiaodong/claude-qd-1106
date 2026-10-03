package com.hardware.shop.common;

import java.util.List;

/**
 * 五金店业务词典与规则参数表：商品类别 / 商品状态、客户等级、
 * 开单步骤流转、开单状态、售后处理方式与售后状态。
 *
 * <p>规则集中在此，Service 直接引用做校验，便于出题时调整或扩展。</p>
 */
public final class Dict {

    private Dict() {
    }

    // ---------------- 商品 ----------------
    /** 商品类别：工具 / 紧固件 / 电工 / 建材 */
    public static final List<String> PRODUCT_CATEGORIES = List.of("工具", "紧固件", "电工", "建材");

    /** 商品状态：在售 / 停用（停用商品不能开单） */
    public static final List<String> PRODUCT_STATUS = List.of("在售", "停用");

    // ---------------- 客户 ----------------
    /** 客户等级：普通 / 批发（批发开单享 9 折） */
    public static final List<String> MEMBER_LEVELS = List.of("普通", "批发");

    // ---------------- 开单步骤 ----------------
    /** 开单步骤流转：选客 → 选型 → 确认 → 完成，只能逐级前进，不可后退 */
    public static final List<String> ORDER_STEP = List.of("选客", "选型", "确认", "完成");

    // ---------------- 开单状态 ----------------
    /** 开单状态：草稿（可作废）→ 已结算 → 已作废 */
    public static final List<String> SALES_ORDER_STATUS = List.of("草稿", "已结算", "已作废");

    // ---------------- 售后 ----------------
    /** 售后处理方式：维修 / 退换（回滚库存）/ 作废 */
    public static final List<String> AFTER_SALE_HANDLE = List.of("维修", "退换", "作废");

    /** 售后状态：待处理 → 已处理 */
    public static final List<String> AFTER_SALE_STATUS = List.of("待处理", "已处理");

    // ---------------- 手机格式 ----------------
    /** 手机号格式：1 开头的 11 位手机号 */
    public static final String PHONE_PATTERN = "1[3-9]\\d{9}";

    // ---------------- 取值 / 校验方法 ----------------

    public static boolean isValidProductCategory(String category) {
        return category != null && PRODUCT_CATEGORIES.contains(category);
    }

    public static boolean isValidProductStatus(String status) {
        return status != null && PRODUCT_STATUS.contains(status);
    }

    public static boolean isValidMemberLevel(String level) {
        return level != null && MEMBER_LEVELS.contains(level);
    }

    /** 开单步骤只能前进：目标步骤序号严格大于当前步骤 */
    public static boolean canForwardStep(String from, String to) {
        int i = ORDER_STEP.indexOf(from);
        int j = ORDER_STEP.indexOf(to);
        return i >= 0 && j >= 0 && j > i;
    }

    public static boolean isValidOrderStep(String step) {
        return step != null && ORDER_STEP.contains(step);
    }

    public static boolean isValidSalesOrderStatus(String status) {
        return status != null && SALES_ORDER_STATUS.contains(status);
    }

    public static boolean isValidAfterSaleHandle(String handle) {
        return handle != null && AFTER_SALE_HANDLE.contains(handle);
    }

    /** 售后状态只能由 待处理 → 已处理 */
    public static boolean canProcessAfterSale(String from) {
        return "待处理".equals(from);
    }

    public static boolean isValidAfterSaleStatus(String status) {
        return status != null && AFTER_SALE_STATUS.contains(status);
    }
}
