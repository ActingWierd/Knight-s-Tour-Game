package knightstour.game;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.layout.StackPane;
import java.util.Stack;

public class ChessBoardKnight extends Application {
    private static final int BOARD_SIZE = 8;
    private static final int SQUARE_SIZE = 60;
    private static final int POINTS_PER_MOVE = 10;
    private static final int REVISIT_PENALTY = 10;
    private static final int UNDO_PENALTY = 5;
    private static final int CONTROL_FONT_SIZE = 16;
    private static final int WIN_FONT_SIZE = 20;

    private Button[][] squares = new Button[BOARD_SIZE][BOARD_SIZE];
    private int[][] visitCount = new int[BOARD_SIZE][BOARD_SIZE];
    private int knightRow = -1;
    private int knightCol = -1;
    private int score = 0;
    private Label scoreLabel;
    private Label moveCountLabel;
    private Label winLabel;
    private Button undoButton;
    private Stack<Move> moveHistory = new Stack<>();

    private static final int[] ROW_MOVES = {-2, -2, -1, -1, 1, 1, 2, 2};
    private static final int[] COL_MOVES = {-1, 1, -2, 2, -2, 2, -1, 1};

    private class Move {
        int row, col, score;
        Move(int row, int col, int score) {
            this.row = row;
            this.col = col;
            this.score = score;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Verdana", FontWeight.BOLD, CONTROL_FONT_SIZE));

        moveCountLabel = new Label("Squares Visited: 0/64");
        moveCountLabel.setFont(Font.font("Verdana", FontWeight.BOLD, CONTROL_FONT_SIZE));

        winLabel = new Label("");
        winLabel.setFont(Font.font("Impact", FontWeight.BOLD, WIN_FONT_SIZE));
        winLabel.setStyle("-fx-text-fill: #06D6A0;");

        undoButton = new Button("Undo (-" + UNDO_PENALTY + " pts)");
        undoButton.setFont(Font.font(CONTROL_FONT_SIZE));
        undoButton.setDisable(true);
        undoButton.setOnAction(e -> undoMove());

        GridPane board = createBoard();

        HBox controls = new HBox(10, scoreLabel, moveCountLabel, undoButton);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(5));

        VBox root = new VBox(10, controls, winLabel, board);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root);
        primaryStage.setTitle("Knight's Tour - Colorful Prototype");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private GridPane createBoard() {
        GridPane board = new GridPane();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Button square = createSquare(row, col);
                squares[row][col] = square;
                board.add(square, col, row);
            }
        }
        return board;
    }

    private Button createSquare(int row, int col) {
        Button square = new Button();
        square.setPrefSize(SQUARE_SIZE, SQUARE_SIZE);
        square.setStyle(getSquareStyle(row, col, false));

        square.setOnAction(e -> handleSquareClick(row, col));
        return square;
    }

    private String getSquareStyle(int row, int col, boolean highlight) {
        // Soft, complementary colors for non-classic board
        String lightColor = "#80ED99"; // light green
        String darkColor = "#3D8361";  // darker green
        String baseColor = (row + col) % 2 == 0 ? lightColor : darkColor;
        String border = highlight ? "-fx-border-color: #FFD166; -fx-border-width: 3px;" : "";
        return "-fx-background-color: " + baseColor + "; -fx-background-radius: 8px; " + border;
    }

    private void handleSquareClick(int row, int col) {
        if (knightRow == -1) {
            placeKnight(row, col);
            updateVisitCount(row, col);
            moveHistory.push(new Move(row, col, score));
            undoButton.setDisable(false);
            return;
        }

        if (isLegalMove(row, col)) {
            if (visitCount[row][col] == 0) score += POINTS_PER_MOVE;
            else score -= REVISIT_PENALTY;

            scoreLabel.setText("Score: " + score);

            placeKnight(row, col);
            updateVisitCount(row, col);
            moveHistory.push(new Move(row, col, score));
            undoButton.setDisable(false);
        }
    }

    private void updateVisitCount(int row, int col) {
        visitCount[row][col]++;
        squares[row][col].setGraphic(createKnightLabel(visitCount[row][col]));

        int unique = countUniqueVisits();
        moveCountLabel.setText(String.format("Squares Visited: %d/64", unique));

        if (unique == 64) {
            winLabel.setText("Congratulations! You won!");
            undoButton.setDisable(true);
        }
    }

    private Label createKnightLabel(int visitNumber) {
        Label label = new Label("\u2658\n" + visitNumber); // White knight Unicode
        label.setFont(Font.font("Segoe UI Symbol", 36));
        label.setTextFill(Color.web("#EF476F")); // pink
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private int countUniqueVisits() {
        int count = 0;
        for (int[] rows : visitCount) {
            for (int val : rows) if (val > 0) count++;
        }
        return count;
    }

    private void placeKnight(int row, int col) {
        if (knightRow != -1) {
            squares[knightRow][knightCol].setGraphic(
                    visitCount[knightRow][knightCol] > 0 ? createVisitLabel(visitCount[knightRow][knightCol]) : null
            );
        }
        knightRow = row;
        knightCol = col;
        clearHighlights();
        highlightLegalMoves();
        squares[knightRow][knightCol].setGraphic(createKnightLabel(visitCount[knightRow][knightCol]));
    }

    private Label createVisitLabel(int number) {
        Label label = new Label(String.valueOf(number));
        label.setFont(Font.font(18));
        label.setTextFill(Color.WHITE);
        return label;
    }

    private void undoMove() {
        if (moveHistory.isEmpty()) return;
        Move lastMove = moveHistory.pop();
        visitCount[knightRow][knightCol]--;
        score -= UNDO_PENALTY;

        if (moveHistory.isEmpty()) {
            knightRow = -1;
            knightCol = -1;
            squares[lastMove.row][lastMove.col].setGraphic(null);
            undoButton.setDisable(true);
        } else {
            Move prev = moveHistory.peek();
            knightRow = prev.row;
            knightCol = prev.col;
            squares[lastMove.row][lastMove.col].setGraphic(
                    visitCount[lastMove.row][lastMove.col] > 0 ? createVisitLabel(visitCount[lastMove.row][lastMove.col]) : null
            );
            squares[knightRow][knightCol].setGraphic(createKnightLabel(visitCount[knightRow][knightCol]));
        }

        scoreLabel.setText("Score: " + score);
        moveCountLabel.setText(String.format("Squares Visited: %d/64", countUniqueVisits()));
        winLabel.setText("");
        clearHighlights();
        highlightLegalMoves();
    }

    private boolean isLegalMove(int row, int col) {
        for (int i = 0; i < 8; i++)
            if (knightRow + ROW_MOVES[i] == row && knightCol + COL_MOVES[i] == col) return true;
        return false;
    }

    private void highlightLegalMoves() {
        for (int i = 0; i < 8; i++) {
            int newRow = knightRow + ROW_MOVES[i];
            int newCol = knightCol + COL_MOVES[i];
            if (isValidPosition(newRow, newCol)) {
                squares[newRow][newCol].setStyle(getSquareStyle(newRow, newCol, true));
            }
        }
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private void clearHighlights() {
        for (int row = 0; row < BOARD_SIZE; row++)
            for (int col = 0; col < BOARD_SIZE; col++)
                squares[row][col].setStyle(getSquareStyle(row, col, false));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
