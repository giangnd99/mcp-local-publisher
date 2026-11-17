package com.gth.posting.mcp.server.tools;

import com.gth.posting.application.dto.CampaignDto;
import com.gth.posting.application.dto.GroupSummaryDto;
import com.gth.posting.application.usecase.createcampaign.CreateCampaignCommand;
import com.gth.posting.application.usecase.createcampaign.CreateCampaignResult;
import com.gth.posting.application.usecase.createcampaign.CreateCampaignUseCase;
import com.gth.posting.application.usecase.dispatchpost.DispatchPostResult;
import com.gth.posting.application.usecase.dispatchpost.DispatchPostUseCase;
import com.gth.posting.application.usecase.findgroups.FindGroupsUseCase;
import com.gth.posting.application.usecase.findgroups.GroupSearchResult;
import com.gth.posting.application.usecase.getcampaign.GetCampaignResult;
import com.gth.posting.application.usecase.getcampaign.GetCampaignUseCase;
import com.gth.posting.application.usecase.listcampaigns.ListCampaignsUseCase;
import com.gth.posting.application.usecase.listgroups.ListGroupsUseCase;
import com.gth.posting.application.usecase.queuebatch.QueueBatchResult;
import com.gth.posting.application.usecase.queuebatch.QueueBatchUseCase;
import com.gth.posting.application.usecase.validatecampaign.ValidateCampaignResult;
import com.gth.posting.application.usecase.validatecampaign.ValidateCampaignUseCase;
import com.gth.posting.application.usecase.verifypost.VerifyPostResult;
import com.gth.posting.application.usecase.verifypost.VerifyPostUseCase;
import com.gth.posting.domain.valueobject.ContentObjective;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/** Công cụ MCP dạng inbound adapter; mọi thao tác nghiệp vụ đi qua application use case. */
@Component
public class PostingManagementMcpTools {

    private static final TypeReference<List<GroupPostInput>> GROUP_POSTS_TYPE = new TypeReference<>() { };
    private static final TypeReference<List<String>> PHOTO_PATHS_TYPE = new TypeReference<>() { };
    private static final TypeReference<List<String>> PRODUCT_FACTS_TYPE = new TypeReference<>() { };

    private final ListGroupsUseCase listGroupsUseCase;
    private final FindGroupsUseCase findGroupsUseCase;
    private final ListCampaignsUseCase listCampaignsUseCase;
    private final CreateCampaignUseCase createCampaignUseCase;
    private final GetCampaignUseCase getCampaignUseCase;
    private final ValidateCampaignUseCase validateCampaignUseCase;
    private final QueueBatchUseCase queueBatchUseCase;
    private final DispatchPostUseCase dispatchPostUseCase;
    private final VerifyPostUseCase verifyPostUseCase;
    private final ObjectMapper objectMapper;

    /** Tiêm các ca sử dụng và bộ chuyển JSON do Spring Boot cung cấp. */
    public PostingManagementMcpTools(ListGroupsUseCase listGroupsUseCase,
                                     FindGroupsUseCase findGroupsUseCase,
                                     ListCampaignsUseCase listCampaignsUseCase,
                                     CreateCampaignUseCase createCampaignUseCase,
                                     GetCampaignUseCase getCampaignUseCase,
                                     ValidateCampaignUseCase validateCampaignUseCase,
                                     QueueBatchUseCase queueBatchUseCase,
                                     DispatchPostUseCase dispatchPostUseCase,
                                     VerifyPostUseCase verifyPostUseCase,
                                     ObjectMapper objectMapper) {
        this.listGroupsUseCase = listGroupsUseCase;
        this.findGroupsUseCase = findGroupsUseCase;
        this.listCampaignsUseCase = listCampaignsUseCase;
        this.createCampaignUseCase = createCampaignUseCase;
        this.getCampaignUseCase = getCampaignUseCase;
        this.validateCampaignUseCase = validateCampaignUseCase;
        this.queueBatchUseCase = queueBatchUseCase;
        this.dispatchPostUseCase = dispatchPostUseCase;
        this.verifyPostUseCase = verifyPostUseCase;
        this.objectMapper = objectMapper;
    }

