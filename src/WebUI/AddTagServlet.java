package WebUI;

import java.io.File;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import RecommenderLogic.Init;

@WebServlet("/AddTagServlet")
public class AddTagServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;

	public AddTagServlet() 
	{
		super();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		// TODO Logic for Adding New Tags
		String input=request.getParameter("additional");
		input=input.replace("--Submit more tags?--", "");
		if(input.trim().length()==0)
		{
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/user.jsp");
			String thisUser=request.getParameter("login");
			request.setAttribute("username",thisUser);
			dispatcher.forward(request,response);
		}
		else
		{
			
			Init.reader.close();
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_41, new StandardAnalyzer(Version.LUCENE_41));
			IndexWriter w = new IndexWriter(FSDirectory.open(new File("/Users/ashutosh/Desktop/IRE/iTagBareBones/Index")), config);
			String URL=request.getParameter("url");
			String Desc=request.getParameter("desc");
			String UserName=request.getParameter("login");
			
			if(!URL.contains("http://"))
				URL="http://"+URL;
			Document doc = new Document();
			input=","+input;

			Field f1 = new StringField("User",UserName,Field.Store.YES);
			doc.add(f1);
			Field f2 = new StringField("URL",URL,Field.Store.YES);
			doc.add(f2);

			Field f3 = new TextField("Name",Desc, Field.Store.YES);
			doc.add(f3);
			Field f4 = new StringField("Tags",input,Field.Store.YES);
			doc.add(f4);
			w.addDocument(doc);
			w.close();
			
			Init.reader = DirectoryReader.open(Init.index);
			Init.searcher = new IndexSearcher(Init.reader);
			System.out.println(input);
			
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/user.jsp");
			String thisUser=request.getParameter("login");
			request.setAttribute("username",thisUser);
			dispatcher.forward(request,response);
			
		}

	}

}
