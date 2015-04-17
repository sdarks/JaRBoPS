package application;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;







import javafx.stage.Stage;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jogamp.opengl.util.Animator;


/**
 * The class that starts the physics simulation and reads configs for it.
 * Also calls all the object importing.
 * @author Sam Dark
 *
 */
public class MainSimulation
{
	/** The high poly renderer objects in the simulation */
	protected ArrayList<CustomObject> objects= new ArrayList<CustomObject>();
	/** The low poly physics objects in the simulation */
	protected ArrayList<CustomObject> lowPolyObjects = new ArrayList<CustomObject>();
	/** The renderer displaying the simulation to the screen */
	protected Renderer renderer = new Renderer();//Renderer for graphics
	
	/**
	 * Reads in the config at the given filepath, imports all the objects needed and starts the physics simulation and renderer.
	 * @param configFilePath The config file to run
	 * @return returns true when the sim closes
	 */
	public boolean runSim(String configFilePath)
	{
		//Prepare arraylists to store the objects in
		objects= new ArrayList<CustomObject>();
		lowPolyObjects = new ArrayList<CustomObject>();
		//Make a renderer
		renderer = new Renderer();
		//Prepare the config to pass to the physics sim
		Config config= new Config();
		try 
		{
			//Read the config file and put the information in the cofig object prepared earlier
			config=readConfig(configFilePath);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		//Import all the objects needed for the config
		for(int i=0;i<config.objs.size();i++)
		{
			objects.add(ObjImporter.importObj(config.objs.get(i).filePath));
			lowPolyObjects.add(ObjImporter.importObj(config.objs.get(i).LPFilePath));
		}
		for(int i=0;i<lowPolyObjects.size();i++)
		{
			objects.get(i).setLPVertices(lowPolyObjects.get(i).getVertices());
			objects.get(i).setLPFaces(lowPolyObjects.get(i).getFaces());
			objects.get(i).setLPName(lowPolyObjects.get(i).getName());
		}
		//Create a window to show the physics in
		final JFrame frame = new JFrame("JaRBoPS");
		frame.setSize(1820,980);
		frame.add(renderer.getCanvas());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setFocusable(true);
		//frame.setUndecorated(true);//Uncomment for borderless window
		frame.setVisible(true);
		//Give the window to the renderer
		renderer.setFrame(frame);
		//Create and start a new simulation
		PhysicsSimulation sim= new PhysicsSimulation();//Create a sim
		sim.init(renderer,objects,config);//Init the sim
		sim.start();//Start the sim
		//An animator to refresh the canvas, will refresh as fast as possible unless v-sync is on
		Animator animator = new Animator(renderer.getCanvas());
		animator.start();
		//While the renderer is running keep the animator going and wait for it to close
		while(renderer.getClosing()==false)
		{
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Closed Simulation");
		//The renderer has stopped so stop the animator
		animator.stop();
		animator=null;
		return true;
	}
	/**
	 * Reads a config file and returns the information as a config object
	 * @param filePath The path to the config file to read
	 * @return The contents of the config file
	 * @throws IOException if it has trouble reading the config
	 */
	protected static Config readConfig(String filePath) throws IOException
	{
		int noObjects=0;
		
		File config= new File(filePath);
		BufferedReader reader = new BufferedReader(new FileReader(config));
		String line;
		//Number of objects
		if((line = reader.readLine()) != null)
		{
			noObjects=Integer.parseInt(line);
		}
		ArrayList<ObjectInit> iObjects= new ArrayList<ObjectInit>();
		Config conf= new Config();
		//File path for high poly object
		for(int i=0;i<noObjects;i++)
		{
			if((line = reader.readLine()) != null)
			{
				iObjects.add(new ObjectInit());
				iObjects.get(i).filePath=line;
			}
		}
		//File path for low poly
		for(int i=0;i<noObjects;i++)
		{
			if((line = reader.readLine()) != null)
				iObjects.get(i).LPFilePath=line;
		}
		//Position
		for(int i=0;i<noObjects;i++)
		{
			if((line = reader.readLine()) != null)
			{
				 Scanner lineScanner = new Scanner(line);
			     while (!lineScanner.hasNextDouble())
			     {
			    	 lineScanner.next();
			     }
			     double x = lineScanner.nextDouble();
			     double y = lineScanner.nextDouble();
			     double z = lineScanner.nextDouble();
			     iObjects.get(i).pos=new Position(x,y,z);
			}
		}
		//Rotation
		for(int i=0;i<noObjects;i++)
		{
			if((line = reader.readLine()) != null)
			{
				 Scanner lineScanner = new Scanner(line);
			     while (!lineScanner.hasNextDouble())
			     {
			    	 lineScanner.next();
			     }
			     double x = lineScanner.nextDouble();
			     double y = lineScanner.nextDouble();
			     double z = lineScanner.nextDouble();
			     iObjects.get(i).rot=new Rotation(x,y,z);
			}
		}
		//CanMove
		for(int i=0;i<noObjects;i++)
		{
			if((line = reader.readLine()) != null)
			{
				iObjects.get(i).canMove=Boolean.valueOf(line);
			}
		}
		//InverseMass
		for(int i=0;i<noObjects;i++)
		{
			if((line = reader.readLine()) != null)
				iObjects.get(i).inverseMass=Integer.parseInt(line);
		}
		//Gravity
		for(int i=0;i<noObjects;i++)
		{
			if((line = reader.readLine()) != null)
				iObjects.get(i).gravity=Boolean.valueOf(line);
		}
		//Velocity
		for(int i=0;i<noObjects;i++)
		{
			if((line = reader.readLine()) != null)
			{
				 Scanner lineScanner = new Scanner(line);
				 while (!lineScanner.hasNextDouble())
				 {
					 lineScanner.next();
				 }
				 double x = lineScanner.nextDouble();
				 double y = lineScanner.nextDouble();
				 double z = lineScanner.nextDouble();
				 iObjects.get(i).velocity=new Position(x,y,z);
			}
		}
		conf.objs=iObjects;
		//Global Gravity
		if((line = reader.readLine()) != null)
			conf.grav=Double.valueOf(line);
		//Global elasticity
		if((line = reader.readLine()) != null)
			conf.elas=Double.valueOf(line);
		reader.close();
		return conf;
	}
}
