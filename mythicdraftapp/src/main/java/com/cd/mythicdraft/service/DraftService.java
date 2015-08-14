package com.cd.mythicdraft.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.cd.mythicdraft.dao.DraftDAO;
import com.cd.mythicdraft.domain.RawCard;
import com.cd.mythicdraft.domain.RawDraft;
import com.cd.mythicdraft.exception.DuplicateDraftException;
import com.cd.mythicdraft.json.JsonAllPicks;
import com.cd.mythicdraft.json.JsonCard;
import com.cd.mythicdraft.json.JsonDraft;
import com.cd.mythicdraft.json.JsonPack;
import com.cd.mythicdraft.json.JsonPackPick;
import com.cd.mythicdraft.json.JsonPlayer;
import com.cd.mythicdraft.json.JsonPlayerStats;
import com.cd.mythicdraft.json.JsonUploadStatus;
import com.cd.mythicdraft.model.Card;
import com.cd.mythicdraft.model.Draft;
import com.cd.mythicdraft.model.DraftPack;
import com.cd.mythicdraft.model.DraftPackAvailablePick;
import com.cd.mythicdraft.model.DraftPackPick;
import com.cd.mythicdraft.model.DraftPlayer;
import com.cd.mythicdraft.model.Player;
import com.cd.mythicdraft.model.Set;

@Service(value = "draftService")
public class DraftService {
	
	private DraftDAO draftDao;

    @Autowired
    public void setDraftDao(final DraftDAO draftDao) {
        this.draftDao = draftDao;
    }

    @Autowired
	private MtgoDraftParserService mtgoDraftParserService;
	
	@Transactional
	public JsonUploadStatus addDraft(final InputStream mtgoDraftStream, 
						 			 final String name, 
						 			 final Integer wins, 
						 			 final Integer losses) {
		RawDraft aDraft = null;
		JsonUploadStatus uploadStatus = new JsonUploadStatus();
		
		try {
			aDraft = mtgoDraftParserService.parse(mtgoDraftStream);
		} catch (IOException e) {
			uploadStatus.setDraftInvalid(true);
		}

		uploadStatus.setDraftInvalid(isDraftValid(aDraft));
		
		if(aDraft != null && !uploadStatus.isDraftInvalid()) {
			try {
				draftDao.addDraft(convertRawDraft(aDraft, name, wins, losses));
			} catch (DuplicateDraftException e) {
				uploadStatus.setDraftDuplicate(true);
			}	
		}
		
		return uploadStatus;
	}

	public JsonDraft getDraftById(final Integer draftId) {
		final Draft draft = draftDao.getDraftById(draftId);
		final JsonDraft jsonDraft = getJsonDraftFromDraft(draft);
		
		return jsonDraft;
	}
	
	public JsonPackPick getPackByIdAndPick(final Integer draftId,
										   final Integer draftPackId, 
										   final Integer pickId) {
		
		final JsonPackPick jsonPackPick = new JsonPackPick();
		final List<JsonCard> availablePicks = new ArrayList<JsonCard>();
		final JsonCard pick = new JsonCard();
		int cardCount = 0;
		
		DraftPackPick packPick = draftDao.getPackByIdAndPick(draftPackId, pickId);
		
		for(DraftPackAvailablePick available : packPick.getDraftPackAvailablePicks()) {
			final String id = available.getCardId() + "_" + cardCount++; 
			final JsonCard card = new JsonCard();
			
			card.setMultiverseId(available.getCardId());
			card.setId(id);
			
			availablePicks.add(card);
		}
		
		final String pickKey = packPick.getCardId()  + "_" + cardCount;
		final JsonCard pickedCard = new JsonCard();
		
		pickedCard.setMultiverseId(packPick.getCardId());
		pickedCard.setId(packPick.getCardId()  + "_" + cardCount);
		
		availablePicks.add(pickedCard);
		
		jsonPackPick.setPick(pickKey);

		long seed = System.nanoTime();
		
		Collections.shuffle(availablePicks, new Random(seed));
		Collections.shuffle(availablePicks, new Random(seed));		
		
		jsonPackPick.setAvailable(availablePicks);		
		
		jsonPackPick.setDraftMetaData(getJsonDraftFromDraft(draftDao.getDraftById(draftId)));
		
		return jsonPackPick;
	}
	
