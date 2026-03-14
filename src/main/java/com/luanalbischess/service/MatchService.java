package com.luanalbischess.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.luanalbischess.domain.Match;
import com.luanalbischess.domain.Move;
import com.luanalbischess.domain.Position;
import com.luanalbischess.domain.chessgame.ChessGame;
import com.luanalbischess.domain.enums.MatchStatus;
import com.luanalbischess.domain.enums.PieceColor;
import com.luanalbischess.helpers.FenConverter;
import com.luanalbischess.helpers.MatchConverter;
import com.luanalbischess.helpers.SanNotationMaker;
import com.luanalbischess.persistence.MatchEntity;
import com.luanalbischess.persistence.MatchRepository;
import com.luanalbischess.websocket.dtos.MoveCreateDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchService {
	private final ChessGame chessGame;
	private final FenConverter fenConverter;
	private final MatchConverter matchConverter;
	private final SanNotationMaker sanMaker;

	private final MatchRepository repository;

	public MatchEntity create(MatchEntity preSetMatch) {
		checkPlayersAvailability(preSetMatch);

		return repository.save(
				MatchEntity.builder()
						.id(UUID.randomUUID())
						.matchType(preSetMatch.getMatchType())
						.whiteId(preSetMatch.getWhiteId())
						.blackId(preSetMatch.getBlackId())
						.matchStatus(MatchStatus.IN_PROGRESS)
						.winnerColor(null)
						.currentFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
						.uciHistory("")
						.sanHistory("")
						.isFinished(false)
						.createdAt(LocalDateTime.now())
						.updatedAt(LocalDateTime.now())
						.build());

	}

	public MatchEntity applyMove(MoveCreateDTO dto) {
		MatchEntity matchEntity = repository.findById(dto.matchId())
				.orElseThrow(() -> new RuntimeException("Match not found"));

		validateMatchData(matchEntity, dto);

		Match matchDomain = matchConverter.toDomain(matchEntity);
		Match beforeMoveCopy = matchConverter.toDomain(matchEntity);

		Move move = fenConverter.uciToMove(dto.fenMove().substring(0, 4));
		Character promotionChar = dto.fenMove().length() == 5 ? dto.fenMove().charAt(4) : null;

		Match afterMove = promotionChar != null
				? chessGame.performMovePromotion(matchDomain, move, String.valueOf(promotionChar))
				: chessGame.performMove(matchDomain, move);

		MatchEntity updatedEntity = matchConverter.toEntity(afterMove, matchEntity);

		updatedEntity.setSanHistory(updatedEntity.getSanHistory().isBlank()
				? sanMaker.make(beforeMoveCopy, afterMove, move)
				: updatedEntity.getSanHistory() + " " + sanMaker.make(beforeMoveCopy, afterMove, move));

		updatedEntity.setUciHistory(updatedEntity.getUciHistory().isBlank()
				? dto.fenMove()
				: updatedEntity.getUciHistory() + " " + dto.fenMove());

		return repository.save(updatedEntity);
	}

	public MatchEntity findById(UUID id) {
		return repository.findById(id).orElseThrow(RuntimeException::new);
	}

	public List<String> getMatchUciMoveOptions(UUID matchId) {
		MatchEntity matchEntity = repository.findById(matchId)
				.orElseThrow(() -> new RuntimeException("Match not found"));

		if (matchEntity.getMatchStatus() != MatchStatus.IN_PROGRESS
				&& matchEntity.getMatchStatus() != MatchStatus.CHECK) {
			return List.of();
		}

		Match matchDomain = matchConverter.toDomain(matchEntity);

		List<String> moveOptions = new ArrayList<>();

		matchDomain.getBoard()
				.getPiecesOnBoardByColor(matchDomain.getCurrentColorTurn())
				.stream()
				.forEach(piece -> {
					List<Position> targets = chessGame.getPiecePossibleMovesPositions(matchDomain, piece);
					targets.forEach(target -> moveOptions
							.add(fenConverter.moveToUci(new Move(piece.getPosition(), target))));

				});

		return moveOptions;
	}

	private UUID getCurrentPlayerId(MatchEntity matchEntity) {
		PieceColor currentColorTurn = fenConverter.fenToColorTurn(matchEntity.getCurrentFen());
		return currentColorTurn == PieceColor.WHITE
				? matchEntity.getWhiteId()
				: matchEntity.getBlackId();
	}

	private void checkPlayersAvailability(MatchEntity preSetmatch) {
		if (repository.existsByWhiteIdAndIsFinishedFalse(preSetmatch.getWhiteId())) {
			throw new RuntimeException("Erro: Usuário WHITE já está em uma partida ativa");
		}
		if (repository.existsByBlackIdAndIsFinishedFalse(preSetmatch.getBlackId())) {
			throw new RuntimeException("Erro: Usuário BLACK já está em uma partida ativa");
		}
	}

	private void validateMatchData(MatchEntity matchEntity, MoveCreateDTO move) {
		if (!matchEntity.getId().equals(move.matchId())) {
			throw new RuntimeException();
		}

		if (!move.playerId().equals(getCurrentPlayerId(matchEntity))) {
			throw new RuntimeException("Não é o seu turno");
		}
		if (matchEntity.getIsFinished()) {
			throw new RuntimeException("A partida já terminou");
		}

	}

	public void deleteMatch(UUID matchId) {
		repository.delete(matchId);

	}

}
