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
    public static int STONES = 0;
    public static int FURY = 0;
    public static int FLY = 0;

    @Getter
    @Setter
    private LinkedList<BoardPoint> body;

    @Getter
    private Weight[][] weightsMap;

    @Getter
    private BoardPoint destination;

    public void eatStone() {
        STONES++;
        //FURY = 0;
    }

    public void fly() {
        // предположение. если еще действует, то таблетку не едим
        if (FLY == 0 && FURY == 0) {
            FLY = 10;

            BoardElement.GOOD_TYPES.add(BoardElement.STONE);
            BoardElement.GOOD_TYPES.addAll(BoardElement.ENEMY);
        }
    }

    public void fury() {
        // предположение. если еще действует, то таблетку не едим
        //if (FURY == 0 && FLY == 0) {
            //FURY = 10;

            BoardElement.GOOD_TYPES.add(BoardElement.STONE);
            BoardElement.EAGER_TYPES.add(BoardElement.STONE);

            BoardElement.GOOD_TYPES.addAll(BoardElement.ENEMY);
            BoardElement.EAGER_TYPES.addAll(BoardElement.ENEMY_HEAD);
        BoardElement.EAGER_TYPES.addAll(ENEMY_BODY);
        //}
    }

    public void peace() {
//        if (FLY > 0) {
//            FLY--;
//            if (FLY == 0) {
//                BoardElement.GOOD_TYPES.remove(BoardElement.STONE);
//                BoardElement.GOOD_TYPES.removeAll(BoardElement.ENEMY);
//            }
//        }
        if (FURY > 0) {
            FURY--;
            if (FURY == 0) {
                BoardElement.GOOD_TYPES.remove(BoardElement.STONE);
                BoardElement.EAGER_TYPES.remove(BoardElement.STONE);

                BoardElement.GOOD_TYPES.removeAll(BoardElement.ENEMY);
                BoardElement.EAGER_TYPES.removeAll(BoardElement.ENEMY_HEAD);
                BoardElement.EAGER_TYPES.removeAll(ENEMY_BODY);
            }
        }


//        if (body.size() < 5)
//        else
//            BoardElement.GOOD_TYPES.add(BoardElement.STONE);
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
        if (STONES > 0 && (BoardElement.ENEMY_HEAD.contains(room.getRoom()[tailPoint.getX() - 1][tailPoint.getY()]) ||
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
        // test stone & grow
        //if (routePoint != null && STONES > 0 && room.getRoom()[routePoint.getX()][routePoint.getY()] == APPLE) {
        //act =true;
        //}
        if (act && STONES > 0)
            STONES--;

        if (routePoint != null && room.getRoom()[routePoint.getX()][routePoint.getY()] == BoardElement.FURY_PILL) {
            FURY += 10;
            fury();
        }

        peace();

        if (room.getEnemies().size() == 1) {
            if (getBody().size() > room.getEnemies().get(0).getBody().size() + 2 && !BoardElement.ENEMY_HEAD_EVIL.equals(room.getRoom()[room.getEnemies().get(0).getBody().getFirst().getX()][room.getEnemies().get(0).getBody().getFirst().getY()])) {
                BoardElement.GOOD_TYPES.addAll(ENEMY_HEAD);
                BoardElement.EAGER_TYPES.addAll(ENEMY_HEAD);
            } else {
                BoardElement.GOOD_TYPES.removeAll(ENEMY_HEAD);
                BoardElement.EAGER_TYPES.removeAll(ENEMY_HEAD);
            }
        }


        Main.writeToFile("[" + body.getFirst().getX() + ", " + body.getFirst().getY() + "] -> " + new SnakeAction(act, result) + " -> [" + destination.getX() + ", " + destination.getY() + "] FURY:" + FURY);
        return new SnakeAction(act, result);
    }

    private void findDestinationPointOnWM(BoardPoint src, Map<BoardPoint, List<BoardPoint>> routePoints, Room roomObj) {
        BoardElement[][] room = roomObj.getRoom();

        StringBuilder eagers = new StringBuilder("EAGERS: ");
        for (BoardElement be : BoardElement.EAGER_TYPES) {
            eagers.append(be.name()).append(" ");
        }
        Main.writeToFile(eagers.toString().trim());

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
                        Main.writeToFile("WEIGHT: " + room[col][row].name() + " [" + col + ", " + row + "] = " + weightsMap[col][row].getFactor() + " CONN: " + weightsMap[col][row].getConnected());
                        destination = BoardPoint.of(col, row);
                    } else {
                        Main.writeToFile("WEIGHT: " + room[col][row].name() + " [" + col + ", " + row + "] = " + weightsMap[col][row].getFactor() + " CONN: " + weightsMap[col][row].getConnected());
                        if (weightsMap[col][row].getFactor() > weightsMap[destination.getX()][destination.getY()].getFactor()) {
                            destination = BoardPoint.of(col, row);
                        }
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
                    destination = BoardPoint.of(bp);
                }
//                destination = BoardPoint.of(roomObj.getWidth() / 2, roomObj.getHeight() / 2);
            }
             if (destination == null) {
                 BoardPoint bp = BoardPoint.of(src.getX() - 1, src.getY());
                 if (destination ==null || weightsMap[bp.getX()][bp.getY()].getFactor() > weightsMap[destination.getX()][destination.getY()].getFactor()) {
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
            if (entry.getValue().contains(destination)) {
                routePoint = entry.getKey();
            }
            Main.writeToFile("ROUTE : [" + entry.getKey().getX() + ", " + entry.getKey().getY() + "] CONN: " + entry.getValue().size());
        }

        return routePoint;
    }

    private int calcNewWavePoint(BoardPoint wP, Room roomObj, Map<BoardPoint, Direction> newWavePoints, int moveCount, Direction move, Map<BoardPoint, List<BoardPoint>> routePoints, BoardPoint origin) {
        BoardElement[][] room = roomObj.getRoom();

        int available = 0;
        if (wP.getX() >= 0 && wP.getY() >= 0) {
            if (BoardElement.GOOD_TYPES.contains(room[wP.getX()][wP.getY()])) {
                available++;
            }

            wP = setWeight(wP, roomObj, weightsMap, moveCount);
            if (wP != null) {
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
        if (BoardElement.GOOD_TYPES.contains(room[wP.getX()][wP.getY()]) &&
                (weightMap[wP.getX()][wP.getY()].getMoves() < 0 ||
                        weightMap[wP.getX()][wP.getY()].getMoves() > moveCount)) {
            weightMap[wP.getX()][wP.getY()].setMoves(moveCount);

            List<Snake> enemies = roomObj.getEnemies().parallelStream()
                    .filter(snake -> snake.getBody().getFirst().equals(wP)
                            || snake.getBody().getFirst().equals(wP.shiftBottom())
                            || snake.getBody().getFirst().equals(wP.shiftTop())
                            || snake.getBody().getFirst().equals(wP.shiftLeft())
                            || snake.getBody().getFirst().equals(wP.shiftRight()))
                    .collect(Collectors.toList());
            if (FURY < moveCount && room[wP.getX()][wP.getY()].equals(BoardElement.STONE)) {
                weightMap[wP.getX()][wP.getY()].setScore(ENEMY_HEAD_EVIL.getScore());
            } else if (!enemies.isEmpty()) {
                enemies.forEach(enemy -> {
                    boolean canEat = ((FURY >= moveCount) || (body.size() >= enemy.getBody().size() + 2))
                            && !room[enemy.getFirst().getX()][enemy.getFirst().getY()].equals(ENEMY_HEAD_EVIL)
                            || ((FURY >= moveCount) && (body.size() >= enemy.getBody().size() + 2));
                    if (canEat) {
                        weightMap[wP.getX()][wP.getY()].setScore(room[wP.getX()][wP.getY()].getScore());
                    } else {
                        weightMap[wP.getX()][wP.getY()].setScore(ENEMY_HEAD_EVIL.getScore());
                    }
                });
            } else if (room[wP.getX()][wP.getY()].equals(FURY_PILL)) {
                List<Integer> enemyDistance = roomObj.getEnemies().parallelStream().map(snake -> { return Math.abs(snake.getBody().getFirst().getX() - wP.getX()) + Math.abs(snake.getBody().getFirst().getY() - wP.getY()); }).collect(Collectors.toList());
                enemyDistance.forEach( dst -> {
                    if ((moveCount < dst) && (FURY + 10 < moveCount + dst) && (weightMap[wP.getX()][wP.getY()].getScore() != -1)) {
                        weightMap[wP.getX()][wP.getY()].setScore(FURY_PILL.getScore());
                    } else {
                        weightMap[wP.getX()][wP.getY()].setScore(-1);
                    }
                });
            } else {
                weightMap[wP.getX()][wP.getY()].setScore(room[wP.getX()][wP.getY()].getScore());
            }
            if (weightMap[wP.getX()][wP.getY()].getScore() != ENEMY_HEAD_EVIL.getScore())
                return wP;
            else
                return null;
        }
        return null;
    }

}
