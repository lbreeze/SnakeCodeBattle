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

    public static int FURY_TIME = 10; // длительность действия таблетки

    @Getter
    @Setter
    private Direction lastMove = Direction.RIGHT;

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
    @Setter
    private Weight[][] weightsMap;

    @Getter
    private BoardPoint destination;

    public void eatStone() {
        stones++;
    }

    public void fury() {
        //BoardElement.EAGER_TYPES.add(BoardElement.STONE);

        //BoardElement.EAGER_TYPES.addAll(BoardElement.ENEMY_HEAD);
        //BoardElement.EAGER_TYPES.addAll(ENEMY_BODY);
    }

    public void peace() {
        if (fury > 0) {
            fury--;
            //if (fury == 0) {
            //   BoardElement.EAGER_TYPES.remove(BoardElement.STONE);
//
            //   BoardElement.EAGER_TYPES.removeAll(BoardElement.ENEMY_HEAD);
            //    BoardElement.EAGER_TYPES.removeAll(ENEMY_BODY);
            //}
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
        Direction result = lastMove;
        boolean act = false;//fury >= weightsMap[getBody().getLast().getX()][getBody().getLast().getY()].getMoves() && stones > 0;

        //System.out.println(lastMove.name() + " " +  room.getRoom()[body.getFirst().getX()][body.getFirst().getY()].name());
        try {
            peace();


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

            BoardPoint tailPoint = getBody().getLast();
            if (stones > 0 && (BoardElement.ENEMY_HEAD.contains(room.getRoom()[tailPoint.getX() - 1][tailPoint.getY()]) ||
                    BoardElement.ENEMY_HEAD.contains(room.getRoom()[tailPoint.getX() + 1][tailPoint.getY()]) ||
                    BoardElement.ENEMY_HEAD.contains(room.getRoom()[tailPoint.getX()][tailPoint.getY() - 1]) ||
                    BoardElement.ENEMY_HEAD.contains(room.getRoom()[tailPoint.getX()][tailPoint.getY() + 1]))) {
                switch (lastMove) {
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
                fury += FURY_TIME;
                fury();
            }

            //if (room.getEnemies().size() == 1 && fury == 0) {
            //if (getBody().size() > room.getEnemies().get(0).getBody().size() + 2 && room.getEnemies().get(0).getFury() == 0) {
            //BoardElement.EAGER_TYPES.addAll(ENEMY_HEAD);
            //} else {
            //BoardElement.EAGER_TYPES.removeAll(ENEMY_HEAD);
            //}
            //}

            lastMove = result;

            Main.writeToFile("ACTION", "FROM [" + body.getFirst().getX() + ", " + body.getFirst().getY() + "] -> " + new SnakeAction(act, result) + " -> [" + destination.getX() + ", " + destination.getY() + "] FURY:" + fury);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SnakeAction(act, result);
    }

    private void findDestinationPointOnWM(BoardPoint src, Map<BoardPoint, List<BoardPoint>> routePoints, Room roomObj) {
        BoardElement[][] room = roomObj.getRoom();

        List<Set<BoardPoint>> areaPoints = new ArrayList<>();
        for (List<BoardPoint> pointsList : routePoints.values()) {
            if (!areaPoints.isEmpty()) {
                boolean contains = false;
                for (Set<BoardPoint> areaPointSet : areaPoints) {
                    contains = !Collections.disjoint(areaPointSet, pointsList);

                    if (contains) {
                        areaPointSet.addAll(pointsList);
                    }
                }

                if (!contains) {
                    areaPoints.add(new HashSet<>(pointsList));
                }

            } else {
                areaPoints.add(new HashSet<>(pointsList));
            }
        }

        destination = null;
        for (int col = 0; col < roomObj.getWidth(); col++) {
            for (int row = 0; row < roomObj.getHeight(); row++) {

                for (Set<BoardPoint> areaPointsList : areaPoints) {
                    if (areaPointsList.contains(BoardPoint.of(col, row))) {
                        if (areaPointsList.size() > weightsMap[col][row].getConnected())
                            weightsMap[col][row].setConnected(areaPointsList.size());
                    }
                }

                for (Snake enemy : roomObj.getEnemies()) {
                    if (enemy.getWeightsMap()[col][row] != null) {
                        int proximity = enemy.getWeightsMap()[col][row].getMoves();
                        //Math.abs(weightsMap[enemy.getBody().getFirst().getX()][enemy.getBody().getFirst().getY()].getMoves() - weightsMap[col][row].getMoves());
                        boolean canEat = ((fury > weightsMap[col][row].getMoves()) && (body.size() >= enemy.getBody().size() + 2))
                                || (((fury > weightsMap[col][row].getMoves()) || (body.size() >= enemy.getBody().size() + 2)) && (enemy.getFury() <= enemy.getWeightsMap()[col][row].getMoves()));
                        if (room[col][row].equals(FURY_PILL) && weightsMap[col][row].getMoves() <= proximity) {
                            canEat = ((body.size() >= enemy.getBody().size() + 2) || (enemy.getFury() == 0));
                        }
                        if ((weightsMap[col][row].getEnemyProximity() > proximity) && !canEat) {
                            weightsMap[col][row].setEnemyProximity(proximity);
                        }
                    }
                }

                if (//BoardElement.EAGER_TYPES.contains(room[col][row]) &&
                        weightsMap[col][row].getScore() > 1 && weightsMap[col][row].getMoves() > 0 && weightsMap[col][row].getFactor(getBody().size()) > 0) {
                    if (destination == null) {
                        Main.writeToFile("WEIGHT", room[col][row].name() + " [" + col + ", " + row + "] = " + weightsMap[col][row].getFactor(getBody().size())
                                + weightsMap[col][row].toString());
                        destination = BoardPoint.of(col, row);
                    } else {
                        if (weightsMap[col][row].getFactor(getBody().size()) > weightsMap[destination.getX()][destination.getY()].getFactor(getBody().size())) {
                            destination = BoardPoint.of(col, row);
                        }

                        Main.writeToFile("WEIGHT", room[col][row].name() + " [" + col + ", " + row + "] = " + weightsMap[col][row].getFactor(getBody().size())
                                + weightsMap[col][row].toString());
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
                if (destination == null || weightsMap[bp.getX()][bp.getY()].getFactor(getBody().size()) > weightsMap[destination.getX()][destination.getY()].getFactor(getBody().size())) {
                    Main.writeToFile("WEIGHT", room[bp.getX()][bp.getY()].name() + " " + bp.toString() + " = " + weightsMap[bp.getX()][bp.getY()].getFactor(getBody().size())
                            + weightsMap[bp.getX()][bp.getY()].toString());
                    destination = BoardPoint.of(bp);
                }
            }
            if (destination == null) {
                BoardPoint bp = BoardPoint.of(src.getX() - 1, src.getY());
                if (destination == null || weightsMap[bp.getX()][bp.getY()].getFactor(getBody().size()) > weightsMap[destination.getX()][destination.getY()].getFactor(getBody().size())) {
                    Main.writeToFile("WEIGHT", room[bp.getX()][bp.getY()].name() + " " + bp.toString() + " = " + weightsMap[bp.getX()][bp.getY()].getFactor(getBody().size())
                            + weightsMap[bp.getX()][bp.getY()].toString());
                    destination = bp;
                }
            }
        }
    }

    private void calcWeightMap(BoardPoint src, Room roomObj, boolean movesOnly, Map<BoardPoint, List<BoardPoint>> routePoints) {
        for (Weight[] moveCol : weightsMap)
            for (int i = 0; i < moveCol.length; i++)
                moveCol[i] = new Weight();

        weightsMap[src.getX()][src.getY()].setMoves(0);
        weightsMap[src.getX()][src.getY()].setScore(0);

        Map<BoardPoint, Direction> wavePoints = new HashMap<>();
        wavePoints.put(BoardPoint.of(src), lastMove);

        boolean first = true;
        int moveCount = 1;
        while (!wavePoints.isEmpty()) {
            Map<BoardPoint, Direction> newWavePoints = new HashMap<>();
            for (BoardPoint wavePoint : wavePoints.keySet()) {

                if (wavePoints.get(wavePoint) != Direction.RIGHT) {
                    calcNewWavePoint(
                            BoardPoint.of(wavePoint.getX() - 1, wavePoint.getY()),
                            roomObj,
                            newWavePoints,
                            moveCount,
                            Direction.LEFT,
                            routePoints,
                            first ? null : wavePoint, movesOnly);
                }


                if (wavePoints.get(wavePoint) != Direction.LEFT) {
                    calcNewWavePoint(
                            BoardPoint.of(wavePoint.getX() + 1, wavePoint.getY()),
                            roomObj,
                            newWavePoints,
                            moveCount,
                            Direction.RIGHT,
                            routePoints,
                            first ? null : wavePoint, movesOnly);
                }

                if (wavePoints.get(wavePoint) != Direction.DOWN) {
                    calcNewWavePoint(
                            BoardPoint.of(wavePoint.getX(), wavePoint.getY() - 1),
                            roomObj,
                            newWavePoints,
                            moveCount,
                            Direction.UP,
                            routePoints,
                            first ? null : wavePoint, movesOnly);
                }

                if (wavePoints.get(wavePoint) != Direction.UP) {
                    calcNewWavePoint(
                            BoardPoint.of(wavePoint.getX(), wavePoint.getY() + 1),
                            roomObj,
                            newWavePoints,
                            moveCount,
                            Direction.DOWN,
                            routePoints,
                            first ? null : wavePoint, movesOnly);
                }

//                if (lastMove != null) {
//                    lastMove = null;
//                }
            }
            first = false;
            wavePoints.clear();
            wavePoints.putAll(newWavePoints);
            moveCount++;
        }
    }

    private BoardPoint findDestinationRoute(BoardPoint src, Room roomObj) {
        Map<BoardPoint, List<BoardPoint>> routePoints = new HashMap<>();

        roomObj.getEnemies().parallelStream().forEach(enemy -> enemy.calcWeightMap(enemy.getBody().getFirst(), roomObj, true, new HashMap<>()));

        calcWeightMap(src, roomObj, false, routePoints);

        findDestinationPointOnWM(src, routePoints, roomObj);

        BoardPoint routePoint = null;
        for (Map.Entry<BoardPoint, List<BoardPoint>> entry : routePoints.entrySet()) {
            if (entry.getValue().contains(destination) && (weightsMap[entry.getKey().getX()][entry.getKey().getY()].getFactor(getBody().size()) > 0)) {
                if (routePoint == null || weightsMap[routePoint.getX()][routePoint.getY()].getFactor(getBody().size()) < weightsMap[entry.getKey().getX()][entry.getKey().getY()].getFactor(getBody().size())) {
                    routePoint = entry.getKey();
                }
            }
            try {
                Main.writeToFile("ROUTE", entry.getKey().toString() + " WEIGHT: " + weightsMap[entry.getKey().getX()][entry.getKey().getY()].getFactor(getBody().size())
                        + weightsMap[entry.getKey().getX()][entry.getKey().getY()].toString()
                        + " DEST: " + entry.getValue().contains(destination));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (routePoint == null) {
            for (Map.Entry<BoardPoint, List<BoardPoint>> entry : routePoints.entrySet()) {
                if (routePoint == null || weightsMap[routePoint.getX()][routePoint.getY()].getFactor(getBody().size()) < weightsMap[entry.getKey().getX()][entry.getKey().getY()].getFactor(getBody().size())) {
                    routePoint = entry.getKey();
                }
            }
        }

        return routePoint;
    }

    private void calcNewWavePoint(BoardPoint wP, Room roomObj, Map<BoardPoint, Direction> newWavePoints, int moveCount, Direction move, Map<BoardPoint, List<BoardPoint>> routePoints, BoardPoint origin, boolean movesOnly) {
        if ((wP.getX() >= 0) && (wP.getY() >= 0) && (wP.getX() < roomObj.getWidth()) && (wP.getY() < roomObj.getHeight())) {
            if (origin == null) {
                routePoints.put(wP, new ArrayList<>());
            }

            wP = calcWeight(wP, roomObj, weightsMap, moveCount, movesOnly);
            if (wP != null) {
                newWavePoints.put(wP, move);
                if (origin == null) {
                    routePoints.get(wP).add(wP);
                } else {
                    for (List<BoardPoint> origins : routePoints.values()) {
                        if (origins.contains(origin)) {
                            origins.add(wP);
                        }
                    }
                }
            }
        }
    }

    private BoardPoint calcWeight(BoardPoint wP, Room roomObj, Weight[][] weightMap, int moveCount, boolean movesOnly) {
        BoardElement[][] room = roomObj.getRoom();
        if ((weightMap[wP.getX()][wP.getY()].getMoves() < 0 ||
                weightMap[wP.getX()][wP.getY()].getMoves() >= moveCount)) {
            weightMap[wP.getX()][wP.getY()].setMoves(moveCount);

            if (!movesOnly) {

/*
            roomObj.getEnemies().forEach(enemy -> {
                int proximity = Math.abs(enemy.getFirst().getX() - wP.getX()) + Math.abs(enemy.getFirst().getY() - wP.getY());
                boolean canEat = (((fury >= moveCount) || (body.size() >= enemy.getBody().size() + 2))
                        && (enemy.getFury() == 0))
                        || ((fury >= moveCount) && (body.size() >= enemy.getBody().size() + 2));
                if (weightMap[wP.getX()][wP.getY()].getEnemyProximity() < proximity && !canEat) {
                    weightMap[wP.getX()][wP.getY()].setEnemyProximity(proximity);
                }
            });
*/

                Snake targetEnemy = roomObj.getEnemies().parallelStream()
                        .filter(snake ->
                                snake.getBody().getFirst().equals(wP)
                        || snake.getBody().getFirst().equals(wP.shiftBottom())
                                        || snake.getBody().getFirst().equals(wP.shiftTop())
                                        || snake.getBody().getFirst().equals(wP.shiftLeft())
                                        || snake.getBody().getFirst().equals(wP.shiftRight())
                        )
                        .findFirst().orElse(null);

                if (room[wP.getX()][wP.getY()].equals(BoardElement.WALL)) {
                    weightMap[wP.getX()][wP.getY()].setScore(WALL.getScore());
                    return null;
                } else if (room[wP.getX()][wP.getY()].equals(BoardElement.STONE)) {
                    if (fury < moveCount && body.size() < 5) {
                        weightMap[wP.getX()][wP.getY()].setScore(WALL.getScore());
                        return null;
                    } else {
                        if (fury >= moveCount) {
                            weightMap[wP.getX()][wP.getY()].setScore(-STONE.getScore() / 10); // 3
                            return wP;
                        } else {
                            weightMap[wP.getX()][wP.getY()].setScore(STONE.getScore());
                            return null;
                        }
                    }
                } else if (BoardElement.ENEMY_TAIL.contains(room[wP.getX()][wP.getY()])) {
                    if (fury >= moveCount) {
                        weightMap[wP.getX()][wP.getY()].setScore(1);

                    } else {
                        weightMap[wP.getX()][wP.getY()].setScore(-1);
                    }
                } else if (BoardElement.ENEMY_BODY.contains(room[wP.getX()][wP.getY()])) {
                    if (fury >= moveCount) {
                        Snake snake = roomObj.getEnemies().parallelStream().filter(enemy -> enemy.getBody().contains(wP)).findFirst().orElse(null);
                        if (snake != null)
                            weightMap[wP.getX()][wP.getY()].setScore(room[wP.getX()][wP.getY()].getScore() * (snake.getBody().size() - snake.getBody().indexOf(wP) - 1));
                        else
                            Main.writeToFile("ENEMY", wP.toString() + " not found in enemy body.");
                    } else {
                        weightMap[wP.getX()][wP.getY()].setScore(ENEMY_HEAD_EVIL.getScore());
                    }
                } else if (targetEnemy != null) {
                    if (targetEnemy.getBody().getFirst().equals(wP)) {
                        boolean canEat = (((fury >= moveCount) || (body.size() >= targetEnemy.getBody().size() + 2)) && (targetEnemy.getFury() == 0))
                        || ((fury >= moveCount) && (body.size() >= targetEnemy.getBody().size() + 2));
                        if (canEat) {
                            weightMap[wP.getX()][wP.getY()].setScore(30 * targetEnemy.getBody().size());
                            weightMap[wP.getX()][wP.getY()].setConnected(getBody().size()); // ignore closed area, always have exit as enemy has and i have an enter
                        } else {
                            weightMap[wP.getX()][wP.getY()].setScore(ENEMY_HEAD_EVIL.getScore());
                            return null;
                        }
                    } else {
                        boolean canEat = (fury >= moveCount) && ((body.size() >= targetEnemy.getBody().size() + 2) || (targetEnemy.getFury() == 0));
                        if (canEat) {
                            if (weightMap[wP.getX()][wP.getY()].getScore() >= -1) {
                                weightMap[wP.getX()][wP.getY()].setScore(10 * targetEnemy.getBody().size());
                                if (weightMap[wP.getX()][wP.getY()].getConnected() < targetEnemy.getBody().size())
                                    weightMap[wP.getX()][wP.getY()].setConnected(targetEnemy.getBody().size()); // have a connected area with enemy body, i can eat it
                            }
                        } else {
                            weightMap[wP.getX()][wP.getY()].setScore(ENEMY_HEAD_EVIL.getScore());
                            return null;
                        }
                    }
/*
                    boolean canEat = (fury >= moveCount) && ((body.size() >= targetEnemy.getBody().size() + 2) || (targetEnemy.getFury() == 0));
                    if (canEat) {
                        if (targetEnemy.getBody().getFirst().equals(wP)) {
                            weightMap[wP.getX()][wP.getY()].setScore(30 * targetEnemy.getBody().size());
                            weightMap[wP.getX()][wP.getY()].setConnected(getBody().size()); // ignore closed area, always have exit as enemy has and i have an enter
                        } else {
                            if (weightMap[wP.getX()][wP.getY()].getScore() >= -1) {
                                weightMap[wP.getX()][wP.getY()].setScore(10 * targetEnemy.getBody().size());
                                if (weightMap[wP.getX()][wP.getY()].getConnected() < targetEnemy.getBody().size())
                                    weightMap[wP.getX()][wP.getY()].setConnected(targetEnemy.getBody().size()); // have a connected area with enemy body, i can eat it
                            }
                        }
                    } else {
                        weightMap[wP.getX()][wP.getY()].setScore(ENEMY_HEAD_EVIL.getScore());
                        return null;
                    }
*/
            } else if (room[wP.getX()][wP.getY()].equals(FURY_PILL)) {
                List<Integer> enemyDistance = roomObj.getEnemies().parallelStream().map(snake -> {
                    return snake.getWeightsMap()[wP.getX()][wP.getY()].getMoves();
                }).collect(Collectors.toList());
                enemyDistance.forEach(dst -> {
                    if ((moveCount <= dst) && (fury < 2 * dst) && (fury + FURY_TIME >= dst / 2)) {
                        weightMap[wP.getX()][wP.getY()].setScore(FURY_PILL.getScore());
                    } else {
                        weightMap[wP.getX()][wP.getY()].setScore(1);
                    }
                });
                } else if (BoardElement.BODY.contains(room[wP.getX()][wP.getY()])) {
                    int cutSize = getBody().size() - getBody().indexOf(wP) - 1;
                    weightMap[wP.getX()][wP.getY()].setScore(room[wP.getX()][wP.getY()].getScore() * cutSize);
                } else {
                    weightMap[wP.getX()][wP.getY()].setScore(room[wP.getX()][wP.getY()].getScore());
                }
            } else {
                weightMap[wP.getX()][wP.getY()].setScore(room[wP.getX()][wP.getY()].getScore());
            }
            return weightMap[wP.getX()][wP.getY()].getScore() < 0 ? null : wP;
        }
        return null;
    }

}
