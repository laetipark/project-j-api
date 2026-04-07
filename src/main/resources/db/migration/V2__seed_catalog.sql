insert into tools (code, name, default_unlocked, active, created_at, updated_at) values ('Rake', 'Rake', true, true, current_timestamp, current_timestamp);
insert into tools (code, name, default_unlocked, active, created_at, updated_at) values ('FishingRod', 'FishingRod', true, true, current_timestamp, current_timestamp);
insert into tools (code, name, default_unlocked, active, created_at, updated_at) values ('Sickle', 'Sickle', true, true, current_timestamp, current_timestamp);
insert into tools (code, name, default_unlocked, active, created_at, updated_at) values ('Lantern', 'Lantern', false, true, current_timestamp, current_timestamp);

insert into regions (code, name, active, created_at, updated_at) values ('Hub', 'Hub', true, current_timestamp, current_timestamp);
insert into regions (code, name, active, created_at, updated_at) values ('Beach', 'Beach', true, current_timestamp, current_timestamp);
insert into regions (code, name, active, created_at, updated_at) values ('DeepForest', 'DeepForest', true, current_timestamp, current_timestamp);
insert into regions (code, name, active, created_at, updated_at) values ('AbandonedMine', 'AbandonedMine', true, current_timestamp, current_timestamp);
insert into regions (code, name, active, created_at, updated_at) values ('WindHill', 'WindHill', true, current_timestamp, current_timestamp);

insert into resources (code, name, active, created_at, updated_at) values ('Fish', 'Fish', true, current_timestamp, current_timestamp);
insert into resources (code, name, active, created_at, updated_at) values ('Shell', 'Shell', true, current_timestamp, current_timestamp);
insert into resources (code, name, active, created_at, updated_at) values ('Seaweed', 'Seaweed', true, current_timestamp, current_timestamp);
insert into resources (code, name, active, created_at, updated_at) values ('Mushroom', 'Mushroom', true, current_timestamp, current_timestamp);
insert into resources (code, name, active, created_at, updated_at) values ('Herb', 'Herb', true, current_timestamp, current_timestamp);
insert into resources (code, name, active, created_at, updated_at) values ('GlowMoss', 'GlowMoss', true, current_timestamp, current_timestamp);
insert into resources (code, name, active, created_at, updated_at) values ('WindHerb', 'WindHerb', true, current_timestamp, current_timestamp);

insert into recipes (code, name, difficulty, sell_price, reputation_reward, active, created_at, updated_at) values ('SushiSet', 'SushiSet', 1, 18, 1, true, current_timestamp, current_timestamp);
insert into recipes (code, name, difficulty, sell_price, reputation_reward, active, created_at, updated_at) values ('SeafoodSoup', 'SeafoodSoup', 2, 26, 2, true, current_timestamp, current_timestamp);
insert into recipes (code, name, difficulty, sell_price, reputation_reward, active, created_at, updated_at) values ('HerbFishSoup', 'HerbFishSoup', 2, 24, 2, true, current_timestamp, current_timestamp);
insert into recipes (code, name, difficulty, sell_price, reputation_reward, active, created_at, updated_at) values ('ForestBasket', 'ForestBasket', 1, 16, 1, true, current_timestamp, current_timestamp);
insert into recipes (code, name, difficulty, sell_price, reputation_reward, active, created_at, updated_at) values ('GlowMossStew', 'GlowMossStew', 3, 36, 3, true, current_timestamp, current_timestamp);
insert into recipes (code, name, difficulty, sell_price, reputation_reward, active, created_at, updated_at) values ('WindHerbSalad', 'WindHerbSalad', 3, 34, 3, true, current_timestamp, current_timestamp);

insert into game_settings (start_day, start_region_id, start_gold, start_reputation, default_service_capacity, default_inventory_slot_limit, created_at, updated_at)
values (
	1,
	(select id from regions where code = 'Hub'),
	0,
	0,
	3,
	8,
	current_timestamp,
	current_timestamp
);

