package puzzle;

public class State {
    private int[][] tileValues;                      // array containing the tile values
    private int empty_loc_row, empty_loc_col;        // the row and column (a.k.a. index) of the empty tile
    private Character actionDirection;                    // char action to arrive at this state
    private State parentNode;                         // a pointer to the parent node

    private Integer g=0, h=0, f=0;

    public final static Character UP = 'U';
    public final static Character RIGHT = 'R';
    public final static Character DOWN = 'D';
    public final static Character LEFT = 'L';

    public final static Character actionNotApplicable = 'N';

    public State (int[][] puzzle, char action, State parent){
        // initializing the puzzle array
        this.tileValues = new int[puzzle.length][];   
        for(int i=0; i<puzzle.length;i++){
            this.tileValues[i] = puzzle[i].clone();
        }
            
        this.actionDirection = action;
        this.parentNode = parent;

        this.setEmptyLocCoords(); // finds the empty tile location

        this.setGValue(this);
        this.setHValue();
        this.setFValue();
    }

    int getEmpRow() {
		return this.empty_loc_row;
	}

	int getEmpCol() {
		return this.empty_loc_col;
	}

    int[][] getTileValues(){
        return this.tileValues;
    }

    Character getAction(){
        return this.actionDirection;
    }

    State getParent(){
        return this.parentNode;
    }

    void setParent(State p){
        this.parentNode = p;
    }

    Integer getGValue(){
        return this.g;
    }

    Integer getHValue(){
        return this.h;
    }

    Integer getFValue(){
        return this.f;
    }

    private void incrementG(){
        this.g++;
    }

    // method to set empty tile coordinates
    private void setEmptyLocCoords(){
        boolean done = false;
        for(int row=0; row<GameBoard.MAP_NUM_ROWS && !done; row++){
            for(int col=0; col<GameBoard.MAP_NUM_COLS; col++){
                if(this.tileValues[row][col] == 0){
                    this.empty_loc_row = row;
                    this.empty_loc_col = col;
                    done = true;
                    break;
                }
            }
        }
    }

    private void setGValue(State node){
        if(node.getAction() == State.actionNotApplicable){
            return;
        }
        else{
            this.incrementG();
            setGValue(node.getParent());
        }
    }

    private void setHValue(){
        Integer totalDistance = 0;
        Integer distance = 0;

        for(int i=0; i<GameBoard.MAP_NUM_ROWS; i++){
            for(int j=0; j<GameBoard.MAP_NUM_COLS; j++){
                switch (this.tileValues[i][j]) {
                    case 1:
                        distance = Math.abs(i-GameBoard.getRowOfSolutionIndex(1)) + Math.abs(j-GameBoard.getColOfSolutionIndex(1));
                        totalDistance+=distance;                      
                        break;
                    case 2:
                        distance = Math.abs(i-GameBoard.getRowOfSolutionIndex(2)) + Math.abs(j-GameBoard.getColOfSolutionIndex(2));
                        totalDistance+=distance;                      
                        break;
                    case 3:
                        distance = Math.abs(i-GameBoard.getRowOfSolutionIndex(3)) + Math.abs(j-GameBoard.getColOfSolutionIndex(3));
                        totalDistance+=distance;                      
                        break;
                    case 4:
                        distance = Math.abs(i-GameBoard.getRowOfSolutionIndex(4)) + Math.abs(j-GameBoard.getColOfSolutionIndex(4));
                        totalDistance+=distance;                      
                        break;
                    case 5:
                        distance = Math.abs(i-GameBoard.getRowOfSolutionIndex(5)) + Math.abs(j-GameBoard.getColOfSolutionIndex(5));
                        totalDistance+=distance;                      
                        break;
                    case 6:
                        distance = Math.abs(i-GameBoard.getRowOfSolutionIndex(6)) + Math.abs(j-GameBoard.getColOfSolutionIndex(6));
                        totalDistance+=distance;                      
                        break;
                    case 7:
                        distance = Math.abs(i-GameBoard.getRowOfSolutionIndex(7)) + Math.abs(j-GameBoard.getColOfSolutionIndex(7));
                        totalDistance+=distance;                      
                        break;
                    case 8:
                        distance = Math.abs(i-GameBoard.getRowOfSolutionIndex(8)) + Math.abs(j-GameBoard.getColOfSolutionIndex(8));
                        totalDistance+=distance;                      
                        break;
                }
            }
        }

        this.h = totalDistance;
    }

    private void setFValue(){
        this.f = this.g + this.h;
    }
    

}
