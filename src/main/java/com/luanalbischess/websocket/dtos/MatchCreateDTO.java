package com.luanalbischess.websocket.dtos;

import java.util.UUID;

import com.luanalbischess.domain.enums.MatchType;

import jakarta.validation.constraints.NotNull;

public record MatchCreateDTO(
		@NotNull
		MatchType type,

		@NotNull(message = "Required field") 
		UUID whiteId,

		@NotNull(message = "Required field") 
		UUID blackId) {

}
