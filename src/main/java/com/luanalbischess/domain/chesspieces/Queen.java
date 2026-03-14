package com.luanalbischess.domain.chesspieces;

import com.luanalbischess.domain.Board;
import com.luanalbischess.domain.Piece;
import com.luanalbischess.domain.Position;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
public class Queen extends Piece{

	@Override
	public boolean[][] possibleMoves() {
		boolean[][] mat = new boolean[Board.ROWS][Board.COLUMNS];
		Position aux = new Position(0, 0);

		int[][] directions = possibleDirections();
		
		int row = position.getRow();
		int column = position.getColumn();

		for (int[] dir : directions) {
			aux.setValues(row + dir[0], column + dir[1]);

			while (canMove(aux)) {
				mat[aux.getRow()][aux.getColumn()] = true;
				aux.setValues(aux.getRow() + dir[0], aux.getColumn() + dir[1]);
			}

			if (canCapture(aux)) {
				mat[aux.getRow()][aux.getColumn()] = true;
			}
		}

		return mat;
	}

	@Override
	protected int[][] possibleDirections() {
		return new int[][] { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 }, { 1, 1 } };
	}

}
