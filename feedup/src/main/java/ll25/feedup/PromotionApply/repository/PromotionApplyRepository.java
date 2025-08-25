package ll25.feedup.PromotionApply.repository;

import ll25.feedup.Mate.domain.Mate;
import ll25.feedup.Promotion.domain.Promotion;
import ll25.feedup.PromotionApply.domain.PromotionApply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionApplyRepository extends JpaRepository<PromotionApply, Long> {
    boolean existsByPromotionAndMate(Promotion promotion, Mate mate);
    long countByPromotion(Promotion promotion);
    @EntityGraph(attributePaths = {"promotion", "promotion.host"})
    Page<PromotionApply> findByMateOrderByAppliedAtDesc(Mate mate, Pageable pageable);
}
