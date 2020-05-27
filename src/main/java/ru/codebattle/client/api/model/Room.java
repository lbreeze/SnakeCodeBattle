package ru.codebattle.client.api.model;

import lombok.Getter;
import lombok.Setter;
import ru.codebattle.client.Main;
import ru.codebattle.client.api.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static ru.codebattle.client.api.BoardElement.ENEMY;
import static ru.codebattle.client.api.BoardElement.ENEMY_HEAD_DEAD;
import static ru.codebattle.client.api.BoardElement.ENEMY_HEAD_SLEEP;

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
            Snake snake = new Snake();
            snake.setBody(new LinkedList<>());
            snake.addFirst(head);
            snake.getBody().addAll(body);
            if (tail != null)
                snake.addLast(tail);

            switch (room[head.getX()][head.getY()]) {
                case HEAD_DOWN:
                    Snake.LAST_MOVE = Direction.DOWN;
                    break;
                case HEAD_UP:
                    Snake.LAST_MOVE = Direction.UP;
                    break;
                case HEAD_LEFT:
                    Snake.LAST_MOVE = Direction.LEFT;
                    break;
                case HEAD_RIGHT:
                    Snake.LAST_MOVE = Direction.RIGHT;
                    break;
                case HEAD_FLY:
                    //snake.fly();
                    break;
                case HEAD_EVIL:
                    //snake.fury();
                    break;
            }
            result.setSnake(snake);

            Main.writeToFile("Enemies: " + enemies.size());
            //enemies = new ArrayList<>();
            for (BoardPoint tailPoint : enemies) {
                snake = new Snake();
                snake.setBody(new LinkedList<>());
                if (result.getEnemies() == null) {
                    result.setEnemies(new ArrayList<>());
                }

                BoardPoint point = BoardPoint.of(tailPoint);
                BoardElement el = room[point.getX()][point.getY()];
                snake.addFirst(point);
                // System.out.println("Enemy part added: " + el.name());
                Direction dir = null;
                do {
                    switch (el) {
                        case ENEMY_TAIL_END_DOWN:
                            point = point.shiftTop();
                            dir = Direction.UP;
                            break;
                        case ENEMY_TAIL_END_UP:
                            point = point.shiftBottom();
                            dir = Direction.DOWN;
                            break;
                        case ENEMY_TAIL_END_LEFT:
                            point = point.shiftRight();
                            dir = Direction.RIGHT;
                            break;
                        case ENEMY_TAIL_END_RIGHT:
                            point = point.shiftLeft();
                            dir = Direction.LEFT;
                            break;
                        case ENEMY_BODY_HORIZONTAL:
                            if (Direction.LEFT.equals(dir)) {
                                point = point.shiftLeft();
                            } else {
                                point = point.shiftRight();
                            }
                            break;
                        case ENEMY_BODY_VERTICAL:
                            if (Direction.UP.equals(dir)) {
                                point = point.shiftTop();
                            } else {
                                point = point.shiftBottom();
                            }
                            break;
                        case ENEMY_BODY_LEFT_DOWN:
                            if (Direction.UP.equals(dir)) {
                                point = point.shiftLeft();
                                dir = Direction.LEFT;
                            } else {
                                point = point.shiftBottom();
                                dir = Direction.DOWN;
                            }
                            break;
                        case ENEMY_BODY_LEFT_UP:
                            if (Direction.DOWN.equals(dir)) {
                                point = point.shiftLeft();
                                dir = Direction.LEFT;
                            } else {
                                point = point.shiftTop();
                                dir = Direction.UP;
                            }
                            break;
                        case ENEMY_BODY_RIGHT_DOWN:
                            if (Direction.UP.equals(dir)) {
                                point = point.shiftRight();
                                dir = Direction.RIGHT;
                            } else {
                                point = point.shiftBottom();
                                dir = Direction.DOWN;
                            }
                            break;
                        case ENEMY_BODY_RIGHT_UP:
                            if (Direction.DOWN.equals(dir)) {
                                point = point.shiftRight();
                                dir = Direction.RIGHT;
                            } else {
                                point = point.shiftTop();
                                dir = Direction.UP;
                            }
                            break;
                        default:
                            System.out.println("ERROR: this is not enemy: " + el.name());
                            break;
                    }

                    el = room[point.getX()][point.getY()];
                    if (!ENEMY.contains(el)) {

                        break;
                    }
                    snake.addFirst(point);
                    // System.out.println("Enemy part added: " + el.name());

                } while (!BoardElement.ENEMY_HEAD.contains(el) || ENEMY_HEAD_DEAD.equals(el) || ENEMY_HEAD_SLEEP.equals(el));

                result.getEnemies().add(snake);
            }
        } else {
            //System.out.println("NEW ROUND!");
            Snake.STONES = 0;
            Snake.FURY = 0;
            Room.MOVE = 0;
            BoardElement.GOOD_TYPES.remove(BoardElement.STONE);
            BoardElement.GOOD_TYPES.removeAll(BoardElement.ENEMY);

            BoardElement.EAGER_TYPES.removeAll(BoardElement.ENEMY_HEAD);
        }

        return result;
    }

    public SnakeAction moveDecision() {
        return snake == null ? null : snake.moveDecision(this);
    }
}