package ru.codebattle.client;

import ru.codebattle.client.api.Direction;
import ru.codebattle.client.api.SnakeAction;
import ru.codebattle.client.api.model.Room;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    private static final String SERVER_ADDRESS = "";

    private static FileWriter fileWriter = null;

    private static Room previousRoom = null;

    private static long lastFrameTime = 0;

    public static void main(String[] args) throws URISyntaxException, IOException {
        SnakeBattleClient client = new SnakeBattleClient(SERVER_ADDRESS);
        while (true) {

            client.run(gameBoard -> {
                long time = System.currentTimeMillis();
                if ((lastFrameTime != 0) && ((time - lastFrameTime >= 1100) || (time - lastFrameTime <= 900))) {
                    System.out.println("Frame delay at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time)) +": " + (time - lastFrameTime) + "ms");
                }
                lastFrameTime = time;
                try {
                    Room room = Room.createFromBoard(gameBoard);
                    if (room.getSnake() != null) {
                        try {
                            if (fileWriter == null) {
                                String gameFile = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
                                System.out.println(gameFile);
                                File f = new File(gameFile + ".log");
                                f.createNewFile();
                                fileWriter = new FileWriter(f, StandardCharsets.UTF_8);
                            }

                            gameBoard.printBoard(fileWriter);
                            fileWriter.write("MOVE: " + Room.MOVE++ + "\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (previousRoom != null) {
                            room.mergeInfo(previousRoom);
                        }
                        previousRoom = room;

                        SnakeAction result = room.moveDecision();
                        if (System.currentTimeMillis() - time > 200)
                            System.out.println("Processing time: " + (System.currentTimeMillis() - time) + "ms");
                        return result;
                    } else {
                        if (fileWriter != null) {
                            try {
                                fileWriter.flush();
                                fileWriter.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        fileWriter = null;
                        Room.MOVE = 0;
                        Room.SCORE = 0;
                        previousRoom = null;
                        return new SnakeAction(false, Direction.values()[new Double(Math.random() * 4).intValue()]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            });

            while (!client.isShouldExit()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            client = new SnakeBattleClient(SERVER_ADDRESS);
        }
        //System.in.read();

        //client.initiateExit();
    }

    public static void writeToFile(String cat, String string) {
        if (fileWriter != null) {
            try {
                fileWriter.write(cat + ": "+ string + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
