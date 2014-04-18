package WebUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Output.OutputGenerator;
import RecommenderLogic.Init;
import RecommenderLogic.simpleTag;

/**
 * Servlet implementation class MainServlet
 */
@WebServlet(description = "Main Servlet", urlPatterns = { "/MainServlet" })
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.setHeader("Cache-Control","private, no-store, no-cache, must-revalidate, post-check=0, pre-check=0"); 
		String user=request.getParameter("login");
		System.out.println(user);
		String url=request.getParameter("url");
		String desc=request.getParameter("desc");
		//response.getWriter().println(user);
		//response.getWriter().println(url);
		//response.getWriter().println(desc.trim());
		//IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_41, new StandardAnalyzer(Version.LUCENE_41));

		if(user==null)
			user="guest";
		String args[] = new String[3];
		args[0]=user;
		args[1]=url;
		String strp=desc.toLowerCase();
		
		StringBuilder stb=new StringBuilder();
		
		if(strp.trim().length()!=0)
		{
			String[] str=strp.split(" ");
			for(String s :str)
			{
				if(Init.stoppers.contains(s))
					continue;
				else
					stb.append(s+" ");
			}
		}
		args[2]=stb.toString().trim();
		//args[2]=DescTextArea.getText();
		if(args[2].length()==0)
			args[2]="@";
		
		
		//args[2]=desc;
		//if(args[2].trim().length()==0)
		//	args[2]="@";
		ArrayList <simpleTag> Tags=new ArrayList<simpleTag>();
		try {
			Tags = RecommenderLogic.Recommender.driver(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for(simpleTag x:Tags)
			System.out.println(x.name+"->"+x.score);
		OutputGenerator.Output_driver(Tags);
		File f=new File(OutputGenerator.fname);
		//System.out.println(OutputGenerator.fname);
		String uname=(String) request.getAttribute("login");
		System.out.println(args[0]);
		request.setAttribute("fname", OutputGenerator.fname);
		request.setAttribute("uname", args[0]);
		request.setAttribute("url", args[1]);
		request.setAttribute("desc", args[2].replaceAll("@",""));
		if(args[0].compareTo("guest")==0)
		{
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/result_guest.jsp");
			dispatcher.forward(request,response);
		}
		else
		{
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/result.jsp");
			dispatcher.forward(request,response);
		}
	}

}
