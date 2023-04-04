package mysql;

import java.sql.*; //Connetion, Statement, PreparedStatement
import javax.sql.*; //DataSource
import java.util.*; //List,ArrayList
import javax.naming.*; //lookup

//DAO:�����Ͻ� ����
public class BoardDAO {
	Connection con=null;
	PreparedStatement pstmt=null;
	Statement stmt=null;
	ResultSet rs=null;
	String sql="";


	//������ : �ܺο��� ��ü���� ���Ѵ�
	private BoardDAO(){}

	//��ü1���� �޸� ����
	private static BoardDAO instance=new BoardDAO();

	//static����, �޼���� ��ü���� ���� �ʰ� ����� �� �ִ�
	//Ŭ�����̸�.�޼���()
	public static BoardDAO getInstance(){ //JSP����� �޼���
		return instance;
	}
	//------------------
	//Ŀ�ؼ� ���
	//------------------
	private Connection getCon() throws Exception{
		Context ct=new InitialContext();
		DataSource ds=(DataSource)ct.lookup("java:comp/env/jdbc/mysql");
		return ds.getConnection();
	}//getCon()-end







	//------------------
	//�۾���, ��۾���	 
	//------------------
	public void insertBoard(BoardDTO dto){
		//�۳��뺸��(content.jsp)�� ��� ���� �Ҷ�  ������ ������ 

		int num=dto.getNum();
		int ref=dto.getRef();
		int re_step=dto.getRe_step();
		int re_level=dto.getRe_level();

		int number=0; //�� �׷�ó��

		try{
			con=getCon(); //Ŀ�ؼ� ���
			pstmt=con.prepareStatement("select max(num) from board"); //�ִ� �۹�ȣ ���
			rs=pstmt.executeQuery();

			if(rs.next()){ //���� �����ϸ�
				number=rs.getInt(1)+1; //1�� �ʵ� ��ȣ //�ִ�۹�ȣ + 1  ref=number 
			}else{ //ó�� ���϶�
				number=1; //ref=number

			}//else-end

			if(num != 0){//����̸�
				//��۳����ֱ� ��ġȮ��
				sql="update board set re_step=re_step+1 where ref=? and re_step>?";
				pstmt=con.prepareStatement(sql); //������ ���� ����

				pstmt.setInt(1,  ref);
				pstmt.setInt(2,  re_step);
				pstmt.executeUpdate();

				re_step=re_step+1; //***
				re_level=re_level+1; //***

			}else{ //����
				ref=number;
				re_step=0;
				re_level=0;
			}//else-end

			//insert
			sql="insert into board(writer,subject,content,pw,regdate,ref,re_step,re_level,ip)";
			sql=sql+" values(?,?,?,?,NOW(),?,?,?,?)";

			pstmt=con.prepareStatement(sql);//������ ���� ����

			pstmt.setString(1, dto.getWriter());
			pstmt.setString(2, dto.getSubject());
			pstmt.setString(3, dto.getContent());
			pstmt.setString(4, dto.getPw());
			pstmt.setInt(5, ref);
			pstmt.setInt(6, re_step);
			pstmt.setInt(7, re_level);
			pstmt.setString(8, dto.getIp());

			pstmt.executeUpdate(); //��������


		}catch(Exception ex){
			System.out.println("insertBoard()����:"+ex);
		}finally{
			try{
				if(rs!=null){rs.close();}
				if(pstmt!=null){pstmt.close();}
				if(con!=null){con.close();}

			}catch(Exception ex2){}
		}//finally-end
	}//insertBoard()-end

	//-----------------
	//�۰���(������ó��, ��ó��)
	//-----------------

	public int getCount(){
		int cnt=0;
		try{
			con=getCon(); //Ŀ�ؼ� ���
			pstmt=con.prepareStatement("select count(*) from board");
			rs=pstmt.executeQuery();

			if(rs.next()){
				cnt=rs.getInt(1);//�ʵ��ȣ
			}
		}catch(Exception ex){
			System.out.println("getCount()����:"+ex);
		}finally{
			try{
				if(rs!=null){rs.close();}
				if(pstmt!=null){pstmt.close();}
				if(con!=null){con.close();}
			}catch(Exception ex2){}
		}//finally-end

		return cnt;
	}//getCount()-end

	//------------------
	//����Ʈ
	//-------------------
	public List getList(int start, int cnt){
		List<BoardDTO> list=null;
		try{
			con=getCon(); //Ŀ�ؼ� ���
			sql="select*from board order by ref desc, re_step asc limit ?,?";
			//limit ?,?
			//limit ������ġ, ����

			pstmt=con.prepareStatement(sql);
			pstmt.setInt(1, start-1); //mysql 0����
			pstmt.setInt(2, cnt);
			rs=pstmt.executeQuery();

			while(rs.next()){
				//rs������ dto�� ��´�, dto�� list�� ��´� return list
				list=new ArrayList<BoardDTO>();
				do{
					BoardDTO dto=new BoardDTO();
					dto.setNum(rs.getInt(1)); //num
					dto.setWriter(rs.getString(2));//writer
					dto.setSubject(rs.getString("subject"));
					dto.setContent(rs.getString("content"));
					dto.setPw(rs.getString("pw"));
					dto.setRegdate(rs.getTimestamp("regdate"));//����Ͻú���
					dto.setReadcount(rs.getInt("readcount"));
					dto.setRef(rs.getInt("ref"));
					dto.setRe_step(rs.getInt("re_step"));
					dto.setRe_level(rs.getInt("re_level"));
					dto.setIp(rs.getString("ip"));

					list.add(dto); //******
				}while(rs.next());
			}//while-end
		}
		catch(Exception ex){
			System.out.println("getList()����:"+ex);
		}
		finally{
			try{
				if(rs!=null){rs.close();}
				if(pstmt!=null){pstmt.close();}
				if(con!=null){con.close();}
			}catch(Exception ex2){}
		}//finally-end
		return list;//*********
	}//getList()-end

