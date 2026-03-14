package com.luanalbischess.websocket.dtos;

import java.util.List;
import java.util.UUID;

import com.luanalbischess.domain.enums.MatchStatus;
import com.luanalbischess.domain.enums.MatchType;
import com.luanalbischess.domain.enums.PieceColor;

import jakarta.validation.constraints.NotNull;

public record MatchResponseDTO(
		@NotNull 
		UUID id,
		
		UUID currentPlayerIdTurn,
		
		PieceColor 
		winnerColor,
	
		@NotNull
		MatchStatus matchStatus,
		@NotNull
		MatchType matchType,
		
		@NotNull
		Boolean isFinished,
		
		@NotNull
		String fen,
		String uciHistory,
		String sanHistory,
		List<String> uciMoveOptions) {
}
