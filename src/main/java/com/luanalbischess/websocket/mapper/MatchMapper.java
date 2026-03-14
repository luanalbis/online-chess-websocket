package com.luanalbischess.websocket.mapper;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.luanalbischess.domain.enums.PieceColor;
import com.luanalbischess.helpers.FenConverter;
import com.luanalbischess.persistence.MatchEntity;
import com.luanalbischess.websocket.dtos.MatchCreateDTO;
import com.luanalbischess.websocket.dtos.MatchResponseDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MatchMapper {
	private final FenConverter fenConverter;

	public MatchEntity toEntity(MatchCreateDTO dto) {

		return MatchEntity.builder()
				.matchType(dto.type())
				.whiteId(dto.whiteId())
				.blackId(dto.blackId())
				.build();
	}

	public MatchResponseDTO toDTO(MatchEntity entity, List<String> moveOptions) {

		return new MatchResponseDTO(
				entity.getId(),
				getCurrentPlayerId(entity),
				entity.getWinnerColor(),
				entity.getMatchStatus(),
				entity.getMatchType(),
				entity.getIsFinished(),
				entity.getCurrentFen(),
				entity.getUciHistory(),
				entity.getSanHistory(),
				moveOptions);

	}

	private UUID getCurrentPlayerId(MatchEntity entity) {
		if (entity.getWinnerColor() != null) {
			return null;
		}
		PieceColor currentColor = fenConverter.fenToColorTurn(entity.getCurrentFen());
		return currentColor == PieceColor.WHITE ? entity.getWhiteId() : entity.getBlackId();
	}
}
