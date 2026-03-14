package com.luanalbischess.helpers;

import java.util.List;

import org.springframework.stereotype.Component;

import com.luanalbischess.domain.Match;
import com.luanalbischess.domain.Move;
import com.luanalbischess.domain.Piece;
import com.luanalbischess.domain.Position;
import com.luanalbischess.domain.chessgame.ChessGame;
import com.luanalbischess.domain.chesspieces.King;
import com.luanalbischess.domain.chesspieces.Pawn;
import com.luanalbischess.domain.enums.MatchStatus;
import com.luanalbischess.domain.enums.PieceType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class SanNotationMaker {
	private final ChessGame chessGame;

	public String make(Match beforeMove, Match afterMove, Move move) {
		Piece moving = beforeMove.getBoard().getPieceByPosition(move.getSource());
		String hasCapturedNotation = hasCapturedPiece(beforeMove, afterMove) ? "x" : "";

		StringBuilder san = new StringBuilder();

		if (isCastlingMove(moving, move)) {
			san.append(move.getTarget().getColumn() == 6 ? "O-O" : "O-O-O");

		} else if (moving instanceof Pawn) {

			if (!hasCapturedNotation.isBlank()) {
				san.append(getColumnNotation(move.getSource().getColumn()));
				san.append(hasCapturedNotation);
			}

			san.append(toSquare(move.getTarget()));

			if (move.getTarget().getRow() == 0 || move.getTarget().getRow() == 7) {
				Piece promoted = afterMove.getBoard().getPieceByPosition(move.getTarget());
				san.append('=').append(promoted.getPieceType().name().charAt(0));
			}

		} else {
			String pieceLetter = getPieceLetterNotation(moving.getPieceType());
			san.append(pieceLetter);

			List<Piece> piecesWithAmbiguity = getPiecesWithAmbiguityMove(moving, beforeMove, move.getTarget());
			if (!piecesWithAmbiguity.isEmpty()) {
				if (piecesWithAmbiguity.stream()
						.noneMatch(p -> p.getPosition().getColumn() == moving.getPosition().getColumn()))
					san.append(getColumnNotation(moving.getPosition().getColumn()));

				if (piecesWithAmbiguity.stream()
						.noneMatch(p -> p.getPosition().getRow() == moving.getPosition().getRow()))
					san.append(getRowNotation(moving.getPosition().getRow()));
			}
			san.append(hasCapturedNotation);
			san.append(toSquare(move.getTarget()));
		}

		san.append(getMatchStatusNotation(afterMove.getMatchStatus()));
		return san.toString();
	}

	private String getMatchStatusNotation(MatchStatus status) {
		return switch (status) {
		case CHECK -> "+";
		case CHECKMATE -> "#";
		default -> "";
		};
	}

	private List<Piece> getPiecesWithAmbiguityMove(Piece piece, Match match, Position target) {
		return match.getBoard()
				.getPiecesOnBoardByColorAndType(piece.getPieceColor(), piece.getPieceType())
				.stream()
				.filter(p -> p != piece &&
						chessGame.getPiecePossibleMovesPositions(match, p)
								.stream()
								.anyMatch(pos -> pos.getRow() == target.getRow() &&
										pos.getColumn() == target.getColumn()))
				.toList();
	}

	private boolean hasCapturedPiece(Match before, Match after) {
		return before.getBoard().getPiecesOnBoard().size() > after.getBoard().getPiecesOnBoard().size();
	}

	private boolean isCastlingMove(Piece moving, Move move) {
		if (!(moving instanceof King)) {
			return false;
		}

		int colDiff = move.getSource().getColumn() - move.getTarget().getColumn();

		return colDiff == 2 || colDiff == -2;
	}

	private String toSquare(Position pos) {
		return getColumnNotation(pos.getColumn()) + getRowNotation(pos.getRow());
	}

	private String getColumnNotation(int col) {
		return String.valueOf((char) ('a' + col));
	}

	private String getRowNotation(int row) {
		return String.valueOf(8 - row);
	}

	private String getPieceLetterNotation(PieceType type) {
		return type == PieceType.KNIGHT ? "N" : type.name().substring(0, 1);
	}

}
