package application;

import java.util.ArrayList;

import org.ejml.data.DenseMatrix64F;

/**
 * An extension of PhysicsSimulation that includes some extra functionality used in config editor.
 * @author Sam Dark
 * @see PhysicsSimulation
 *
 */
public class ConfigSimulation extends PhysicsSimulation
{
	/** The high poly objects waiting to be added to the simulation */
	private ArrayList<CustomObject> waitingObjects=new ArrayList<CustomObject>();//Objects waiting to be added
	/** The low poly objects waiting to be added to the simulation */
	//private ArrayList<CustomObject> waitingLPObjects=new ArrayList<CustomObject>();//Low poly objects waiting to be added
	/** Is there an object waiting to be removed? If so this will be the position of the object to remove, otherwise -1 suggests no object waiting to be removed */
	private int removeObject=-1;//The object to remove, -1 suggest no object waiting to be removed
	/**
	 * Runs the config simulation until the renderer it is using closes.
	 */
	public void run()
	{
		Boolean running=true;
		while(running==true)
		{
			//If renderer closes no point keeping the sim running in the background
			if(render.getClosing()==false)
			{
				//Config update instead of standard update ensures 
				configUpdate();
			}
			else
			{
				running=false;
			}
		}
		System.out.println("Physics closed");
	}
	/**
	 * A version of the standard update method that just adds and removes objects that are waiting to be added/removed 
	 * instead of updating object positions etc.
	 * Doesn't override the normal update so both methods can be used if required for any reason.
	 */
	private void configUpdate()
	{
		//If there are objects waiting to be added add them
		if(waitingObjects.size()>0)
		{
			for(int i=0; i<waitingObjects.size();i++)
			{
				objects.add(waitingObjects.get(i));
				//lowPolyObjects.add(waitingLPObjects.get(i));
				render.setObjects(objects);
				render.updateVerticesInScene();
				render.requestVBOUpdate();
			}
			waitingObjects= new ArrayList<CustomObject>();
		}
		//If there is an object waiting to be removed remove it
		if(removeObject!=-1)
		{
			objects.remove(removeObject);
			render.setObjects(objects);
			render.updateVerticesInScene();
			render.requestVBOUpdate();
			removeObject=-1;
		}
		render.setObjects(objects);
	}
	/**
	 * Initialises the objects in the config simulation. Slightly different to the standard initObjects method.
	 * Sets the current thread to max priority.
	 * @param initObjs The objects to initialise
	 */
	private void initObjects(ArrayList<ObjectInit> initObjs)
	{
		//Give priority to the thread running the physics
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		objects=render.initTextures(objects);
		for(int i=0; i<initObjs.size();i++)
		{
			//Apply gravity
			objects.get(i).setAcc(new Position(0,gravity,0));
			//Set position
			objects.get(i).setPosition(initObjs.get(i).pos);
			//Set Rotation
			objects.get(i).setRotation(initObjs.get(i).rot);
			//Set can move
			objects.get(i).setCanMove(initObjs.get(i).canMove);
			//Set inverseMass
			objects.get(i).setInverseMass(initObjs.get(i).inverseMass);
			//Set object names
			objects.get(i).setName(initObjs.get(i).filePath);
			//Set velocity
			objects.get(i).setVel(initObjs.get(i).velocity);
		}
		
		updateObjects(0);
		render.updateVerticesInScene();
	}
	/**
	 * Gets the names of all objects in the sim
	 * @return The names of the object (filenames minus extension)
	 */
	public ArrayList<String> getObjectNames()
	{
		ArrayList<String> output= new ArrayList<String>();
		for(int i=0; i<objects.size();i++)
		{
			output.add(objects.get(i).getName()+"("+i+")");
		}
		return output;
	}
	/**
	 * Increases or decreases the x position of an object
	 * @param objNo The number of the object to change (the position of it in the ArrayList of objects)
	 * @param inc The amount to increase/decrease the position by (negative for decrease)
	 */
	public void changeX(int objNo, double inc)
	{
		objects.get(objNo).getPosition().setX(objects.get(objNo).getPosition().getX()+inc);
	}
	/**
	 * Increases or decreases the y position of an object
	 * @param objNo The number of the object to change (the position of it in the ArrayList of objects)
	 * @param inc The amount to increase/decrease the position by (negative for decrease)
	 */
	public void changeY(int objNo, double inc)
	{
		objects.get(objNo).getPosition().setY(objects.get(objNo).getPosition().getY()+inc);
	}
	/**
	 * Increases or decreases the z position of an object
	 * @param objNo The number of the object to change (the position of it in the ArrayList of objects)
	 * @param inc The amount to increase/decrease the position by (negative for decrease)
	 */
	public void changeZ(int objNo, double inc)
	{
		objects.get(objNo).getPosition().setZ(objects.get(objNo).getPosition().getZ()+inc);
	}
	/**
	 * Increase or decreases an objects x rotation (the rotation about the x axis)
	 * @param objNo The number of the object to change (the position of it in the ArrayList of objects)
	 * @param inc The amount to increase or decrease by (negative for decrease)
	 */
	public void changeRX(int objNo, double inc)
	{
		objects.get(objNo).getRotation().setX(objects.get(objNo).getRotation().getX()+inc);
	}
	/**
	 * Increase or decreases an objects y rotation (the rotation about the y axis)
	 * @param objNo The number of the object to change (the position of it in the ArrayList of objects)
	 * @param inc The amount to increase or decrease by (negative for decrease)
	 */
	public void changeRY(int objNo, double inc)
	{
		objects.get(objNo).getRotation().setY(objects.get(objNo).getRotation().getY()+inc);
	}
	/**
	 * Increase or decreases an objects z rotation (the rotation about the z axis)
	 * @param objNo The number of the object to change (the position of it in the ArrayList of objects)
	 * @param inc The amount to increase or decrease by (negative for decrease)
	 */
	public void changeRZ(int objNo, double inc)
	{
		objects.get(objNo).getRotation().setZ(objects.get(objNo).getRotation().getZ()+inc);
	}
	/**
	 * Sets the x component of velocity of an object. This is saved as initial velocity in configs.
	 * @param objNo The number of the object to change (the position of it in the ArrayList of objects)
	 * @param inpX The number to set this component of the velocity to
	 */
	public void setXVel(int objNo, int inpX)
	{
		objects.get(objNo).setVel(new Position(inpX,objects.get(objNo).getVel().getY(),objects.get(objNo).getVel().getZ()));
	}
	/**
	 * Sets the y component of velocity of an object. This is saved as initial velocity in configs.
	 * @param objNo The number of the object to change (the position of it in the ArrayList of objects)
	 * @param inpY The number to set this component of the velocity to
	 */
	public void setYVel(int objNo, int inpY)
	{
		objects.get(objNo).setVel(new Position(objects.get(objNo).getVel().getX(),inpY,objects.get(objNo).getVel().getZ()));
	}
	/**
	 * Sets the z component of velocity of an object. This is saved as initial velocity in configs.
	 * @param objNo The number of the object to change (the position of it in the ArrayList of objects)
	 * @param inpZ The number to set this component of the velocity to
	 */
	public void setZVel(int objNo, int inpZ)
	{
		objects.get(objNo).setVel(new Position(objects.get(objNo).getVel().getX(),objects.get(objNo).getVel().getY(),inpZ));
	}
	/**
	 * Adds an object (and its associated low poly version) to the simulation. 
	 * (Puts the objects into waiting queues which are added at next update)
	 * @param obj The object to add
	 * @param lowPolyObj The low poly version of the object
	 */
	public void addObject(CustomObject obj, CustomObject lowPolyObj)
	{
		waitingObjects.add(obj);
	}
	/**
	 * Removes an object from the simulation.
	 * (Not instant, will remove at next update)
	 * @param objNo The number of the object to remove (Its position in the ArrayList of objects)
	 */
	public void removeObject(int objNo)
	{
		removeObject=objNo;
	}
	/**
	 * Gets all the objects in the simulation
	 * @return The ArrayList of objects
	 */
	public ArrayList<CustomObject> getObjects()
	{
		return objects;
	}
	
	
}
