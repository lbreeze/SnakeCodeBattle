package ru.codebattle.client.api.model;

import lombok.Getter;
import lombok.Setter;
import ru.codebattle.client.Main;
import ru.codebattle.client.api.BoardElement;
import ru.codebattle.client.api.BoardPoint;
import ru.codebattle.client.api.Direction;
import ru.codebattle.client.api.SnakeAction;

import java.util.*;
import java.util.stream.Collectors;

import static ru.codebattle.client.api.BoardElement.*;

public class Snake {

    public static Direction LAST_MOVE = Direction.RIGHT;

    @Getter
    @Setter
    private int stones = 0;

    @Getter
    @Setter
    private int fury = 0;

    @Getter
    @Setter
    private LinkedList<BoardPoint> body;

    @Getter
    private Weight[][] weightsMap;

    @Getter
    private BoardPoint destination;

    public void eatStone() {
        stones++;
    }

    public void fury() {
            BoardElement.EAGER_TYPES.add(BoardElement.STONE);

            BoardElement.EAGER_TYPES.addAll(BoardElement.ENEMY_HEAD);
        BoardElement.EAGER_TYPES.addAll(ENEMY_BODY);
    }

    public void peace() {
        if (fury > 0) {
            fury--;
            if (fury == 0) {
                BoardElement.EAGER_TYPES.remove(BoardElement.STONE);

                BoardElement.EAGER_TYPES.removeAll(BoardElement.ENEMY_HEAD);
                BoardElement.EAGER_TYPES.removeAll(ENEMY_BODY);
            }
        }
    }

    public BoardPoint getFirst() {
        return getBody().getFirst();
    }

    public BoardPoint getLast() {
        return getBody().getLast();
    }

    public void addFirst(BoardPoint item) {
        getBody().addFirst(item);
    }

    public void addLast(BoardPoint item) {
        getBody().addLast(item);
    }

    public SnakeAction moveDecision(Room room) {
        Direction result = LAST_MOVE;

        BoardPoint routePoint = findDestinationRoute(body.getFirst(), room);

        if (routePoint != null) {
            if (routePoint.getX() == body.getFirst().getX() && routePoint.getY() == body.getFirst().getY() + 1) {
                result = Direction.DOWN;
            }

            if (routePoint.getX() == body.getFirst().getX() && routePoint.getY() == body.getFirst().getY() - 1) {
                result = Direction.UP;
            }

            if (routePoint.getX() == body.getFirst().getX() - 1 && routePoint.getY() == body.getFirst().getY()) {
                result = Direction.LEFT;
            }

            if (routePoint.getX() == body.getFirst().getX() + 1 && routePoint.getY() == body.getFirst().getY()) {
                result = Direction.RIGHT;
            }

            if (room.getRoom()[routePoint.getX()][routePoint.getY()] == BoardElement.STONE) {
                eatStone();
            }
        }
        LAST_MOVE = result;

        boolean act = false;
        BoardPoint tailPoint = getBody().getLast();
        if (stones > 0 && (BoardElement.ENEMY_HEAD.contains(room.getRoom()[tailPoint.getX() - 1][tailPoint.getY()]) ||
                BoardElement.ENEMY_HEAD.contains(room.getRoom()[tailPoint.getX() + 1][tailPoint.getY()]) ||
                BoardElement.ENEMY_HEAD.contains(room.getRoom()[tailPoint.getX()][tailPoint.getY() - 1]) ||
                BoardElement.ENEMY_HEAD.contains(room.getRoom()[tailPoint.getX()][tailPoint.getY() + 1]))) {
            switch (LAST_MOVE) {
                case LEFT:
                    act = !BoardElement.HEAD.contains(room.getRoom()[tailPoint.getX() - 1][tailPoint.getY()]);
                    break;
                case RIGHT:
                    act = !BoardElement.HEAD.contains(room.getRoom()[tailPoint.getX() + 1][tailPoint.getY()]);
                    break;
                case UP:
                    act = !BoardElement.HEAD.contains(room.getRoom()[tailPoint.getX()][tailPoint.getY() - 1]);
                    break;
                case DOWN:
                    act = !BoardElement.HEAD.contains(room.getRoom()[tailPoint.getX()][tailPoint.getY() + 1]);
                    break;
            }


        }
        if (act && stones > 0)
            stones--;

        if (routePoint != null && room.getRoom()[routePoint.getX()][routePoint.getY()] == BoardElement.FURY_PILL) {
            fury += 10;
            fury();
        }

        peace();

        if (room.getEnemies().size() == 1 && fury == 0) {
            if (getBody().size() > room.getEnemies().get(0).getBody().size() + 2 && room.getEnemies().get(0).getFury() == 0) {
                BoardElement.EAGER_TYPES.addAll(ENEMY_HEAD);
            } else {
                BoardElement.EAGER_TYPES.removeAll(ENEMY_HEAD);
            }
        }


        Main.writeToFile("[" + body.getFirst().getX() + ", " + body.getFirst().getY() + "] -> " + new SnakeAction(act, result) + " -> [" + destination.getX() + ", " + destination.getY() + "] FURY:" + fury);
        return new SnakeAction(act, result);
    }

