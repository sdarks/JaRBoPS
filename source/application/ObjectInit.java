package application;
/**
 * A data structure for storing information needed to initialise objects
 * @author Sam Dark
 *
 */
public class ObjectInit 
{
	/** Filepath of the object */
	String filePath;
	/** Filepath of the low poly version of the object */
	String LPFilePath;
	/** The position of the object */
	Position pos;
	/** The rotation of the object */
	Rotation rot;
	/** Can the object move? */
	boolean canMove;
	/** The inverse mass (1/mass) of the object */
	int inverseMass;
	/** Is the object effected by gravity? */
	boolean gravity;
	/** The velocity of the object */
	Position velocity;
	
	/**
	 * Creates an empty object
	 */
	ObjectInit()
	{
		
	}
}
