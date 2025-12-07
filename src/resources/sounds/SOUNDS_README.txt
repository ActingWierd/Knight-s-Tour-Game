KNIGHT'S TOUR - SOUND EFFECTS GUIDE
=====================================

This folder should contain the following sound files in WAV format:

Required Sound Files:
---------------------
1. move.wav         - Played when the knight is placed on a square
2. invalid.wav      - Played when an invalid move is attempted
3. complete.wav     - Played when the tour is completed
4. high_score.wav   - Played when a new high score is achieved
5. undo.wav         - Played when the undo button is pressed
6. click.wav        - (Optional) For button clicks

Where to Get Free Sound Effects:
---------------------------------
1. Freesound.org (https://freesound.org/)
   - Free sound effects library
   - Search for: "click", "error", "success", "fanfare", "undo"
   - Requires free account

2. Zapsplat.com (https://www.zapsplat.com/)
   - Free sound effects
   - Search categories: UI Sounds, Game Sounds

3. Mixkit.co (https://mixkit.co/free-sound-effects/)
   - No account required
   - Browse "Interface & Buttons" and "Game" categories

Sound Recommendations:
----------------------
- move.wav: Short "click" or "tap" sound (chess piece placement)
- invalid.wav: Short "buzz" or "error beep"
- complete.wav: Success chime or fanfare (1-2 seconds)
- high_score.wav: Celebration sound or triumphant fanfare (2-3 seconds)
- undo.wav: Soft "swoosh" or reverse sound
- click.wav: UI button click

Technical Requirements:
-----------------------
- Format: WAV (recommended) or MP3
- Length: Keep sounds short (under 3 seconds, except high_score which can be longer)
- Volume: Normalize all sounds to similar volume levels

Installation:
-------------
Simply place the sound files in this folder (src/resources/sounds/)
The game will automatically load them when it starts.

Note: The game will work without sounds - if sound files are not found,
it will print a warning and continue silently.
