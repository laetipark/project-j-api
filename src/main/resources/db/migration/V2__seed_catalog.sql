insert into tools (code, name, default_unlocked, active, created_at, updated_at)
select seed.code, seed.name, seed.default_unlocked, seed.active, current_timestamp, current_timestamp
from (select 'Rake' as code, 'Rake' as name, true as default_unlocked, true as active
      union all
      select 'FishingRod', 'FishingRod', true, true
      union all
      select 'Sickle', 'Sickle', true, true
      union all
      select 'Lantern', 'Lantern', false, true) seed
		 left join tools existing on existing.code = seed.code
where existing.id is null;

insert into regions (code, name, active, created_at, updated_at)
select seed.code, seed.name, seed.active, current_timestamp, current_timestamp
from (select 'Hub' as code, 'Hub' as name, true as active
      union all
      select 'Beach', 'Beach', true
      union all
      select 'DeepForest', 'DeepForest', true
      union all
      select 'AbandonedMine', 'AbandonedMine', true
      union all
      select 'WindHill', 'WindHill', true) seed
		 left join regions existing on existing.code = seed.code
where existing.id is null;

insert into resources (code, name, active, created_at, updated_at)
select seed.code, seed.name, seed.active, current_timestamp, current_timestamp
from (select 'Fish' as code, 'Fish' as name, true as active
      union all
      select 'Shell', 'Shell', true
      union all
      select 'Seaweed', 'Seaweed', true
      union all
      select 'Mushroom', 'Mushroom', true
      union all
      select 'Herb', 'Herb', true
      union all
      select 'GlowMoss', 'GlowMoss', true
      union all
      select 'WindHerb', 'WindHerb', true) seed
		 left join resources existing on existing.code = seed.code
where existing.id is null;

insert into game_settings (start_region_id, start_gold, start_reputation, default_service_capacity, default_inventory_slot_limit, created_at, updated_at)
select start_region.id, 0, 0, 3, 8, current_timestamp, current_timestamp
from regions start_region
where start_region.code = 'Hub'
  and not exists (select 1
                  from game_settings);

insert into resource_gather_rules (region_id, resource_id, required_tool_id, active, created_at, updated_at)
select region.id, resource.id, tool.id, seed.active, current_timestamp, current_timestamp
from (select 'Beach' as region_code, 'Fish' as resource_code, 'FishingRod' as tool_code, true as active
      union all
      select 'Beach', 'Shell', 'Rake', true
      union all
      select 'Beach', 'Seaweed', 'Sickle', true
      union all
      select 'DeepForest', 'Mushroom', 'Sickle', true
      union all
      select 'DeepForest', 'Herb', 'Sickle', true
      union all
      select 'AbandonedMine', 'GlowMoss', 'Lantern', true
      union all
      select 'WindHill', 'WindHerb', 'Sickle', true) seed
		 join regions region on region.code = seed.region_code
	     join resources resource on resource.code = seed.resource_code
	     left join tools tool on tool.code = seed.tool_code
	     left join resource_gather_rules existing
				   on existing.region_id = region.id
					   and existing.resource_id = resource.id
where existing.id is null;

insert into portal_rules (code, name, from_region_id, to_region_id, required_tool_id, required_reputation, active, created_at, updated_at)
select seed.code,
       seed.name,
       from_region.id,
       to_region.id,
       tool.id,
       seed.required_reputation,
       seed.active,
       current_timestamp,
       current_timestamp
from (select 'GoToBeach' as code, 'GoToBeach' as name, 'Hub' as from_region_code, 'Beach' as to_region_code, null as required_tool_code, 0 as required_reputation, true as active
      union all
      select 'ReturnToHubFromBeach', 'ReturnToHubFromBeach', 'Beach', 'Hub', null, 0, true
      union all
      select 'GoToDeepForest', 'GoToDeepForest', 'Hub', 'DeepForest', null, 0, true
      union all
      select 'ReturnToHubFromDeepForest', 'ReturnToHubFromDeepForest', 'DeepForest', 'Hub', null, 0, true
      union all
      select 'GoToAbandonedMine', 'GoToAbandonedMine', 'Hub', 'AbandonedMine', 'Lantern', 0, true
      union all
      select 'ReturnToHubFromAbandonedMine', 'ReturnToHubFromAbandonedMine', 'AbandonedMine', 'Hub', null, 0, true
      union all
      select 'GoToWindHill', 'GoToWindHill', 'Hub', 'WindHill', null, 0, true
      union all
      select 'ReturnToHubFromWindHill', 'ReturnToHubFromWindHill', 'WindHill', 'Hub', null, 0, true
      union all
      select 'WindHillShortcut', 'WindHillShortcut', 'DeepForest', 'WindHill', null, 6, true) seed
		 join regions from_region on from_region.code = seed.from_region_code
	     join regions to_region on to_region.code = seed.to_region_code
	     left join tools tool on tool.code = seed.required_tool_code
	     left join portal_rules existing on existing.code = seed.code
where existing.id is null;

insert into upgrades (code, name, upgrade_type, target_value, tool_id, gold_cost, prerequisite_upgrade_id, active, created_at, updated_at)
select seed.code,
       seed.name,
       seed.upgrade_type,
       seed.target_value,
       tool.id,
       seed.gold_cost,
       null,
       seed.active,
       current_timestamp,
       current_timestamp
from (select 'inventory_12_slots' as code, 'inventory 12 slots' as name, 'INVENTORY_SLOT' as upgrade_type, 12 as target_value, null as tool_code, 30 as gold_cost, true as active
      union all
      select 'inventory_16_slots', 'inventory 16 slots', 'INVENTORY_SLOT', 16, null, 65, true
      union all
      select 'unlock_lantern', 'unlock lantern', 'TOOL_UNLOCK', null, 'Lantern', 45, true) seed
		 left join tools tool on tool.code = seed.tool_code
	     left join upgrades existing on existing.code = seed.code
where existing.id is null;

update upgrades
set prerequisite_upgrade_id = (select prerequisite.id
                               from (select id
                                     from upgrades
                                     where code = 'inventory_12_slots') prerequisite)
where code = 'inventory_16_slots'
  and (
	prerequisite_upgrade_id is null
		or prerequisite_upgrade_id <> (select prerequisite.id
		                               from (select id
		                                     from upgrades
		                                     where code = 'inventory_12_slots') prerequisite)
	);

insert into upgrade_resource_costs (upgrade_id, resource_id, quantity, created_at, updated_at)
select upgrade_entity.id, resource.id, seed.quantity, current_timestamp, current_timestamp
from (select 'inventory_12_slots' as upgrade_code, 'Shell' as resource_code, 3 as quantity
      union all
      select 'inventory_16_slots', 'Herb', 4
      union all
      select 'unlock_lantern', 'Mushroom', 2) seed
		 join upgrades upgrade_entity on upgrade_entity.code = seed.upgrade_code
	     join resources resource on resource.code = seed.resource_code
	     left join upgrade_resource_costs existing
				   on existing.upgrade_id = upgrade_entity.id
					   and existing.resource_id = resource.id
where existing.id is null;
