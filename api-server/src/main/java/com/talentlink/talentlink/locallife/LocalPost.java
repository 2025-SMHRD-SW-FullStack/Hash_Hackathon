package com.talentlink.talentlink.locallife;

import com.talentlink.talentlink.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocalPost extends BaseTimeEntity { // createdAt, updatedAt은 여기서 상속받음

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private String writerNickname;
    private String address;
    private String imageUrl;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LocalPostLike> likes = new ArrayList<>();

    public int getLikeCount() {
        return likes != null ? likes.size() : 0;
    }

}