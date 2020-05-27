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

    private static final String SERVER_ADDRESS = "http://codebattle-pro-2020s1.westeurope.cloudapp.azure.com/codenjoy-contest/board/player/wus1ggiwdddwopmlj5zb?code=5908849919158231956&gameName=snakebattle";

    private static FileWriter fileWriter = null;

    public static void main(String[] args) throws URISyntaxException, IOException {
        SnakeBattleClient client = new SnakeBattleClient(SERVER_ADDRESS);
        while (true) {

            client.run(gameBoard -> {
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
                    SnakeAction result = room.moveDecision();
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
                    return new SnakeAction(false, Direction.values()[new Double(Math.random() * 4).intValue()]);
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

    public static void writeToFile(String string) {
        if (fileWriter != null) {
            try {
                fileWriter.write(string + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