insert into resource_gather_rules (region_id, resource_id, required_tool_id, active, created_at, updated_at)
values ((select id from regions where code = 'Beach'), (select id from resources where code = 'Fish'), (select id from tools where code = 'FishingRod'), true, current_timestamp, current_timestamp);
insert into resource_gather_rules (region_id, resource_id, required_tool_id, active, created_at, updated_at)
values ((select id from regions where code = 'Beach'), (select id from resources where code = 'Shell'), (select id from tools where code = 'Rake'), true, current_timestamp, current_timestamp);
insert into resource_gather_rules (region_id, resource_id, required_tool_id, active, created_at, updated_at)
values ((select id from regions where code = 'Beach'), (select id from resources where code = 'Seaweed'), (select id from tools where code = 'Sickle'), true, current_timestamp, current_timestamp);
insert into resource_gather_rules (region_id, resource_id, required_tool_id, active, created_at, updated_at)
values ((select id from regions where code = 'DeepForest'), (select id from resources where code = 'Mushroom'), (select id from tools where code = 'Sickle'), true, current_timestamp, current_timestamp);
insert into resource_gather_rules (region_id, resource_id, required_tool_id, active, created_at, updated_at)
values ((select id from regions where code = 'DeepForest'), (select id from resources where code = 'Herb'), (select id from tools where code = 'Sickle'), true, current_timestamp, current_timestamp);
insert into resource_gather_rules (region_id, resource_id, required_tool_id, active, created_at, updated_at)
values ((select id from regions where code = 'AbandonedMine'), (select id from resources where code = 'GlowMoss'), (select id from tools where code = 'Lantern'), true, current_timestamp, current_timestamp);
insert into resource_gather_rules (region_id, resource_id, required_tool_id, active, created_at, updated_at)
values ((select id from regions where code = 'WindHill'), (select id from resources where code = 'WindHerb'), (select id from tools where code = 'Sickle'), true, current_timestamp, current_timestamp);

insert into recipe_ingredients (recipe_id, resource_id, quantity, created_at, updated_at)
values ((select id from recipes where code = 'SushiSet'), (select id from resources where code = 'Fish'), 2, current_timestamp, current_timestamp);
insert into recipe_ingredients (recipe_id, resource_id, quantity, created_at, updated_at)
values ((select id from recipes where code = 'SushiSet'), (select id from resources where code = 'Seaweed'), 1, current_timestamp, current_timestamp);
insert into recipe_ingredients (recipe_id, resource_id, quantity, created_at, updated_at)
values ((select id from recipes where code = 'SeafoodSoup'), (select id from resources where code = 'Fish'), 1, current_timestamp, current_timestamp);
insert into recipe_ingredients (recipe_id, resource_id, quantity, created_at, updated_at)
values ((select id from recipes where code = 'SeafoodSoup'), (select id from resources where code = 'Shell'), 1, current_timestamp, current_timestamp);
insert into recipe_ingredients (recipe_id, resource_id, quantity, created_at, updated_at)
values ((select id from recipes where code = 'SeafoodSoup'), (select id from resources where code = 'Seaweed'), 1, current_timestamp, current_timestamp);
insert into recipe_ingredients (recipe_id, resource_id, quantity, created_at, updated_at)
values ((select id from recipes where code = 'HerbFishSoup'), (select id from resources where code = 'Fish'), 1, current_timestamp, current_timestamp);
insert into recipe_ingredients (recipe_id, resource_id, quantity, created_at, updated_at)
values ((select id from recipes where code = 'HerbFishSoup'), (select id from resources where code = 'Herb'), 2, current_timestamp, current_timestamp);
insert into recipe_ingredients (recipe_id, resource_id, quantity, created_at, updated_at)
values ((select id from recipes where code = 'ForestBasket'), (select id from resources where code = 'Mushroom'), 2, current_timestamp, current_timestamp);
insert into recipe_ingredients (recipe_id, resource_id, quantity, created_at, updated_at)
values ((select id from recipes where code = 'ForestBasket'), (select id from resources where code = 'Herb'), 1, current_timestamp, current_timestamp);
insert into recipe_ingredients (recipe_id, resource_id, quantity, created_at, updated_at)
values ((select id from recipes where code = 'GlowMossStew'), (select id from resources where code = 'GlowMoss'), 1, current_timestamp, current_timestamp);
insert into recipe_ingredients (recipe_id, resource_id, quantity, created_at, updated_at)
values ((select id from recipes where code = 'GlowMossStew'), (select id from resources where code = 'Mushroom'), 1, current_timestamp, current_timestamp);
insert into recipe_ingredients (recipe_id, resource_id, quantity, created_at, updated_at)
values ((select id from recipes where code = 'GlowMossStew'), (select id from resources where code = 'Herb'), 1, current_timestamp, current_timestamp);
insert into recipe_ingredients (recipe_id, resource_id, quantity, created_at, updated_at)
values ((select id from recipes where code = 'WindHerbSalad'), (select id from resources where code = 'WindHerb'), 2, current_timestamp, current_timestamp);
insert into recipe_ingredients (recipe_id, resource_id, quantity, created_at, updated_at)
values ((select id from recipes where code = 'WindHerbSalad'), (select id from resources where code = 'Herb'), 1, current_timestamp, current_timestamp);
insert into recipe_ingredients (recipe_id, resource_id, quantity, created_at, updated_at)
values ((select id from recipes where code = 'WindHerbSalad'), (select id from resources where code = 'Seaweed'), 1, current_timestamp, current_timestamp);

