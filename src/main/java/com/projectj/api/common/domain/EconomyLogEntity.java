package com.projectj.api.common.domain;

import com.projectj.api.dayrun.domain.DayRunEntity;
import com.projectj.api.player.domain.PlayerEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "economy_logs")
public class EconomyLogEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(name = "fk_economy_logs_players"))
	private PlayerEntity player;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "day_run_id", foreignKey = @ForeignKey(name = "fk_economy_logs_day_runs"))
	private DayRunEntity dayRun;

	@Enumerated(EnumType.STRING)
	@Column(name = "log_type", nullable = false, length = 40)
	private EconomyLogType logType;

	@Column(name = "gold_delta", nullable = false)
	private int goldDelta;

	@Column(name = "reason_code", nullable = false, length = 80)
	private String reasonCode;

	@Column(name = "note", length = 500)
	private String note;

	public EconomyLogEntity(){
	}

	public Long getId(){
		return id;
	}

	public PlayerEntity getPlayer(){
		return player;
	}

	public void setPlayer(PlayerEntity player){
		this.player = player;
	}

	public DayRunEntity getDayRun(){
		return dayRun;
	}

	public void setDayRun(DayRunEntity dayRun){
		this.dayRun = dayRun;
	}

	public EconomyLogType getLogType(){
		return logType;
	}

	public void setLogType(EconomyLogType logType){
		this.logType = logType;
	}

	public int getGoldDelta(){
		return goldDelta;
	}

	public void setGoldDelta(int goldDelta){
		this.goldDelta = goldDelta;
	}

	public String getReasonCode(){
		return reasonCode;
	}

	public void setReasonCode(String reasonCode){
		this.reasonCode = reasonCode;
	}

	public String getNote(){
		return note;
	}

	public void setNote(String note){
		this.note = note;
	}

}
