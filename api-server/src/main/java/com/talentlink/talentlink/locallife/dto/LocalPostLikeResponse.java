package com.talentlink.talentlink.locallife.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocalPostLikeResponse {
    private boolean liked;
    private long likeCount;
}
