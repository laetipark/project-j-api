create table tools (
	id bigint auto_increment primary key,
	code varchar(80) not null,
	name varchar(120) not null,
	default_unlocked boolean not null default false,
	active boolean not null default true,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint uk_tools_code unique (code)
);

create table regions (
	id bigint auto_increment primary key,
	code varchar(80) not null,
	name varchar(120) not null,
	active boolean not null default true,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint uk_regions_code unique (code)
);

create table resources (
	id bigint auto_increment primary key,
	code varchar(80) not null,
	name varchar(120) not null,
	active boolean not null default true,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint uk_resources_code unique (code)
);

create table recipes (
	id bigint auto_increment primary key,
	code varchar(80) not null,
	name varchar(120) not null,
	difficulty int not null,
	sell_price int not null,
	reputation_reward int not null,
	active boolean not null default true,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint uk_recipes_code unique (code)
);

create table game_settings (
	id bigint auto_increment primary key,
	start_day int not null,
	start_region_id bigint not null,
	start_gold int not null,
	start_reputation int not null,
	default_service_capacity int not null,
	default_inventory_slot_limit int not null,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint fk_game_settings_regions foreign key (start_region_id) references regions (id)
);

create table resource_gather_rules (
	id bigint auto_increment primary key,
	region_id bigint not null,
	resource_id bigint not null,
	required_tool_id bigint null,
	active boolean not null default true,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint uk_resource_gather_rules_region_resource unique (region_id, resource_id),
	constraint fk_resource_gather_rules_regions foreign key (region_id) references regions (id),
	constraint fk_resource_gather_rules_resources foreign key (resource_id) references resources (id),
	constraint fk_resource_gather_rules_tools foreign key (required_tool_id) references tools (id)
);

create table recipe_ingredients (
	id bigint auto_increment primary key,
	recipe_id bigint not null,
	resource_id bigint not null,
	quantity int not null,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint uk_recipe_ingredients_recipe_resource unique (recipe_id, resource_id),
	constraint fk_recipe_ingredients_recipes foreign key (recipe_id) references recipes (id),
	constraint fk_recipe_ingredients_resources foreign key (resource_id) references resources (id)
);

create table portal_rules (
	id bigint auto_increment primary key,
	code varchar(80) not null,
	name varchar(120) not null,
	from_region_id bigint not null,
	to_region_id bigint not null,
	required_phase varchar(40) not null,
	required_tool_id bigint null,
	required_reputation int not null default 0,
	active boolean not null default true,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint uk_portal_rules_code unique (code),
	constraint fk_portal_rules_from_regions foreign key (from_region_id) references regions (id),
	constraint fk_portal_rules_to_regions foreign key (to_region_id) references regions (id),
	constraint fk_portal_rules_tools foreign key (required_tool_id) references tools (id)
);

create table upgrades (
	id bigint auto_increment primary key,
	code varchar(80) not null,
	name varchar(120) not null,
	upgrade_type varchar(40) not null,
	target_value int null,
	tool_id bigint null,
	gold_cost int not null,
	prerequisite_upgrade_id bigint null,
	active boolean not null default true,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint uk_upgrades_code unique (code),
	constraint fk_upgrades_tools foreign key (tool_id) references tools (id),
	constraint fk_upgrades_prerequisite foreign key (prerequisite_upgrade_id) references upgrades (id)
);

create table upgrade_resource_costs (
	id bigint auto_increment primary key,
	upgrade_id bigint not null,
	resource_id bigint not null,
	quantity int not null,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint uk_upgrade_resource_costs_upgrade_resource unique (upgrade_id, resource_id),
	constraint fk_upgrade_resource_costs_upgrades foreign key (upgrade_id) references upgrades (id),
	constraint fk_upgrade_resource_costs_resources foreign key (resource_id) references resources (id)
);

create table players (
	id bigint auto_increment primary key,
	public_id varchar(36) not null,
	display_name varchar(120) null,
	current_day int not null,
	current_phase varchar(40) not null,
	current_region_id bigint not null,
	gold int not null,
	reputation int not null,
	service_capacity int not null,
	inventory_slot_limit int not null,
	selected_recipe_id bigint null,
	version bigint not null default 0,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint uk_players_public_id unique (public_id),
	constraint fk_players_current_regions foreign key (current_region_id) references regions (id),
	constraint fk_players_selected_recipes foreign key (selected_recipe_id) references recipes (id)
);

