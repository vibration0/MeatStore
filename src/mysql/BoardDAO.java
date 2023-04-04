package mysql;

import java.sql.*; //Connetion, Statement, PreparedStatement
import javax.sql.*; //DataSource
import java.util.*; //List,ArrayList
import javax.naming.*; //lookup

//DAO:비지니스 로직
public class BoardDAO {
	Connection con=null;
	PreparedStatement pstmt=null;
	Statement stmt=null;
	ResultSet rs=null;
	String sql="";


	//생성자 : 외부에서 객체생성 못한다
	private BoardDAO(){}

	//객체1개로 메모리 절약
	private static BoardDAO instance=new BoardDAO();

	//static변수, 메서드는 객체생성 하지 않고도 사용할 수 있다
	//클래스이름.메서드()
	public static BoardDAO getInstance(){ //JSP사용할 메서드
		return instance;
	}
	//------------------
	//커넥션 얻기
	//------------------
	private Connection getCon() throws Exception{
		Context ct=new InitialContext();
		DataSource ds=(DataSource)ct.lookup("java:comp/env/jdbc/mysql");
		return ds.getConnection();
	}//getCon()-end







	//------------------
	//글쓰기, 답글쓰기	 
	//------------------
	public void insertBoard(BoardDTO dto){
		//글내용보기(content.jsp)에 답글 쓰기 할때  보내준 데이터 

		int num=dto.getNum();
		int ref=dto.getRef();
		int re_step=dto.getRe_step();
		int re_level=dto.getRe_level();

		int number=0; //글 그룹처리

		try{
			con=getCon(); //커넥션 얻기
			pstmt=con.prepareStatement("select max(num) from board"); //최대 글번호 얻기
			rs=pstmt.executeQuery();

			if(rs.next()){ //글이 존재하면
				number=rs.getInt(1)+1; //1은 필드 번호 //최대글번호 + 1  ref=number 
			}else{ //처음 글일때
				number=1; //ref=number

			}//else-end

			if(num != 0){//답글이면
				//답글끼워넣기 위치확보
				sql="update board set re_step=re_step+1 where ref=? and re_step>?";
				pstmt=con.prepareStatement(sql); //생성시 인자 들어간다

				pstmt.setInt(1,  ref);
				pstmt.setInt(2,  re_step);
				pstmt.executeUpdate();

				re_step=re_step+1; //***
				re_level=re_level+1; //***

			}else{ //원글
				ref=number;
				re_step=0;
				re_level=0;
			}//else-end

			//insert
			sql="insert into board(writer,subject,content,pw,regdate,ref,re_step,re_level,ip)";
			sql=sql+" values(?,?,?,?,NOW(),?,?,?,?)";

			pstmt=con.prepareStatement(sql);//생성시 인자 들어간다

			pstmt.setString(1, dto.getWriter());
			pstmt.setString(2, dto.getSubject());
			pstmt.setString(3, dto.getContent());
			pstmt.setString(4, dto.getPw());
			pstmt.setInt(5, ref);
			pstmt.setInt(6, re_step);
			pstmt.setInt(7, re_level);
			pstmt.setString(8, dto.getIp());

			pstmt.executeUpdate(); //쿼리실행


		}catch(Exception ex){
			System.out.println("insertBoard()예외:"+ex);
		}finally{
			try{
				if(rs!=null){rs.close();}
				if(pstmt!=null){pstmt.close();}
				if(con!=null){con.close();}

			}catch(Exception ex2){}
		}//finally-end
	}//insertBoard()-end

	//-----------------
	//글갯수(페이지처리, 블럭처리)
	//-----------------

	public int getCount(){
		int cnt=0;
		try{
			con=getCon(); //커넥션 얻기
			pstmt=con.prepareStatement("select count(*) from board");
			rs=pstmt.executeQuery();

			if(rs.next()){
				cnt=rs.getInt(1);//필드번호
			}
		}catch(Exception ex){
			System.out.println("getCount()예외:"+ex);
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
	//리스트
	//-------------------
	public List getList(int start, int cnt){
		List<BoardDTO> list=null;
		try{
			con=getCon(); //커넥션 얻기
			sql="select*from board order by ref desc, re_step asc limit ?,?";
			//limit ?,?
			//limit 시작위치, 갯수

			pstmt=con.prepareStatement(sql);
			pstmt.setInt(1, start-1); //mysql 0부터
			pstmt.setInt(2, cnt);
			rs=pstmt.executeQuery();

			while(rs.next()){
				//rs내용을 dto에 담는다, dto를 list에 담는다 return list
				list=new ArrayList<BoardDTO>();
				do{
					BoardDTO dto=new BoardDTO();
					dto.setNum(rs.getInt(1)); //num
					dto.setWriter(rs.getString(2));//writer
					dto.setSubject(rs.getString("subject"));
					dto.setContent(rs.getString("content"));
					dto.setPw(rs.getString("pw"));
					dto.setRegdate(rs.getTimestamp("regdate"));//년월일시분초
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
			System.out.println("getList()예외:"+ex);
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
	//조횟수 증가, 글내용보기
	//--------------

	public BoardDTO getBoard(int num){
		BoardDTO dto=null;
		try{
			con=getCon();//커넥션 얻기
			//조횟수 증가
			sql="update board set readcount=readcount+1 where num="+num;
			pstmt=con.prepareStatement(sql);
			pstmt.executeUpdate();
			//조회수 증가 끝

			//글내용보기
			pstmt=con.prepareStatement("select*from board where num="+num);
			rs=pstmt.executeQuery();

			//rs내용을 dto담는다. return dto
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
			System.out.println("getBoard()예외:"+ex);
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
	//글 수정 폼
	//---------------------
	public BoardDTO getUpdate(int num){
		BoardDTO dto=null;

		try{
			con=getCon();
			pstmt=con.prepareStatement("select * from board where num="+num);
			
			rs=pstmt.executeQuery();

			if(rs.next()){
				//rs내용을 dto에 담는다 ,return dto
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
			System.out.println("getUpdate()예외:" +ex);

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
	//DB글 수정
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
				if(dto.getPw().equals(dbpw)){//암호가 일치 하면 글 수정
					sql="update board set writer=?, subject=?, content=? where num=?";
					pstmt=con.prepareStatement(sql); //생성시 인자 들어간다

					pstmt.setString(1, dto.getWriter());
					pstmt.setString(2, dto.getSubject());
					pstmt.setString(3, dto.getContent());
					pstmt.setInt(4, dto.getNum());

					pstmt.executeUpdate(); //쿼리 실행

					x=1; //정상적으로 수정
				}else{//암호가 틀리면
					x=-1;
				}
			}//if-end
		}catch(Exception ex){
			System.out.println("updateBoard()예외:"+ex);
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
	//글삭제
	//----------------
	public int deleteArticle(int num, String pw){
		String dbpw="";
		int x=-100;
		
		try{
			con=getCon();
			pstmt=con.prepareStatement("select pw from board where num="+num);//생성시 인자 들어간다
			rs=pstmt.executeQuery();//쿼리 실행
			
			if(rs.next()){
				dbpw=rs.getString("pw");
				if(pw.equals(dbpw)){
					//암호가 일치하면 글삭제
					pstmt=con.prepareStatement("delete from board where num="+num);
					pstmt.executeUpdate();//쿼리 실행
					x=1; //삭제 성공
				}else{//암호가 틀릴때 처리
					x=-1;
				}
			}//if-end
		}catch(Exception ex){
			System.out.println("deleteArticle() 예외:"+ex);
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
