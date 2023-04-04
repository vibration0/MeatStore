package board;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource; 
import com.oreilly.servlet.MultipartRequest;

// [Class BoardDAO]
public class BoardDAO {

	// �������� ����
	Connection connection = null;
	PreparedStatement pstmt = null;
	Statement stmt = null;
	ResultSet resultset = null;
	MultipartRequest multipart = null;
	String sqlparam = "";
	String dbpw = "";
	int checkparam = 0;

	// �����̺� �����ڸ� ���� �̱��� ���� ����
	private BoardDAO() {}
	
	// �̱��� ������ ���� ��ü ����
	private static BoardDAO instance = new BoardDAO();
	
	// �ݺ��Ǵ� ����ó���� ���� �޼ҵ�
	public void exceptionHandling () {
		try {
			if(resultset != null)  {resultset.close();}
			if(pstmt != null)      {pstmt.close();}
			if(connection != null) {connection.close();}
		}
		catch(Exception ex2) {}
	}

	
//------------------------------------------------------------------------------------------------//
//------------------------------------------------------------------------------------------------//
//------------------------------------------------------------------------------------------------//


	// [�ν��Ͻ� ��ȯ - getInstance]
	public static BoardDAO getInstance() { 
		return instance;
	}

	// [Ŀ�ؼ� ��ȯ - getConnection]
	private Connection getConnection() throws Exception {
		Context context = new InitialContext();
		DataSource datasource = (DataSource)context.lookup("java:comp/env/jdbc/mysql");
		return datasource.getConnection();
	}

	// [�۰��� ��ȯ - getCount]
	public int getCount() {
		int count = 0;
		try {
			connection = getConnection(); 
			pstmt = connection.prepareStatement("select count(*) from board");
			resultset = pstmt.executeQuery();
			
			if(resultset.next()) {
				count = resultset.getInt(1);
			}
		}
		catch(Exception ex) {
			System.out.println("getCount()����:"+ex);
		}
		finally {
			exceptionHandling();
		}
		return count;
	}

	// [�۸�� ��ȯ - getList]
	public List getList(int start, int count) {
		BoardDTO dto = null;
		List<BoardDTO> list = null;
		try {
			connection = getConnection(); 
			sqlparam = "select * from board order by ref desc, re_step asc limit ?, ?";

			pstmt = connection.prepareStatement(sqlparam);
			pstmt.setInt(1, start-1); 
			pstmt.setInt(2, count);
			resultset = pstmt.executeQuery();

			while(resultset.next()) {
				list = new ArrayList<BoardDTO>();
				do {
					dto = new BoardDTO();
					dto.setNum(resultset.getInt("num"));
					dto.setWriter(resultset.getString("writer"));
					dto.setSubject(resultset.getString("subject"));
					dto.setContent(resultset.getString("content"));
					dto.setPw(resultset.getString("pw"));
					dto.setRegdate(resultset.getTimestamp("regdate"));
					dto.setReadcount(resultset.getInt("readcount"));
					dto.setRef(resultset.getInt("ref"));
					dto.setRe_step(resultset.getInt("re_step"));
					dto.setRe_level(resultset.getInt("re_level"));
					dto.setIp(resultset.getString("ip"));
					dto.setFileupload(resultset.getString("fileupload"));
					list.add(dto); 
				}
				while(resultset.next());
			}
		}
		catch(Exception ex) {
			System.out.println("getList()����:"+ex);
		}
		finally {
			exceptionHandling();
		}
		return list;
	}

	// [���� ���� - getBoard]
	public BoardDTO getBoard(int num) {
		BoardDTO dto = null;
		try {
			connection = getConnection();
			sqlparam="update board set readcount = readcount+1 where num="+num;
			pstmt = connection.prepareStatement(sqlparam);
			pstmt.executeUpdate();
			pstmt = connection.prepareStatement("select * from board where num="+num);
			resultset = pstmt.executeQuery();

			if(resultset.next()) {
				dto = new BoardDTO();
				dto.setNum(resultset.getInt("num"));
				dto.setWriter(resultset.getString("writer"));
				dto.setSubject(resultset.getString("subject"));
				dto.setContent(resultset.getString("content"));
				dto.setPw(resultset.getString("pw"));
				dto.setRegdate(resultset.getTimestamp("regdate"));
				dto.setReadcount(resultset.getInt("readcount"));
				dto.setRef(resultset.getInt("ref"));
				dto.setRe_step(resultset.getInt("re_step"));
				dto.setRe_level(resultset.getInt("re_level"));
				dto.setIp(resultset.getString("ip"));
				dto.setFileupload(resultset.getString("fileupload"));
			}
		}
		catch(Exception ex) {
			System.out.println("getBoard()����:"+ex);
		}
		finally {
			exceptionHandling();
		}
		return dto;
	}

