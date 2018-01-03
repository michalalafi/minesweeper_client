package vastja.minesweeper_client.nodes;
import javafx.beans.NamedArg;
import javafx.scene.layout.GridPane;
import vastja.minesweeper_client.controllers.CellController;

public class Board extends GridPane {

	private static int size = 10;
	
	public final String name;
	
	private Cell cells[][] = new Cell[size][size]; 
	
	
	public Board(@NamedArg("name") String name) {
		
		super();
		
		this.name = name;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				Cell cell = new Cell(j,i, this);
				cell.setOnMouseClicked(CellController.handler);
				this.add(cell, j, i);
				this.cells[i][j] = cell;
			}
		}
		
	}
	
	public Cell getCell(int row, int column) {
		if (row >= size || row < 0 || column >= size || column < 0) {
			return null;
		}
		return this.cells[row][column];
	}
	
	public static void setSize(int width) {
		
		if (width > 0) {
			size = width;
		}
		
	}
	
	public static int getSize() {
		return size;
	}


}
