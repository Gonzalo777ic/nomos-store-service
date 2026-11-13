package com.valeerp.store.service.model;

import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "promotion_targets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"promotion_id", "target_type", "target_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionTarget extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;
    @Column(name = "target_type", nullable = false)
    private String targetType;
    @Column(name = "target_id", nullable = false)
    private Long targetId;
}