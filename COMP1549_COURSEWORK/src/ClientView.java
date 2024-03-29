

// import of several libraries
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import javax.swing.*;

// inherited the JFrame in ClientView Class
public class ClientView extends JFrame {

    private static final long serialVersionUID = 1L;	// Text Field for client
    private JFrame frame;			// Jframe for client view
    private JTextField clientTypingBoard;		// text field for client to type messages
    private JList clientActiveUsersList;		// list to display active users
    private JTextArea clientMessageBoard;		// text area to display messages
    private JButton clientDisconnectBtn;		//  button to disconnect from server
    private JRadioButton PrivateMsgBtn;		// radio button for sending private message
    private JRadioButton AllUserMsgBtn;		// radio button for sending message to all users
    DataInputStream inputStream;		// input stream
    DataOutputStream outStream;			// output stream
    DefaultListModel<String> dm;		// defaultlistmodel for active users
    String id, clientIds = "";			// Strings for client ID and selcected Cleint ID

    public ClientView() {			// created a constructor for ClientView class
        initialize();			// initialize method
    }
    
    /* this method calls the constructor*/
    public ClientView(String id, Socket s) {
        initialize();
        this.id = id;					// ID assign to client
        try {
            frame.setTitle("Client View - " + id);
            dm = new DefaultListModel<String>();			// initialize defaultlistmodel for active users
            clientActiveUsersList.setModel(dm);				// set model for active users list
            inputStream = new DataInputStream(s.getInputStream());
            outStream = new DataOutputStream(s.getOutputStream());
            new Read().start();				// started a new thread to read messages from server
        } catch (Exception ex) {
            ex.printStackTrace();				// printing stack tree if an exception occurs
        }
    }

