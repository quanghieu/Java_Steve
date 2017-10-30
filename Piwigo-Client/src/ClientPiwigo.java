import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import javax.swing.JFrame;

public class ClientPiwigo {

	private static final String url = "jdbc:mysql://localhost";

	private static final String user = "root";

	private static final String password = "123";
	
	static Connection con;
	
	private static void connectDb() {
		try {
			con = DriverManager.getConnection(url, user, password);
			System.out.println("Success");
			String query1 = "use piwigo_client;";
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
	
	private Socket s;

	public ClientPiwigo(String host, int port, File file, String attrs, String key) {
		try {
			s = new Socket(host, port);
			// Upload request
			sendRequest(1);
//			int guid = uploadClient(file);
			// sendFile(file);
//			sendMetadata(guid,attrs,key);
			System.out.println("Call");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static String execCmd(String cmd) throws java.io.IOException {
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

	private void reKey(String fileName, File reKeyToken, File nKey, String attrs) throws IOException{
		InputStream is = s.getInputStream();
		OutputStream os = s.getOutputStream();
		sendRequest(2);
		while(is.read() == 0);
		os.write(1);
		PrintWriter pw = new PrintWriter(s.getOutputStream(),true);
		pw.println(fileName);
		int epoch = 0;
		while((epoch = is.read()) != 0);
		
	}
	private void sendMetadata(int guid, String attrs, String keyFile) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		// input
		System.out.println("GUID is "+guid);
		String outMetadata = "/home/hieu/Downloads/piwigo_client/metadata.cpabe";
//		String keyFile = "/home/hieu/Downloads/piwigo_client/keyfile1.txt";
		System.out.println("./metadata_encrypt.sh "+keyFile+" "+guid+" "+attrs);
		Process p = Runtime.getRuntime().exec("./metadata_encrypt.sh "+keyFile+" "+guid+" "+attrs);
		p.waitFor();
		System.out.println(execCmd("./metadata_encrypt.sh "+keyFile+" "+guid+" "+attrs));
		File f = new File(outMetadata);
		byte[] buffer = new byte[(int) f.length()];
		OutputStream os = s.getOutputStream();
		System.out.println("Send metadata");
		FileInputStream fis = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(fis);
		bis.read(buffer, 0, buffer.length);
		os.write(1);
		os.write(buffer, 0, buffer.length);
		os.flush();
	}

	private int uploadClient(String file) throws IOException {
		// TODO Auto-generated method stub
		InputStream dis = s.getInputStream();
		OutputStream os = s.getOutputStream();
		while(dis.available() == 0){
			
		}
		int ack = dis.read();
		if (ack == 0) { // start upload
			System.out.println("Acknowledgement");
			// send filename first
			sendFile(new File(file));
			// send ecrypted file
		}
		
		// wait a guid response
//		InputStream guidis = s.getInputStream();
		while(dis.read() != 0){
			
		}
		int guid = dis.read();
		// Encrypt metadata
		return guid;
	}

	private void sendRequest(int i) throws IOException {
		// TODO Auto-generated method stub
		DataOutputStream dos = new DataOutputStream(s.getOutputStream());
		dos.write(i);
	}

	public void sendFile(File f) throws IOException {
		OutputStream os = s.getOutputStream();
		DataOutputStream dos = new DataOutputStream(s.getOutputStream());
		InputStream is = s.getInputStream();
		PrintWriter pw = new PrintWriter(s.getOutputStream(),true);
		pw.println(f.getName());
		System.out.println("Loop enter");
		while(is.available() == 0){
			
		}
		byte[] buffer = new byte[(int) f.length()];
		System.out.println("Break free");
		FileInputStream fis = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(fis);
		bis.read(buffer, 0, buffer.length);
		os.write(buffer, 0, buffer.length);
		os.flush();
		
		System.out.println("Finish write: "+f.length()+" bytes");
//		fis.close();
//		dos.close();
	}

	public static void main(String[] args) {
//		Client fc = new Client("localhost", 1988, "/home/hieu/Downloads/photo.jpg");
//		connectDb();
		MyFlowLayout myUI=new MyFlowLayout("Demo Sieve Client");
		myUI.setSize(600, 100);
		myUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myUI.setLocationRelativeTo(null);
		myUI.setVisible(true);
	}

}