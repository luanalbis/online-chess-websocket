package com.luanalbischess.domain.chessgame;

import static com.luanalbischess.domain.enums.PieceColor.BLACK;
import static com.luanalbischess.domain.enums.PieceColor.WHITE;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.luanalbischess.domain.Match;
import com.luanalbischess.domain.Move;
import com.luanalbischess.domain.Piece;
import com.luanalbischess.domain.Position;
import com.luanalbischess.domain.chesspieces.King;
import com.luanalbischess.domain.chesspieces.Pawn;
import com.luanalbischess.domain.chesspieces.Rook;
import com.luanalbischess.domain.enums.MatchStatus;
import com.luanalbischess.domain.enums.PieceColor;
import com.luanalbischess.domain.enums.PieceType;

@Component
public class ChessMatchStateUpdater {

	protected void updateMatchState(Match match, Move move) {
		Piece moved = match.getBoard().getPieceByPosition(move.getSource());
		if (isPromotionMove(moved, move.getTarget())) {
			throw new UnsupportedOperationException("This method dont support promotion moves");
		}
		Piece captured = match.movePiece(move.getSource(), move.getTarget());

		updateFullMove(match);
		updateHalfMoveClock(match, moved, captured);
		updateEnPassantTargetPosition(match, moved, move);
		updateCastlingRights(match, moved, move.getSource(), captured);
		updateMatchStatus(match);
		updateColorTurn(match);

	}

	protected void updateMatchStateWithPromotion(Match match, Move move, PieceType chosenType) {
		Piece moved = match.getBoard().getPieceByPosition(move.getSource());
		if (!isPromotionMove(moved, move.getTarget())) {
			throw new UnsupportedOperationException("This move is not a promotion move");
		}
		Piece captured = match.movePiece(move.getSource(), move.getTarget());

		updateFullMove(match);
		updateHalfMoveClock(match, moved, captured);
		updateEnPassantTargetPosition(match, moved, move);
		updateCastlingRights(match, moved, move.getSource(), captured);

		match.promotePawn(move.getTarget(), chosenType);

		updateMatchStatus(match);
		updateColorTurn(match);

	}

	private boolean isPromotionMove(Piece piece, Position target) {

		return piece.getPieceType() == PieceType.PAWN && (target.getRow() == 0 || target.getRow() == 7);
	}

	private void updateFullMove(Match match) {
		if (match.getCurrentColorTurn() == BLACK)
			match.setFullMove(match.getFullMove() + 1);

	}

	private void updateColorTurn(Match match) {
		if (match.getMatchStatus() == MatchStatus.CHECKMATE
				|| match.getMatchStatus() == MatchStatus.DRAW) {
			return;
		}
		PieceColor currentTurn = match.getCurrentColorTurn();
		PieceColor newTurn = currentTurn == WHITE ? BLACK : WHITE;
		match.setCurrentColorTurn(newTurn);

	}

	private void updateHalfMoveClock(Match match, Piece movedPiece, Piece captured) {
		if (captured != null || movedPiece instanceof Pawn) {
			match.setHalfMoveClock(0);
			return;
		}

		match.setHalfMoveClock(match.getHalfMoveClock() + 1);
	}

	private void updateEnPassantTargetPosition(Match match, Piece moved, Move move) {

		Position source = move.getSource();
		Position target = move.getTarget();

		if (!(moved instanceof Pawn)) {
			match.setEnPassantPositionTarget(null);
			return;
		}

		if (source.getRow() == 6 && target.getRow() == 4) {
			match.setEnPassantPositionTarget(new Position(5, source.getColumn()));
			return;
		}

		if (source.getRow() == 1 && target.getRow() == 3) {
			match.setEnPassantPositionTarget(new Position(2, source.getColumn()));
			return;
		}

		match.setEnPassantPositionTarget(null);

	}

	private void updateCastlingRights(Match match, Piece moved, Position source, Piece captured) {
		Map<PieceColor, String[]> sidesMap = Map.of(
				WHITE, new String[] { "K", "Q" },
				BLACK, new String[] { "k", "q" });

		if (captured instanceof Rook) {
			String[] sides = sidesMap.get(captured.getPieceColor());
			String kingSide = sides[0];
			String queenSide = sides[1];

			Position pos = captured.getPosition();
			int homeRow = captured.getPieceColor() == WHITE ? 7 : 0;

			boolean queenRookCaptured = pos.getColumn() == 0 && pos.getRow() == homeRow;
			boolean kingRookCaptured = pos.getColumn() == 7 && pos.getRow() == homeRow;

			match.getCastlingRights().put(
					kingSide,
					match.getCastlingRights().get(kingSide) && !kingRookCaptured);

			match.getCastlingRights().put(
					queenSide,
					match.getCastlingRights().get(queenSide) && !queenRookCaptured);
		}

		String[] sides = sidesMap.get(moved.getPieceColor());
		String kingSide = sides[0];
		String queenSide = sides[1];

		if (moved instanceof King) {
			match.getCastlingRights().put(kingSide, false);
			match.getCastlingRights().put(queenSide, false);
			return;
		}

		if (moved instanceof Rook) {
			int homeRow = moved.getPieceColor() == WHITE ? 7 : 0;

			boolean kingRookMoved = source.getRow() == homeRow && source.getColumn() == 7;
			boolean queenRookMoved = source.getRow() == homeRow && source.getColumn() == 0;

			boolean prevKingSideValue = match.getCastlingRights().get(kingSide);
			boolean prevQueenSideValue = match.getCastlingRights().get(queenSide);

			match.getCastlingRights().put(kingSide, prevKingSideValue && !kingRookMoved);

			match.getCastlingRights().put(queenSide, prevQueenSideValue && !queenRookMoved);
		}
	}

	private void updateMatchStatus(Match match) {

		if (match.isCheckMate(match.getOpponentColor(match.getCurrentColorTurn()))) {
			match.setMatchStatus(MatchStatus.CHECKMATE);
			match.setWinnerColor(match.getCurrentColorTurn());
			return;
		}
		if (match.isInCheck(match.getOpponentColor(match.getCurrentColorTurn()))) {
			match.setMatchStatus(MatchStatus.CHECK);
			return;
		}

		if (match.getHalfMoveClock() >= 50) {
			match.setMatchStatus(MatchStatus.DRAW);
			return;
		}

		match.setMatchStatus(MatchStatus.IN_PROGRESS);
	}

}