    /* This Read class extending thread for reading messages from server*/
    class Read extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    String m = inputStream.readUTF();			// read messages from input stream
                    System.out.println("inside read thread : " + m);
                    if (m.contains(":;.,/=")) {				// check if message conttians special delimiter
                        m = m.substring(6);					// remove the delimiter 
                        dm.clear();
                        StringTokenizer st = new StringTokenizer(m, ",");			// Tokenzie the messages
                        while (st.hasMoreTokens()) {		// loop through tokens
                            String u = st.nextToken();		//get next token
                            if (!id.equals(u))
                                dm.addElement(u);
                        }
                    } else {
                        clientMessageBoard.append("" + m + "\n");			// append messagees to message board
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    /*This method generates teh unique ID Randomly and returns it*/
    private String generateUniqueID() {
        int randomNum = (int) ((Math.random() * (999999 - 100000)) + 100000);
        return String.valueOf(randomNum);
    }

    /*Initialize method to intitalize GUI components*/
    private void initialize() {
        frame = new JFrame();				// created a new JFrame
        frame.setBounds(150, 90, 926, 720);			// set bounds of JFrame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);		// set layout to  null
        frame.setTitle("Client View");

        // add text area for client messages
        clientMessageBoard = new JTextArea();
        clientMessageBoard.setEditable(false);
        clientMessageBoard.setBounds(12, 29, 530, 495);
        frame.getContentPane().add(clientMessageBoard);
        
        // add text field for client 
        clientTypingBoard = new JTextField();
        clientTypingBoard.setHorizontalAlignment(SwingConstants.LEFT);
        clientTypingBoard.setBounds(12, 533, 530, 84);
        frame.getContentPane().add(clientTypingBoard);
        clientTypingBoard.setColumns(10);

        // added button for sending messages
        JButton clientSendMsgBtn = new JButton("Send");
        clientSendMsgBtn.addActionListener(new ActionListener() {			// use of action listner for send button
            public void actionPerformed(ActionEvent e) {			// get message from text field
                String textAreaMessage = clientTypingBoard.getText();
                if (textAreaMessage != null && !textAreaMessage.isEmpty()) {	// check if message is not empty
                    try {
                        String messageToBeSentToServer = "";
                        String cast = "broadcast";
                        int flag = 0;
                        if (PrivateMsgBtn.isSelected()) {		// check if private message radio button is selected or not
                            cast = "multicast";
                            List<String> clientList = clientActiveUsersList.getSelectedValuesList();
                            if (clientList.size() == 0)
                                flag = 1;
                            for (String selectedUsr : clientList) {			// loop through selected users
                                if (clientIds.isEmpty())
                                    clientIds += selectedUsr;
                                else
                                    clientIds += "," + selectedUsr;
                            }
                            messageToBeSentToServer = cast + ":" + clientIds + ":" + textAreaMessage;			// constructed message for multicast
                        } else {
                            messageToBeSentToServer = cast + ":" + textAreaMessage;		// constructed message for broadcast
                        }	
                        if (cast.equalsIgnoreCase("multicast")) {
                            if (flag == 1) {			// chekc if no user found
                                JOptionPane.showMessageDialog(frame, "No user selected");
                            } else {
                                outStream.writeUTF(messageToBeSentToServer);		// send message to server
                                clientTypingBoard.setText("");				// clear the text field when messge is sent
                                clientMessageBoard.append("Sent to " + clientIds + ": " + textAreaMessage + "\n");
                            }
                        } else {
                            outStream.writeUTF(messageToBeSentToServer);
                            clientTypingBoard.setText("");
                            clientMessageBoard.append("Send All :" + textAreaMessage + "\n");
                        }
                        clientIds = "";				// clear ClientIDs
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "User does not exist anymore.");
                    }
                }
            }
        });
        clientSendMsgBtn.setFont(new Font("Tahoma", Font.PLAIN, 20));
        clientSendMsgBtn.setBounds(554, 533, 137, 84);
        clientSendMsgBtn.setForeground(Color.WHITE);
        clientSendMsgBtn.setBackground(Color.BLACK);
        frame.getContentPane().add(clientSendMsgBtn);

        // add list for displaying active users
        clientActiveUsersList = new JList();
        clientActiveUsersList.setToolTipText("Active Users");
        clientActiveUsersList.setBounds(554, 63, 327, 457);
        frame.getContentPane().add(clientActiveUsersList);

        clientDisconnectBtn = new JButton("Leave");				// button for leaving the server
        clientDisconnectBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    outStream.writeUTF("exit");
                    clientMessageBoard.append("You are disconnected now.\n");
                    frame.dispose();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        clientDisconnectBtn.setFont(new Font("Tahoma", Font.PLAIN, 20));
        clientDisconnectBtn.setBounds(703, 533, 193, 84);
        clientDisconnectBtn.setForeground(Color.WHITE);
        clientDisconnectBtn.setBackground(Color.BLACK);
        frame.getContentPane().add(clientDisconnectBtn);
        
        // added label for active users
        JLabel lblNewLabel = new JLabel("Active Clients");
        lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
        lblNewLabel.setBounds(559, 43, 95, 16);
        frame.getContentPane().add(lblNewLabel);
        
        
        // add button for requisting details of selected users
        JButton btnRequestDetails = new JButton("Request Details");
        btnRequestDetails.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {		// action listner method for request details
                String selectedUser = clientActiveUsersList.getSelectedValue().toString();
                if (selectedUser != null && !selectedUser.isEmpty()) {		// check if selected use is not empey
                    try {
                        outStream.writeUTF("requestDetails:" + selectedUser);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select a user from the active users list.");// for error message
                }
            }
        });
        btnRequestDetails.setBounds(300, 650, 150, 25);
        frame.getContentPane().add(btnRequestDetails);

        
        /* Created the button and added them to frame*/
        PrivateMsgBtn = new JRadioButton("Private");		
        PrivateMsgBtn.addActionListener(new ActionListener() {		// actionlsitner for private message button
        	public void actionPerformed(ActionEvent e) {
                clientActiveUsersList.setEnabled(true);		// enable active users list
            }
        });
        PrivateMsgBtn.setSelected(true);
        PrivateMsgBtn.setFont(new Font("Tahoma", Font.PLAIN, 15));
        PrivateMsgBtn.setBounds(682, 24, 72, 25);
        frame.getContentPane().add(PrivateMsgBtn);	// set private message buttton

        AllUserMsgBtn = new JRadioButton("All");
        AllUserMsgBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {		// actionlistner for all users message buutton
                clientActiveUsersList.setEnabled(false);		
            }
        });
        AllUserMsgBtn.setFont(new Font("Tahoma", Font.PLAIN, 15));
        AllUserMsgBtn.setBounds(774, 24, 107, 25);
        frame.getContentPane().add(AllUserMsgBtn);

        ButtonGroup btngrp = new ButtonGroup();
        btngrp.add(PrivateMsgBtn);
        btngrp.add(AllUserMsgBtn);

        JLabel lblUniqueId = new JLabel("Unique ID: " + generateUniqueID());
        lblUniqueId.setBounds(12, 12, 200, 16);
        frame.getContentPane().add(lblUniqueId);

        frame.setVisible(true);
    }
}



