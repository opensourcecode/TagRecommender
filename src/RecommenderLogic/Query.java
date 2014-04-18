package RecommenderLogic;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;


public class Query 
{
	static String User;
	static String URL;
	static String Desc;
	static String mURL;
	static LinkedHashMap<String,Double> FinalTags;
	
	public Query(String user,String url,String desc)
	{
		this.User=user;
		this.URL=url;
		this.Desc=desc;
	}
	
	public static void truncateRed()
	{
		mURL=(URL.replaceAll("http://","")).replaceAll("[+\\-&|!(){}\\[\\]^\"~*?:\\\\/\\\\]+", "\\\\$0");
		URL= URL.replaceAll("[+\\-&|!(){}\\[\\]^\"~*?:\\\\/\\\\]+", "\\\\$0");
		Desc=Desc.replaceAll("[+\\-&|!(){}\\[\\]^\"~*?:\\\\/\\\\]+", "\\\\$0");
		User=User.toLowerCase();
		URL=URL.toLowerCase();
		mURL=mURL.toLowerCase();
		Desc=Desc.toLowerCase();
	}
	
	
	public static void ExecuteQuery() throws Exception
	{
		LinkedHashMap<String,Double> x[]=TagFeatures.getTags(User,mURL,Desc); //x[0]=user tags, x[1]=community
		//TODO **** VERY IMPORTANT :: if x returns empty UserTagSet and commTagSet output "No suggestions"
		//-------Tag Scoring Process-----------------------------
		Double PTW=(Double) 0.7,CTW=(Double) 0.3;
		int numres=0;
	     FinalTags=new LinkedHashMap<String,Double>(); 
		BufferedWriter tagWriter=new BufferedWriter(new FileWriter(new File("/Users/ashutosh/Desktop/IRE/iTagBareBones/tags.dat")));
		
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

		for(String x1:FinalTags.keySet())
		{
			if (numres<50)
			{
				writeToFile.setLength(0);
				numres++;
				/*System.out.println(feature1(x1,Desc)+" "+feature2(x1,URL));
				System.out.println(feature3(x1,URL)+" "+feature4(x1,User)+" "+feature5(x1));*/
				writeToFile.append("0 ");
				writeToFile.append("qid:007 ");
				writeToFile.append("1"+":"+TagFeatures.feature1(x1, Desc)+" ");
				writeToFile.append("2"+":"+TagFeatures.feature2(x1, URL)+" ");
				writeToFile.append("3"+":"+TagFeatures.feature3(x1, URL)+" ");
				writeToFile.append("4"+":"+TagFeatures.feature4(x1, User)+" ");
				writeToFile.append("5"+":"+TagFeatures.feature5(x1)+" ");
				writeToFile.append(" #"+":"+x1+" \n");
				tagWriter.write(writeToFile.toString());
			}
			else
				break;
		}
	//	Initializers.reader.close();
		tagWriter.close();
	}
	
	public static void ProcessRunner() throws IOException, InterruptedException
	{
		String[] cmd = { "/Users/ashutosh/Desktop/IRE/iTagBareBones/svm_classify","/Users/ashutosh/Desktop/IRE/iTagBareBones/tags.dat","/Users/ashutosh/Desktop/IRE/iTagBareBones/Model.dat","rank.dat" };
		Process px = Runtime.getRuntime().exec(cmd);
		px.waitFor();
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<simpleTag> RankCombiner() throws IOException
	{
		BufferedReader resultReader=new BufferedReader(new FileReader(new File("/Users/ashutosh/Desktop/IRE/iTagBareBones/rank.dat")));
		ArrayList <simpleTag> OutputTags=new ArrayList<simpleTag>();
		
		int numres=0;
		String line;
		for(String Tag:FinalTags.keySet())
			if (numres<15)
			{
				line=resultReader.readLine();
				OutputTags.add(new simpleTag(Tag,Double.parseDouble(line)));
				numres++;
			}
		Collections.sort(OutputTags,new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1)
			{
				if( ((simpleTag) arg0).score < ((simpleTag) arg1).score) return 1;
				else if ( ((simpleTag) arg0).score > ((simpleTag) arg1).score) return -1;
				else return 0; 
			}
			
		});
		
		for(simpleTag Tag :OutputTags)
		{
			Tag.score=FinalTags.get(Tag.name);
		}
		
		resultReader.close();
		File rankfile=new File("/Users/ashutosh/Desktop/IRE/iTagBareBones/rank.dat");
		//rankfile.delete();
		File tagfile=new File("/Users/ashutosh/Desktop/IRE/iTagBareBones/tags.dat");
		//tagfile.delete();
		
		return OutputTags;
	}

}
