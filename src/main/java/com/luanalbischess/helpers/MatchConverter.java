package com.luanalbischess.helpers;

import org.springframework.stereotype.Component;

import com.luanalbischess.domain.Match;
import com.luanalbischess.domain.enums.MatchStatus;
import com.luanalbischess.persistence.MatchEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MatchConverter {
	private final FenConverter fenConverter;

	public Match toDomain(MatchEntity entity) {
		if (entity == null)
			return null;

		Match match = fenConverter.fenToMatch(entity.getCurrentFen());
		match.setMatchStatus(entity.getMatchStatus());

		return match;

	}

	public MatchEntity toEntity(Match domain, MatchEntity entity) {
		if (domain == null)
			return null;

		entity.setCurrentFen(fenConverter.toFen(domain));
		entity.setMatchStatus(domain.getMatchStatus());
		entity.setWinnerColor(domain.getWinnerColor());
		entity.setIsFinished(
				domain.getMatchStatus() == MatchStatus.CHECKMATE || domain.getMatchStatus() == MatchStatus.DRAW);

		return entity;
	}
}
