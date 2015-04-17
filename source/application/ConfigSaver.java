package application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * A utility class for saving configs to a file
 * @author Sam Dark
 *
 */
public class ConfigSaver 
{
	/**
	 * Saves a config to file. Saves to /configs/filename
	 * @param filename The name of the file to save including extension
	 * @param objects The objects in the config to save
	 * @param grav The gravity of the config to save
	 * @param elas The elasticity of the object to save
	 * @throws IOException Throws exceptions if it cannot save to the file
	 */
	public static void saveConfig(String filename, ArrayList<CustomObject> objects, double grav, double elas) throws IOException
	{
		String current="";
		try {
			current = new java.io.File( "." ).getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String fullFilename=current+"/configs/"+filename;
		File configFile = new File(fullFilename);
		FileOutputStream outputStream = new FileOutputStream(configFile);
	 
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		//Number of objects
		writer.write(String.valueOf(objects.size()));
		writer.newLine();
		//Object filenames
		for(int i=0;i<objects.size();i++)
		{
			writer.write(objects.get(i).getName());
			writer.newLine();
		}
		//Low poly version file names
		for(int i=0;i<objects.size();i++)
		{
			writer.write("lowPoly"+objects.get(i).getName());
			writer.newLine();
		}
		//Starting positions
		for(int i=0;i<objects.size();i++)
		{
			Position tempPos= objects.get(i).getPosition();
			writer.write(tempPos.getX()+" "+tempPos.getY()+" "+tempPos.getZ());
			writer.newLine();
		}
		//Starting rotations
		for(int i=0;i<objects.size();i++)
		{
			Rotation tempRot= objects.get(i).getRotation();
			writer.write(tempRot.getX()+" "+tempRot.getY()+" "+tempRot.getZ());
			writer.newLine();
		}
		//Can each object move?
		for(int i=0;i<objects.size();i++)
		{
			writer.write(String.valueOf(objects.get(i).canMove()));
			writer.newLine();
		}
		//Inverse mass for each object
		for(int i=0;i<objects.size();i++)
		{
			writer.write(String.valueOf((int)objects.get(i).getInverseMass()));
			writer.newLine();
		}
		//Are the object effected by gravity?
		for(int i=0;i<objects.size();i++)
		{
			writer.write(String.valueOf(objects.get(i).getGravity()));
			writer.newLine();
		}
		//Starting velocity
		for(int i=0;i<objects.size();i++)
		{
			writer.write(String.valueOf(objects.get(i).getVel().getX()+" "+objects.get(i).getVel().getY()+" "+objects.get(i).getVel().getZ()));
			writer.newLine();
		}
		//Global gravity
		writer.write(String.valueOf(grav));
		writer.newLine();
		//Global elasticity
		writer.write(String.valueOf(elas));
		writer.newLine();
		//Close the writer
		writer.close();
	}
}
