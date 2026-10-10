package com.library.vo;

import lombok.Data;

@Data
public class StudentCardQueryVO {
    private String sno;
    private String studentName;
    /** 学生类型：本科生/研究生 */
    private String studentType;
    private String college;
    private String major;
    /** 是否毕业：0未毕业 1已毕业 */
    private Integer graduated;
    /** 当前借书证号，无证时为 null */
    private String cardNo;
    /** 借书证状态码：0注销 1正常 2挂失，无证时为 null */
    private Integer status;
    /** 是否可新办 */
    private Boolean canCreate;
    /** 新办/补办时将生成的证号预览 */
    private String previewCardNo;
}