insert into portal_rules (code, name, from_region_id, to_region_id, required_phase, required_tool_id, required_reputation, active, created_at, updated_at)
values ('GoToBeach', 'GoToBeach', (select id from regions where code = 'Hub'), (select id from regions where code = 'Beach'), 'morning_explore', null, 0, true, current_timestamp, current_timestamp);
insert into portal_rules (code, name, from_region_id, to_region_id, required_phase, required_tool_id, required_reputation, active, created_at, updated_at)
values ('ReturnToHubFromBeach', 'ReturnToHubFromBeach', (select id from regions where code = 'Beach'), (select id from regions where code = 'Hub'), 'morning_explore', null, 0, true, current_timestamp, current_timestamp);
insert into portal_rules (code, name, from_region_id, to_region_id, required_phase, required_tool_id, required_reputation, active, created_at, updated_at)
values ('GoToDeepForest', 'GoToDeepForest', (select id from regions where code = 'Hub'), (select id from regions where code = 'DeepForest'), 'morning_explore', null, 0, true, current_timestamp, current_timestamp);
insert into portal_rules (code, name, from_region_id, to_region_id, required_phase, required_tool_id, required_reputation, active, created_at, updated_at)
values ('ReturnToHubFromDeepForest', 'ReturnToHubFromDeepForest', (select id from regions where code = 'DeepForest'), (select id from regions where code = 'Hub'), 'morning_explore', null, 0, true, current_timestamp, current_timestamp);
insert into portal_rules (code, name, from_region_id, to_region_id, required_phase, required_tool_id, required_reputation, active, created_at, updated_at)
values ('GoToAbandonedMine', 'GoToAbandonedMine', (select id from regions where code = 'Hub'), (select id from regions where code = 'AbandonedMine'), 'morning_explore', (select id from tools where code = 'Lantern'), 0, true, current_timestamp, current_timestamp);
insert into portal_rules (code, name, from_region_id, to_region_id, required_phase, required_tool_id, required_reputation, active, created_at, updated_at)
values ('ReturnToHubFromAbandonedMine', 'ReturnToHubFromAbandonedMine', (select id from regions where code = 'AbandonedMine'), (select id from regions where code = 'Hub'), 'morning_explore', null, 0, true, current_timestamp, current_timestamp);
insert into portal_rules (code, name, from_region_id, to_region_id, required_phase, required_tool_id, required_reputation, active, created_at, updated_at)
values ('GoToWindHill', 'GoToWindHill', (select id from regions where code = 'Hub'), (select id from regions where code = 'WindHill'), 'morning_explore', null, 0, true, current_timestamp, current_timestamp);
insert into portal_rules (code, name, from_region_id, to_region_id, required_phase, required_tool_id, required_reputation, active, created_at, updated_at)
values ('ReturnToHubFromWindHill', 'ReturnToHubFromWindHill', (select id from regions where code = 'WindHill'), (select id from regions where code = 'Hub'), 'morning_explore', null, 0, true, current_timestamp, current_timestamp);
insert into portal_rules (code, name, from_region_id, to_region_id, required_phase, required_tool_id, required_reputation, active, created_at, updated_at)
values ('WindHillShortcut', 'WindHillShortcut', (select id from regions where code = 'DeepForest'), (select id from regions where code = 'WindHill'), 'morning_explore', null, 6, true, current_timestamp, current_timestamp);

insert into upgrades (code, name, upgrade_type, target_value, tool_id, gold_cost, prerequisite_upgrade_id, active, created_at, updated_at)
values ('inventory_12_slots', 'inventory 12 slots', 'INVENTORY_SLOT', 12, null, 30, null, true, current_timestamp, current_timestamp);
insert into upgrades (code, name, upgrade_type, target_value, tool_id, gold_cost, prerequisite_upgrade_id, active, created_at, updated_at)
values ('inventory_16_slots', 'inventory 16 slots', 'INVENTORY_SLOT', 16, null, 65, (select id from upgrades where code = 'inventory_12_slots'), true, current_timestamp, current_timestamp);
insert into upgrades (code, name, upgrade_type, target_value, tool_id, gold_cost, prerequisite_upgrade_id, active, created_at, updated_at)
values ('unlock_lantern', 'unlock lantern', 'TOOL_UNLOCK', null, (select id from tools where code = 'Lantern'), 45, null, true, current_timestamp, current_timestamp);

insert into upgrade_resource_costs (upgrade_id, resource_id, quantity, created_at, updated_at)
values ((select id from upgrades where code = 'inventory_12_slots'), (select id from resources where code = 'Shell'), 3, current_timestamp, current_timestamp);
insert into upgrade_resource_costs (upgrade_id, resource_id, quantity, created_at, updated_at)
values ((select id from upgrades where code = 'inventory_16_slots'), (select id from resources where code = 'Herb'), 4, current_timestamp, current_timestamp);
insert into upgrade_resource_costs (upgrade_id, resource_id, quantity, created_at, updated_at)
values ((select id from upgrades where code = 'unlock_lantern'), (select id from resources where code = 'Mushroom'), 2, current_timestamp, current_timestamp);
