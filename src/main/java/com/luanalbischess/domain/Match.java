package com.luanalbischess.domain;

import java.util.Map;
import java.util.Set;

import com.luanalbischess.domain.chesspieces.Bishop;
import com.luanalbischess.domain.chesspieces.King;
import com.luanalbischess.domain.chesspieces.Knight;
import com.luanalbischess.domain.chesspieces.Pawn;
import com.luanalbischess.domain.chesspieces.Queen;
import com.luanalbischess.domain.chesspieces.Rook;
import com.luanalbischess.domain.enums.MatchStatus;
import com.luanalbischess.domain.enums.PieceColor;
import com.luanalbischess.domain.enums.PieceType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match {

	private Board board;
	private PieceColor currentColorTurn;
	private Position enPassantPositionTarget;
	private Map<String, Boolean> castlingRights;
	private Integer halfMoveClock;
	private Integer fullMove;
	private MatchStatus matchStatus;
	private PieceColor winnerColor;

	public Piece movePiece(Position source, Position target) {
		Piece moving = board.getPieceByPosition(source);

		if (isACastlingMove(moving.getPieceType(), source, target)) {
			handleCastlingMove(source, target);
			return null;
		}

		Piece captured = getCapturedPiece(source, target);

		if (captured != null) {
			board.removePieceWithPosition(captured.getPosition());
		}

		board.removePieceWithPosition(source);

		moving.setPosition(target);
		board.placePiece(moving);

		return captured;
	}

	public void undoMove(Position source, Position target, Piece captured) {

		if (isACastlingMove(board.getPieceByPosition(target).getPieceType(), source, target)) {
			handleCastlingUndoMove(source, target);
			return;
		}

		Piece moved = board.removePieceWithPosition(target);

		moved.setPosition(source);
		board.placePiece(moved);

		if (captured != null) {
			board.placePiece(captured);
		}
	}

	public boolean isInCheck(PieceColor color) {
		Position kingPosition = getKingByColor(color).getPosition();

		return board.getPiecesOnBoardByColor(getOpponentColor(color))
				.stream()
				.map(Piece::possibleMoves)
				.anyMatch(mat -> mat[kingPosition.getRow()][kingPosition.getColumn()]);
	}

	public boolean isCheckMate(PieceColor color) {
		if (!isInCheck(color))
			return false;

		for (Piece piece : board.getPiecesOnBoardByColor(color)) {
			if (canPieceFreeKingInCheck(piece)) {
				return false;
			}
		}

		return true;
	}

	private boolean canPieceFreeKingInCheck(Piece piece) {
		boolean[][] possibleMoves = piece.possibleMoves();

		if (piece instanceof Pawn && enPassantPositionTarget != null) {
			Piece vulnerable = getEnPassantVulnerable();

			if (vulnerable.getPieceColor() != piece.getPieceColor()) {
				boolean canEnPassant = piece.getPosition().getRow() == vulnerable.getPosition().getRow()
						&& Math.abs(piece.getPosition().getColumn() - vulnerable.getPosition().getColumn()) == 1;
				possibleMoves[enPassantPositionTarget.getRow()][enPassantPositionTarget.getColumn()] = canEnPassant;
			}
		}

		for (int row = 0; row < Board.ROWS; row++) {
			for (int col = 0; col < Board.COLUMNS; col++) {
				if (possibleMoves[row][col]) {
					Position source = piece.getPosition();
					Position target = new Position(row, col);

					Piece captured = movePiece(source, target);
					boolean check = isInCheck(piece.getPieceColor());
					undoMove(source, target, captured);

					if (!check) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private Piece getCapturedPiece(Position source, Position target) {
		if (isAEnPassantMove(source, target)) {
			return getEnPassantVulnerable();
		}
		return board.getPieceByPosition(target);
	}

	private boolean isACastlingMove(PieceType type, Position source, Position target) {
		if (type != PieceType.KING) {
			return false;
		}
		return source.getColumn() == target.getColumn() + 2 || source.getColumn() == target.getColumn() - 2;
	}

	private void handleCastlingMove(Position source, Position target) {
		int rookRow = source.getRow();
		int sourceRookCol = source.getColumn() > target.getColumn() ? 0 : 7;
		int targetRookCol = (sourceRookCol == 0) ? 3 : 5;

		Position rookSource = new Position(rookRow, sourceRookCol);
		Position rookTarget = new Position(rookRow, targetRookCol);

		Piece rook = board.removePieceWithPosition(rookSource);
		Piece king = board.removePieceWithPosition(source);

		king.setPosition(target);
		rook.setPosition(rookTarget);

		board.placePiece(king);
		board.placePiece(rook);

	}

	private void handleCastlingUndoMove(Position source, Position target) {

		int rookRow = source.getRow();
		int sourceRookCol = source.getColumn() > target.getColumn() ? 0 : 7;
		int targetRookCol = (sourceRookCol == 0) ? 3 : 5;

		Position rookSource = new Position(rookRow, sourceRookCol);
		Position rookTarget = new Position(rookRow, targetRookCol);

		Piece rook = board.removePieceWithPosition(rookTarget);
		Piece king = board.removePieceWithPosition(target);

		king.setPosition(source);
		rook.setPosition(rookSource);

		board.placePiece(king);
		board.placePiece(rook);
	}

	public Piece getKingByColor(PieceColor color) {
		return board.getPiecesOnBoard()
				.stream()
				.filter(p -> p instanceof King && p.getPieceColor() == color)
				.findFirst()
				.orElseThrow(() -> new RuntimeException("There is no " + color + " King on the board"));
	}

	private boolean isAEnPassantMove(Position source, Position target) {
		if (source.getColumn() == target.getColumn()) {
			return false;
		}
		Piece movingPiece = board.getPieceByPosition(source);
		return (movingPiece instanceof Pawn) && (board.getPieceByPosition(target) == null);
	}

	public Piece getEnPassantVulnerable() {
		if (enPassantPositionTarget == null)
			return null;
		int pawnRow = enPassantPositionTarget.getRow() == 2 ? 3 : 4;
		int pawnCol = enPassantPositionTarget.getColumn();
		return board.getPieceByPosition(new Position(pawnRow, pawnCol));
	}

	public PieceColor getOpponentColor(PieceColor color) {
		return color == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK;
	}

	public void promotePawn(Position target, PieceType chosenType) {
		Set<PieceType> allowedTypes = Set.of(PieceType.QUEEN, PieceType.BISHOP, PieceType.ROOK, PieceType.KNIGHT);
		if (!allowedTypes.contains(chosenType)) {
			throw new RuntimeException(
					"You choose a not allowed type for promotion. Types allowed: QUEEN,ROOK,BISHOP,NIGHT! Chosen: " + chosenType);
		}

		Piece promotedPawn = board.getPieceByPosition(target);
		Piece newPromotedInstance = switch (chosenType) {
		case QUEEN -> new Queen();
		case BISHOP -> new Bishop();
		case ROOK -> new Rook();
		case KNIGHT -> new Knight();
		default -> null;
		};

		newPromotedInstance.setBoard(promotedPawn.getBoard());
		newPromotedInstance.setPieceType(chosenType);
		newPromotedInstance.setPieceColor(promotedPawn.getPieceColor());
		newPromotedInstance.setPosition(promotedPawn.getPosition());

		board.removePieceWithPosition(promotedPawn.getPosition());
		board.placePiece(newPromotedInstance);

	}
}
