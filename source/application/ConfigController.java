package application;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import application.ConfigCreatorSim;

/**
 * A controller for controlling the JavaFX GUI for config creation
 * @author Sam Dark
 *
 */
public class ConfigController 
{
	@FXML
	private MenuBar menu;
	@FXML
	private ChoiceBox addObjectBox;
	@FXML
	private ChoiceBox removeObjectBox;
	@FXML
	private ChoiceBox currentObjectBox;
	@FXML
	private Button addObjectButton;
	@FXML
	private Button removeObjectButton;
	@FXML
	private Slider gravSlider;
	@FXML
	private Slider elasSlider;
	@FXML
	private Text gravText;
	@FXML
	private Text elasText;
	
	//Rotation and translation buttons
	@FXML
	private Button xRPlus;
	@FXML
	private Button xRMinus;
	@FXML
	private Button xTPlus;
	@FXML
	private Button xTMinus;
	
	@FXML
	private Button yRPlus;
	@FXML
	private Button yRMinus;
	@FXML
	private Button yTPlus;
	@FXML
	private Button yTMinus;
	
	@FXML
	private Button zRPlus;
	@FXML
	private Button zRMinus;
	@FXML
	private Button zTPlus;
	@FXML
	private Button zTMinus;
	
	@FXML
	private Slider velXSlider;
	@FXML
	private Slider velYSlider;
	@FXML
	private Slider velZSlider;
	@FXML
	private Text velXText;
	@FXML
	private Text velYText;
	@FXML
	private Text velZText;
	
	
	@FXML
	private CheckBox fineControlT;
	@FXML
	private CheckBox fineControlR;
	@FXML
	private Button testWindowButton;
	
	@FXML
	private Slider inverseMassSlider;
	@FXML
	private CheckBox canMoveBox;
	@FXML
	private Text inverseMassText;
	@FXML
	private CheckBox gravityBox;
	
	//Menu stuff
	@FXML
	private MenuItem loadMenuItem;
	@FXML
	private MenuItem newMenuItem;
	@FXML
	private MenuItem saveMenuItem;
	
