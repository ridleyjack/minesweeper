/*
 * James Riley Jackson
 * 300200062
 * MineSweeper Part 3
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;


public class MineSweeper extends Application {
	
	final static String imageFolder = "/res/";
	final static String digitFolder = "/res/Digits/";

	public static void main(String[] args) {		
		launch(args);
	}
	
	private Stage pStage;
	private Game game;
	private SimpleButton smileButton;
	private BoardButton[][] boardButton;
	private DigitDisplay timeTicker;
	private DigitDisplay flagTicker;
	private Timeline timer;
	private int difficulty;
	
	private BoardClick_Handler boardClick_Handler;
	
	public void start(Stage theStage) {
		pStage = theStage;
		difficulty = 0;
		createGame(8, 8, 10);
	}
	public void createGame(int width, int height, int mines) {		
		game = new Game(width, height, mines);
		boardClick_Handler = new BoardClick_Handler();
		BorderPane border = new BorderPane();
		
		//Create Header
		HBox header = new HBox();
		header.setAlignment(Pos.CENTER);
		smileButton = new SimpleButton(Icon.F_SMILE.image());
		smileButton.setOnAction(new smileClick_Handler());		
		flagTicker = new DigitDisplay(Icon.F_SMILE.image().getHeight());
		flagTicker.setCount(mines);
		timeTicker = new DigitDisplay(Icon.F_SMILE.image().getHeight());
		header.getChildren().addAll(flagTicker, smileButton, timeTicker);
				
		//Timer
		if (timer != null) timer.getKeyFrames().clear();
		timer = new Timeline(new KeyFrame(Duration.millis(1000), e-> {
			timeTicker.setCount(timeTicker.getCount()+1);
			if (timeTicker.getCount() == 999) timer.stop(); 
		}));
		timer.setCycleCount(Timeline.INDEFINITE);		
		MenuBar menuBar = createMenuBar();		
		
		//Create Board	
		boardButton = new BoardButton[game.boardHeight()][game.boardWidth()];
		GridPane board = new GridPane();
		board.setAlignment(Pos.TOP_CENTER);
		for (int y = 0; y < game.boardHeight(); y++) {
			for (int x = 0; x < game.boardWidth(); x++) {
				boardButton[y][x] = new BoardButton(x, y);
				boardButton[y][x].setOnMouseClicked(boardClick_Handler);
				boardButton[y][x].setOnMousePressed(e -> {
					BoardButton sender = (BoardButton) e.getSource();
					if (e.getButton() == MouseButton.PRIMARY & !game.isOver() & !sender.isRevealed()
							& !sender.isFlagged()) {
						smileButton.setImg((Icon.F_OH.image()));
						smileButton.setOHFace(true);
					}
				});
				boardButton[y][x].setOnMouseReleased(e -> {
					if (smileButton.isOHFace()) {
						smileButton.setImg(Icon.F_SMILE.image());
						smileButton.setOHFace(false);
					}
				});
				board.add(boardButton[y][x], x, y);
			}
		}
		
		VBox gamePanel = new VBox(); //Redundant frame used to get proper format with CSS
		//CSS Styles
		String headerStyle = "-fx-border-style: solid; -fx-border-width: 2px;"
				+ "-fx-border-color: darkgray white white darkgray;"
				+ "-fx-border-radius: 7px;"
				+ "-fx-padding: 4px 4px 4px 4px;";
		String boardStyle = "-fx-border-style: solid; -fx-border-width: 2px;"
				+ "-fx-border-color: darkgray white white darkgray;"
				+ "-fx-border-radius: 10px;"
				+ "-fx-padding: 4px 4px 4px 4px;";
		String panelStyle = "-fx-background-color: lightgray;"
				+ "-fx-padding: 10px 10px 10px 10px;"
				+ "-fx-border-color: white darkgray darkgray white;"
				+ "-fx-border-style: solid; -fx-border-width: 1px;"
				+ "-fx-border-radius: 7px;";
		
		header.setStyle(headerStyle);
		board.setStyle(boardStyle);
		gamePanel.setStyle(panelStyle);
		gamePanel.setSpacing(10);
		
		gamePanel.getChildren().addAll(header, board);
		border.setTop(menuBar);
		border.setCenter(gamePanel);
		pStage.setScene(new Scene(border));
		pStage.setTitle("Mine Sweeper");
		pStage.show();

		header.setSpacing( ( header.getWidth() - ( 2*flagTicker.getWidth() + smileButton.getWidth() + 35 ) )/2 );
	}
	public void resetGame() {
		game = new Game(game.boardWidth(), game.boardHeight(), game.boardMineNum());
		smileButton.setImg(Icon.F_SMILE.image());
		timer.stop();
		timeTicker.setCount(0);
		flagTicker.setCount(game.boardMineNum());
		for (int y = 0; y < game.boardHeight(); y ++) {
			for (int x = 0; x < game.boardWidth(); x++) {				
				boardButton[y][x].reset();
			}
		}
	}
	public void revealMines(boolean win) {
		for(int y = 0; y < game.boardHeight(); y++){
			for( int x = 0; x < game.boardWidth(); x++){
				BoardButton b = boardButton[y][x];
				if (game.getBoardVal(x, y) == 10 && b.isRevealed() == false) {
					if (win) b.setImg(Icon.FLAG.image());
					else if(!b.isFlagged()) b.setImg(Icon.M_GREY.image());
				}
				else if(b.isFlagged()) {
					b.setImg(Icon.M_MISFLAGGED.image());
				}
			}
		}
	}
	private void createCustomGame() {		
		Stage subStage = new Stage();
		subStage.setTitle("Custom Game");
		
		TextField heightField = new TextField();
		heightField.setPromptText("Enter Height");
		TextField widthField = new TextField();
		widthField.setPromptText("Enter Width");
		TextField mineField = new TextField();
		mineField.setPromptText("Enter # Mines");
		
		Label feedback= new Label("");
		feedback.setTextFill(Color.RED);
		Button submit = new Button("Submit");
		submit.setOnAction(e -> {
			int width = 0;
			int height = 0;
			int mines = 0;
			try {
				width = Integer.parseInt(widthField.getText());
				height = Integer.parseInt(heightField.getText());
				mines = Integer.parseInt(mineField.getText());
			}
			catch(Exception ex) {
				width = -1;
				height = -1;
				mines = -1;
				feedback.setText("Invalid Field(s)!");
			}
			if (width <= 0  && mines <= 0 && height <= 0)
				feedback.setText("Invalid Field(s)!");		
			else if (width < 4 || height < 4) {
				feedback.setText("Height and Width must be at least 4 squares each");
			}
			else if ( mines > width*height*0.6)
				feedback.setText("Only 60% Of Board May Be Mines!");	
			else {
				createGame(width, height, mines);
				subStage.close(); 	
			}
		});
		
		GridPane pane = new GridPane();
		pane.setVgap(5);
		pane.setPadding(new Insets(15,15,15,15));
		pane.add(heightField, 0, 1);
		pane.add(widthField, 0, 2);
		pane.add(mineField, 0, 3);
		pane.add(feedback, 0, 4);
		pane.add(submit, 0, 5);
		subStage.setScene(new Scene(pane));
		subStage.setHeight(240);
		subStage.setWidth(350);
		subStage.show();
		submit.requestFocus();
	}
	private MenuBar createMenuBar() {
		MenuBar mBar = new MenuBar();
		Menu gameMenu = new Menu("New Game");			
		MenuItem beginner = new MenuItem("Beginner"); 
		beginner.setOnAction(e -> {
			difficulty = 0;
			createGame(8, 8, 10);
		});
		MenuItem intermediate = new MenuItem("Intermediate");
		intermediate.setOnAction(e -> {
			difficulty = 1;
			createGame(16, 16, 40);
		});
		MenuItem expert = new MenuItem("Expert");
		expert.setOnAction(e -> {
			difficulty = 2;
			createGame(32, 16, 99);
		});
		MenuItem custom = new MenuItem("Custom");	
		custom.setOnAction(e -> {
			createCustomGame();
			difficulty = -1;
		});
		gameMenu.getItems().addAll(beginner, intermediate, expert, custom);
		
		Menu scoresMenu = new Menu("High Scores");
		MenuItem view = new MenuItem("View");
		view.setOnAction(e-> {
			ScoreSheet.displayScores();
		});
		MenuItem reset = new MenuItem("Reset");
		reset.setOnAction(e-> {
			ScoreSheet.clearScores();
		});
		scoresMenu.getItems().addAll(view, reset);
		
		mBar.getMenus().addAll(gameMenu, scoresMenu);
		return mBar;
	}
	
	class smileClick_Handler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			resetGame();			
		}	
	}
	 class BoardClick_Handler implements EventHandler<MouseEvent>{
		@Override
		public void handle(MouseEvent event) {
			BoardButton sender = (BoardButton) event.getSource();
			if (event.getButton() == MouseButton.PRIMARY) {
				if (!game.isOver() && !sender.isFlagged()) {
					if (!sender.isRevealed()) {
						if (game.isFirstTurn()) {							
							game.reGenBoard(sender.X, sender.Y);
							timer.play();
							game.setFirstTurn(false);
							this.handle(event);
							return;
						} else {
							tryTile(sender);
						}
					} // end if !isRevealed
					else if (game.getBoardVal(sender.X, sender.Y) != 0) {
						// They are clicking on an number. Reveal non flagged tiles
						// if flagNum = tileNum
						int val = game.getBoardVal(sender.X, sender.Y);
						int flagCount = 0;
						ArrayList<BoardButton> neighbours = new ArrayList<BoardButton>(8);
						for (int y = -1; y <= 1; y++)
							for (int x = -1; x <= 1; x++) {
								if (y == 0 && x == 0)
									continue;
								int x2 = sender.X + x;
								int y2 = sender.Y + y;
								if (x2 >= game.boardWidth() || x2 < 0)
									continue;
								if (y2 >= game.boardHeight() || y2 < 0)
									continue;
								
								if (boardButton[y2][x2].isFlagged())
									flagCount++;
								else
									neighbours.add(boardButton[y2][x2]);
							} // end of for loops
						if (val == flagCount) {
							for (BoardButton b : neighbours)
								if (!b.isRevealed()) tryTile(b);
						}
					}
					if (game.isFirstTurn())
						game.setFirstTurn(false);
					// End Game Condition
					if (game.getSquaresLeft() == 0) {
						smileButton.setImg(Icon.F_WIN.image());
						game.setOver(true);
						timer.stop();
						if ( difficulty >= 0 ) 
							ScoreSheet.Determine_If_HighScore( difficulty, timeTicker.getCount() );
						revealMines(true);
					}
				} // end if !gameOver && !flagged
			} // end if Primary Click
			else { // it's a secondary click
				if (!game.isOver() && !sender.isRevealed()) {
					if (sender.isFlagged()) { //remove flag
						sender.setImg(Icon.COVER.image());
						sender.setFlagged(false);
						game.decFlagsLeft(-1);
						flagTicker.setCount(game.getFlagsLeft());
					} else { // place flag
						sender.setImg(Icon.FLAG.image());
						sender.setFlagged(true);
						game.decFlagsLeft(1);
						flagTicker.setCount(game.getFlagsLeft());
					}
				}
			} // end if Secondary Click
		} // end handle()
			
		private void reveal_Neighbouring_ZeroTiles(int x, int y) {
			BoardButton b = boardButton[y][x];
			int val = game.getBoardVal(x, y);
			if( b.isRevealed()) return;
			if( b.isFlagged()) return;
			if( val != 0 ) {
				b.setImg(Icon.number(val).image());
				b.setRevealed(true);
				game.decSquaresLeft(1);
				return;			
			}
			
			b.setImg(Icon.ZERO.image());
			b.setRevealed(true);
			game.decSquaresLeft(1);
			
			for (int y2 = -1; y2 <= 1; y2++)
				for (int x2 = -1; x2 <= 1; x2++){
					if(x2 == 0 && y2 == 0) continue;
					if ( x + x2 >= 0 && x + x2 < game.boardWidth() && 
						 y + y2 >= 0 && y + y2 < game.boardHeight() ) //then
						reveal_Neighbouring_ZeroTiles( x + x2 , y + y2 );
				}
		}
		private void tryTile(BoardButton sender) {
			int val = game.getBoardVal(sender.X, sender.Y);
			if (val == 0) {
				reveal_Neighbouring_ZeroTiles(sender.X,sender.Y);
			}
			else {
				Icon icon = Icon.COVER;
				if (val < 9 ) {
					icon = Icon.number(val);
					game.decSquaresLeft(1);	
				}
				else if (val == 10) {
						icon = Icon.M_RED;
						smileButton.setImg(Icon.F_DEAD.image());
						game.setOver(true);
						revealMines(false);	
						timer.stop();
				}
				sender.setRevealed(true);
				sender.setImg(icon.image());
			}
		}
	} //End boardClick Handler
} // End Application Class
/*
 * Graphics/Application Components
 */
 enum Icon{
	 ZERO (MineSweeper.imageFolder + "0.png"),
	 ONE (MineSweeper.imageFolder + "1.png"),
	 TWO (MineSweeper.imageFolder + "2.png"),
	 THREE (MineSweeper.imageFolder + "3.png"),
	 FOUR (MineSweeper.imageFolder + "4.png"),
	 FIVE (MineSweeper.imageFolder + "5.png"),
	 SIX (MineSweeper.imageFolder + "6.png"),	 
	 SEVEN (MineSweeper.imageFolder + "7.png"),
	 EIGHT (MineSweeper.imageFolder + "8.png"),
	 COVER (MineSweeper.imageFolder + "cover.png"),
	 
	 F_DEAD (MineSweeper.imageFolder + "face-dead.png"),
	 F_SMILE (MineSweeper.imageFolder + "face-smile.png"),
	 F_WIN (MineSweeper.imageFolder + "face-win.png"),
	 F_OH (MineSweeper.imageFolder + "face-O.png"),
	 
	 FLAG (MineSweeper.imageFolder + "flag.png"),
	 M_GREY (MineSweeper.imageFolder + "mine-grey.png"),
	 M_MISFLAGGED (MineSweeper.imageFolder + "mine-misflagged.png"),
	 M_RED (MineSweeper.imageFolder + "mine-red.png");	 
	 
	 static Icon number(int i) {
		 if (i == 0) return ZERO; 
		 if (i == 1) return ONE; 
		 if (i == 2) return TWO; 
		 if (i == 3) return THREE; 
		 if (i == 4) return FOUR; 
		 if (i == 5) return FIVE;
		 if (i == 6) return SIX;
		 if (i == 7) return SEVEN;
		 if (i == 8) return EIGHT; 
		 return COVER;
	 }
	 
	private Image img;
	Icon(String p) {
		try {
			img = new Image(p);
		}
		catch (Exception e){
			System.err.println("Error Loading Image! Name: '" + this.name() + "' Path: '" + p + "'\nProgram Terminated.");
			System.exit(0);
		}
	}
	public Image image() {
		return this.img;
	}
}//End Enum Icon
class SimpleButton extends Button { // The Smile Face Button
	private ImageView c;
	private boolean isOHFace = false;

