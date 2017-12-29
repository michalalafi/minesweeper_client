package vastja.minesweeper_client.nodes;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import vastja.minesweeper_client.App;

public class Cell extends Rectangle {

	private static final Image init = new Image(App.class.getResource("/vastja/minesweeper_client/resources/img/blank.gif").toString());
	private static final Image hit = new Image(App.class.getResource("/vastja/minesweeper_client/resources/img/bombdeath.gif").toString());
	private static final Image flagged = new Image(App.class.getResource("/vastja/minesweeper_client/resources/img/bombflagged.gif").toString());
	
	private static final int SIDE = 30;
	private static final int MINE = -1;
	
	private int row;
	private int column;
	
	private boolean isFlagged;
	private boolean mine = false;
	
	private Board board;
	
	public Cell(int column, int row, Board board) {
		super(SIDE, SIDE);
		this.column = column;
		this.row = row;
		this.board = board;
		isFlagged = false;
		this.setFill(new ImagePattern(init));
	}
	
	public void cancellPredicted() {
		isFlagged = false;
		this.setFill(new ImagePattern(init));
	}
	
	public void hit(int number) {
		
		String imgName = "open" + number + ".gif";;
		
		if (number == MINE) {
			mine = true;
			imgName = "bombdeath.gif";
		}
		//TODO
		
		Image img = new Image(App.class.getResource("/vastja/minesweeper_client/resources/img/" + imgName).toString());
		this.setFill(new ImagePattern(img));
	}
	
	public void endGameReveal(int number) {
		
		String imgName;
		
		if (number == MINE && isFlagged) {
			imgName = "bombdisarmed.gif";
			mine = true;
		}
		else if (number == MINE && !mine) {
			imgName = "bombrevealed.gif";
			mine = true;
		}
		else {
			//TODO
			return;
		}
		
		Image img = new Image(App.class.getResource("/vastja/minesweeper_client/resources/img/" + imgName).toString());
		this.setFill(new ImagePattern(img));
	}
	
	public void endGameCheck() {
		if (isFlagged && mine == false) {
			Image img = new Image(App.class.getResource("/vastja/minesweeper_client/resources/img/bombmisflagged.gif").toString());
			this.setFill(new ImagePattern(img));
		}
	}
	
	
	public void predictMine() {
		isFlagged = true;
		this.setFill(new ImagePattern(flagged));
	}

	public int getColumn() {
		return column;
	}

	public int getRow() {
		return row;
	}

	public Board getBoard() {
		return board;
	}
	
	public boolean isFlagged() {
		return isFlagged;
	}
}
