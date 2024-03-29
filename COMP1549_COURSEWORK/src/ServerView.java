

// import necessary libraries
import java.awt.EventQueue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;


// public class created
public class ServerView {

    private static final long serialVersionUID = 1L;			
    private static Map<String, Socket> allUsersList = new ConcurrentHashMap<>();
    private static Set<String> activeUserSet = new HashSet<>();
    private static int port = 6001;
    private static String coordinator = null;
    private JFrame frame;
    private ServerSocket serverSocket;
    private JTextArea serverMessageBoard;
    private JList allUserNameList;
    private JList activeClientList;
    private DefaultListModel<String> activeDlm = new DefaultListModel<>();			// defaultlistmodel for storing active users and all username
    private DefaultListModel<String> allDlm = new DefaultListModel<>();

    /*Main method to make GUI visible*/
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ServerView window = new ServerView();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();			// print stack tree on exception
                }
            }
        });
    }
    
    /*Constructor ServerView*/
    public ServerView() {
        initialize();			// initialize GUI components
        try {
            serverSocket = new ServerSocket(port);		// create server socket 
            serverMessageBoard.append("Server started on port: " + port + "\n");
            serverMessageBoard.append("Waiting for the clients...\n");
            new ClientAccept().start();				// start client accepting thread
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Thread for acceptping client connections
    class ClientAccept extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();			// accepts client connection
                    String uName = new DataInputStream(clientSocket.getInputStream()).readUTF();		// read username from client
                    DataOutputStream cOutStream = new DataOutputStream(clientSocket.getOutputStream());		// create output stream from client
                    if (activeUserSet != null && activeUserSet.contains(uName)) {
                        cOutStream.writeUTF("Username already taken");
                    } else {
                        allUsersList.put(uName, clientSocket);		// add user to all users list and active users set
                        activeUserSet.add(uName);
                        cOutStream.writeUTF("");
                        activeDlm.addElement(uName);
                        if (!allDlm.contains(uName))			// add user to active users list
                            allDlm.addElement(uName);
                        activeClientList.setModel(activeDlm);
                        allUserNameList.setModel(allDlm);
                        serverMessageBoard.append("Client " + uName + " Connected...\n");			// print message
                        new MsgRead(clientSocket, uName).start();		// start prepating client list thread
                        new PrepareCLientList().start();
                        if (coordinator == null) {			// if there is no coordintar then set 
                            coordinator = uName;
                            informCoordinator(uName);
                        } else {
                            informNewMemberAboutCoordinator(uName);		//inform  new member about coordinator
                        }
                    }
                } catch (IOException ioex) {
                    ioex.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    // this method will create the coordinator
    private void informCoordinator(String member) {
        try {
            new DataOutputStream(allUsersList.get(member).getOutputStream())
                    .writeUTF("You are the coordinator.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // this method will inform new client about cooridinator
    private void informNewMemberAboutCoordinator(String newMember) {
        try {
            new DataOutputStream(allUsersList.get(newMember).getOutputStream())
                    .writeUTF("Current coordinator : " + coordinator);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    // this method will make the any active user as coordinator if there is no coordinator
    private void updateCoordinator(String leavingMember) {
        if (leavingMember.equals(coordinator)) {
            Iterator<String> itr = activeUserSet.iterator();
            if (itr.hasNext()) {
                coordinator = itr.next();
                informCoordinator(coordinator);
            } else {
                coordinator = null; // No active members left
            }
        }
    }

    // This method will send member details to requester
    private void sendMemberDetails(String requesterId, String targetUser) {
        try {
            if (allUsersList.containsKey(targetUser)) {
                Socket targetSocket = allUsersList.get(targetUser);
                String userDetails = "Details of " + targetUser + ":\n" 			// prints the user details on request
                      + "Username: " + targetUser + "\n" + "IP Address: " +targetSocket.getInetAddress().getHostAddress() + "\n" +
                      "Port ID: 6001";;
                new DataOutputStream(allUsersList.get(requesterId).getOutputStream()).writeUTF(userDetails);
            } else {
                new DataOutputStream(allUsersList.get(requesterId).getOutputStream()).writeUTF("User " + targetUser + " not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();		// print exception
        }
    }
    
    
    // created the thread for reading messages from client
    class MsgRead extends Thread {
        Socket s;			// declared socket for coommunication
        String Id;
        private MsgRead(Socket s, String uname) {
            this.s = s;
            this.Id = uname;
        }

        @Override
        public void run() {				
            while (allUserNameList != null && !allUsersList.isEmpty()) {		// will iterate while there are active users
                try {
                    String message = new DataInputStream(s.getInputStream()).readUTF();		// read a message from the client input stream
                    System.out.println("message read ==> " + message);
                    String[] msgList = message.split(":");			// split the message by colon
                    serverMessageBoard.append("< " + Id + " >" + message + "\n");
                    if (message.startsWith("requestDetails:")) {
                        String[] parts = message.split(":", 2);				// split the messsage to extract the target username
                        if (parts.length > 1) {
                            String targetUser = parts[1];
                            sendMemberDetails(Id, targetUser);
                        }
                    }
                    if (msgList[0].equalsIgnoreCase("multicast")) {		// handle multicast messages
                        String[] sendToList = msgList[1].split(",");
                        for (String usr : sendToList) {						// split the client list and send the message to each client
                            try {
                                if (activeUserSet.contains(usr)) {
                                    new DataOutputStream(((Socket) allUsersList.get(usr)).getOutputStream())
                                            .writeUTF("Sent by " + Id + " :" + msgList[2]);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (msgList[0].equalsIgnoreCase("broadcast")) {  // handle broad cast messages
                        Iterator<String> itr1 = allUsersList.keySet().iterator();
                        while (itr1.hasNext()) {
                            String usrName = (String) itr1.next();
                            if (!usrName.equalsIgnoreCase(Id)) {
                                try {
                                    if (activeUserSet.contains(usrName)) {
                                        new DataOutputStream(((Socket) allUsersList.get(usrName)).getOutputStream())
                                                .writeUTF("Sent by " + Id + " :" + msgList[1]);
                                    } else {
                                        new DataOutputStream(s.getOutputStream())
                                                .writeUTF("Message couldn't be delivered to user " + usrName + " because it is disconnected.\n");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else if (msgList[0].equalsIgnoreCase("exit")) {			// disconnection of client
                        activeUserSet.remove(Id);
                        serverMessageBoard.append(Id + " disconnected....\n");			// this will remove the client from the active user list
                        new PrepareCLientList().start();			// start a new thread to update the client list
                        updateCoordinator(Id);						// updates the coordinator if necessary
                        Iterator<String> itr = activeUserSet.iterator();
                        while (itr.hasNext()) {					// inform other client about disconnectino of a client
                            String usrName2 = (String) itr.next();
                            if (!usrName2.equalsIgnoreCase(Id)) {
                                try {
                                    new DataOutputStream(((Socket) allUsersList.get(usrName2)).getOutputStream())
                                            .writeUTF(Id + " disconnected...");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                new PrepareCLientList().start();
                            }
                        }
                        activeDlm.removeElement(Id);
                        activeClientList.setModel(activeDlm);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
    // thread for preparing and upadating the client list
    class PrepareCLientList extends Thread {
        @Override
        public void run() {
            try {
                String ids = "";		// initialize a string to store user IDs
                Iterator itr = activeUserSet.iterator();			// iterate over the active user set to collect user IDs
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    ids += key + ",";
                }
                if (ids.length() != 0) {			// this will remove last comma from the ID string
                    ids = ids.substring(0, ids.length() - 1);
                }
                itr = activeUserSet.iterator();				// iterate over the active users set again to send the updated user list to clients
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    try {
                        new DataOutputStream(((Socket) allUsersList.get(key)).getOutputStream())
                                .writeUTF(":;.,/=" + ids);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    // method to initalize the server View
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 796, 530);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setTitle("Server View");

        serverMessageBoard = new JTextArea();
        serverMessageBoard.setEditable(false);
        serverMessageBoard.setBounds(12, 29, 489, 435);
        frame.getContentPane().add(serverMessageBoard);
        serverMessageBoard.setText("Starting the Server...\n");

        allUserNameList = new JList();
        allUserNameList.setBounds(526, 324, 218, 140);
        frame.getContentPane().add(allUserNameList);
        activeClientList = new JList();
        activeClientList.setBounds(526, 78, 218, 156);
        frame.getContentPane().add(activeClientList);

        JLabel lblNewLabel = new JLabel("All Usernames");
        lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
        lblNewLabel.setBounds(530, 295, 127, 16);
        frame.getContentPane().add(lblNewLabel);

        JLabel lblNewLabel_1 = new JLabel("Active Client");
        lblNewLabel_1.setBounds(526, 53, 98, 23);
        frame.getContentPane().add(lblNewLabel_1);
    }
}



