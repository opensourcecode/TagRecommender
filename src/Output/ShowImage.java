package Output;

import java.awt.Graphics;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ShowImage extends Panel {

	private static final long serialVersionUID = 1L;
	public static Integer IMGHT;
	BufferedImage  image;
	public ShowImage() 
	{
		try {

			File input = new File(OutputGenerator.fname);
			image = ImageIO.read(input);
			if(image.getWidth()>image.getHeight())
				IMGHT=image.getHeight();
			else
				IMGHT=image.getWidth();
			
		} 
		catch (IOException ie) 
		{
			System.out.println("Error:"+ie.getMessage());
		}
	}

	public void paint(Graphics g) 
	{
		g.drawImage( image, 0, 0, null);
	}
}
