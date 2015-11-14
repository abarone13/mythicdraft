package com.cd.mythicdraft.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cd.mythicdraft.json.JsonAllPicks;
import com.cd.mythicdraft.json.JsonDeck;
import com.cd.mythicdraft.json.JsonDraft;
import com.cd.mythicdraft.json.JsonPackPick;
import com.cd.mythicdraft.json.JsonPlayer;
import com.cd.mythicdraft.json.JsonPlayerStats;
import com.cd.mythicdraft.json.JsonRecentDrafts;
import com.cd.mythicdraft.service.DraftService;

@Controller
public class DraftController {

	@Autowired
	private DraftService draftService;
	
	@RequestMapping(value = "/draft/recent/page/{pageNumber}")
	public @ResponseBody JsonRecentDrafts getRecentDrafts(@PathVariable final Integer pageNumber) {
		return draftService.getRecentDrafts(10, pageNumber);
	}
	
	@RequestMapping(value = "/draft/{draftId}",
					method = RequestMethod.GET)
	public @ResponseBody JsonDraft getDraftById(@PathVariable final Integer draftId) {
		
		return draftService.getDraftById(draftId);
	}
	
	@RequestMapping(value = "/draft/{draftId}/pack/{packId}/pick/{pickId}",
					method = RequestMethod.GET)	
	public @ResponseBody JsonPackPick getPackByIdAndPick(@PathVariable final Integer draftId,
														 @PathVariable final Integer packId,
		   	   											 @PathVariable final Integer pickId) {
		
		return draftService.getPackByIdAndPick(draftId, packId, pickId);
	}
	
	@RequestMapping(value = "/draft/{draftId}/all",
					method = RequestMethod.GET)	
	public @ResponseBody JsonAllPicks getAllPicks(@PathVariable final Integer draftId) {

		return draftService.getAllPicks(draftId);
	}
	
	@RequestMapping(value = "/draft/player/{playerId}")
	public @ResponseBody JsonPlayerStats getDraftsByPlayerId(@PathVariable final Integer playerId) {
		return draftService.getDraftsByPlayerId(playerId);
	}
	
	@RequestMapping(value = "/player/search")
	public @ResponseBody List<JsonPlayer> getPlayersSearch(@RequestParam("name") final String name) {
		return draftService.getPlayersSearch(name);
	}	
	
	@RequestMapping(value = "/draft/{deckId}/deck")
	public @ResponseBody JsonDeck getDeckById(@PathVariable final Integer deckId) {
		return draftService.getDeckById(deckId);
	}
}


