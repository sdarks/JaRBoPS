package application;
import java.util.ArrayList;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;

import com.jogamp.opengl.util.texture.Texture;

/**
 * A custom format for objects. 
 * Contains both rendering information (faces, vertices, textures etc), meta data (name etc) and physics information (position, rotation etc).
 * @author Sam Dark
 *
 */
public class CustomObject 
{
	/** The name of the object (filename without extension) */
	private String name;//Name of the object (filename without extension)
	/** The name of the low poly object (filename without extension) */
	private String LPName;//Name of the object (filename without extension)
	
	//Rendering things
	/** The faces of the object */
	private ArrayList<TriangularFace> faces= new ArrayList<TriangularFace>();
	/** Vertices of the objects, static to reduce memory for objects with identical vertices */
	private static ArrayList<ArrayList<Vertex>> vertices = new ArrayList<ArrayList<Vertex>>();
	/** The position of the objects vertices in the vertex list */
	private int vertexNo=0;
	/** The name of the objects which have each position in the vertices list */
	private static ArrayList<String> vertexNames = new ArrayList<String>();
	///** The vertices of the object */
	//private ArrayList<Vertex> vertices= new ArrayList<Vertex>();
	/** The list of textures used across all objects */
	private static ArrayList<Texture> textures=new ArrayList<Texture>();
	/** The names of the textures used across all objects, in the same order as the textures list */
	private static ArrayList<String> textureNames= new ArrayList<String>();
	/** The position of this objects texture in the texture list */
	private int textureNo=0;//Texture that belongs to this object instant in textures list
	/** The material of this object */
	private Material material= null;//Material of the object
	/** Does the object have a texture */
	private boolean hasTexture=false;//Does the object have a texture
	
	//Low Poly Physics things
	/** The faces of the object */
	private ArrayList<TriangularFace> lpFaces= new ArrayList<TriangularFace>();
	/** The vertices of the object */
	private ArrayList<Vertex> lpVertices= new ArrayList<Vertex>();
	/** List of the actual LP Vertices (not relative to position/rotation) */
	private ArrayList<Vertex> actualLPVertices= new ArrayList<Vertex>();
	/** Has the actual vertex position of the low poly vertices been updated since the last movement */
	private boolean actualLPVertsUpdated=false;
	
	//Physics things
	/** The position of the object in 3d space */
	private Position position=new Position();//Position of the object in 3d space
	/** The rotation of the object about x,y,z axis */
	private Rotation rotation=new Rotation();//Rotation of the object about x,y,z axis 
	/** The position of the object after the last physics update */
	private Position prevPosition=new Position();//Position last update
	/** The rotation of the object after the last physics update */
	private Rotation prevRotation=new Rotation();//Rotation last update
	/** The matrix representing translation (position) of the object from the origin (4x4 matrix) */
	private DenseMatrix64F translationMatrix;
	/** The matrix representing rotation of the object around x,y,z axis (4x4 matrix) */
	private DenseMatrix64F rotationMatrix=new DenseMatrix64F();
	/** The translation matrix from the last update */
	private DenseMatrix64F prevTranslationMatrix;//From last update
	/** The rotation matrix from the last update */
	private DenseMatrix64F prevRotationMatrix=new DenseMatrix64F();//From last update
	/** The acceleration of the object in x,y,z per second (not per update) */
	private Position acc=new Position(0,0,0);//Acceleration of the object per second not per update
	/** The velocity of the object in x,y,z per second (not per update) */
	private Position vel=new Position(0,0,0);//Velocity of the object per second not per update
	/** The velocity of the object last update */
	private Position prevVel=new Position(0,0,0);//Velocity last update
	/** Can the object move? */
	private boolean canMove=true;//Can the object move?
	/** The inverse mass (1/mass) of the object */
	private double inverseMass=900;//Inverse mass (1/mass)
	/** The list of actual vertex positions of the object from last update (not relative to object position/rotation) */
	private ArrayList<Vertex> oldActualVertices= new ArrayList<Vertex>();//Actual vertices from last update
	/** The list of actual vertex positions of the low poly part of the object from last update (not relative to object position/rotation) */
	private ArrayList<Vertex> oldActualLPVertices= new ArrayList<Vertex>();//Actual lp vertices from last update
	/** Is the object effected by gravity */
	private boolean gravity=true;//Is the object effected by gravity?
	/** The center of mass of the object assuming each vertex has mass 1 and only vertices have mass*/
	private Position centerOfMass;//Center of mass of the object
	/** The center of mass of the object relative to its position */
	private Position relativeCenterOfMass;//Center of mass relative to object position
	/** The location of the last collision */
	private Position lastCol=null;//Last collision location
	/**  Has the actual vertex position of the vertices been updated since the last movement */
	private boolean actualVertsUpdated=false;
	/** List of the actual vertices (not relative to position/rotation) */
	private ArrayList<Vertex> actualVertices= new ArrayList<Vertex>();
	
