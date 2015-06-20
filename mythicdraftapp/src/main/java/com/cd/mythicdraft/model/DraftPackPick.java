package com.cd.mythicdraft.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
@Table(name = "DRAFT_PACK_PICK")
public class DraftPackPick implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "DRAFT_PACK_ID",
			insertable = false,
			updatable = false)
	private Integer draftPackId;

	@Column(name = "CARD_ID",
			insertable = false,
			updatable = false)	
	private Integer cardId;
	
	@Column(name = "SEQUENCE_ID")	
	private Integer sequenceId;
	
	@ManyToOne(optional = false,
			   cascade = CascadeType.ALL)
	@JoinColumn(name = "DRAFT_PACK_ID")
	private DraftPack draftPack;
	
	@ManyToOne(optional = false,
			   cascade = CascadeType.ALL)
	@JoinColumn(name = "CARD_ID")
	private Card card;
	
	@OneToMany(cascade = CascadeType.ALL)
	private List<DraftPackAvailablePick> draftPackAvailablePicks;	

	public Integer getDraftPackId() {
		return draftPackId;
	}

	public void setDraftPackId(Integer draftPackId) {
		this.draftPackId = draftPackId;
	}

	public Integer getCardId() {
		return cardId;
	}

	public void setCardId(Integer cardId) {
		this.cardId = cardId;
	}

	public Integer getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(Integer sequenceId) {
		this.sequenceId = sequenceId;
	}

	public DraftPack getDraftPack() {
		return draftPack;
	}

	public void setDraftPack(DraftPack draftPack) {
		this.draftPack = draftPack;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
	}	
}
