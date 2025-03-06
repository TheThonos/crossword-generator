package dev.cqb13.crosswordgenerator.generator;

public enum Direction {
    Across,
    Down;


    private Direction () {}

    public Direction other() {
        return switch (this) {
            case Down -> Across;
            case Across -> Down;
        };
    }
}
