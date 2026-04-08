package com.projectj.api.player.service;

import com.projectj.api.catalog.domain.ResourceEntity;
import com.projectj.api.catalog.domain.ToolEntity;
import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.common.exception.ErrorCode;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.domain.PlayerInventoryEntity;
import com.projectj.api.player.domain.PlayerStorageEntity;
import com.projectj.api.player.domain.PlayerToolEntity;
import com.projectj.api.player.repository.PlayerInventoryRepository;
import com.projectj.api.player.repository.PlayerStorageRepository;
import com.projectj.api.player.repository.PlayerToolRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlayerResourceService{

	private final PlayerInventoryRepository playerInventoryRepository;
	private final PlayerStorageRepository playerStorageRepository;
	private final PlayerToolRepository playerToolRepository;

	public PlayerResourceService(
		PlayerInventoryRepository playerInventoryRepository,
		PlayerStorageRepository playerStorageRepository,
		PlayerToolRepository playerToolRepository
	){
		this.playerInventoryRepository = playerInventoryRepository;
		this.playerStorageRepository = playerStorageRepository;
		this.playerToolRepository = playerToolRepository;
	}

	public List<PlayerInventoryEntity> getInventory(PlayerEntity player){
		return playerInventoryRepository.findByPlayer_IdAndDeletedAtIsNullOrderByResource_CodeAsc(player.getId());
	}

	public List<PlayerStorageEntity> getStorage(PlayerEntity player){
		return playerStorageRepository.findByPlayer_IdAndDeletedAtIsNullOrderByResource_CodeAsc(player.getId());
	}

	public List<PlayerToolEntity> getUnlockedTools(PlayerEntity player){
		return playerToolRepository.findByPlayer_IdAndDeletedAtIsNullOrderByTool_CodeAsc(player.getId());
	}

	public boolean hasTool(PlayerEntity player, ToolEntity tool){
		if(tool == null){
			return true;
		}
		return playerToolRepository.existsByPlayer_IdAndTool_IdAndDeletedAtIsNull(player.getId(), tool.getId());
	}

	public void unlockTool(PlayerEntity player, ToolEntity tool){
		if(hasTool(player, tool)){
			return;
		}
		PlayerToolEntity playerTool = new PlayerToolEntity();
		playerTool.setPlayer(player);
		playerTool.setTool(tool);
		playerToolRepository.save(playerTool);
	}

	public void addInventory(PlayerEntity player, ResourceEntity resource, int quantity){
		if(quantity < 1){
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "Quantity must be positive.");
		}

		PlayerInventoryEntity inventory = playerInventoryRepository.findByPlayer_IdAndResource_IdAndDeletedAtIsNull(player.getId(), resource.getId())
			.orElse(null);
		if(inventory == null){
			long occupiedSlots = playerInventoryRepository.countByPlayer_IdAndQuantityGreaterThanAndDeletedAtIsNull(player.getId(), 0);
			if(occupiedSlots >= player.getInventorySlotLimit()){
				throw new BusinessException(ErrorCode.INVENTORY_CAPACITY_EXCEEDED, "Inventory slot limit would be exceeded.");
			}
			inventory = playerInventoryRepository.findByPlayer_IdAndResource_Id(player.getId(), resource.getId()).orElse(null);
			if(inventory == null){
				inventory = new PlayerInventoryEntity();
				inventory.setPlayer(player);
				inventory.setResource(resource);
			}
			inventory.restore();
			inventory.setQuantity(quantity);
		}else{
			inventory.restore();
			inventory.setQuantity(inventory.getQuantity() + quantity);
		}
		playerInventoryRepository.save(inventory);
	}

	public void removeInventory(PlayerEntity player, ResourceEntity resource, int quantity){
		if(quantity < 1){
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "Quantity must be positive.");
		}
		PlayerInventoryEntity inventory = playerInventoryRepository.findByPlayer_IdAndResource_IdAndDeletedAtIsNull(player.getId(), resource.getId())
			.orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_RESOURCE, "Inventory resource is insufficient: " + resource.getCode()));
		if(inventory.getQuantity() < quantity){
			throw new BusinessException(ErrorCode.INSUFFICIENT_RESOURCE, "Inventory resource is insufficient: " + resource.getCode());
		}
		int remaining = inventory.getQuantity() - quantity;
		if(remaining == 0){
			inventory.setQuantity(0);
			inventory.markDeleted(Instant.now());
			playerInventoryRepository.save(inventory);
			return;
		}
		inventory.setQuantity(remaining);
		playerInventoryRepository.save(inventory);
	}

	public void addStorage(PlayerEntity player, ResourceEntity resource, int quantity){
		if(quantity < 1){
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "Quantity must be positive.");
		}
		PlayerStorageEntity storage = playerStorageRepository.findByPlayer_IdAndResource_IdAndDeletedAtIsNull(player.getId(), resource.getId())
			.orElseGet(() -> playerStorageRepository.findByPlayer_IdAndResource_Id(player.getId(), resource.getId()).orElse(null));
		if(storage == null){
			storage = new PlayerStorageEntity();
			storage.setPlayer(player);
			storage.setResource(resource);
			storage.setQuantity(quantity);
		}else{
			storage.restore();
			storage.setQuantity(storage.getQuantity() + quantity);
		}
		playerStorageRepository.save(storage);
	}

	public void removeStorage(PlayerEntity player, ResourceEntity resource, int quantity){
		if(quantity < 1){
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "Quantity must be positive.");
		}
		PlayerStorageEntity storage = playerStorageRepository.findByPlayer_IdAndResource_IdAndDeletedAtIsNull(player.getId(), resource.getId())
			.orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_RESOURCE, "Storage resource is insufficient: " + resource.getCode()));
		if(storage.getQuantity() < quantity){
			throw new BusinessException(ErrorCode.INSUFFICIENT_RESOURCE, "Storage resource is insufficient: " + resource.getCode());
		}
		int remaining = storage.getQuantity() - quantity;
		if(remaining == 0){
			storage.setQuantity(0);
			storage.markDeleted(Instant.now());
			playerStorageRepository.save(storage);
			return;
		}
		storage.setQuantity(remaining);
		playerStorageRepository.save(storage);
	}

	public void consumeCombinedResource(PlayerEntity player, ResourceEntity resource, int quantity){
		int inventoryQuantity = getInventoryQuantityMap(player).getOrDefault(resource.getCode(), 0);
		int storageQuantity = getStorageQuantityMap(player).getOrDefault(resource.getCode(), 0);
		if(inventoryQuantity + storageQuantity < quantity){
			throw new BusinessException(ErrorCode.INSUFFICIENT_RESOURCE, "Combined resource is insufficient: " + resource.getCode());
		}

		int remaining = quantity;
		if(inventoryQuantity > 0){
			int consumedFromInventory = Math.min(inventoryQuantity, remaining);
			removeInventory(player, resource, consumedFromInventory);
			remaining -= consumedFromInventory;
		}
		if(remaining > 0){
			removeStorage(player, resource, remaining);
		}
	}

	public Map<String, Integer> getInventoryQuantityMap(PlayerEntity player){
		Map<String, Integer> quantities = new HashMap<>();
		for(PlayerInventoryEntity inventory : getInventory(player)){
			quantities.put(inventory.getResource().getCode(), inventory.getQuantity());
		}
		return quantities;
	}

	public Map<String, Integer> getStorageQuantityMap(PlayerEntity player){
		Map<String, Integer> quantities = new HashMap<>();
		for(PlayerStorageEntity storage : getStorage(player)){
			quantities.put(storage.getResource().getCode(), storage.getQuantity());
		}
		return quantities;
	}

	public Map<String, Integer> getCombinedQuantityMap(PlayerEntity player){
		Map<String, Integer> combined = new HashMap<>(getStorageQuantityMap(player));
		for(Map.Entry<String, Integer> entry : getInventoryQuantityMap(player).entrySet()){
			combined.merge(entry.getKey(), entry.getValue(), Integer::sum);
		}
		return combined;
	}

}