	public List<JsonDraft> getRecentDrafts(final Integer numberOfDrafts) {
		return draftDao.getRecentDrafts(numberOfDrafts)
				.stream()
				.map(this::getJsonDraftFromDraft)
				.collect(Collectors.toList());
	}
	
	public JsonPlayerStats getDraftsByPlayerId(final Integer playerId) {
		final JsonPlayerStats stats = new JsonPlayerStats();
		final List<JsonDraft> drafts = draftDao.getDraftsByPlayerId(playerId)
											   .stream()
											   .map(this::getJsonDraftFromDraft)
											   .collect(Collectors.toList()); 
		
		stats.setDrafts(drafts);
		stats.setPlayer(getJsonPlayerFromPlayer(draftDao.getPlayerById(playerId)));
		
		Integer gamesPlayed = 0;
		Integer gamesWon = 0;
		
		for(JsonDraft draft : drafts) {
			gamesPlayed += draft.getWins() + draft.getLosses();
			gamesWon += draft.getWins();
		}
		
		stats.setWinPercentage(BigDecimal.valueOf(gamesWon).divide(BigDecimal.valueOf(gamesPlayed), 2, RoundingMode.DOWN));
		
		return stats;
	}	
	
	@Transactional(readOnly = true)
	public JsonAllPicks getAllPicks(final Integer draftId) {
		final JsonAllPicks picks = new JsonAllPicks();
		
		final List<Integer> allCards = draftDao.getDistinctMultiverseIdsForDraft(draftId);
		final List<Integer> picksInOrder = draftDao.getAllPicksInOrder(draftId);
		
		picks.setAllCards(allCards);
		picks.setPicksInOrder(picksInOrder);
		
		return picks;
	}
	
	public List<JsonPlayer> getPlayersSearch(final String searchString) {
		return draftDao.getPlayersSearch(searchString)
				.stream()
				.map(this::getJsonPlayerFromPlayer)
				.collect(Collectors.toList());
	}

	private Draft convertRawDraft(RawDraft aRawDraft, String name, Integer wins, Integer losses) {
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
		draft.setWins(wins);
		draft.setLosses(losses);
		
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
			
			for(DraftPackPick draftPackPick : createDraftPackPicks(aRawDraft.getPackToListOfPickToAvailablePicksMap().get(i),
																   cardNameToCardMap,
																   aRawDraft.getTempIdToCardNameMap())) {
				
				draftPack.addDraftPackPick(draftPackPick);
			}
			
			draftPack.setSequenceId(i);
			draftPack.setPackSize(aRawDraft.getPackSizes().get(i));
			
			draftPacks.add(draftPack);
		}
		