    /** Liệt kê nhóm cùng khu vực và trạng thái cho phép đăng để agent chọn chính xác. */
    @McpTool(name = "list_groups", description = "Liệt kê các nhóm Facebook đã lưu, khu vực, số thành viên và trạng thái có thể đăng.")
    public List<GroupSummaryDto> listGroups() {
        return listGroupsUseCase.handle();
    }

    /** Tìm nhóm đã lưu để agent dùng mã và tên đã xác nhận trong chiến dịch. */
    @McpTool(name = "find_groups", description = "Tìm nhóm đã lưu theo mã, tên hoặc khu vực. Chỉ trả nhóm chuyên laptop, PC, máy tính hoặc công nghệ đã bật quyền đăng; luôn loại nhóm tên chợ, rao vặt, thanh lý. Hãy dùng nhóm trả về trước khi tạo chiến dịch.")
    public List<GroupSearchResult> findGroups(
            @McpToolParam(description = "Từ khóa, có thể là mã nhóm, tên nhóm hoặc khu vực", required = true) String query,
            @McpToolParam(description = "Số kết quả tối đa, từ 1 đến 50", required = true) int limit) {
        return findGroupsUseCase.handle(query, limit);
    }

    /** Liệt kê chiến dịch hiện có để agent tiếp tục kiểm tra hoặc xếp lịch. */
    @McpTool(name = "list_campaigns", description = "Liệt kê chiến dịch đã tạo, mới nhất trước, kèm mã chiến dịch và trạng thái.")
    public List<CampaignDto> listCampaigns() {
        return listCampaignsUseCase.handle();
    }

    /** Xem chi tiết một chiến dịch và các bài riêng theo từng nhóm. */
    @McpTool(name = "get_campaign", description = "Đọc thông tin chiến dịch và các bài đăng theo nhóm bằng mã chiến dịch.")
    public GetCampaignResult getCampaign(
            @McpToolParam(description = "Mã chiến dịch cần xem", required = true) String campaignId) {
        return getCampaignUseCase.handle(campaignId);
    }

    /** Tạo chiến dịch từ nội dung riêng cho từng nhóm; công cụ này chưa đăng lên Facebook. */
    @McpTool(name = "create_campaign", description = "Tạo chiến dịch nháp với nội dung riêng cho từng nhóm đã lưu. Chưa đăng bài. Mặc định BRAND_AWARENESS; có thể chọn EDUCATIONAL, SOFT_SELL hoặc DIRECT_SALE. Nếu bỏ trống brandName, mặc định Laptop GTH; brandPromise mô tả lời hứa đã xác nhận của thương hiệu. Hãy gọi list_groups trước; productFactsJson là dữ kiện sản phẩm đã xác nhận; groupsJson gồm groupId và text (để text rỗng để ứng dụng tự soạn theo mục tiêu, không cần API key khi AI tắt); photoPathsJson là mảng đường dẫn ảnh hoặc [].")
    public CreateCampaignResult createCampaign(
            @McpToolParam(description = "Tên chiến dịch", required = true) String name,
            @McpToolParam(description = "Tên sản phẩm", required = true) String productName,
            @McpToolParam(description = "Giá theo VND; chỉ cần khi mục tiêu DIRECT_SALE hoặc SOFT_SELL", required = false) Long priceAmount,
            @McpToolParam(description = "Thông tin bảo hành đã xác nhận; có thể bỏ trống", required = false) String warrantyText,
            @McpToolParam(description = "Số điện thoại/Zalo; có thể bỏ trống với bài nhận diện hoặc chia sẻ kiến thức", required = false) String phoneNumber,
            @McpToolParam(description = "Mục tiêu: BRAND_AWARENESS (mặc định), EDUCATIONAL, SOFT_SELL hoặc DIRECT_SALE", required = false) String contentObjective,
            @McpToolParam(description = "Mảng JSON thông tin đã xác nhận như cấu hình, tình trạng; ví dụ [\"Core i5\",\"RAM 8GB\"]. Có thể bỏ trống nếu chưa có", required = false) String productFactsJson,
            @McpToolParam(description = "Tên thương hiệu cần quảng bá; mặc định Laptop GTH", required = false) String brandName,
            @McpToolParam(description = "Lời hứa/định vị thương hiệu dựa trên thông tin đã xác nhận; mặc định GTH hiểu nhu cầu, chọn máy phù hợp và chăm sóc sau mua", required = false) String brandPromise,
            @McpToolParam(description = "Mảng JSON đường dẫn ảnh trên máy chạy ứng dụng, ví dụ [\"/data/photo.jpg\"]", required = true) String photoPathsJson,
            @McpToolParam(description = "Mảng JSON nhóm đã chọn từ list_groups. Mỗi phần tử cần groupId; text có thể để rỗng để ứng dụng tự soạn.", required = true) String groupsJson) {
        List<String> photoPaths = parseJson(photoPathsJson, PHOTO_PATHS_TYPE, "Danh sách đường dẫn ảnh");
        List<String> productFacts = productFactsJson == null || productFactsJson.isBlank()
                ? List.of() : parseJson(productFactsJson, PRODUCT_FACTS_TYPE, "Thông tin sản phẩm");
        List<GroupPostInput> groupPosts = parseJson(groupsJson, GROUP_POSTS_TYPE, "Danh sách bài theo nhóm");
        List<CreateCampaignCommand.GroupInput> groups = groupPosts.stream()
                .map(group -> new CreateCampaignCommand.GroupInput(
                        group.groupId(), group.name(), group.memberCount(), group.region(), group.text()))
                .toList();
        return createCampaignUseCase.handle(new CreateCampaignCommand(
                name, productName, priceAmount == null ? 0 : priceAmount, warrantyText, phoneNumber,
                ContentObjective.from(contentObjective), productFacts, brandName, brandPromise, photoPaths, groups));
    }