	// [�� ����ֱ� - insertBoard]
	public void insertBoard(BoardDTO dto) {
		int num = dto.getNum();
		int ref = dto.getRef();
		int re_step = dto.getRe_step();
		int re_level = dto.getRe_level();
		int number = 0; 

		try {
			connection = getConnection(); 
			pstmt = connection.prepareStatement("select max(num) from board"); 
			resultset = pstmt.executeQuery();

			if(resultset.next()) { 
				number = resultset.getInt(1)+1; 
			}
			else { 
				number = 1; 
			}
			if(num != 0) {
				sqlparam="update board set re_step = re_step+1 where ref = ? and re_step>?";
				pstmt = connection.prepareStatement(sqlparam); 
				pstmt.setInt(1, ref);
				pstmt.setInt(2, re_step);
				pstmt.executeUpdate();
				re_step = re_step+1; 
				re_level = re_level+1; 
			}
			else {
				ref = number;
				re_step = 0;
				re_level = 0;
			}
			sqlparam="insert into board(writer, subject, content, pw, regdate, ref, re_step, re_level, ip, fileupload)";
			sqlparam = sqlparam+" values(?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?)";

			pstmt = connection.prepareStatement(sqlparam);
			pstmt.setString(1, dto.getWriter());
			pstmt.setString(2, dto.getSubject());
			pstmt.setString(3, dto.getContent());
			pstmt.setString(4, dto.getPw());
			pstmt.setInt(5, ref);
			pstmt.setInt(6, re_step);
			pstmt.setInt(7, re_level);
			pstmt.setString(8, dto.getIp());
			pstmt.setString(9, dto.getFileupload());
			pstmt.executeUpdate();


		}
		catch(Exception ex) {
			System.out.println("insertBoard()����:"+ex);
		}
		finally {
			exceptionHandling();
		}
	}

	// [�� �����ϱ� - getUpdate]
	public BoardDTO getUpdate(int num) {
		BoardDTO dto = null;
		try {
			connection = getConnection();
			pstmt = connection.prepareStatement("select * from board where num="+num);
			resultset = pstmt.executeQuery();

			if(resultset.next()) {
				dto = new BoardDTO();
				dto.setNum(resultset.getInt("num"));
				dto.setWriter(resultset.getString("writer"));
				dto.setSubject(resultset.getString("subject"));
				dto.setContent(resultset.getString("content"));
				dto.setPw(resultset.getString("pw"));
				dto.setRegdate(resultset.getTimestamp("regdate"));
				dto.setReadcount(resultset.getInt("readcount"));
				dto.setRef(resultset.getInt("ref"));
				dto.setRe_step(resultset.getInt("re_step"));
				dto.setRe_level(resultset.getInt("re_level"));
				dto.setIp(resultset.getString("ip"));
				dto.setFileupload(resultset.getString("fileupload"));
			}
		}
		catch(Exception ex) {
			System.out.println("getUpdate()����:" +ex);
		}
		finally {
			exceptionHandling();
		}
		return dto;
	}

	// [������ �� DB ���� - updateBoard]
	public int updateBoard(BoardDTO dto) {
		try {
			connection = getConnection();
			pstmt = connection.prepareStatement("select pw from board where num = ?");
			pstmt.setInt(1, dto.getNum());
			resultset = pstmt.executeQuery();

			if(resultset.next()) {
				dbpw = resultset.getString("pw");

				if(dto.getPw().equals(dbpw)) {
					sqlparam="update board set writer=?, subject=?, content=?, fileupload=? where num=?";
					pstmt = connection.prepareStatement(sqlparam); 
					pstmt.setString(1, dto.getWriter());
					pstmt.setString(2, dto.getSubject());
					pstmt.setString(3, dto.getContent());
					pstmt.setString(4, dto.getFileupload());
					pstmt.setInt(5, dto.getNum());
					pstmt.executeUpdate(); 
					checkparam = 1; 
				}
				else {
					checkparam = -1;
				}
			}
		}
		catch(Exception ex) {
			System.out.println("updateBoard()����:"+ex);
		}
		finally {
			exceptionHandling();
		}
		return checkparam;
	}

	// [�� ���� - getDelete]
	public int getDelete(int num, String pw) {
		try {
			connection = getConnection();
			pstmt = connection.prepareStatement("select pw from board where num="+num);
			resultset = pstmt.executeQuery();
			
			if(resultset.next()) {
				dbpw = resultset.getString("pw");
				if(pw.equals(dbpw)) {
					pstmt = connection.prepareStatement("delete from board where num="+num);
					pstmt.executeUpdate();
					checkparam = 1; 
				}
				else {
					checkparam = -1;
				}
			}
		}
		catch(Exception ex) {
			System.out.println("getDelete() ����:"+ex);
		}
		finally {
			exceptionHandling();
		}
		return checkparam;
	}
	
}
