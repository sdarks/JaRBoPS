package application;
/**
 * Stores a rotation in 3d space, made up of an X, Y, and Z axis rotation
 * @author Sam Dark
 *
 */
public class Rotation 
{
	/** Rotation angle about the x axis */
	private double thetaX=0;//The rotation about the x axis
	/** Rotation angle about the y axis */
	private double thetaY=0;//The rotation about the y axis
	/** Rotation angle about the z axis */
	private double thetaZ=0;//The rotation about the z axis
	/** Acceleration of the rotation in x,y,z */
	private Position acc=new Position(0,0,0);//Acceleration of the rotation
	/** Velocity of the rotation in x,y,z */
	private Position vel=new Position(0,0,0);//Velocity of the rotation
	
	
	
	/**
	 * Creates a rotation object with no rotation. 0 degrees and no vector
	 */
	Rotation()
	{
		thetaX=0;
		thetaY=0;
		thetaZ=0;
		acc=new Position(0,0,0);
		vel=new Position(0,0,0);
	}
	/**
	 * Creates a rotation object with a custom rotation in each axis
	 * @param inpX The x axis rotation
	 * @param inpY The y axis rotation
	 * @param inpZ The z axis rotation
	 */
	Rotation(double inpX, double inpY, double inpZ)
	{
		thetaX=inpX;
		thetaY=inpY;
		thetaZ=inpZ;
		acc=new Position(0,0,0);
		vel=new Position(0,0,0);
	}
	/**
	 * Creates a rotation object as a copy of an existing rotation
	 * @param inpRot The rotation to copy
	 */
	Rotation(Rotation inpRot)
	{
		thetaX=inpRot.thetaX;
		thetaY=inpRot.thetaY;
		thetaZ=inpRot.thetaZ;
		acc=inpRot.acc;
		vel=inpRot.acc;
	}
	/**
	 * Sets the rotation for each axis.
	 * @param inpX The x axis rotation
	 * @param inpY The y axis rotation
	 * @param inpZ The z axis rotation
	 */
	public void setRotation(double inpX, double inpY, double inpZ)
	{
		thetaX=inpX;
		thetaY=inpY;
		thetaZ=inpZ;
	}
	/**
	 * Gets an array of doubles with the rotation information in.
	 * @return An array of doubles {x,y,z}
	 */
	public double[] getRotation()
	{
		double[] output= {thetaX,thetaY,thetaZ};
		return output;
	}
	
	/**
	 * Gets the x axis rotation
	 * @return The x axis rotation
	 */
	public double getX()
	{
		return thetaX;
	}
	/**
	 * Gets the y axis rotation
	 * @return The y axis rotation
	 */
	public double getY()
	{
		return thetaY;
	}
	/**
	 * Gets the z axis rotation 
	 * @return The z axis rotation
	 */
	public double getZ()
	{
		return thetaZ;
	}
	/**
	 * Sets the x axis component of the rotation
	 * @param inpX The x to set to
	 */
	public void setX(double inpX)
	{
		
		thetaX=inpX;
		if(thetaX>=360)
			thetaX-=360;
	}
	/**
	 * Sets the y axis component of the rotation
	 * @param inpY The y to set to
	 */
	public void setY(double inpY)
	{
		thetaY=inpY;
		if(thetaY>=360)
			thetaY-=360;
	}
	/**
	 * Sets the z axis component of the rotation
	 * @param inpZ The z to set to
	 */
	public void setZ(double inpZ)
	{
		thetaZ=inpZ;
		if(thetaZ>=360)
			thetaZ-=360;
	}
	/**
	 * Gets the acceleration of the rotation, has an acceleration of each component
	 * @return The acceleration
	 */
	public Position getAcc()
	{
		return acc;
	}
	/**
	 * Sets the acceleration of the rotation, has an acceleration of each component
	 * @param inpAcc the acceleration to set to
	 */
	public void setAcc(Position inpAcc)
	{
		acc=inpAcc;
	}
	/**
	 * Gets the velocity of the rotation, has a velocity of each component
	 * @return The velocity
	 */
	public Position getVel()
	{
		return vel;
	}
	/**
	 * Sets the velocity of the rotation, has a velocity of each component
	 * @param inpVel The velocity to set to
	 */
	public void setVel(Position inpVel)
	{
		vel=inpVel;
	}
}
