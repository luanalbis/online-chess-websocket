package com.luanalbischess.domain.chessgame;

import org.springframework.stereotype.Component;

import com.luanalbischess.domain.Board;
import com.luanalbischess.domain.Match;
import com.luanalbischess.domain.Move;
import com.luanalbischess.domain.Piece;
import com.luanalbischess.domain.Position;
import com.luanalbischess.domain.chesspieces.King;
import com.luanalbischess.domain.chesspieces.Pawn;
import com.luanalbischess.domain.enums.MatchStatus;
import com.luanalbischess.domain.enums.PieceColor;

@Component
public class ChessMoveLogic {
	protected void validateMove(Match match, Move move) {

		Board board = match.getBoard();

		if (!board.thereIsAPiece(move.getSource())) {
			throw new RuntimeException("There is no piece in this position");
		}

		Piece piece = board.getPieceByPosition(move.getSource());

		if (piece.getPieceColor() != match.getCurrentColorTurn()) {
			throw new RuntimeException("This is not your turn");
		}


		if (!isAValidMove(match, move)) {
			throw new RuntimeException("This is not a valid move");
		}

	}

	protected boolean[][] calculatePiecePossibleMoves(Match match, Piece piece) {
		boolean[][] possibleMoves = piece.possibleMoves();
		boolean[][] validMoves = new boolean[Board.ROWS][Board.COLUMNS];
		
		if (piece instanceof King king) {
			calculateKingCastlingRights(possibleMoves, match, king);
		}

		if (piece instanceof Pawn pawn) {
			calculatePawnEnPassant(possibleMoves, match, pawn);
		}

		for (int row = 0; row < Board.ROWS; row++) {
			for (int col = 0; col < Board.COLUMNS; col++) {
				if (possibleMoves[row][col]) {
					Position target = new Position(row, col);
					validMoves[row][col] = canMoveWithoutCheck(match, new Move(piece.getPosition(), target));
				}
			}
		}

		return validMoves;
	}

	private boolean isAValidMove(Match match, Move move) {
		Piece piece = match.getBoard().getPieceByPosition(move.getSource());
		Position target = move.getTarget();

		boolean[][] validMoves = calculatePiecePossibleMoves(match, piece);
		return validMoves[target.getRow()][target.getColumn()];
	}

	private boolean canMoveWithoutCheck(Match match, Move move) {
		Position source = move.getSource();
		Position target = move.getTarget();

		Piece captured = match.movePiece(source, target);

		boolean isSafe = !match.isInCheck(match.getCurrentColorTurn());
		match.undoMove(source, target, captured);

		return isSafe;
	}

	private void calculatePawnEnPassant(boolean[][] possibleMoves, Match match, Pawn pawn) {
		Piece vulnerable = match.getEnPassantVulnerable();
		if (vulnerable == null || vulnerable.getPieceColor() == pawn.getPieceColor()) {
			return;
		}

		if (pawn.getPosition().getRow() != vulnerable.getPosition().getRow()) {
			return;
		}
		if (Math.abs(pawn.getPosition().getColumn() - vulnerable.getPosition().getColumn()) != 1) {
			return;
		}

		Position target = match.getEnPassantPositionTarget();
		possibleMoves[target.getRow()][target.getColumn()] = true;
	}

	private void calculateKingCastlingRights(boolean[][] possibleMoves, Match match, King king) {
		if (match.getMatchStatus() == MatchStatus.CHECK || king.getPosition().getColumn() != 4) {
			return;
		}

		Piece[][] matPieces = match.getBoard().getMatPieces();

		int row = king.getPosition().getRow();
		int col = king.getPosition().getColumn();

		boolean isWhite = king.getPieceColor() == PieceColor.WHITE;

		boolean canKingSide = isWhite ? match.getCastlingRights().get("K") : match.getCastlingRights().get("k");
		boolean canQueenSide = isWhite ? match.getCastlingRights().get("Q") : match.getCastlingRights().get("q");

		boolean isRightSideFree = matPieces[row][col + 1] == null && matPieces[row][col + 2] == null;
		boolean isLeftSideFree = matPieces[row][col - 1] == null
				&& matPieces[row][col - 2] == null
				&& matPieces[row][col - 3] == null;

		if (canKingSide
				&& isRightSideFree
				&& canMoveWithoutCheck(match, new Move(king.getPosition(), new Position(row, col + 1)))
				&& canMoveWithoutCheck(match, new Move(king.getPosition(), new Position(row, col + 2)))) {

			possibleMoves[row][col + 2] = true;
		}

		if (canQueenSide
				&& isLeftSideFree
				&& canMoveWithoutCheck(match, new Move(king.getPosition(), new Position(row, col - 1)))
				&& canMoveWithoutCheck(match, new Move(king.getPosition(), new Position(row, col - 2)))) {

			possibleMoves[row][col - 2] = true;
		}

	}

}
