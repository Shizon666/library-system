package com.library.common;

/**
 * 借书证相关的中文常量定义与转换工具类。
 * 数据库中证件状态、操作类型、归还状态、罚款状态等字段存储的是中文字符串，
 * 此处统一管理避免硬编码散落各处，便于维护和国际化扩展。
 */
public final class CardConstants {

    // ========== 借书证状态常量 ==========

    /** 证件正常状态，可正常借还书 */
    public static final String STATUS_NORMAL = "正常";

    /** 证件挂失状态，挂失期间不可借书，但可以还书 */
    public static final String STATUS_LOST = "挂失";

    /** 证件注销状态，注销后不可再使用 */
    public static final String STATUS_CANCELLED = "注销";

    // ========== 卡片操作类型常量（对应 cardrec 表的 op_type 字段） ==========

    /** 新办借书证操作 */
    public static final String OP_CREATE = "新办";

    /** 挂失借书证操作 */
    public static final String OP_LOSS = "挂失";

    /** 补办借书证操作（生成新卡号） */
    public static final String OP_REPLACE = "补办";

    /** 注销借书证操作 */
    public static final String OP_CANCEL = "注销";

    // ========== 归还记录状态常量（对应 borrowrec 表的 return_status 字段） ==========

    /** 尚未归还 */
    public static final String RET_UNRETURNED = "未归还";

    /** 在应还日期前归还 */
    public static final String RET_ON_TIME = "按时还";

    /** 超过应还日期后归还（产生逾期罚款） */
    public static final String RET_OVERDUE = "超时还";

    /** 通过挂失流程归还（如遗失赔偿后标记归还） */
    public static final String RET_LOST = "挂失还";

    // ========== 罚款缴纳状态常量 ==========

    /** 罚款尚未缴纳 */
    public static final String FINE_UNPAID = "未缴";

    /** 罚款已缴纳 */
    public static final String FINE_PAID = "已缴";

    /** 私有构造方法，防止工具类被实例化 */
    private CardConstants() {
    }

    /**
     * 将中文证件状态转换为数字编码（1=正常，2=挂失，0=注销）。
     * 部分 SQL 查询条件需要使用数字编码，此方法提供转换。
     * @param cardStatus 中文状态字符串
     * @return 对应的数字编码，无法识别时返回 -1
     */
    public static int cardStatusToCode(String cardStatus) {
        if (STATUS_NORMAL.equals(cardStatus)) return 1;
        if (STATUS_LOST.equals(cardStatus)) return 2;
        if (STATUS_CANCELLED.equals(cardStatus)) return 0;
        return -1;
    }

    /**
     * 将中文操作类型转换为字符串编码（"1"新办，"2"挂失，"3"补办，"4"注销）。
     * @param opType 中文操作类型
     * @return 对应的字符串编码，无法识别时返回 "0"
     */
    public static String opTypeToCode(String opType) {
        return switch (opType) {
            case OP_CREATE -> "1";
            case OP_LOSS -> "2";
            case OP_REPLACE -> "3";
            case OP_CANCEL -> "4";
            default -> "0";
        };
    }

    /**
     * 将中文操作类型转换为整数编码（1=新办，2=挂失，3=补办，4=注销）。
     * @param opType 中文操作类型
     * @return 对应的整数编码，无法识别时返回 0
     */
    public static int opTypeToInt(String opType) {
        return switch (opType) {
            case OP_CREATE -> 1;
            case OP_LOSS -> 2;
            case OP_REPLACE -> 3;
            case OP_CANCEL -> 4;
            default -> 0;
        };
    }

    /**
     * 判断学生是否为研究生类型。
     * 类型字段可能包含"研究生"字样（如"硕士研究生"），使用 contains 判断。
     * @param type 学生类型字符串
     * @return true=研究生，false=非研究生或类型为null
     */
    public static boolean isGradStudent(String type) {
        return type != null && type.contains("研究生");
    }
}
