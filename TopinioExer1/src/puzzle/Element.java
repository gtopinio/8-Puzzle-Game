package puzzle;
import java.util.Arrays;

import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class Element {
    private String type;
	protected Image img;
	protected ImageView imgView;
	protected GameBoard gameStage;
	protected int row, col;

    // The images are stored as constant values
    public final static int IMAGE_SIZE = 60;
    public final static Image ZERO_IMAGE = new Image("images/zero.png",GameBoard.CELL_WIDTH,GameBoard.CELL_WIDTH,false,false);
    public final static Image ONE_IMAGE = new Image("images/one.png",GameBoard.CELL_WIDTH,GameBoard.CELL_WIDTH,false,false);
    public final static Image TWO_IMAGE = new Image("images/two.png",GameBoard.CELL_WIDTH,GameBoard.CELL_WIDTH,false,false);
    public final static Image THREE_IMAGE = new Image("images/three.png",GameBoard.CELL_WIDTH,GameBoard.CELL_WIDTH,false,false);
    public final static Image FOUR_IMAGE = new Image("images/four.png",GameBoard.CELL_WIDTH,GameBoard.CELL_WIDTH,false,false);
    public final static Image FIVE_IMAGE = new Image("images/five.png",GameBoard.CELL_WIDTH,GameBoard.CELL_WIDTH,false,false);
    public final static Image SIX_IMAGE = new Image("images/six.png",GameBoard.CELL_WIDTH,GameBoard.CELL_WIDTH,false,false);
    public final static Image SEVEN_IMAGE = new Image("images/seven.png",GameBoard.CELL_WIDTH,GameBoard.CELL_WIDTH,false,false);
    public final static Image EIGHT_IMAGE = new Image("images/eight.png",GameBoard.CELL_WIDTH,GameBoard.CELL_WIDTH,false,false);

    public final static String ZERO_TYPE = "0";
    public final static String ONE_TYPE = "1";
    public final static String TWO_TYPE = "2";
    public final static String THREE_TYPE = "3";
    public final static String FOUR_TYPE = "4";
    public final static String FIVE_TYPE = "5";
    public final static String SIX_TYPE = "6";
    public final static String SEVEN_TYPE = "7";
    public final static String EIGHT_TYPE = "8";

    public Element(String type, GameBoard gameStage) { // Note: an ELEMENT has an imgView (to set up an Image)
		this.type = type;
		this.gameStage = gameStage;

		// load image depending on the type
		switch(this.type) { // This determines which picture to use, depending on the element to be added on the layout
            case Element.ZERO_TYPE: this.img = Element.ZERO_IMAGE; break;
			case Element.ONE_TYPE: this.img = Element.ONE_IMAGE; break;
            case Element.TWO_TYPE: this.img = Element.TWO_IMAGE; break;
            case Element.THREE_TYPE: this.img = Element.THREE_IMAGE; break;
            case Element.FOUR_TYPE: this.img = Element.FOUR_IMAGE; break;
            case Element.FIVE_TYPE: this.img = Element.FIVE_IMAGE; break;
            case Element.SIX_TYPE: this.img = Element.SIX_IMAGE; break;
            case Element.SEVEN_TYPE: this.img = Element.SEVEN_IMAGE; break;
            case Element.EIGHT_TYPE: this.img = Element.EIGHT_IMAGE; break;

		}

		this.setImageView();
		this.setMouseHandler();
	}

    protected void loadImage(String filename,int width, int height){
		try{
			this.img = new Image(filename,width,height,false,false);
		} catch(Exception e){}
	}

	// getters

	String getType(){
		return this.type;
	}

	Image getImage(){
		return this.img;
	}

	int getRow() {
		return this.row;
	}

	int getCol() {
		return this.col;
	}

	protected ImageView getImageView(){
		return this.imgView;
	}

	// setters
	void setType(String type){
		this.type = type;
	}

    void initRowCol(int i, int j) {
		this.row = i;
		this.col = j;
	}

	// method to change image of an Element
	private void changeImage(Element element, Image image) {
		this.imgView.setImage(image);

	}

	private void setImg(Image img){
		this.img = img;
	}

	// method to adjust the properties of an image
    private void setImageView() {
		// initialize the image view of this element
		this.imgView = new ImageView();
		this.imgView.setImage(this.img);
		this.imgView.setLayoutX(0); // JavaFX method of setLayout for Image. Sets the X/Y positioning of the node
		this.imgView.setLayoutY(0);
		this.imgView.setPreserveRatio(true);

        this.imgView.setFitWidth(GameBoard.CELL_WIDTH);
        this.imgView.setFitHeight(GameBoard.CELL_HEIGHT);
	}

	// method to handle tile movements on the 3x3 grid
    private void setMouseHandler(){
		Element element = this;
		this.imgView.setOnMouseClicked(new EventHandler<MouseEvent>(){ //Notice that the ImageView element is the one that interacts
			public void handle(MouseEvent e) {
				// Getting the clicked-tile coordinates
				int currentRow = element.getRow();
				int currentCol = element.getCol();
				int empRow = 0, empCol = 0, count = 0;

				String currentType = element.getType();
				Image currentImg = element.getImage();
				Character desiredAction = State.actionNotApplicable;	// to know if tile movement is valid

				// finding the location of the empty tile (element) first
					for(int x=0; x<GameBoard.MAP_NUM_ROWS; x++){
						for(int y=0; y<GameBoard.MAP_NUM_COLS; y++){
							if(gameStage.getPieceCells().get(count).getType() == ZERO_TYPE){
								empRow = gameStage.getPieceCells().get(count).getRow();
								empCol = gameStage.getPieceCells().get(count).getCol();
							}
							count++;
						}
					}

				// Checking UP movement
				if(currentRow + 1 == gameStage.getEmpRow() && currentCol == gameStage.getEmpCol()){
					desiredAction = State.UP;
				}
				// Checking RIGHT movement
				else if(currentCol - 1 == gameStage.getEmpCol() && currentRow == gameStage.getEmpRow()){
					desiredAction = State.RIGHT;
				}
				// Checking DOWN movement
				else if(currentRow - 1 == gameStage.getEmpRow() && currentCol == gameStage.getEmpCol()){
					desiredAction = State.DOWN;
				}
				// Checking LEFT movement
				else if(currentCol + 1 == gameStage.getEmpCol() && currentRow == gameStage.getEmpRow()){
					desiredAction = State.LEFT;
				}

				// The desired action is a valid direction, update the gameBoard by swapping two tiles (tile clicked and empty tile)
				if(desiredAction != State.actionNotApplicable && gameStage.notClickable() == false){
					boolean stopper = true;	// to stop the for loops once there has been already an update of values

					// Updating the gameBoard (2x2 int array) while also updating the pieceCells (Element array) to update images
					for(int i=0; i<GameBoard.MAP_NUM_ROWS && stopper; i++){
						for(int j=0; j<GameBoard.MAP_NUM_COLS; j++){
							if(gameStage.getGameBoard()[i][j] == 0){
								// Updating the gameBoard
								// Changing empty tile to next tile value
								gameStage.setGameBoardValue(i, j, Integer.parseInt(currentType));
								// Changing next tile value to zero
								gameStage.setGameBoardValue(currentRow, currentCol, 0);

								// Updating the empty tile image from pieceCells
								for(Element tile: gameStage.getPieceCells()){
									if(tile.getRow() == empRow && tile.getCol() == empCol){
										tile.setImg(currentImg);
										tile.changeImage(tile, currentImg);
										tile.setType(currentType);
										break;
									}
								}

								// Updating the tile being clicked with an empty tile or ZERO image/type
								element.setImg(ZERO_IMAGE);
								element.changeImage(element, ZERO_IMAGE);
								element.setType(ZERO_TYPE);
								stopper = false;
								break;
							}
						}
					}
					// Printing the next state
					System.out.println("---New State---");
					gameStage.printPuzzle(gameStage.getGameBoard());
					if(Arrays.deepEquals(gameStage.getGameBoard(), GameBoard.goalPuzzle)){
						gameStage.setWinPrompt("Congrats! You Won!");
						System.out.println("Congrats! You Won!");
					} else{
						gameStage.removeWinPrompt();
					}
				}
				
			}	//end of handle()
		});
	}
	
}
