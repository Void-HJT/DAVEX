package DveAgent.info;

public class CompileParameter {
    // 参数类型
    public enum CompileParameterType {
        // 命令行直接传参，为该类型时，value表示参数传参时的位置
        ARG,
        // 命令行传参，为该类型时，value表示参数传参时的值 如 --name=123
        FLAG
    }

    private String name;
    private CompileParameterType parameterType;
    private Object value;
    private Boolean required;
    private String description;

    public CompileParameter(String name, CompileParameterType parameterType, Object value, String description,
            Boolean required) {
        this.name = name;
        this.parameterType = parameterType;
        setValue(value);
        this.description = description;
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CompileParameterType getParameterType() {
        return parameterType;
    }

    public void setParameterType(CompileParameterType parameterType) {
        this.parameterType = parameterType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        if (this.parameterType == CompileParameterType.ARG && !(value instanceof Integer)) {
            throw new IllegalArgumentException("Value must be an Integer when parameterType is ARG");
        } else if (this.parameterType == CompileParameterType.FLAG && !(value instanceof String)) {
            throw new IllegalArgumentException("Value must be a String when parameterType is FLAG");
        }
        this.value = value;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
