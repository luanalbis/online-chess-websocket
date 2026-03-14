package com.luanalbischess.persistence;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import jakarta.validation.Valid;

@Repository
public class MatchRepository {
	private final Map<UUID, MatchEntity> matches = new ConcurrentHashMap<>();

	public MatchEntity save(@Valid MatchEntity match) {
		matches.put(match.getId(), match);
		match.setUpdatedAt(LocalDateTime.now());
		return match;
	}

	public Optional<MatchEntity> findById(UUID id) {
		return Optional.ofNullable(matches.get(id));
	}

	public boolean existsByWhiteIdAndIsFinishedFalse(UUID whiteId) {
		return matches.values().stream()
				.anyMatch(m -> m.getWhiteId().equals(whiteId) && !m.getIsFinished());
	}

	public boolean existsByBlackIdAndIsFinishedFalse(UUID blackId) {
		return matches.values().stream()
				.anyMatch(m -> m.getBlackId().equals(blackId) && !m.getIsFinished());
	}

	public void delete(UUID matchId) {
		matches.remove(matchId);
	}
}
