package com.gth.posting.application.usecase.findgroups;

import com.gth.posting.application.port.out.repository.GroupRepository;
import com.gth.posting.domain.entity.FbGroup;
import com.gth.posting.domain.valueobject.GroupId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Kiểm tra thuật toán tìm nhóm mà không cần kết nối cơ sở dữ liệu thật. */
class FindGroupsServiceTest {

    private FindGroupsService service;

    /** Chuẩn bị danh sách nhóm mẫu để kiểm tra thứ tự và bỏ dấu tiếng Việt. */
    @BeforeEach
    void setUp() {
        GroupRepository repository = mock(GroupRepository.class);
        when(repository.findAll()).thenReturn(List.of(
                new FbGroup(GroupId.of("123456"), "Laptop và PC Thủ Dầu Một", 1000,
                        "Bình Dương", true, null),
                new FbGroup(GroupId.of("654321"), "Công nghệ máy tính Phú Hòa", 500,
                        "Thủ Dầu Một", true, null),
                new FbGroup(GroupId.of("222222"), "Chợ PC - Laptop Bình Dương", 5000,
                        "Bình Dương", true, null)));
        service = new FindGroupsService(repository);
    }

    /** Từ khóa không dấu vẫn tìm được tên nhóm tiếng Việt có dấu. */
    @Test
    void searchesVietnameseNameWithoutDiacritics() {
        var results = service.handle("thu dau mot", 10);

        assertEquals(2, results.size());
        assertEquals("123456", results.getFirst().group().groupId());
    }

    /** Mã nhóm khớp chính xác được xếp lên đầu danh sách. */
    @Test
    void ranksExactGroupIdFirst() {
        var results = service.handle("654321", 10);

        assertEquals("654321", results.getFirst().group().groupId());
        assertEquals(1000, results.getFirst().score());
    }

    /** Không cho tìm toàn bộ kho khi agent gửi từ khóa rỗng. */
    @Test
    void rejectsBlankQuery() {
        assertThrows(IllegalArgumentException.class, () -> service.handle("   ", 10));
    }

    /** Không trả nhóm chợ dù từ khóa khớp chính xác tên nhóm. */
    @Test
    void doesNotReturnMarketGroups() {
        var results = service.handle("cho pc", 10);

        assertEquals(0, results.size());
    }
}