    private void findDestinationPointOnWM(BoardPoint src, Map<BoardPoint, List<BoardPoint>> routePoints, Room roomObj) {
        BoardElement[][] room = roomObj.getRoom();

        destination = null;
        for (int col = 0; col < roomObj.getWidth(); col++) {
            for (int row = 0; row < roomObj.getHeight(); row++) {
                for (List<BoardPoint> pointsList : routePoints.values()) {
                    if (pointsList.contains(BoardPoint.of(col, row))) {
                        weightsMap[col][row].setConnected(pointsList.size());
                    }
                }
                if (BoardElement.EAGER_TYPES.contains(room[col][row]) &&
                        weightsMap[col][row].getMoves() > 0) {
                    if (destination == null) {
                        Main.writeToFile("WEIGHT: " + room[col][row].name() + " [" + col + ", " + row + "] = " + weightsMap[col][row].getFactor() + " AVAIL: " + weightsMap[col][row].getAvailable());
                        destination = BoardPoint.of(col, row);
                    } else {
                        if (weightsMap[col][row].getFactor() > weightsMap[destination.getX()][destination.getY()].getFactor()) {
                            destination = BoardPoint.of(col, row);
                        }

                        Main.writeToFile("WEIGHT: " + room[col][row].name() + " [" + col + ", " + row + "] = " + weightsMap[col][row].getFactor() + " AVAIL: " + weightsMap[col][row].getAvailable());
/*
                        if (weightsMap[x][y].getMoves() < weightsMap[destination[0]][destination[1]].getMoves()) {
                            destination = new int[] {x, y };
                        } else if (weightsMap[x][y].getScore() > weightsMap[destination[0]][destination[1]].getScore()) {
                            destination = new int[] {x, y };
                        } else if (weightsMap[x][y].getAvailable() > weightsMap[destination[0]][destination[1]].getAvailable()) {
                            destination = new int[] {x, y};
                        }
*/
                    }
                }
            }
        }
        if (destination == null) {
            for (BoardPoint bp : routePoints.keySet()) {
                if (destination == null || weightsMap[bp.getX()][bp.getY()].getFactor() > weightsMap[destination.getX()][destination.getY()].getFactor()) {
                    Main.writeToFile("WEIGHT: " + room[bp.getX()][bp.getY()].name() + " [" + bp.getX() + ", " + bp.getY() + "] = " + weightsMap[bp.getX()][bp.getY()].getFactor() + " AVAIL: " + weightsMap[bp.getX()][bp.getY()].getAvailable());
                    destination = BoardPoint.of(bp);
                }
            }
             if (destination == null) {
                 BoardPoint bp = BoardPoint.of(src.getX() - 1, src.getY());
                 if (destination ==null || weightsMap[bp.getX()][bp.getY()].getFactor() > weightsMap[destination.getX()][destination.getY()].getFactor()) {
                     Main.writeToFile("WEIGHT: " + room[bp.getX()][bp.getY()].name() + " [" + bp.getX() + ", " + bp.getY() + "] = " + weightsMap[bp.getX()][bp.getY()].getFactor() + " AVAIL: " + weightsMap[bp.getX()][bp.getY()].getAvailable());
                     destination = bp;
                 }
             }
        }
    }

