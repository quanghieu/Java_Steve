import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.SocketSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.swing.plaf.synth.SynthSpinnerUI;

public class ThirdParty {

	private static final String thirdParty_location = "/home/hieu/Downloads/piwigo_thirdParty/";

	private static final String key_location = thirdParty_location + "key/";

	private static final String client_host = "localhost";

	private static final String key_loc = "/var/www/html/piwigo/upload/buffer/user_key.key";

	private static final String key_hom = "/home/hieu/Downloads/piwigo_thirdParty/key.txt";

	static ServerSocket ss;

	public ThirdParty(String host, int port, String file) {
		try {
			String fileName = "photo.jpg";
			Socket s = new Socket(host, port);
			// Upload request
			sendRequest(s, 3);
			requestFilename(s, fileName);
			receiveMetadata(s);
			int guid = decryptMetadata(); // call decrypt
			receiveData(s, guid, fileName); // fetch guid and retrieve data
			// int guid = uploadClient(file);
			// sendFile(file);
			// sendMetadata(guid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void retrieveFile(Socket s, String fileName) throws IOException, InterruptedException {
		sendRequest(s, 3);
		// requestFilename(s, fileName);
		DataInputStream dis = new DataInputStream(s.getInputStream());
		while (dis.readUTF().equals("Prepare to receive")) {
			String file = dis.readUTF();
			System.out.println("Retrieving file "+file);
			receiveMetadata(s);
			int guid = decryptMetadata(); // call decrypt
			receiveData(s, guid, file);
			decryptFile(file);
			System.out.println(System.currentTimeMillis());
		}
	}

	private static void decryptFile(String fileName) {
		// TODO Auto-generated method stub
		fileName = thirdParty_location.concat(fileName);
		String resultFile = fileName.substring(0, fileName.length() - 4);
		System.out.println("Result File" + resultFile);
		// MyJniFunc.Decrypt(fileName, key_hom, resultFile);
		homo_decrypt(fileName, key_hom, resultFile);
	}

	private static void homo_decrypt(String fileName, String keyHom, String resultFile) {
		// TODO Auto-generated method stub
		Process p;
		try {
	//		 System.out.println(execCmd("./homo_decrypt.sh "
	//		 +thirdParty_location+" "+fileName+" "+keyHom+ " "+resultFile));
			p = Runtime.getRuntime().exec(
					"./homo_decrypt.sh " + thirdParty_location + " " + fileName + " " + keyHom + " " + resultFile);
			System.out.println(
					"./homo_decrypt.sh " + thirdParty_location + " " + fileName + " " + keyHom + " " + resultFile);
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void receiveData(Socket s, int guid, String fileName) throws IOException {
		// TODO Auto-generated method stub
		// Send GUID
		System.out.println("Receiving data");
		OutputStream os = s.getOutputStream();
		InputStream is = s.getInputStream();
		// os.write(1);
		os.write(guid);

		byte[] buffer = new byte[602386];
		FileOutputStream fos = new FileOutputStream(thirdParty_location + fileName);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		int byteRead = is.read(buffer, 0, buffer.length);
		int current = byteRead;

		/*
		 * do { byteRead = is.read(buffer, 0, buffer.length - current);
		 * if(byteRead >= 0) current+=byteRead; } while(byteRead > -1);
		 */

		bos.write(buffer, 0, current);
	}

	private static void copyFile(String File, String To) {
		InputStream inStream = null;
		OutputStream outStream = null;
		String[] str = File.split("/");
		String fileName = str[str.length - 1];

		try {

			File afile = new File(File);
			File bfile = new File(To + "/" + fileName);

			inStream = new FileInputStream(afile);
			outStream = new FileOutputStream(bfile);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = inStream.read(buffer)) > 0) {

				outStream.write(buffer, 0, length);

			}

			inStream.close();
			outStream.close();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public static String execCmd(String cmd) throws java.io.IOException {
		java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream())
				.useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	private static int decryptMetadata() throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		System.out.println("Decrypt metadata");
		String keyFile = thirdParty_location + "usr_privCP_Java.key";
		System.out.println("./decrypt_metadata.sh " + keyFile);
		Process p = Runtime.getRuntime().exec("./decrypt_metadata.sh " + keyFile);
		p.waitFor();
//		System.out.println(execCmd("./decrypt_metadata.sh " + keyFile));
		FileInputStream fis = new FileInputStream("/home/hieu/Downloads/piwigo_thirdParty/GUID.txt");
		BufferedReader bis = new BufferedReader(new InputStreamReader(fis));
		String str_guid = bis.readLine();
		int guid = Integer.parseInt(str_guid);
		return guid;
	}

	private static void receiveMetadata(Socket s) throws IOException {
		// TODO Auto-generated method stub
		byte[] buffer = new byte[65536];
		System.out.println("Receiving metadata");
		InputStream is = s.getInputStream();
		FileOutputStream fos = new FileOutputStream(thirdParty_location + "/Metadata.cpabe");
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		int byteRead = is.read(buffer, 0, buffer.length);
		int current = byteRead;
		System.out.println("Read " + current + " bytes of metadata");
		/*
		 * do { byteRead = is.read(buffer, 0, buffer.length - current);
		 * if(byteRead >= 0) current+=byteRead; } while(byteRead > -1);
		 */

		bos.write(buffer, 0, current);
		bos.flush();
	}

	private static void requestFilename(Socket s, String fileName) throws IOException {
		// TODO Auto-generated method stub
		PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
		pw.println(fileName);
	}

	private static void sendRequest(Socket s, int i) throws IOException {
		// TODO Auto-generated method stub
		DataOutputStream dos = new DataOutputStream(s.getOutputStream());
		dos.write(i);
	}

	public static void main(String[] args) {
		System.out.println("Sieve import daemon is running");

		try {
			ss = new ServerSocket(1993);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// ThirdParty tp = new ThirdParty("localhost", 1988,
				// "/home/hieu/Downloads/photo.jpg");
				while (true) {
					System.out.println("Press any button to retrieve file");
					Scanner scanIn = new Scanner(System.in);
					String fileName;
					fileName = scanIn.nextLine();
					System.out.println(System.currentTimeMillis());
					try {
						Socket s = new Socket("localhost", 1988);
						retrieveFile(s, fileName);
						System.out.println(System.currentTimeMillis());
					} catch (IOException | InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		t.start();

		Thread key_th = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {

					try {

						Socket s = ss.accept();
						DataInputStream dis = new DataInputStream(s.getInputStream());
						InputStream is = s.getInputStream();
						FileOutputStream fos = new FileOutputStream(thirdParty_location + "usr_privCP_Java.key");
						System.out.println("File name " + thirdParty_location + "usr_privCP_Java.key");
						long fileSize = dis.readLong();
						BufferedOutputStream bos = new BufferedOutputStream(fos);

						int bytesLeft = (int) fileSize;
						System.out.println("File size is " + bytesLeft);
						byte[] buffer = new byte[1024];
						int n = 0;
						while (bytesLeft > 0) {
							n = is.read(buffer, 0, Math.min(buffer.length, bytesLeft));
							if (n < 0) {
								throw new EOFException("Expected " + bytesLeft + " more bytes to read");
							}
							bos.write(buffer, 0, n);
							System.out.println("Writing File... ");
							bytesLeft -= n;
							System.out.println(bytesLeft + " bytes left to read");
						}
						bos.close();
						for (int i = 0; i < buffer.length; i++) {
							System.out.println(buffer[i] + " ");
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		});
		key_th.start();
	}

	private static void saveKey() {
		// TODO Auto-generated method stub
	}

	private static void keyRequest() {
		// TODO Auto-generated method stub

	}

}