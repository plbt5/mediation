/**
 * 
 */
package nl.tue.siop.layer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Paul Brandt <paul@brandt.name>
 *
 */
public class Utilities {

	static String readFile(String path, Charset encoding) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded, encoding);
			}
	
	static String readFile(File path, Charset encoding) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(path.toPath());
			  return new String(encoded, encoding);
			}
}
