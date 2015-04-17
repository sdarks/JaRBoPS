package application;
/**
 * A class for representing a vertex and its associated normal. Contains a position and normal vector.
 * @author Sam Dark
 *
 */
public class Vertex extends Position
{
	/** The x component of the normal of the vertex */
	private double normalX=0;//x component of the normal
	/** The y component of the normal of the vertex */
	private double normalY=0;//y component of the normal
	/** The z component of the normal of the vertex */
	private double normalZ=0;//z component of the normal
	/** The x texture coord of the vertex */
	private double textureX=0;
	/** The y texture coord of the vertex */
	private double textureY=0;
	/** The z texture coord of the vertex (should be 0)*/
	private double textureZ=0;
	/**
	 * Creates a default vertex with position (0,0,0) and normal (0,0,0).
	 */
	Vertex()
	{
		super(0,0,0);
		setNormal(0,0,0);
	}
	/**
	 * Creates a vertex with a normal of (0,0,0) and position (vX,vY,vZ).
	 * @param vX The x component of the vertex position
	 * @param vY The y component of the vertex position
	 * @param vZ The z component of the vertex position
	 */
	Vertex(double vX,double vY,double vZ)
	{
		super(vX,vY,vZ);
		setNormal(0,0,0);
	}
	/**
	 * Creates a vertex with position (vX,vY,vZ) and normal (nX,nY,nZ).
	 * @param vX The x component of the vertex position
	 * @param vY The y component of the vertex position
	 * @param vZ The z component of the vertex position
	 * @param nX The x component of the normal vector
	 * @param nY The y component of the normal vector
	 * @param nZ The z component of the normal vector
	 */
	Vertex(double vX,double vY,double vZ,double nX,double nY,double nZ)
	{
		super(vX,vY,vZ);
		setNormal(nX,nY,nZ);
	}
	/**
	 * Creates a vertex with position defined by inpVertex and normal vector defined by inpNormal
	 * @param inpVertex The array of doubles containing the vertex position in the form {x,y,z}
	 * @param inpNormal The array of doubles containing the normal vector in the form {x,y,z}
	 */
	Vertex(double[] inpVertex, double[] inpNormal)
	{
		super(inpVertex);
		setNormal(inpNormal);
	}
	/**
	 * Sets the contents of this vertex equal to that of another, essentially making it a copy of the input vertex
	 * @param inpVert The vertex to copy
	 */
	public void copyOf(Vertex inpVert)
	{
		x=inpVert.x;
		y=inpVert.y;
		z=inpVert.z;
		normalX=inpVert.normalX;
		normalY=inpVert.normalY;
		normalZ=inpVert.normalZ;
		textureX=inpVert.textureX;
		textureY=inpVert.textureY;
		textureZ=inpVert.textureZ;
	}
	/**
	 * Sets the normal x, y and z to (nX,nY,nZ)
	 * @param nX The x component of the normal
	 * @param nY The y component of the normal
	 * @param nZ The z component of the normal
	 */
	public void setNormal(double nX,double nY,double nZ)
	{
		normalX=nX;
		normalY=nY;
		normalZ=nZ;
	}
	/**
	 * Sets the normal x, y and z to the values in inpNormal
	 * @param inpNormal The array of doubles containing the new normal in the form {x,y,z}
	 */
	public void setNormal(double[] inpNormal)
	{
		normalX=inpNormal[0];
		normalY=inpNormal[1];
		normalZ=inpNormal[2];
	}
	/**
	 * Gets an array of doubles containing the normal vector x, y and z components
	 * @return The array of doubles containing the normal in the form {x,y,z}
	 */
	public double[] getNormal()
	{
		double[] output={normalX,normalY,normalZ};
		return output;
	}
	/**
	 * Sets the texture coords x, y and z to the values in inpNormal
	 * @param textureCoords The Position object containing the new texture coords.
	 */
	public void setTextureCoords(Position textureCoords)
	{
		textureX=textureCoords.getX();
		textureY=textureCoords.getY();
		textureZ=textureCoords.getZ();
	}
	/**
	 * Gets an array of doubles containing the texture coordinates x, y and z components
	 * @return The array of doubles containing the texture coords in the form {x,y,z}
	 */
	public double[] getTextureCoords()
	{
		double[] output={textureX,textureY,textureZ};
		return output;
	}
}
