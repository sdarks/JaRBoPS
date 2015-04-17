package application;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.media.opengl.GLException;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 * Class for importing .obj models into a renderer. Imports them as a CustomObject object.
 * Requires as much memory as the .obj file size.
 * Currently supports object files with only triangular faces, no groups and will only use one material form the file.
 * @author Sam Dark
 *
 */
public class ObjImporter 
{	
	/** The position of the importer in the character array */
	private static int position=0;//Stores the position of the importer in the character array
	/** The position of the importer in the material file */
	private static int materialPos=0;
	/** The list of characters from the file */
	private static ArrayList<Character> chars= new ArrayList<Character>();
	/** The list of vertices extracted from the file */
	private static ArrayList<Vertex> vertices= new ArrayList<Vertex>();
	/** The list of normals extracted from the file */
	private static ArrayList<Position> normals= new ArrayList<Position>();
	/** The list of texture coordinates extracted from the file */
	private static ArrayList<Position> textureCoords= new ArrayList<Position>();
	/** The list of faces extracted from the file */
	private static ArrayList<TriangularFace> faces= new ArrayList<TriangularFace>();
	/** The list of material files extracted from the file */
	private static ArrayList<String> materialFiles = new ArrayList<String>();
	/** The list of material extracted from the file */
	private static ArrayList<Material> materials= new ArrayList<Material>(); 
	/** The current material file as a list of characters */
	private static ArrayList<Character> materialFile = new ArrayList<Character>();
	/** The name of the current active material */
	private static String activeMaterialName="";
	/** Are the normals in the object file in order? */
	private static boolean normalsInOrder=true;
	/** Does the object in the file have a texture? */
	private static boolean hasTexture=false;
	
	/**
	 * Imports the object specified in the .obj file and returns a CustomObject containing the object.
	 * @param filename The filename of the .obj file
	 * @return A CustomObject containing the details of the object in the file
	 */
	public static CustomObject importObj(String filename)
	{
		//Reset all arraylists and position as new file
		position=0;
		materialPos=0;
		chars=new ArrayList<Character>();
		vertices= new ArrayList<Vertex>();
		normals =new ArrayList<Position>();
		faces= new ArrayList<TriangularFace>();
		textureCoords= new ArrayList<Position>();
		normalsInOrder=true;
		materialFiles = new ArrayList<String>();
		materialFile = new ArrayList<Character>();
		materials = new ArrayList<Material>();
		activeMaterialName="";
		hasTexture=false;
		
		/*Read the file into memory 
		(should be almost no situation where the file is too big for memory, files tend to be only a few mb max)*/
		//If file read correctly extract the information from the file
	    if(readFileIn(filename, chars)==true)
	    {
	    	goThroughChars();//Extract all the information from the characters in the file
	    	CustomObject output = new CustomObject(vertices,faces,filename);//Create an object to store information gathered from file
	    	//Set the material of the object
	    	for(int i=0; i<materials.size(); i++)
	    	{
	    		if(materials.get(i).getName().matches(activeMaterialName))
	    		{
	    			output.setMaterial(materials.get(i));
	    		}
	    	}
	    	//If the object has a texture set the flag
	    	if(hasTexture)
	    	{
	    		output.setHasTexture(true);
	    	}
	    	else
	    	{
	    		//Otherwise get default material
	    		if(output.getMaterial()==null)
	    		{
	    			output.setMaterial(new Material("Default"));
	    		}
	    	}
	 	    return output;
	    }
	    //Otherwise return an error as the file didn't read properly
	    else
	    {
	    	System.out.println("Error reading .obj file ("+filename+"), the file may not exist or there was an error reading it, please try again");
	    }
	    return null;
	}
	/**
	 * Gets the list of materials the object importer has stored
	 * @return The materials the object importer currently has stored
	 */
	public static ArrayList<Material> getMaterials()
	{
		return materials;
	}
	
