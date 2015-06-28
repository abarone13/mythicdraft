package com.cd.mythicdraft.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cd.mythicdraft.dao.DraftDAO;
import com.cd.mythicdraft.domain.RawCard;
import com.cd.mythicdraft.domain.RawDraft;
import com.cd.mythicdraft.model.Card;
import com.cd.mythicdraft.model.Draft;
import com.cd.mythicdraft.model.DraftPack;
import com.cd.mythicdraft.model.DraftPlayer;
import com.cd.mythicdraft.model.Player;
import com.cd.mythicdraft.model.Set;

@Service(value = "draftService")
public class DraftService {

	private static final Logger logger = Logger.getLogger(DraftService.class);	
	
	@Autowired
	private DraftDAO draftDao;
	
	@Autowired
	private MtgoDraftParserService mtgoDraftParserService;
	
	@Transactional
	public void addDraft(final InputStream mtgoDraftStream, final String name) {
		RawDraft aDraft;
		
		try {
			aDraft = mtgoDraftParserService.parse(mtgoDraftStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		draftDao.addDraft(convertRawDraft(aDraft, name));
	}
	
	private Draft convertRawDraft(RawDraft aRawDraft, String name) {
		Draft draft = new Draft();
		
		draft.setName(name);
		
		for(DraftPlayer aPlayer : createDraftPlayers(aRawDraft)) {
			draft.addDraftPlayer(aPlayer);	
		}

		for(DraftPack aPack : createDraftPacks(aRawDraft)) {
			draft.addDraftPack(aPack);	
		}		
		
		draft.setEventDate(aRawDraft.getEventDate());
		draft.setEventId(aRawDraft.getEventId());
		
		return draft;
	}

	private List<DraftPlayer> createDraftPlayers(RawDraft aRawDraft) {
		List<DraftPlayer> draftPlayers = new ArrayList<DraftPlayer>(8);
		
		List<String> allPlayers = new ArrayList<String>(aRawDraft.getOtherPlayers());
		allPlayers.add(aRawDraft.getActivePlayer());
		
		Map<String, Player> existingPlayers = draftDao.getPlayersByName(allPlayers);
		
		for(String aPlayer : aRawDraft.getOtherPlayers()) {
			DraftPlayer otherPlayer = new DraftPlayer();
			Player player = existingPlayers.get(aPlayer);
			
			if(player == null) {
				player = new Player();
				player.setName(aPlayer);
			}
			
			otherPlayer.setPlayer(player);
			otherPlayer.setIsActivePlayer(false);
			
			draftPlayers.add(otherPlayer);
		}
		
		DraftPlayer activePlayer = new DraftPlayer();
		Player player = existingPlayers.get(aRawDraft.getActivePlayer());
		
		if(player == null) {
			player = new Player();
			player.setName(aRawDraft.getActivePlayer());
		}
		
		activePlayer.setPlayer(player);
		activePlayer.setIsActivePlayer(true);
		
		draftPlayers.add(activePlayer);
		
		return draftPlayers;
	}

	private List<DraftPack> createDraftPacks(RawDraft aRawDraft) {
		List<DraftPack> draftPacks = new ArrayList<DraftPack>(3);
		
		final Map<String, Card> cardNameToCardMap = draftDao.getCardNameToCardMap(createCardNameToCardSetMap(aRawDraft));
		final List<Set> packSets = draftDao.getSetsByName(aRawDraft.getPackSets());
		
		for(int i = 0; i < aRawDraft.getPackSets().size(); i++) {
			String aSetName = aRawDraft.getPackSets().get(i);
			DraftPack draftPack = new DraftPack();
			
			for(Set aSet : packSets) {
				if(aSet.getName().equals(aSetName)){
					draftPack.setSet(aSet);
				}
			}
			
			draftPack.setSequenceId(i);
			
			draftPacks.add(draftPack);
		}
		
		return draftPacks;
	}

	private Map<String, String> createCardNameToCardSetMap(RawDraft aRawDraft) {
		Map<String, String> cardNameToCardSetMap = new HashMap<String, String>();
		
		for(Map.Entry<String, RawCard> anEntry : aRawDraft.getCardNameToRawCardMap().entrySet()) {
			cardNameToCardSetMap.put(anEntry.getKey(), anEntry.getValue().getSetCode());
		}
		
		return cardNameToCardSetMap;
	}	
}
