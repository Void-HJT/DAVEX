package DavexBase.info;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.sql.Timestamp;

/**
 * 判决书文件筛选条件DTO
 * 所有字段均为可选项，未传则不参与筛选
 */
@Data
public class VerdictFilterDTO {
    /**
     * 判决时间-开始（时间段筛选：>= startTime）
     * 示例："2020-01-01 00:00:00"（需与前端约定时间格式）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp judgeTimeStart;

    /**
     * 判决时间-结束（时间段筛选：<= endTime）
     * 示例："2024-12-31 23:59:59"
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp judgeTimeEnd;

    /**
     * 判决书类型（模糊筛选：包含该字符串）
     * 示例："刑事判决书"
     */
    private String judgeType;

    /**
     * 判决地点（模糊筛选：包含该字符串）
     * 示例："北京市"
     */
    private String judgeDistrict;

    /**
     * 案由（模糊筛选：包含该字符串）
     * 示例："贪污罪"
     */
    private String judgeCause;
}
