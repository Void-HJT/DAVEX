package DavexBase.common;

import java.util.ArrayList;
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
// 检查是否有 "-C" 的部分
        if (uid.matches("^DAVEX-C[A-Za-z]*\\d+$")) {
            return "center"; // 格式为 DAVEX-CXXn 的类型
        } else if (uid.matches("^DAVEX-C[A-Za-z]*\\d+-A[A-Za-z]*\\d+$")) {
            return "application"; // 格式为 DAVEX-CXXn-AXXn 的类型
        } else if (uid.matches("^DAVEX-C[A-Za-z]*\\d+-G[A-Za-z]*\\d+$")) {
            return "agent"; // 格式为 DAVEX-CXXn-GXXn 的类型
        } else if (uid.matches("^DAVEX-C[A-Za-z]*\\d+-G[A-Za-z]*\\d+-F\\d+$") || uid.matches("^DAVEX-C[A-Za-z]*\\d+-F\\d+$")) {
            return "folder"; // 格式为 DAVEX-CXXn-GXXn-Fn 的类型
        } else if (uid.matches("^DAVEX-C[A-Za-z]*\\d+-G[A-Za-z]*\\d-D\\d+$") || uid.matches("^DAVEX-C[A-Za-z]*\\d+-D\\d+$")) {
            return "file"; // 格式为 DAVEX-CXXn-GXXn-Dn 的类型
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

        // 使用 ArrayList 动态存储数字
        ArrayList<Integer> numbers = new ArrayList<>();

        if (!this.getUidType().equals("UNKNOWN")) {
            // 找到所有的数字
            while (matcher.find()) {
                // 将找到的数字添加到动态数组中
                numbers.add(Integer.parseInt(matcher.group()));
            }

            // 只找到一个数字时，返回单个数字数组
            if (numbers.size() == 1) {
                return new int[] { numbers.get(0) };
            }
        }

        // 将 ArrayList 转换为 int[]
        return numbers.stream().mapToInt(i -> i).toArray();
    }
}
