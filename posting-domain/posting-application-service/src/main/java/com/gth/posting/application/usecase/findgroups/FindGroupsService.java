package com.gth.posting.application.usecase.findgroups;

import com.gth.posting.application.dto.GroupSummaryDto;
import com.gth.posting.application.port.out.repository.GroupRepository;
import com.gth.posting.domain.entity.FbGroup;
import com.gth.posting.domain.service.GroupPostingPolicy;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Tìm nhóm đã xác nhận bằng mã chính xác và mức độ khớp tên/khu vực. */
@Service
public class FindGroupsService implements FindGroupsUseCase {

    private static final int MAX_LIMIT = 50;
    private final GroupRepository groupRepository;

    /** Khởi tạo ca sử dụng với cổng đọc nhóm đã lưu. */
    public FindGroupsService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    /** Chuẩn hóa từ khóa, chấm điểm từng nhóm rồi trả danh sách theo mức khớp. */
    @Override
    public List<GroupSearchResult> handle(String query, int limit) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Từ khóa tìm nhóm không được để trống.");
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("Số kết quả phải nằm trong khoảng 1 đến " + MAX_LIMIT + ".");
        }

        String normalizedQuery = normalize(query);
        if (normalizedQuery.isBlank()) {
            throw new IllegalArgumentException("Từ khóa cần có chữ cái hoặc mã nhóm.");
        }
        String[] tokens = normalizedQuery.split(" ");
        return groupRepository.findAll().stream()
                .filter(group -> group.postingAllowed() && GroupPostingPolicy.isTechnologyName(group.name()))
                .map(group -> score(group, query.strip(), normalizedQuery, tokens))
                .filter(result -> result.score() > 0)
                .sorted(Comparator.comparingInt(GroupSearchResult::score).reversed()
                        .thenComparing(result -> result.group().name(), String.CASE_INSENSITIVE_ORDER))
                .limit(limit)
                .toList();
    }

    /** Chấm mức khớp: mã nhóm chính xác, tên cụm từ, toàn bộ từ khóa hoặc khu vực. */
    private GroupSearchResult score(FbGroup group, String rawQuery, String query, String[] tokens) {
        GroupSummaryDto summary = new GroupSummaryDto(group.id().value(), group.name(), group.memberCount(),
                group.region(), group.postingAllowed(), group.lastPostAt());
        if (group.id().value().equals(rawQuery)) {
            return new GroupSearchResult(summary, 1000, "Khớp chính xác mã nhóm");
        }

        String name = normalize(group.name());
        String region = normalize(group.region());
        if (name.equals(query)) {
            return new GroupSearchResult(summary, 900, "Tên nhóm khớp chính xác");
        }
        if (name.contains(query)) {
            return new GroupSearchResult(summary, 800, "Tên nhóm chứa cụm từ tìm kiếm");
        }

        long nameTokenMatches = Arrays.stream(tokens).filter(token -> name.contains(token)).count();
        if (tokens.length > 0 && nameTokenMatches == tokens.length) {
            int score = 600 + (int) nameTokenMatches * 10;
            return new GroupSearchResult(summary, score, "Tên nhóm khớp tất cả từ khóa");
        }

        if (region.contains(query) || (tokens.length > 0
                && Arrays.stream(tokens).allMatch(token -> name.contains(token) || region.contains(token)))) {
            return new GroupSearchResult(summary, 400, "Tên nhóm hoặc khu vực khớp từ khóa");
        }
        return new GroupSearchResult(summary, 0, "Không khớp");
    }

    /** Bỏ dấu tiếng Việt và ký tự phân cách để tìm đúng dù người dùng gõ không dấu. */
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String deaccented = Normalizer.normalize(value.toLowerCase(Locale.ROOT).replace('đ', 'd'),
                Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return deaccented.replaceAll("[^a-z0-9]+", " ").trim().replaceAll("\\s+", " ");
    }
}
