package DavexBase.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandleUid {

    private String uid;

    // 构造函数，传入uid进行解析
    public HandleUid(String uid) {
        this.uid = uid;
    }

    /**
     * 获取uid的类型，预留未来扩展空间。
     * @return uid的类型
     */
    public String getUidType() {
        if (uid == null || !uid.startsWith("DAVEX")) {
            return "UNKNOWN";
        }

        // 检查是否有 "-C" 的部分
        if (uid.matches("^DAVEX-C\\d+$")) {
            return "center"; // 格式为 DAVEX-Cn 的类型
        } else if (uid.matches("^DAVEX-C\\d+-AXX\\d+$")) {
            return "application"; // 格式为 DAVEX-Cn-AXXn 的类型
        } else if (uid.matches("^DAVEX-C\\d+-GXX\\d+$")) {
            return "agent"; // 格式为 DAVEX-Cn-GXXn 的类型
        } else if (uid.matches("^DAVEX-C\\d+-FXX\\d+$")) {
            return "folder"; // 格式为 DAVEX-Cn-FXXn 的类型
        } else if (uid.matches("^DAVEX-C\\d+-DXX\\d+$")) {
            return "file"; // 格式为 DAVEX-Cn-DXXn 的类型
        }

        // 其他扩展类型可以在这里添加
        return "UNKNOWN";
    }

    /**
     * 获取uid中出现的数字。
     * 如果存在中间和末尾两个数字，分别返回中间数字和末尾数字。
     * @return 一个包含中间和末尾数字的数组（可以只包含一个数字）
     */
    public int[] getNumbers() {
        // 正则表达式匹配数字
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(uid);

        // 数字数组，最多可以存两个数字
        int[] numbers = new int[2];
        int index = 0;
        if(this.getUidType()!="UNKNOWN"){
            // 找到所有的数字
            while (matcher.find()) {
                if (index < numbers.length) {
                    numbers[index] = Integer.parseInt(matcher.group());
                    index++;
                }
            }

            // 只找到一个数字时，返回单个数字
            if (index == 1) {
                return new int[] { numbers[0] };
            }
        }
        return numbers;
    }
}
