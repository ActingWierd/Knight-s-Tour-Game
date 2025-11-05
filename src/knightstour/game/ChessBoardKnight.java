package knightstour.game;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Knight's Tour Game (JavaFX)
 * - Keeps existing gameplay style: click to place/move the knight, legal knight moves enforced,
 *   scoring for new squares, penalties for revisits and undo, win when all squares are visited.
 * - New menu UX: glass card over background image, level picker with live details, Start/Tutorial/Quit.
 *
 * NOTE: Place menu_background.png here for best results:
 *   src/main/resources/images/menu_background.png
 */
public class ChessBoardKnight extends Application {

    // -------------------------
    // Levels (unchanged model)
    // -------------------------
    private final Level[] levels = {
            new Level("Easy (6x6)", 6, 10, 10, 5),
            new Level("Classic (8x8)", 8, 10, 10, 5),
            new Level("Hard (10x10)", 10, 15, 15, 10)
    };
    private Level currentLevel = levels[1]; // default Classic

    // -------------------------
    // Game configuration
    // -------------------------
    private static final int SQUARE_SIZE = 60;
    private static final Color LIGHT_COLOR = Color.web("#E5E7EB"); // light gray
    private static final Color DARK_COLOR  = Color.web("#9CA3AF"); // darker gray
    private static final Color MOVE_HIGHLIGHT = Color.web("#34D399"); // green
    private static final Color VISITED_COLOR  = Color.web("#6366F1"); // indigo
    private static final Color START_COLOR    = Color.web("#F59E0B"); // amber

    // -------------------------
    // UI fields
    // -------------------------
    private Stage mainStage;
    private Scene menuScene;
    private Scene gameScene;

    private VBox gameRoot;         // contains controls + board
    private GridPane boardPane;    // the board grid
    private Button[][] squares;    // buttons per square

    private Label scoreLabel;
    private Label moveCountLabel;
    private Label winLabel;
    private Button undoButton;

    // Menu fields
    private ComboBox<Level> menuLevelSelector;
    private Label levelDetails;

    // -------------------------
    // Game state
    // -------------------------
    private int score = 0;
    private int moveCount = 0;

    private int boardSize;               // convenience from currentLevel
    private boolean[][] visited;         // visited squares
    private int currentRow = -1;         // knight position
    private int currentCol = -1;
    private final Stack<int[]> moveStack = new Stack<>(); // history of moves

    // Knight move deltas
    private static final int[][] KNIGHT_DELTAS = {
            {-2, -1}, {-2, 1},
            {-1, -2}, {-1, 2},
            {1, -2},  {1, 2},
            {2, -1},  {2, 1}
    };

