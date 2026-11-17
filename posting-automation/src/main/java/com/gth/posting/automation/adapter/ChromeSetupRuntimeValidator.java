package com.gth.posting.automation.adapter;

import com.gth.posting.application.dto.SetupSettings;
import com.gth.posting.application.port.out.configuration.SetupRuntimeValidator;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Bộ chuyển đổi kiểm tra Node.js và MCP bridge trên máy đang chạy ứng dụng. */
@Component
public class ChromeSetupRuntimeValidator implements SetupRuntimeValidator {

    /** Kiểm tra cài đặt Chrome chỉ khi người vận hành yêu cầu bật tự động hóa. */
    @Override
    public void validate(SetupSettings settings) {
        if (!settings.mcpEnabled()) {
            return;
        }
        validateNodeCommand(settings.chromeCommand());
        validateBridgeScript(settings.chromeScript());
    }

    /** Tìm lệnh Node.js theo đường dẫn tuyệt đối hoặc trong PATH hiện tại. */
    private void validateNodeCommand(String command) {
        if (isBlank(command)) {
            throw new IllegalArgumentException("Chưa chọn chương trình Node.js.");
        }
        Path commandPath;
        try {
            commandPath = Path.of(command);
        } catch (InvalidPathException exception) {
            throw new IllegalArgumentException("Đường dẫn Node.js không hợp lệ.");
        }
        if (commandPath.isAbsolute()) {
            if (!Files.isRegularFile(commandPath) || !Files.isExecutable(commandPath)) {
                throw new IllegalArgumentException("Không tìm thấy chương trình Node.js tại đường dẫn đã nhập.");
            }
            return;
        }
        if (!findInPath(command)) {
            throw new IllegalArgumentException("Không tìm thấy Node.js. Hãy cài Node.js hoặc nhập đường dẫn đầy đủ.");
        }
    }

    /** Bảo đảm tệp chạy MCP bridge tồn tại trước khi lưu cấu hình bật kết nối. */
    private void validateBridgeScript(String script) {
        if (isBlank(script)) {
            throw new IllegalArgumentException("Chưa chọn tệp MCP bridge dùng để kết nối Chrome.");
        }
        try {
            if (!Files.isRegularFile(Path.of(script))) {
                throw new IllegalArgumentException("Không tìm thấy tệp MCP bridge tại đường dẫn đã nhập.");
            }
        } catch (InvalidPathException exception) {
            throw new IllegalArgumentException("Đường dẫn tệp MCP bridge không hợp lệ.");
        }
    }

    /** Tìm chương trình có tên tương ứng trong các thư mục được khai báo ở PATH. */
    private boolean findInPath(String command) {
        String pathValue = System.getenv("PATH");
        if (isBlank(pathValue)) {
            return false;
        }
        List<String> candidates = new ArrayList<>(List.of(command));
        if (isWindows() && !command.toLowerCase().endsWith(".exe")) {
            candidates.add(command + ".exe");
        }
        for (String directory : pathValue.split(File.pathSeparator)) {
            for (String candidate : candidates) {
                Path executable = Path.of(directory, candidate);
                if (Files.isRegularFile(executable) && (isWindows() || Files.isExecutable(executable))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Nhận diện Windows để áp dụng cách tìm tệp thực thi phù hợp. */
    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    /** Kiểm tra chuỗi có null hoặc chỉ chứa khoảng trắng hay không. */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