	/** The simulation that is used to run the test window */
	private ConfigCreatorSim mainSim;//Paused sim shown in the config window
	/** Currently selected object */
	private int curObj=0;//Currently selected object
	/** Names of the objects in the simulation */
	private ObservableList<String> objNames;
	/** The list of possible objects that can be loaded (filenames) */
	private ArrayList<String> possibleObjects;//List of possible objects that can be loaded in
	/** Rendering objects*/
	private ArrayList<CustomObject> objects= new ArrayList<CustomObject>();
	/** Low poly physics objects*/
	private ArrayList<CustomObject> lowPolyObjects= new ArrayList<CustomObject>();
	/** Is fine control for translation selected */
	private boolean fineCtrlT=false;
	/** Is fine control for rotation selected */
	private boolean fineCtrlR=false;
	/** The amount the translation increments with fine control selected */
	private double fcInc=0.2f;//Fine control increment for translation
	/** The amount the translation increments without fine control selected */
	private double inc=2f;//Translation increment
	/** The amount the rotation increments with fine control selected */
	private double fcRInc=2;//Fine control rotation increment
	/** The amount the rotation increments without fine control selected */
	private double rInc=10;//Rotation increment
	/** The initial string put in the "save config" box when the user goes to save a config */
	private String configFileName="defaultConfig.conf";
	/** The inverse mass currently selected */
	private int iMass=0;
	/** The list of the possible loadable configs filenames */
	private ObservableList<String> configsList;//List of possible configs
	/** The currently selected gravity value from the slider */
	private double grav;//Config wide gravity
	/** The currently selected elasticity value from the slider */
	private double elas;//Config wide elasticity
	/**
	 * Creates a config controller with no sim
	 */
	public ConfigController()
	{
		this(null);
	}
	/**
	 * Creates a config controller with an input sim
	 * @param inpSim The input sim
	 */
	public ConfigController(ConfigCreatorSim inpSim)
	{
		mainSim=inpSim;
	}
	/**
	 * Sets the simulation running in the config creator
	 * @param inpSim The sim to set to
	 */
	public void setMainSim(ConfigCreatorSim inpSim)
	{
		mainSim=inpSim;
	}
	/**
	 * Initializes the config controller
	 */
	@FXML
	private void initialize()
	{
		mainSim=new ConfigCreatorSim();//Create a new sim for the config creator
		
		//Get all the possible objects that can be used in config creation
		File dir = new File("objects");
    	File[] files= dir.listFiles(
    	new FilenameFilter() 
    	{ 
    		public boolean accept(File dir, String filename)
    	    { 
    			return filename.endsWith(".obj"); 
    	    }
    	});
    	ArrayList<String> filenames = new ArrayList<String>();
    	for(int i=0; i<files.length; i++)
    	{
    		filenames.add(files[i].getName());
    	}
    	possibleObjects=filenames;
    	//Remove all the low poly objects as they are just used for physics
    	int k=0;
    	while(k<possibleObjects.size())
    	{
    		if(possibleObjects.get(k).substring(0,7).matches("lowPoly"))
    		{
    			possibleObjects.remove(k);
    			k--;
    		}
    		k++;	
    	}
    	//Add the possible objects to the add object box
    	for(int i=0;i<possibleObjects.size();i++)
    	{
    			addObjectBox.getItems().add(possibleObjects.get(i));
    	}
    	//Pre load the objects
    	preLoadPossibleObjects();
    	//Setup the actions for the various buttons and menus
		setActions();
		openTestWindow();
		
	}
	/**
	 * Opens a preview window for the config creator UI to see changes to the config
	 */
	private void openTestWindow()
	{
		//Open the test window
    	String current="";
		try 
		{
			current = new java.io.File( "." ).getCanonicalPath();
		} 
		catch (IOException ex) 
		{
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
    	
		mainSim.runSimInConfigMode(current+"/configs/"+configFileName);
		new Thread(mainSim).start();
		objNames=FXCollections.observableList(mainSim.getObjectNames());
		objNames.addListener(new ListChangeListener() 
		{
			 
            @Override
            public void onChanged(ListChangeListener.Change change) 
            {
                currentObjectBox.setItems(objNames);
            }
        });
		//Set the object boxes to contain the items now in the config window
		currentObjectBox.setItems(objNames);
		removeObjectBox.setItems(objNames);
		gravSlider.adjustValue(mainSim.getGravity());
		elasSlider.adjustValue(mainSim.getElasticity());
		gravText.setText(String.format("%.3g%n", mainSim.getGravity()));
	}
	/**
	 * Preloads the possible objects that can be added to the config so that there is no delay when adding an object to the config
	 */
	private void preLoadPossibleObjects()
	{
		for(int i=0;i<possibleObjects.size();i++)
		{
			objects.add(ObjImporter.importObj(possibleObjects.get(i)));
			lowPolyObjects.add(ObjImporter.importObj("lowPoly"+possibleObjects.get(i)));
			objects.get(i).setName(possibleObjects.get(i));
			lowPolyObjects.get(i).setName("lowPoly"+possibleObjects.get(i));
		}
		for(int i=0;i<lowPolyObjects.size();i++)
		{
			objects.get(i).setLPVertices(lowPolyObjects.get(i).getVertices());
			objects.get(i).setLPFaces(lowPolyObjects.get(i).getFaces());
			objects.get(i).setLPName(lowPolyObjects.get(i).getName());
		}
		//Put the objects in a visible position so that they can be seen when added
		for(int j=0; j<objects.size();j++)
		{
			objects.get(j).getPosition().setZ(-50f);
			lowPolyObjects.get(j).getPosition().setZ(-50f);
		}
	}
	/**
	 * Sets up the actions for all the buttons and menus in the GUI
	 */
	private void setActions()
	{
		setMiscButtons();
		setTranslateButtons();
		setRotateButtons();
		setMenuButtons();
		setExtraButtons();
		setVelocitySliders();
	}
	/**
	 * Sets up the miscellaneous buttons and menus etc
	 */
	private void setMiscButtons()
	{
		testWindowButton.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    			openTestWindow();
    	    }
    	});
		currentObjectBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() 
		{
			public void changed(ObservableValue obsVal, Number val, Number newVal)
			{
				//Update the currently selected object and associated sliders/boxes
				curObj=newVal.intValue();
				if(curObj!=-1)
				{
					gravityBox.setSelected(mainSim.getObjects().get(curObj).getGravity());
			    	canMoveBox.setSelected(mainSim.getObjects().get(curObj).canMove());
			    	inverseMassSlider.setValue(mainSim.getObjects().get(curObj).getInverseMass());
			    	Position tempVel=mainSim.getObjects().get(curObj).getVel();
			    	velXSlider.adjustValue(tempVel.getX());
			    	velYSlider.adjustValue(tempVel.getY());
			    	velZSlider.adjustValue(tempVel.getZ());
				}
			}
		});
		removeObjectButton.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	//Remove the object and update the list of object names
    			mainSim.removeObject(removeObjectBox.getSelectionModel().getSelectedIndex());
    			objNames.clear();
    			objNames.addAll(FXCollections.observableList(mainSim.getObjectNames()));
    	    }
    	});
		fineControlT.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    			fineCtrlT=fineControlT.isSelected();
    	    }
    	});
		fineControlR.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    			fineCtrlR=fineControlR.isSelected();
    	    }
    	});
		addObjectButton.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	//Add an object, init the textures for it and update the objects list
    			mainSim.addObject(new CustomObject(objects.get(addObjectBox.getSelectionModel().getSelectedIndex())),new CustomObject(lowPolyObjects.get(addObjectBox.getSelectionModel().getSelectedIndex())));
    			mainSim.initTextures();
    			objNames.clear();
    			objNames.addAll(FXCollections.observableList(mainSim.getObjectNames()));
    	    }
    	});
	}
	/**
	 * Setup the actions for the extra buttons for objects (inverseMassSlider,canMoveBox and gravityBox)
	 */
	private void setExtraButtons()
	{
		inverseMassSlider.valueProperty().addListener(new ChangeListener<Number>() 
		{
            public void changed(ObservableValue<? extends Number> observable,
                Number oldValue, Number updatedValue) 
            {
            	iMass=updatedValue.intValue();
            	inverseMassText.setText(""+updatedValue.intValue());
            	if(curObj!=-1)
            		mainSim.getObjects().get(curObj).setInverseMass(iMass);
            }
        });
		canMoveBox.selectedProperty().addListener(new ChangeListener<Boolean>() 
		{
			public void changed(ObservableValue<? extends Boolean> obsVal, Boolean oldVal, Boolean newVal)
			{
				if(curObj!=-1)
					mainSim.getObjects().get(curObj).setCanMove(newVal);
			}
		});
		gravityBox.selectedProperty().addListener(new ChangeListener<Boolean>() 
		{
			public void changed(ObservableValue<? extends Boolean> obsVal, Boolean oldVal, Boolean newVal)
			{
				if(curObj!=-1)
					mainSim.getObjects().get(curObj).setGravity(newVal);
			}
		});
		gravSlider.valueProperty().addListener(new ChangeListener<Number>() 
		{
            public void changed(ObservableValue<? extends Number> observable,
                Number oldValue, Number updatedValue) 
            {
            	grav=updatedValue.doubleValue();
            	gravText.setText(String.format("%.3g%n", updatedValue.doubleValue()));
            }
        });
		elasSlider.valueProperty().addListener(new ChangeListener<Number>() 
		{
			public void changed(ObservableValue<? extends Number> observable,
				Number oldValue, Number updatedValue) 
		    {
				elas=updatedValue.doubleValue();
				elasText.setText(String.format("%.3g%n", updatedValue.doubleValue()));
		    }
		});
	}
	/**
	 * Setup the actions for the menu buttons
	 */
	private void setMenuButtons()
	{
		loadMenuItem.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	//Make a load config window and load the resulting config
    			final Stage dialogStage = new Stage();
    			VBox dialogBox = new VBox(10);
    			dialogBox.getChildren().add(new Text("Select a Config"));
    			dialogStage.setTitle("Load Config");
    			Scene dialogScene = new Scene(dialogBox, 250, 100);
    			dialogStage.setScene(dialogScene);
    			dialogStage.show();
    			ChoiceBox configChoiceBox=new ChoiceBox(getLoadableConfigs());
    			dialogBox.getChildren().add(configChoiceBox);
    			Button loadButton= new Button("Load");
    			loadButton.setOnAction(new EventHandler<ActionEvent>() 
    			{
    	    	    @Override public void handle(ActionEvent e) 
    	    	    {
    	    	    	configFileName=(String) configChoiceBox.getSelectionModel().getSelectedItem();
    	    	    	dialogStage.close();
    	    	    }
    	    	});
    			dialogBox.getChildren().add(loadButton);
    	    }
    	});
		saveMenuItem.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	//Make a save config window and save the config as the filename given
    			final Stage dialogStage = new Stage();
    			VBox dialogBox = new VBox(10);
    			dialogBox.getChildren().add(new Text("Filename to save:"));
    			dialogStage.setTitle("Save Config");
    			Scene dialogScene = new Scene(dialogBox, 250, 100);
    			dialogStage.setScene(dialogScene);
    			dialogStage.show();
    			TextField textField=new TextField("ConfigName");
    			Text confText= new Text(".conf");
    			confText.setLayoutX(200);
    			AnchorPane pane = new AnchorPane();
    			AnchorPane.setLeftAnchor(textField,0.0);
    			AnchorPane.setRightAnchor(confText, 70.0);
    			AnchorPane.setTopAnchor(textField, 10.0);
    			AnchorPane.setTopAnchor(confText, 15.0);
    			pane.getChildren().addAll(textField,confText);
    			dialogBox.getChildren().add(pane);
    			Button saveButton= new Button("Save");
    			saveButton.setOnAction(new EventHandler<ActionEvent>() 
    			{
    	    	    @Override public void handle(ActionEvent e) 
    	    	    {
    	    	    	String fileNameToSave=textField.getText()+".conf";
    	    	    	try 
    	    	    	{
    	    	    		ArrayList<CustomObject> objs= mainSim.getObjects();
							ConfigSaver.saveConfig(fileNameToSave, objs, grav, elas);
						} 
    	    	    	catch (IOException e1) 
    	    	    	{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
    	    	    	configsList.add(fileNameToSave);
    	    	    	dialogStage.close();
    	    	    }
    	    	});
    			dialogBox.getChildren().add(saveButton);
    	    }
    	});
		newMenuItem.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	configFileName="defaultConfig.conf";
    	    }
    	});
	}
	
	/**
	 * Setup the actions for the rotate buttons
	 */
	private void setRotateButtons()
	{
		xRPlus.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	if(fineCtrlR)
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeRX(curObj, fcRInc);
    	    	}
    	    	else
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeRX(curObj, rInc);
    	    	}
    	    }
    	});
		xRMinus.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	if(fineCtrlR)
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeRX(curObj, -fcRInc);
    	    	}
    	    	else
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeRX(curObj, -rInc);
    	    	}
    	    }
    	});
		yRPlus.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	if(fineCtrlR)
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeRY(curObj, fcRInc);
    	    	}
    	    	else
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeRY(curObj, rInc);
    	    	}
    	    }
    	});
		yRMinus.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	if(fineCtrlR)
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeRY(curObj, -fcRInc);
    	    	}
    	    	else
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeRY(curObj, -rInc);
    	    	}
    	    }
    	});
		zRPlus.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	if(fineCtrlR)
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeRZ(curObj, fcRInc);
    	    	}
    	    	else
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeRZ(curObj, rInc);
    	    	}
    	    }
    	});
		zRMinus.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	if(fineCtrlR)
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeRZ(curObj, -fcRInc);
    	    	}
    	    	else
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeRZ(curObj, -rInc);
    	    	}
    	    }
    	});
		
	}
	/**
	 * Setup the actions for the translate buttons
	 */
	private void setTranslateButtons()
	{
		xTPlus.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	if(fineCtrlT)
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeX(curObj, fcInc);
    	    	}
    	    	else
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeX(curObj, inc);
    	    	}
    	    }
    	});
		xTMinus.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	if(fineCtrlT)
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeX(curObj, -fcInc);
    	    	}
    	    	else
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeX(curObj, -inc);
    	    	}
    	    }
    	});
		yTPlus.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	if(fineCtrlT)
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeY(curObj, fcInc);
    	    	}
    	    	else
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeY(curObj, inc);
    	    	}
    	    }
    	});
		yTMinus.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	if(fineCtrlT)
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeY(curObj, -fcInc);
    	    	}
    	    	else
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeY(curObj, -inc);
    	    	}
    	    }
    	});
		zTPlus.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	if(fineCtrlT)
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeZ(curObj, fcInc);
    	    	}
    	    	else
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeZ(curObj, inc);
    	    	}
    	    }
    	});
		zTMinus.setOnAction(new EventHandler<ActionEvent>() 
		{
    	    @Override public void handle(ActionEvent e) 
    	    {
    	    	if(fineCtrlT)
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeZ(curObj, -fcInc);
    	    	}
    	    	else
    	    	{
    	    		if(curObj!=-1)
    	    			mainSim.changeZ(curObj, -inc);
    	    	}
    	    }
    	});
	}
	/**
	 * Setup the actions for the initial velocity sliders
	 */
	private void setVelocitySliders()
	{
		velXSlider.valueProperty().addListener(new ChangeListener<Number>() 
		{
			public void changed(ObservableValue<? extends Number> observable,
				Number oldValue, Number updatedValue) 
			{
				if(curObj!=-1)
					mainSim.setXVel(curObj,updatedValue.intValue());
				velXText.setText(""+updatedValue.intValue());
			}
		});
		velYSlider.valueProperty().addListener(new ChangeListener<Number>() 
		{
			public void changed(ObservableValue<? extends Number> observable,
				Number oldValue, Number updatedValue) 
			{
				if(curObj!=-1)
					mainSim.setYVel(curObj,updatedValue.intValue());
				velYText.setText(""+updatedValue.intValue());
			}
		});
		velZSlider.valueProperty().addListener(new ChangeListener<Number>() 
		{
			public void changed(ObservableValue<? extends Number> observable,
				Number oldValue, Number updatedValue) 
			{
				if(curObj!=-1)
					mainSim.setZVel(curObj,updatedValue.intValue());
				velZText.setText(""+updatedValue.intValue());
			}
		});
	}
	/**
	 * Gets the list of possible loadable configs
	 * @return The list of configs as an ObservableList of Strings
	 */
	private ObservableList<String> getLoadableConfigs()
	{
		//Looks for all the files with the config extension in the config folder
		//and returns them as an ObservableList
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
    	return FXCollections.observableArrayList(filenames);
	}
	/**
	 * Sets the list of loadable configs to the input list
	 * @param inpConfigs The config list to set to
	 */
	public void setConfigList(ObservableList<String> inpConfigs)
	{
		configsList=inpConfigs;
	}
}
