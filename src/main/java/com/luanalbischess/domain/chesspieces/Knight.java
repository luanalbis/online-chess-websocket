package com.luanalbischess.domain.chesspieces;

import com.luanalbischess.domain.Board;
import com.luanalbischess.domain.Piece;
import com.luanalbischess.domain.Position;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
public class Knight extends Piece {

	@Override
	public boolean[][] possibleMoves() {
		boolean[][] mat = new boolean[Board.ROWS][Board.COLUMNS];

		int[][] directions = possibleDirections();
		
		int row = position.getRow();
		int column = position.getColumn();

		for (int[] dir : directions) {
			Position aux = new Position(row + dir[0], column + dir[1]);

			if (canMove(aux) || canCapture(aux)) {
				mat[aux.getRow()][aux.getColumn()] = true;
			}
		}

		return mat;
	}

	@Override
	protected int[][] possibleDirections() {
		return new int[][] { { -2, -1 }, { -2, 1 }, { -1, -2 }, { -1, 2 }, { 1, -2 }, { 1, 2 }, { 2, -1 }, { 2, 1 } };
	}

}
