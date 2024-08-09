package DveBase.info;

import java.util.List;
import java.util.Map;

public class QueryRequest {

    // 表名
    private String tableName;

    // 要查询的列名列表
    private List<String> columns;

    // 查询条件，键为列名，值为条件值
    private Map<String, Object> conditions;

    // 排序列及顺序（ASC/DESC）
    private Map<String, String> orderBy;

    // 分页参数：偏移量
    private Integer offset;

    // 分页参数：限制条数
    private Integer limit;

    // getters and setters

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public Map<String, Object> getConditions() {
        return conditions;
    }

    public void setConditions(Map<String, Object> conditions) {
        this.conditions = conditions;
    }

    public Map<String, String> getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(Map<String, String> orderBy) {
        this.orderBy = orderBy;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
