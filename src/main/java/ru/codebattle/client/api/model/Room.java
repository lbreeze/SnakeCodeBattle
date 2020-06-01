package ru.codebattle.client.api.model;

import lombok.Getter;
import lombok.Setter;
import ru.codebattle.client.Main;
import ru.codebattle.client.api.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static ru.codebattle.client.api.BoardElement.*;

public class Room {

    public static int SCORE = 0;
    public static int MOVE = 0;

    @Getter
    @Setter
    private int width;

    @Getter
    @Setter
    private int height;

    @Getter
    @Setter
    private BoardElement[][] room;

    @Getter
    @Setter
    private Snake snake = null;

    @Getter
    @Setter
    private List<Snake> enemies = new ArrayList<>();

    public static Room createFromBoard(GameBoard board) {
        Room result = new Room();
        result.setWidth(board.size());
        result.setHeight(board.size());

        BoardElement[][] room = new BoardElement[result.getWidth()][result.getHeight()];
        result.setRoom(room);

        BoardPoint head = null, tail = null;
        List<BoardPoint> body = new LinkedList<>();

        List<BoardPoint> enemies = new LinkedList<>();

        for (int i = 0; i < result.getWidth() * result.getHeight(); i++) {
            BoardPoint point = new BoardPoint(i % result.getWidth(), i / result.getWidth());
            BoardElement element = BoardElement.valueOf(board.getBoardString().charAt(i));
            room[point.getX()][point.getY()] = element;

            if (BoardElement.HEAD.contains(element)) {
                head = point;
            }

            if (BoardElement.BODY.contains(element)) {
                body.add(point);
            }

            if (BoardElement.TAIL.contains(element)) {
                tail = point;
            }

            if (BoardElement.ENEMY_TAIL.contains(element)) {
                enemies.add(point);
            }
        }

        if (head != null) {
            result.setSnake(createSnake(tail, result));

            if (result.getEnemies() == null) {
                result.setEnemies(new ArrayList<>());
            }
            for (BoardPoint tailPoint : enemies) {
                result.getEnemies().add(createSnake(tailPoint, result));
            }

        } else {
            //System.out.println("NEW ROUND!");
            Room.MOVE = 0;

            //BoardElement.EAGER_TYPES.removeAll(BoardElement.ENEMY_HEAD);
            //BoardElement.EAGER_TYPES.removeAll(BoardElement.ENEMY_BODY);
        }

        return result;
    }