    // =========================================================
    // App start
    // =========================================================
    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;
        buildMenuScene(mainStage);          // new UX menu
        setupGameScene();                   // game scene scaffold (board created on reset)
        mainStage.setTitle("Knight's Tour");
        mainStage.setScene(menuScene);
        mainStage.show();
    }

    // =========================================================
    // MENU (Redesigned UX)
    // =========================================================
    private void buildMenuScene(Stage stage) {
        StackPane root = new StackPane();

        // Background image
        Image bg = loadMenuBackground();
        ImageView bgView = new ImageView(bg);
        bgView.setPreserveRatio(true);
        bgView.setFitWidth(1280);
        bgView.setFitHeight(800);

        // Gradient overlay to improve text contrast
        Rectangle gradient = new Rectangle();
        gradient.widthProperty().bind(root.widthProperty());
        gradient.heightProperty().bind(root.heightProperty());
        gradient.setFill(new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.color(0,0,0,0.25)),
                new Stop(0.6, Color.color(0,0,0,0.45)),
                new Stop(1.0, Color.color(0,0,0,0.55))
        ));

        // Glass card container
        VBox card = new VBox(16);
        card.setMaxWidth(520);
        card.setMinWidth(360);
        card.setPadding(new Insets(28));
        card.setAlignment(Pos.CENTER);

        Rectangle glass = new Rectangle();
        glass.setArcWidth(28);
        glass.setArcHeight(28);
        glass.widthProperty().bind(card.widthProperty().add(32));
        glass.heightProperty().bind(card.heightProperty().add(32));
        glass.setFill(Color.rgb(255,255,255,0.12));
        glass.setStroke(Color.rgb(255,255,255,0.24));
        glass.setEffect(new BoxBlur(12, 12, 3));

        DropShadow shadow = new DropShadow();
        shadow.setRadius(24);
        shadow.setSpread(0.06);
        shadow.setColor(Color.rgb(0,0,0,0.45));

        StackPane cardHolder = new StackPane(glass, card);
        cardHolder.setEffect(shadow);
        cardHolder.setPickOnBounds(false);

        // Title & subtitle
        Label title = new Label("Knight’s Tour");
        // If "Cinzel" isn't packaged, JavaFX will fall back automatically.
        title.setFont(Font.font("Cinzel", FontWeight.EXTRA_BOLD, 42));
        title.setTextFill(Color.WHITE);
        title.setTextAlignment(TextAlignment.CENTER);

        Label subtitle = new Label("Visit every square exactly once with the lone knight.");
        subtitle.setFont(Font.font("Verdana", 14));
        subtitle.setTextFill(Color.rgb(255,255,255, 0.92));
        subtitle.setWrapText(true);
        subtitle.setTextAlignment(TextAlignment.CENTER);

        Region spacer1 = new Region();
        spacer1.setMinHeight(8);

        // Level selection
        Label selectLabel = new Label("Choose difficulty");
        selectLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        selectLabel.setTextFill(Color.web("#E5E7EB"));

        menuLevelSelector = new ComboBox<>(FXCollections.observableArrayList(levels));
        menuLevelSelector.setMaxWidth(Double.MAX_VALUE);
        menuLevelSelector.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Level item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name);
            }
        });
        menuLevelSelector.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Level item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name);
            }
        });
        menuLevelSelector.getSelectionModel().select(currentLevel);

        Tooltip.install(menuLevelSelector, new Tooltip(
                "Pick a board size and scoring rules. You can change this before starting."
        ));

        levelDetails = new Label();
        levelDetails.setFont(Font.font("Verdana", 12));
        levelDetails.setTextFill(Color.web("#F3F4F6"));
        levelDetails.setWrapText(true);
        levelDetails.setTextAlignment(TextAlignment.CENTER);
        levelDetails.setStyle("-fx-opacity: 0.92;");
        updateLevelDetails(menuLevelSelector.getValue());
        menuLevelSelector.valueProperty().addListener((obs, oldV, newV) -> updateLevelDetails(newV));

        Separator sep = new Separator();
        sep.setOpacity(0.65);

        // Buttons
        Button startBtn = primaryButton("Start Game");
        startBtn.setOnAction(e -> {
            Level sel = menuLevelSelector.getValue();
            if (sel != null) currentLevel = sel;
            resetGame();
            mainStage.setScene(gameScene);
        });
        Tooltip.install(startBtn, new Tooltip("Begin the tour on the selected board."));

        Button tutorialBtn = secondaryButton("Tutorial");
        tutorialBtn.setOnAction(e -> showTutorialDialog());
        Tooltip.install(tutorialBtn, new Tooltip("See how the knight moves and the goal."));

        Button quitBtn = ghostButton("Quit");
        quitBtn.setOnAction(e -> mainStage.close());

        HBox buttons = new HBox(12, startBtn, tutorialBtn, quitBtn);
        buttons.setAlignment(Pos.CENTER);

        // Keyboard shortcuts
        card.setOnKeyPressed(ke -> {
            if (ke.getCode() == KeyCode.ENTER) startBtn.fire();
            if (ke.getCode() == KeyCode.ESCAPE) quitBtn.fire();
        });

        // Assemble
        card.getChildren().setAll(
                title, subtitle, spacer1,
                selectLabel, menuLevelSelector, levelDetails, sep, buttons
        );

        root.getChildren().addAll(bgView, gradient, cardHolder);
        StackPane.setAlignment(cardHolder, Pos.CENTER);

        menuScene = new Scene(root, 960, 600);
        // Keep background fitting viewport
        menuScene.widthProperty().addListener((o, a, b) -> bgView.setFitWidth(b.doubleValue()));
        menuScene.heightProperty().addListener((o, a, b) -> bgView.setFitHeight(b.doubleValue()));

        // Focus to card so Enter/Esc work immediately
        menuScene.windowProperty().addListener((obs, oldWin, newWin) -> {
            if (newWin != null) {
                newWin.focusedProperty().addListener((o2, was, is) -> {
                    if (is) card.requestFocus();
                });
            }
        });
    }

    private Image loadMenuBackground() {
        // First try classpath (recommended)
        String[] paths = new String[]{
                "/resources/images/menu_background.png",
                "/images/menu_background.png",
                "/menu_background.png"
        };
        for (String p : paths) {
            try {
                var url = getClass().getResource(p);
                if (url != null) return new Image(url.toExternalForm());
            } catch (Exception ignored) { }
        }
        // Dev fallback (your uploaded path)
        try {
            java.io.File f = new java.io.File("/mnt/data/menu_background.png");
            if (f.exists()) return new Image(f.toURI().toString());
        } catch (Exception ignored) { }

        // Solid color fallback
        Canvas c = new Canvas(64, 64);
        GraphicsContext g = c.getGraphicsContext2D();
        g.setFill(Color.web("#0f172a"));
        g.fillRect(0,0,64,64);
        return c.snapshot(null, null);
    }

    private void updateLevelDetails(Level lvl) {
        if (lvl == null) { levelDetails.setText(""); return; }
        String txt = String.format(
                "Board: %dx%d • Points/Move: %d • Revisit Penalty: -%d • Undo Penalty: -%d",
                lvl.boardSize, lvl.boardSize, lvl.pointsPerMove, lvl.revisitPenalty, lvl.undoPenalty
        );
        levelDetails.setText(txt);
    }

    private Button primaryButton(String text) {
        Button b = new Button(text);
        b.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        b.setMinWidth(140);
        b.setStyle(
                "-fx-background-color: linear-gradient(#22c55e, #16a34a);"+
                        "-fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 12 18 12 18;"
        );
        b.setOnMouseEntered(e -> b.setStyle(
                "-fx-background-color: linear-gradient(#34d399, #16a34a);"+
                        "-fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 12 18 12 18;"
        ));
        b.setOnMouseExited(e -> b.setStyle(
                "-fx-background-color: linear-gradient(#22c55e, #16a34a);"+
                        "-fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 12 18 12 18;"
        ));
        return b;
    }

    private Button secondaryButton(String text) {
        Button b = new Button(text);
        b.setFont(Font.font("Verdana", FontWeight.SEMI_BOLD, 15));
        b.setMinWidth(120);
        b.setStyle(
                "-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: #F3F4F6;"+
                        "-fx-background-radius: 12; -fx-padding: 10 16 10 16;"+
                        "-fx-border-color: rgba(255,255,255,0.35); -fx-border-radius: 12;"
        );
        b.setOnMouseEntered(e -> b.setStyle(
                "-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white;"+
                        "-fx-background-radius: 12; -fx-padding: 10 16 10 16;"+
                        "-fx-border-color: rgba(255,255,255,0.55); -fx-border-radius: 12;"
        ));
        b.setOnMouseExited(e -> b.setStyle(
                "-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: #F3F4F6;"+
                        "-fx-background-radius: 12; -fx-padding: 10 16 10 16;"+
                        "-fx-border-color: rgba(255,255,255,0.35); -fx-border-radius: 12;"
        ));
        return b;
    }

    private Button ghostButton(String text) {
        Button b = new Button(text);
        b.setFont(Font.font("Verdana", 14));
        b.setMinWidth(90);
        b.setStyle("-fx-background-color: transparent; -fx-text-fill: #E5E7EB; -fx-padding: 8 12 8 12;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; -fx-padding: 8 12 8 12; -fx-background-radius: 10;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color: transparent; -fx-text-fill: #E5E7EB; -fx-padding: 8 12 8 12;"));
        return b;
    }

    private void showTutorialDialog() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Knight’s Tour Tutorial");
        a.setHeaderText("How the Knight Moves");
        a.setContentText(
                "• The knight moves in an L-shape: 2 in one direction, 1 perpendicular.\n" +
                        "• Visit each square exactly once.\n" +
                        "• Score: +" + currentLevel.pointsPerMove + " new square, " +
                        "-" + currentLevel.revisitPenalty + " revisit, " +
                        "-" + currentLevel.undoPenalty + " per undo.\n\n" +
                        "Tip: Look ahead for dead ends. Corner traps are common!"
        );
        a.showAndWait();
    }

    // =========================================================
    // GAME SCENE / BOARD / LOGIC  (kept aligned with your original)
    // =========================================================
    private void setupGameScene() {
        scoreLabel = new Label();
        scoreLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        moveCountLabel = new Label();
        moveCountLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        winLabel = new Label("");
        winLabel.setFont(Font.font("Impact", FontWeight.BOLD, 28));
        winLabel.setStyle("-fx-text-fill: #06D6A0;");

        undoButton = new Button("Undo");
        undoButton.setFont(Font.font(16));
        undoButton.setDisable(true);
        undoButton.setOnAction(e -> undoMove());

        Button backButton = new Button("Back to Menu");
        backButton.setFont(Font.font(16));
        backButton.setOnAction(e -> mainStage.setScene(menuScene));

        HBox controls = new HBox(12, scoreLabel, moveCountLabel, undoButton, backButton);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(8));

        VBox root = new VBox(8, controls, winLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        boardPane = createBoard();
        root.getChildren().add(boardPane);

        gameRoot = root;
        gameScene = new Scene(gameRoot, 700, 720);
    }

    private GridPane createBoard() {
        boardSize = currentLevel.boardSize;
        GridPane board = new GridPane();
        board.setHgap(0);
        board.setVgap(0);
        board.setAlignment(Pos.CENTER);

        squares = new Button[boardSize][boardSize];
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                Button square = createSquare(r, c);
                squares[r][c] = square;
                board.add(square, c, r);
            }
        }
        return board;
    }

    private Button createSquare(int row, int col) {
        Button b = new Button();
        b.setMinSize(SQUARE_SIZE, SQUARE_SIZE);
        b.setMaxSize(SQUARE_SIZE, SQUARE_SIZE);
        b.setFocusTraversable(false);

        boolean light = ((row + col) % 2 == 0);
        b.setStyle("-fx-background-color: " + (light ? toRgb(LIGHT_COLOR) : toRgb(DARK_COLOR)) + ";"
                + "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        b.setOnAction(e -> handleSquareClick(row, col));
        return b;
    }

    private void handleSquareClick(int row, int col) {
        // First click sets starting position
        if (currentRow == -1 && currentCol == -1) {
            placeKnight(row, col, true);
            return;
        }
        // Only legal knight moves allowed from current position
        if (!isLegalKnightMove(currentRow, currentCol, row, col)) return;

        boolean isRevisit = visited[row][col];
        placeKnight(row, col, !isRevisit);

        // scoring
        if (isRevisit) {
            score -= currentLevel.revisitPenalty;
        } else {
            score += currentLevel.pointsPerMove;
        }

        // win?
        if (visitedCount() == boardSize * boardSize) {
            winLabel.setText("Tour Complete! 🎉");
        }

        updateUIStatus();
    }

    private void placeKnight(int row, int col, boolean markVisited) {
        // Clear previous marker text styling
        if (currentRow >= 0 && currentCol >= 0) {
            Button prev = squares[currentRow][currentCol];
            prev.setText(visited[currentRow][currentCol] ? "•" : "");
            prev.setStyle(prev.getStyle()); // keep bg
        }

        currentRow = row;
        currentCol = col;

        if (visited == null) visited = new boolean[boardSize][boardSize];
        if (markVisited) visited[row][col] = true;

        Button here = squares[row][col];
        here.setText("♞");
        here.setStyle(here.getStyle() + "; -fx-text-fill: #111827; -fx-font-size: 18px; -fx-font-weight: bold;");

        moveStack.push(new int[]{row, col});
        moveCount++;

        // Update coloring to hint legal moves
        refreshHighlights();
    }

    private void refreshHighlights() {
        // Reset all square backgrounds to base board colors + visited tint
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                boolean light = ((r + c) % 2 == 0);
                String base = "-fx-background-color: " + (light ? toRgb(LIGHT_COLOR) : toRgb(DARK_COLOR)) + ";";
                if (visited != null && visited[r][c]) {
                    base += " -fx-background-insets: 0; -fx-background-radius: 0; -fx-border-color: rgba(0,0,0,0.18);";
                    base += " -fx-effect: null;";
                }
                squares[r][c].setStyle(base);
                if (visited != null && visited[r][c] && !(r == currentRow && c == currentCol)) {
                    squares[r][c].setText("•");
                    squares[r][c].setTextFill(Color.WHITE);
                } else if (!(r == currentRow && c == currentCol)) {
                    squares[r][c].setText("");
                }
            }
        }
        // Highlight legal next moves
        if (currentRow >= 0 && currentCol >= 0) {
            for (int[] d : KNIGHT_DELTAS) {
                int nr = currentRow + d[0];
                int nc = currentCol + d[1];
                if (inBounds(nr, nc)) {
                    Button b = squares[nr][nc];
                    b.setStyle(b.getStyle() + " -fx-background-color: " + toRgb(MOVE_HIGHLIGHT) + ";");
                }
            }
            // Mark start square distinctly
            squares[currentRow][currentCol].setStyle(squares[currentRow][currentCol].getStyle()
                    + " -fx-background-color: " + toRgb(START_COLOR) + ";");
        }
    }

    private boolean isLegalKnightMove(int r1, int c1, int r2, int c2) {
        int dr = Math.abs(r1 - r2);
        int dc = Math.abs(c1 - c2);
        return (dr == 2 && dc == 1) || (dr == 1 && dc == 2);
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < boardSize && c >= 0 && c < boardSize;
    }

    private int visitedCount() {
        int n = 0;
        if (visited == null) return 0;
        for (int r = 0; r < boardSize; r++)
            for (int c = 0; c < boardSize; c++)
                if (visited[r][c]) n++;
        return n;
    }

    private void undoMove() {
        if (moveStack.isEmpty()) return;

        // Remove current
        int[] last = moveStack.pop();
        int lr = last[0], lc = last[1];
        // If the square had been marked visited only by this move, revert visitation.
        // For simplicity, keep it visited if visited earlier; we recompute visited from stack.
        recomputeVisitedFromStack();

        // Reposition knight to previous spot (if any)
        if (!moveStack.isEmpty()) {
            int[] prev = moveStack.peek();
            currentRow = prev[0];
            currentCol = prev[1];
        } else {
            currentRow = -1;
            currentCol = -1;
            winLabel.setText("");
        }

        moveCount = Math.max(0, moveCount - 1);
        score -= currentLevel.undoPenalty;

        refreshBoardAfterUndo();
        updateUIStatus();
    }

    private void recomputeVisitedFromStack() {
        visited = new boolean[boardSize][boardSize];
        for (int[] m : moveStack) visited[m[0]][m[1]] = true;
    }

    private void refreshBoardAfterUndo() {
        // Redraw all squares text markers and highlights
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                Button b = squares[r][c];
                boolean light = ((r + c) % 2 == 0);
                b.setStyle("-fx-background-color: " + (light ? toRgb(LIGHT_COLOR) : toRgb(DARK_COLOR)) + ";"
                        + "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
                if (visited[r][c]) {
                    b.setText("•");
                } else {
                    b.setText("");
                }
            }
        }
        if (currentRow >= 0 && currentCol >= 0) {
            squares[currentRow][currentCol].setText("♞");
            squares[currentRow][currentCol].setStyle(squares[currentRow][currentCol].getStyle()
                    + " -fx-background-color: " + toRgb(START_COLOR) + "; -fx-text-fill: #111827; -fx-font-size: 18px; -fx-font-weight: bold;");
        }
        refreshHighlights();
    }

    private void updateUIStatus() {
        scoreLabel.setText("Score: " + score);
        moveCountLabel.setText("Moves: " + moveCount);
        undoButton.setDisable(moveStack.isEmpty());
    }

    private void resetGame() {
        score = 0;
        moveCount = 0;
        currentRow = -1;
        currentCol = -1;
        moveStack.clear();
        boardSize = currentLevel.boardSize;
        visited = new boolean[boardSize][boardSize];
        winLabel.setText("");
        updateUIStatus();

        if (gameRoot != null) {
            if (boardPane != null) {
                gameRoot.getChildren().remove(boardPane);
            }
            boardPane = createBoard();
            gameRoot.getChildren().add(boardPane);
        }
    }

    // =========================================================
    // Helpers
    // =========================================================
    private static String toRgb(Color c) {
        int r = (int)Math.round(c.getRed() * 255.0);
        int g = (int)Math.round(c.getGreen() * 255.0);
        int b = (int)Math.round(c.getBlue() * 255.0);
        return String.format("rgb(%d,%d,%d)", r, g, b);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
