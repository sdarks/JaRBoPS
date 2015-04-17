package application;
import java.util.ArrayList;

/**
 * Stores information about a triangular face (3 vertices)
 * @author Sdarks
 *
 */
public class TriangularFace 
{
	/** The vertices in the face */
	private ArrayList<Integer> vertices=new ArrayList<Integer>();//Currently stored as 1=first vertex in array maybe change to 0
	
	/**
	 * Creates a face with the input vertices
	 * @param inpVertices The vertices of the face
	 */
	TriangularFace(ArrayList<Integer> inpVertices)
	{
		vertices=new ArrayList<Integer>();
		for(int i=0;i<3;i++)
		{
			vertices.add(inpVertices.get(i));
		}
	}
	/**
	 * Gets the vertices in the face
	 * @return The face vertices
	 */
	public ArrayList<Integer> getVertices()
	{
		return vertices;
	}
	/**
	 * Gets a single vertex from the vertices list
	 * @param vertex The index of the vertex to get
	 * @return The index of the vertex in the objects vertex list
	 */
	public int getVertex(int vertex)
	{
		return vertices.get(vertex);
	}
}
