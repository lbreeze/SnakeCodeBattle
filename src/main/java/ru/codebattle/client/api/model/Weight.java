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
    private int available = 0;

    @Getter
    @Setter
    private int connected = 0;

    @Getter
    @Setter
    private int enemyProximity = 0;

    public double getFactor() {
        return (double) (available == 0 ? -300 : score) / Math.pow(moves, 3); // * Math.pow(connected, 0.1)
    }

}
