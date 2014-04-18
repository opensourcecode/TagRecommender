package RecommenderLogic;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import org.jsoup.Jsoup;

import Output.OutputGenerator;
import RecommenderLogic.simpleTag;

public class Recommender 
{
	public static String dataSetLocation;
	public static File IndexFile=new File("/Users/ashutosh/Desktop/IRE/iTagBareBones/Index");
	public static File TagsFile=new File("/Users/ashutosh/Desktop/IRE/iTagBareBones/tags");
	public static boolean ran=false;
	/**
	 * Main Start of the Recommender
	 * @throws Exception 
	 */
	public static ArrayList<simpleTag> driver(String[] args) throws Exception 
	{

		dataSetLocation="/Users/ashutosh/Desktop/IRE/iTagBareBones/full.csv";

		if(!IndexFile.exists() || !TagsFile.exists())
		{
			if(!IndexBuilder.generateIndex(dataSetLocation))
			{
				System.out.println("Indexing Failed due to an exception");
				System.exit(1);
			}
		}

		//
		if(!ran)
		{
			Init.InitComponents();
			Init.LoadTags();
			Init.LoadStopWords();
			ran=true;
		}
		File t=new File("/Users/ashutosh/Desktop/IRE/iTagBareBones/Train.dat");
		if(!t.exists())
		{
			Trainer.Train();
			try 
			{
				Trainer.ProcessModel();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if(args[1].length()==0)
			args[1]="qwerty";

		Query ap=new Query(args[0],args[1],args[2]);
		Query.truncateRed();
		Query.ExecuteQuery();
		Query.ProcessRunner();
		ArrayList <simpleTag> RankedTags=Query.RankCombiner();
		if(RankedTags.size()==0)
		{
			try
			{
				System.setProperty("http.proxyHost", "proxy.iiit.ac.in");
				System.setProperty("http.proxyPort", "8080");
				String urlarg=args[1];
				if(!urlarg.contains("http://"))
					urlarg="http://"+urlarg;

				String titles=Jsoup.connect(urlarg).get().title();
				Random rand=new Random();
				if(titles!=null)
				{
					int counter=0;
					String[] out=titles.split("[ +\\-&,.|!(){}\\[\\]^\"~*?:\\\\/\\\\]+");
					for(String str :out)
					{
						if(str.trim().length()<=2)
							continue;
						if(Init.stoppers.contains(str))
							continue;
						if(counter>=15)
							break;
						counter++;
						RankedTags.add(new simpleTag(str, rand.nextDouble()));
					}
				}
			}
			catch(Exception ex)
			{
				RankedTags.clear();
			}
		}
		OutputGenerator.Output_driver(RankedTags);
		return RankedTags;
	}

}