create table player_inventory (
	id bigint auto_increment primary key,
	player_id bigint not null,
	resource_id bigint not null,
	quantity int not null,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint uk_player_inventory_player_resource unique (player_id, resource_id),
	constraint fk_player_inventory_players foreign key (player_id) references players (id),
	constraint fk_player_inventory_resources foreign key (resource_id) references resources (id)
);

create table player_storage (
	id bigint auto_increment primary key,
	player_id bigint not null,
	resource_id bigint not null,
	quantity int not null,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint uk_player_storage_player_resource unique (player_id, resource_id),
	constraint fk_player_storage_players foreign key (player_id) references players (id),
	constraint fk_player_storage_resources foreign key (resource_id) references resources (id)
);

create table player_tools (
	id bigint auto_increment primary key,
	player_id bigint not null,
	tool_id bigint not null,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint uk_player_tools_player_tool unique (player_id, tool_id),
	constraint fk_player_tools_players foreign key (player_id) references players (id),
	constraint fk_player_tools_tools foreign key (tool_id) references tools (id)
);

create table day_runs (
	id bigint auto_increment primary key,
	player_id bigint not null,
	day_number int not null,
	selected_recipe_id bigint null,
	gather_success_count int not null default 0,
	gather_failure_count int not null default 0,
	total_gathered_quantity int not null default 0,
	service_skipped boolean not null default false,
	sold_count int not null default 0,
	earned_gold int not null default 0,
	earned_reputation int not null default 0,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint uk_day_runs_player_day unique (player_id, day_number),
	constraint fk_day_runs_players foreign key (player_id) references players (id),
	constraint fk_day_runs_selected_recipes foreign key (selected_recipe_id) references recipes (id)
);

create table player_upgrade_purchases (
	id bigint auto_increment primary key,
	player_id bigint not null,
	upgrade_id bigint not null,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint uk_player_upgrade_purchases_player_upgrade unique (player_id, upgrade_id),
	constraint fk_player_upgrade_purchases_players foreign key (player_id) references players (id),
	constraint fk_player_upgrade_purchases_upgrades foreign key (upgrade_id) references upgrades (id)
);

create table gather_logs (
	id bigint auto_increment primary key,
	player_id bigint not null,
	day_run_id bigint not null,
	region_id bigint not null,
	resource_id bigint not null,
	quantity_requested int not null,
	quantity_granted int not null,
	success boolean not null,
	failure_reason varchar(200) null,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint fk_gather_logs_players foreign key (player_id) references players (id),
	constraint fk_gather_logs_day_runs foreign key (day_run_id) references day_runs (id),
	constraint fk_gather_logs_regions foreign key (region_id) references regions (id),
	constraint fk_gather_logs_resources foreign key (resource_id) references resources (id)
);

create table service_logs (
	id bigint auto_increment primary key,
	player_id bigint not null,
	day_run_id bigint not null,
	recipe_id bigint null,
	requested_capacity int not null,
	cookable_count int not null,
	sold_count int not null,
	earned_gold int not null,
	earned_reputation int not null,
	skipped boolean not null default false,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint fk_service_logs_players foreign key (player_id) references players (id),
	constraint fk_service_logs_day_runs foreign key (day_run_id) references day_runs (id),
	constraint fk_service_logs_recipes foreign key (recipe_id) references recipes (id)
);

create table economy_logs (
	id bigint auto_increment primary key,
	player_id bigint not null,
	day_run_id bigint null,
	log_type varchar(40) not null,
	gold_delta int not null,
	reason_code varchar(80) not null,
	note varchar(500) null,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint fk_economy_logs_players foreign key (player_id) references players (id),
	constraint fk_economy_logs_day_runs foreign key (day_run_id) references day_runs (id)
);

create table storage_logs (
	id bigint auto_increment primary key,
	player_id bigint not null,
	resource_id bigint not null,
	action_type varchar(40) not null,
	quantity int not null,
	created_at timestamp not null default current_timestamp,
	updated_at timestamp not null default current_timestamp,
	constraint fk_storage_logs_players foreign key (player_id) references players (id),
	constraint fk_storage_logs_resources foreign key (resource_id) references resources (id)
);
