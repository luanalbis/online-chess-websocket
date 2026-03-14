package com.luanalbischess.helpers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.luanalbischess.domain.Board;
import com.luanalbischess.domain.Match;
import com.luanalbischess.domain.Move;
import com.luanalbischess.domain.Piece;
import com.luanalbischess.domain.Position;
import com.luanalbischess.domain.chesspieces.Bishop;
import com.luanalbischess.domain.chesspieces.King;
import com.luanalbischess.domain.chesspieces.Knight;
import com.luanalbischess.domain.chesspieces.Pawn;
import com.luanalbischess.domain.chesspieces.Queen;
import com.luanalbischess.domain.chesspieces.Rook;
import com.luanalbischess.domain.enums.PieceColor;
import com.luanalbischess.domain.enums.PieceType;

@Component
public class FenConverter {

	public String toFen(Match match) {
		String board = toFenBoard(match.getBoard().getMatPieces());
		String turn = toFenTurn(match.getCurrentColorTurn());
		String castlingRight = toFenCastlingRights(match.getCastlingRights());
		String enPassantPosition = toFenEnPassantTarget(match.getEnPassantPositionTarget());
		String halfMoveClock = match.getHalfMoveClock().toString();
		String fullMove = match.getFullMove().toString();

		return String.join(" ",
				board,
				turn,
				castlingRight,
				enPassantPosition,
				halfMoveClock,
				fullMove);
	}

	public Match fenToMatch(String fen) {
		return Match.builder()
				.board(fenToBoard(fen))
				.currentColorTurn(fenToColorTurn(fen))
				.castlingRights(fenToCastlingRights(fen))
				.enPassantPositionTarget(fenToEnPassantTarget(fen))
				.halfMoveClock(fenToHalfMoveClock(fen))
				.fullMove(fenToFullMoveNumber(fen))
				.build();
	}

	public Board fenToBoard(String fen) {
		Piece[][] matPieces = new Piece[8][8];
		String[] rows = extractFenStringBoard(fen).split("/");

		for (int i = 0; i < 8; i++) {
			String expandedRow = expandFenBoardRow(rows[i]);
			for (int j = 0; j < 8; j++) {
				char type = expandedRow.charAt(j);
				Piece piece = toPiece(type);
				if (piece != null) {
					piece.setPosition(new Position(i, j));
					piece.setPieceColor(toPieceColor(type));
					piece.setPieceType(toPieceType(type));
				}
				matPieces[i][j] = piece;
			}
		}

		Board board = Board.builder().matPieces(matPieces).build();
		board.getPiecesOnBoard().forEach(p -> p.setBoard(board));

		return board;
	}

	public PieceColor fenToColorTurn(String fen) {
		char turn = extractFenCurrentPlayer(fen);
		return turn == 'w' ? PieceColor.WHITE : PieceColor.BLACK;
	}

	private Map<String, Boolean> fenToCastlingRights(String fen) {
		Map<String, Boolean> castlingRights = new HashMap<>();
		String fenCastlingRights = extractFenCastlingRights(fen);

		castlingRights.put("K", fenCastlingRights.contains("K"));
		castlingRights.put("k", fenCastlingRights.contains("k"));
		castlingRights.put("Q", fenCastlingRights.contains("Q"));
		castlingRights.put("q", fenCastlingRights.contains("q"));

		return castlingRights;
	}

	public int fenToHalfMoveClock(String fen) {
		return extractFenHalfMoveClock(fen);
	}

	public int fenToFullMoveNumber(String fen) {
		return extractFenFullMoveNumber(fen);
	}

	public boolean fenToCanWhiteKingSideCastle(String fen) {
		return extractFenCastlingRights(fen).contains("K");
	}

	public boolean fenToCanWhiteQueenSideCastle(String fen) {
		return extractFenCastlingRights(fen).contains("Q");
	}

	public boolean fenToCanBlackKingSideCastle(String fen) {
		return extractFenCastlingRights(fen).contains("k");
	}

	public boolean fenToCanBlackQueenSideCastle(String fen) {
		return extractFenCastlingRights(fen).contains("q");
	}

	public Position fenToEnPassantTarget(String fen) {
		return toPosition(extractFenEnPassantTarget(fen));
	}

	public String toFenBoard(Piece[][] board) {
		StringBuilder sb = new StringBuilder();

		for (int row = 0; row < 8; row++) {
			int empty = 0;
			for (int col = 0; col < 8; col++) {
				Piece p = board[row][col];
				if (p == null) {
					empty++;
				} else {
					if (empty > 0) {
						sb.append(empty);
						empty = 0;
					}
					sb.append(toFenPiece(p));
				}
			}
			if (empty > 0) {
				sb.append(empty);
			}

			if (row < 7) {
				sb.append('/');
			}
		}

		return sb.toString();
	}

