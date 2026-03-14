package com.luanalbischess.websocket.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record JoinFriendlyMatchDTO(
		@NotNull
		UUID matchId,
		
		@NotNull
		UUID playerId
		) {

}