	//Used in get actual vertex, here for GC reasons
	/** Used in "getActualVertex()" just here for garbage collection reduction */
	private DenseMatrix64F vertexMatrix=new DenseMatrix64F(4,1,false, 0,0,0,0);
	/** Used in "getActualVertex()" just here for garbage collection reduction */
	private DenseMatrix64F tempMatrix=new DenseMatrix64F(4,1,false,0,0,0,0);
	/** Used in "getActualVertex()" just here for garbage collection reduction */
	private Vertex vertex= new Vertex();
	/** Used in "getActualVertex()" just here for garbage collection reduction */
	private Vertex outputVertex= new Vertex();
	/** Here for gc reduction, used in getting actual vertices for efficiency */
	private ArrayList<Vertex> tempVertexList= new ArrayList<Vertex>();
	/**
	 * Creates an object with variables initialised to null
	 */
	CustomObject()
	{
		vertices=null;
		faces=null;
		relativeCenterOfMass=null;
		rotationMatrix=CommonOps.identity(4);
	}
	/**
	 * Creates an object with the input face and vertices 
	 * @param inpVertices The vertices of the object
	 * @param inpFaces The faces of the object
	 * @param name The name of the object
	 */
	CustomObject(ArrayList<Vertex> inpVertices, ArrayList<TriangularFace> inpFaces, String name)
	{
		int temp=isInVertexNames(name);
		if(temp!=-1)
		{
			vertexNo=temp;
		}
		else
		{
			vertices.add(inpVertices);
			vertexNames.add(name);
			vertexNo=vertices.size()-1;
		}
		faces=inpFaces;
		rotationMatrix=CommonOps.identity(4);
		relativeCenterOfMass=relativeCenterOfMass();
	}
	/**
	 * Creates a custom object as a copy of an existing one
	 * @param inpObj The object to copy
	 */
	CustomObject(CustomObject inpObj)
	{
		faces=new ArrayList<TriangularFace>(inpObj.faces);
		vertexNo=inpObj.vertexNo;
		textureNo=inpObj.textureNo;
		material=inpObj.material;
		hasTexture=inpObj.hasTexture;
		position=new Position(inpObj.position.getX(),inpObj.position.getY(),inpObj.position.getZ());
		rotation=new Rotation(inpObj.rotation.getX(),inpObj.rotation.getY(),inpObj.rotation.getZ());
		translationMatrix=inpObj.translationMatrix;
		rotationMatrix=inpObj.rotationMatrix;
		canMove=inpObj.canMove;
		inverseMass=inpObj.inverseMass;
		oldActualVertices=inpObj.oldActualVertices;
		name=inpObj.name;
		gravity=inpObj.gravity;
		relativeCenterOfMass=relativeCenterOfMass();
	}
	/**
	 * Sets the position of the last collision
	 * @param inpCol The position to set to
	 */
	public void setLastCol(Position inpCol)
	{
		lastCol=inpCol;
	}
	/**
	 * Gets the position of the last collision
	 * @return The position
	 */
	public Position getLastCol()
	{
		return lastCol;
	}
	/**
	 * Sets if the object is effected by gravity
	 * @param inpGrav Object effected by gravity? True or false
	 */
	public void setGravity(boolean inpGrav)
	{
		gravity=inpGrav;
	}
	/**
	 * Gets if the object is effected by gravity
	 * @return Object effected by gravity? True or false
	 */
	public boolean getGravity()
	{
		return gravity;
	}
	/**
	 * Sets the name of the object, this is used in the config creator
	 * @param inpName The name to give the object
	 */
	public void setName(String inpName)
	{
		name=inpName;
	}
	/**
	 * Sets the name of the low poly part of the object, this is used in the config creator
	 * @param inpName The name to give the object
	 */
	public void setLPName(String inpName)
	{
		LPName=inpName;
	}
	/**
	 * Gets the name of the object for use in the config creator
	 * @return The name of the object
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * Gets the name of the low poly part of the object for use in the config creator
	 * @return The name of the object
	 */
	public String getLPName()
	{
		return LPName;
	}
	/**
	 * Sets the inverse mass of the object. Smaller number is more mass 
	 * @param inpIMass The inverse mass to set to
	 */
	public void setInverseMass(double inpIMass)
	{
		if(inpIMass!=0)
		{
			inverseMass=inpIMass;
		}
	}
	/**
	 * Gets the inverse mass (1/mass) of the object
	 * @return The inverse mass
	 */
	public double getInverseMass()
	{
		return inverseMass;
	}
	/**
	 * Sets the objects material to the one input
	 * @param inpMaterial The material to set to
	 */
	public void setMaterial(Material inpMaterial)
	{
		material=inpMaterial;
	}
	/**
	 * Sets the objects position to the one input
	 * @param inpPosition The position to set to
	 */
	public void setPosition(Position inpPosition)
	{
		prevPosition=position;
		position=inpPosition;
		updateMatrices();
	}
	/**
	 * Gets the material associated with the object
	 * @return The objects material
	 */
	public Material getMaterial()
	{
		return material;
	}
	/**
	 * Gets the position of the object
	 * @return The objects position
	 */
	public Position getPosition()
	{
		return position;
	}
	/**
	 * Sets the faces of the object
	 * @param inpFaces The faces to set to
	 */
	public void setFaces(ArrayList<TriangularFace> inpFaces)
	{
		faces=inpFaces;
	}
	/**
	 * Sets the low poly faces of the objects
	 * @param inpFaces The faces to set to
	 */
	public void setLPFaces(ArrayList<TriangularFace> inpFaces)
	{
		lpFaces=inpFaces;
	}
	/**
	 * Gets the faces of the object
	 * @return The objects faces
	 */
	public ArrayList<TriangularFace> getFaces()
	{
		return faces;
	}
	/**
	 * Gets the vertices of the object
	 * @return The objects vertices
	 */
	public ArrayList<Vertex> getVertices()
	{
		return vertices.get(vertexNo);
	}
	/**
	 * Gets the low poly faces of the object
	 * @return The objects low poly faces
	 */
	public ArrayList<TriangularFace> getLPFaces()
	{
		return lpFaces;
	}
	/**
	 * Gets the low poly vertices of the object
	 * @return The objects low poly vertices
	 */
	public ArrayList<Vertex> getLPVertices()
	{
		return lpVertices;
	}
	/**
	 * Sets the objects texture to the one input
	 * @param inpTexture The texture to set to
	 */
	public void setTexture(Texture inpTexture)
	{
		//Is the texture file name for this object the same as an existing one
		int result =isInTextureNames(material.getMapKd());
		if(result==-1)//If it is not add the texture to the list
		{
			textures.add(inpTexture);
			textureNames.add(material.getMapKd());
			textureNo=textures.size()-1;
		}
		else//Otherwise use the existing texture
		{
			textureNo=result;
		}
	}
	/**
	 * Gets the texture of the object
	 * @return The objects texture
	 */
	public Texture getTexture()
	{
		return textures.get(textureNo);
	}
	/**
	 * Sets the value that says if the object has a texture.
	 * @param inpHasTexture The value to set to, true for has a texture, false for no texture
	 */
	public void setHasTexture(boolean inpHasTexture)
	{
		hasTexture=inpHasTexture;
	}
	/**
	 * Gets if the object has a texture or not
	 * @return Does the object have a texture, true for yes, false for no
	 */
	public boolean hasTexture()
	{
		return hasTexture;
	}
	/**
	 * Sets the objects rotation to the one input
	 * @param inpRotation The rotation to set to
	 */
	public void setRotation(Rotation inpRotation)
	{
		rotation=inpRotation;
		updateMatrices();
	}
	/**
	 * Gets the rotation of the object
	 * @return The objects rotation
	 */
	public Rotation getRotation()
	{
		return rotation;
	}
	/**
	 * Clears the static texture list and texture names list. Call only on the closing of a simulation.
	 */
	public static void clearTextures()
	{
		textures=null;
		textureNames=null;
	}
	/**
	 * Checks if an object name is in the list of objects with vertices stored
	 * @param inpName The name to check
	 * @return The index of the name in the array, or -1 if not present
	 */
	private static int isInVertexNames(String inpName)
	{
		if(vertexNames==null)
		{
			vertexNames=new ArrayList<String>();
		}
		if(vertices==null)
		{
			vertices=new ArrayList<ArrayList<Vertex>>();
		}
		System.out.println(vertexNames.size());
		System.out.println(vertices.size());
		for(int i=0; i<vertexNames.size();i++)//Check all the texture names
		{
			System.out.println(vertexNames.get(i));
			if(inpName.matches(vertexNames.get(i)))//If it's there return the index
			{
				return i;
			}
		}
		return -1;//Otherwise texture isn't there so return -1
	}
	
