package RecommenderLogic;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


public class Init
{
	public static StandardAnalyzer analyzer;
	public static FSDirectory index;
	public static IndexReader reader;
	public static IndexSearcher searcher;
	public static int hitsPerPage;
	public static QueryParser parser;
	public static HashSet<String> stoppers;
	
	public static void InitComponents() throws IOException {
		// TODO Auto-generated constructor stub
		//Initialize Lucene Index Variables
				analyzer = new StandardAnalyzer(Version.LUCENE_41);
				index = FSDirectory.open(new File("/Users/ashutosh/Desktop/IRE/iTagBareBones/Index"));
				reader = DirectoryReader.open(index);
				searcher = new IndexSearcher(reader);
				hitsPerPage=reader.numDocs();
				parser=new QueryParser(Version.LUCENE_41, "User", analyzer);
	}
	
	public static void LoadTags() throws NumberFormatException, IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(new File("/Users/ashutosh/Desktop/IRE/iTagBareBones/tags")));
		String line;
		while( (line=br.readLine() ) !=null)
		{
			Integer count=Integer.parseInt(br.readLine());
			IndexBuilder.GlobalTags.put(line,count);
		}
		br.close();
	}
	
	public static void LoadStopWords() throws NumberFormatException, IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(new File("/Users/ashutosh/Desktop/IRE/iTagBareBones/stopwords")));
		String line;
		stoppers=new HashSet<String>();
		while( (line=br.readLine() ) !=null)
		{
			stoppers.add(line.trim());
		}
		br.close();
	}


}
