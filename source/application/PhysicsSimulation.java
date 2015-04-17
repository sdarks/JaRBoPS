package application;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.ejml.alg.dense.mult.VectorVectorMult;
import org.ejml.data.D1Matrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleBase;
import org.ejml.simple.SimpleMatrix;

/**
 * A class for running a physics simulation on a set of objects given certain starting parameters. 
 * Passes the resulting data to a renderer to be displayed on the screen.
 * @author Sam Dark
 *
 */
public class PhysicsSimulation extends Thread
{
	/** The renderer for displaying to the screen */
	protected Renderer render;//Renderer for displaying the physics
	/** The high poly objects given to the renderer */
	protected ArrayList<CustomObject> objects=null;//The objects displayed on the renderer
	/** The time the last physics update happened (in ms) */
	protected long lastUpdate=0;//The time the last update happened (in ms)
	/** The time since the simulations per second text was last updated (in ms) */
	protected long timeSinceLastSimDisplay=0;//The time since the simulation time text was displayed on screen (in ms)
	
	
	//Matrices used for collision detection
	/** Used for collision detection, here for garbage collection reasons */
	protected DenseMatrix64F ab;
	/** Used for collision detection, here for garbage collection reasons */
	protected DenseMatrix64F ac;
	/** Used for collision detection, here for garbage collection reasons */
	protected DenseMatrix64F n;
	/** Used for collision detection, here for garbage collection reasons */
	protected DenseMatrix64F qp;
	/** Used for collision detection, here for garbage collection reasons */
	protected DenseMatrix64F ap;
	/** Used for collision detection, here for garbage collection reasons */
	protected DenseMatrix64F e;
	
	/** The global gravity of the simulation */
	protected double gravity=-9;//Global gravity (negative is towards ground)
	/** The global elasticity of the simulation */
	protected double elas=-0.4;//Global elasticity
	/** The AABBs of the objects in the simulation */
	protected ArrayList<AABB> AABBs= new ArrayList<AABB>();//Bounding boxes for the objects
	/** How many times the collisions are resolved each update */
	protected int colResolves=1;//How many times collisions are resolved per update
	
	/** For logging sim time for benchmarking */
	protected ArrayList<Long> times= new ArrayList<Long>();
	
	/**
	 * Runs the simulation until the attached renderer is closed.
	 * Must init the physics sim before calling this.
	 */
	public void run()
	{
		while(render.getClosing()==false)
		{
				update();
		}
		try {
			saveLogToFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Failed to save log to file");
			e.printStackTrace();
		}
		System.out.println("Physics closed");
	}
	
	/**
	 * Logs the current sim time to be saved to a log file on close
	 * @param simTime The sim time to log
	 */
	private void takeSimTimeLog(long simTime)
	{
		times.add(simTime);
	}
	
