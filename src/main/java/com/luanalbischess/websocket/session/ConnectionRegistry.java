package com.luanalbischess.websocket.session;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ConnectionRegistry {
	private final Map<UUID, String> playerToSession = new ConcurrentHashMap<>();
	private final Map<String, UUID> sessionToPlayer = new ConcurrentHashMap<>();

	private final Queue<UUID> matchmakingQueue = new ConcurrentLinkedQueue<>();
	private final Map<UUID, UUID> playerToWaitingFriendltMatchRoomId = new ConcurrentHashMap<>();
	private final Map<UUID, Set<UUID>> waitingFriendltMatchRoomIdToPlayer = new ConcurrentHashMap<>();

	private final Map<UUID, UUID> playerToOnlineMatch = new ConcurrentHashMap<>();
	private final Map<UUID, UUID> playerToFriendlyMatch = new ConcurrentHashMap<>();

	private final Map<UUID, Set<UUID>> onlineMatchToPlayers = new ConcurrentHashMap<>();
	private final Map<UUID, Set<UUID>> friendlyMatchToPlayers = new ConcurrentHashMap<>();

	public void register(String sessionId, UUID playerId) {
		if (playerToSession.containsKey(playerId))
			return;
		playerToSession.put(playerId, sessionId);
		sessionToPlayer.put(sessionId, playerId);
	}

	public void unregister(String sessionId) {
		UUID playerId = sessionToPlayer.remove(sessionId);
		if (playerId == null) {
			return;
		}

		playerToSession.remove(playerId);
		matchmakingQueue.remove(playerId);

		UUID waitingRoomId = playerToWaitingFriendltMatchRoomId.remove(playerId);
		if (waitingRoomId != null) {
			waitingFriendltMatchRoomIdToPlayer.remove(waitingRoomId);
			log.info("Player {} removido da waiting friendly match {}", playerId, waitingRoomId);
		}

		log.info("Player {} removido (sessionId {})", playerId, sessionId);
	}

	public void addToMatchmaking(String sessionId, UUID playerId) {
		if (isInMatchmaking(playerId) || isInOnlineMatch(playerId))
			return;

		matchmakingQueue.add(playerId);
		log.info("Player adicionado no matchmaking (playerId {})", playerId);
	}

	public UUID pollMatchMaking() {
		UUID playerId = matchmakingQueue.poll();
		if (playerId != null) {
			log.info("Player {} removido da fila de matchmaking para criar partida", playerId);
		}
		return playerId;
	}

	public boolean isInMatchmaking(UUID playerId) {
		return matchmakingQueue.contains(playerId);
	}

	public void assignPlayersToMatch(UUID matchId, Set<UUID> players) {
		players.forEach(playerId -> playerToOnlineMatch.put(playerId, matchId));
		onlineMatchToPlayers.put(matchId, players);
		players.forEach(playerId -> log.info("Player {} atribuído à partida {}", playerId, matchId));

	}

	public UUID getOnlineMatch(UUID playerId) {
		return playerToOnlineMatch.get(playerId);
	}

	public boolean isInOnlineMatch(UUID playerId) {
		return playerToOnlineMatch.containsKey(playerId);
	}

	public int getMatchMakingCount() {
		return matchmakingQueue.size();
	}

	public void finishOnlineMatch(UUID matchId) {
		Set<UUID> players = onlineMatchToPlayers.remove(matchId);
		if (players != null) {
			players.forEach(playerToOnlineMatch::remove);

			UUID first = players.iterator().next();
			UUID second = players.stream().filter(p -> !p.equals(first)).findFirst().orElse(null);

			log.info("Partida {} finalizada, players {} e {} removidos", matchId, first, second);
		}
	}

	//

	public void addToWaitingFriendlyMatchRoom(UUID roomId, UUID playerId) {
		playerToWaitingFriendltMatchRoomId.put(playerId, roomId);
		waitingFriendltMatchRoomIdToPlayer.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(playerId);
		log.info("Player {} adicionado em waiting friendly room(roomId {})", playerId, roomId);
	}

	public boolean isWaitingFriendlyMatch(UUID playerId) {
		return playerToWaitingFriendltMatchRoomId.containsKey(playerId);
	}

	public Set<UUID> getPlayersIdInWaitingFriendlyRoom(UUID roomId) {
		return waitingFriendltMatchRoomIdToPlayer.get(roomId);
	}

	public void assignPlayersToFriendlyMatch(UUID matchId, Set<UUID> players) {
		players.forEach(playerId -> playerToFriendlyMatch.put(playerId, matchId));
		friendlyMatchToPlayers.put(matchId, players);

		UUID whitePlayerId = players.iterator().next();
		UUID roomId = playerToWaitingFriendltMatchRoomId.remove(whitePlayerId);
		waitingFriendltMatchRoomIdToPlayer.remove(roomId);

		players.forEach(playerId -> log.info("Player {} atribuído à friendly match {}", playerId, matchId));
	}

	public UUID getFriendlyMatch(UUID playerId) {
		return playerToFriendlyMatch.get(playerId);
	}

	public boolean isInFriendlyMatch(UUID playerId) {
		return playerToFriendlyMatch.containsKey(playerId);
	}

	public void finishFriendlyMatch(UUID roomId) {
		Set<UUID> players = friendlyMatchToPlayers.remove(roomId);
		if (players != null) {
			players.stream().forEach(playerToFriendlyMatch::remove);
			log.info("Friendly match {} finalizada, players {} removidos", roomId, players);
		}
	}

}
