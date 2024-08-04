package DveCommon.info;

import java.util.Arrays;

public class Parameter {
    // 参数类型
    public enum ArgumentsType {
        // 位置参数
        POS,
        // 选项参数
        FLAG
    }

    public enum LimitType {
        NUM,
        ENUM,
        STRING
    }

    public static class NUMLimit<T extends Number> {
        private T min;
        private T max;
        private T defaultValue;

        public NUMLimit() {
        }

        public NUMLimit(T min, T max) {
            this.min = min;
            this.max = max;
        }

        public T getMin() {
            return min;
        }

        public void setMin(T min) {
            this.min = min;
        }

        public T getMax() {
            return max;
        }

        public void setMax(T max) {
            this.max = max;
        }

        public T getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(T defaultValue) {
            this.defaultValue = defaultValue;
        }

    }

    public static class ENUMLimit {
        private String[] values;
        private String defaultValue;

        public ENUMLimit() {
        }

        public ENUMLimit(String[] values, String defaultValue) {
            if (!Arrays.asList(values).contains(defaultValue)) {
                throw new IllegalArgumentException("defaultValue must be in values");
            }
            this.values = values;
            this.defaultValue = defaultValue;
        }

        public String[] getValues() {
            return values;
        }

        public void setValues(String[] values) {
            this.values = values;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    public static class STRINGLimit {
        private String defaultValue;

        public STRINGLimit() {
        }

        public STRINGLimit(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    private String name;
    private ArgumentsType parameterType;
    private Object posORflag;
    private LimitType limitType;
    private Object limit;
    private Boolean required;
    private String description;

    public Parameter(String name, ArgumentsType parameterType, LimitType limitType, Object posORflag,
            Object limit, String description,
            Boolean required) {
        this.name = name;
        this.parameterType = parameterType;
        setPosORflag(posORflag);
        this.limitType = limitType;
        setLimit(limit);
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

    public ArgumentsType getParameterType() {
        return parameterType;
    }

    public void setParameterType(ArgumentsType parameterType) {
        this.parameterType = parameterType;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Object getPosORflag() {
        switch (parameterType) {
            case POS:
                return (Integer) posORflag;
            case FLAG:
            default:
                return (String) posORflag;
        }
    }

    public void setPosORflag(Object posORflag) {
        switch (parameterType) {
            case POS:
                if (posORflag instanceof Integer) {
                    this.posORflag = posORflag;
                } else {
                    throw new IllegalArgumentException("posORflag must be Integer:"+posORflag.toString());
                }
                break;
            case FLAG:
            default:
                if (posORflag instanceof String) {
                    this.posORflag = posORflag;
                } else {
                    throw new IllegalArgumentException("posORflag must be String:"+posORflag.toString());
                }
                break;
        }
    }

    public Object getLimit() {
        return limit;
    }

    public void setLimit(Object limit) {
        switch (limitType) {
            case NUM:
                if (!(limit instanceof NUMLimit<?>)) {
                    throw new IllegalArgumentException("limit must be NUMLimit");
                }
                break;

            case ENUM:
                if (!(limit instanceof ENUMLimit)) {
                    throw new IllegalArgumentException("limit must be ENUMLimit");
                }
                break;
            case STRING:
            default:
                if (!(limit instanceof STRINGLimit)) {
                    throw new IllegalArgumentException("limit must be STRINGLimit");
                }
                break;
        }
        this.limit = limit;
    }

    public LimitType getLimitType() {
        return limitType;
    }

    public void setLimitType(LimitType limitType) {
        this.limitType = limitType;
    }
}
