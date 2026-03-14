package com.luanalbischess.websocket.controller;

import java.util.Set;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.luanalbischess.domain.enums.MatchType;
import com.luanalbischess.service.MatchService;
import com.luanalbischess.websocket.dtos.JoinFriendlyMatchDTO;
import com.luanalbischess.websocket.dtos.MatchCreateDTO;
import com.luanalbischess.websocket.dtos.MoveCreateDTO;
import com.luanalbischess.websocket.mapper.MatchMapper;
import com.luanalbischess.websocket.session.ConnectionRegistry;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {
	private static final String MATCHMAKING_QUEUE = "/queue/matchmaking/";
	private static final String MATCH_TOPIC = "/topic/match/";
	private static final String FRIENDLY_MATCH_TOPIC_REQUEST = "/topic/friendlymatch/request/";

	private final MatchService matchService;
	private final MatchMapper matchMapper;
	private final SimpMessagingTemplate messaging;
	private final ConnectionRegistry connectionRegistry;

	@MessageMapping("/matchmaking")
	public void matchmaking(UUID playerId, SimpMessageHeaderAccessor headerAccessor) {
		String sessionId = headerAccessor.getSessionId();

		if (sessionId == null || playerId == null)
			return;

		if (connectionRegistry.isInOnlineMatch(playerId)) {
			log.info("Player {} já está em partida online, reconectando...", playerId);
			reconnect(MatchType.ONLINE, playerId);
			return;
		}

		connectionRegistry.register(sessionId, playerId);
		connectionRegistry.addToMatchmaking(sessionId, playerId);
		log.info("Player {} registrado e adicionado ao matchmaking", playerId);

		if (connectionRegistry.getMatchMakingCount() >= 2) {
			UUID white = connectionRegistry.pollMatchMaking();
			UUID black = connectionRegistry.pollMatchMaking();
			log.info("Criando partida online entre {} (white) e {} (black)", white, black);
			createMatch(MatchType.ONLINE, white, black);
		}
	}

	@MessageMapping("/friendlymatch/request")
	public void requestFriendly(UUID playerId, SimpMessageHeaderAccessor headerAccessor) {
		String sessionId = headerAccessor.getSessionId();

		if (sessionId == null || playerId == null)
			return;

		if (connectionRegistry.isWaitingFriendlyMatch(playerId))
			return;

		if (connectionRegistry.isInFriendlyMatch(playerId)) {
			log.info("Player {} já está em friendly match, reconectando...", playerId);
			reconnect(MatchType.FRIENDLY, playerId);
			return;
		}

		UUID waitingFriendlyId = UUID.randomUUID();
		connectionRegistry.register(sessionId, playerId);
		connectionRegistry.addToWaitingFriendlyMatchRoom(waitingFriendlyId, playerId);
		log.info("Player {} registrado e aguardando friendly match (roomId {})", playerId, waitingFriendlyId);
		messaging.convertAndSend(FRIENDLY_MATCH_TOPIC_REQUEST + playerId.toString(), waitingFriendlyId);
	}

	@MessageMapping("/friendlymatch/join")
	public void joinFriendly(@Valid JoinFriendlyMatchDTO dto, SimpMessageHeaderAccessor headerAccessor) {
		String sessionId = headerAccessor.getSessionId();
		if (sessionId == null)
			return;

		connectionRegistry.addToWaitingFriendlyMatchRoom(dto.matchId(), dto.playerId());
		Set<UUID> players = connectionRegistry.getPlayersIdInWaitingFriendlyRoom(dto.matchId());

		if (players.size() != 2 || players.stream().anyMatch(connectionRegistry::isInFriendlyMatch)) {
			players.forEach(player -> reconnect(MatchType.FRIENDLY, player));
			return;
		}

		UUID whiteId = players.iterator().next();
		UUID blackId = players.stream().filter(p -> !p.equals(whiteId)).findFirst().orElseThrow();

		connectionRegistry.register(sessionId, dto.playerId());
		connectionRegistry.assignPlayersToFriendlyMatch(dto.matchId(), players);
		log.info("Criando friendly match {} entre {} (white) e {} (black)", dto.matchId(), whiteId, blackId);
		createMatch(MatchType.FRIENDLY, whiteId, blackId);
	}

	@MessageMapping("/move")
	public void move(MoveCreateDTO dto) {
		try {
			var entity = matchService.applyMove(dto);
			var response = matchMapper.toDTO(entity, matchService.getMatchUciMoveOptions(entity.getId()));
			messaging.convertAndSend(MATCH_TOPIC + entity.getId(), response);

			if (response.isFinished()) {
				matchService.deleteMatch(entity.getId());

				switch (response.matchType()) {
				case ONLINE -> connectionRegistry.finishOnlineMatch(response.id());
				case FRIENDLY -> connectionRegistry.finishFriendlyMatch(response.id());
				default -> throw new IllegalArgumentException("Unexpected value: " + response.matchType());
				}
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void reconnect(MatchType type, UUID playerId) {
		try {
			UUID matchId = switch (type) {
			case ONLINE -> connectionRegistry.getOnlineMatch(playerId);
			case FRIENDLY -> connectionRegistry.getFriendlyMatch(playerId);
			default -> throw new IllegalArgumentException("Unexpected value: " + type);
			};

			log.info("Reconectando player {} para {} match (matchId {})", playerId, type, matchId);

			var entity = matchService.findById(matchId);
			var response = matchMapper.toDTO(entity, matchService.getMatchUciMoveOptions(matchId));
			messaging.convertAndSend(MATCHMAKING_QUEUE + playerId.toString(), response);

		} catch (Exception e) {
			log.error("Erro ao reconectar player {}: {}", playerId, e.getMessage(), e);
		}
	}

	private void createMatch(MatchType type, UUID white, UUID black) {
		try {
			var entity = matchService.create(matchMapper.toEntity(new MatchCreateDTO(type, white, black)));
			var response = matchMapper.toDTO(entity, matchService.getMatchUciMoveOptions(entity.getId()));

			log.info("Partida criada (type: {}, matchId: {}) entre {} (white) e {} (black)", type, entity.getId(),
					white, black);

			if (type == MatchType.ONLINE)
				connectionRegistry.assignPlayersToMatch(entity.getId(), Set.of(white, black));
			if (type == MatchType.FRIENDLY)
				connectionRegistry.assignPlayersToFriendlyMatch(entity.getId(), Set.of(white, black));

			messaging.convertAndSend(MATCHMAKING_QUEUE + white.toString(), response);
			messaging.convertAndSend(MATCHMAKING_QUEUE + black.toString(), response);
		} catch (Exception e) {
			log.error("Erro ao criar partida: {}", e.getMessage(), e);
		}
	}
}
