package application;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * The main application that is executed. Start point of the program.
 * @author Sam Dark
 *
 */
public class MainApp extends Application 
{
	/** The main stage the UI is on */
	private Stage primaryStage;
	/** The main pane the UI is in */
	private Pane mainPane;
	 
	@Override
	/**
	 * Starts the application
	 */
	public void start(Stage primaryStage) 
	{
	    this.primaryStage = primaryStage;
	    this.primaryStage.setTitle("JaRBoPS Menu");

	    initMainLayout();

	}

	/**
	 * Initialises the main GUI pane
	 */
	public void initMainLayout() 
	{
		try
		{
			//Load main GUI.
	    	FXMLLoader loader = new FXMLLoader();
	    	loader.setLocation(MainApp.class.getResource("/GUI/MainGUI.fxml"));
	    	Pane mainPane = (Pane) loader.load();
	    	// Give the controller access to the main app.
	    	MainGUIController controller = loader.getController();
	    	controller.setMainApp(this);
	    	// Show the scene containing the root layout.
	        Scene scene = new Scene(mainPane);
	        primaryStage.setScene(scene);
	        primaryStage.show();//Show the UI
	    } 
		catch (IOException e) 
		{
	    	e.printStackTrace();
	    }
	}
	/**
	 * Launch the application.
	 * Calls start().
	 * @param args Program arguments
	 * 
	 */
	public static void main(String[] args) 
	{
		launch(args);//Calls start()
	}
	/**
	 * Returns the primary stage the main GUI is in
	 * @return The primary stage
	 */
	public Stage getPrimStage()
	{
		return primaryStage;
	}
}
