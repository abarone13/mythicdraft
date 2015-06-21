package com.cd.mythicdraft.service;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.cd.mythicdraft.domain.RawDraft;
import com.cd.mythicdraft.grammar.impl.MTGODraftListenerImpl;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
public class MtgoDraftParserServiceTest {

	@Configuration
	static class ContextConfiguration {
		@Bean
		public MtgoDraftParserService mtgoDraftParserService() {
			MtgoDraftParserService mtgoDraftParserService = new MtgoDraftParserService();
			return mtgoDraftParserService;
		}
		
		@Bean
		public MTGODraftListenerImpl mtgoDraftListener() {
			MTGODraftListenerImpl mtgoDraftListener = new MTGODraftListenerImpl();
			return mtgoDraftListener;
		}
	}
	
	@Autowired
	private MtgoDraftParserService mtgoDraftParserService;
	
	@Test
	public void testTPR() throws Exception{
		InputStream tprDraftStream = loadResource("TPRTPRTPR1.txt");
		
		RawDraft aDraft = mtgoDraftParserService.parse(tprDraftStream);
		
		assertTrue(aDraft.getOtherPlayers().size() == 7);
		assertTrue(!aDraft.getActivePlayer().isEmpty());
		assertTrue(aDraft.getPackSets().size() == 3);
		assertTrue(aDraft.getEventId() == 8283017);

		System.out.println(aDraft.toString());
	}

	@Test
	public void testDTKDTKFRF() throws Exception{
		InputStream tprDraftStream = loadResource("DTKDTKFRF1.txt");
		
		RawDraft aDraft = mtgoDraftParserService.parse(tprDraftStream);
		
		assertTrue(aDraft.getOtherPlayers().size() == 7);
		assertTrue(!aDraft.getActivePlayer().isEmpty());
		assertTrue(aDraft.getPackSets().size() == 3);
		assertTrue(aDraft.getEventId() == 8305539);		
		
		String shouldBeFrf = aDraft.getPackSets().get(2);
		
		assertTrue("FRF".equals(shouldBeFrf));
		
		System.out.println(aDraft.toString());		
	}	

	@Test	
	public void testMM2MM2MM21() throws Exception{
		InputStream tprDraftStream = loadResource("MM2MM2MM21.txt");
		
		RawDraft aDraft = mtgoDraftParserService.parse(tprDraftStream);
		
		assertTrue(aDraft.getOtherPlayers().size() == 7);
		assertTrue(!aDraft.getActivePlayer().isEmpty());
		assertTrue(aDraft.getPackSets().size() == 3);
		assertTrue(aDraft.getEventId() == 8404751);		
		
		System.out.println(aDraft.toString());		
	}	
	
	@Test	
	public void testMM2MM2MM22() throws Exception{
		InputStream tprDraftStream = loadResource("MM2MM2MM22.txt");
		
		RawDraft aDraft = mtgoDraftParserService.parse(tprDraftStream);
		
		assertTrue(aDraft.getOtherPlayers().size() == 7);
		assertTrue(!aDraft.getActivePlayer().isEmpty());
		assertTrue(aDraft.getPackSets().size() == 3);
		assertTrue(aDraft.getEventId() == 8404278);		
		
		System.out.println(aDraft.toString());		
	}	
	
	private InputStream loadResource(String resourceName) throws IOException {
		return this.getClass().getResourceAsStream(resourceName);
	}
}
