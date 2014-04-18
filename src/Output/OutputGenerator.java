package Output;

import RecommenderLogic.simpleTag;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class OutputGenerator
{
	public static String fname;

	public static class Word
	{
		private String fontFamily=null;
		private Double weight;
		private String text="";
		private Shape shape;
		private Rectangle2D bounds;
		private Color fill=null;
		private Color stroke=null;
		private float lineHeight=1.0f;
		private String title=null;
		private String url=null;


		public Word(String text,Double weight)
		{
			this.text=text;
			this.weight=weight;
			if(this.weight<=0) throw new IllegalArgumentException("bad weight "+weight);
		}

		public String getText()
		{
			return text;
		}

		public Double getWeight()
		{
			return weight;
		}

		public void setFontFamily(String fontFamily)
		{
			this.fontFamily = fontFamily;
		}

		public String getFontFamily()
		{
			return fontFamily;
		}

		public void setFill(Color fill)
		{
			this.fill = fill;
		}

		public Color getFill()
		{
			return fill;
		}

		public void setStroke(Color stroke)
		{
			this.stroke = stroke;
		}

		public Color getStroke()
		{
			return stroke;
		}

		public float getLineHeight()
		{
			return lineHeight;
		}

		public void setLineHeight(float lineHeight)
		{
			this.lineHeight = lineHeight;
		}

		public void setTitle(String title)
		{
			this.title = title;
		}

		public String getTitle()
		{
			return title;
		}

		public void setUrl(String url)
		{
			this.url = url;
		}

		public String getUrl()
		{
			return url;
		}

	}
	private int biggestSize=72;
	private int smallestSize=15	;
	private List<Word> words=new ArrayList<Word>();
	private String fontFamily="Serif";
	private Random rand=new Random();
	private Rectangle2D imageSize=null;
	private Color fill=generateRandomColor(new Color(0,0,255));
	//private Color stroke=generateRandomColor(new Color(0,0,0));
	private double dRadius=10.0;
	private int dDeg=10;
	private boolean useArea=false;
	private int doSortType=-1;
	private Integer outputWidth=null;
	private boolean allowRotate=false;

	public OutputGenerator()
	{
	}
	
	public Color generateRandomColor(Color mix) {
	    Random random = new Random();
	    int red = random.nextInt(256);
	    int green = random.nextInt(256);
	    int blue = random.nextInt(256);

	    // mix the color
	    if (mix != null) {
	        red = (red + mix.getRed()) / 2;
	        green = (green + mix.getGreen()) / 2;
	        blue = (blue + mix.getBlue()) / 2;
	    }

	    Color color = new Color(red, green, blue);
	    return color;
	}


	public void doLayout()
	{
		this.imageSize=new Rectangle2D.Double(0, 0, 0, 0);
		if(this.words.isEmpty()) return;
		/** sort from biggest to lowest */

		switch(doSortType)
		{
		case 1:
		{
			Collections.sort(this.words,new Comparator<Word>()
					{
				@Override
				public int compare(Word w1, Word w2)
				{
					return (w2.getWeight()-w1.getWeight())>0?1:-1;
				}
					});
			break;
		}
		case 2:
		{
			Collections.sort(this.words,new Comparator<Word>()
					{
				@Override
				public int compare(Word w1, Word w2)
				{
					return (w1.getWeight()-w2.getWeight())>0?1:-1;
				}
					});
			break;
		}
		case 3:
		{
			Collections.sort(this.words,new Comparator<Word>()
					{
				@Override
				public int compare(Word w1, Word w2)
				{
					return w1.getText().compareToIgnoreCase(w2.getText());
				}
					});
			break;
		}
		default:
		{
			Collections.shuffle(this.words,this.rand);
			break;
		}
		}
		Word first=this.words.get(0);
		double high = -Double.MAX_VALUE;
		double low = Double.MAX_VALUE;
		for(Word w:this.words)
		{
			high= Math.max(high, w.getWeight());
			low= Math.min(low, w.getWeight());
		}


		/* create small image */
		BufferedImage img=new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		/* get graphics from this image */
		Graphics2D g=Graphics2D.class.cast(img.getGraphics());
		FontRenderContext frc = g.getFontRenderContext();


		for(Word w:this.words)
		{
			String ff=w.getFontFamily();
			if(ff==null) ff=this.fontFamily;
			int fontSize=(int)(((w.getWeight()-low)/(high-low))*(this.biggestSize-this.smallestSize))+this.smallestSize;
			Font font=new Font(ff,Font.BOLD,fontSize);
			//System.err.println("fontsize:"+fontSize);
			TextLayout textLayout=new TextLayout(w.getText(), font, frc);
			Shape shape=textLayout.getOutline(null);
			if(this.allowRotate && this.rand.nextBoolean())
			{
				AffineTransform rotate=AffineTransform.getRotateInstance(Math.PI/2.0);
				shape=rotate.createTransformedShape(shape);
			}
			Rectangle2D bounds= shape.getBounds2D();
			AffineTransform centerTr=AffineTransform.getTranslateInstance(-bounds.getCenterX(),-bounds.getCenterY());
			w.shape= centerTr.createTransformedShape(shape);
			w.bounds=w.shape.getBounds2D();
		}
		g.dispose();

		//first point
		Point2D.Double center=new Point2D.Double(0,0);

		for(int i=1;i< this.words.size();++i)
		{
			Word current=this.words.get(i);

			//calculate current center
			center.x=0;
			center.y=0;
			double totalWeight=0.0;
			for(int prev=0;prev< i;++prev)
			{
				Word wPrev=this.words.get(prev);
				center.x+= (wPrev.bounds.getCenterX())*wPrev.getWeight();
				center.y+= (wPrev.bounds.getCenterY())*wPrev.getWeight();
				totalWeight+=wPrev.getWeight();
			}
			center.x/=(totalWeight);
			center.y/=(totalWeight);

			boolean done=false;
			double radius=0.5*Math.min(
					first.bounds.getWidth(),
					first.bounds.getHeight());

			while(!done)
			{
				//System.err.println(""+i+"/"+words.size()+" rad:"+radius);
				int startDeg=rand.nextInt(360);
				//loop over spiral
				int prev_x=-1;
				int prev_y=-1;
				for(int deg=startDeg;deg<startDeg+360;deg+=dDeg)
				{
					double rad=((double)deg/Math.PI)*180.0;
					int cx=(int)(center.x+radius*Math.cos(rad));
					int cy=(int)(center.y+radius*Math.sin(rad));
					if(prev_x==cx && prev_y==cy) continue;
					prev_x=cx;
					prev_y=cy;

					AffineTransform moveTo=AffineTransform.getTranslateInstance(cx,cy);
					Shape candidate=moveTo.createTransformedShape(current.shape);
					Area area1=null;
					Rectangle2D bound1=null;
					if(useArea)
					{
						area1=new Area(candidate);
					}
					else
					{
						bound1=new Rectangle2D.Double(
								current.bounds.getX()+cx,
								current.bounds.getY()+cy,
								current.bounds.getWidth(),
								current.bounds.getHeight()
								);
					}
					//any collision ?
					int prev=0;
					for(prev=0;prev< i;++prev)
					{
						if(useArea)
						{
							Area area2=new Area(this.words.get(prev).shape);
							area2.intersect(area1);
							if(!area2.isEmpty()) break;
						}
						else
						{
							if(bound1.intersects(this.words.get(prev).bounds))
							{
								break;
							}
						}
					}
					//no collision: we're done
					if(prev==i)
					{
						current.shape=candidate;
						current.bounds=candidate.getBounds2D();
						done=true;
						break;
					}
				}
				radius+=this.dRadius;
			}
		}

		double minx=Integer.MAX_VALUE;
		double miny=Integer.MAX_VALUE;
		double maxx=-Integer.MAX_VALUE;
		double maxy=-Integer.MAX_VALUE;
		for(Word w:words)
		{
			minx=Math.min(minx, w.bounds.getMinX()+1);
			miny=Math.min(miny, w.bounds.getMinY()+1);
			maxx=Math.max(maxx, w.bounds.getMaxX()+1);
			maxy=Math.max(maxy, w.bounds.getMaxY()+1);
		}
		AffineTransform shiftTr=AffineTransform.getTranslateInstance(-minx, -miny);
		for(Word w:words)
		{
			w.shape=shiftTr.createTransformedShape(w.shape);
			w.bounds=w.shape.getBounds2D();
		}
		this.imageSize=new Rectangle2D.Double(0,0,maxx-minx,maxy-miny);
	}

	public void add(Word word)
	{
		this.words.add(word);
	}

	public void saveAsPNG(File file)
			throws IOException
			{
		AffineTransform scale=new AffineTransform();
		Dimension dim=new Dimension(
				(int)this.imageSize.getWidth(),
				(int)this.imageSize.getHeight()	
				);

		if(this.outputWidth!=null)
		{
			double ratio=this.outputWidth/dim.getWidth();
			dim.width=this.outputWidth;
			dim.height=(int)(dim.getHeight()*ratio);
			if(dim.height==0)dim.height=1;
			scale=AffineTransform.getScaleInstance(ratio, ratio);
		}

		BufferedImage img=new BufferedImage(
				dim.width,
				dim.height,
				BufferedImage.TYPE_INT_ARGB
				);

		Graphics2D g=(Graphics2D)img.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setTransform(scale);
		for(Word w:this.words)
		{
			Color c1=generateRandomColor(new Color(rand.nextInt(255),rand.nextInt(255),rand.nextInt(255)));
			w.setFill(c1);
			c1=generateRandomColor(new Color(0,0,0));
			w.setStroke(c1);
			
			Color c=w.getFill();
			if(c==null) c=this.fill;
			if(c!=null)
			{
				g.setColor(c);
				g.fill(w.shape);
			}

		}

		g.dispose();
		ImageIO.write(img, "png", file);
			}

	public void setAllowRotate(boolean allowRotate)
	{
		this.allowRotate = allowRotate;
	}

	public void setBiggestSize(int biggestSize)
	{
		this.biggestSize = biggestSize;
	}

	public void setSmallestSize(int smallestSize)
	{
		this.smallestSize = smallestSize;
	}

	public void setSortType(int doSortType)
	{
		this.doSortType = doSortType;
	}

	public void setUseArea(boolean useArea)
	{
		this.useArea = useArea;
	}

	public static void Output_driver( ArrayList<simpleTag> Tags)
	{
		try
		{
			OutputGenerator app=new OutputGenerator();
			File fileOut=null;
			for( simpleTag Tag  : Tags)
			{
				app.add(new Word(Tag.name,Tag.score));
			}

			String rfname="/Users/ashutosh/Documents/IAS/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/TagRecommender/"+"tags"+Tags.hashCode()+".png";;
			fname="tags"+Tags.hashCode()+".png";
			fileOut=new File(rfname);
			app.fontFamily="Sans";
			app.outputWidth=400;
			app.allowRotate=false;
			
			app.doLayout();
			app.saveAsPNG(fileOut);
			
			JFrame frame = new JFrame("Tag Cloud");
			Panel panel = new ShowImage();
			frame.getContentPane().add(panel);
			frame.setSize(app.outputWidth, Output.ShowImage.IMGHT);
			
			frame.setVisible(true);
		}
		catch(Throwable err)
		{
			err.printStackTrace();
		}
	}
}