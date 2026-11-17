package com.gth.posting.presentation.controller;

import com.gth.posting.application.dto.SetupSettings;
import com.gth.posting.application.usecase.setupsettings.GetSetupSettingsUseCase;
import com.gth.posting.application.usecase.setupsettings.SaveSetupSettingsUseCase;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Bộ điều khiển trang thiết lập, chỉ gọi các ca sử dụng của tầng ứng dụng. */
@Controller
public class SetupPageController {

    private final GetSetupSettingsUseCase getSetupSettings;
    private final SaveSetupSettingsUseCase saveSetupSettings;
    private final int serverPort;

    /** Nhận các ca sử dụng cần thiết để đọc và lưu lựa chọn của người vận hành. */
    public SetupPageController(GetSetupSettingsUseCase getSetupSettings,
                               SaveSetupSettingsUseCase saveSetupSettings,
                               @Value("${server.port:8085}") int serverPort) {
        this.getSetupSettings = getSetupSettings;
        this.saveSetupSettings = saveSetupSettings;
        this.serverPort = serverPort;
    }

    /** Hiển thị trang cấu hình thân thiện thay cho việc sửa trực tiếp tệp YAML. */
    @GetMapping({"/", "/setup"})
    public String showSetup(@RequestParam(required = false) String saved, Model model) {
        addSettings(model, getSetupSettings.handle());
        model.addAttribute("saved", "true".equals(saved));
        model.addAttribute("mcpEndpoint", "http://127.0.0.1:" + serverPort + "/mcp");
        return "setup";
    }

    /** Nhận cấu hình từ biểu mẫu, lưu qua ca sử dụng và báo lỗi ngay trên trang. */
    @PostMapping("/setup")
    public String saveSetup(
            @RequestParam String databaseUrl,
            @RequestParam String databaseUser,
            @RequestParam(defaultValue = "") String databasePassword,
            @RequestParam(defaultValue = "false") boolean mcpEnabled,
            @RequestParam(defaultValue = "node") String chromeCommand,
            @RequestParam(defaultValue = "") String chromeScript,
            @RequestParam(defaultValue = "false") boolean aiEnabled,
            @RequestParam(defaultValue = "") String openAiApiKey,
            @RequestParam(defaultValue = "Laptop GTH") String pageName,
            @RequestParam long globalGapMinutes,
            @RequestParam long groupGapHours,
            Model model,
            RedirectAttributes redirectAttributes) {
        SetupSettings settings = new SetupSettings(databaseUrl, databaseUser, databasePassword, false,
                mcpEnabled, chromeCommand, chromeScript, aiEnabled, openAiApiKey, false,
                pageName, globalGapMinutes, groupGapHours);
        try {
            saveSetupSettings.handle(settings);
            redirectAttributes.addAttribute("saved", "true");
            return "redirect:/";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            addSettings(model, settings);
            model.addAttribute("mcpEndpoint", "http://127.0.0.1:" + serverPort + "/mcp");
            model.addAttribute("error", exception.getMessage());
            return "setup";
        }
    }

    /** Đưa dữ liệu cấu hình an toàn cho giao diện; các bí mật luôn được bỏ trống. */
    private void addSettings(Model model, SetupSettings settings) {
        model.addAttribute("settings", settings);
        model.addAttribute("databasePasswordConfigured", settings.databasePasswordConfigured());
        model.addAttribute("openAiApiKeyConfigured", settings.openAiApiKeyConfigured());
    }
}