	public char toFenPiece(Piece piece) {
		char c = switch (piece.getPieceType()) {
		case PAWN -> 'p';
		case ROOK -> 'r';
		case KNIGHT -> 'n';
		case BISHOP -> 'b';
		case QUEEN -> 'q';
		case KING -> 'k';
		default -> ' ';
		};
		return piece.getPieceColor() == PieceColor.WHITE ? Character.toUpperCase(c) : c;
	}

	private String toFenTurn(PieceColor currentColorTurn) {
		return currentColorTurn == PieceColor.WHITE ? "w" : "b";
	}

	public String toFenCastlingRights(Map<String, Boolean> castlingRight) {
		StringBuilder sb = new StringBuilder();

		if (Boolean.TRUE.equals(castlingRight.get("K")))
			sb.append('K');
		if (Boolean.TRUE.equals(castlingRight.get("Q")))
			sb.append('Q');
		if (Boolean.TRUE.equals(castlingRight.get("k")))
			sb.append('k');
		if (Boolean.TRUE.equals(castlingRight.get("q")))
			sb.append('q');

		return sb.isEmpty() ? "-" : sb.toString();
	}

	public String toFenPosition(Position pos) {
		if (pos == null)
			return "-";
		char col = (char) ('a' + pos.getColumn());
		int row = 8 - pos.getRow();
		return "" + col + row;
	}

	public String toFenEnPassantTarget(Position pos) {
		return toFenPosition(pos);
	}

	private Piece toPiece(char c) {
		if (c == '0')
			return null;

		Piece piece = switch (c) {
		case 'P', 'p' -> new Pawn();
		case 'R', 'r' -> new Rook();
		case 'N', 'n' -> new Knight();
		case 'B', 'b' -> new Bishop();
		case 'Q', 'q' -> new Queen();
		case 'K', 'k' -> new King();
		default -> null;
		};

		if (piece == null)
			return null;

		return piece;
	}

	private PieceType toPieceType(char c) {
		return switch (Character.toLowerCase(c)) {
		case 'p' -> PieceType.PAWN;
		case 'r' -> PieceType.ROOK;
		case 'n' -> PieceType.KNIGHT;
		case 'b' -> PieceType.BISHOP;
		case 'q' -> PieceType.QUEEN;
		case 'k' -> PieceType.KING;
		default -> null;
		};
	}

	public PieceColor toPieceColor(char c) {
		return Character.isUpperCase(c) ? PieceColor.WHITE : PieceColor.BLACK;
	}

	public Position toPosition(String fenPos) {
		if (fenPos == null || fenPos.equals("-"))
			return null;
		int col = fenPos.charAt(0) - 'a';
		int row = 8 - Character.getNumericValue(fenPos.charAt(1));
		return new Position(row, col);
	}

	public String moveToUci(Move move) {
		return toFenPosition(move.getSource()) + toFenPosition(move.getTarget());
	}

	public Move uciToMove(String uci) {
		if (uci.length() != 4) {
			throw new RuntimeException("Uci precisa ter 4 letras");
		}

		return new Move(toPosition(uci.substring(0, 2)), toPosition(uci.substring(2, 4)));
	}

	private String expandFenBoardRow(String row) {
		StringBuilder expanded = new StringBuilder();
		for (char c : row.toCharArray()) {
			expanded.append(Character.isDigit(c) ? "0".repeat(c - '0') : c);
		}
		return expanded.toString();
	}

	private String extractFenStringBoard(String fen) {
		return fen.split(" ")[0];
	}

	private char extractFenCurrentPlayer(String fen) {
		return fen.split(" ")[1].charAt(0);
	}

	private String extractFenCastlingRights(String fen) {
		return fen.split(" ")[2];
	}

	private String extractFenEnPassantTarget(String fen) {
		return fen.split(" ")[3];
	}

	private int extractFenHalfMoveClock(String fen) {
		return Integer.parseInt(fen.split(" ")[4]);
	}

	private int extractFenFullMoveNumber(String fen) {
		return Integer.parseInt(fen.split(" ")[5]);
	}

	public String toFenMove(Move move) {
		String fenSource = toFenPosition(move.getSource());
		String fenTarget = toFenPosition(move.getTarget());
		return fenSource + fenTarget;
	}
}
