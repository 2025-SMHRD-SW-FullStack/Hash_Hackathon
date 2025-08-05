package com.talentlink.talentlink.favorite;

import com.talentlink.talentlink.favorite.dto.FavoriteDeleteRequest;
import com.talentlink.talentlink.favorite.dto.FavoriteRequest;
import com.talentlink.talentlink.favorite.dto.FavoriteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorite")
@Tag(name="관심",description = "관심 관련 API")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping
    @Operation(summary = "관심 전체 목록",description = "관심을 표시한 전체 글 목록을 가져옵니다.")
    public ResponseEntity<List<FavoriteResponse>> getAllList(@RequestParam String userId){
        List<FavoriteResponse> favorites = favoriteService.getAll(userId);
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/save")
    @Operation(summary = "관심 저장")
    public ResponseEntity<Object> saveFavorite(@RequestBody FavoriteRequest resDto){
        favoriteService.saveFavorite(resDto);
        return ResponseEntity.ok("success");
    }

    @PostMapping("/delete")
    @Operation(summary = "관심 삭제")
    public ResponseEntity<String> delFavorite(@RequestBody FavoriteDeleteRequest dto){
        favoriteService.deleteFavorite(dto.getId());
        return ResponseEntity.ok("success");
    }

}
