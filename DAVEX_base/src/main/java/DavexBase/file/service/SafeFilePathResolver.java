package DavexBase.file.service;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * 在配置的数据根目录内安全构造文件路径。
 * 用户文件名和数据库目录名都只能是单个普通路径段。
 */
@Component
public class SafeFilePathResolver {

    public Path resolve(Path dataRoot, List<String> directoryNames, String fileName) {

        if (dataRoot == null) {
            throw new IllegalArgumentException("数据根目录不能为空");
        }

        Path normalizedRoot = dataRoot.toAbsolutePath().normalize();
        Path resolved = normalizedRoot;

        if (directoryNames != null) {
            for (String directoryName : directoryNames) {
                validateSegment(directoryName, "目录名");
                resolved = resolved.resolve(directoryName);
            }
        }

        validateSegment(fileName, "文件名");
        resolved = resolved.resolve(fileName).normalize();

        // 最终路径必须仍然位于配置的数据根目录下。
        if (!resolved.startsWith(normalizedRoot)) {
            throw new IllegalArgumentException("文件路径越出数据根目录");
        }

        return resolved;
    }

    private void validateSegment(String segment, String fieldName) {
        if (segment == null || segment.isBlank()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }

        if (".".equals(segment)
                || "..".equals(segment)
                || segment.contains("/")
                || segment.contains("\\")
                || segment.contains(":")
                || Path.of(segment).isAbsolute()) {
            throw new IllegalArgumentException(fieldName + "包含非法路径内容");
        }
    }
}