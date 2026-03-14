package com.luanalbischess.domain;

import java.util.ArrayList;
import java.util.List;

import com.luanalbischess.domain.enums.PieceColor;
import com.luanalbischess.domain.enums.PieceType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Board {
	public static final Integer ROWS = 8;
	public static final Integer COLUMNS = 8;

	Piece[][] matPieces;

	public Piece getPieceByPosition(int row, int col) {
		return matPieces[row][col];
	}

	public Piece getPieceByPosition(Position p) {
		return getPieceByPosition(p.getRow(), p.getColumn());
	}

	public boolean thereIsAPiece(int row, int col) {
		return getPieceByPosition(row, col) != null;
	}

	public boolean thereIsAPiece(Position p) {
		return thereIsAPiece(p.getRow(), p.getColumn());
	}

	public boolean positionExists(Position p) {
		int row = p.getRow();
		int col = p.getColumn();

		return row >= 0 && row < 8 && col >= 0 && col < 8;
	}

	public List<Piece> getPiecesOnBoard() {
		List<Piece> pieces = new ArrayList<>();

		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				if (matPieces[row][col] != null) {
					pieces.add(matPieces[row][col]);
				}
			}
		}

		return pieces;
	}

	public void placePiece(Piece piece) {
		var pos = piece.getPosition();
		if (thereIsAPiece(new Position(pos.getRow(), pos.getColumn())))
			throw new RuntimeException("There is already a piece on position");

		this.matPieces[pos.getRow()][pos.getColumn()] = piece;
	}

	public Piece removePieceWithPosition(Position position) {
		if (!this.positionExists(position))
			throw new RuntimeException("Position not on the board");

		Piece removed = this.getPieceByPosition(position);
		this.matPieces[position.getRow()][position.getColumn()] = null;
		return removed;
	}

	public List<Piece> getPiecesOnBoardByColor(PieceColor color) {
		return getPiecesOnBoard().stream()
				.filter(p -> (p.getPieceColor()) == color)
				.toList();
	}

	public List<Piece> getPiecesOnBoardByColorAndType(PieceColor color, PieceType type) {
		return getPiecesOnBoard().stream()
				.filter(p -> (p.getPieceColor()) == color && p.getPieceType() == type)
				.toList();
	}

	public void printBoard() {
		System.out.println("=== TABULEIRO ===");
		for (int row = 0; row < Board.ROWS; row++) {
			for (int col = 0; col < Board.COLUMNS; col++) {
				Piece piece = getPieceByPosition(new Position(row, col));
				if (piece == null) {
					System.out.print(". ");
				} else {
					String pieceChar = piece.getClass().getSimpleName().substring(0, 1);
					String colorChar = piece.getPieceColor().toString().substring(0, 1);
					System.out.print(pieceChar + colorChar + " ");
				}
			}
			System.out.println();
		}
		System.out.println("=================");
	}

	
}
