package WebUI;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/ImageServlet")
public class ImageServlet extends HttpServlet 
{
	private static final long serialVersionUID = -8084138612164586175L;

	public ImageServlet() 
	{
		super();
	}

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String relativePath = trimToEmpty(request.getPathInfo());

    	if(isXSSAttack(relativePath) == false) 
    	{
    		String pathToFile = this.getServletContext().getRealPath(request.getPathInfo());
    		File file = new File(pathToFile);

    		System.out.println("Looking for file " + file.getAbsolutePath());

    		if(!file.exists() || !file.isFile()) {
    			httpError(404, response);
    		} else {
    			try {
    				streamImageFile(file, response);
    			} catch(Exception e) {
    				// Tell the user there was some internal server error.\
    				// 500 - Internal server error.
    				httpError(500, response);
    				e.printStackTrace();
    			}
    		}
    	} else {
    		// what to do if i think it is a XSS attack ?!?
    	}
    }

    private void streamImageFile(File file, HttpServletResponse response) {
    	// find the right MIME type and set it as content type
    	response.setContentType(getContentType(file));
    	BufferedInputStream bis = null;
    	BufferedOutputStream bos = null;
    	try {
    		response.setContentLength((int) file.length());

    		// Use Buffered Stream for reading/writing.
    		bis = new BufferedInputStream(new FileInputStream(file));
    		bos = new BufferedOutputStream(response.getOutputStream());

    		byte[] buff = new byte[(int) file.length()];
    		int bytesRead;

    		// Simple read/write loop.
    		while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
    			bos.write(buff, 0, bytesRead);
    		}
    	} catch (Exception e) {

    		throw new RuntimeException(e);
    	} finally {
    		if (bis != null) {
    			try {
    				bis.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    				// To late to do anything about it now, we may have already sent some data to user.
    			}
    		}
    		if (bos != null) {
    			try {
    				bos.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    				// To late to do anything about it now, we may have already sent some data to user.
    			}
    		}
    	} 
    }

    private String getContentType(File file) {
    	if(file.getName().length() > 0) {
    		String[] parts = file.getName().split("\\.");
    		if(parts.length > 0) {
    			// only last part interests me
    			String extention = parts[parts.length - 1];
    			if(extention.equalsIgnoreCase("jpg")) {
    				return "image/jpg";
    			} else if(extention.equalsIgnoreCase("gif")) {
    				return "image/gif";	
    			} else if(extention.equalsIgnoreCase("png")) {
    				return "image/png";
    			}
    		}
    	}
    	throw new RuntimeException("Can not find content type for the file " +  file.getAbsolutePath());
    }

    private String trimToEmpty(String pathInfo) {
    	if(pathInfo == null) {
    		return "";
    	} else {
    		return pathInfo.trim();
    	}
    }

    private void httpError(int statusCode, HttpServletResponse response) {
    	try {
    		response.setStatus(statusCode);
    		response.setContentType("text/html");
    		PrintWriter writer = response.getWriter();
    		writer.append("<html><body><h1>Error Code: " + statusCode + "</h1><body></html>");
    		writer.flush();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }

    private boolean isXSSAttack(String path) {
    	boolean xss = false;
    	// Split on the bases of know file separator
    	String[] parts = path.split("/|\\\\");

    	// Now verify that no part contains anything harmful
    	for(String part : parts) {
    		// No double dots .. 
    		// No colons :
    		// No semicolons ;
    		if(part.trim().contains("..") || part.trim().contains(":") || part.trim().contains(";")) {
    			// Fire in the hole!
    			xss = true;
    			break;
    		}
    	}
    	return xss;
    }

    /**
     * @see HttpServlet#doPost(Ht/promotions/some.jpgtpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    }
}


