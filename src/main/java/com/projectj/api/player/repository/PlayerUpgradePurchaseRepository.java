package com.projectj.api.player.repository;

import com.projectj.api.player.domain.PlayerUpgradePurchaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerUpgradePurchaseRepository extends JpaRepository<PlayerUpgradePurchaseEntity, Long>{

	boolean existsByPlayer_IdAndUpgrade_IdAndDeletedAtIsNull(Long playerId, Long upgradeId);

	@Query("""
		select purchase.upgrade.id
		from PlayerUpgradePurchaseEntity purchase
		where purchase.player.id = :playerId
		  and purchase.deletedAt is null
	""")
	List<Long> findUpgradeIdsByPlayerId(@Param("playerId") Long playerId);

	@Query("""
		select purchase.upgrade.code
		from PlayerUpgradePurchaseEntity purchase
		where purchase.player.id = :playerId
		  and purchase.deletedAt is null
		  and purchase.upgrade.deletedAt is null
		order by purchase.id asc
		""")
	List<String> findUpgradeCodesByPlayerId(@Param("playerId") Long playerId);

}
