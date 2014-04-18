package RecommenderLogic;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.lucene.queryparser.classic.ParseException;

public class Trainer 
{
	public static void Train() throws IOException, ParseException
	{
		ArrayList<String> urls = new ArrayList<String>();
		BufferedReader bufr=new BufferedReader(new FileReader(new File("urls.txt")));
		String urx;
		while( (urx=bufr.readLine()) !=null)
			urls.add(urx);
		bufr.close();

		String[] aLine; //0-> UserName; 1->URL; 2->Descriptiom; 3->Tags

		LinkedHashMap<String,Double> x[];
		LinkedHashMap<String,Double> FinalTags;
		Double PTW=(Double) 0.7,CTW=(Double) 0.3;

		BufferedReader datasetReader=new BufferedReader(new FileReader(new File(Recommender.dataSetLocation)));
		BufferedWriter trainer=new BufferedWriter(new FileWriter(new File("Train.dat")));
		String dataLine;//TODO MAY BE WE CAN IMPROVE PERFORMANCE BY HAVING A DS THAT HAS ALL LINES?

		//Get number of lines in DataSet
		LineNumberReader  lnr = new LineNumberReader(new FileReader(new File(Recommender.dataSetLocation)));
		lnr.skip(Long.MAX_VALUE); 
		
		long processedlines=0;
		System.out.println("Training...");

		while( (dataLine=datasetReader.readLine()) !=null)
		{
			
			if(processedlines % 100 == 0) 
				System.out.println(" "+processedlines);
			
			FinalTags = new LinkedHashMap<String,Double>();

			aLine = dataLine.split("\t+");
			if(aLine.length<4)
				continue;

			String User=aLine[0];
			String origurl=aLine[1];
			String mURL=aLine[1].replaceAll("http://","").replaceAll("[+\\-&|!(){}\\[\\]^\"~*?:\\\\/\\\\]+", "\\\\$0");
			String URL=aLine[1].replaceAll("[+\\-&|!(){}\\[\\]^\"~*?:\\\\/\\\\]+", "\\\\$0");
			String Desc=aLine[2].replaceAll("[+\\-&|!(){}\\[\\]^\"~*?:\\\\/\\\\]+", "\\\\$0");

			User=User.toLowerCase();
			URL=URL.toLowerCase();
			Desc=Desc.toLowerCase();
			mURL=mURL.toLowerCase();

			x=TagFeatures.getTags(User,mURL,Desc);       //x[0]=user tags, x[1]=community

			int numres=0;
			for (String p:x[1].keySet())
			{

				if(p.trim().length()==0) continue;

				if(x[0].containsKey(p))
					FinalTags.put(p, (Double) (PTW*x[0].get(p)+CTW*x[1].get(p)));
				else
					FinalTags.put(p,(Double) (CTW*x[1].get(p)));

			}
			FinalTags=TagFeatures.sortByVal(FinalTags);

			StringBuilder writeToFile=new StringBuilder();
			/*int cnt=0;
                        for(String m : FinalTags.keySet())
                                {cnt++;System.out.println("Tag:"+cnt+" "+m);}*/

			for(String x1:FinalTags.keySet())
			{
				if (numres<5)
				{
					numres++;
					//System.out.print("Tag :"+x1+" ");//TODO DELETE THIS
					writeToFile.setLength(0);
					//Append target
					if(x[0].containsKey(x1))
						writeToFile.append("1 ");
					else
						writeToFile.append("0 ");
					try
					{
						//Append the URL id
						writeToFile.append("qid:"+urls.indexOf(origurl)+" ");

						//Append each of the features
						writeToFile.append("1"+":"+TagFeatures.feature1(x1, Desc)+" ");
						writeToFile.append("2"+":"+TagFeatures.feature2(x1, URL)+" ");
						writeToFile.append("3"+":"+TagFeatures.feature3(x1, URL)+" ");
						writeToFile.append("4"+":"+TagFeatures.feature4(x1, User)+" ");
						writeToFile.append("5"+":"+TagFeatures.feature5(x1)+" ");
						writeToFile.append(" #"+":"+x1+" ");

						trainer.write(writeToFile.toString()+"\n");
					}
					catch(Exception ex)
					{
					}
				}
				else
					break;
			}
			//long end=System.currentTimeMillis();
			//System.out.println("Time:"+(end-start)+"ms");
			processedlines++;
		}
		trainer.close();
		datasetReader.close();
		lnr.close();
	}

	public static void ProcessModel() throws IOException, InterruptedException
	{
		String[] cmd = { "./svm_learn", "-z","p","Train.dat","Model.dat" };
		Process px = Runtime.getRuntime().exec(cmd);
		px.waitFor();
	}
}
