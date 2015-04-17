package application;

import java.util.ArrayList;

/**
 * Simple storage class for storing information about a configuration
 * @author Sam Dark
 *
 */
public class Config 
{
	/** Information about the objects in the config */
	public ArrayList<ObjectInit> objs= new ArrayList<ObjectInit>();//Information about the objects in the config
	/** The gravity value of the simulation */
	public double grav=0;//gravity
	/** The elasticity value of the simulation */
	public double elas=0;//elasticity ("bounciness")
	
}
