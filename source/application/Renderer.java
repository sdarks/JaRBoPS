package application;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;

import com.jogamp.opengl.util.texture.TextureIO;

/**
 * Custom renderer for displaying objects of the type CustomObject to the screen in 3d using OpenGL. 
 * Requires OpenGL3 support for VBOS.
 * @author Sam Dark
 *
 */
public class Renderer implements GLEventListener
{
	/** The FPS counter for displaying fps (and other strings) to the screen */
	private FPSSystem FPSCounter;//Object for handling fps tracking and displaying strings to the screen
	/** The openGL canvas used for drawing on */
	private GLCanvas canvas;//The opengl canvas for drawing on
	/** The frame the openGL canvas is in */
	private JFrame frame;//The frame the canvas is in
	/** The objects to draw to the screen */
	private ArrayList<CustomObject> objects=new ArrayList<CustomObject>();//List of objects to draw to the screen
	/** GLU instance */
	private GLU glu;
	/** Is the renderer closing? */
    private boolean closing=false;
    
	//For using VBOs
    /** The VBO for handing all the information to openGL */
	private int VBO[]=null;
	/** Floatbuffers used for VBO */
	private FloatBuffer vertices, normals, textures, colours;
	/** Current VBO Size in objects**/
	private int VBOSize=0;
	
	//For drawing to the screen
	/** The number of vertices in the scene */
	private int numVerticesInScene=0;
	/** Is vsync on? */
	private boolean vSync=true;//VSync on?
	/** Is wireframe mode on? */
	private boolean wireframe=false;//Wireframe on?
	/** Are textures enabled? */
	private boolean texturesOn=true;//Textures on?
	/** The string to display to the screen */
	private String stringForDisplay=null;//String for displaying to screen
	
	/** Are there any textures waiting to be initialised? */
	private boolean toInitTextures=false;//Are there any textures to add?
	/** Has a VBO update been requested? */
	private boolean VBOUpdate=false;//Did anything request a VBOUpdate?
	
	/** The camera object that stores camera position and orientation */
	private Camera cam;//Camera
	/** The list of keys and if they are currently pressed (true for pressed but not released yet) */
	private boolean[] keyboard= new boolean[200];
	/** The time the last camera update happened */
	private long timeOfLastKeyUpdate=0;
	/** The time since the last camera update */
	private long timeCounter=0;
	
