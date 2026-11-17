package com.gth.posting.automation.mcpgateway;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cau noi duy nhat toi cac MCP server automation.
 *
 * Spring AI 2.0 tao san bean {@code List<McpSyncClient>} tu cau hinh
 * spring.ai.mcp.client.stdio.connections.*. Gateway nay tim server nao
 * co tool can goi, nen them mot MCP server moi chi la them cau hinh.
 */
@Component
public class McpToolGateway {

    private static final Logger log = LoggerFactory.getLogger(McpToolGateway.class);

    private final ObjectProvider<List<McpSyncClient>> clientsProvider;
    private final Map<String, McpSyncClient> ownerOfTool = new LinkedHashMap<>();

    public McpToolGateway(ObjectProvider<List<McpSyncClient>> clientsProvider) {
        this.clientsProvider = clientsProvider;
    }

    public List<McpSyncClient> clients() {
        List<McpSyncClient> clients = clientsProvider.getIfAvailable(List::of);
        return clients == null ? List.of() : clients;
    }

    public boolean isAvailable() {
        return !clients().isEmpty();
    }

    /** Danh sach ten tool cua tat ca server dang ket noi. Dung cho health check. */
    public List<String> availableTools() {
        List<String> names = new ArrayList<>();
        for (McpSyncClient client : clients()) {
            try {
                McpSchema.ListToolsResult result = client.listTools();
                result.tools().forEach(tool -> names.add(tool.name()));
            } catch (RuntimeException ex) {
                log.warn("Khong lay duoc danh sach tool tu MCP client: {}", ex.getMessage());
            }
        }
        return names;
    }

    public boolean supports(String toolName) {
        return resolveClient(toolName) != null;
    }

    public Optional<String> call(String toolName, Map<String, Object> arguments) {
        McpSyncClient client = resolveClient(toolName);
        if (client == null) {
            log.warn("Khong MCP server nao cung cap tool '{}'. Dang co: {}", toolName, availableTools());
            return Optional.empty();
        }
        try {
            McpSchema.CallToolResult result = client.callTool(
                    McpSchema.CallToolRequest.builder(toolName).arguments(arguments).build());
            return Optional.of(render(result));
        } catch (RuntimeException ex) {
            log.warn("Goi tool '{}' that bai: {}", toolName, ex.getMessage());
            return Optional.empty();
        }
    }

    private McpSyncClient resolveClient(String toolName) {
        McpSyncClient cached = ownerOfTool.get(toolName);
        if (cached != null) {
            return cached;
        }
        for (McpSyncClient client : clients()) {
            try {
                boolean owned = client.listTools().tools().stream()
                        .anyMatch(tool -> tool.name().equals(toolName));
                if (owned) {
                    ownerOfTool.put(toolName, client);
                    return client;
                }
            } catch (RuntimeException ex) {
                log.debug("Bo qua MCP client loi: {}", ex.getMessage());
            }
        }
        return null;
    }

    /** Gom toan bo text content thanh mot chuoi de adapter tu phan tich. */
    private String render(McpSchema.CallToolResult result) {
        StringBuilder sb = new StringBuilder();
        if (result.content() != null) {
            for (McpSchema.Content content : result.content()) {
                if (content instanceof McpSchema.TextContent text) {
                    if (!sb.isEmpty()) {
                        sb.append('\n');
                    }
                    sb.append(text.text());
                }
            }
        }
        if (Boolean.TRUE.equals(result.isError())) {
            sb.append("\n[isError=true]");
        }
        return sb.toString();
    }
}
