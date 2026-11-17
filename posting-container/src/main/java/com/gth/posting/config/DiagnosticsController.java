package com.gth.posting.config;

import com.gth.posting.automation.mcpgateway.McpToolGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Chan doan ket noi MCP. Chi de van hanh noi bo tren may shop.
 * Tra ve danh sach tool that do MCP server cung cap, de doi chieu voi
 * posting.automation.tools.* trong application.yml.
 */
@RestController
@RequestMapping("/api/diagnostics")
public class DiagnosticsController {

    private final McpToolGateway gateway;

    public DiagnosticsController(McpToolGateway gateway) {
        this.gateway = gateway;
    }

    @GetMapping("/mcp")
    public Map<String, Object> mcp() {
        List<String> tools = gateway.availableTools();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("available", gateway.isAvailable());
        body.put("clients", gateway.clients().size());
        body.put("tools", tools);
        return body;
    }

    @GetMapping("/mcp/call")
    public Map<String, Object> call(@RequestParam String tool,
                                    @RequestParam(required = false) String code,
                                    @RequestParam(required = false) String url,
                                    @RequestParam(required = false) String selector,
                                    @RequestParam(required = false) String filePath,
                                    @RequestParam(required = false) Boolean newWindow) {
        Map<String, Object> args = new LinkedHashMap<>();
        if (code != null) {
            args.put("code", code);
        }
        if (url != null) {
            args.put("url", url);
        }
        if (selector != null) {
            args.put("selector", selector);
        }
        if (filePath != null) {
            args.put("filePath", filePath);
        }
        if (newWindow != null) {
            args.put("newWindow", newWindow);
        }
        var result = gateway.call(tool, args);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tool", tool);
        body.put("ok", result.isPresent());
        body.put("raw", result.orElse(""));
        return body;
    }
}