	//--------------
	//��Ƚ�� ����, �۳��뺸��
	//--------------

	public BoardDTO getBoard(int num){
		BoardDTO dto=null;
		try{
			con=getCon();//Ŀ�ؼ� ���
			//��Ƚ�� ����
			sql="update board set readcount=readcount+1 where num="+num;
			pstmt=con.prepareStatement(sql);
			pstmt.executeUpdate();
			//��ȸ�� ���� ��

			//�۳��뺸��
			pstmt=con.prepareStatement("select*from board where num="+num);
			rs=pstmt.executeQuery();

			//rs������ dto��´�. return dto
			if(rs.next()){
				dto=new BoardDTO();

				dto.setNum(rs.getInt("num"));
				dto.setWriter(rs.getString("writer"));
				dto.setSubject(rs.getString("subject"));
				dto.setContent(rs.getString("content"));
				dto.setPw(rs.getString("pw"));
				dto.setRegdate(rs.getTimestamp("regdate"));
				dto.setReadcount(rs.getInt("readcount"));
				dto.setRef(rs.getInt("ref"));
				dto.setRe_step(rs.getInt("re_step"));
				dto.setRe_level(rs.getInt("re_level"));
				dto.setIp(rs.getString("ip"));

			}//if-end
		}catch(Exception ex){
			System.out.println("getBoard()����:"+ex);
		}finally{
			try{
				if(rs!=null){rs.close();}
				if(pstmt!=null){pstmt.close();}
				if(con!=null){con.close();}
			}catch(Exception ex2){}
		}//finally-end
		return dto;
	}//getBoard()-end
	//---------------------
	//�� ���� ��
	//---------------------
	public BoardDTO getUpdate(int num){
		BoardDTO dto=null;

		try{
			con=getCon();
			pstmt=con.prepareStatement("select * from board where num="+num);
			
			rs=pstmt.executeQuery();

			if(rs.next()){
				//rs������ dto�� ��´� ,return dto
				dto=new BoardDTO();
				dto.setNum(rs.getInt("num"));
				dto.setWriter(rs.getString("writer"));
				dto.setSubject(rs.getString("subject"));
				dto.setContent(rs.getString("content"));
				dto.setPw(rs.getString("pw"));

				dto.setRegdate(rs.getTimestamp("regdate"));
				dto.setReadcount(rs.getInt("readcount"));

				dto.setRef(rs.getInt("ref"));
				dto.setRe_step(rs.getInt("re_step"));
				dto.setRe_level(rs.getInt("re_level"));

				dto.setIp(rs.getString("ip"));

			}//if-end
		}catch(Exception ex){
			System.out.println("getUpdate()����:" +ex);

		}finally{
			try{
				if(rs!=null){rs.close();}
				if(pstmt!=null){pstmt.close();}
				if(con!=null){con.close();}
			}catch(Exception ex2){}
		}//finally -end
		return dto;
	}//getUpdate()-end

	//-------------
	//DB�� ����
	//-------------
	public int updateBoard(BoardDTO dto){
		int x=-100;
		String dbpw="";
		try{
			con=getCon();
			pstmt=con.prepareStatement("select pw from board where num=?");
			pstmt.setInt(1, dto.getNum());
			rs=pstmt.executeQuery();

			if(rs.next()){
				dbpw=rs.getString("pw");
				if(dto.getPw().equals(dbpw)){//��ȣ�� ��ġ �ϸ� �� ����
					sql="update board set writer=?, subject=?, content=? where num=?";
					pstmt=con.prepareStatement(sql); //������ ���� ����

					pstmt.setString(1, dto.getWriter());
					pstmt.setString(2, dto.getSubject());
					pstmt.setString(3, dto.getContent());
					pstmt.setInt(4, dto.getNum());

					pstmt.executeUpdate(); //���� ����

					x=1; //���������� ����
				}else{//��ȣ�� Ʋ����
					x=-1;
				}
			}//if-end
		}catch(Exception ex){
			System.out.println("updateBoard()����:"+ex);
		}finally{
			try{
				if(rs!=null){rs.close();}
				if(pstmt!=null){pstmt.close();}
				if(con!=null){con.close();}
			}catch(Exception ex2){}
		}//finally-end
		return x;
	}//updateBoard()-end
	//----------------
	//�ۻ���
	//----------------
	public int deleteArticle(int num, String pw){
		String dbpw="";
		int x=-100;
		
		try{
			con=getCon();
			pstmt=con.prepareStatement("select pw from board where num="+num);//������ ���� ����
			rs=pstmt.executeQuery();//���� ����
			
			if(rs.next()){
				dbpw=rs.getString("pw");
				if(pw.equals(dbpw)){
					//��ȣ�� ��ġ�ϸ� �ۻ���
					pstmt=con.prepareStatement("delete from board where num="+num);
					pstmt.executeUpdate();//���� ����
					x=1; //���� ����
				}else{//��ȣ�� Ʋ���� ó��
					x=-1;
				}
			}//if-end
		}catch(Exception ex){
			System.out.println("deleteArticle() ����:"+ex);
		}finally{
			try{
				if(rs!=null){rs.close();}
				if(pstmt!=null){pstmt.close();}
				if(con!=null){con.close();}
			}catch(Exception ex2){}
		}//finally-end
		return x;
	}//deleteArticle()-end
}//class-end
