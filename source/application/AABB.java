package application;
import java.util.ArrayList;

/**
 * An Axis Aligned Bounding Box (AABB) used for collision detection.
 * 
 * @author Sam Dark
 *
 */
public class AABB 
{
	/** The vertices of the AABB */
	private ArrayList<Vertex> vertices= new ArrayList<Vertex>();
	/** The values used to assemble the vertices */
	private double minX,maxX,minY,maxY,minZ,maxZ;
	/**
	 * Creates an AAB with the given input values
	 * @param iMinX Min X Coord of the AABB
	 * @param iMaxX Max X Coord of the AABB
	 * @param iMinY Min Y Coord of the AABB
	 * @param iMaxY Max Y Coord of the AABB
	 * @param iMinZ Min Z Coord of the AABB
	 * @param iMaxZ Max Z Coord of the AABB
	 */
	AABB(double iMinX,double iMaxX,double iMinY,double iMaxY,double iMinZ,double iMaxZ)
	{
		minX=iMinX;
		maxX=iMaxX;
		minY=iMinY;
		maxY=iMaxY;
		minZ=iMinZ;
		maxZ=iMaxZ;
		//Top face
		vertices.add(new Vertex(minX,maxY,minZ));
		vertices.add(new Vertex(maxX,maxY,minZ));
		vertices.add(new Vertex(minX,maxY,maxZ));
		vertices.add(new Vertex(maxX,maxY,maxZ));
		//Lower face
		vertices.add(new Vertex(minX,minY,minZ));
		vertices.add(new Vertex(maxX,minY,minZ));
		vertices.add(new Vertex(minX,minY,maxZ));
		vertices.add(new Vertex(maxX,minY,maxZ));
		
	}
	/**
	 * Gets all the vertices of the AABB
	 * @return the vertices
	 */
	public ArrayList<Vertex> getVertices()
	{
		return vertices;
	}
	/**
	 * Gets the minimum x coord of the AABB
	 * @return The min x
	 */
	public double getMinX()
	{
		return minX;
	}
	/**
	 * Gets the maximum x coord of the AABB
	 * @return The max x
	 */
	public double getMaxX()
	{
		return maxX;
	}
	/**
	 * Gets the minimum y coord of the AABB
	 * @return The min y
	 */
	public double getMinY()
	{
		return minY;
	}
	/**
	 * Gets the maximum y coord of the AABB
	 * @return The max y
	 */
	public double getMaxY()
	{
		return maxY;
	}
	/**
	 * Gets the minimum z coord of the AABB
	 * @return The min z
	 */
	public double getMinZ()
	{
		return minZ;
	}
	/**
	 * Gets the maximum z coord of the AABB
	 * @return The max z
	 */
	public double getMaxZ()
	{
		return maxZ;
	}
}