	/**
	 * Saves the sim time log to file. Saves to /logs/.
	 * @throws IOException Throws exceptions if it cannot save to the file
	 */
	public void saveLogToFile() throws IOException
	{
		String filename="simTimes.csv";
		String current="";
		try {
			current = new java.io.File( "." ).getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String fullFilename=current+"/logs/"+filename;
		File logFile = new File(fullFilename);
		FileOutputStream outputStream = new FileOutputStream(logFile);
	 
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		//Number of objects
		for(int i=0;i<times.size();i++)
		{
			writer.write(String.valueOf(times.get(i))+",");
		}
		//Close the writer
		writer.close();
	}
	
	/**
	 * Initialises the physics simulation with some input initial values.
	 * @param inpRend The renderer to output to
	 * @param inpObjs The objects to simulate
	 * @param inpConf The config to run containing information about the global values and object starting values
	 */
	public void init(Renderer inpRend, ArrayList<CustomObject> inpObjs, Config inpConf)
	{
		render=inpRend;
		objects=new ArrayList<CustomObject>();
		objects=inpObjs;
		lastUpdate=System.currentTimeMillis();
		gravity=inpConf.grav;
		elas=inpConf.elas;
		initObjects(inpConf.objs);
		ab=new DenseMatrix64F(3,1,true,0,0,0);
		ac=new DenseMatrix64F(3,1,true,0,0,0);
		n=new DenseMatrix64F();
		qp= new DenseMatrix64F(3,1,true,0,0,0);
		ap= new DenseMatrix64F(3,1,true,0,0,0);
		e=new DenseMatrix64F();
	}
	/**
	 * Initialises the objects in the simulation using values passes. 
	 * Then initialises textures on the objects and gives them an update of 0 time.
	 * Also sets the current thread to max priority.
	 * @param initObjs The initial values to be assigned to the objects
	 */
	private void initObjects(ArrayList<ObjectInit> initObjs)
	{
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		for(int i=0; i<initObjs.size();i++)
		{
			//Set name
			objects.get(i).setName(initObjs.get(i).filePath);
			//Set position
			objects.get(i).setPosition(initObjs.get(i).pos);
			//Set Rotation
			objects.get(i).setRotation(initObjs.get(i).rot);
			//Set can move
			objects.get(i).setCanMove(initObjs.get(i).canMove);
			//Set inverseMass
			objects.get(i).setInverseMass(initObjs.get(i).inverseMass);
			//Set if the object is effected by gravity
			objects.get(i).setGravity(initObjs.get(i).gravity);
			//Set the initial velocity of the objects
			objects.get(i).setVel(initObjs.get(i).velocity);
			//If object is effected by gravity
			if(objects.get(i).getGravity())
			{
				//Apply gravity
				objects.get(i).setAcc(new Position(0,gravity,0));
			}
			else
			{
				//No gravity
				objects.get(i).setAcc(new Position(0,0,0));
			}
		}
		sortObjects();
		//Some extra initialisation
		objects=render.initTextures(objects);
		render.requestVBOUpdate();
		updateObjects(0);
		render.updateVerticesInScene();
		
	}
	/**
	 * Sorts the objects in the sim alphabetically based on name. 
	 */
	private void sortObjects()
	{
		for(int i=0; i<objects.size();i++)
		{
			System.out.println(objects.get(i).getName());
		}
		Collections.sort(objects, new Comparator<CustomObject>() {
		    public int compare(CustomObject obj1, CustomObject obj2) {
		        return obj1.getName().compareTo(obj2.getName());
		    }
		});
		
	}
	/**
	 * Updates the simulation based on the time since the last update. 
	 * Updates the position, velocity etc of all the objects, detects and resolves collisions and then sends the updated objects to the renderer.
	 */
	private void update()
	{
		
		long time=(System.currentTimeMillis()-lastUpdate);//The time since the last update, how long we will be simulating this update. Also known as time step
		long maxTime=16;//The maximum time an update can simulate. Prevents the engine not being able to update as fast as it needs. Will cause slow motion effect if physics takes longer than this to update.
		//Note: 16ms is approximately the time the update has to update once per frame at 60fps
		//If time is more than max time update the max time instead.
		time=Math.min(time,maxTime);
		
		//Every 500ms update the physics update time display on the screen
		timeSinceLastSimDisplay+=time;
		if(timeSinceLastSimDisplay>500)
		{
			double simTime=(double)time/1000;
			render.setStringForDisplay("Simulating:"+time+"ms   "+"Which is: "+Math.round((1/simTime)*1000.0)/1000.0+" per second");
			timeSinceLastSimDisplay=0;
			takeSimTimeLog(time);
		}
		lastUpdate=System.currentTimeMillis();//The last update is now as we are updating
		
		//Update the objects by the time step
		updateObjects(time);
		
		//Create axis aligned bounding boxes for collision detection
		AABBs=new ArrayList<AABB>();
		for(int j=0; j<objects.size();j++)
		{
			CustomObject temp=objects.get(j);
			AABB tempAABB=getAABB(temp);
			AABBs.add(tempAABB);
		}
		
		//Resolve collisions "colResolves" number of times, more resolutions can help with stacking
		int x=0;
		while(x<colResolves)
		{
			//If there was a collision keep resolving
			if(checkAABBCollisions(AABBs,time))
			{
				
				x++;
			}
			//Otherwise stop resolving collisions
			else
			{
				x=colResolves+1;
			}
		}
		//Pass the updated objects to the renderer
		render.setObjects(objects);
		
	}
	/**
	 * Checks collisions between axis aligned bounding boxes and moves on to the
	 * next stage of the collision detection if AABBs are colliding.
	 * @param inpAABBs The AABBs to check collisions between.
	 * @param time The time step for this update, used to resolve collisions later.
	 * @return True if there were any collisions False if there were no collisions.
	 */
	private boolean checkAABBCollisions(ArrayList<AABB> inpAABBs, double time)
	{
		boolean hasCollided=false;//No collisions yet
		//Check all the objects for collisions
		for(int i=0; i<inpAABBs.size();i++)
		{
			for(int j=0; j<inpAABBs.size();j++)
			{
				//Don't check for collisions with objects that have already been done or with themselves
				//Example: check object1 and object2 then don't check object2 and object1 as it has already been checked
				if(j>i)
				{
					if(objects.get(i).canMove()==false && objects.get(j).canMove()==false)
					{
						//Don't check or resolve for collisions between objects that don't move
					}
					else
					{
						//Check if the bounding boxes are colliding either way around
						if(checkAABB(inpAABBs.get(i),inpAABBs.get(j))||checkAABB(inpAABBs.get(j),inpAABBs.get(i)))
						{
							resolveCollision(inpAABBs,time,i,j);//Resolve the collision between the objects
							hasCollided=true;//There has been a collision
						}
					}
				}
			}
		}
		return hasCollided;
	}
	/**
	 * Resolves a collision between two objects that have intersecting AABBs and updates them if a full collision is present
	 * @param inpAABBs The list of AABBs, used for reducing number of points to test for collisions
	 * @param time The time step for this update, used for resolving the collisions
	 * @param i The first object to test
	 * @param j The second object to test
	 */
	private void resolveCollision(ArrayList<AABB> inpAABBs, double time, int i, int j)
	{
		//Check if an actual collision happened
		double[] results=checkCollision(objects.get(i), objects.get(j),time,inpAABBs.get(i),inpAABBs.get(j));
		//If the objects are colliding deal with the collisions
		if(results[0]!=-1)
		{
			boolean angled=false;
			//Undo the movement of both objects
			objects.get(i).undoPosRot();
			objects.get(j).undoPosRot();
			//Move them to the point of collision (results[1] is the collision time)
			objects.get(i).postCollisionMove(results[1]);
			objects.get(j).postCollisionMove(results[1]);
			//Get the impulse for each object
			Position faceNormal=faceNormal(objects.get(j),(int)results[2]);
			ArrayList<Position> newVels=getImpulses(objects.get(i).getVel(), objects.get(j).getVel(), faceNormal, objects.get(i).getInverseMass(),objects.get(j).getInverseMass());
			Position newVeli=newVels.get(0);
			Position newVelj=newVels.get(1);
			//Get the new velocities for the objects
			newVeli=new Position(objects.get(i).getVel().getX()/1.5+newVeli.getX(),objects.get(i).getVel().getY()/1.5+newVeli.getY(),objects.get(i).getVel().getZ()/1.5+newVeli.getZ());
			newVelj=new Position(objects.get(j).getVel().getX()/1.5+newVeli.getX(),objects.get(j).getVel().getY()/1.5+newVeli.getY(),objects.get(j).getVel().getZ()/1.5+newVeli.getZ());	
			//Apply the new velocities to the objects
			objects.get(i).setVel(newVeli);
			objects.get(j).setVel(newVelj);
		}
	}
	/**
	 * Gets the length of a vector
	 * @param v The vector to get the length of
	 * @return The length of the vector as a double
	 */
	private double vectorLength(Position v)
	{
		return Math.sqrt( (v.getX()*v.getX()) + (v.getY()*v.getY()) + (v.getZ()*v.getZ()) );
	}
	/**
	 * Calculates the normal of a face and returns the vector.
	 * @param obj The object the face to check is on
	 * @param faceNo The face of the object to check
	 * @return The normal of the face
	 */
	private Position faceNormal(CustomObject obj, int faceNo)
	{
		//Make sure the face given is a valid face number
		if(faceNo<obj.getLPFaces().size())
		{
			//Get the positions of the 3 vertices in the face
			Position p1= obj.getActualLPVertex(obj.getFaces().get(faceNo).getVertices().get(0)-1);
			Position p2= obj.getActualLPVertex(obj.getFaces().get(faceNo).getVertices().get(1)-1);
			Position p3= obj.getActualLPVertex(obj.getFaces().get(faceNo).getVertices().get(2)-1);
			//Make 2 vectors, point2-point1 and point3-point1
			Position u= new Position(p2.getX()-p1.getX(), p2.getY()-p1.getY(), p2.getZ()-p1.getZ());
			Position v= new Position(p3.getX()-p1.getX(), p3.getY()-p1.getY(), p3.getZ()-p1.getZ());
			//Calculate the normal from the two vectors
			Position normal=new Position();
			normal.setX((u.getY()*v.getZ())-(u.getZ()*v.getY()));
			normal.setY((u.getZ()*v.getX())-(u.getX()*v.getZ()));
			normal.setZ((u.getX()*v.getY())-(u.getY()*v.getX()));
			//Normalise the normal
			double normalLength=vectorLength(normal);
			normal.setX(normal.getX()/normalLength);
			normal.setY(normal.getY()/normalLength);
			normal.setZ(normal.getZ()/normalLength);
			return normal;
		}
		return new Position(0,0,0);//The face wasn't valid
	}
	/**
	 * Gets the impulses to be applied to the objects in the collision
	 * @param va The velocity of the first object
	 * @param vb The velocity of the second object
	 * @param n The normal of the face the collision is happening on
	 * @param ima The inverse mass of the first object
	 * @param imb The inverse mass of the second object
	 * @return The impulse vectors in an ArrayList
	 */
	private ArrayList<Position> getImpulses(Position va, Position vb, Position n, double ima, double imb)
	{
		//Formulas
		//Before resolution
		//Vr= Va-Vb
		//I= (1+e)*N*(Vr.N)/(1/ma+1/mb)
		//After resolution
		//Va -= I*1/ma
		//Vb += I* 1/mb
		//ima=1/ma
		//imb=1/mb
		
		ArrayList<Position> impulseVectors=new ArrayList<Position>();//The ArrayList returned at the end
		
		DenseMatrix64F Vr = new DenseMatrix64F(3,1,true,vb.getX()-va.getX(), vb.getY()-va.getY(), vb.getZ()-va.getZ());//The relative velocity of the objects
		DenseMatrix64F N = new DenseMatrix64F(3,1,true,n.getX(), n.getY(), n.getZ());//The face normal in matrix form
		
		double onePlusE= 1+elas;//Elasticity or coefficient of restitution
		double VrdotN=VectorVectorMult.innerProd(Vr,N);//Dot product of Vr and N (Vr.N)
		VrdotN=VrdotN/(ima+imb);//Divide by inverse mass A + inverse mass B ((Vr.N)/(1/ma+1/mb))
		DenseMatrix64F I = new DenseMatrix64F(3,1,true,1,1,1);//Array for storing impulse
		CommonOps.scale(VrdotN,N,I);//Scale the normal by the double we just calculated (N*(Vr.N)/(1/ma+1/mb))
		CommonOps.scale(onePlusE,I);//Scale by (1+e) for (I= (1+e)*N*(Vr.N)/(1/ma+1/mb))
		
		DenseMatrix64F impulseVectMatrixA = new DenseMatrix64F(3,1,true,1,1,1);//Impulse vector for A
		CommonOps.scale(ima, I, impulseVectMatrixA);//Times impulse by inverse mass to get the impulse for this object
		
		//Add to the impulse vectors list
		Position impulseVector=new Position();
		impulseVector.setX(impulseVectMatrixA.get(0));
		impulseVector.setY(impulseVectMatrixA.get(1));
		impulseVector.setZ(impulseVectMatrixA.get(2));
		impulseVectors.add(impulseVector);
		
		//Change the sign for second object as they should be applied impulses in opposite directions
		DenseMatrix64F impulseVectMatrixB = new DenseMatrix64F(3,1,true,1,1,1);
		CommonOps.scale(imb, I, impulseVectMatrixB);//Multiply impulse by object inverse mass
		CommonOps.changeSign(impulseVectMatrixB);//Change the sign for impulse in the other direction
		//Add second impulse to list
		Position impulseVectorB=new Position();
		impulseVectorB.setX(impulseVectMatrixB.get(0));
		impulseVectorB.setY(impulseVectMatrixB.get(1));
		impulseVectorB.setZ(impulseVectMatrixB.get(2));
		impulseVectors.add(impulseVectorB);
		//Return the list
		return impulseVectors;
	}
	/**
	 * Checks if any vertices in the first AABB specified are within the second AABB
	 * @param box1 The first AABB
	 * @param box2 The second AABB
	 * @return True if any vertices from box1 are inside box2, False otherwise
	 */
	private boolean checkAABB(AABB box1, AABB box2)
	{
		//For every vertex in the first box
		for(int i=0;i<box1.getVertices().size();i++)
		{
			Vertex tempVert=box1.getVertices().get(i);
			if(tempVert.getX()>=box2.getMinX()&&tempVert.getX()<=box2.getMaxX())//The vertex is within the X boundary of the box
			{
				if(tempVert.getY()>=box2.getMinY()&&tempVert.getY()<=box2.getMaxY())//Y
				{
					if(tempVert.getZ()>=box2.getMinZ()&&tempVert.getZ()<=box2.getMaxZ())//Z
					{
						//The vertex is within x,y and z boundaries of box so is in the box
						return true;
					}
				}
			}
		}
		//None of the vertices are in the box
		return false;
	}
	/**
	 * Updates the position, velocity, acceleration etc of all the objects by the given time step
	 * @param time The time step to update the objects by
	 */
	protected void updateObjects(double time)
	{
		//For all objects
		for(int i=0; i<objects.size();i++)
		{
			//Accelerate the object
			objects.get(i).accelerate(time);
			//Move the object
			objects.get(i).move(time);
		}
	}
	/**
	 * Gets the middle point of an object using the AABB for that object
	 * @param objNo The object to get the middle point of
	 * @return The position that is the middle point of that object
	 */
	protected Position getMiddleOfObject(int objNo)
	{
		Position middleOfObject= new Position();
		middleOfObject.setX((AABBs.get(objNo).getMaxX()/2)+AABBs.get(objNo).getMinX());//Middle x
		middleOfObject.setY((AABBs.get(objNo).getMaxY()/2)+AABBs.get(objNo).getMinY());//Middle y
		middleOfObject.setZ((AABBs.get(objNo).getMaxZ()/2)+AABBs.get(objNo).getMinZ());//Middle z
		return middleOfObject;
	}
	/**
	 * Gets the axis aligned bounding box (or AABB) of an object
	 * @param inpObj The object to get the AABB of
	 * @return The AABB of the input object
	 */
	protected AABB getAABB(CustomObject inpObj)
	{
		//Get the vertices of the object
		ArrayList<Vertex> vertices=inpObj.getActualLPVertices();
		//Get the vertices on the extremes, min/max X,Y and Z as those are the edges of the box
		Vertex firstVert=vertices.get(0);
		double maxX=firstVert.getX();
		double maxY=firstVert.getY();
		double maxZ=firstVert.getZ();
		double minX=maxX;
		double minY=maxY;
		double minZ=maxZ;
				
		for(int i=0;i<vertices.size();i++)
		{
			Vertex vert=vertices.get(i);
			double vertX=vert.getX();
			double vertY=vert.getY();
			double vertZ=vert.getZ();
			if(vertX<minX)
			{
				minX=vertX;
			}
			if(vertX>maxX)
			{
				maxX=vertX;
			}
			if(vertY<minY)
			{
				minY=vertY;
			}
			if(vertY>maxY)
			{
				maxY=vertY;
			}
			if(vertZ<minZ)
			{
				minZ=vertZ;
			}
			if(vertZ>maxZ)
			{
				maxZ=vertZ;
			}
			
		}
		//return the AABB
		return new AABB(minX,maxX,minY,maxY,minZ,maxZ);
	}
	/**
	 * Checks for a collision between two objects and returns the results of the check.
	 * @param obj1 The first object to check
	 * @param obj2 The second object to check with
	 * @param time The time step of this update (used to calculate the collision time)
	 * @param AABB1 The AABB of the first object
	 * @param AABB2 The AABB of the second object
	 * @return The results of the check. results[0] is -1 if the check failed to find a collision otherwise 1.  results[1] is the time the collision occurred during the update. results[2] is the face of object 2 that the object 1 intersected
	 */
	private double[] checkCollision(CustomObject obj1, CustomObject obj2, double time, AABB AABB1, AABB AABB2)
	{

		double[] result= new double[10];
		
		//Get the vertices of each object
		ArrayList<Vertex> o2Verts=obj2.getActualLPVertices();
		ArrayList<Vertex> o1Verts=obj1.getActualLPVertices();
		
		//Check which points of the first object are within the AABB of the second object and add them to a list
		ArrayList<Position> points= new ArrayList<Position>();
		for(int j=0; j<obj1.getLPVertices().size(); j++)
		{
			Position p1=obj1.getPrevActualLPVertex(j);
			Position p2=o1Verts.get(j);
			if(isPointInBoundingBox(p1,AABB2)||isPointInBoundingBox(p2,AABB2))
			{
				points.add(p1);
				points.add(p2);
			}
			
		}
		//If there are no points no collision
		if(points.size()==0)
		{
			result[0]=-1;
			return result;
		}
		
		//Check every face of object 2 for collisions with the vertices of object 1
		for(int i=0; i<obj2.getLPFaces().size(); i++)
		{
			//Get the vertices of the face we are checking
			Position v1=o2Verts.get(obj2.getLPFaces().get(i).getVertices().get(0)-1);
			Position v2=o2Verts.get(obj2.getLPFaces().get(i).getVertices().get(1)-1);
			Position v3=o2Verts.get(obj2.getLPFaces().get(i).getVertices().get(2)-1);
			
			//Don't bother checking for collisions with faces that don't have any vertices within the first objects AABB
			//As these can't possibly be the intersecting points
			if(isPointInBoundingBox(v1,AABB1)||isPointInBoundingBox(v2,AABB1)||isPointInBoundingBox(v3,AABB1))
			{
				//Check the points that are in the bounding box of the second object with the face that is in the bounding box of the first object
				result=checkFaceRayCollision(v1,v2,v3,points,time);
			}
			else
			{
				result[0]=-1;
			}
			//If we got a collision result stop searching and return the result
			if(result[0]!=-1)
			{
				result[2]=i;//Add the face on the second object that the first object collided with
				return result;
			}
			
		}
		//If we got no results the collision check failed and there was no collision
		double[] failed= {-1};
		return failed;
	}
	/**
	 * Gets if a point is within a bounding box
	 * @param point The point to test
	 * @param box The box to test
	 * @return True if the point is within the box, False otherwise
	 */
	private boolean isPointInBoundingBox(Position point, AABB box)
	{
		if(point.getX()>=box.getMinX()&&point.getX()<=box.getMaxX())//Check if it's within x boundary
		{
			if(point.getY()>=box.getMinY()&&point.getY()<=box.getMaxY())//Check y
			{
				if(point.getZ()>=box.getMinZ()&&point.getZ()<=box.getMaxZ())//Check z
				{
					//It is within the box
					return true;
				}
			}
		}
		//It is not within the box
		return false;
	}
	/**
	 * Checks for collisions between a list of rays and a face
	 * @param a The first vertex of the face
	 * @param b The second vertex of the face
	 * @param c The third vertex of the face
	 * @param points The points of the rays
	 * @param time The time step of the current update
	 * @return The results. results[0] is -1 if no collision or 1 if one of the rays collides. results[1] is the time of collision.
	 */
	private double[] checkFaceRayCollision(Position a, Position b, Position c, ArrayList<Position> points, double time)
	{
		//Precalculate some values used by all the tests
		//Two vectors used by all tests
		ab.set(0, b.getX()-a.getX());
		ab.set(1, b.getY()-a.getY());
		ab.set(2, b.getZ()-a.getZ());
		ac.set(0, c.getX()-a.getX());
		ac.set(1, c.getY()-a.getY());
		ac.set(2, c.getZ()-a.getZ());
		n=crossProduct(ab,ac);//Normal of the triangle used for all tests
		//Storage for results
		double[] results= new double[10];
		results[0]=-1;
		//Test all the rays
		for(int i=0; i<points.size();i++)
		{
			results=rayPlaneIntersectTest(points.get(i),points.get(i+1),a,b,c,time,n,ab,ac);//Check if the ray intersects the triangle
			//If it does don't run any more tests
			if(results[0]!=-1)
			{
				return results;
			}
			i++;//Otherwise continue testing
		}
		//The tests failed and no collisions
		double[] failed={-1};
		return failed;
	}
	/**
	 * Tests for intersection between a ray and a plane (triangle) and returns the time of collision.
	 * 
	 * @param p The start point of the ray
	 * @param q The end point of the ray
	 * @param a The first point of the triangle
	 * @param b The second point of the triangle
	 * @param c The third point of the triangle
	 * @param time The time of the current update step
	 * @param n The normal of the triangle
	 * @param ab b-a
	 * @param ac c-a
	 * @return The results of the test. results[0] is -1 if no intersection or 1 if there is intersection. results[1] is the time of intersection.
	 */
	private double[] rayPlaneIntersectTest(Position p, Position q, Position a, Position b, Position c, double time, DenseMatrix64F n,DenseMatrix64F ab, DenseMatrix64F ac)
	{
		//For outputting the results
		double[] output=new double[10];
		//p-q
		qp.set(0,p.getX()-q.getX());
		qp.set(1,p.getY()-q.getY());
		qp.set(2,p.getZ()-q.getZ());
		
		//Check if the ray is pointing at the triangle
		//d=0 means ray is parallel d<0 means ray facing away
		double d=VectorVectorMult.innerProd(qp, n);
		if(d<=0.0)
		{
			//Ray is not even pointing at triangle so can't be intersecting
			output[0]=-1;
			return output;
		}
		//p-a
		ap.set(0,p.getX()-a.getX());
		ap.set(1,p.getY()-a.getY());
		ap.set(2,p.getZ()-a.getZ());
		//t is dot product of ap and n
		double t=VectorVectorMult.innerProd(ap,n);
		//If t<0 not intersecting
		if(t<0.0)
		{
			output[0]=-1;
			return output;
		}
		//If t>d not intersecting
		if(t>d)
		{
			output[0]=-1;
			return output;
		}
		//e is croos product of qp and ap
		e=crossProduct(qp, ap);
		
		//If v>d or v<0 not intersecting
		double v = VectorVectorMult.innerProd(ac,e);//Dot product
		if(v<0.0 || v>d)
		{
			output[0]=-1;
			return output;
		}
		//If v+w>d or w<0 not intersecting
		double w= -VectorVectorMult.innerProd(ab,e);//Dot product
		if(w<0.0 || v+w>d)
		{
			output[0]=-1;
			return output;
		}
		//It intersects so output results
		double ood = 1.0/d;
		t*=ood;
		output[0]=1;//intersected
		output[1]=t*time;//time of intersection
		//Optional barycentric coordinates of the collision
		//v*=ood;
		//w*=ood;
		//double u=1.0 - v - w;
		//output[2]=u;
		//output[3]=v;
		//output[4]=w;
		return output;
	}
	/**
	 * Gets the cross product of two vectors
	 * @param a The first vector
	 * @param b The second vector
	 * @return The cross product of the two vectors
	 */
	private DenseMatrix64F crossProduct(DenseMatrix64F a, DenseMatrix64F b)
	{
		DenseMatrix64F output= new DenseMatrix64F(1,3);
		output.set(0,(a.get(1)*b.get(2))-(a.get(2)*b.get(1)));
		output.set(1,(a.get(2)*b.get(0))-(a.get(0)*b.get(2)));
		output.set(2,(a.get(0)*b.get(1))-(a.get(1)*b.get(0)));
		return output;
	}
	/**
	 * Gets the gravity value of the simulation
	 * @return The gravity value
	 */
	public double getGravity()
	{
		return gravity;
	}
	/**
	 * Gets the elasticity value of the simulation
	 * @return The elasticity value
	 */
	public double getElasticity()
	{
		return elas;
	}
}
