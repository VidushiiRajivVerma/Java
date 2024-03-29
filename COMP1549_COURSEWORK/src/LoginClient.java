
// import of several libraries
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import javax.swing.*;


// class loginclient extends the JFrame
public class LoginClient extends JFrame{


	private JFrame frame;			// declare variables
	private JTextField clientUserName;
	private int port = 6001;			// port number for sever connection

	public static void main(String[] args) { // main function which will make GUI visible
		EventQueue.invokeLater(new Runnable() {				
			public void run() {
				try {
					LoginClient window = new LoginClient();		// create an instance of loginclient
					window.frame.setVisible(true);		// make the frame visisble
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	/*Constructor for loginclient class*/
	public LoginClient() {
		initialize();			// this will initilize method to set up the GUI
	}
	
	/*Initialize the contents of the frame. */
	private void initialize() { // it will initialize the components of GUI
		frame = new JFrame();				// created the  main frame
		frame.setBounds(100, 100, 619, 342);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Client Register");			// title for main frame

		clientUserName = new JTextField();			// text field for entering uername
		clientUserName.setBounds(207, 50, 200, 50);
		frame.getContentPane().add(clientUserName);
		clientUserName.setColumns(10);

		JButton clientLoginBtn = new JButton("Join");
		clientLoginBtn.addActionListener(new ActionListener() { //action will be taken on clicking login button
			public void actionPerformed(ActionEvent e) {
				try {
					String id = clientUserName.getText();  // username entered by user
					Socket s = new Socket("localhost", port); 		// create a socket
					DataInputStream inputStream = new DataInputStream(s.getInputStream()); 		// create input and output stream
					DataOutputStream outStream = new DataOutputStream(s.getOutputStream());
					outStream.writeUTF(id); 		// send username to the output stream
					
					String msgFromServer = new DataInputStream(s.getInputStream()).readUTF(); 		// receive message on socket
					if(msgFromServer.equals("Username already taken")) {		//if server sent this message then prompt user to enter other username
						JOptionPane.showMessageDialog(frame,  "Username already taken\n"); 		// show message in other dialog box
					}else {
						new ClientView(id, s); 		// otherwise just create a new thread of Client view and close the register jframe
						frame.dispose();
					}
				}catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		
		clientLoginBtn.setFont(new Font("Tahoma", Font.PLAIN, 17));
		clientLoginBtn.setBounds(207, 139, 132, 61);
		clientLoginBtn.setForeground(Color.WHITE);
		clientLoginBtn.setBackground(new Color(0,0,0));
		frame.getContentPane().add(clientLoginBtn);

		JLabel lblNewLabel = new JLabel("Username");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 17));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(44, 50, 132, 47);
		frame.getContentPane().add(lblNewLabel);
	}

	
}