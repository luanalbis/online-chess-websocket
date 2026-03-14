package com.luanalbischess.websocket.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MoveCreateDTO(
		@NotNull(message = "Required field") 
		UUID matchId,
		
		@NotNull(message = "Required field") 
		UUID playerId,
		
		@NotNull(message = "Required field") 
		@Size(min = 4, max = 5) 
		String fenMove

) {
}
