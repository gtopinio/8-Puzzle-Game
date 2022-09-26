/*
 * Topinio, Mark Genesis C. - CMSC170 X-4L
 * Exercises 1-3 (8-Puzzle Game)
 */

package puzzle;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application{

    public static void main(String[] args){
        launch(args);
    }

    public void start(Stage stage) {
          // Starting the 8-Puzzle Board Application
          GameBoard board = new GameBoard();
          board.setStage(stage);
    }
}