	/**
	 * Reads the contents of a file into a given ArrayList of characters.
	 * @param filename The file to read in
	 * @param output The ArrayList of characters to put the file in
	 * @return True if file read successfully, false if error
	 */
	private static boolean readFileIn(String filename, ArrayList<Character> output)
	{
		String current="";
		try 
		{
			current = new java.io.File( "." ).getCanonicalPath();
		
			//System.out.println("Current dir of obj importer:"+current+filename);
			filename=current+"/objects/"+filename;
		
			BufferedReader file=null;//File reader
		
			file = new BufferedReader(new FileReader(filename));//Open the file
		
		
			int read;//The result of the read (-1 if error)
			Character readChar;//The character it read in
	    
	    	//Read a character in and add it to the characters list
			while((read=file.read()) != -1)
			{
				readChar=(char) read;
				output.add(readChar);
			}
		
			file.close();
		}
	    catch (IOException e) 
	    {
			//File failed to close TODO
			e.printStackTrace();
			return false;
		}
	    return true;
	}
	/**
	 * Goes through the characters extracting the various information about vertices, faces, textures etc
	 */
	private static void goThroughChars()
	{
		position=0;//Reset the position in the character array
		
		//Go through all the characters
		while(position<chars.size())
		{
			switch(chars.get(position))
			{
				case 'v'://Vertex information
				{
					if(chars.get(position+1)=='n')//Vertex normal
					{
						addVertexNormal();
					}
					else if(chars.get(position+1)=='t')//Vertex texture coordinate
					{
						addTextureCoord();
					}
					else if(chars.get(position+1)==' ')//Vertex position
					{
						addVertex();
					}
					else//Unrecognised
					{
						position=skipLine(position, chars);//Ignore line
					}
					break;
				}
				case 'f'://Face information
					{
						addFace();
						break;
					}
				case 'g': position=skipLine(position, chars);break;//Groups TODO
				case '#': position=skipLine(position, chars);break;//Comment, ignore
				case 'm'://Material library
				{
					getMaterialFile();
					break;
				}
				case 'u'://Use material
				{
					setActiveMaterial();
					break;
				}
				default: position=skipLine(position, chars);break;//Unrecognised, ignore
			}
			position++;
		}
		
		//If the normals are in the order given and not shared between faces
		if(normalsInOrder==true)
		{
			for(int i=0;i<vertices.size();i++)
			{
				vertices.get(i).setNormal(normals.get(i).getPosition());
			}
		}
		
		
	}
	/**
	 * Sets the current active material
	 */
	private static void setActiveMaterial()
	{
		ArrayList<String> tokens = getTokens();
		activeMaterialName=tokens.get(0);
		activeMaterialName=activeMaterialName.substring(0, activeMaterialName.length()-1);
	}
	/**
	 * Gets a material file and adds it to the material file list.
	 * Then gets the contents from the file
	 */
	private static void getMaterialFile()
	{
		ArrayList<String> tokens = getTokens();
		
		if(tokens.size()>0)
		{
			String tempFileName=tokens.get(0);
			tempFileName=tempFileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "");
			materialFiles.add(tempFileName);//Store the filename for later
			getMaterialsFromFile(tempFileName);//Get contents of material file
		}
	}
	/**
	 * Gets the materials from a given material file and stores them
	 * @param fileName The material file to read
	 */
	private static void getMaterialsFromFile(String fileName)
	{
		materialPos=0;//Reset the position counter
		materialFile = new ArrayList<Character>();
		readFileIn(fileName, materialFile);
		//Go through all the characters
		while(materialPos<materialFile.size())
		{
			switch(materialFile.get(materialPos))
			{
				case 'n'://newmtl TODO make this better, check for full "newmtl" not just "n"
				{
					boolean sameMat=true;
					Material tempMaterial=new Material();
					String tempName=getTokensMaterials().get(0);
					tempName=tempName.substring(0,tempName.length()-1);
					tempMaterial.setName(tempName);
					materialPos=skipLine(materialPos, materialFile);
					while(materialPos<materialFile.size()&&sameMat==true)
					{
						switch(materialFile.get(materialPos))
						{
							case 'n'://Vertex information
							{
								sameMat=false;
								materialPos--;
								break;
							}
							case 'K'://Face information
							{
								if(materialFile.get(materialPos+1)=='a')//Ambient colour
								{
									tempMaterial.setKa(getColour());
									
								}
								else if(materialFile.get(materialPos+1)=='d')//Diffuse colour
								{
									tempMaterial.setKd(getColour());
								}
								else if(materialFile.get(materialPos+1)=='s')//Specular colour
								{
									tempMaterial.setKs(getColour());
								}
								else//Unrecognised
								{
									materialPos=skipLine(materialPos, materialFile);//Ignore line
								}
								break;
							}
							case 'd'://Dissolve (Transparency)
							{
								tempMaterial.setD(Float.parseFloat(getTokensMaterials().get(0)));
								break;
							}
							case 'T'://Dissolve (Transparency)
							{
								tempMaterial.setD(Float.parseFloat(getTokensMaterials().get(0)));
								break;
							}
							case 'm':
							{
								hasTexture=true;
								tempMaterial.setMapKd(getKdFileName());
								materialPos=skipLine(materialPos, materialFile);
								break;
							}
							default: materialPos=skipLine(materialPos, materialFile);break;//Unrecognised, ignore
						}
						materialPos++;
					}
					materials.add(tempMaterial);
					break;
				}
				default: materialPos=skipLine(materialPos, materialFile);break;//Unrecognised, ignore
			}
			materialPos++;
		}
	}
	/**
	 * Gets the filename of the diffuse map (kd) from the material file
	 * @return The diffuse map filename
	 */
	private static String getKdFileName()
	{
		
		String map="";
		String filename=null;
		for(int i=0;i<6;i++)
		{
			if(materialPos+i<materialFile.size())
				map+=materialFile.get(materialPos+i);
		}
		if(map.matches("map_Kd"));
		{
			ArrayList<String> tokens = getTokensMaterials();
			
			filename = tokens.get(0);
		}
		return filename;

	}
	/**
	 * Gets rgb colour information for one colour from the material file
	 * @return The colour as a position where (x,y,z)=(r,g,b);
	 */
	private static Position getColour()
	{
		Position colour= new Position();
		//Gets the tokens containing the colour
		ArrayList<String> tokens = getTokensMaterials();
		//Return the colour
		colour.setPosition(Float.parseFloat(tokens.get(0)),Float.parseFloat(tokens.get(1)),Float.parseFloat(tokens.get(2)));
		return colour;
	}
	
	
	/**
	 * Gets the tokens in the line that the current position is on, adjusts the standard position
	 * @return The tokens in the line as an ArrayList of Strings
	 */
	private static ArrayList<String> getTokens()
	{
		ArrayList<String> tokens= new ArrayList<String>();
		position=skipUntilSpace(position, chars);//Skip past the "vt" "f" etc
		position=skipUntilNotSpace(position, chars);//Skip past the whitespace
		//Until the end of the line (or file if it's the last line)
		while(position<chars.size()&&!(chars.get(position)=='\n'))
		{
			String tempToken="";
			
			//Get the token
			while(position<chars.size()&&chars.get(position)!=' '&&chars.get(position)!='\n')
			{
				tempToken+=chars.get(position);
				position++;
			}
			tokens.add(tempToken);
			position=skipUntilNotSpace(position, chars);//Skip past white space to the next token
		}
		return tokens;
	}
	/**
	 * Gets the tokens in the line that the current position is on, adjusts the materialPos position
	 * @return The tokens in the line as an ArrayList of Strings
	 */
	private static ArrayList<String> getTokensMaterials()
	{
		ArrayList<String> tokens= new ArrayList<String>();
		materialPos=skipUntilSpace(materialPos, materialFile);//Skip past the "vt" "f" etc
		materialPos=skipUntilNotSpace(materialPos, materialFile);//Skip past the whitespace
		materialPos=skipUntilNotSpace(materialPos, materialFile);
		//Until the end of the line (or file if it's the last line)
		while(materialPos<materialFile.size()&&!(materialFile.get(materialPos)=='\n'))
		{
			String tempToken="";
			
			//Get the token
			while(materialPos<materialFile.size()&&materialFile.get(materialPos)!=' '&&materialFile.get(materialPos)!='\n')
			{
				tempToken+=materialFile.get(materialPos);
				materialPos++;
			}
			tokens.add(tempToken);
			materialPos=skipUntilNotSpace(materialPos, materialFile);//Skip past white space to the next token
		}
		return tokens;
	}
	/**
	 * Adds a vertex to the vertices list with the position specified in the current line
	 */
	private static void addVertex()
	{
		Vertex vertex= new Vertex();
		//Gets the tokens containing the vertex position
		ArrayList<String> tokens = getTokens();
		//Adds the vertex to the vertices list
		vertex.setPosition(Float.parseFloat(tokens.get(0)),Float.parseFloat(tokens.get(1)),Float.parseFloat(tokens.get(2)));
		vertices.add(vertex);
	}
	/**
	 * Adds a normal specified on the current line to the normals list
	 */
	private static void addVertexNormal()
	{
		ArrayList<String> tokens = getTokens();//The tokens containing the normal
		//Add the normal to the normals list
		normals.add(new Position(Float.parseFloat(tokens.get(0)),Float.parseFloat(tokens.get(1)),Float.parseFloat(tokens.get(2))));
	}
	/**
	 * Adds the current line as a texture coordinate to the list of texture coordinates
	 */
	private static void addTextureCoord()
	{
		//The texture coordinate can be expressed as a 3d position with z=0.0
		Position textureCoord= new Position();
		
		//Get the tokens describing the texture coordinate
		ArrayList<String> tokens=getTokens();
		
		//If the file does not specify the z coordinate of the position (it is always 0.0)
		if(tokens.size()<3)
		{
			tokens.add("0.0");
		}
		//Add the texture coordinate gathered
		textureCoord.setPosition(Float.parseFloat(tokens.get(0)),Float.parseFloat(tokens.get(1)),Float.parseFloat(tokens.get(2)));
		textureCoords.add(textureCoord);
	}
	/**
	 * Adds a face to the list of faces. Also adds any texture or normal information to the vertices in the face
	 */
	private static void addFace()
	{
		ArrayList<Integer> face = new ArrayList<Integer>();
		//Get the tokens describing the texture coordinate
		ArrayList<String> tokens=getTokens();
		
		//Deal with all the vertices that are listed in the face
		for(int i=0; i<tokens.size();i++)
		{
			String temp="";
			if(normalsInOrder==true&&tokens.get(i).contains("/"))
			{
				//The normals are not in order as they have a specified order in the face
				normalsInOrder=false;
			}
			//Add the normals listed in the face declaration to the corresponding vertex and return the vertex
			temp=addNormalAndTextureToVertex(tokens.get(i));
			//Add the list of vertices to the face
			face.add(Integer.parseInt(temp));
		}
		//Add the face to the list of faces
		faces.add(new TriangularFace(face));
	}
	/**
	 * Adds a normal and texture coordinate (if present) to a vertex given a face triplet in the form "x/y/z" where y and z are optional
	 * @param inp A face triplet (for example "4/15/11")
	 * @return The vertex specified in the face triplet
	 */
	private static String addNormalAndTextureToVertex(String inp)
	{
		//The input will be in the format "a/b/c" where b and c are optional
		String temp=inp;
		int texture=0;
		int index = inp.indexOf("/");
		int lastSlash= inp.lastIndexOf("/");
		
		String output="";
		if (index != -1)
		{
		   temp = inp.substring(0, index);
		}
		output=temp;//Output the vertex we are adding the normal to
		int vertex=Integer.parseInt(temp);//The vertex involved
		
		if(index != -1 && lastSlash != -1)
		{
			temp=inp.substring(index+1,lastSlash);
		}
		if(hasTexture)
		{
			texture=Integer.parseInt(temp);//The texture coords for the vertex
		}
		if (lastSlash != -1)
		{
		   temp = inp.substring(lastSlash+1, inp.length());
		}
		temp=temp.replaceAll("\\s","");//Remove any whitespace before parseInt
		int normal=Integer.parseInt(temp);//The normal for the vertex
		
		
		vertices.get(vertex-1).setNormal(normals.get(normal-1).getPosition());//Add the normal to the vertex
		if(hasTexture)
			vertices.get(vertex-1).setTextureCoords(textureCoords.get(texture-1));//Add the texture coords to the vertex
		return output;
	}
	/**
	 * Moves the position in the chars list to after the next newline character
	 * @param inpPosition The current position in the array of characters
	 * @param inpChars The array of characters to search through
	 * @return The new position in the array of characters
	 */
	private static int skipLine(int inpPosition, ArrayList<Character> inpChars)
	{
		int temp=inpPosition;
		while(temp<inpChars.size()&&inpChars.get(temp)!='\n')
		{
			temp++;
		}
		return temp;
	}
	/**
	 * Moves the position in the chars list to the next non-space character
	 * @param inpPosition The current position in the array of characters
	 * @param inpChars The array of characters to search through
	 * @return The new position in the array of characters
	 */
	private static int skipUntilNotSpace(int inpPosition, ArrayList<Character> inpChars)
	{
		int temp=inpPosition;
		while((temp<inpChars.size())&&inpChars.get(temp)==' ')
		{
			temp++;
		}
		return temp;
	}
	/**
	 * Moves the position in the chars list to the next space character
	 * @param inpPosition The current position in the array of characters
	 * @param inpChars The array of characters to search through
	 * @return The new position in the array of characters
	 */
	private static int skipUntilSpace(int inpPosition, ArrayList<Character> inpChars)
	{
		int temp=inpPosition;
		while((temp<inpChars.size())&&inpChars.get(temp)!=' ')
		{
			temp++;
		}
		return temp;
	}
	
}
