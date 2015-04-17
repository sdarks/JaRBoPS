package application;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import application.MainApp;
import application.MainSimulation;
/**
 * The GUI controller for the main window. 
 * Controls all the buttons and menus etc
 * @author Sam Dark
 *
 */
public class MainGUIController 
{
	@FXML
	private Button runButton;
	@FXML
	private ChoiceBox configSelect;
	@FXML
	private Button configCreateButton;
	/** The main application, used to minimise it when the simulation or config window is opened */
	private MainApp mainApp;//The main application with stage etc. Used for minimising.
	/** The simulation running the physics (not config simulation or config window) */
	private MainSimulation mainSim;//The simulation running the physics (not config simulation)
	/** The list of possible configs that can be loaded (for the drop down box) */
	private ObservableList<String> configs;//The possible configs to load
	
	/**
	 * Initialise without a simulation or application
	 */
	public MainGUIController()
	{
		this(null,null);
	}
	
	/**
	 * Initialise with a simulation and application
	 * @param inpApp The input application. This is minimised when a simulation is run.
	 * @param inpSim The simulation to run the physics
	 */
	public MainGUIController(MainApp inpApp, MainSimulation inpSim)
	{
		mainApp=inpApp;
		mainSim=inpSim;
	}
	/**
	 * Sets the application that is minimised when another window is opened
	 * @param inpApp The application to set to
	 */
	public void setMainApp(MainApp inpApp)
	{
		mainApp=inpApp;
	}
	/**
	 * Sets the simulation used to run the physics (not config creator) with
	 * @param inpSim The simulation to use
	 */
	public void setMainSim(MainSimulation inpSim)
	{
		mainSim=inpSim;
	}
	@FXML
	/**
	 * Initialises the controller, called once on start
	 */
	private void initialize()
	{
		mainSim=new MainSimulation();//The sim the physics will be run with
		
    	initConfigFilesList();//Get the list of config files in the config folder
    	
    	//Set the actions for the two buttons
    	runButton.setOnAction(new EventHandler<ActionEvent>() 
    	{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	if(configSelect.getSelectionModel().isEmpty()==false)
    	    	{
	    	    	//When the run button is clicked run the simulation with the config selected 
	    	    	//and minimise the main window
	    	    	Stage window = mainApp.getPrimStage();
	    	        window.setIconified(true);//Minimise
	    	    	while(mainSim.runSim("configs/"+(String)configSelect.getSelectionModel().getSelectedItem())==false)
	    	    	{
	    	    		try {
	    					Thread.sleep(200);
	    				} catch (InterruptedException ex) {
	    					// TODO Auto-generated catch block
	    					ex.printStackTrace();
	    				}
	    	    	}
	    	    	window.setIconified(false);
	    	    }
    	    }
    	});
    	configCreateButton.setOnAction(new EventHandler<ActionEvent>() 
    	{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	Parent root;
    	        try 
    	        {
    	        	//When the config create is clicked
    	        	//Open the config creator window and minimise the main window
    	        	
    	        	//Load the config UI
    	        	FXMLLoader loader = new FXMLLoader();
	    	        loader.setLocation(ConfigController.class.getResource("/GUI/ConfigCreator.fxml"));
	    	        //Load the config pane
	    	        Pane configPane = (Pane) loader.load();
	    	        
	    	        //Get the controller loaded with the UI
    	            ConfigController controller = loader.getController();
    	            //Set the list of configs
    	            controller.setConfigList(configs);
    	            //Setup the window
    	            Stage stage = new Stage();
    	            stage.setTitle("Config Creator");
    	            stage.setScene(new Scene(configPane,600, 450));
    	            stage.show();//Show the stage
    	            //Minimise the main window
    	            Stage window = mainApp.getPrimStage();
        	        window.setIconified(true);//Minimise
        	        
        	        stage.showingProperty().addListener(new ChangeListener<Boolean>()
        	        {
        	        	 @Override public void changed(ObservableValue<? extends Boolean> prop, Boolean oldValue, Boolean newValue) 
        	        	 {
        		    	    	window.setIconified(false);
        	        	 }
        	        });

    	        } 
    	        catch (IOException ex) 
    	        {
    	            ex.printStackTrace();
    	        }
    	    }
    	});
		
	}
	/**
	 * Initialises the list of configs by getting all the config filenames in the config folder
	 */
	public void initConfigFilesList()
	{
		File dir = new File("configs");
    	File[] files= dir.listFiles(
    	new FilenameFilter() 
    	{ 
    		public boolean accept(File dir, String filename)
    	    { 
    			return filename.endsWith(".conf"); 
    	    }
    	});
    	ArrayList<String> filenames = new ArrayList<String>();
    	for(int i=0; i<files.length; i++)
    	{
    		filenames.add(files[i].getName());
    	}
    	
    	configs=FXCollections.observableList(filenames);
    	configSelect.setItems(configs);
    	configSelect.getSelectionModel().select(0);
	}
}
