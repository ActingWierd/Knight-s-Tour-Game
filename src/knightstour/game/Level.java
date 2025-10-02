package knightstour.game;

public class Level {
    public final String name;
    public final int boardSize;
    public final int pointsPerMove;
    public final int revisitPenalty;
    public final int undoPenalty;

    public Level(String name, int boardSize, int pointsPerMove, int revisitPenalty, int undoPenalty) {
        this.name = name;
        this.boardSize = boardSize;
        this.pointsPerMove = pointsPerMove;
        this.revisitPenalty = revisitPenalty;
        this.undoPenalty = undoPenalty;
    }

    @Override
    public String toString() {
        return name;
    }
}