		return draftPacks;
	}

	private List<DraftPackPick> createDraftPackPicks(final List<MutablePair<Integer, List<Integer>>> listOfPicksToAvailablePicks,
													 final Map<String, Card> cardNameToCardMap, 
													 final Map<Integer, String> tempIdToCardNameMap) {
		List<DraftPackPick> packPicks = new ArrayList<DraftPackPick>(listOfPicksToAvailablePicks.size());
		
		for(int i = 0; i < listOfPicksToAvailablePicks.size(); i++) {
			Pair<Integer, List<Integer>> pickToAvailablePicks = listOfPicksToAvailablePicks.get(i);
			DraftPackPick packPick = new DraftPackPick();
			Card pick = cardNameToCardMap.get(tempIdToCardNameMap.get(pickToAvailablePicks.getLeft()));
			
			packPick.setCard(pick);
			packPick.setCardId(pick.getId());
			packPick.setSequenceId(i);
			
			for(Integer availablePickTempId : pickToAvailablePicks.getRight()) {
				DraftPackAvailablePick availablePick = new DraftPackAvailablePick();
				Card available = cardNameToCardMap.get(tempIdToCardNameMap.get(availablePickTempId));
				availablePick.setCard(available);
				availablePick.setDraftPackPick(packPick);
				availablePick.setCardId(available.getId());
				
				packPick.addDraftPackAvailablePick(availablePick);
			}

			packPicks.add(packPick);
		}
		
		return packPicks;
	}

	private Map<String, String> createCardNameToCardSetMap(RawDraft aRawDraft) {
		Map<String, String> cardNameToCardSetMap = new HashMap<String, String>();
		
		for(Map.Entry<String, RawCard> anEntry : aRawDraft.getCardNameToRawCardMap().entrySet()) {
			cardNameToCardSetMap.put(anEntry.getKey(), anEntry.getValue().getSetCode());
		}
		
		return cardNameToCardSetMap;
	}

	private JsonDraft getJsonDraftFromDraft(final Draft draft) {
		final JsonDraft jsonDraft = new JsonDraft();
		final List<JsonPlayer> otherPlayers = new ArrayList<JsonPlayer>();
		final List<JsonPack> jsonPacks = new ArrayList<JsonPack>();		
		final DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy h:mm:ss aa");
		
		jsonDraft.setEventDate(fmt.print(new DateTime(draft.getEventDate())));
		jsonDraft.setCreated(fmt.print(new DateTime(draft.getCreated())));
		jsonDraft.setEventId(draft.getEventId());
		jsonDraft.setId(draft.getId());
		jsonDraft.setName(draft.getName());
		jsonDraft.setWins(draft.getWins());
		jsonDraft.setLosses(draft.getLosses());
		
		for(DraftPlayer draftPlayer : draft.getDraftPlayers()) {
			Player player = draftPlayer.getPlayer();
			JsonPlayer jsonPlayer = getJsonPlayerFromPlayer(player);
			
			if(draftPlayer.getIsActivePlayer()) {
				jsonDraft.setActivePlayer(jsonPlayer);
			} else {
				otherPlayers.add(jsonPlayer);
			}
		}
		
		jsonDraft.setPlayers(otherPlayers);
		
		for(DraftPack pack : draft.getDraftPacks()) {
			JsonPack jsonPack = new JsonPack();
			
			jsonPack.setId(pack.getId());
			jsonPack.setSetCode(pack.getSet().getName());
			jsonPack.setPackSize(pack.getPackSize());
			
			jsonPacks.add(jsonPack);
		}
		
		jsonDraft.setPacks(jsonPacks);
		
		return jsonDraft;
	}	
	
	private JsonPlayer getJsonPlayerFromPlayer(final Player aPlayer) {
		final JsonPlayer jsonPlayer = new JsonPlayer();
		
		jsonPlayer.setId(aPlayer.getId());
		jsonPlayer.setName(aPlayer.getName());
		
		return jsonPlayer;
	}

	private boolean isDraftValid(RawDraft aDraft) {
		if(aDraft.getPackSets().isEmpty() || 
		   aDraft.getPackSets().size() < 3) {
			return true;
		}
		
		//Simplest way I could think of identifying an incomplete draft.
		//If there aren't exactly 3 last picks, this file is bogus.
		int lastPicks = 0;
		Map<Integer, List<MutablePair<Integer, List<Integer>>>> pickMapList = aDraft.getPackToListOfPickToAvailablePicksMap();
		
		for(Map.Entry<Integer, List<MutablePair<Integer, List<Integer>>>> entry : pickMapList.entrySet()) {
			List<MutablePair<Integer, List<Integer>>> listOfPackPicks = entry.getValue();
			
			if(listOfPackPicks == null || listOfPackPicks.contains(null)){
				return true;
			}			
			
			for(MutablePair<Integer, List<Integer>> packPicks : listOfPackPicks) {
				List<Integer> available = packPicks.getRight();
				
				if(CollectionUtils.isEmpty(available)) {
					lastPicks++;
				}
			}
			
		}
		
		if(lastPicks != 3) {
			return true;
		}
		
		return false;
	}	
}
