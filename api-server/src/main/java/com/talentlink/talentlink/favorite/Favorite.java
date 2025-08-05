package com.talentlink.talentlink.favorite;

import com.talentlink.talentlink.common.BaseTimeEntity;
import com.talentlink.talentlink.talentbuy.TalentBuy;
import com.talentlink.talentlink.talentsell.TalentSell;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "favorite")
public class Favorite extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id",length = 100)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_fav_buy"))
    private TalentBuy buyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sell_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_fav_sell"))
    private TalentSell sellId;

}
