package com.luanalbischess.domain;

import com.luanalbischess.domain.enums.PieceColor;
import com.luanalbischess.domain.enums.PieceType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class Piece {
	private PieceColor pieceColor;
	private PieceType pieceType;
	private Board board;
	protected Position position;

	public abstract boolean[][] possibleMoves();

	protected abstract int[][] possibleDirections();

	protected boolean isThereOpponentPiece(Position position) {
		Piece piece = board.getPieceByPosition(position);
		return piece != null && piece.getPieceColor() != this.pieceColor;
	}

	protected boolean canMove(Position pos) {
		return board.positionExists(pos) && board.getPieceByPosition(pos) == null;
	}

	protected boolean canCapture(Position pos) {
		return board.positionExists(pos) && isThereOpponentPiece(pos);
	}

}
