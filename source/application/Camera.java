package application;
/**
 * A class for storing camera position and rotation and adjusting it.
 * Used in the renderer.
 * 
 * @author Sam Dark
 * @see Renderer
 */
public class Camera 
{
	/** The position of the camera in 3d space */
	private Position position;
	/** The rotation of the camera about x,y and z axis */
	private Rotation rot;
	
	/**
	 * Creates a camera object with initial position and rotation values
	 * @param x Initial x position
	 * @param y Initial y position
	 * @param z Initial z position
	 * @param rotX Initial x axis rotation
	 * @param rotY Initial y axis rotation 
	 * @param rotZ Initial z axis rotation
	 */
	Camera(double x, double y, double z,double rotX, double rotY, double rotZ)
	{
		position=new Position(x,y,z);
		rot=new Rotation(rotX,rotY,rotZ);
	}
	/**
	 * Gets the x y and z axis rotations of the camera
	 * @return The camera rotation
	 */
	public Rotation getRotation()
	{
		return rot;
	}
	/**
	 * Increases the cameras x axis rotation by an input value
	 * @param inc The amount to increase by, negative decreases
	 */
	public void incRotX(double inc)
	{
		rot.setX(rot.getX()+inc);
	}
	/**
	 * Increases the cameras y axis rotation by an input value
	 * @param inc The amount to increase by, negative decreases
	 */
	public void incRotY(double inc)
	{
		rot.setY(rot.getY()+inc);
	}
	/**
	 * Increases the cameras z axis rotation by an input value
	 * @param inc The amount to increase by, negative decreases
	 */
	public void incRotZ(double inc)
	{
		rot.setZ(rot.getZ()+inc);
	}
	/**
	 * Gets the cameras position in the world
	 * @return The camera position
	 */
	public Position getPosition()
	{
		return position;
	}
	/**
	 * Increases the cameras x position by an input value
	 * @param inc The amount to increase by, negative decreases
	 */
	public void incX(double inc)
	{
		position.setX(position.getX()+inc);
	}
	/**
	 * Increases the cameras y position by an input value
	 * @param inc The amount to increase by, negative decreases
	 */
	public void incY(double inc)
	{
		position.setY(position.getY()+inc);
	}
	/**
	 * Increases the cameras z position by an input value
	 * @param inc The amount to increase by, negative decreases
	 */
	public void incZ(double inc)
	{
		position.setZ(position.getZ()+inc);
	}
}
