package WebUI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import RecommenderLogic.Init;

@WebServlet(description = "Checks Login", urlPatterns = { "/Login" })
public class LoginServlet extends HttpServlet 
{
	private static final long serialVersionUID = 18912L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	
	private static HashSet<String> UIDS=new HashSet<String>();
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Init.InitComponents();
		Init.LoadTags();
		Init.LoadStopWords();
		
		
		
		resp.setHeader("Cache-Control","private, no-store, no-cache, must-revalidate, post-check=0, pre-check=0"); 
		String thisUser=req.getParameter("login");
		//resp.getWriter().print(req.getContextPath());
		BufferedReader buf = new BufferedReader(new FileReader(new File("/Users/ashutosh/Desktop/IRE/iTagBareBones/users.txt")));
		String line;
		while ( (line=buf.readLine()) !=null) UIDS.add(line);
		buf.close();
		//resp.getWriter().print(UIDS.size());

		if(UIDS.contains(thisUser))
		{
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/user.jsp");
			req.setAttribute("username",thisUser);

			dispatcher.forward(req,resp);

			//Valid User. Proceed to next screen and store the username
			//resp.getWriter().print("Valid");
		}
		else if(thisUser.compareTo("guest")==0)
		{
			//Invalid user. Redirect back to this screen
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/guest.jsp");
			req.setAttribute("username",thisUser);
			req.setAttribute("message","Invalid User Name!");
			dispatcher.forward(req,resp);
		}
		else
		{
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
			req.setAttribute("username",thisUser);
			dispatcher.forward(req,resp);
		}	 

	}
}



