package action.member;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import member.MemberDAO;
import command.CommandAction;

public class DeleteProAction implements CommandAction{

	
	public String requestPro(HttpServletRequest request,
			HttpServletResponse response) throws Throwable {
		
		//deleteForm.jsp���� ���� ������ �ޱ� 
				String id=request.getParameter("id");
				String pw=request.getParameter("pw");
				
				MemberDAO dao=MemberDAO.getInstance();//dao ��ü ���
				int x =dao.deleteMember(id, pw);//dao�޼��� ȣ��
				
				//jsp���� ����� �Ӽ� ���� 
				request.setAttribute("x", x);
				//x=1;//����
				//x=-1;//��ȣƲ��
		
				return "/member/deletePro.jsp";
	}//requestPro()-end

}//class-end