	public SimpleButton( Image img ){
		double height = img.getHeight();
		double width = img.getWidth();
		
		setMinWidth(width);
		setMaxWidth(width);
		setMinHeight(height);
		setMaxHeight(height);

		setImg(img);
	}
	public boolean isOHFace() {
		return isOHFace;
	}
	public void setOHFace(boolean isOHFace) {
		this.isOHFace = isOHFace;
	}
	public void setImg(Image img) {		
		c = new ImageView(img);
		c.setFitHeight(this.getHeight());
		c.setFitWidth(this.getWidth());
		
		setGraphic(c);
	}
}//End Simp Button
class BoardButton extends SimpleButton {
	final int X;
	final int Y;	
	private boolean revealed = false;
	private boolean flag = false;
	public BoardButton(int x, int y) {
		super(Icon.COVER.image());
		X = x;
		Y = y;		
	}
	public void reset() {
		revealed = false;
		flag = false;
		setImg(Icon.COVER.image());
	}
	public boolean isRevealed() { return revealed; }
	public void setRevealed(boolean r) { revealed = r;}
	public boolean isFlagged() { return flag; }
	public void setFlagged(boolean f) { flag = f; }
}//end boardButton
class DigitDisplay extends HBox {
	private static Image[] digit;
	static {
		digit = new Image[]{ new Image(MineSweeper.digitFolder + "0.png"), new Image(MineSweeper.digitFolder +"1.png"), new Image(MineSweeper.digitFolder +"2.png"),
							 new Image(MineSweeper.digitFolder + "3.png"), new Image(MineSweeper.digitFolder +"4.png"), new Image(MineSweeper.digitFolder +"5.png"),
							 new Image(MineSweeper.digitFolder +"6.png"), new Image(MineSweeper.digitFolder +"7.png"), new Image(MineSweeper.digitFolder +"8.png"), 
							 new Image(MineSweeper.digitFolder +"9.png"), new Image(MineSweeper.digitFolder +"neg.png")};
	}
	