    private static Snake createSnake(BoardPoint tailPoint, Room roomObj) {
        BoardElement[][] room = roomObj.getRoom();

        Snake snake = null;
        if (tailPoint != null) {
            snake = new Snake();
            snake.setBody(new LinkedList<>());
            snake.setWeightsMap(new Weight[roomObj.getWidth()][roomObj.getHeight()]);

            BoardPoint point = BoardPoint.of(tailPoint);
            BoardElement el = room[point.getX()][point.getY()];
            snake.addFirst(point);
            // System.out.println("Enemy part added: " + el.name());
            Direction dir = null;
            do {
                switch (el) {
                    case ENEMY_TAIL_END_DOWN:
                    case TAIL_END_DOWN:
                        point = point.shiftTop();
                        dir = Direction.UP;
                        break;
                    case ENEMY_TAIL_END_UP:
                    case TAIL_END_UP:
                        point = point.shiftBottom();
                        dir = Direction.DOWN;
                        break;
                    case ENEMY_TAIL_END_LEFT:
                    case TAIL_END_LEFT:
                        point = point.shiftRight();
                        dir = Direction.RIGHT;
                        break;
                    case ENEMY_TAIL_END_RIGHT:
                    case TAIL_END_RIGHT:
                        point = point.shiftLeft();
                        dir = Direction.LEFT;
                        break;
                    case ENEMY_BODY_HORIZONTAL:
                    case BODY_HORIZONTAL:
                        if (Direction.LEFT.equals(dir)) {
                            point = point.shiftLeft();
                        } else {
                            point = point.shiftRight();
                        }
                        break;
                    case ENEMY_BODY_VERTICAL:
                    case BODY_VERTICAL:
                        if (Direction.UP.equals(dir)) {
                            point = point.shiftTop();
                        } else {
                            point = point.shiftBottom();
                        }
                        break;
                    case ENEMY_BODY_LEFT_DOWN:
                    case BODY_LEFT_DOWN:
                        if (Direction.UP.equals(dir)) {
                            point = point.shiftLeft();
                            dir = Direction.LEFT;
                        } else {
                            point = point.shiftBottom();
                            dir = Direction.DOWN;
                        }
                        break;
                    case ENEMY_BODY_LEFT_UP:
                    case BODY_LEFT_UP:
                        if (Direction.DOWN.equals(dir)) {
                            point = point.shiftLeft();
                            dir = Direction.LEFT;
                        } else {
                            point = point.shiftTop();
                            dir = Direction.UP;
                        }
                        break;
                    case ENEMY_BODY_RIGHT_DOWN:
                    case BODY_RIGHT_DOWN:
                        if (Direction.UP.equals(dir)) {
                            point = point.shiftRight();
                            dir = Direction.RIGHT;
                        } else {
                            point = point.shiftBottom();
                            dir = Direction.DOWN;
                        }
                        break;
                    case ENEMY_BODY_RIGHT_UP:
                    case BODY_RIGHT_UP:
                        if (Direction.DOWN.equals(dir)) {
                            point = point.shiftRight();
                            dir = Direction.RIGHT;
                        } else {
                            point = point.shiftTop();
                            dir = Direction.UP;
                        }
                        break;
                    default:
                        if (!HEAD.contains(el) && !ENEMY_HEAD.contains(el)) {
                            System.out.println("ERROR: this is not snake: " + el.name());
                        }
                        break;
                }

                el = room[point.getX()][point.getY()];
                if (!ENEMY.contains(el) && !TAIL.contains(el) && !BODY.contains(el) && !HEAD.contains(el)) {

                    break;
                }
                snake.addFirst(point);

            } while (!(BoardElement.ENEMY_HEAD.contains(el) || BoardElement.HEAD.contains(el))
                    || ENEMY_HEAD_DEAD.equals(el)
                    || ENEMY_HEAD_SLEEP.equals(el)
                    //|| HEAD_DEAD.equals(el)
                    || HEAD_SLEEP.equals(el));

//            StringBuilder snakeStr = new StringBuilder();
//            snake.getBody().forEach(pt -> {
//                snakeStr.append(pt.toString());
//            });
            // Main.writeToFile("SNAKE", snakeStr.toString());
        }
        return snake;
    }

    public void mergeInfo(Room previousRoom) {
        Snake prevSnake = previousRoom.getSnake();
        if (prevSnake != null) {
            snake.setFury(prevSnake.getFury());
            snake.setStones(prevSnake.getStones());
            snake.setLastMove(prevSnake.getLastMove());
        }

        List<Snake> prevEnemies = previousRoom.getEnemies();
        enemies.parallelStream().forEach(enemy -> {
            prevEnemies.parallelStream()
                    .filter(prevEnemy -> prevEnemy.getBody().contains(enemy.getBody().getLast()))
                    .findFirst()
                    .ifPresent(prev -> {
                        if (previousRoom.getRoom()[enemy.getBody().getFirst().getX()][enemy.getBody().getFirst().getY()].equals(FURY_PILL)) {
                            prev.setFury(prev.getFury() + Snake.FURY_TIME);
                        }
                        enemy.setFury(prev.getFury() == 0 ? 0 : prev.getFury() - 1);

                        Direction lastMove = null;

                        BoardPoint prevHead = prev.getBody().getFirst();
                        BoardPoint enemyHead = enemy.getBody().getFirst();
                        if (enemyHead.getX() == prevHead.getX() - 1) {
                            lastMove = Direction.LEFT;
                        } else if (enemyHead.getX() == prevHead.getX() + 1) {
                            lastMove = Direction.RIGHT;
                        } else if (enemyHead.getY() == prevHead.getY() - 1) {
                            lastMove = Direction.UP;
                        } else if (enemyHead.getY() == prevHead.getY() + 1) {
                            lastMove = Direction.DOWN;
                        }
                        enemy.setLastMove(lastMove);

                        Main.writeToFile("ENEMY", "SIZE: " + enemy.getBody().size() + " FURY: " + enemy.getFury());
                    });
        });
    }

    public SnakeAction moveDecision() {
        return snake == null ? null : snake.moveDecision(this);
    }
}