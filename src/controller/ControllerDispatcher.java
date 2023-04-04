package controller;
import javax.servlet.*;
import javax.servlet.http.*;
import com.oreilly.servlet.*;
import com.oreilly.servlet.multipart.*;
import java.io.*;
import java.util.*;

import command.CommandAction;

//컨트롤러(서블릿+java문법)
public class ControllerDispatcher extends HttpServlet{
	private Map map=new HashMap();
	//init():초기화 작업
	public void init(ServletConfig config) throws ServletException{
		String path=config.getServletContext().getRealPath("/");//실제 경로 얻기
		
		//WEB_INF/command.properties
		String pros=path+config.getInitParameter("proFile");
		Properties pp=new Properties();
		FileInputStream ff=null;
		
		try{
			ff=new FileInputStream(pros);
			pp.load(ff);
		}catch(Exception ex){
			System.out.println("파일 읽기 에러:"+ex);
		}
		
		Iterator keyIter=pp.keySet().iterator();
		while(keyIter.hasNext()){
			String key=(String)keyIter.next();
			String className=pp.getProperty(key);
			
			//	    (key)					(vlaue)=className
			// /board/wirteForm.do=action.board.WriteFormAction
			
			try{
				Class commandClass=Class.forName(className);//클래스를 만든다
				Object commandObject=commandClass.newInstance();//클랙스 객체 생성
				map.put(key, commandObject);
				
			}catch(Exception ex){
				System.out.println("properties파일 내용을 클래스로 만들던 중 예외발생"+ex);
			}
		}
	}//init()-end
	//웹 브라우저 요청시. get,post
	public void doGet(HttpServletRequest request,HttpServletResponse response) throws IOException,ServletException{
		reqPro(request,response);//메서드 호출
	}//doGet()-end
	
	public void doPost(HttpServletRequest request,HttpServletResponse response) throws IOException,ServletException{
		reqPro(request,response);//메서드 호출
	}//doPost()-end
	
	//사용자 정의 메서드
	private void reqPro(HttpServletRequest request,HttpServletResponse response) 
			throws IOException,ServletException{
		String view="";
		CommandAction commandAction=null;//상위 클래스 변수로 하위 객체 처리
		
		try{
			String uri=request.getRequestURI();//   /context패스=프로젝트 이름
			//요청URI:/02_jsp/ch04_innerObject/03_request.jsp
			//ContextPath:/02_jsp
			
			if(uri.indexOf(request.getContextPath())==0){
				uri=uri.substring(request.getContextPath().length());
			}//if-end
			
			//commandAction=(CommandAction)map.get(key);
			commandAction=(CommandAction)map.get(uri);
			view=commandAction.requestPro(request, response);// /board/list.jsp
			
		}catch(Throwable ex){
			throw new ServletException(ex);
		}
		request.setAttribute("CONTENT", view);
		
		RequestDispatcher rd=request.getRequestDispatcher("/template/template.jsp");
		rd.forward(request, response);//JSP로 포워딩
	}//reqPro()-end
}//class-end