    private BoardPoint findDestinationRoute(BoardPoint src, Room roomObj) {

        weightsMap = new Weight[roomObj.getWidth()][roomObj.getHeight()];
        for (Weight[] moveCol : weightsMap)
            for (int i = 0; i < moveCol.length; i++)
                moveCol[i] = new Weight();

        weightsMap[src.getX()][src.getY()].setMoves(0);
        weightsMap[src.getX()][src.getY()].setScore(0);

        Map<BoardPoint, Direction> wavePoints = new HashMap<>();
        wavePoints.put(BoardPoint.of(src), LAST_MOVE);

        boolean first = true;
        int moveCount = 1;
        Map<BoardPoint, List<BoardPoint>> routePoints = new HashMap<>();
        while (!wavePoints.isEmpty()) {
            Map<BoardPoint, Direction> newWavePoints = new HashMap<>();
            for (BoardPoint wavePoint : wavePoints.keySet()) {
                int available = 0;

                if (wavePoints.get(wavePoint) != Direction.RIGHT) {
                    available += calcNewWavePoint(
                            BoardPoint.of(wavePoint.getX() - 1, wavePoint.getY()),
                            roomObj,
                            newWavePoints,
                            moveCount,
                            Direction.LEFT,
                            routePoints,
                            first ? null : wavePoint);
                }


                if (wavePoints.get(wavePoint) != Direction.LEFT) {
                    available += calcNewWavePoint(
                            BoardPoint.of(wavePoint.getX() + 1, wavePoint.getY()),
                            roomObj,
                            newWavePoints,
                            moveCount,
                            Direction.RIGHT,
                            routePoints,
                            first ? null : wavePoint);
                }

                if (wavePoints.get(wavePoint) != Direction.DOWN) {
                    available += calcNewWavePoint(
                            BoardPoint.of(wavePoint.getX(), wavePoint.getY() - 1),
                            roomObj,
                            newWavePoints,
                            moveCount,
                            Direction.UP,
                            routePoints,
                            first ? null : wavePoint);
                }

                if (wavePoints.get(wavePoint) != Direction.UP) {
                    available += calcNewWavePoint(
                            BoardPoint.of(wavePoint.getX(), wavePoint.getY() + 1),
                            roomObj,
                            newWavePoints,
                            moveCount,
                            Direction.DOWN,
                            routePoints,
                            first ? null : wavePoint);
                }

                if (weightsMap[wavePoint.getX()][wavePoint.getY()].getAvailable() < available)
                    weightsMap[wavePoint.getX()][wavePoint.getY()].setAvailable(available);

                if (LAST_MOVE != null) {
                    LAST_MOVE = null;
                }
            }
            first = false;
            wavePoints.clear();
            wavePoints.putAll(newWavePoints);
            moveCount++;
        }

        findDestinationPointOnWM(src, routePoints, roomObj);

        BoardPoint routePoint = null;
        for (Map.Entry<BoardPoint, List<BoardPoint>> entry : routePoints.entrySet()) {
            if (routePoint == null) {
                routePoint = entry.getKey();
            } else {
                // TODO !!!!
                if (entry.getValue().contains(destination) && (weightsMap[entry.getKey().getX()][entry.getKey().getY()].getFactor() > 0)) {
                    routePoint = entry.getKey();
                } else if  (2 * weightsMap[routePoint.getX()][routePoint.getY()].getFactor() < weightsMap[entry.getKey().getX()][entry.getKey().getY()].getFactor()) {
                    routePoint = entry.getKey();
                }
            }
            Main.writeToFile("ROUTE : [" + entry.getKey().getX() + ", " + entry.getKey().getY() + "] WEIGHT: " + weightsMap[entry.getKey().getX()][entry.getKey().getY()].getFactor() + " AVAIL: " + weightsMap[entry.getKey().getX()][entry.getKey().getY()].getAvailable());
        }

        return routePoint;
    }

