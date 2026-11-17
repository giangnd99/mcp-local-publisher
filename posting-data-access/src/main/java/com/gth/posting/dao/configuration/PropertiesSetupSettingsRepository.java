package com.gth.posting.dao.configuration;

import com.gth.posting.application.dto.SetupSettings;
import com.gth.posting.application.port.out.configuration.SetupSettingsRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/** Bộ chuyển đổi lưu cấu hình cài đặt vào tệp properties bên ngoài ứng dụng. */
@Component
public class PropertiesSetupSettingsRepository implements SetupSettingsRepository {

    private static final Path SETTINGS_FILE = Path.of("config", "posting-settings.properties");

    /** Đọc cấu hình từ tệp; nếu chưa có tệp thì trả về cấu hình mặc định dễ chạy. */
    @Override
    public SetupSettings load() {
        Properties properties = new Properties();
        if (Files.exists(SETTINGS_FILE)) {
            try (InputStream input = Files.newInputStream(SETTINGS_FILE)) {
                properties.load(input);
            } catch (IOException exception) {
                throw new IllegalStateException("Không đọc được tệp cấu hình " + SETTINGS_FILE, exception);
            }
        }
        return toSettings(properties);
    }

    /** Lưu các lựa chọn và giữ lại bí mật cũ nếu ô nhập bí mật được để trống. */
    @Override
    public void save(SetupSettings settings) {
        Properties properties = readProperties();
        properties.setProperty("posting.setup.db-url", settings.databaseUrl().trim());
        properties.setProperty("posting.setup.db-user", settings.databaseUser().trim());
        properties.setProperty("posting.setup.mcp-enabled", Boolean.toString(settings.mcpEnabled()));
        properties.setProperty("posting.setup.chrome-command", safe(settings.chromeCommand(), "node"));
        properties.setProperty("posting.setup.chrome-script", safe(settings.chromeScript(), ""));
        properties.setProperty("posting.setup.ai-enabled", Boolean.toString(settings.aiEnabled()));
        properties.setProperty("posting.setup.ai-provider", settings.aiEnabled() ? "openai" : "none");
        properties.setProperty("posting.setup.page-name", safe(settings.pageName(), "Laptop GTH"));
        properties.setProperty("posting.setup.global-gap-minutes", Long.toString(settings.globalGapMinutes()));
        properties.setProperty("posting.setup.group-gap-hours", Long.toString(settings.groupGapHours()));

        if (!isBlank(settings.databasePassword())) {
            properties.setProperty("posting.setup.db-password", settings.databasePassword());
        }
        if (!isBlank(settings.openAiApiKey())) {
            properties.setProperty("posting.setup.openai-api-key", settings.openAiApiKey());
        }

        writeProperties(properties);
    }

    /** Chuyển các khóa properties thành cấu hình trung lập của tầng ứng dụng. */
    private SetupSettings toSettings(Properties properties) {
        return new SetupSettings(
                properties.getProperty("posting.setup.db-url",
                        "jdbc:postgresql://localhost:5433/postgres?currentSchema=posting"),
                properties.getProperty("posting.setup.db-user", "postgres"),
                "",
                !isBlank(properties.getProperty("posting.setup.db-password")),
                Boolean.parseBoolean(properties.getProperty("posting.setup.mcp-enabled", "false")),
                properties.getProperty("posting.setup.chrome-command", "node"),
                properties.getProperty("posting.setup.chrome-script", detectBridgeScript()),
                Boolean.parseBoolean(properties.getProperty("posting.setup.ai-enabled", "false")),
                "",
                !isBlank(properties.getProperty("posting.setup.openai-api-key")),
                properties.getProperty("posting.setup.page-name", "Laptop GTH"),
                parseLong(properties, "posting.setup.global-gap-minutes", 6L),
                parseLong(properties, "posting.setup.group-gap-hours", 12L));
    }

    /** Đọc lại tệp hiện có để cập nhật cài đặt mà không xóa các bí mật đã lưu. */
    private Properties readProperties() {
        Properties properties = new Properties();
        if (!Files.exists(SETTINGS_FILE)) {
            return properties;
        }
        try (InputStream input = Files.newInputStream(SETTINGS_FILE)) {
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Không đọc được tệp cấu hình " + SETTINGS_FILE, exception);
        }
    }

    /** Ghi cấu hình ra đĩa và giới hạn quyền đọc tệp trên hệ thống hỗ trợ POSIX. */
    private void writeProperties(Properties properties) {
        try {
            Files.createDirectories(SETTINGS_FILE.getParent());
            try (OutputStream output = Files.newOutputStream(SETTINGS_FILE)) {
                properties.store(output, "Cau hinh duoc tao tu giao dien Posting Management");
            }
            restrictFilePermissions();
        } catch (IOException exception) {
            throw new IllegalStateException("Không lưu được tệp cấu hình " + SETTINGS_FILE, exception);
        }
    }

    /** Chỉ cho phép tài khoản đang chạy ứng dụng đọc và sửa tệp chứa thông tin bí mật. */
    private void restrictFilePermissions() throws IOException {
        Set<PosixFilePermission> permissions = EnumSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE);
        try {
            Files.setPosixFilePermissions(SETTINGS_FILE, permissions);
        } catch (UnsupportedOperationException ignored) {
            // Hệ điều hành không hỗ trợ POSIX sẽ dùng quyền mặc định của thư mục người dùng.
        }
    }

    /** Đọc số nguyên trong tệp và dùng giá trị dự phòng nếu cấu hình bị thiếu hoặc sai. */
    private long parseLong(Properties properties, String key, long defaultValue) {
        try {
            return Long.parseLong(properties.getProperty(key, Long.toString(defaultValue)));
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    /** Tự tìm MCP bridge ở các thư mục cài đặt Node.js phổ biến trên máy hiện tại. */
    private String detectBridgeScript() {
        Path home = Path.of(System.getProperty("user.home", "."));
        List<Path> nodeRoots = new ArrayList<>();
        nodeRoots.add(home.resolve(".nvm/versions/node"));
        nodeRoots.add(home.resolve(".npm-global/lib/node_modules"));
        nodeRoots.add(Path.of("/usr/local/lib/node_modules"));
        nodeRoots.add(Path.of("/usr/lib/node_modules"));
        String appData = System.getenv("APPDATA");
        if (!isBlank(appData)) {
            nodeRoots.add(Path.of(appData, "npm", "node_modules"));
        }

        for (Path root : nodeRoots) {
            String detected = findBridgeUnder(root);
            if (!isBlank(detected)) {
                return detected;
            }
        }
        return "";
    }

    /** Tìm tệp chạy bridge trong thư mục gốc hoặc các phiên bản Node.js con của NVM. */
    private String findBridgeUnder(Path root) {
        Path direct = root.resolve("mcp-chrome-bridge/dist/mcp/mcp-server-stdio.js");
        if (Files.isRegularFile(direct)) {
            return direct.toAbsolutePath().toString();
        }
        if (!Files.isDirectory(root)) {
            return "";
        }
        try (var versions = Files.list(root)) {
            return versions.map(version -> version.resolve(
                            "lib/node_modules/mcp-chrome-bridge/dist/mcp/mcp-server-stdio.js"))
                    .filter(Files::isRegularFile)
                    .map(path -> path.toAbsolutePath().toString())
                    .findFirst()
                    .orElse("");
        } catch (IOException exception) {
            return "";
        }
    }

    /** Trả về giá trị dự phòng khi chuỗi đầu vào không có nội dung. */
    private String safe(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value.trim();
    }

    /** Kiểm tra chuỗi có null hoặc chỉ chứa khoảng trắng hay không. */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
