package ru.codebattle.client.api.model;

import lombok.Getter;
import lombok.Setter;

public class Weight {

    @Getter
    @Setter
    private int moves = -1;

    @Getter
    @Setter
    private int score = -1;

    @Getter
    @Setter
    private int connected = 0;

    // todo unused
    @Getter
    @Setter
    private int enemyProximity = Integer.MAX_VALUE;

    public double getFactor(int snakeLen) {
        return (double) (
                score + (
                        (connected < snakeLen) ? -30 * (snakeLen - connected) : 0
                )  - (
                        enemyProximity == 0 || enemyProximity >= 4 ? 0 : 100/Math.sqrt(enemyProximity)
                )
        ) / moves;
    }

    @Override
    public String toString() {
        return
                ", MOVES=" + moves +
                ", SCORE=" + score +
                ", CONN=" + connected +
                ", PROX=" + enemyProximity
                ;
    }
}
