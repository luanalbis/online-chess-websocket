package com.luanalbischess.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import com.luanalbischess.domain.enums.MatchStatus;
import com.luanalbischess.domain.enums.MatchType;
import com.luanalbischess.domain.enums.PieceColor;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchEntity {

	@NotNull
	private UUID id;

	@NotNull
	private UUID whiteId;

	@NotNull
	private UUID blackId;

	private PieceColor winnerColor;

	@NotNull
	private MatchStatus matchStatus;

	@NotNull
	private MatchType matchType;

	@NotNull
	private Boolean isFinished;

	@NotNull
	private String currentFen;

	@NotNull
	private String uciHistory;

	@NotNull
	private String sanHistory;

	@NotNull
	private LocalDateTime createdAt;

	@NotNull
	private LocalDateTime updatedAt;

	private LocalDateTime finishedAt;
}