	/**
	 * Creates a renderer and prepares it for rendering.
	 */
	Renderer()
	{
		closing=false;
		FPSCounter = new FPSSystem();//For calculating fps
		//Prepare OpenGL
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		//Prepare a canvas and set it listening for rendering events
		canvas = new GLCanvas(caps);
		canvas.addGLEventListener(this);
		//Add listener for key presses
		addKeyListener();
	}
	/**
	 * Returns if the renderer is closing. When the frame is closed this will change to true.
	 * @return True or false. False if the renderer is not closing
	 */
	public boolean getClosing()
	{
		if(frame.isVisible()==false)
		{
			closing=true;
			System.out.println("Closing renderer");
		}
		return closing;
	}
	/**
	 * Sets the objects the renderer is to display to the given objects
	 * @param inpObjs The objects to display
	 */
	public void setObjects(ArrayList<CustomObject> inpObjs)
	{
		if(objects.size()>0)
		{
			if(objects.size()>=inpObjs.size())
			{
				for(int i=0;i<inpObjs.size();i++)
				{
					objects.set(i, inpObjs.get(i));
				}
			}
			else
			{
				objects=inpObjs;
			}
		}
		else
		{
			objects=inpObjs;
		}
	}
	/**
	 * Sets the frame the canvas is in, used for closing the frame when exit is chosen.
	 * @param inpFrame The frame the canvas is attached to
	 */
	public void setFrame(JFrame inpFrame)
	{
		frame=inpFrame;
	}
	/**
	 * Gets the canvas being used for OpenGL
	 * @return The GLCanvas being used
	 */
	public GLCanvas getCanvas()
	{
		return canvas;
	}
	/**
	 * Sets up the hotkeys for turning textures on/off etc and for closing the program
	 */
	private void addKeyListener()
	{
		canvas.addKeyListener(new KeyListener()
		{
			@Override
			public void keyPressed(KeyEvent e) 
			{
				int code=e.getKeyCode();//The key pressed
				if(code==KeyEvent.VK_ESCAPE)//Escape to exit TODO add a confirm dialogue
				{
					closing=true;
					frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
				}
				else if(code==KeyEvent.VK_V)//V for VSync on/off
				{
					vSync=!vSync;
				}
				else if(code==KeyEvent.VK_B)//B for wireframe on/off
				{
					wireframe=!wireframe;
				}
				else if(code==KeyEvent.VK_N)//N for textures on/off
				{
					texturesOn=!texturesOn;
				}
				else if(code==KeyEvent.VK_SPACE)
				{
					cam=new Camera(0,0,0,0,0,0);//Reset the camera
				}
				else
				{
					keyboard[code]=true;//The key is pressed
				}
			}
			@Override
			public void keyReleased(KeyEvent e) 
			{
				keyboard[e.getKeyCode()]=false;//The key is released
			}
			@Override
			public void keyTyped(KeyEvent e) 
			{
				//Unused
			}
		});
	}
	/**
	 * Checks if the camera movement hotkeys are being pressed and updates the camera accordingly
	 * limits the camera to 60 updates per second
	 */
	public void checkKeys()
	{
		long curTime=System.currentTimeMillis();
		long timeDifference=curTime-timeOfLastKeyUpdate;
		if(timeDifference>=16)
		{
			//Strafe right
			if(keyboard[KeyEvent.VK_D])
			{
				double ang=Math.toRadians(cam.getRotation().getY());
				double plusZ=Math.sin(ang)/5;
				double plusX=Math.cos(ang)/5;
				cam.incZ(-plusZ);
				cam.incX(plusX);
			}
			//Strafe left
			if(keyboard[KeyEvent.VK_A])
			{
				double ang=Math.toRadians(cam.getRotation().getY());
				double plusZ=Math.sin(ang)/5;
				double plusX=Math.cos(ang)/5;
				cam.incZ(plusZ);
				cam.incX(-plusX);
			}
			//Move forwards
			if(keyboard[KeyEvent.VK_W])
			{
				double ang=Math.toRadians(cam.getRotation().getY());
				double plusZ=Math.cos(ang)/5;
				double plusX=Math.sin(ang)/5;
				cam.incZ(-plusZ);
				cam.incX(-plusX);
				
			}
			//Move backwards
			if(keyboard[KeyEvent.VK_S])
			{
				double ang=Math.toRadians(cam.getRotation().getY());
				double plusZ=Math.cos(ang)/5;
				double plusX=Math.sin(ang)/5;
				cam.incZ(plusZ);
				cam.incX(plusX);
			}
			//Move up
			if(keyboard[KeyEvent.VK_SHIFT])
			{
				cam.incY(0.2);
			}
			//Move down
			if(keyboard[KeyEvent.VK_CONTROL])
			{
				cam.incY(-0.2);
			}
			//Turn clockwise
			if(keyboard[KeyEvent.VK_E])
			{
				cam.incRotY(-0.5);
			}
			//Turn anti clockwise
			if(keyboard[KeyEvent.VK_Q])
			{
				cam.incRotY(0.5);
			}
			timeOfLastKeyUpdate=curTime;
		}
	}
	/**
	 * Initialises OpenGL settings, lighting and VBOs for use later. Is run once on startup.
	 */
	public void init(GLAutoDrawable drawable) 
	{
		//OpenGL initialization code
		GL2 gl= drawable.getGL().getGL2();
		glu= new GLU();

		gl.glShadeModel(GLLightingFunc.GL_SMOOTH);//Smooth Shading
	    gl.glEnable(GLLightingFunc.GL_LIGHTING);//Enable lighting
	    gl.glEnable(GL.GL_TEXTURE_2D);//Enable 2d textures
	    gl.glEnable(GL.GL_CULL_FACE);//Enable face culling
	    gl.glEnable(GLLightingFunc.GL_NORMALIZE);//Normalize the normal vectors
	    gl.glTexEnvf(GL2.GL_TEXTURE_ENV,GL2.GL_TEXTURE_ENV_MODE , GL2.GL_MODULATE);//The texture environment
	    gl.glEnable(GL.GL_DEPTH_TEST); //Turn on depth testing
	    
	    //Ambient and diffuse from surface colour, call before glEnable(color_material)
	    gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE);
	    gl.glEnable(GLLightingFunc.GL_COLOR_MATERIAL);
	    