    private int calcNewWavePoint(BoardPoint wP, Room roomObj, Map<BoardPoint, Direction> newWavePoints, int moveCount, Direction move, Map<BoardPoint, List<BoardPoint>> routePoints, BoardPoint origin) {
        BoardElement[][] room = roomObj.getRoom();

        int available = 0;
        if (wP.getX() >= 0 && wP.getY() >= 0 && wP.getX() < roomObj.getWidth() && wP.getY() < roomObj.getHeight()) {
            wP = setWeight(wP, roomObj, weightsMap, moveCount);
            if (wP != null) {
                available++;
                newWavePoints.put(wP, move);
                if (origin == null) {
                    List<BoardPoint> wPList = new ArrayList<>();
                    wPList.add(wP);
                    routePoints.put(wP, wPList);
                } else {
                    for (List<BoardPoint> origins : routePoints.values()) {
                        if (origins.contains(origin)) {
                            origins.add(wP);
                        }
                    }
                }
            }
        }
        return available;
    }

    private BoardPoint setWeight(BoardPoint wP, Room roomObj, Weight[][] weightMap, int moveCount) {
        BoardElement[][] room = roomObj.getRoom();
        if ((weightMap[wP.getX()][wP.getY()].getMoves() < 0 ||
                        weightMap[wP.getX()][wP.getY()].getMoves() > moveCount)) {
            weightMap[wP.getX()][wP.getY()].setMoves(moveCount);

            List<Snake> enemies = roomObj.getEnemies().parallelStream()
                    .filter(snake -> snake.getBody().getFirst().equals(wP)
                            || snake.getBody().getFirst().equals(wP.shiftBottom())
                            || snake.getBody().getFirst().equals(wP.shiftTop())
                            || snake.getBody().getFirst().equals(wP.shiftLeft())
                            || snake.getBody().getFirst().equals(wP.shiftRight()))
                    .collect(Collectors.toList());
            if (fury < moveCount && room[wP.getX()][wP.getY()].equals(BoardElement.STONE)) {
                if (body.size() < 5) {
                    weightMap[wP.getX()][wP.getY()].setScore(WALL.getScore());
                } else {
                    weightMap[wP.getX()][wP.getY()].setScore(-STONE.getScore());
                }
            } else if (fury >= moveCount && BoardElement.ENEMY_BODY.contains(room[wP.getX()][wP.getY()])) {
                Snake enemy = roomObj.getEnemies().parallelStream()
                        .filter(snake -> snake.getBody().getFirst().equals(wP)).findFirst().orElse(null);
                Main.writeToFile("ENEMY FURY [" + wP.getX() + ", " + wP.getY() + "] : " + (enemy == null ? "NULL" : enemy.getFury()));
                if (enemy == null || enemy.getFury() == 0) {
                    weightMap[wP.getX()][wP.getY()].setScore(-ENEMY_HEAD_EVIL.getScore());
                } else {
                    weightMap[wP.getX()][wP.getY()].setScore(room[wP.getX()][wP.getY()].getScore());
                }
            } else if (!enemies.isEmpty()) {
                enemies.forEach(enemy -> {
                    boolean canEat = (((fury >= moveCount) || (body.size() >= enemy.getBody().size() + 2))
                            && enemy.getFury() == 0)
                            || ((fury >= moveCount) && (body.size() >= enemy.getBody().size() + 2));
                    if (canEat) {
                        weightMap[wP.getX()][wP.getY()].setScore(room[wP.getX()][wP.getY()].getScore());
                    } else {
                        weightMap[wP.getX()][wP.getY()].setScore(ENEMY_HEAD_EVIL.getScore());
                    }
                });
            } else if (room[wP.getX()][wP.getY()].equals(FURY_PILL)) {
                List<Integer> enemyDistance = roomObj.getEnemies().parallelStream().map(snake -> { return Math.abs(snake.getBody().getFirst().getX() - wP.getX()) + Math.abs(snake.getBody().getFirst().getY() - wP.getY()); }).collect(Collectors.toList());
                enemyDistance.forEach( dst -> {
                    if ((moveCount < dst) && (fury + 10 > dst / 2)) {
                        weightMap[wP.getX()][wP.getY()].setScore(FURY_PILL.getScore());
                    }
                });
            } else {
                weightMap[wP.getX()][wP.getY()].setScore(room[wP.getX()][wP.getY()].getScore());
            }
            return wP;
        }
        return null;
    }

}
