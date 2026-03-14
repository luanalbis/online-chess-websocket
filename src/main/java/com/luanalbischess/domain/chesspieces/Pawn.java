package com.luanalbischess.domain.chesspieces;

import com.luanalbischess.domain.Board;
import com.luanalbischess.domain.Piece;
import com.luanalbischess.domain.Position;
import com.luanalbischess.domain.enums.PieceColor;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
public class Pawn extends Piece {

	@Override
	public boolean[][] possibleMoves() {
		boolean[][] mat = new boolean[Board.ROWS][Board.COLUMNS];
		Position aux = new Position(0, 0);

		int step = (getPieceColor() == PieceColor.WHITE) ? -1 : 1;
		
		int row = position.getRow();
		int column = position.getColumn();

		aux.setValues(row + step, column);
		if (canMove(aux)) {
			mat[aux.getRow()][aux.getColumn()] = true;

			boolean isInitialRow = (getPieceColor() == PieceColor.WHITE && row == 6) ||
					(getPieceColor() == PieceColor.BLACK && row == 1);

			if (isInitialRow) {
				Position middle = new Position(row + step, column);
				Position twoSteps = new Position(row + (step * 2), column);

				if (canMove(middle) && canMove(twoSteps)) {
					mat[twoSteps.getRow()][twoSteps.getColumn()] = true;
				}
			}
		}

		aux.setValues(row + step, column - 1);
		if (canCapture(aux)) {
			mat[aux.getRow()][aux.getColumn()] = true;
		}

		aux.setValues(row + step, column + 1);
		if (canCapture(aux)) {
			mat[aux.getRow()][aux.getColumn()] = true;
		}

		return mat;
	}

	@Override
	protected int[][] possibleDirections() {
		throw new UnsupportedOperationException("Pawn does not use possibleDirections()");
	}
}
