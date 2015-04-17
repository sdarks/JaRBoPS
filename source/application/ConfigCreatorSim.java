package application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;

import javax.swing.JFrame;

import com.jogamp.opengl.util.Animator;

/**
 * An extension of MainSimulation with some extra functionality used by the config creator
 * 
 * @author Sam Dark
 *
 */
public class ConfigCreatorSim extends MainSimulation implements Runnable
{
	/** The simulation used and displayed in the window */
	private ConfigSimulation sim;//The sim used
	/**
	 * Runs the the renderer and animator
	 */
	public void run()
	{
		//An animator to refresh the canvas, will refresh as fast as possible unless v-sync is on
		Animator animator = new Animator(renderer.getCanvas());
		animator.start();
		//Wait around until the renderer is closed
		while(renderer.getClosing()==false)
		{
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Closed Simulation");
		//Stop the animator
		animator.stop();
		animator=null;
	}
	/**
	 * Runs a sim in config mode (things won't move around unless moved by the gui buttons)
	 * @param configPath The config to run from
	 */
	public void runSimInConfigMode(String configPath)
	{
		objects= new ArrayList<CustomObject>();
		lowPolyObjects = new ArrayList<CustomObject>();
		renderer = new Renderer();
		Config config= new Config();
		try 
		{
			config=readConfig(configPath);
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		
		final JFrame frame = new JFrame("Simulation Window");
		frame.setSize(800,600);
		frame.add(renderer.getCanvas());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setFocusable(true);
		//frame.setUndecorated(true);//Uncomment for fullscreen windowed
		frame.setVisible(true);
		frame.toFront();
        frame.repaint();
		renderer.setFrame(frame);

		sim= new ConfigSimulation();
		sim.init(renderer,objects,config);
		sim.start();
		
	}
	/**
	 * Gets the names of the objects in the running sim as a List of Strings
	 * @return The object names
	 */
	public List<String> getObjectNames()
	{
		return sim.getObjectNames();
	}
	/**
	 * Changes the x position value of an object
	 * @param objNo The object to change
	 * @param inc The increase in x (negative for decrease)
	 */
	public void changeX(int objNo,double inc)
	{
		sim.changeX(objNo,inc);
	}
	/**
	 * Changes the y position value of an object
	 * @param objNo The object to change
	 * @param inc The increase in y (negative for decrease)
	 */
	public void changeY(int objNo,double inc)
	{
		sim.changeY(objNo,inc);
	}
	/**
	 * Changes the z position value of an object
	 * @param objNo The object to change
	 * @param inc The increase in z (negative for decrease)
	 */
	public void changeZ(int objNo,double inc)
	{
		sim.changeZ(objNo,inc);
	}
	/**
	 * Changes the x rotation value of an object
	 * @param objNo The object to change
	 * @param inc The increase in x (negative for decrease)
	 */
	public void changeRX(int objNo,double inc)
	{
		sim.changeRX(objNo,inc);
	}
	/**
	 * Changes the y rotation value of an object
	 * @param objNo The object to change
	 * @param inc The increase in y (negative for decrease)
	 */
	public void changeRY(int objNo,double inc)
	{
		sim.changeRY(objNo,inc);
	}
	/**
	 * Changes the z rotation value of an object
	 * @param objNo The object to change
	 * @param inc The increase in z (negative for decrease)
	 */
	public void changeRZ(int objNo,double inc)
	{
		sim.changeRZ(objNo,inc);
	}
	/**
	 * Sets the initial x velocity of an object
	 * @param objNo The object to change
	 * @param inpX The x component of starting velocity
	 */
	public void setXVel(int objNo, int inpX)
	{
		sim.setXVel(objNo, inpX);
	}
	/**
	 * Sets the initial y velocity of an object
	 * @param objNo The object to change
	 * @param inpY The y component of starting velocity
	 */
	public void setYVel(int objNo, int inpY)
	{
		sim.setYVel(objNo, inpY);
	}
	/**
	 * Sets the initial z velocity of an object
	 * @param objNo The object to change
	 * @param inpZ The z component of starting velocity
	 */
	public void setZVel(int objNo, int inpZ)
	{
		sim.setZVel(objNo, inpZ);
	}
	/**
	 * Adds an object (and low poly physics version of an object) to the running simulation
	 * @param obj The object to add
	 * @param lowPolyObj The low poly version of the object to add
	 */
	public void addObject(CustomObject obj, CustomObject lowPolyObj)
	{
		sim.addObject(obj, lowPolyObj);
	}
	/**
	 * Removes an object from the running simulation
	 * @param objNo The number of the object to remove
	 */
	public void removeObject(int objNo)
	{
		sim.removeObject(objNo);
	}
	/**
	 * Returns the objects in the sim
	 * @return The objects in the currently running sim
	 */
	public ArrayList<CustomObject> getObjects()
	{
		return sim.getObjects();
	}
	/**
	 * Used to initialize textures in the renderer when needed
	 * Call this any time a new object is added or textures are changed
	 */
	public void initTextures()
	{
		renderer.initTextures(objects);
	}
	/**
	 * Gets the gravity value of the currently running simulation
	 * @return The gravity value
	 */
	public double getGravity()
	{
		return sim.getGravity();
	}
	/**
	 * Gets the elasticity value of the currently running simulation
	 * @return The elasticity value
	 */
	public double getElasticity()
	{
		return sim.getElasticity();
	}
	
}
