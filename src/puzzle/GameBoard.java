package puzzle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.Collectors;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class GameBoard {
    // Application components
    private Scene scene;
	private Stage stage;
	private Group root;
	private Canvas canvas;
    private String filePath;

    private Text win; // win prompt if puzzle is solvable and player got the correct answer
    private Text sol;
    private Text pathCostText;
    private Boolean noLongerClickable;
    private Button solutionBtn;

    // Puzzle components
	private GridPane map;
	private int[][] gameBoard;
    private Integer pathCost;
    private String algoChoice;
    private String puzzlePrompt; // this holds the prompt whether or not the puzzle is solvable
    private State initState;    // this holds the initial puzzle board
    private ArrayList<Element> pieceCells;
    private ArrayList<Character> solution;  // This should hold the expected solution for the initial puzzle
    private ArrayList<State> trackStates;
    private ArrayList<State> shownStates;

    public final static int[][] goalPuzzle = { {1,2,3}, {4,5,6}, {7,8,0}}; // This is the actual solution for the 8-puzzle game

    // GUI and GridPane size specifications
    public final static int MAX_CELLS = 9;
    public final static int MAP_NUM_ROWS = 3;
    public final static int MAP_NUM_COLS = 3;
    public final static int MAP_WIDTH = 700;
    public final static int MAP_HEIGHT = 700;
    public final static int CELL_WIDTH = 60;
    public final static int CELL_HEIGHT = 70;
    public final static int WINDOW_WIDTH = 400;
    public final static int WINDOW_HEIGHT = 500;

    // Constructor
	public GameBoard() {
		this.root = new Group();
		this.scene = new Scene(root, GameBoard.WINDOW_WIDTH,GameBoard.WINDOW_HEIGHT, Color.PALEGREEN);
		this.canvas = new Canvas(GameBoard.WINDOW_WIDTH,GameBoard.WINDOW_HEIGHT);
        
		this.map = new GridPane(); // Main layout for the game
		this.pieceCells = new ArrayList<Element>(); // List to hold the pattern of the Grid Pane
        this.solution = new ArrayList<Character>(); // List to hold the action movements for the puzzle
        this.trackStates = new ArrayList<State>();
        this.shownStates = new ArrayList<State>();
        this.noLongerClickable = false;
		this.gameBoard = new int[GameBoard.MAP_NUM_ROWS][GameBoard.MAP_NUM_COLS];

	}

    // getters

    int[][] getGameBoard(){
        return this.gameBoard;
    }

    ArrayList<Element> getPieceCells(){
        return this.pieceCells;
    }

    Boolean notClickable(){
        return this.noLongerClickable;
    }

    // setters
    void setWinPrompt(String newPrompt){
        Font promptFont = Font.font("Tw Cen MT",FontWeight.NORMAL,25);
        win = new Text(newPrompt);
        win.setFont(promptFont);
        win.setLayoutX(95); win.setLayoutY(85);
        this.root.getChildren().addAll(win);
        
    }

    // sets the tiles unclickable if user opts for solution
    void setNoLongerClickable(){
        this.noLongerClickable = true;
    }

    // removes the win prompt and it's used whenever the current board doesn't match the solution board
    void removeWinPrompt(){
        this.root.getChildren().remove(this.win);
    }

    // sets the file path of the selected input file
    private void setFilePath(String path){
        this.filePath = path;
    }

    // sets the solution button
    private void setSolBtn(){
        Font btnFont = Font.font("Tw Cen MT",FontWeight.NORMAL,20);
        // Adding a solution button
        solutionBtn = new Button("Solution");
        solutionBtn.setFont(btnFont); solutionBtn.setLayoutX(220); solutionBtn.setLayoutY(350);
        this.addEventHandler(solutionBtn);
    }

    // removes the solution button
    private void removeSolutionBtn(){
        this.root.getChildren().remove(this.solutionBtn);
    }

    // clears the root node of the scene
    private void clearRoot(){
        this.root.getChildren().clear();
    }

    // method to display the solution and path cost on screen
    private void setSolutionPrompt(){
        Font promptFont = Font.font("Tw Cen MT",FontWeight.NORMAL,22);
        // Solution text
        sol = new Text(this.solution.stream().map(Object::toString).collect(Collectors.joining(" ")));
        sol.setFont(promptFont);
        // sol.setLayoutX(140); sol.setLayoutY(420);

        // Path Cost Text
        pathCostText = new Text("Path Cost: "+String.valueOf(this.pathCost));
        pathCostText.setFont(promptFont);
        pathCostText.setLayoutX(130); pathCostText.setLayoutY(480);

        // Text t = new Text("U R D L U R D L U R D L U R D L U R D L U R D L U R D L U R D L U R D L U R D L U R D L U R D L U R D L U R D L U R D L U R D L");
        
        // Setting up the scroll pane for displaying solution
        ScrollPane sp = new ScrollPane();
        sp.setLayoutX(0); sp.setLayoutY(400);
        sp.setPrefWidth(400);
        sp.setPrefHeight(40);
        sp.setContent(sol);

        this.root.getChildren().addAll(pathCostText, sp);
    }
    
    private void clearSolutionPrompt(){
        this.root.getChildren().removeAll(this.sol, this.pathCostText);
    }
    

    // method to add the stage elements
    public void setStage(Stage stage) {
        this.stage = stage;

        this.initGameBoard();
        this.createMap(this.gameBoard);

        // initializing the initial node
        this.initState = new State(this.gameBoard, State.actionNotApplicable, null);

        // checking if puzzle is solvable
        setPromptValues();
        
        this.setSolBtn();
        // Adding a drop down list or choice box for the algorithms
        ChoiceBox<String> solutionBox = new ChoiceBox<>();
        solutionBox.getItems().addAll("BFS", "DFS", "A*");
        solutionBox.setValue("BFS"); solutionBox.setStyle("-fx-font: 20px \"Tw Cen MT\";");
        solutionBox.setLayoutX(90); solutionBox.setLayoutY(350);
        
        // Extracting the values of the choice box
        this.solutionBtn.setOnAction(e -> getChoice(solutionBox));

        // Adding a select file button
        Font btnFont = Font.font("Tw Cen MT",FontWeight.NORMAL,16);
        Button selectBtn = new Button("Select a File");
        selectBtn.setFont(btnFont); selectBtn.setLayoutX(110); selectBtn.setLayoutY(8);
        this.addEventHandler(selectBtn);

        // Adding a reset button
        Button resetBtn = new Button("Reset");
        resetBtn.setFont(btnFont); resetBtn.setLayoutX(220); resetBtn.setLayoutY(8);
        this.addEventHandler(resetBtn);
        
        // set stage elements here
        this.root.getChildren().add(this.canvas);
        this.root.getChildren().add(this.map);
        this.root.getChildren().addAll(solutionBtn, solutionBox, selectBtn, resetBtn);

        this.stage.setTitle("8-Puzzle Game");
        this.stage.setScene(this.scene);
        this.stage.show();

    }

    // method to set new stage based on new input file
    private void modifyStage(Stage stage){
        this.stage = stage;
        this.pieceCells.clear();
        this.map.getChildren().clear();
        this.noLongerClickable = false;

        this.createMap(this.gameBoard);

        // initializing the initial node
        this.initState = new State(this.gameBoard, State.actionNotApplicable, null);

        // checking if puzzle is solvable
        setPromptValues();

        this.setSolBtn();
        // Adding a drop down list or choice box for the algorithms
        ChoiceBox<String> solutionBox = new ChoiceBox<>();
        solutionBox.getItems().addAll("BFS", "DFS", "A*");
        solutionBox.setValue("BFS"); solutionBox.setStyle("-fx-font: 20px \"Tw Cen MT\";");
        solutionBox.setLayoutX(90); solutionBox.setLayoutY(350);
        
        // Extracting the values of the choice box
        this.solutionBtn.setOnAction(e -> getChoice(solutionBox));

        // Adding a select file button
        Font btnFont = Font.font("Tw Cen MT",FontWeight.NORMAL,16);
        Button selectBtn = new Button("Select a File");
        selectBtn.setFont(btnFont); selectBtn.setLayoutX(110); selectBtn.setLayoutY(8);
        this.addEventHandler(selectBtn);
        
        // Adding a reset button
        Button resetBtn = new Button("Reset");
        resetBtn.setFont(btnFont); resetBtn.setLayoutX(220); resetBtn.setLayoutY(8);
        this.addEventHandler(resetBtn);
        
        // set stage elements here
        this.root.getChildren().add(this.canvas);
        this.root.getChildren().add(this.map);
        this.root.getChildren().addAll(solutionBtn, solutionBox, selectBtn, resetBtn);

        this.stage.setTitle("8-Puzzle Game");
        this.stage.setScene(this.scene);
        this.stage.show();
    }

    // method to display prompt based on the validity of the input file or gameboard values
    private void setPromptValues(){
        // Fonts
        Font promptFont = Font.font("Tw Cen MT",FontWeight.NORMAL,30);
        if(!checkValidPuzzle()){
            // Message to user about the puzzle
            this.puzzlePrompt = "Invalid puzzle values.";
            Text prompt = new Text(this.puzzlePrompt);
            prompt.setFont(promptFont); prompt.setFill(Color.BLACK); prompt.setStrokeWidth(1.5);
            prompt.setLayoutX(75); prompt.setLayoutY(65);

            Text secondPrompt = new Text("Please enter correct values.");
            secondPrompt.setFont(promptFont); secondPrompt.setFill(Color.BLACK); secondPrompt.setStrokeWidth(1.5);
            secondPrompt.setLayoutX(35); secondPrompt.setLayoutY(90);

            this.root.getChildren().addAll(prompt, secondPrompt);
        }

        else if(!isSolvable(this.gameBoard)){
            // Showing info on terminal
            this.puzzlePrompt = "Non-solvable.";
            System.out.println(this.puzzlePrompt);

            // Showing info on GUI

            // Message to user about the puzzle
            Text prompt = new Text(this.puzzlePrompt);
            prompt.setFont(promptFont); prompt.setFill(Color.BLACK); prompt.setStrokeWidth(1.5);
            prompt.setLayoutX(115); prompt.setLayoutY(65);

            Text secondPrompt = new Text("No matter how hard you try.");
            secondPrompt.setFont(promptFont); secondPrompt.setFill(Color.BLACK); secondPrompt.setStrokeWidth(1.5);
            secondPrompt.setLayoutX(35); secondPrompt.setLayoutY(90);

            this.root.getChildren().addAll(prompt, secondPrompt);
        }
        else{
            // showing info on terminal
            this.puzzlePrompt = "Solvable. You can do this!";
            System.out.println(this.puzzlePrompt);

            // Showing info on GUI

            // Message to user about the puzzle
            Text prompt = new Text(this.puzzlePrompt);
            prompt.setFont(promptFont); prompt.setFill(Color.BLACK); prompt.setStrokeWidth(1.5);
            prompt.setLayoutX(45); prompt.setLayoutY(65);
            this.root.getChildren().addAll(prompt);

        }
    }

    // method to get the input values from the input file
    private void initGameBoard(){
     // Getting the input from the default file
     InputStream is = Main.class.getResourceAsStream("inputFile.in");
     String line = "";

     int count = 0;

     try {
         BufferedReader br = new BufferedReader(new InputStreamReader(is));

         while((line = br.readLine()) != null){
             String[] strArray = line.split(" "); // strArray should hold the values per line (which is three values per row)

             for(int i = 0; i < strArray.length; i++){
                 int num = Integer.parseInt(strArray[i]); // type casting the string as integer

                 this.gameBoard[count][i] = num;
             }
             count++;
         }
         // printing the extracted 3 x 3 array
         printPuzzle(gameBoard);
         
     } catch (FileNotFoundException e) {
         e.printStackTrace();
     }
     catch (IOException e) {
         e.printStackTrace();
     }
    }

    // method to modify initial gameboard
    private void changeGameBoard(String path){
        String line = "";

     int count = 0;

     try {
         try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            while((line = br.readLine()) != null){
                 String[] strArray = line.split(" "); // strArray should hold the values per line (which is three values per row)

                 for(int i = 0; i < strArray.length; i++){
                     int num = Integer.parseInt(strArray[i]); // type casting the string as integer

                     this.gameBoard[count][i] = num;
                 }
                 count++;
             }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

         // printing the extracted 3 x 3 array
         printPuzzle(gameBoard);
         
     } catch (FileNotFoundException e) {
         e.printStackTrace();
     }
     catch (IOException e) {
         e.printStackTrace();
     }
    }

    // method to create 3x3 gameboard = 9 tiles. It assigns an Element in the pieceCells array based on the gameboard index.
	private void createMap(int[][] gBoard){
		for(int i=0;i<GameBoard.MAP_NUM_ROWS;i++){
			for(int j=0;j<GameBoard.MAP_NUM_COLS;j++){
                switch (gBoard[i][j]) {
                    case 0:
                        Element zero = new Element(Element.ZERO_TYPE, this);
                        this.instantiateNode(zero, i, j);
                        break;
                    case 1:
                        Element one = new Element(Element.ONE_TYPE, this);
                        this.instantiateNode(one, i, j);
                        break;
                    case 2:
                        Element two = new Element(Element.TWO_TYPE, this);
                        this.instantiateNode(two, i, j);
                        break;
                    case 3:
                        Element three = new Element(Element.THREE_TYPE, this);
                        this.instantiateNode(three, i, j);
                        break;
                    case 4:
                        Element four = new Element(Element.FOUR_TYPE, this);
                        this.instantiateNode(four, i, j);
                        break;
                    case 5:
                        Element five = new Element(Element.FIVE_TYPE, this);
                        this.instantiateNode(five, i, j);
                        break;
                    case 6:
                        Element six = new Element(Element.SIX_TYPE, this);
                        this.instantiateNode(six, i, j);
                        break;
                    case 7:
                        Element seven = new Element(Element.SEVEN_TYPE, this);
                        this.instantiateNode(seven, i, j);
                        break;
                    case 8:
                        Element eight = new Element(Element.EIGHT_TYPE, this);
                        this.instantiateNode(eight, i, j);
                        break;
                }
			}
		}

		this.setGridPaneProperties();
		this.setGridPaneContents();
	}

	// method to set the initial tile (Element) coordinates on the board and add it to the pieceCells
	private void instantiateNode(Element node, int i, int j){
		node.initRowCol(i, j);
		this.pieceCells.add(node);
	}

	// method to set size and location of the grid pane
	private void setGridPaneProperties(){
		this.map.setPrefSize(GameBoard.MAP_WIDTH, GameBoard.MAP_HEIGHT);
		//set the map to x and y location; add border color to see the size of the gridpane/map
		this.map.setLayoutX(GameBoard.WINDOW_WIDTH*0.23);
	    this.map.setLayoutY(GameBoard.WINDOW_WIDTH*0.3);
	    this.map.setVgap(8.3);
	    this.map.setHgap(15);
	}

	// method to add row and column constraints of the grid pane
	private void setGridPaneContents(){

		 //loop that will set the constraints of the elements in the grid pane
	     int counter=0;
	     for(int row=0;row<GameBoard.MAP_NUM_ROWS;row++){
	    	 for(int col=0;col<GameBoard.MAP_NUM_COLS;col++){
	    		 // map each element constraints
	    		 GridPane.setConstraints(pieceCells.get(counter).getImageView(),col,row);
	    		 counter++;
	    	 }
	     }

	   //loop to add each element to the gridpane/map
	     for(Element piece: pieceCells){
	    	 this.map.getChildren().add(piece.getImageView());
	     }
	}

    // method to solve the puzzle using the Breadth-First Search algorithm
    private State searchBFS() {
        Queue<State> frontier = new LinkedList<>();
        frontier.add(this.initState);

        ArrayList<State> explored = new ArrayList<>();
        State resultNode = this.initState;

        while( frontier.size() != 0) {
            State currentState = frontier.remove();
            explored.add(currentState);

            if(goalTest(currentState)) {
                resultNode = currentState;
                break;
            }
            else{
                for(Character action: actions(currentState)){
                    // Check if the next resulting state has already been expanded in the frontier or explored list.
                    // If hasDup becomes true, it means that the resulting state has already been explored
                    Boolean hasDup = false;

                    for(State explore: explored){
                        if(Arrays.deepEquals(explore.getTileValues(), result(currentState, action).getTileValues())){
                            hasDup = true;
                        }
                    }
                    for(State front: frontier){
                        if(Arrays.deepEquals(front.getTileValues(), result(currentState, action).getTileValues())){
                            hasDup = true;
                        }
                    }
                    // If hasDup is false, then add it to the frontier
                    if(!hasDup) frontier.add(result(currentState, action));
                }
            }
        }
        return resultNode;
    }

    // method to solve the puzzle using the Depth-First Search algorithm
    private State searchDFS() {
        Stack<State> frontier = new Stack<>();
        frontier.add(this.initState);

        ArrayList<State> explored = new ArrayList<>();
        State resultNode = this.initState;

        while( frontier.size() != 0) {
            State currentState = frontier.pop();
            explored.add(currentState);

            if(goalTest(currentState)) {
                resultNode = currentState;
                break;
            }
            else{
                for(Character action: actions(currentState)){
                    // Check if the next resulting state has already been expanded in the frontier or explored list.
                    // If hasDup becomes true, it means that the resulting state has already been explored
                    Boolean hasDup = false;

                    for(State explore: explored){
                        if(Arrays.deepEquals(explore.getTileValues(), result(currentState, action).getTileValues())){
                            hasDup = true;
                        }
                    }
                    for(State front: frontier){
                        if(Arrays.deepEquals(front.getTileValues(), result(currentState, action).getTileValues())){
                            hasDup = true;
                        }
                    }
                    // If hasDup is false, then add it to the frontier
                    if(!hasDup) frontier.push(result(currentState, action));
                }
            }
        }
        return resultNode;
    }

     // method to solve the puzzle using the A* Search algorithm
     private State searchAStar() {
        Queue<State> openList = new LinkedList<>();
        openList.add(this.initState);

        ArrayList<State> closedList = new ArrayList<>();
        State resultNode = this.initState; // buffer state to return

        // debug
        // Boolean cont = true;
        // Scanner read = new Scanner(System.in);

        while( openList.size() != 0) {
            State bestNode = findMinF(openList);
            openList.remove(bestNode);
            closedList.add(bestNode);
            // System.out.println("Openlist/frontier size: "+ openList.size());
            // System.out.println("Current bestnode:");
            // printPuzzle(bestNode.getTileValues());
            // System.out.println("");

            if(goalTest(bestNode)) {
                resultNode = bestNode;
                break;
            }
            else{
                for(Character action: actions(bestNode)){
                    // Check if the next resulting state has already been expanded in the openList or closedList list.
                    // If hasDup becomes true, it means that the resulting state has already been closedList
                    Boolean hasDup = false;
                    Boolean lessFDup = false;
                    State checkNode = result(bestNode, action);
                    // System.out.println("Action: " + action);
                    // printPuzzle(checkNode.getTileValues());

                    for(State closed: closedList){
                        if(Arrays.deepEquals(closed.getTileValues(), checkNode.getTileValues())){
                            hasDup = true;
                        }
                    }
                    for(State open: openList){
                        boolean sameState = false;
                        if(Arrays.deepEquals(open.getTileValues(), checkNode.getTileValues())){
                            hasDup = true;
                            sameState = true; // if sameState is true, that means a duplicate state is found
                        }
                        if(sameState && checkNode.getGValue() < open.getGValue()){
                            lessFDup = true;    // if the checkNode has less g value, set lessFDup to true and add checkNode to openList
                        }
                    }

                    // for(State open: openList){
                    //     if(Arrays.deepEquals(open.getTileValues(), checkNode.getTileValues())
                    //         && checkNode.getGValue() < open.getGValue() ){
                    //         lessFDup = true;
                    //     }
                    // }

                    if(lessFDup) {
                        openList.add(checkNode);
                    }
                    // If hasDup is false, then add it to the openList
                    else if(!hasDup) {
                        openList.add(checkNode);
                    }
                }
            }
            // System.out.println("These are the current states in openlist:");
            // for(State s: openList){
            //     System.out.println("");
            //     printPuzzle(s.getTileValues());
            // }

            // System.out.println("Continue?: ");
            // cont = read.nextBoolean();
        }
        return resultNode;
    }

    State findMinF(Queue<State> openList){
        State minStateF = this.initState; //buffer

        Iterator<State> it = openList.iterator();
            int min = Integer.MAX_VALUE;
            while (it.hasNext()) {
                State i = it.next();
                min = Math.min(i.getFValue(), min);
            }
            for(State s: openList){
                if(s.getFValue()==min){
                    minStateF = s;
                    break;
                }
            }
            return minStateF;
    }

    // A method that returns true if the state, s, is the goal, and false otherwise. 
    // Therefore, it will only return true if all the cells are ordered sequentially from Top-Bottom,
    // Left-Right, with ascending values from 1-8 (such that the empty cell is located at 2,2).
    private Boolean goalTest(State test){
        Boolean isGoal = false;
            if(Arrays.deepEquals(GameBoard.goalPuzzle, test.getTileValues())) isGoal = true;
        return isGoal;
    }

    // Given a state, s, the actions method will return a list of possible actions.
    private ArrayList<Character> actions(State currentState){
        ArrayList<Character> actionList = new ArrayList<>();
        int row = currentState.getEmpRow(); // x-coordinate of the empty tile
        int col = currentState.getEmpCol(); // y-coordinate of the empty tile

        // Checking UP movement
        if(row - 1 >= 0){
            actionList.add(State.UP);
        }
        // Checking RIGHT movement
        if(col + 1 <= 2){
            actionList.add(State.RIGHT);
        }
        // Checking DOWN movement
        if(row + 1 <= 2){
            actionList.add(State.DOWN);
        }
        // Checking LEFT movement
        if(col - 1 >= 0){
            actionList.add(State.LEFT);
        }

        return actionList;
    }

    // Given a state, s, and an action, a, the result method will return the next state.
    private State result(State currentState, Character action){
        int[][] nextTileValues = new int[currentState.getTileValues().length][];
        for(int i=0; i<currentState.getTileValues().length; i++){
            nextTileValues[i] = currentState.getTileValues()[i].clone();
        }

            int new_empty_loc_row, new_empty_loc_col;

        if(action == State.UP){

            new_empty_loc_row = currentState.getEmpRow()-1;
            new_empty_loc_col = currentState.getEmpCol();

            // getting old value of next tile and putting it to the previous empty tile
            nextTileValues[currentState.getEmpRow()][currentState.getEmpCol()] = nextTileValues[new_empty_loc_row][new_empty_loc_col];
            // setting zero to the next tile
            nextTileValues[new_empty_loc_row][new_empty_loc_col] = 0;
        }
        else if(action == State.RIGHT){

            new_empty_loc_row = currentState.getEmpRow();
            new_empty_loc_col = currentState.getEmpCol()+1;

            // getting old value of next tile and putting it to the previous empty tile
            nextTileValues[currentState.getEmpRow()][currentState.getEmpCol()] = nextTileValues[new_empty_loc_row][new_empty_loc_col];
            // setting zero to the next tile
            nextTileValues[new_empty_loc_row][new_empty_loc_col] = 0;

        }
        else if(action == State.DOWN){
            new_empty_loc_row = currentState.getEmpRow()+1;
            new_empty_loc_col = currentState.getEmpCol();

            // getting old value of next tile and putting it to the previous empty tile
            nextTileValues[currentState.getEmpRow()][currentState.getEmpCol()] = nextTileValues[new_empty_loc_row][new_empty_loc_col];
            // setting zero to the next tile
            nextTileValues[new_empty_loc_row][new_empty_loc_col] = 0;
        }
        else if(action == State.LEFT){
            new_empty_loc_row = currentState.getEmpRow();
            new_empty_loc_col = currentState.getEmpCol()-1;
            

            // getting old value of next tile and putting it to the previous empty tile
            nextTileValues[currentState.getEmpRow()][currentState.getEmpCol()] = nextTileValues[new_empty_loc_row][new_empty_loc_col];
            // setting zero to the next tile
            nextTileValues[new_empty_loc_row][new_empty_loc_col] = 0;
            
        }
        State nextState = new State(nextTileValues, action, currentState);
        return nextState;
    }

    // A utility function to count inversions in given array 'arr[]'
    static int getInvCount(int[] arr){
        int inv_count = 0;
        for (int i = 0; i < MAX_CELLS; i++)
            for (int j = i + 1; j < MAX_CELLS; j++)
                // Value 0 is used for empty space
                if (arr[i] > 0 &&
                                arr[j] > 0 && arr[i] > arr[j])
                    inv_count++;
        return inv_count;
    }

    // method to know if puzzle is solvable. This method returns true if 8-puzzle game is solvable.
    // inversion is a case in which for two integers i<j, ith element in the array is larger than jth element.
    // if the number of inversions is odd, the game is not solvable.
    // From: https://www.geeksforgeeks.org/check-instance-8-puzzle-solvable
    static boolean isSolvable(int[][] puzzle){
        int linearPuzzle[];
        linearPuzzle = new int[MAX_CELLS];
        int k = 0;
        
    // Converting 2-D puzzle to linear form
        for(int i=0; i<3; i++)
            for(int j=0; j<3; j++)
                linearPuzzle[k++] = puzzle[i][j];
        
        // Count inversions in given 8 puzzle
        int invCount = getInvCount(linearPuzzle);
    
        // return true if inversion count is even.
        return (invCount % 2 == 0);
    }

    // method to print a 2x2 int array or puzzle
    void printPuzzle(int [][] puzzle){
        for(int i=0;i<3;i++){
            System.out.println(Arrays.toString(puzzle[i])); // print final board content
        }
    }

    // method to track the solution for the puzzle and add each action to the solution array
    private void findParentAction(State node){
        if(node.getAction() == State.actionNotApplicable){
            Collections.reverse(this.solution);
            // printing out the solution
            System.out.print("Solution: ");
            for(Character a: this.solution){
                System.out.print(a+" ");
            }
            System.out.println("");
            this.pathCost = this.solution.size();
            return;
        }
        else{
            this.solution.add(node.getAction());
            findParentAction(node.getParent());
        }
    }

    // method to clear solution list
    private void clearSolution(){
        this.solution.clear();
    }

    // method to track the (solution) states and save it to trackStates
    private void setTrackStates(State node){
        if(node.getAction() == State.actionNotApplicable){
            trackStates.add(initState);
            Collections.reverse(this.trackStates);
            // Adding the initial state also
            System.out.println(this.trackStates.size());
            return;
        }
        else{
            this.trackStates.add(node);
            setTrackStates(node.getParent());
        }
    }

    // method to set button for tracking solution states
    private void setTransitionButton(){
        Font btnFont = Font.font("Tw Cen MT",FontWeight.NORMAL,20);
        // Adding a next button
        Button nextBtn = new Button("Next");
        nextBtn.setFont(btnFont); nextBtn.setLayoutX(220); nextBtn.setLayoutY(350);
        this.addEventHandler(nextBtn);
        this.root.getChildren().add(nextBtn);
    }

    // method to clear the tracked states
    private void clearTrackedStates(){
        this.trackStates.clear();
    }

    // method to clear the shown states
    private void clearShownStates(){
        this.shownStates.clear();
    }

    // method to set or update a value to an index in the gameBoard
    void setGameBoardValue(int row, int col, int val){
        this.gameBoard[row][col] = val;
    }

    // method to return the row index of the current empty tile from the gameBoard
    int getEmpRow(){
        int val = 0;
        boolean check = true;
        for(int i=0; i<GameBoard.MAP_NUM_ROWS && check; i++){
            for(int j=0; j<GameBoard.MAP_NUM_COLS; j++){
                if(this.gameBoard[i][j] == 0){
                    val = i;
                    check = false;
                }
            }
        }
        return val;
    }

    // method to return the column index of the current empty tile from the gameBoard
    int getEmpCol(){
        int val = 0;
        boolean check = true;
        for(int i=0; i<GameBoard.MAP_NUM_ROWS && check; i++){
            for(int j=0; j<GameBoard.MAP_NUM_COLS; j++){
                if(this.gameBoard[i][j] == 0){
                    val = j;
                    check = false;
                }
            }
        }
        return val;
    }

    // method to check if the puzzle values are correct or not
    private Boolean checkValidPuzzle(){
        ArrayList<Integer> checkList = new ArrayList<>();
        for(int i=0; i<GameBoard.MAP_NUM_ROWS; i++){
            for(int j=0; j<GameBoard.MAP_NUM_COLS; j++){
                checkList.add(this.gameBoard[i][j]);
            }
        }
        ArrayList<Integer> fullList = new ArrayList<>();
        fullList.add(0); fullList.add(1); fullList.add(2); fullList.add(3);
        fullList.add(4); fullList.add(5); fullList.add(6); fullList.add(7); fullList.add(8);
        if(checkList.containsAll(fullList)){
            return true;
        }
        return false;
    }

    // extracting the solution box value
    private void getChoice(ChoiceBox<String> solBox){
        String choice = solBox.getValue();
        this.algoChoice = choice;
    }

    // method to create solution file
    private void createSolutionFile(){
        File absPath = new File("src/solution");

        System.out.println(absPath.getAbsolutePath());
        final Formatter file;

        try {
            // creating the actual file
            file = new Formatter(absPath.getAbsolutePath() + "Puzzle.out");
            System.out.println("Successful solution file creation");

            // writing the file contents
            file.format("%s", this.solution);
            file.close();

        } catch (Exception e) {
            System.out.println("File error");
        }
    }

    // method to return the row index of a number in the solution 2D array
    static int getRowOfSolutionIndex(int val){
        int rowIndex = 0;
        for(int i=0; i<GameBoard.MAP_NUM_ROWS; i++){
            for(int j=0; j<GameBoard.MAP_NUM_COLS; j++){
                if(GameBoard.goalPuzzle[i][j] == val){
                    rowIndex = i;
                }
            }
        }
        return rowIndex;
    }

     // method to return the column index of a number in the solution 2D array
    static int getColOfSolutionIndex(int val){
        int colIndex = 0;
        for(int i=0; i<GameBoard.MAP_NUM_ROWS; i++){
            for(int j=0; j<GameBoard.MAP_NUM_COLS; j++){
                if(GameBoard.goalPuzzle[i][j] == val){
                    colIndex = j;
                }
            }
        }
        return colIndex;
    }

    // event handler for buttons
    private void addEventHandler(Button btn) {
		btn.setOnMouseClicked(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent arg0) {
				switch(btn.getText()){
				case "Solution":
                    removeWinPrompt();
                    if(checkValidPuzzle()){
                        if(algoChoice=="BFS" && isSolvable(gameBoard)){
                            // Using the BFS algorithm
                            State temp = searchBFS();

                            // clearing the previous answers first
                            clearSolutionPrompt();
                            clearSolution();
                            // tracking the solution
                            findParentAction(temp);
                            setSolutionPrompt();

                            clearTrackedStates();
                            clearShownStates();
                            setTrackStates(temp);

                            setTransitionButton();
                            removeSolutionBtn();
                            setNoLongerClickable();
                            pieceCells.clear();
                            map.getChildren().clear();

                            // creating the solution file
                            createSolutionFile();

                            System.out.println("Path Cost: "+pathCost+" ("+algoChoice+")");
                        }
                        else if (algoChoice=="DFS" && isSolvable(gameBoard)){
                            // Using the DFS algorithm
                            State temp = searchDFS();
    
                            // clearing the previous answers first
                            clearSolutionPrompt();
                            clearSolution();
                            // tracking the solution
                            findParentAction(temp);
                            setSolutionPrompt();

                            clearTrackedStates();
                            clearShownStates();
                            setTrackStates(temp);

                            setTransitionButton();
                            removeSolutionBtn();
                            setNoLongerClickable();
                            pieceCells.clear();
                            map.getChildren().clear();

                            // creating the solution file
                            createSolutionFile();

                            System.out.println("Path Cost: "+pathCost+" ("+algoChoice+")");
                        }
                        else if(algoChoice=="A*" && isSolvable(gameBoard)){
                            // Queue<State> test = new LinkedList<>();
                            // test.add(result(initState, State.UP));
                            // test.add(result(initState, State.RIGHT));
                            // test.add(result(initState, State.DOWN));
                            // test.add(result(initState, State.LEFT));
                            // State best = findMinF(test);
                            // printPuzzle(best.getTileValues());

                            // System.out.println(result(initState, State.UP).getFValue());
                            // System.out.println(result(initState, State.RIGHT).getFValue());
                            // System.out.println(result(initState, State.DOWN).getFValue());
                            // System.out.println(result(initState, State.LEFT).getFValue());

                            // // Using the A* Search algorithm
                            State temp = searchAStar();
                            printPuzzle(temp.getTileValues());

                            // clearing the previous answers first
                            clearSolutionPrompt();
                            clearSolution();
                            // tracking the solution
                            findParentAction(temp);
                            setSolutionPrompt();

                            clearTrackedStates();
                            clearShownStates();
                            setTrackStates(temp);

                            setTransitionButton();
                            removeSolutionBtn();
                            setNoLongerClickable();
                            pieceCells.clear();
                            map.getChildren().clear();

                            // creating the solution file
                            createSolutionFile();

                            System.out.println("Path Cost: "+pathCost+" ("+algoChoice+")");
                        }
                    }                    
					break;
                case "Next":

                    // clearing the pieceCells and map images first before going to the next state
                    // next button won't trigger if tracked states and shown states are equal in size
                    if(trackStates.size() != shownStates.size()){
                        pieceCells.clear();
                        map.getChildren().clear();

                        // if state has not been shown, add to shownStates and present its gameBoard
                    for(State s: trackStates){
                        if(!shownStates.contains(s)){
                            shownStates.add(s);
                            createMap(s.getTileValues());

                            break;
                        } else{
                            // continue
                        }
                    }
                    }
                    // Tell user that solution sequence is done
                    else if(trackStates.size() == shownStates.size()){
                        removeWinPrompt();
                        setWinPrompt("   End of solution!");
                    }
                    break;

                case "Select a File":
                FileChooser fileChooser = new FileChooser();
                File f = fileChooser.showOpenDialog(null);

                if(f != null){
                    String path = f.getAbsolutePath();
                    System.out.println(path);
                    setFilePath(path);
                    changeGameBoard(filePath);
                    clearRoot();
                    modifyStage(stage);
                }
                break;

                case "Reset":
                    clearRoot();
                    pieceCells.clear();
                    map.getChildren().clear();
                    noLongerClickable = false;
                    setStage(stage);
                break;
				}
			}
		});

	}
}

/*
 *      References:
 *          isSolvable method: https://www.geeksforgeeks.org/check-instance-8-puzzle-solvable
 *          ArrayList of Characters to String: https://stackoverflow.com/a/23183963/15416780
 *          Finding minimum value (for F): https://stackoverflow.com/a/46355428/15416780
 *          File Chooser tutorial: https://www.youtube.com/watch?v=A6sA9KItwpY
 *          Creating files: https://www.youtube.com/watch?v=G0DfmD0KKyc
 */