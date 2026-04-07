package com.projectj.api.dayrun.service;

import com.projectj.api.catalog.domain.RecipeEntity;
import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.common.exception.ErrorCode;
import com.projectj.api.dayrun.domain.DayRunEntity;
import com.projectj.api.dayrun.repository.DayRunRepository;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.domain.PlayerPhase;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DayRunService{

	private final DayRunRepository dayRunRepository;

	public DayRunService(DayRunRepository dayRunRepository){
		this.dayRunRepository = dayRunRepository;
	}

	public DayRunEntity getCurrentDayRun(PlayerEntity player){
		return dayRunRepository.findByPlayer_IdAndDayNumber(player.getId(), player.getCurrentDay())
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "Current day run was not found."));
	}

	public Optional<DayRunEntity> getPreviousDayRun(PlayerEntity player){
		return dayRunRepository.findTopByPlayer_IdAndDayNumberLessThanOrderByDayNumberDesc(player.getId(), player.getCurrentDay());
	}

	public DayRunEntity createDayRun(PlayerEntity player){
		DayRunEntity dayRun = new DayRunEntity();
		dayRun.setPlayer(player);
		dayRun.setDayNumber(player.getCurrentDay());
		dayRun.setServiceSkipped(false);
		dayRun.setSoldCount(0);
		dayRun.setEarnedGold(0);
		dayRun.setEarnedReputation(0);
		dayRun.setGatherFailureCount(0);
		dayRun.setGatherSuccessCount(0);
		dayRun.setTotalGatheredQuantity(0);
		return dayRunRepository.save(dayRun);
	}

	public void recordRecipeSelection(DayRunEntity dayRun, RecipeEntity recipe){
		dayRun.setSelectedRecipe(recipe);
		dayRunRepository.save(dayRun);
	}

	public void recordGatherResult(DayRunEntity dayRun, boolean success, int grantedQuantity){
		if(success){
			dayRun.setGatherSuccessCount(dayRun.getGatherSuccessCount() + 1);
			dayRun.setTotalGatheredQuantity(dayRun.getTotalGatheredQuantity() + grantedQuantity);
		}else{
			dayRun.setGatherFailureCount(dayRun.getGatherFailureCount() + 1);
		}
		dayRunRepository.save(dayRun);
	}

	public void recordServiceResult(DayRunEntity dayRun, RecipeEntity recipe, boolean skipped, int soldCount, int earnedGold, int earnedReputation){
		dayRun.setSelectedRecipe(recipe);
		dayRun.setServiceSkipped(skipped);
		dayRun.setSoldCount(soldCount);
		dayRun.setEarnedGold(earnedGold);
		dayRun.setEarnedReputation(earnedReputation);
		dayRunRepository.save(dayRun);
	}

	public DayRunEntity resolveLastSettlement(PlayerEntity player){
		if(player.getCurrentPhase() == PlayerPhase.SETTLEMENT){
			return getCurrentDayRun(player);
		}
		return getPreviousDayRun(player).orElse(null);
	}

}
