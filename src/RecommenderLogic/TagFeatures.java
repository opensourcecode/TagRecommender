package RecommenderLogic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;

public class TagFeatures 
{
	public static String feature1(String Tag,String Desc) throws Exception
	{
		String[] str=Desc.split(" ");
		int totalwords=str.length;
		int wordcount=0;
		Double res;
		for(String s :str)
		{
			if(s.compareTo(Tag)==0)
				wordcount++;
		}
		if(totalwords==0) 
		{
			return "0";
		}
		else 
		{
			res=(wordcount*1.0)/totalwords;
			if(res==0)
				return "0.000";
			else
			{
				if(0!=Double.parseDouble(String.format("%.3f",res)))
					return String.format("%.3f",res);
				else
					return "0.001";
			}
		}
	}

	public static String feature2(String Tag,String URL) throws Exception
	{
		int totalwords=0;
		int wordcount=0;
		Double res;

		while(URL.indexOf(Tag)!=-1)
		{
			wordcount++;
			URL= URL.replaceFirst(Tag, "");
		}

		String[] str=URL.split("[/.~]");

		for(String s :str)
			totalwords++;

		if(totalwords==0) 
		{
			res=0.0;
			return "0";
		}
		else 
		{
			res=(wordcount*1.0)/totalwords;
			if(res==0)
				return "0.000";
			else
			{
				if(0!=Double.parseDouble(String.format("%.3f",res)))
					return String.format("%.3f",res);
				else
					return "0.001";
			}
		}
	}