	    gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);//Use nicest perspective correction
		
	    gl.glClearColor(0.2f,0.2f,0.2f, 1.0f);//Set the background color to (0,0,0,1) RGBA
	    gl.glClearDepth(1.0f);//Clear the whole z-buffer
	    gl.glClearStencil(0);//Index used when stencil buffer is cleared
	    gl.glDepthFunc(GL.GL_LEQUAL);//Less than or equal depth testing
	    initLights(gl);//Initialise the light/s
		initVBO(drawable);//Initialise the VBOs used for drawing the objects
		cam= new Camera(0,0,0,0,0,0);
		
	}
	/**
	 * Creates and initialises the lights used for lighting
	 * @param gl The opengl in use
	 */
	private void initLights(GL2 gl)
	{
	    //Set up the colour of each light type
	    float lightKa[]={.05f, .05f, .05f, 1.0f};//Ambient
	    float lightKd[]={.9f, .9f, .9f, 1.0f};//Diffuse
	    float lightKs[]={1f, 1f, 1f, 1.0f};//Specular
	    
	    //Pass each colour to Light0
	    gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_AMBIENT, lightKa,0);
	    gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_DIFFUSE, lightKd,0);
	    gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_SPECULAR, lightKs,0);
	    
	    //Set up a position for the light
	    float lightPos[]={0, 0, 20, 1};//Default position
	    gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_POSITION, lightPos,0);//Pass the position
	    
	    gl.glEnable(GLLightingFunc.GL_LIGHT0);//Enable the light
	}
	/**
	 * Sets an external string to display on the screen. Used for displaying physics update time.
	 * @param inpString The string to display
	 */
	public void setStringForDisplay(String inpString)
	{
		stringForDisplay=inpString;
	}
	/**
	 * Creates and set textures on any objects with textures and sets toInitTextures to false.
	 */
	private void initObjectsTextures()
	{
		
		ArrayList<CustomObject> tempObjs = objects;
		int size=tempObjs.size();
		//For each object
		for(int i=0;i<size;i++)
		{
			//If it has a texture
			if(tempObjs.get(i).hasTexture())
			{
				try //Try and create its texture from the file stored
				{
					String current="";
					current = new java.io.File( "." ).getCanonicalPath();
					String fullpath=current+"/objects/"+tempObjs.get(i).getMaterial().getMapKd();
					tempObjs.get(i).setTexture(TextureIO.newTexture(new File(fullpath),true));
				} catch (GLException | IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
		toInitTextures=false;
		objects= tempObjs;
	}
	/**
	 * Requests that the renderer add textures to the input objects, waits until it is done then returns the objects with textures.
	 * @param inpObjs The objects to create textures for
	 * @return The objects with textures added
	 */
	public ArrayList<CustomObject> initTextures(ArrayList<CustomObject> inpObjs)
	{
		setObjects(inpObjs);//Sets the objects used by the renderer to those given
		toInitTextures=true;//There are textures waiting
		
		//While the textures haven't been set
		while(toInitTextures)
		{
			try 
			{
				Thread.sleep(1);//Sleep for a bit
			} catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		return objects;
	}
	/**
	 * Gets the objects currently being displayed by the renderer
	 * @return The objects in use
	 */
	public ArrayList<CustomObject> getObjects()
	{
		return objects;
	}
	/**
	 * Updates the counter that displays the number of vertices in the scene. 
	 * Call once each time a new object or multiple objects are added.
	 * This method is expensive.
	 */
	public void updateVerticesInScene()
	{
		numVerticesInScene=numVertices();//Get the number of vertices in the scene
	}
	/**
	 * Requests that the renderer redo its VBO. Used if the objects materials, textures, vertices (not position/rotation) are changed.
	 * Expensive, try not to call this, definitely not once per frame.
	 */
	public void requestVBOUpdate()
	{
		VBOUpdate=true;
	}
	/**
	 * Sets up the VBO used for rendering. 
	 * Expensive method, called only when the vertices in the scene (number of objects, object types etc) are changed.
	 * Not called on a position/rotation change of objects.
	 * @param drawable The drawable object for OpenGL
	 */
	private void initVBO(GLAutoDrawable drawable)
	{
		GL2 gl2 = drawable.getGL().getGL2();//Prep OpenGL
		int size=objects.size();
		VBOSize=size;
		/*
		 * Create the VBO to store buffer indexes. Stored as follows:
		 * {vertices, normals, textures, materials}
		 * {4*objNo, 4*objNo+1, 4*objNo+2, 4*objNo+3}
		 */
		VBO=new int[size*4];
		//Generate buffers for storing all the information and place the indexes into the VBO. 4 for each object
		gl2.glGenBuffers(size*4, VBO,0);
		
		//For every object in the scene
		for (int objNo=0;objNo<size;objNo++) 
		{
		    //Save the current object we are looking at (easier than using get(objNo) each time
			CustomObject tempObj=objects.get(objNo);
		    
		    /*
		     * Vertices
		     */
		    
		    //Bind the buffer we are working with (this case, vertices or 4*objNo)
		    gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, VBO[4*objNo]);

		    //Create a buffer for the vertices. Size of the buffer is the number of doubles used.
		    //Size is: number of faces * number of vertices per face * doubles per vertex
		    // Or: faces*3*3
		    int bufferSize=tempObj.getFaces().size()*3*3;
		    //Allocate the required space to the vertices buffer
		    vertices=FloatBuffer.allocate(bufferSize);

		    //Add all the vertices to the vertices buffer.
		    //Some are duplicated when shared between faces as a sacrifice of some memory space for speed.
		    for (int faceNo=0;faceNo<tempObj.getFaces().size();faceNo++) //For each face
		    {
		        //Get each vertex of the face
		        for (int vertexNo=0;vertexNo<3;vertexNo++) 
		        {
		        	//Get the index of the vertex in the vertex list
		            int vertexIndex=tempObj.getFaces().get(faceNo).getVertices().get(vertexNo);

		            //Add x,y,z of the vertex to the buffer 
		            vertices.put((float)tempObj.getVertices().get(vertexIndex-1).getX());
		            vertices.put((float)tempObj.getVertices().get(vertexIndex-1).getY());
		            vertices.put((float)tempObj.getVertices().get(vertexIndex-1).getZ());
		        }  
		    }

		    //Rewind the buffer (so it can used from the start when drawing)
		    vertices.rewind();

		    //Write the vertex buffer to the VBO. Size is bufferSize*sizeOfFloat.
		    //Static draw because it will be read often but modified once,
		    //This option just helps with performance optimisation within OpenGL.
		    gl2.glBufferData(GL.GL_ARRAY_BUFFER, bufferSize *4, vertices, GL.GL_STATIC_DRAW);

		    //Free the vertex buffer up for garbage collection
		    vertices=null;

		    
		    /*
		     * Normals
		     */

		    
		    //Bind the buffer we are working with (this case, normals or 4*objNo+1)
		    gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, VBO[4*objNo+1]);

		    //Create a buffer for the normals. Size of the buffer is the number of doubles used.
		    //Size is: number of faces * number of normals per face * doubles per normal
		    //Or: faces*3*3
		    bufferSize=tempObj.getFaces().size()*3*3;
		    normals=FloatBuffer.allocate(bufferSize);

		    //Add each normal to the normals buffer
		    //Again some may be duplicated for speed but less likely
		    for (int faceNo=0;faceNo<tempObj.getFaces().size();faceNo++) 
		    {
		        //For each vertex as each vertex has one normal
		        for (int vertexNo=0;vertexNo<3;vertexNo++) 
		        {
		        	//The index of the vertex we are getting normals from in the vertex list
		            int vertexIndex=tempObj.getFaces().get(faceNo).getVertices().get(vertexNo);

		            //Add the normal x,y,z to the buffer
		            double[] normal = tempObj.getVertices().get(vertexIndex-1).getNormal();
		            normals.put((float)normal[0]);//Normal x
		            normals.put((float)normal[1]);//Normal y
		            normals.put((float)normal[2]);//Normal z
		        }  
		    }

		    //Rewind the buffer (so it can used from the start when drawing)
		    normals.rewind();

		    //Write the normals buffer to the VBO. Size is bufferSize*sizeOfFloat.
		    //Static draw because it will be read often but modified once,
		    //This option just helps with performance optimisation within OpenGL.
		    gl2.glBufferData(GL.GL_ARRAY_BUFFER, bufferSize *4, normals, GL.GL_STATIC_DRAW);

		    //Free the normals buffer for garbage collection
		    normals=null;

		    /*
		     * Textures
		     */
		    
		    if(tempObj.hasTexture()) //No point doing textures if the object doesn't have any
		    {
		    	//Bind the buffer we are working with (this case, textures or 4*objNo+2)
		        gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, VBO[4*objNo+2]);  

		        //Create a buffer for the textures if the object has a texture. Size of the buffer is the number of doubles used.
			    //Size is: number of faces * number of vertices per face * doubles per texture coord
			    // Or: faces*3*2
			    bufferSize=tempObj.getFaces().size()*3*2;
			    textures=FloatBuffer.allocate(bufferSize);
			    
			    //Add all the texture coords to the textures buffer
			    //Should be no duplication here unless parts of the texture are reused
		        for (int faceNo=0;faceNo<tempObj.getFaces().size();faceNo++) 
		        {
		        	//For each vertex as each vertex has one set of texture coords
		            for (int vertexNo=0;vertexNo<3;vertexNo++) 
		            {
		            	//The index of the vertex we are getting texture coords from in the vertex list
		                int vertexIndex=tempObj.getFaces().get(faceNo).getVertices().get(vertexNo);
		                double[] textureCoords = tempObj.getVertices().get(vertexIndex-1).getTextureCoords();
		                //Put the texture coords into the buffer
		                textures.put((float)textureCoords[0]);//X
		                textures.put((float)textureCoords[1]);//Y
		            }  
		        }

		        //Rewind the buffer (so it can used from the start when drawing)
		        textures.rewind();  

		        //Write the texture buffer to the VBO. Size is bufferSize*sizeOfFloat.
			    //Static draw because it will be read often but modified once,
			    //This option just helps with performance optimisation within OpenGL.
		        gl2.glBufferData(GL.GL_ARRAY_BUFFER, bufferSize*4, textures, GL.GL_STATIC_DRAW); 

		        //Free the buffer for garbage collection
		        textures = null;
		    }

		    /*
		     * Colours
		     */
		    
		    
		    //Bind the buffer we are working with (colours, or 4*objNo+3)
		    gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, VBO[4*objNo+3]);

		 	//Create a buffer for the colours. Size of the buffer is the number of doubles used.
		    //Size is: number of faces * number of vertices per face * doubles per vertex
		    // Or: faces*3*3 as using 3 double colour (assuming alpha is 1)
		    bufferSize=tempObj.getFaces().size()*3*3;
		    colours=FloatBuffer.allocate(bufferSize);

		    //Add all the colour information to the buffer
		    for (int faceNo=0;faceNo<tempObj.getFaces().size();faceNo++) 
		    {
		    	//If the object has a material use it
		    	if(tempObj.getMaterial()!=null)
		    	{
		    		//3 times as each vertex in a face has the same colour
		            for (int i=0; i<3; i++) 
		            {
		                colours.put((float)tempObj.getMaterial().getKa().getX());//R
		                colours.put((float)tempObj.getMaterial().getKa().getY());//G
		                colours.put((float)tempObj.getMaterial().getKa().getZ());//B
		                //A Assumed 1
		            }
		    	}
		    	else//Otherwise give it a default colour
		    	{
		    		//9 times as 3 vertices in face and 3 doubles in a colour
		    		for(int i=0; i<9;i++)
		    		{
		    			colours.put(0.5f);//R,G and B
		    		}
		    	}
		        
		    }

		    //Rewind the buffer (so it can used from the start when drawing)
		    colours.rewind();

		    //Write the colours buffer to the VBO. Size is bufferSize*sizeOfFloat.
		    //Static draw because it will be read often but modified once,
		    //This option just helps with performance optimisation within OpenGL.
		    gl2.glBufferData(GL.GL_ARRAY_BUFFER, bufferSize*4, colours, GL.GL_STATIC_DRAW);

		    //Free colours buffer for garbage collection
		    colours = null;
		}
	}
	/**
	 * Disposes all the OpenGL stuff. Called on program exit.
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) 
	{
		for(int i=0;i<objects.size();i++)
		{
			objects.get(i).getTexture().destroy(drawable.getGL().getGL2());
		}
		CustomObject.clearTextures();
	}
	/**
	 * Displays all objects in the scene using the VBO. 
	 * initVBO() must be called some time previously before this works.
	 * @param drawable The drawable to draw to.
	 */
	@Override
	public void display(GLAutoDrawable drawable)
	{
		checkKeys();
		float lightPos[]={0, 60, 0, 1};//Position of the main light
	
		displaySetup(drawable);//Setup the display
			
			//Setup OpenGL and GLU
		GL2 gl2 = drawable.getGL().getGL2();
		GLU glu = new GLU();
	
			//Clear everything ready for drawing
		gl2.glColor3f(1,1,1);
		gl2.glClearDepth(1.0f);
		gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		int size=objects.size();
		if(VBOSize!=size)
		{
			initVBO(drawable);
		}
		if(size>0)
		{
			
			//Enable client states for normals and vertices as they are always used (colours and textures are not)
			gl2.glEnableClientState(GL2.GL_NORMAL_ARRAY);
			gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
	
			//Draw all of the objects in the scene.
			for (int i=0; i<size; i++) 
			{
				gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);//Switch to modelview matrix for drawing
				gl2.glPushMatrix();//Store the current matrix
			    gl2.glLoadIdentity(); //Reset the modelview matrix so nothing interferes with the drawing of this object
			    
			    
				//Camera rotation
			    Rotation camRot=cam.getRotation();
				gl2.glRotated(-camRot.getX(), 1, 0, 0);
				gl2.glRotated(-camRot.getY(), 0, 1, 0);
				gl2.glRotated(-camRot.getZ(), 0, 0, 1);
				
			    //Camera position
				Position camPos=cam.getPosition();
				gl2.glTranslated(-camPos.getX(),-camPos.getY(), -camPos.getZ());

				
			    //Get the current object to draw
			    CustomObject tempObj=objects.get(i);   
			    int numFaces=tempObj.getFaces().size()*3;
			    
			    //Set the translation of the object
			    gl2.glTranslatef((float)tempObj.getPosition().getX(),(float)tempObj.getPosition().getY(),(float)tempObj.getPosition().getZ());
			    //Set the rotation of the object
			    gl2.glRotatef((float)tempObj.getRotation().getX(), 1f, 0f, 0f);
			    gl2.glRotatef((float)tempObj.getRotation().getY(), 0f, 1f, 0f);
			    gl2.glRotatef((float)tempObj.getRotation().getZ(), 0f, 0f, 1f);
			      
			    //Disable textures until they are used as they may not be
			    gl2.glDisable(GL.GL_TEXTURE_2D);
			    
			    //Disable colour materials until they are used as they may not be
			    gl2.glDisable(GL2.GL_COLOR_MATERIAL);
	
			    //If the object has a texture use it 
			    if(texturesOn&&tempObj.hasTexture()) 
			    {
			    	//Enable the texture client state and textures in general as we have a texture
			        gl2.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
			        gl2.glEnable(GL.GL_TEXTURE_2D);    
			        //Get the texture and bind it and the texture buffer
			        gl2.glBindTexture(GL.GL_TEXTURE_2D, tempObj.getTexture().getTextureObject());
			        gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, VBO[4*i+2]);
			        gl2.glTexCoordPointer(2, GL.GL_FLOAT, 0, 0);//2 because 2 doubles in a texture coord
			        
			    } 
			    //Otherwise just use colours
			    else 
			    { 
			    	//Enable colour client state and colour materials
			        gl2.glEnableClientState(GL2.GL_COLOR_ARRAY);
			        gl2.glEnable(GL2.GL_COLOR_MATERIAL);
			        //Bind the colour buffer
			        gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, VBO[4*i+3]);
			        gl2.glColorPointer(3, GL.GL_FLOAT, 0, 0);//3 because 3 double colour (RGB, no A)
			    }
	
			    //Bind the vertex buffer
			    gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, VBO[4*i]);
			    gl2.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
			    
			    //Bind the normal buffer
			    gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, VBO[4*i+1]);
			    gl2.glNormalPointer(GL.GL_FLOAT, 0, 0);
			     
			    //Draw the VBO
			    gl2.glDrawArrays(GL.GL_TRIANGLES, 0, numFaces);//Number of triangles=number of faces as triangular faces
			    
			    gl2.glPopMatrix();//Return the matrix we had before the object
			}
	
			//Disable normal and vertex client states as we are done with them
			gl2.glDisableClientState(GL2.GL_NORMAL_ARRAY);
			gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);
			
			//Push and reset the modelview matrix
			gl2.glPushMatrix();
		    gl2.glLoadIdentity();
		    gl2.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_POSITION, lightPos,0);//Draw the lights
		    gl2.glPopMatrix();//Return the modelview matrix

		}
		gl2.glDisable(GL.GL_TEXTURE_2D);
		displayGUI(drawable);//Draw the gui to the screen
	}
	/**
	 * Sets up the display ready for drawing. 
	 * Inits textures if there are any waiting and enables/disables wireframes, vsync and textures. 
	 * @param drawable The OpenGL drawable
	 */
	private void displaySetup(GLAutoDrawable drawable)
	{
		GL2 gl2 = drawable.getGL().getGL2();
		
		if(toInitTextures && (objects.size()>0))//If there are textures waiting to be added
		{
			initObjectsTextures();//Add the textures
		}
		if(VBOUpdate && (objects.size()>0))
		{
			initVBO(drawable);
			VBOUpdate=false;
		}
		if(wireframe)//Enable wireframe
		{
			gl2.glPolygonMode( GL.GL_FRONT_AND_BACK, GL2.GL_LINE );
		}
		else//Disable wireframe
		{
			gl2.glPolygonMode( GL.GL_FRONT_AND_BACK, GL2.GL_FILL );
		}
		
		if(vSync)//Enable vsync
		{
			drawable.getGL().setSwapInterval(1);//Switches on/off v-sync 1 for on, 0 for off
		}
		else//Disables vsync
		{
			drawable.getGL().setSwapInterval(0);//Switches on/off v-sync 1 for on, 0 for off
		}
	}
	/**
	 * Displays the GUI to the screen. Calculates fps and frame time to do this.
	 * @param drawable The drawable to draw the GUI to
	 */
	private void displayGUI(GLAutoDrawable drawable)
	{
		//Shows the fps and number of vertices text
		FPSCounter.showFPSAndVerts(drawable,glu, numVerticesInScene);
		//Adds a frame to the fps counter and checks the fps
		FPSCounter.addFrame();
        FPSCounter.checkFPS();
        //If there is a custom string to display
        if(stringForDisplay!=null)
        {
        	FPSCounter.displayString(drawable, glu, stringForDisplay,300,0);//Display it
        }
        //Display the keybindings and status to the screen
        FPSCounter.displayString(drawable, glu, "Keybinds:", 1, 280);
        FPSCounter.displayString(drawable, glu, "VSync-V="+vSync, 1, 270);
        FPSCounter.displayString(drawable, glu, "Wireframe-B="+wireframe, 1, 260);
        FPSCounter.displayString(drawable, glu, "Textures-N="+texturesOn, 1, 250);
	}
	/**
	 * Reshapes the canvas. Called when the window is resized or resolution changed
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) 
	{
		GL2 gl2 = drawable.getGL().getGL2();
		
		if (height == 0)
		{
			height = 1;//Prevent dividing by 0
		}
	    double aspect = (double)width / height;
	    
	    //Set the viewport to the entire window
	    gl2.glViewport(0, 0, width, height);
	 
	    //Setup perspective projection with an aspect ratio matching the viewport
	    gl2.glMatrixMode(GLMatrixFunc.GL_PROJECTION);//Select projection matrix
	    gl2.glLoadIdentity();//Reset it
	    glu.gluPerspective(60, aspect, 0.1, 1000.0);//vertical fov, aspect, zNear, zFar
	 
	    //Go back to modelview matrix and reset it
	    gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
	    gl2.glLoadIdentity();
	}
	/**
	 * Gets the number of vertices currently in the scene.
	 * Note: this is the number of vertices in the object, it does not take into account the duplication of vertices in the VBO
	 * @return The number of vertices in the scene
	 */
	private int numVertices()
	{
		int vertices=0;
		//For all the objects
		int size=objects.size();
		for(int i=0;i<size;i++)
		{
			//Get the vertices in each object
			vertices+=objects.get(i).getVertices().size();
		}
		return vertices;
	}

}
