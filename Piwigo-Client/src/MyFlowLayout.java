import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MyFlowLayout extends JFrame {

	private static final long serialVersionUID = 1L;
	private File chosenFile;
	private File chosenKey = new File("/home/hieu/Downloads/piwigo_client/key_file1.txt");
	String stt = "No key selected!!!";
	public MyFlowLayout(String title)

	{

		setTitle(title);

		JPanel pnFlow = new JPanel();

		pnFlow.setLayout(new FlowLayout());


		JButton btn1 = new JButton("Choose file");
		JButton btn2 = new JButton("Choose key");

		JTextArea txtType = new JTextArea("Type");
		JTextField type = new JTextField();
		JTextArea txtLoc = new JTextArea("Location");
		JTextField location = new JTextField();
		JTextArea txtYear = new JTextArea("Year");
		JTextField year = new JTextField();
		JTextArea txtStt = new JTextArea(stt);
		type.setColumns(5);
		location.setColumns(5);
		year.setColumns(5);
		
		pnFlow.add(btn2);
		pnFlow.add(btn1);

		pnFlow.add(txtType);
		pnFlow.add(type);
		pnFlow.add(txtLoc);
		pnFlow.add(location);
		pnFlow.add(txtYear);
		pnFlow.add(year);
		pnFlow.add(txtStt);
		Container con = getContentPane();

		con.add(pnFlow);
		btn1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JFileChooser chooser = new JFileChooser();
				int choice = chooser.showOpenDialog(pnFlow);
				if(choice != JFileChooser.APPROVE_OPTION) return;
				chosenFile = chooser.getSelectedFile();
				System.out.println(chosenFile.getName());
				String attrs = "TYPE"+type.getText()+" and LOCATION"+location.getText()+" and YEAR"+year.getText()+" and EPOCH0";
				System.out.println(attrs);
				if(attrs.equals("")){
					System.out.println("No attribute is specified");
					return;
				}
//				Client fc = new Client("localhost", 1988, chosenFile.getPath(),attrs,chosenKey.getPath());
			}
		});
		
		btn2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JFileChooser chooser = new JFileChooser();
				int choice = chooser.showOpenDialog(pnFlow);
				if(choice != JFileChooser.APPROVE_OPTION) return;
				chosenKey = chooser.getSelectedFile();
				txtStt.setText("Key file is "+chosenKey.getPath());
			}
		});
	}

	public static void main(String[] args)

	{

		MyFlowLayout myUI = new MyFlowLayout("Demo Sieve Client");

		myUI.setSize(600, 100);

		myUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		myUI.setLocationRelativeTo(null);

		myUI.setVisible(true);

	}

}