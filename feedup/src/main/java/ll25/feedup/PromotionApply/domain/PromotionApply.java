package ll25.feedup.PromotionApply.domain;

import jakarta.persistence.*;
import ll25.feedup.Mate.domain.Mate;
import ll25.feedup.Promotion.domain.Promotion;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_applies",
        uniqueConstraints = @UniqueConstraint(columnNames = {"promotion_id", "mate_id"}))
@Getter
@NoArgsConstructor
public class PromotionApply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mate_id")
    private Mate mate;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "review_written", nullable = false)
    private boolean reviewWritten;

    public PromotionApply(Promotion promotion, Mate mate, LocalDateTime appliedAt) {
        this.promotion = promotion;
        this.mate = mate;
        this.appliedAt = appliedAt;
        this.reviewWritten = false;
    }
}