	public ImageView[] slot = new ImageView[3];
	private int count;
	
	public DigitDisplay(double height){
		super();
		slot[0] = new ImageView(digit[0]);
		slot[1] = new ImageView(digit[0]);
		slot[2] = new ImageView(digit[0]);
		
		double width = digit[0].getWidth()*1.6;
		slot[0].setFitHeight(height);
		slot[0].setFitWidth(width);
		slot[1].setFitHeight(height);
		slot[1].setFitWidth(width);
		slot[2].setFitHeight(height);
		slot[2].setFitWidth(width);
		super.getChildren().addAll(slot);
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
		update();
	}
	private void update() {
		int val = count >= 0 ? count : -count;
		for(int i = 2; i >= 0; i--){
			slot[i].setImage(digit[val % 10]);
			val = val/10;
		}
		if (count < 0) slot[0].setImage(digit[10]);
	}	
}// end Counter
class ScoreSheet {
	final static String scoreFile = "highscores.txt";
	public static HighScore[] loadScores() {
		HighScore[] scores = new HighScore[3];
		File f = new File(scoreFile);
		try {
			Scanner sc = new Scanner(f);
			for(int i = 0; i < 3; i++){
				if (sc.hasNextLine()) {
					String s = sc.nextLine();
					if (s != "") scores[i] = new HighScore(s);
				}
			}
			sc.close();
		} catch (Exception e) {

		}
		return scores;
	}
	public static void clearScores() {
		File f = new File(scoreFile);
		try {
			PrintWriter pw = new PrintWriter(f);
			pw.println();
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void displayScores() {
		Stage s = new Stage();
		HighScore[] scores = loadScores();
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(10,5, 5,20));
		grid.setHgap(40); 
		grid.setVgap(5);
		grid.add(new Label("Beginner:"), 0, 0);
		grid.add(new Label("Intermediate:"), 0, 1);
		grid.add(new Label("Expert:"), 0, 2);
		
		for (int i = 0; i < 3; i++) {
			if (scores[i] != null) {
				grid.add(new Label(scores[i].getName()), 1, i);
				grid.add(new Label(String.valueOf(scores[i].getTime())), 2, i);				
			}
		}
		Button b = new Button("Ok");
		b.setPadding(new Insets(1, 0, 2, 0));
		b.setOnAction( e-> s.close() );
		b.setPrefWidth(100);
		grid.add(b, 0, 3);
		grid.setStyle("-fx-border-style: dashed; -fx-border-width: 5px; -fx-border-color: black;");
		s.setScene(new Scene(grid, 400, 130));
		s.setTitle("Mine Sweeper HighScores");
		s.show();
	}
	public static void addHighScore(HighScore[] scores, int level, int time){
		Stage s = new Stage();
		s.initStyle(StageStyle.UNDECORATED);
		VBox b = new VBox();
		b.setPadding(new Insets(10,10,10,10));
		b.getChildren().add(new Label("New High Score!"));
		b.getChildren().add(new Label("Enter Your Name:"));
		TextField t = new TextField();
		b.getChildren().add(t);
		Button submit = new Button("Submit");
		submit.setOnAction(e-> {
			File f = new File(scoreFile);
			String in = t.getText();
			if (in.length() != 0) {
				scores[level] = new HighScore(in, time);
				try {
					PrintWriter pw = new PrintWriter(f);
					for (int i = 0; i < 3; i++){
						if(scores[i] == null) pw.println();
						else pw.println(scores[i].getName() + "-" + scores[i].getTime());
					}
					pw.close();
				} catch (FileNotFoundException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			s.close();	
		}
		});
		b.getChildren().add(submit);
		s.setScene(new Scene(b));
		s.show();		
	}
	public static void Determine_If_HighScore(int level, int time) {
		HighScore[] scores = loadScores();
		if (scores[level] == null || scores[level].getTime() >= time ) {
			addHighScore(scores, level, time);
		}
	}
}
class HighScore {
	private String name;
	private int time;
	public HighScore(String s){
		String[] part = s.split("-");
		name = part[0];
		time = Integer.parseInt(part[1]);
	}
	public HighScore(String n, int t){
		name = n;
		time = t;
	}
	public int getTime() { 
		return time;
	}
	public String getName(){
		return name;
	}
}
/*
 *  Game Stuff
 */
class Game {
	private int[][] board;
	private int squaresLeft;
	private int flagsLeft;
	private boolean isOver;	
	private int boardMineNum;
	private boolean firstTurn;
	
	public boolean isFirstTurn() {
		return firstTurn;
	}

	public void setFirstTurn(boolean firstTurn) {
		this.firstTurn = firstTurn;
	}

	public Game(int width, int height, int mines) {
		board = new int[height][width];
		flagsLeft = mines;
		squaresLeft = height*width - mines;
		isOver = false;
		boardMineNum = mines;
		firstTurn = true;
		
		int x = 0;
		int y = 0;
		while (mines > 0) {
			do {
				x = (int)(Math.random() * (width));
				y = (int)(Math.random() * (height));
			} while(board[y][x] == 10);
			board[y][x] = 10;
			mines --;
		}
		genBoardNumbers();
	}

	public Game() {
		board = new int[][]{ {0, 0, 0, 0, 0},
						     {0, 10, 0, 0, 0},
						     {0, 0, 0, 10, 0},
						     {0, 0, 0, 10, 0},
						     {0, 0, 0, 0, 0} };						    
		boardMineNum = 3;
		flagsLeft = 3;
		squaresLeft = 5*5 - 3; // 3 = mineCount
		firstTurn = true;
		genBoardNumbers();
	}
	public void reGenBoard(int firstX, int firstY) {
		do {
			int mines = 0;
			// Clear Board
			for (int y = 0; y < boardHeight(); y++) {
				for (int x = 0; x < boardWidth(); x++){
					if (board[y][x]== 10) mines++;
					board[y][x] = 0;
				}
			}

			int x = 0;
			int y = 0;
			while (mines > 0) {
				do {
					x = (int)(Math.random() * (boardWidth()));
					y = (int)(Math.random() * (boardHeight()));
				} while(board[y][x] == 10 || (y == firstY && x == firstX));
				board[y][x] = 10;
				mines --;
			}
			genBoardNumbers();
		} while(board[firstY][firstX] != 0);
	}
	private void genBoardNumbers() {
		//10 == bomb
		//check each map square
		for (int y = 0; y < boardHeight(); y++) {
			for (int x = 0; x < boardWidth(); x++) {				
				if(board[y][x] == 10) continue;
				int bombs = 0;
				//check neighboring 8 squares (+ self, but self isn't a mine)
				for(int y2 = -1; y2 <= 1; y2++) {
					for(int x2 = -1; x2 <= 1; x2++){
						if(y+y2 >= 0 && y+y2 < boardHeight() && x+x2 >= 0 && x+x2 < boardWidth()) {
							if(board[y+y2][x+x2] == 10) bombs++;
						}
					}
				}//End check neighbor
				board[y][x] = bombs;
			}
		}//End check Map		
	}
	public int boardWidth() {
		return board.length == 0 ? 0 : board[0].length;
	}
	public int boardHeight() {
		return board.length;
	}	
	public int getBoardVal(int x, int y) {
		if ( x >= 0 && x < boardWidth() && y >= 0 && y < boardHeight()) return board[y][x];
		return -1;
	}
	public boolean isOver() {
		return isOver;
	}
	public void setOver(boolean isOver) {
		this.isOver = isOver;
	}
	public int getSquaresLeft() {
		return squaresLeft;
	}
	public int getFlagsLeft() {
		return flagsLeft;
	}
	public void decSquaresLeft(int a) {
		squaresLeft -= a;
	}
	public void decFlagsLeft(int a) {
		flagsLeft -= a;
	}	
	public int boardMineNum() {
		return boardMineNum;
	}
} 