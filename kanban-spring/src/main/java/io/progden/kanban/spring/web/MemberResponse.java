package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.BoardRole;
import io.progden.kanban.core.domain.User;
import java.util.UUID;

// userId：拖曳頭像追加卡片負責人（uc-assign-card-owner-by-drag）需要的是 User.id，不是 username
// （POST /api/cards/{cardId}/assignees/drag 吃 userId，見 CardController／dragAssignCardOwner）；
// 「看板成員」item（board-members，見 kanban-frontend BoardMembersItem.tsx）要讓成員頭像也能拖曳，
// 原本這裡沒帶 userId，前端只好硬把 workload 查詢借來湊 id，不乾淨，直接把它加進來最直接。
record MemberResponse(UUID userId, String username, String displayName, BoardRole role) {

    static MemberResponse of(User user, BoardRole role) {
        return new MemberResponse(user.getId(), user.getUsername(), user.getDisplayName(), role);
    }
}
