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
                        (connected < snakeLen) ? -10 * (snakeLen - connected) : 0
                )  - (
                        enemyProximity >= snakeLen/2 ? 0 : 50 * (snakeLen/2 - enemyProximity) // default length/2 = 4
                )
        ) / moves;
    }

}
