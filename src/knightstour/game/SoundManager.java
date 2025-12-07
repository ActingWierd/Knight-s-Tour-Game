package knightstour.game;

import javafx.scene.media.AudioClip;
import java.net.URL;

/**
 * Manages sound effects for the Knight's Tour game.
 * Loads and plays audio clips for various game events.
 */
public class SoundManager {
    private AudioClip moveSound;
    private AudioClip invalidSound;
    private AudioClip completeSound;
    private AudioClip highScoreSound;
    private AudioClip undoSound;
    private AudioClip clickSound;

    private boolean soundEnabled = true;

    public SoundManager() {
        loadSounds();
    }

    /**
     * Load all sound effects from resources folder
     */
    private void loadSounds() {
        try {
            moveSound = loadSound("/resources/sounds/move.wav");
            invalidSound = loadSound("/resources/sounds/invalid.wav");
            completeSound = loadSound("/resources/sounds/complete.wav");
            highScoreSound = loadSound("/resources/sounds/high_score.wav");
            undoSound = loadSound("/resources/sounds/undo.wav");
            clickSound = loadSound("/resources/sounds/click.wav");
        } catch (Exception e) {
            System.err.println("Warning: Could not load some sound effects: " + e.getMessage());
            System.err.println("Game will continue without sounds.");
        }
    }

    /**
     * Load a single sound file
     */
    private AudioClip loadSound(String path) {
        try {
            URL resource = getClass().getResource(path);
            if (resource != null) {
                return new AudioClip(resource.toExternalForm());
            } else {
                System.err.println("Sound file not found: " + path);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error loading sound: " + path + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Play sound when knight is placed on a square
     */
    public void playMoveSound() {
        playSound(moveSound);
    }

    /**
     * Play sound when an invalid move is attempted
     */
    public void playInvalidSound() {
        playSound(invalidSound);
    }

    /**
     * Play sound when tour is completed
     */
    public void playCompleteSound() {
        playSound(completeSound);
    }

    /**
     * Play sound when a new high score is achieved
     */
    public void playHighScoreSound() {
        playSound(highScoreSound);
    }

    /**
     * Play sound when undo is used
     */
    public void playUndoSound() {
        playSound(undoSound);
    }

    /**
     * Play sound for button clicks
     */
    public void playClickSound() {
        playSound(clickSound);
    }

    /**
     * Play a sound clip if sounds are enabled
     */
    private void playSound(AudioClip clip) {
        if (soundEnabled && clip != null) {
            try {
                clip.play();
            } catch (Exception e) {
                System.err.println("Error playing sound: " + e.getMessage());
            }
        }
    }

    /**
     * Enable or disable all sounds
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    /**
     * Check if sounds are enabled
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Toggle sound on/off
     */
    public void toggleSound() {
        soundEnabled = !soundEnabled;
    }
}
