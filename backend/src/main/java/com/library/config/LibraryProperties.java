package com.library.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 图书馆业务规则配置类，对应 application.yml 中 library.* 前缀。
 * 将超期罚款、借阅上限等可调参数外部化，便于运维修改无需改代码。
 * 所有字段均有默认值，不配 yml 也能正常启动运行。
 */
@Data
@Component
@ConfigurationProperties(prefix = "library")
public class LibraryProperties {

    /** 默认借阅天数，从借书日起算，超过此天数视为逾期 */
    private int borrowDays = 30;

    /** 超期每天罚款金额（元），逾期罚款 = 逾期天数 * 此值 */
    private BigDecimal finePerDay = new BigDecimal("1.00");

    /** 未缴罚款累计超过此金额（元）则禁止借书 */
    private BigDecimal maxUnpaidFine = new BigDecimal("20.00");

    /** 本科生最大同时在借数量，超出后不可再借 */
    private int undergradBorrowLimit = 5;

    /** 研究生最大同时在借数量，比本科生更高 */
    private int gradBorrowLimit = 10;
}