	/**
	 * Checks if a filename is in the list of existing texture filenames
	 * @param inpName The name to check
	 * @return The index of the filename in the array, or -1 if not present
	 */
	private static int isInTextureNames(String inpName)
	{
		if(textureNames==null)
		{
			textureNames=new ArrayList<String>();
		}
		if(textures==null)
		{
			textures=new ArrayList<Texture>();
		}
		for(int i=0; i<textureNames.size();i++)//Check all the texture names
		{
			if(inpName.matches(textureNames.get(i)))//If it's there return the index
			{
				return i;
			}
		}
		return -1;//Otherwise texture isn't there so return -1
	}
	/**
	 * Sets the vertices of the object to the input ones
	 * @param inpVertices The vertices to set to
	 */
	public void setVertices(ArrayList<Vertex> inpVertices) 
	{
		vertices.add(inpVertices);
		vertexNames.add(name);
		vertexNo=vertices.size()-1;
	}
	/**
	 * Sets the vertices of the low poly part of the object to the input ones
	 * @param inpVertices The vertices to set to
	 */
	public void setLPVertices(ArrayList<Vertex> inpVertices) 
	{
		lpVertices=inpVertices;
	}
	/**
	 * Gets the list of vertices of the object at their actual position, not relative to the position/rotation of the object
	 * @return The list of vertices
	 */
	public ArrayList<Vertex> getActualVertices()
	{
		if(position==prevPosition&&rotation==prevRotation&&oldActualVertices.size()>0)
		{
			return oldActualVertices;
		}
		else if(actualVertsUpdated==true)
		{
			return actualVertices;
		}
		else
		{
			updateActualVertices();
			return actualVertices;
		}

	}
	/**
	 * Gets the list of low poly vertices of the object at their actual position, not relative to the position/rotation of the object
	 * @return The list of vertices
	 */
	public ArrayList<Vertex> getActualLPVertices()
	{

		if(position==prevPosition&&rotation==prevRotation&&oldActualLPVertices.size()>0)
		{
			return oldActualLPVertices;
		}
		else if(actualLPVertsUpdated==true)
		{
			return actualLPVertices;
		}
		else
		{
			updateActualLPVertices();
			return actualLPVertices;
		}
	}
	/**
	 * Updates the list of actual low poly vertices or creates a new one is the current one is empty
	 */
	private void updateActualLPVertices()
	{
		if(actualLPVertices.size()==0)
		{
			for(int i=0;i<lpVertices.size();i++)
			{
				actualLPVertices.add(getNewActualLPVertex(i));
			}
		}
		else
		{
			for(int i=0;i<lpVertices.size();i++)
			{
				updateActualLPVertex(i);
			}
		}
		actualLPVertsUpdated=true;
	}
	/**
	 * Updates the list of actual vertices or creates a new one if the current one is empty
	 */
	private void updateActualVertices()
	{
		if(actualVertices.size()==0)
		{
			for(int i=0;i<vertices.get(vertexNo).size();i++)
			{
				actualVertices.add(getNewActualVertex(i));
			}
		}
		else
		{
			for(int i=0;i<vertices.get(vertexNo).size();i++)
			{
				updateActualVertex(i);
			}
		}
		actualLPVertsUpdated=true;
	}
	/**
	 * Gets the actual position (not relative to the position/rotation of the object) of a vertex
	 * @param vertexNumber The vertex to get the position of
	 * @return The vertex with actual position
	 */
	public Vertex getActualVertex(int vertexNumber)
	{
		if(position==prevPosition&&rotation==prevRotation&&oldActualVertices.size()>0)
		{
			return oldActualVertices.get(vertexNumber);
		}
		else if(actualVertsUpdated==true)
		{
			return actualVertices.get(vertexNumber);
		}
		else
		{
			updateActualVertices();
			return actualVertices.get(vertexNumber);
		}
	}
	/**
	 * Gets the actual position (not relative to the position/rotation of the object) of a low poly vertex
	 * @param vertexNumber The vertex to get the position of
	 * @return The vertex with actual position
	 */
	public Vertex getActualLPVertex(int vertexNumber)
	{
		if(position==prevPosition&&rotation==prevRotation&&oldActualLPVertices.size()>0)
		{
			return oldActualLPVertices.get(vertexNumber);
		}
		else if(actualLPVertsUpdated==true)
		{
			return actualLPVertices.get(vertexNumber);
		}
		else
		{
			updateActualLPVertices();
			return actualLPVertices.get(vertexNumber);
		}
	}
	/**
	 * Updates the actual position (not relative to the position/rotation of the object) of a low poly vertex
	 * @param vertexNumber The vertex to update the position of
	 */
	private void updateActualLPVertex(int vertexNumber)
	{
		
		//Get the relative position of the vertex
		vertex.copyOf(lpVertices.get(vertexNumber));
		vertexMatrix.set(0,vertex.getX());
		vertexMatrix.set(1,vertex.getY());
		vertexMatrix.set(2,vertex.getZ());
		vertexMatrix.set(3,1);
		//Multiply it by the two transform matrices
		CommonOps.mult(rotationMatrix, vertexMatrix, tempMatrix);
		CommonOps.mult(translationMatrix, tempMatrix, vertexMatrix);
		
		actualLPVertices.get(vertexNumber).setPosition(vertexMatrix.get(0),vertexMatrix.get(1),vertexMatrix.get(2));
	}
	/**
	 * Updates the actual position (not relative to the position/rotation of the object) of a vertex
	 * @param vertexNumber The vertex to update the position of
	 */
	private void updateActualVertex(int vertexNumber)
	{
		
		//Get the relative position of the vertex
		vertex.copyOf(vertices.get(vertexNo).get(vertexNumber));
		vertexMatrix.set(0,vertex.getX());
		vertexMatrix.set(1,vertex.getY());
		vertexMatrix.set(2,vertex.getZ());
		vertexMatrix.set(3,1);
		//Multiply it by the two transform matrices
		CommonOps.mult(rotationMatrix, vertexMatrix, tempMatrix);
		CommonOps.mult(translationMatrix, tempMatrix, vertexMatrix);
		
		actualLPVertices.get(vertexNumber).setPosition(vertexMatrix.get(0),vertexMatrix.get(1),vertexMatrix.get(2));
	}
	/**
	 * Gets the actual position (not relative to the position/rotation of the object) of a low poly vertex.
	 * Returns a vertex object instead of just updating an existing one
	 * @param vertexNumber The vertex to get the position of
	 * @return The vertex with actual position
	 */
	private Vertex getNewActualLPVertex(int vertexNumber)
	{
		if(position==prevPosition&&rotation==prevRotation&&oldActualLPVertices.size()>0)
		{
			return oldActualLPVertices.get(vertexNumber);
		}
		//Get the relative position of the vertex
		vertex.copyOf(lpVertices.get(vertexNumber));
		vertexMatrix.set(0,vertex.getX());
		vertexMatrix.set(1,vertex.getY());
		vertexMatrix.set(2,vertex.getZ());
		vertexMatrix.set(3,1);
		//Multiply it by the two transform matrices
		CommonOps.mult(rotationMatrix, vertexMatrix, tempMatrix);
		CommonOps.mult(translationMatrix, tempMatrix, vertexMatrix);
		//Get the actual position of the matrix
		outputVertex=new Vertex(vertexMatrix.get(0),vertexMatrix.get(1),vertexMatrix.get(2));
		//Output it
		return outputVertex;
	}
	/**
	 * Gets the actual position (not relative to the position/rotation of the object) of a vertex.
	 * Returns a vertex object instead of just updating an existing one
	 * @param vertexNumber The vertex to get the position of
	 * @return The vertex with actual position
	 */
	private Vertex getNewActualVertex(int vertexNumber)
	{
		if(position==prevPosition&&rotation==prevRotation&&oldActualLPVertices.size()>0)
		{
			return oldActualVertices.get(vertexNumber);
		}
		//Get the relative position of the vertex
		vertex.copyOf(vertices.get(vertexNo).get(vertexNumber));
		vertexMatrix.set(0,vertex.getX());
		vertexMatrix.set(1,vertex.getY());
		vertexMatrix.set(2,vertex.getZ());
		vertexMatrix.set(3,1);
		//Multiply it by the two transform matrices
		CommonOps.mult(rotationMatrix, vertexMatrix, tempMatrix);
		CommonOps.mult(translationMatrix, tempMatrix, vertexMatrix);
		//Get the actual position of the matrix
		outputVertex=new Vertex(vertexMatrix.get(0),vertexMatrix.get(1),vertexMatrix.get(2));
		//Output it
		return outputVertex;
	}
	/**
	 * Gets the actual position of a vertex (not relative) from the <strong>previous update</strong>
	 * @param vertexNumber The vertex to get position of
	 * @return The vertex with actual position
	 */
	public Vertex getPrevActualVertex(int vertexNumber)
	{

		vertex.copyOf(vertices.get(vertexNo).get(vertexNumber));
		vertexMatrix.set(0,vertex.getX());
		vertexMatrix.set(1,vertex.getY());
		vertexMatrix.set(2,vertex.getZ());
		vertexMatrix.set(3,1);
		//vertexMatrix=transformMatrix.mult(vertexMatrix);
		CommonOps.mult(prevRotationMatrix, vertexMatrix, tempMatrix);
		CommonOps.mult(prevTranslationMatrix, tempMatrix, vertexMatrix);
		//outputVertex.setX(vertexMatrix.get(0));
		//outputVertex.setY(vertexMatrix.get(1));
		//outputVertex.setZ(vertexMatrix.get(2));
		
		outputVertex=new Vertex(vertexMatrix.get(0),vertexMatrix.get(1),vertexMatrix.get(2));
		return outputVertex;
	}
	/**
	 * Gets the actual position of a low poly vertex (not relative) from the <strong>previous update</strong>
	 * @param vertexNumber The vertex to get position of
	 * @return The vertex with actual position
	 */
	public Vertex getPrevActualLPVertex(int vertexNumber)
	{

		vertex.copyOf(lpVertices.get(vertexNumber));
		vertexMatrix.set(0,vertex.getX());
		vertexMatrix.set(1,vertex.getY());
		vertexMatrix.set(2,vertex.getZ());
		vertexMatrix.set(3,1);
		//vertexMatrix=transformMatrix.mult(vertexMatrix);
		CommonOps.mult(prevRotationMatrix, vertexMatrix, tempMatrix);
		CommonOps.mult(prevTranslationMatrix, tempMatrix, vertexMatrix);
		//outputVertex.setX(vertexMatrix.get(0));
		//outputVertex.setY(vertexMatrix.get(1));
		//outputVertex.setZ(vertexMatrix.get(2));
		
		outputVertex=new Vertex(vertexMatrix.get(0),vertexMatrix.get(1),vertexMatrix.get(2));
		return outputVertex;
	}
	/**
	 * Rotates the object about a point given an x axis rotation, y axis rotation, z axis rotation 
	 * and the position of the point relative to the object.
	 * 
	 * Unsure if working or not, experimental.
	 * 
	 * @param relativePoint The point to rotate around relative to the object
	 * @param xRot The x axis rotation
	 * @param yRot The y axis rotation
	 * @param zRot The z axis rotation
	 */
	public void rotateAboutPoint(Position relativePoint, double xRot, double yRot, double zRot)
	{
		//Get the rotation matrix
		double xTheta=xRot;
		double[] xRotation= {1,0,0,0,
							0,Math.cos(Math.toRadians(xTheta)),-Math.sin(Math.toRadians(xTheta)),0,
							0,Math.sin(Math.toRadians(xTheta)),Math.cos(Math.toRadians(xTheta)),0,
							0,0,0,1};
		
		DenseMatrix64F xRotationMatrix = new DenseMatrix64F(4,4,true, xRotation);
		
		double yTheta=yRot;
		double[] yRotation= {Math.cos(Math.toRadians(yTheta)),0,Math.sin(Math.toRadians(yTheta)),0,
							0,1,0,0,
							-Math.sin(Math.toRadians(yTheta)),0,Math.cos(Math.toRadians(yTheta)),0,
							0,0,0,1};
		DenseMatrix64F yRotationMatrix = new DenseMatrix64F(4,4,true, yRotation);
		
		double zTheta=zRot;
		double[] zRotation= {Math.cos(Math.toRadians(zTheta)),-Math.sin(Math.toRadians(zTheta)),0,0,
							Math.sin(Math.toRadians(zTheta)),Math.cos(Math.toRadians(zTheta)),0,0,
							0,0,1,0,
							0,0,0,1};
		DenseMatrix64F zRotationMatrix = new DenseMatrix64F(4,4,true, zRotation);
		
		
		//Translate -point
		double[] translation= {-relativePoint.getX(),-relativePoint.getY(),-relativePoint.getZ(),0};
		DenseMatrix64F translationMatrix1=new DenseMatrix64F(4,1,false,translation);
		DenseMatrix64F identityMat=CommonOps.identity(4);
		CommonOps.insert(translationMatrix1,identityMat,0,3);
		translationMatrix1=identityMat;
		
		//Rotate
		DenseMatrix64F rotationMatrix1 = CommonOps.identity(4);
		CommonOps.mult(yRotationMatrix, zRotationMatrix, rotationMatrix1);
		DenseMatrix64F tempMat=CommonOps.identity(4);
		CommonOps.mult(xRotationMatrix, rotationMatrix1, tempMat);
		rotationMatrix1=tempMat;
		

		//Translate +point (undo previous translate)
		double[] translation2= {relativePoint.getX(),relativePoint.getY(),relativePoint.getZ(),0};
		DenseMatrix64F translationMatrix2=new DenseMatrix64F(4,1,false,translation2);
		DenseMatrix64F identityMat2=CommonOps.identity(4);
		CommonOps.insert(translationMatrix2,identityMat2,0,3);
		translationMatrix2=identityMat2;
		
		//Combine to a transform matrix
		DenseMatrix64F transformMatrix= CommonOps.identity(4);
		DenseMatrix64F tempMatrix1= CommonOps.identity(4);
		CommonOps.mult(translationMatrix1, rotationMatrix1, tempMatrix1);
		CommonOps.mult(tempMatrix1,translationMatrix2,transformMatrix);
		
		//Get the translation out of the matrix by seeing where point 0,0,0 ends up
		Position testPos= new Position(0,0,0);
		DenseMatrix64F testMatrix=new DenseMatrix64F(4,1,false, 0,0,0,0);
		DenseMatrix64F tempMatrix2=new DenseMatrix64F(4,1,false, 0,0,0,0);
		testMatrix.set(0, testPos.getX());
		testMatrix.set(1, testPos.getY());
		testMatrix.set(2, testPos.getZ());
		testMatrix.set(3, 1);
		
		CommonOps.mult(transformMatrix, testMatrix, tempMatrix2);
		
		//The translation of the object after the rotation
		Position translationOut=new Position(tempMatrix2.get(0),tempMatrix2.get(1),tempMatrix2.get(2));

		
		//Get the rotation of the object after it is rotated about the point
		double xRotOut=0;
		double yRotOut=0;
		double zRotOut=0;
		double yRadians=Math.asin(transformMatrix.get(2));
		yRotOut=Math.toDegrees(yRadians);
		
		double COverCosYRot = transformMatrix.get(6)/Math.cos(yRadians);
		double xRadians=Math.asin(COverCosYRot);
		xRotOut=Math.toDegrees(xRadians);
		
		double AOverCosYRot = transformMatrix.get(0)/Math.cos(yRadians);
		double zRadians=Math.acos(AOverCosYRot);
		zRotOut=Math.toDegrees(zRadians);
		
		//The rotation of the object
		Position rotationOut=new Position(xRotOut,yRotOut,zRotOut);
		
		//Apply the rotation and translation to the object
		position.setX(position.getX()+translationOut.getX()-relativePoint.getX());
		position.setY(position.getY()+translationOut.getY()-relativePoint.getY());
		position.setZ(position.getZ()+translationOut.getZ()-relativePoint.getZ());
		rotation.setX(rotation.getX()+rotationOut.getX());
		rotation.setY(rotation.getY()+rotationOut.getY());
		rotation.setZ(rotation.getZ()+rotationOut.getZ());
		//Update the transformation matrices as it has been moved
		updateMatrices();
		
	}
	/**
	 * Updates the transformation matrices. Called after a position or rotation change. 
	 * Also updates center of mass
	 */
	private void updateMatrices()
	{
		actualLPVertsUpdated=false;
		actualVertsUpdated=false;
		//Store the current matrices as they're about to be changed
		prevTranslationMatrix=translationMatrix;
		prevRotationMatrix=rotationMatrix;
		
		//Get the rotation matrices
		double xTheta=rotation.getX();
		double[] xRotation= {1,0,0,0,
							0,Math.cos(Math.toRadians(xTheta)),-Math.sin(Math.toRadians(xTheta)),0,
							0,Math.sin(Math.toRadians(xTheta)),Math.cos(Math.toRadians(xTheta)),0,
							0,0,0,1};
		
		DenseMatrix64F xRotationMatrix = new DenseMatrix64F(4,4,true, xRotation);
		
		double yTheta=rotation.getY();
		double[] yRotation= {Math.cos(Math.toRadians(yTheta)),0,Math.sin(Math.toRadians(yTheta)),0,
							0,1,0,0,
							-Math.sin(Math.toRadians(yTheta)),0,Math.cos(Math.toRadians(yTheta)),0,
							0,0,0,1};
		DenseMatrix64F yRotationMatrix = new DenseMatrix64F(4,4,true, yRotation);
		
		double zTheta=rotation.getZ();
		double[] zRotation= {Math.cos(Math.toRadians(zTheta)),-Math.sin(Math.toRadians(zTheta)),0,0,
							Math.sin(Math.toRadians(zTheta)),Math.cos(Math.toRadians(zTheta)),0,0,
							0,0,1,0,
							0,0,0,1};
		DenseMatrix64F zRotationMatrix = new DenseMatrix64F(4,4,true, zRotation);
		
		//Get the translation matrix
		double[] translation= {position.getX(),position.getY(),position.getZ(),0};
		translationMatrix=new DenseMatrix64F(4,1,false,translation);
		DenseMatrix64F identityMat=CommonOps.identity(4);
		CommonOps.insert(translationMatrix,identityMat,0,3);
		translationMatrix=identityMat;
		//Combine the rotation matrices
		CommonOps.mult(yRotationMatrix, zRotationMatrix, rotationMatrix);
		DenseMatrix64F tempMat=CommonOps.identity(4);
		CommonOps.mult(xRotationMatrix, rotationMatrix, tempMat);
		rotationMatrix=tempMat;
		
		//Also update center of mass
		DenseMatrix64F cOfMassMatrix=new DenseMatrix64F(4,1,false, 0,0,0,0);
		cOfMassMatrix.set(0,relativeCenterOfMass.getX());
		cOfMassMatrix.set(1,relativeCenterOfMass.getY());
		cOfMassMatrix.set(2,relativeCenterOfMass.getZ());
		cOfMassMatrix.set(3,1);
		CommonOps.mult(rotationMatrix, cOfMassMatrix, tempMatrix);
		CommonOps.mult(translationMatrix, tempMatrix, cOfMassMatrix);
		centerOfMass=new Position(cOfMassMatrix.get(0),cOfMassMatrix.get(1),cOfMassMatrix.get(2));
	}
	/**
	 * Gets the center of mass of the object in actual space, not relative to the object.
	 * (Calculated as if each vertex has equal mass and nothing else has mass)
	 * @return The center of mass of the object
	 */
	public Position getCenterOfMass()
	{
		return centerOfMass;
	}
	/**
	 * Sets the acceleration of the object
	 * @param inpAcc The acceleration to set to
	 */
	public void setAcc(Position inpAcc)
	{
		acc=inpAcc;
	}
	/**
	 * Sets the velocity of the object
	 * @param inpVel The velocity to set to
	 */
	public void setVel(Position inpVel)
	{
			prevVel=vel;
			vel=inpVel;
	}
	/**
	 * gets the acceleration of the object
	 * @return The acceleration
	 */
	public Position getAcc()
	{
		return acc;
	}
	/**
	 * Gets the velocity of the object
	 * @return The velocity
	 */
	public Position getVel()
	{
		if(canMove)
		{
			return vel;
		}
		else
		{
			return new Position(0,0,0);
		}
	}
	/**
	 * Sets if the object can move or not. Set to false for objects that shouldn't move.
	 * This is for things like walls and floors so they don't get effected by gravity
	 * @param inpMove True or false
	 */
	public void setCanMove(boolean inpMove)
	{
		canMove=inpMove;
	}
	/**
	 * Can the object move true or false
	 * @return true or false
	 */
	public boolean canMove()
	{
		return canMove;
	}
	/**
	 * Moves the object according to its velocity
	 * @param time How much time to advance the object by
	 */
	public void move(double time)
	{
		//If the object can move then move it
		if(canMove)
		{
			prevPosition=new Position(position.getX(), position.getY(), position.getZ());
			prevRotation=new Rotation(rotation);
			
			position.setX(position.getX()+(vel.getX()*(0.001*time)));
			position.setY(position.getY()+(vel.getY()*(0.001*time)));
			position.setZ(position.getZ()+(vel.getZ()*(0.001*time)));
			
			rotation.setX(rotation.getX()+(rotation.getVel().getX()*(0.001*time)));
			rotation.setY(rotation.getY()+(rotation.getVel().getY()*(0.001*time)));
			rotation.setZ(rotation.getZ()+(rotation.getVel().getZ()*(0.001*time)));
			updateMatrices();
		}
	}	
	/**
	 * Moves the object according to its velocity. Doesn't update the previous position and rotation
	 * @param time How much time to advance the object by
	 */
	public void postCollisionMove(double time)
	{
		if(canMove)
		{
			position.setX(position.getX()+(vel.getX()*(0.001*time*0.98)));
			position.setY(position.getY()+(vel.getY()*(0.001*time*0.98)));
			position.setZ(position.getZ()+(vel.getZ()*(0.001*time*0.98)));
			//0.98 so it's not quite colliding
			rotation.setX(rotation.getX()+(rotation.getVel().getX()*(0.001*time*0.98)));
			rotation.setY(rotation.getY()+(rotation.getVel().getY()*(0.001*time*0.98)));
			rotation.setZ(rotation.getZ()+(rotation.getVel().getZ()*(0.001*time*0.98)));
			updateMatrices();
		}
	}	
	/**
	 * Accelerate the object by a certain time peroid
	 * @param time The amount of time to accelerate the object for
	 */
	public void accelerate(double time)
	{
		acceleratePosition(time);
		accelerateRotation(time);
	}
	/**
	 * Accelerate just the position of the object by a certain time
	 * @param time The time to accelerate for
	 */
	public void acceleratePosition(double time)
	{
		prevVel=vel;
		vel.setX(vel.getX()+(acc.getX()*(0.001*time)));
		if(vel.getY()>-10)
		vel.setY(vel.getY()+(acc.getY()*(0.001*time)));
		vel.setZ(vel.getZ()+(acc.getZ()*(0.001*time)));
	}
	/**
	 * Accelerate just the rotation of the object by a certain time
	 * @param time The time to accelerate for
	 */
	public void accelerateRotation(double time)
	{
		Position rVel=new Position();
		rVel.setX(rotation.getVel().getX()+(rotation.getAcc().getX()*(0.001*time)));
		rVel.setY(rotation.getVel().getY()+(rotation.getAcc().getY()*(0.001*time)));
		rVel.setZ(rotation.getVel().getZ()+(rotation.getAcc().getZ()*(0.001*time)));
		rotation.setVel(rVel);
	}
	/**
	 * Reverts the position and rotation of the object to its last update
	 */
	public void undoPosRot()
	{
		if(canMove)
		{
			position=prevPosition;
			rotation=prevRotation;
		}
	}
	/**
	 * Reverts the velocity of the object to its last update
	 */
	public void undoVel()
	{
		vel=prevVel;
	}
	/**
	 * Calculates the relative center of mass of the object assuming mass only comes from vertices
	 * and each vertex has equal mass
	 * @return The relative position of the center of mass
	 */
	private Position relativeCenterOfMass()
	{
		//Center of mass when all vertices are the only points of weight and equally weighted is the average position of vertices
		Position cOfMass= new Position();
		double xTot=0;
		double yTot=0;
		double zTot=0;
		//Get totals
		for(int i=0; i<vertices.get(vertexNo).size();i++)
		{
			xTot+=vertices.get(vertexNo).get(i).getX();
			yTot+=vertices.get(vertexNo).get(i).getY();
			zTot+=vertices.get(vertexNo).get(i).getZ();
		}
		//Average them
		xTot=xTot/vertices.get(vertexNo).size();
		yTot=yTot/vertices.get(vertexNo).size();
		zTot=zTot/vertices.get(vertexNo).size();
		//Average is center of mass
		cOfMass.setX(xTot);
		cOfMass.setY(yTot);
		cOfMass.setZ(zTot);
		return cOfMass;
	}
}
