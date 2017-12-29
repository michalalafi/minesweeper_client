package vastja.minesweeper_client.controllers;

import org.w3c.dom.events.Event;

import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import vastja.minesweeper_client.App;
import vastja.minesweeper_client.nodes.Cell;
import vastja.minesweeper_client.utils.Client;

public class CellController {

	public static EventHandler<? super MouseEvent> handler = event -> {
		
		if (((MouseEvent) event).getButton() == MouseButton.PRIMARY) {
			reveal(event);
		}
		else if (((MouseEvent) event).getButton() == MouseButton.SECONDARY) {
			predictMine(event);
		}
		
	};
	
	private static void reveal(MouseEvent event) {
		if (event.getSource() instanceof Cell) {
			Cell cell = (Cell) event.getSource();
			if (!cell.isFlagged()) {
				App.getGameController().reveal(cell.getRow(), cell.getColumn());
			}
		}
	}
	
	private static void predictMine(MouseEvent event) {
		if (event.getSource() instanceof Cell) {
			Cell cell = (Cell) event.getSource();
			if (!cell.isFlagged()) {
				cell.predictMine();
			}
			else {
				cell.cancellPredicted();
			}
		}
	}
}
