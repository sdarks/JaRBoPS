package application;
/**
 * Stores a position in 3d space.
 * @author Sam Dark
 *
 */
public class Position
{
	//The position in 3d space
	/** The x coordinate */
	protected double x=0;
	/** The y coordinate */
	protected double y=0;
	/** The z coordinate */
	protected double z=0;
	
	/**
	 * Creates a new position of (0,0,0)
	 */
	Position()
	{
		setPosition(0,0,0);
	}
	/**
	 * Creates a new position object with custom position
	 * @param inpX The x component of the position
	 * @param inpY The y component of the position
	 * @param inpZ The z component of the position
	 */
	Position(double inpX, double inpY, double inpZ)
	{
		setPosition(inpX,inpY,inpZ);
	}
	/**
	 * Creates a new position object with a custom position given an array of doubles
	 * @param inpPosition The custom {x,y,z} components in an array of doubles
	 */
	Position(double[] inpPosition)
	{
		setPosition(inpPosition);
	}
	/**
	 * Sets the position to the input one.
	 * @param inpX The x component of the position
	 * @param inpY The y component of the position
	 * @param inpZ The z component of the position
	 */
	public void setPosition(double inpX, double inpY, double inpZ)
	{
		x=inpX;
		y=inpY;
		z=inpZ;
	}
	/**
	 * Sets the position to one specified in an array of doubles
	 * @param inpPosition The array of doubles to set to, in order {x,y,z}
	 */
	public void setPosition(double[] inpPosition)
	{
		x=inpPosition[0];
		y=inpPosition[1];
		z=inpPosition[2];
	}
	/**
	 * Gets an array of doubles containing the position.
	 * @return An array of doubles for the position {x,y,z}
	 */
	public double[] getPosition()
	{
		double[] output= {x,y,z};
		return output;
	}
	/**
	 * Gets the x component of the position
	 * @return The x component
	 */
	public double getX()
	{
		return x;
	}
	/**
	 * Gets the y component of the position
	 * @return The y component
	 */
	public double getY()
	{
		return y;
	}
	/**
	 * Gets the z component of the position
	 * @return The z component
	 */
	public double getZ()
	{
		return z;
	}
	/**
	 * Sets the x coord of the position to the input
	 * @param inpX The x coord to change to
	 */
	public void setX(double inpX)
	{
		x=inpX;
	}
	/**
	 * Sets the y coord of the position to the input
	 * @param inpY The y coord to change to
	 */
	public void setY(double inpY)
	{
		y=inpY;
	}
	/**
	 * Sets the z coord of the position to the input
	 * @param inpZ The z coord to change to
	 */
	public void setZ(double inpZ)
	{
		z=inpZ;
	}
}
