package com.talentlink.talentlink.like;

import com.talentlink.talentlink.like.dto.LikeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private LikeBuyRepository likeBuyRepository;

    @Autowired
    private LIkeSellRepository lIkeSellRepository;

    public void addLike(LikeRequest likeReq){
        if (likeReq.getType().equals("삽니다")){

        }else if(likeReq.getType().equals("팝니다")){

        }else{

        }
    }

    public void subLike(LikeRequest likeReq){
        if (likeReq.getType().equals("삽니다")){

        }else if(likeReq.getType().equals("팝니다")){

        }else{

        }
    }

}
