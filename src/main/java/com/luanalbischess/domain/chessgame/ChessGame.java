package com.luanalbischess.domain.chessgame;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.luanalbischess.domain.Board;
import com.luanalbischess.domain.Match;
import com.luanalbischess.domain.Move;
import com.luanalbischess.domain.Piece;
import com.luanalbischess.domain.Position;
import com.luanalbischess.domain.enums.PieceType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ChessGame {

	private final ChessMoveLogic moveLogic;
	private final ChessMatchStateUpdater stateUpdater;

	public Match performMove(Match match, Move move) {
		moveLogic.validateMove(match, move);
		stateUpdater.updateMatchState(match, move);
		return match;
	}

	public Match performMovePromotion(Match match, Move move, String promotionChar) {
		PieceType chosenType = switch (promotionChar) {
		case "Q" -> PieceType.QUEEN;
		case "R" -> PieceType.ROOK;
		case "B" -> PieceType.BISHOP;
		case "N" -> PieceType.KNIGHT;
		default -> throw new IllegalArgumentException("Invalid promotion piece: " + promotionChar);
		};

		moveLogic.validateMove(match, move);
		stateUpdater.updateMatchStateWithPromotion(match, move, chosenType);
		return match;
	}

	public List<Position> getPiecePossibleMovesPositions(Match match, Piece piece) {
		boolean[][] matPossibleMoves = moveLogic.calculatePiecePossibleMoves(match, piece);
		List<Position> targets = new ArrayList<>();

		for (int row = 0; row < Board.ROWS; row++) {
			for (int col = 0; col < Board.COLUMNS; col++) {
				if (matPossibleMoves[row][col]) {
					targets.add(new Position(row, col));
				}
			}
		}

		return targets;
	}

}
