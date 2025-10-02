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
import java.util.Stack;

public class ChessBoardKnight extends Application {
    private static final int BOARD_SIZE = 8;
    private static final int SQUARE_SIZE = 60;
    private static final int KNIGHT_SIZE = 30;
    private static final int POINTS_PER_MOVE = 10;
    private static final int UNDO_PENALTY = 5;
    private static final int CONTROL_FONT_SIZE = 14;
    private static final int WIN_FONT_SIZE = 18;

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
        // Create score display with smaller font
        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font(CONTROL_FONT_SIZE));
        scoreLabel.setStyle("-fx-padding: 2;");

        // Create move count display with smaller font
        moveCountLabel = new Label("Squares Visited: 0/64");
        moveCountLabel.setFont(Font.font(CONTROL_FONT_SIZE));
        moveCountLabel.setStyle("-fx-padding: 2;");

        // Create win message label with reduced size
        winLabel = new Label("");
        winLabel.setFont(Font.font("System", FontWeight.BOLD, WIN_FONT_SIZE));
        winLabel.setStyle("-fx-text-fill: green; -fx-padding: 2;");

        // Create compact undo button
        undoButton = new Button("Undo (-" + UNDO_PENALTY + " pts)");
        undoButton.setFont(Font.font(CONTROL_FONT_SIZE));
        undoButton.setDisable(true);
        undoButton.setOnAction(e -> undoMove());

        // Create the chess board
        GridPane board = createBoard();

        // Create compact control panel
        HBox controls = new HBox(5);
        controls.getChildren().addAll(scoreLabel, moveCountLabel, undoButton);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(2, 0, 2, 0));

        // Create layout with reduced spacing
        VBox root = new VBox(5);
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(controls, winLabel, board);
        root.setPadding(new Insets(5));

        Scene scene = new Scene(root);
        primaryStage.setTitle("Knight's Tour");
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
        square.setFont(Font.font(KNIGHT_SIZE));

        square.setOnAction(e -> handleSquareClick(row, col));
        return square;
    }

    private String getSquareStyle(int row, int col, boolean isLegalMove) {
        String baseColor = (row + col) % 2 == 0 ? "#FFFFFF" : "#A9A9A9";
        String highlightColor = isLegalMove ? "-fx-border-color: green; -fx-border-width: 3px;" : "";
        return "-fx-background-color: " + baseColor + ";" + highlightColor;
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
            score += POINTS_PER_MOVE;
            scoreLabel.setText("Score: " + score);

            placeKnight(row, col);
            updateVisitCount(row, col);
            moveHistory.push(new Move(row, col, score));
            undoButton.setDisable(false);
        }
    }

    private void updateVisitCount(int row, int col) {
        visitCount[row][col]++;
        squares[row][col].setText("♞\n" + visitCount[row][col]);

        int uniqueSquaresVisited = countUniqueVisits();
        moveCountLabel.setText(String.format("Squares Visited: %d/64", uniqueSquaresVisited));

        // Check for win condition
        if (uniqueSquaresVisited == 64) {
            winLabel.setText("Good Job, you have won the game!");
            undoButton.setDisable(true);
        }
    }

    private int countUniqueVisits() {
        int count = 0;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (visitCount[row][col] > 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private void placeKnight(int row, int col) {
        if (knightRow != -1) {
            String visitText = visitCount[knightRow][knightCol] > 0 ?
                    String.valueOf(visitCount[knightRow][knightCol]) : "";
            squares[knightRow][knightCol].setText(visitText);
        }

        knightRow = row;
        knightCol = col;
        clearHighlights();
        highlightLegalMoves();
    }

    private void undoMove() {
        if (moveHistory.isEmpty()) return;

        Move lastMove = moveHistory.pop();
        visitCount[knightRow][knightCol]--;

        score -= UNDO_PENALTY;

        if (moveHistory.isEmpty()) {
            knightRow = -1;
            knightCol = -1;
            squares[lastMove.row][lastMove.col].setText("");
            undoButton.setDisable(true);
        } else {
            Move previousMove = moveHistory.peek();
            knightRow = previousMove.row;
            knightCol = previousMove.col;

            squares[lastMove.row][lastMove.col].setText(
                    visitCount[lastMove.row][lastMove.col] > 0 ?
                            String.valueOf(visitCount[lastMove.row][lastMove.col]) : "");
            squares[knightRow][knightCol].setText("♞\n" + visitCount[knightRow][knightCol]);
        }

        scoreLabel.setText("Score: " + score);
        moveCountLabel.setText(String.format("Squares Visited: %d/64", countUniqueVisits()));
        winLabel.setText("");
        clearHighlights();
        highlightLegalMoves();
    }

    private boolean isLegalMove(int row, int col) {
        for (int i = 0; i < 8; i++) {
            if (knightRow + ROW_MOVES[i] == row && knightCol + COL_MOVES[i] == col) {
                return true;
            }
        }
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
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                squares[row][col].setStyle(getSquareStyle(row, col, false));
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}