	public static String feature3(String Tag,String URL) throws Exception
	{
		Double res;
		String URLQuery="URL:"+URL;
		Query urlq = Init.parser.parse(URLQuery);

		TopScoreDocCollector collector=TopScoreDocCollector.create(Init.hitsPerPage, true);
		Init.searcher.search(urlq, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		HashSet<String> alltags=new HashSet<String>();

		for(int i=0;i<hits.length;++i)
		{
			int docId = hits[i].doc;
			Document d = Init.searcher.doc(docId);
			String t[]=d.get("Tags").split(",");//Ignore t[0]

			for(int j=1;j<t.length;j++)
			{
				t[j]=t[j].trim();
				if(t[j].length()!=0)
					alltags.add(t[j]);
			}
		}

		int freq=0;
		for ( String tag : alltags )
		{
			if(tag.compareTo(Tag)==0)freq++;
		}

		if(alltags.size()==0) 
			return "0";
		else
		{
			res=( (double) freq )/alltags.size();
			if(res==0)
				return "0.000";
			else
			{
				if(0!=Double.parseDouble(String.format("%.3f",res)))
					return String.format("%.3f",res);
				else
					return "0.001";
			}
		}
	}

	public static String feature4(String Tag,String User) throws Exception
	{
		Double res;
		String UserQuery="User:"+User;
		Query userq = Init.parser.parse(UserQuery);

		TopScoreDocCollector collector=TopScoreDocCollector.create(Init.hitsPerPage, true);
		Init.searcher.search(userq, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		HashSet<String> alltags=new HashSet<String>();

		for(int i=0;i<hits.length;++i)
		{
			int docId = hits[i].doc;
			Document d = Init.searcher.doc(docId);
			String t[]=d.get("Tags").split(",");//Ignore t[0]

			for(int j=1;j<t.length;j++)
			{
				t[j]=t[j].trim();
				if(t[j].length()!=0)
					alltags.add(t[j]);
			}
		}

		int freq=0;
		for ( String tag : alltags )
		{
			if(tag.compareTo(Tag)==0)freq++;
		}

		if(alltags.size()==0) 
			return "0";
		else 
		{
			res= ( (double) freq )/alltags.size();
			if(res==0)
				return "0.000";
			else
			{
				if(0!=Double.parseDouble(String.format("%.3f",res)))
					return String.format("%.3f",res);
				else
					return "0.001";
			}
		}
	}

	public static String feature5(String Tag) throws Exception
	{
		int freq=0;
		if(IndexBuilder.GlobalTags.containsKey(Tag)) freq=IndexBuilder.GlobalTags.get(Tag);
		Double res;
		if(IndexBuilder.GlobalTags.size()==0) 
			return "0";
		else 
		{
			res= ( (double) freq )/IndexBuilder.GlobalTags.size();
			if(res==0)
				return "0.000";
			else
			{
				if(0!=Double.parseDouble(String.format("%.3f",res)))
					return String.format("%.3f",res);
				else
					return "0.001";
			}
		}
	}

	public static LinkedHashMap<String,Double>[] getTags( String User,String URL,String Name) 
	{
		LinkedHashMap<String,Double> userTagSet = new LinkedHashMap<String,Double>();             //LinkedHashMap to maintain insertion order
		LinkedHashMap<String,Double> communityTagSet = new LinkedHashMap<String,Double>();

		ArrayList<simpleTag>uTags=new ArrayList<simpleTag>();
		ArrayList<simpleTag>cTags=new ArrayList<simpleTag>();

		// 1. Build Query
		String userQuery="User:"+User+" AND (URL:http*"+URL+"* OR "+"Name:"+Name+"*)";
		//String communityQuery="URL:http*"+URL+"* OR "+"Name:"+Name+"*";

		//System.out.println(userQuery);
		//System.out.println(communityQuery);

		// 2. Parse Query
		long start=System.currentTimeMillis();
		try
		{

			Query q1 = Init.parser.parse(userQuery);
			//Query q2 = Init.parser.parse(communityQuery);
			BooleanQuery q2 = new BooleanQuery();


			{
				String URLQuery="URL:http*"+URL+"*";
				String DescQuery="Name:"+Name+"*";
				
				Query URLQ=Init.parser.parse(URLQuery);
				Query DescQ=Init.parser.parse(DescQuery);
				
				URLQ.setBoost(12);
				DescQ.setBoost(1);
				
				q2.add(URLQ, Occur.SHOULD); // or Occur.SHOULD if this clause is optional
				q2.add(DescQ, Occur.SHOULD); // or Occur.MUST if this clause is required*/
			}

			// 3. Search user tags
			TopScoreDocCollector collector = TopScoreDocCollector.create(Init.hitsPerPage, true);
			Init.searcher.search(q1, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			// 4. add user tags
			//System.out.println("Found " + hits.length + " user hits.");//TODO DELETE
			for(int i=0;i<hits.length;++i)
			{
				int docId = hits[i].doc;
				Document d = Init.searcher.doc(docId);
				//System.out.println((i + 1) + ". " + d.get("User") + "\t" + d.get("URL") + "\t" + d.get("Name"));
				String t[]=d.get("Tags").split(",");//Ignore t[0]

				for(int j=1;j<t.length;j++)
				{
					t[j]=t[j].trim();
					if(t[j].length()!=0)
						uTags.add(new simpleTag(t[j],(double) hits[i].score));
					//userTagSet.put(t[j],(double) hits[i].score);
				}
			}

			// 5. finally populate the User tagset
			for(simpleTag x:uTags)
			{
				if(userTagSet.containsKey(x.name))
					userTagSet.put(x.name, userTagSet.get(x.name)+(Double)x.score*1.5);
				else
					userTagSet.put(x.name, x.score*1.5);
			}

			// 6. search community tags
			collector = TopScoreDocCollector.create(Init.hitsPerPage, true);
			Init.searcher.search(q2, collector);
			hits = collector.topDocs().scoreDocs;

			// 7. add community tags
			//System.out.println("Found " + hits.length + " community hits.");//TODO DELETE
			for(int i=0;i<hits.length;++i)
			{
				int docId = hits[i].doc;
				Document d = Init.searcher.doc(docId);
				//System.out.println((i + 1) + ". " + d.get("User") + "\t" + d.get("URL") + "\t" + d.get("Name")+"-->"+d.get("Tags"));
				String t[]=d.get("Tags").split(",");//ignore t[0]
				for(int j=1;j<t.length;j++)
				{
					t[j]=t[j].trim();
					if(t[j].length()!=0)
						cTags.add(new simpleTag(t[j], (double) hits[i].score));
					//communityTagSet.put(t[j],(double)hits[i].score);
				}

			}

			// 8. finally populate the Community tagset
			for(simpleTag x:cTags)
			{
				if(communityTagSet.containsKey(x.name))
					communityTagSet.put(x.name, communityTagSet.get(x.name)+(Double)x.score);
				else
					communityTagSet.put(x.name, x.score);
			}
		}
		catch(Exception e)
		{
			userTagSet.clear();
			communityTagSet.clear();
		}
		LinkedHashMap<String,Double> tagSets[] = new LinkedHashMap[2];
		tagSets[0]=userTagSet;
		tagSets[1]=communityTagSet;

		return tagSets;
	}

	static LinkedHashMap<String,Double> sortByVal(LinkedHashMap<String,Double> unsortMap) {

		LinkedList list = new LinkedList(unsortMap.entrySet());

		// sort list based on comparator
		Collections.sort(list, new Comparator() 
		{
			public int compare(Object o1, Object o2) 
			{
				return ((Comparable) ((Map.Entry) (o2)).getValue())
						.compareTo(((Map.Entry) (o1)).getValue());
			}
		});

		// put sorted list into map again
		//LinkedHashMap make sure order in which keys were inserted
		LinkedHashMap sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}


}
