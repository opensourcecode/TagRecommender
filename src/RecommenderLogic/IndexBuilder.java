package RecommenderLogic;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class IndexBuilder
{
	public static HashMap<String,Integer> GlobalTags=new HashMap<String,Integer>();
	public static boolean generateIndex(String dataset)
	{
		try
		{
			StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
			Directory index = FSDirectory.open(new File( "Index" ));

			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_41, analyzer);
			config.setRAMBufferSizeMB(256.0);
			config.setOpenMode(OpenMode.CREATE);
			IndexWriter w = new IndexWriter(index, config);

			String fieldnames[]={"User","URL","Name","Tags"};

			String[] row;
			BufferedReader br=new BufferedReader(new FileReader(new File(dataset)));

			File tagfile=new File("tags");
			BufferedWriter bw=new BufferedWriter(new FileWriter(tagfile));

			System.out.println("Preprocessing the files..Please Wait ...");
			JOptionPane.showMessageDialog (null,"Generating Index.. This may take some time .." );
			String line;

			while ((line = br.readLine()) != null) 
			{
				line=line.toLowerCase();
				Document doc = new Document();
				row=line.split("\t+");

				if(row.length>=4)
				{
					row[3]=","+row[3];
					for( int i=0 ; i<2 ; i++)
					{
						Field f = new StringField(fieldnames[i], row[i], Field.Store.YES);
						doc.add(f);
					}
					Field f = new TextField(fieldnames[2], row[2], Field.Store.YES);
					doc.add(f);

					Field f1 = new StringField(fieldnames[3],row[3],Field.Store.YES);
					doc.add(f1);
					w.addDocument(doc);

					//*** if file tags doesnt exist :
					String s[]=row[3].split(",");
					for(int i=1;i<s.length;i++)
					{
						s[i]=s[i].trim();
						if(s[i].length()!=0)
						{
							if(GlobalTags.containsKey(s[i]))
							{
								int count=GlobalTags.get(s[i]);
								count++;
								GlobalTags.put(s[i],count);
							}
							else
								GlobalTags.put(s[i],1);
						}
					}
				}
			}

			for(String tag : GlobalTags.keySet())
			{
				bw.write(tag+"\n");
				bw.write(GlobalTags.get(tag)+"\n");
			}
			GlobalTags.clear();
			bw.close();

			br.close();
			w.close();
			System.out.println("Indexing complete.\n");
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}


}