    /** Kiểm tra nội dung và trạng thái các bài trước khi xếp lịch. */
    @McpTool(name = "validate_campaign", description = "Kiểm tra chiến dịch trước khi xếp lịch: nội dung, trùng lặp và điều kiện đăng.")
    public ValidateCampaignResult validateCampaign(
            @McpToolParam(description = "Mã chiến dịch cần kiểm tra", required = true) String campaignId) {
        return validateCampaignUseCase.handle(campaignId);
    }

    /** Xếp các bài hợp lệ thành lô theo giới hạn nhịp đăng cấu hình trong ứng dụng. */
    @McpTool(name = "queue_campaign", description = "Xếp tối đa 20 bài của chiến dịch vào lịch đăng. Công cụ chỉ xếp lịch, chưa đăng lên Facebook.")
    public QueueBatchResult queueCampaign(
            @McpToolParam(description = "Mã chiến dịch cần xếp lịch", required = true) String campaignId,
            @McpToolParam(description = "Số bài tối đa đưa vào lô, từ 1 đến 20", required = true) int batchSize) {
        return queueBatchUseCase.handle(campaignId, batchSize);
    }

    /** Đăng một bài đã đến hạn; đây là thao tác có tác động thật lên Facebook. */
    @McpTool(name = "dispatch_next_post", description = "ĐĂNG THẬT một bài đến hạn trong chiến dịch lên Facebook bằng Chrome đang đăng nhập. Chỉ gọi khi người dùng yêu cầu đăng; chịu giới hạn nhịp đăng và trạng thái kiểm duyệt của nhóm.")
    public DispatchPostResult dispatchNextPost(
            @McpToolParam(description = "Mã chiến dịch có bài đã xếp lịch", required = true) String campaignId) {
        return dispatchPostUseCase.handle(campaignId);
    }

    /** Tìm lại bài trong đúng feed nhóm khi lần đăng trước chưa thể xác nhận. */
    @McpTool(name = "verify_post", description = "Tìm bài theo câu riêng trong feed của đúng nhóm để xác nhận trạng thái. Dùng postJobId từ kết quả get_campaign sau dispatch_next_post có trạng thái SUBMITTING hoặc PENDING_APPROVAL.")
    public VerifyPostResult verifyPost(
            @McpToolParam(description = "Mã bài đăng cần xác minh", required = true) String postJobId) {
        return verifyPostUseCase.handle(postJobId);
    }

    /** Đọc một mảng JSON thành kiểu dữ liệu cần thiết và trả lỗi rõ ràng khi JSON sai. */
    private <T> T parseJson(String json, TypeReference<T> type, String fieldName) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException(fieldName + " không được để trống.");
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException(fieldName + " không đúng định dạng JSON.", exception);
        }
    }

    /** Dữ liệu MCP nhận cho một nội dung đăng riêng theo nhóm Facebook. */
    public record GroupPostInput(String groupId, String name, int memberCount, String region, String text) {
    }
}
