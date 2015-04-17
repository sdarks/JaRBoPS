package application;
/**
 * Used for storing information about the material of an object
 * @author Sam Dark
 *
 */
public class Material 
{
	/** The name of the material */
	private String name;//Name of the material
	/** The ambient colour of the material */
	private Position ka;//Ambient Colour
	/** The diffuse colour of the material */
	private Position kd;//Diffuse Colour
	/** The specular colour of the material */
	private Position ks;//Specular Colour
	/** The specular weight of the material */
	private double ns;//Specular weight
	/** The transparency of the material */
	private double d;//Transparency
	
	/** The ambient map (image) for the material */
	private String mapKa;//Ambient map
	/** The diffuse map (image) for the material */
	private String mapKd;//Diffuse map
	/** The specular map (image) for the material */
	private String mapKs;//Specular map
	/** The alpha (transparency) map (image) for the material */
	private String mapD;//Alpha map
	
	/**
	 * Creates a blank material
	 */
	Material()
	{
		name=null;
		ka=null;
		kd=null;
		ks=null;
		ns=0;
		d=1;
		mapKa=null;
		mapKd=null;
		mapKs=null;
		mapD=null;
	}
	/**
	 * Creates a material with default values and a name
	 * @param inpName The name of the material
	 */
	Material(String inpName)
	{
		name=inpName;
		//Default values
		ka=new Position(0.4f,0.4f,0.4f);
		kd=new Position(1f,1f,1f);
		ks=new Position(1f,1f,1f);
		ns=0;
		d=1;
		mapKa=null;
		mapKd=null;
		mapKs=null;
		mapD=null;
	}
	
	/**
	 * Gets the name of the material
	 * @return The name of the material
	 */
	public String getName() 
	{
		return name;
	}
	/**
	 * Sets the name of the material
	 * @param inpName The name to set the material name to
	 */
	public void setName(String inpName) 
	{
		name = inpName;
	}
	/**
	 * Gets the ambient colour value of the material
	 * @return The ambient colour
	 */
	public Position getKa() 
	{
		return ka;
	}
	/**
	 * Sets the ambient colour value of the material
	 * @param inpKa The ambient colour to set
	 */
	public void setKa(Position inpKa) 
	{
		ka = inpKa;
	}
	/**
	 * Gets the diffuse colour of the material
	 * @return The diffuse colour
	 */
	public Position getKd() 
	{
		return kd;
	}
	/**
	 * Sets the diffuse colour of the material
	 * @param inpKd The diffuse colour to set
	 */
	public void setKd(Position inpKd) 
	{
		kd = inpKd;
	}
	/**
	 * Gets the specular colour of the material
	 * @return The specular colour
	 */
	public Position getKs() 
	{
		return ks;
	}
	/**
	 * Sets the specular colour of the material
	 * @param inpKs The specular colour to set
	 */
	public void setKs(Position inpKs) 
	{
		ks = inpKs;
	}
	/**
	 * Gets the specular weight of the material
	 * @return The specular weight
	 */
	public double getNs()
	{
		return ns;
	}
	/**
	 * Sets the specular weight of the material
	 * @param inpNs The specular weight
	 */
	public void setNs(double inpNs)
	{
		ns = inpNs;
	}
	/**
	 * Gets the alpha of the material
	 * @return The alpha
	 */
	public double getD() 
	{
		return d;
	}
	/**
	 * Sets the alpha of the material
	 * @param inpD The alpha to set
	 */
	public void setD(double inpD) 
	{
		d = inpD;
	}
	/**
	 * Gets the ambient texture map of the material
	 * @return The ambient map
	 */
	public String getMapKa() 
	{
		return mapKa;
	}
	/**
	 * Sets the ambient texture map of the material
	 * @param inpMapKa The ambient map to set
	 */
	public void setMapKa(String inpMapKa) 
	{
		mapKa = inpMapKa;
	}
	/**
	 * Gets the diffuse texture map of the material
	 * @return The diffuse map
	 */
	public String getMapKd() 
	{
		return mapKd;
	}
	/**
	 * Sets the diffuse texture map of the material
	 * @param inpMapKd The diffuse map to set
	 */
	public void setMapKd(String inpMapKd) 
	{
		mapKd = inpMapKd;
	}
	/**
	 * Gets the specular texture map of the material
	 * @return The specular colour map
	 */
	public String getMapKs() 
	{
		return mapKs;
	}
	/**
	 * Sets the specular texture map of the material
	 * @param inpMapKs The specular colour map to set
	 */
	public void setMapKs(String inpMapKs) 
	{
		mapKs = inpMapKs;
	}
	/**
	 * Gets the alpha texture map of the material
	 * @return The alpha map
	 */
	public String getMapD() 
	{
		return mapD;
	}
	/**
	 * Sets the alpha texture map of the material
	 * @param inpMapD The alpha map to set
	 */
	public void setMapD(String inpMapD) 
	{
		mapD = inpMapD;
	}
	
	
}
