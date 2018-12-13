/**
 * @author Paul Dennis
 * Jul 23, 2018
 */
package shipyard;

import java.util.List;

import javafx.application.Application;
import javafx.stage.Stage;

import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.Scene;

@SuppressWarnings("restriction")
public class StarNetworkCanvas extends Application {

	public static final int CANVAS_WIDTH = 650;
	public static final int CANVAS_HEIGHT = 650;
	
	public static final int SCALE = 6;
	
	StarNetwork map;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		Group rootGroup = new Group();

        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        canvas.setFocusTraversable(true);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        
        map = new StarNetwork();
        drawSystems(gc);
        
        
        rootGroup.getChildren().add(canvas);
        canvas.setOnKeyPressed(keyPressEvent -> {
        	if (keyPressEvent.getCode() == KeyCode.SPACE) {
        		System.out.println("Advancing the clock");
        		SpaceTunnel newTunnel = map.makeConnection();
        		drawNewTunnel(gc, newTunnel);
        		System.out.println("Connected = " + map.isConnected());
        	}
        });
        
        Scene scene = new Scene(rootGroup, CANVAS_WIDTH, CANVAS_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
	}
	
	private void drawSystems (GraphicsContext gc) {
		map.getSystems().forEach(system -> {
			gc.strokeOval(system.getXCoord() * SCALE, system.getYCoord() * SCALE, 10, 10);
			gc.strokeText(system.getName(), system.getXCoord() * SCALE, system.getYCoord() * SCALE - 5);
		});
	}
	
	@SuppressWarnings("unused")
	private void drawNewTunnels (GraphicsContext gc, List<SpaceTunnel> newTunnels) {
		newTunnels.forEach(tunnel -> drawNewTunnel(gc, tunnel));
	}
	
	private void drawNewTunnel (GraphicsContext gc, SpaceTunnel newTunnel) {
		Starsystem first = newTunnel.getFirstSystem();
		Starsystem second = newTunnel.getSecondSystem();
		gc.strokeLine(first.getXCoord() * SCALE + 5, first.getYCoord() * SCALE + 5, second.getXCoord() * SCALE + 5, second.getYCoord() * SCALE + 5);
	}
	
	@SuppressWarnings("unused")
	private void drawTunnels (GraphicsContext gc) {
		map.getTunnels().forEach(tunnel -> {
			Starsystem first = tunnel.getFirstSystem();
			Starsystem second = tunnel.getSecondSystem();
			gc.strokeLine(first.getXCoord() * SCALE + 5, first.getYCoord() * SCALE + 5, second.getXCoord() * SCALE + 5, second.getYCoord() * SCALE + 5);
		});
	}
	
	public static void main(String[] args) throws Exception {
		StarNetworkCanvas.launch(args);
	}

}
