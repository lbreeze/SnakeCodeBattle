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

    public double getFactor() {
        return (double) ((Math.pow(score, 2) + 1) * Math.sqrt(available) * connected) / Math.pow(moves, 4);
    }
}
