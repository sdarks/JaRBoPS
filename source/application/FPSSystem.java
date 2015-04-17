package application;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * An fps system for use with OpenGL applications. Requires GL2 capabilities.
 * @author Sam Dark
 *
 */
public class FPSSystem 
{
	/** The current fps */
	private double fps=0;//The current fps
	/** The current fps in readable form (less decimal places) */
	private double readableFPS=0;//FPS in readable form (rounded)
	/** The time it took to render the current frame (1/fps) */
	private double frameTime=0;//Time for current frame (1/fps)
	/** The time it took to render the current frame in readable form (less decimal places) */
	private double readableFrameTime=0;//Readable time for current frame (rounded)
	/** The time the fps was last updated */
	private long lastTime=0;//The time the fps was last updated
	/** The time the fps system was started */
	private long startTime=0;//The time the fps system was started
	/** How many frames there have been since the last fps update */
	private int framesSinceLast=0;//The frames since the last fps update
	/** The length of time between fps updates in ms */
	private long updateLength=0;//The length of time between the fps updates in ms
	
	/**
	 * Creates and starts an fps system with the default 500ms update length
	 */
	FPSSystem()
	{
		updateLength=500;//Default update length of 500ms
		init();//Initialises the FPSSystem
	}
	/**
	 * Creates and starts an FPSSystem with a custom update length
	 * @param inpUpdateLength The custom update length in ms
	 */
	FPSSystem(long inpUpdateLength)
	{
		updateLength=inpUpdateLength;
		init();//Initialises the FPSSystem
	}
	/**
	 * Initialises the FPSSystem. Must be called when created or the FPSSystem will not function correctly
	 */
	private void init()
	{
		//The time the fps counter started
		startTime=System.currentTimeMillis();
	}
	/**
	 * Updates the FPS number based on the time since the last update and the number of frames since then
	 */
	public void checkFPS()
	{
		long runningTime=System.currentTimeMillis()-startTime;//How long the program has been running
		//Update fps every so many ms as defined by "updateLength"
		if(runningTime>(lastTime+updateLength))
		{
			//fps is frames since the last check divided by time (in seconds) since last check
			fps=framesSinceLast/((double)(runningTime-lastTime)/1000);
			readableFPS=Math.round(fps*100.0)/100.0;//Round the fps counter to make it more readable
			frameTime=1/fps;
			readableFrameTime=Math.round(frameTime*100000.0)/100000.0;
			lastTime=runningTime;//We just updated the fps
			framesSinceLast=0;//No frames yet as we just updated
		}
	}
	/**
	 * Draws the fps, frame time and number of vertices to the screen. Requires openGL2 capabilities and GLU for orthogonal projection.
	 * @param drawable The drawable GLAutoDrawable object to draw to.
	 * @param glu The GLU to use for orthogonal projection
	 * @param noVerts The number of vertices it is displaying (easier if this is shown with fps).
	 */
	public void showFPSAndVerts(GLAutoDrawable drawable, GLU glu, int noVerts)
	{
		String fpsString="FPS:"+readableFPS + "  Frame time:"+readableFrameTime+ "  Vertices:"+noVerts;
		displayString(drawable,glu,fpsString,1,1);
	}
	/**
	 * Draws the fps and frame time to the screen. Requires openGL2 capabilities and GLU for orthogonal projection.
	 * @param drawable The drawable GLAutoDrawable object to draw to.
	 * @param glu The GLU to use for orthogonal projection
	 */
	public void showFPS(GLAutoDrawable drawable, GLU glu)
	{
		String fpsString="FPS:"+readableFPS + "  Frame time:"+readableFrameTime;
		displayString(drawable,glu,fpsString,1,1);
	}
	/**
	 * Displays a given string to a given position
	 * @param inp The string to display
	 * @param drawable The drawable GLAutoDrawable object to draw to.
	 * @param glu The GLU to use for orthogonal projection
	 * @param inpX The x position on the screen to display to
	 * @param inpY The y position on the screen to display to
	 */
	public void displayString(GLAutoDrawable drawable, GLU glu,String inp, int inpX, int inpY)
	{
		GL2 gl= drawable.getGL().getGL2(); //Assumes we have OpenGL2 capabilities
		GLUT glut= new GLUT();//GLUT for bitmaps
		gl.glDisable(GLLightingFunc.GL_LIGHTING);//Disables any lighting that may interfere with the drawing
		gl.glDisable(GL.GL_TEXTURE_2D);
		//Backup the modelview matrix
	    gl.glPushMatrix();//Save the current modelview matrix to restore later
	    gl.glLoadIdentity();//Reset the modelview matrix
		//Switch to orthogonal projection (2d)
	    gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);//Switch to the projection matrix
	    gl.glPushMatrix();//Save the projection matrix to restore later
	    gl.glLoadIdentity();//Reset the projection matrix
	    glu.gluOrtho2D(0, 400, 0, 300);//Set to orthogonal projection (2d)
		
		//Draw the fps number to the screen
		gl.glColor3f(0.0f,1.0f,0.0f);//Colour of the fps text, green is standard
		gl.glRasterPos2f(inpX,inpY);//Position of the text
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18,inp);//Draw the fps in size 18 Helvetica with a "FPS:" label before it
		
		//Restore matrices and lighting
	    gl.glPopMatrix();//Restore the projection matrix saved earlier
	    gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);//Switch back to the modelview matrix
	    gl.glPopMatrix();//Restore the modelview matrix saved earlier
	    
	    gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glEnable(GLLightingFunc.GL_LIGHTING);//Re-enables the lighting
	}
	/**
	 * Increases the number of frames since the last fps update. Should be called with every draw to the screen.
	 */
	public void addFrame()
	{
		//Another frame has been shown since last check
		framesSinceLast++;
	}
	/**
	 * Gets the current average fps number
	 * @return The current fps
	 */
	public double getFPS()
	{
		return fps;
	}
	
	
}
