import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server extends Thread {

	private ServerSocket ss;

	private static final String url = "jdbc:mysql://localhost";

	private static final String user = "root";

	private static final String password = "123";

	Connection con;

	int GUID = 0;

	public Server(int port) {
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		connectDb();
		while (true) {
			try {
				Socket clientSock = ss.accept();
				int req = requestCategorization(clientSock);
				if (req == 1) {
					UploadServer(clientSock);
				} else if (req == 2) {
					reKeyServer(clientSock);
				} else if (req == 3) {
					ShareServer(clientSock);
				}
				// saveFile(clientSock);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void reKeyServer(Socket clientSock) throws IOException, SQLException {
		// TODO Auto-generated method stub
		OutputStream os = clientSock.getOutputStream();
		InputStream is = clientSock.getInputStream();
		os.write(1);
		while(is.read() == 0);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String reqName = br.readLine();
		String epochCmd = "select EPOCH from piwigo_table where Filename ='"+reqName+"'";
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(epochCmd);
		int epoch = 0;
		int guid;
		while(rs.next()){
			epoch = rs.getInt("EPOCH");
			guid = rs.getInt("ID");
		}
		os.write(epoch);
	}

	private void ShareServer(Socket clientSock) throws IOException, SQLException {
		// TODO Auto-generated method stub
		OutputStream os = clientSock.getOutputStream();
		InputStream is = clientSock.getInputStream();
		String fileName = null;
		os.write(1);
		System.out.println("Enter share server");
		while (is.read() == 0)
			;
		System.out.println("Break frees");
		// return metadata
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		fileName = br.readLine();
		System.out.println("File " + fileName + " is requested");
		String sqlComm = "select ID, Storage, Metadata from piwigo_encrypt where Filename ='" + fileName + "'";
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sqlComm);
		String metadata = null;
		int guid = 0;
		String fileStorage = null;
		while (rs.next()) {
			guid = rs.getInt("ID");
			fileStorage = rs.getString("Storage");
			metadata = rs.getString("Metadata");
		}
		sendFile(clientSock, metadata);
		System.out.println("Finish send metadata");
		int reqGUID;
		while ((reqGUID = is.read()) == 0)
			;
		System.out.println("request GUID: " + reqGUID);
		if (reqGUID != guid) {
			System.out.println("Wrong GUID");
			return;
		}
		os.write(1);
		sendFile(clientSock, fileStorage);
		System.out.println("Finish send ");
	}

	private void sendFile(Socket sk, String file) throws IOException {
		// TODO Auto-generated method stub
		File f = new File(file);
		OutputStream os = sk.getOutputStream();
		byte[] buffer = new byte[(int) f.length()];

		FileInputStream fis = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(fis);
		bis.read(buffer, 0, buffer.length);
		System.out.println("Buffer "+buffer);
		os.write(buffer, 0, buffer.length);
		os.flush();
	}

	private void connectDb() {
		try {
			con = DriverManager.getConnection(url, user, password);
			System.out.println("Success");
			String query1 = "use piwigo;";
			// String query2 = "create table piwigo_encrypt("
			// + "ID int,"
			// + "Filename varchar(255),"
			// + "Storage varchar(255),"
			// + "Metadata varchar(255),"
			// + "Epoch int )";
			Statement stmt = con.createStatement();
			stmt.executeQuery(query1);
		} catch (Exception e) {
			System.out.println("Fail");
			e.printStackTrace();
		}
	}

	private void UploadServer(Socket Sock) throws IOException, SQLException {
		// TODO Auto-generated method stub
		InputStream is = Sock.getInputStream();
		OutputStream os = Sock.getOutputStream();
		String fileName = null;

		os.write(0);
		while (is.available() == 0) {

		}
		System.out.println("Quit waiting");
		saveFile(Sock, fileName);
		// received and store metadata
		while (is.read() == 0)
			;
		saveMetadata(Sock);
	}

	private void saveMetadata(Socket Sock) throws IOException, SQLException {
		// TODO Auto-generated method stub
		String path = "/var/www/html/piwigo/upload/metadata/";
		Date now = new Date();
		Timestamp cur = new Timestamp(now.getTime());
		InputStream is = Sock.getInputStream();
		byte[] buffer = new byte[602386];
		String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String filename = path + "" + timestamp + ".cpabe";
		FileOutputStream fos = new FileOutputStream(filename);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		int byteRead = is.read(buffer, 0, buffer.length);
		int current = byteRead;

		/*
		 * do { byteRead = is.read(buffer, 0, buffer.length - current);
		 * if(byteRead >= 0) current+=byteRead; } while(byteRead > -1);
		 */

		bos.write(buffer, 0, current);
		bos.flush();
		bos.close();

		// Update database
		String query = "Update piwigo_encrypt set Metadata ='" + filename + "' where ID = " + GUID + "";
		Statement stmt = con.createStatement();
		stmt.executeUpdate(query);
	}

	private int requestCategorization(Socket clientSock) throws IOException {
		// TODO Auto-generated method stub
		DataInputStream dis = new DataInputStream(clientSock.getInputStream());
		int request = dis.read();
		// request 1: Upload file
		// request 2: Re-encryption
		// request 3: 3rd party request
		return request;
	}

	private void saveFile(Socket clientSock, String name) throws IOException, SQLException {

		InputStream is = clientSock.getInputStream();
		OutputStream os = clientSock.getOutputStream();
		String path = "/var/www/html/piwigo/upload/buffer/";
		Date now = new Date();
		Timestamp cur = new Timestamp(now.getTime());
		String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String loc = path + "" + timestamp + ".jpg";
		FileOutputStream fos = new FileOutputStream(loc);
		StringBuilder sb = new StringBuilder();
		String line;

		while (is.available() == 0) {

		}
		System.out.println("quit wait");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		// name = br.readLine();

		// sb.append(br.readLine());

		// name = sb.toString();
		name = br.readLine();
		System.out.println("filename:" + name);
		// insert filename to mysql
		String insQuery = "insert into piwigo_encrypt (Filename, Storage, Metadata, Epoch) " + "values ('" + name
				+ "', '" + loc + "', '" + null + "', '" + 0 + "');";
		String getUID = "select ID from piwigo_encrypt order by ID desc limit 1";
		Statement stmt = con.createStatement();
		stmt.executeUpdate(insQuery);
		ResultSet rs = stmt.executeQuery(getUID);

		while (rs.next()) {
			GUID = rs.getInt(1);
		}
		System.out.println("Current row is " + GUID);
		os.write(0);

		byte[] buffer = new byte[602386];

		// int filesize = 15123; // Send file size in separate msg

		BufferedOutputStream bos = new BufferedOutputStream(fos);
		int byteRead = is.read(buffer, 0, buffer.length);
		int current = byteRead;

		/*
		 * do { byteRead = is.read(buffer, 0, buffer.length - current);
		 * if(byteRead >= 0) current+=byteRead; } while(byteRead > -1);
		 */

		bos.write(buffer, 0, current);
		bos.flush();
		// fos.close();
		os.write(GUID);
		System.out.println("Received" + current + "bytes");
	}

	public static void main(String[] args) {
		Server fs = new Server(1988);
		fs.start();
	}

}