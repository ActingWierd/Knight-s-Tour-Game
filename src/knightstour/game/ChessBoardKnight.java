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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;




public class ChessBoardKnight extends Application {
    // Level selection fields
    private Level[] levels = {
        new Level("Easy (6x6)", 6, 10, 10, 5),
        new Level("Classic (8x8)", 8, 10, 10, 5),
        new Level("Hard (10x10)", 10, 15, 15, 10)
    };
    private Level currentLevel = levels[1]; // Default to Classic

    private static final int SQUARE_SIZE = 60;
    private static final int CONTROL_FONT_SIZE = 16;
    private static final int WIN_FONT_SIZE = 20;

    private Button[][] squares;
    private int[][] visitCount;
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

    private GridPane boardPane;
    private Stage mainStage;
    private Scene menuScene;
    private Scene gameScene;
    private VBox menuRoot;
    private VBox gameRoot;
    private javafx.scene.control.ComboBox<Level> menuLevelSelector;

    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;
        setupMenuScene();
        primaryStage.setTitle("Knight's Tour - Colorful Prototype");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    private void setupMenuScene() {
        Label title = new Label("Knight's Tour");
        title.setFont(Font.font("Impact", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#06D6A0"));
        Label selectLabel = new Label("Select Difficulty:");
        selectLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        menuLevelSelector = new javafx.scene.control.ComboBox<>(javafx.collections.FXCollections.observableArrayList(levels));
        menuLevelSelector.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Level item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name);
            }
        });
        menuLevelSelector.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Level item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name);
            }
        });
        menuLevelSelector.getSelectionModel().select(currentLevel);
        Button startButton = new Button("Start Game");
        startButton.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        startButton.setOnAction(e -> {
            currentLevel = menuLevelSelector.getValue();
            setupGameScene();
            mainStage.setScene(gameScene);
            resetGame();
        });
        VBox menuBox = new VBox(20, title, selectLabel, menuLevelSelector, startButton);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(40));
        menuRoot = menuBox;
        menuScene = new Scene(menuRoot, 700, 700);
    }

    private void setupGameScene() {
        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Verdana", FontWeight.BOLD, CONTROL_FONT_SIZE));
        moveCountLabel = new Label();
        moveCountLabel.setFont(Font.font("Verdana", FontWeight.BOLD, CONTROL_FONT_SIZE));
        winLabel = new Label("");
        winLabel.setFont(Font.font("Impact", FontWeight.BOLD, WIN_FONT_SIZE));
        winLabel.setStyle("-fx-text-fill: #06D6A0;");
        undoButton = new Button();
        undoButton.setFont(Font.font(CONTROL_FONT_SIZE));
        undoButton.setDisable(true);
        undoButton.setOnAction(e -> undoMove());
        Button backButton = new Button("Back to Menu");
        backButton.setFont(Font.font(CONTROL_FONT_SIZE));
        backButton.setOnAction(e -> mainStage.setScene(menuScene));
        HBox controls = new HBox(10, scoreLabel, moveCountLabel, undoButton, backButton);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(5));
        VBox root = new VBox(0, controls, winLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
        boardPane = createBoard();
        root.getChildren().add(boardPane);
        gameRoot = root;
        gameScene = new Scene(gameRoot, 700, 700);
    }

    private GridPane createBoard() {
        GridPane board = new GridPane();
        squares = new Button[currentLevel.boardSize][currentLevel.boardSize];
        for (int row = 0; row < currentLevel.boardSize; row++) {
            for (int col = 0; col < currentLevel.boardSize; col++) {
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
        String lightColor = "#bfcf8f"; // new light green
        String darkColor = "#769656"; // new dark green
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
            if (visitCount[row][col] == 0) score += currentLevel.pointsPerMove;
            else score -= currentLevel.revisitPenalty;
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
        moveCountLabel.setText(String.format("Squares Visited: %d/%d", unique, currentLevel.boardSize * currentLevel.boardSize));
        if (unique == currentLevel.boardSize * currentLevel.boardSize) {
            winLabel.setText("Congratulations! You won!");
            undoButton.setDisable(true);
        }
    }

    private Label createKnightLabel(int visitNumber) {
        Label label = new Label("\u2658\n" + visitNumber);
        label.setFont(Font.font("Segoe UI Symbol", 36));
        label.setTextFill(Color.web("#EF476F"));
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
        score -= currentLevel.undoPenalty;
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
        moveCountLabel.setText(String.format("Squares Visited: %d/%d", countUniqueVisits(), currentLevel.boardSize * currentLevel.boardSize));
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
        return row >= 0 && row < currentLevel.boardSize && col >= 0 && col < currentLevel.boardSize;
    }

    private void clearHighlights() {
        for (int row = 0; row < currentLevel.boardSize; row++)
            for (int col = 0; col < currentLevel.boardSize; col++)
                squares[row][col].setStyle(getSquareStyle(row, col, false));
    }

    private void resetGame() {
        knightRow = -1;
        knightCol = -1;
        score = 0;
        moveHistory.clear();
        visitCount = new int[currentLevel.boardSize][currentLevel.boardSize];
        winLabel.setText("");
        scoreLabel.setText("Score: 0");
        moveCountLabel.setText(String.format("Squares Visited: 0/%d", currentLevel.boardSize * currentLevel.boardSize));
        undoButton.setText("Undo (-" + currentLevel.undoPenalty + " pts)");
        undoButton.setDisable(true);
        if (gameRoot != null) {
            int boardIndex = gameRoot.getChildren().indexOf(boardPane);
            boardPane = createBoard();
            if (boardIndex != -1) {
                gameRoot.getChildren().set(boardIndex, boardPane);
            } else {
                gameRoot.getChildren().add(boardPane);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
