package com.talentlink.talentlink.like;

import com.talentlink.talentlink.like.dto.LikeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name="좋아요",description = "좋아요 관련 API")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @GetMapping
    @Operation(summary = "좋아요 증가",description = "좋아요를 누르면 증가")
    public void likeUp(@RequestParam LikeRequest likeReq){
        likeService.addLike(likeReq);
    }

    @GetMapping
    @Operation(summary = "좋아요 감소",description = "좋아요를 누르면 감소")
    public void likeDown(@RequestParam LikeRequest likeReq){
        likeService.subLike(likeReq);
    }

}
