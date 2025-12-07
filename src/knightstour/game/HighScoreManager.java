package knightstour.game;

import java.io.*;
import java.util.Properties;

/**
 * Manages high scores for the Knight's Tour game.
 * Tracks best score and fewest moves for each difficulty level.
 */
public class HighScoreManager {
    private static final String SCORE_FILE = "knightstour_highscores.properties";
    private Properties scores;

    public HighScoreManager() {
        scores = new Properties();
        loadScores();
    }

    /**
     * Load high scores from file
     */
    private void loadScores() {
        File file = new File(SCORE_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                scores.load(fis);
            } catch (IOException e) {
                System.err.println("Error loading high scores: " + e.getMessage());
            }
        }
    }

    /**
     * Save high scores to file
     */
    private void saveScores() {
        try (FileOutputStream fos = new FileOutputStream(SCORE_FILE)) {
            scores.store(fos, "Knight's Tour High Scores");
        } catch (IOException e) {
            System.err.println("Error saving high scores: " + e.getMessage());
        }
    }

    /**
     * Get the best score for a difficulty level
     */
    public int getBestScore(int boardSize) {
        String key = "score_" + boardSize;
        return Integer.parseInt(scores.getProperty(key, "0"));
    }

    /**
     * Get the fewest moves for a difficulty level
     */
    public int getFewestMoves(int boardSize) {
        String key = "moves_" + boardSize;
        return Integer.parseInt(scores.getProperty(key, "999"));
    }

    /**
     * Check if this is a new high score and update if so
     * @return true if new high score was set
     */
    public boolean checkAndUpdateScore(int boardSize, int score) {
        int currentBest = getBestScore(boardSize);
        if (score > currentBest) {
            scores.setProperty("score_" + boardSize, String.valueOf(score));
            saveScores();
            return true;
        }
        return false;
    }

    /**
     * Check if this is a new record for fewest moves and update if so
     * @return true if new record was set
     */
    public boolean checkAndUpdateMoves(int boardSize, int moves) {
        int currentBest = getFewestMoves(boardSize);
        if (moves < currentBest) {
            scores.setProperty("moves_" + boardSize, String.valueOf(moves));
            saveScores();
            return true;
        }
        return false;
    }

    /**
     * Get the best attempt score (even if tour not completed)
     */
    public int getBestAttemptScore(int boardSize) {
        String key = "attempt_score_" + boardSize;
        return Integer.parseInt(scores.getProperty(key, "0"));
    }

    /**
     * Get the most squares visited (even if tour not completed)
     */
    public int getMostSquaresVisited(int boardSize) {
        String key = "attempt_squares_" + boardSize;
        return Integer.parseInt(scores.getProperty(key, "0"));
    }

    /**
     * Check and update best attempt score (for incomplete tours)
     * @return true if new record was set
     */
    public boolean checkAndUpdateAttemptScore(int boardSize, int score) {
        int currentBest = getBestAttemptScore(boardSize);
        if (score > currentBest) {
            scores.setProperty("attempt_score_" + boardSize, String.valueOf(score));
            saveScores();
            return true;
        }
        return false;
    }

    /**
     * Check and update most squares visited (for incomplete tours)
     * @return true if new record was set
     */
    public boolean checkAndUpdateSquaresVisited(int boardSize, int squaresVisited) {
        int currentBest = getMostSquaresVisited(boardSize);
        if (squaresVisited > currentBest) {
            scores.setProperty("attempt_squares_" + boardSize, String.valueOf(squaresVisited));
            saveScores();
            return true;
        }
        return false;
    }

    /**
     * Check if tour was completed (all squares visited)
     */
    public static boolean isTourComplete(int boardSize, int squaresVisited) {
        return squaresVisited == (boardSize * boardSize);
    }
}
