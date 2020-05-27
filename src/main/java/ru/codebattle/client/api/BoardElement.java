package ru.codebattle.client.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public enum BoardElement {
    NONE(' ', 0),         // пустое место
    WALL('☼', -100),         // а это стенка
    START_FLOOR('#', -100),  // место старта змей
    OTHER('?', 0),        // этого ты никогда не увидишь :)

    APPLE('○', 10),        // яблоки надо кушать от них становишься длинее
    STONE('●', 5),        // а это кушать не стоит - от этого укорачиваешься
    FLYING_PILL('©', 0),  // таблетка полета - дает суперсилы
    FURY_PILL('®', 20),    // таблетка ярости - дает суперсилы
    GOLD('$', 10),         // золото - просто очки

    // голова твоей змеи в разных состояниях и напрвлениях
    HEAD_DOWN('▼', 0),
    HEAD_LEFT('◄', 0),
    HEAD_RIGHT('►', 0),
    HEAD_UP('▲', 0),
    HEAD_DEAD('☻', 0),    // этот раунд ты проиграл
    HEAD_EVIL('♥', 0),    // ты скушал таблетку ярости
    HEAD_FLY('♠', 0),     // ты скушал таблетку полета
    HEAD_SLEEP('&', 0),   // твоя змейка ожидает начала раунда

    // хвост твоей змейки
    TAIL_END_DOWN('╙', 0),
    TAIL_END_LEFT('╘', 0),
    TAIL_END_UP('╓', 0),
    TAIL_END_RIGHT('╕', 0),
    TAIL_INACTIVE('~', 0),

    // туловище твоей змейки
    BODY_HORIZONTAL('═', -20),
    BODY_VERTICAL('║', -20),
    BODY_LEFT_DOWN('╗', -20),
    BODY_LEFT_UP('╝', -20),
    BODY_RIGHT_DOWN('╔', -20),
    BODY_RIGHT_UP('╚', -20),

    // змейки противников
    ENEMY_HEAD_DOWN('˅', 40),
    ENEMY_HEAD_LEFT('<', 40),
    ENEMY_HEAD_RIGHT('>', 40),
    ENEMY_HEAD_UP('˄', 40),
    ENEMY_HEAD_DEAD('☺', 0),   // этот раунд противник проиграл
    ENEMY_HEAD_EVIL('♣', -50),   // противник скушал таблетку ярости
    ENEMY_HEAD_FLY('♦', 0),    // противник скушал таблетку полета
    ENEMY_HEAD_SLEEP('ø', 0),  // змейка противника ожидает начала раунда

    // хвосты змеек противников
    ENEMY_TAIL_END_DOWN('¤', -10),
    ENEMY_TAIL_END_LEFT('×', -10),
    ENEMY_TAIL_END_UP('æ', -10),
    ENEMY_TAIL_END_RIGHT('ö', -10),
    ENEMY_TAIL_INACTIVE('*', 0),

    // туловище змеек противников
    ENEMY_BODY_HORIZONTAL('─', 20),
    ENEMY_BODY_VERTICAL('│', 20),
    ENEMY_BODY_LEFT_DOWN('┐', 20),
    ENEMY_BODY_LEFT_UP('┘', 20),
    ENEMY_BODY_RIGHT_DOWN('┌', 20),
    ENEMY_BODY_RIGHT_UP('└', 20);

    final char symbol;
    final int score;

    public static final Set<BoardElement> GOOD_TYPES = new HashSet<>(Arrays.asList(NONE, APPLE, GOLD, FURY_PILL, FLYING_PILL,
            TAIL_END_DOWN,
            TAIL_END_LEFT,
            TAIL_END_UP,
            TAIL_END_RIGHT,
            TAIL_INACTIVE

            // туловище твоей змейки
//            BODY_HORIZONTAL,
//            BODY_VERTICAL,
//            BODY_LEFT_DOWN,
//            BODY_LEFT_UP,
//            BODY_RIGHT_DOWN,
//            BODY_RIGHT_UP
            ));
    public static final Set<BoardElement> EAGER_TYPES = new HashSet<>(Arrays.asList(APPLE, GOLD, FURY_PILL, FLYING_PILL));

    public static final Set<BoardElement> HEAD = new HashSet<>(Arrays.asList(HEAD_DOWN,
            HEAD_LEFT,
            HEAD_RIGHT,
            HEAD_UP,
            HEAD_EVIL,    // ты скушал таблетку ярости
            HEAD_FLY     // ты скушал таблетку полета
            ));

    public static final Set<BoardElement> BODY = new HashSet<>(Arrays.asList(
            BODY_HORIZONTAL,
            BODY_VERTICAL,
            BODY_LEFT_DOWN,
            BODY_LEFT_UP,
            BODY_RIGHT_DOWN,
            BODY_RIGHT_UP
    ));

    public static final Set<BoardElement> TAIL = new HashSet<>(Arrays.asList(
            TAIL_END_DOWN,
            TAIL_END_LEFT,
            TAIL_END_UP,
            TAIL_END_RIGHT
    ));

    public static final Set<BoardElement> ENEMY_HEAD = new HashSet<>(Arrays.asList(ENEMY_HEAD_DOWN,
            ENEMY_HEAD_LEFT,
            ENEMY_HEAD_RIGHT,
            ENEMY_HEAD_UP,
            ENEMY_HEAD_EVIL,   // противник скушал таблетку ярости
            ENEMY_HEAD_FLY    // противник скушал таблетку полета
    ));

    public static final Set<BoardElement> ENEMY_TAIL = new HashSet<>(Arrays.asList(ENEMY_TAIL_END_DOWN,
            ENEMY_TAIL_END_LEFT,
            ENEMY_TAIL_END_UP,
            ENEMY_TAIL_END_RIGHT
    ));

    public static final Set<BoardElement> ENEMY_BODY = new HashSet<>(Arrays.asList(ENEMY_BODY_HORIZONTAL,
            ENEMY_BODY_VERTICAL,
            ENEMY_BODY_LEFT_DOWN,
            ENEMY_BODY_LEFT_UP,
            ENEMY_BODY_RIGHT_DOWN,
            ENEMY_BODY_RIGHT_UP));

    public static final Set<BoardElement> ENEMY = new HashSet<>(Arrays.asList(ENEMY_HEAD_DOWN,
            ENEMY_HEAD_LEFT,
            ENEMY_HEAD_RIGHT,
            ENEMY_HEAD_UP,
            ENEMY_HEAD_EVIL,   // противник скушал таблетку ярости
            ENEMY_HEAD_FLY,    // противник скушал таблетку полета

            // хвосты змеек противников
            ENEMY_TAIL_END_DOWN,
            ENEMY_TAIL_END_LEFT,
            ENEMY_TAIL_END_UP,
            ENEMY_TAIL_END_RIGHT,

            // туловище змеек противников
            ENEMY_BODY_HORIZONTAL,
            ENEMY_BODY_VERTICAL,
            ENEMY_BODY_LEFT_DOWN,
            ENEMY_BODY_LEFT_UP,
            ENEMY_BODY_RIGHT_DOWN,
            ENEMY_BODY_RIGHT_UP));

    @Override
    public String toString() {
        return String.valueOf(symbol);
    }

    public static BoardElement valueOf(char ch) {
        for (BoardElement el : BoardElement.values()) {
            if (el.symbol == ch) {
                return el;
            }
        }
        throw new IllegalArgumentException("No such element for " + ch);
    }
}
