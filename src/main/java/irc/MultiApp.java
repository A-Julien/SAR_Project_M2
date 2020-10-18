/***
 * Irc class : simple implementation of a chat using JAVANAISE 
 * Contact: 
 *
 * Authors: 
 */

package irc;

import jvn.JvnException;
import jvn.Proxy.JvnProxy;
import jvn.Server.JvnServerImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.rmi.RemoteException;


/**
 * Quick and dirty app !
 * Allow to run several shared object
 */
public class MultiApp extends JPanel implements Serializable{
    public String text = "";
    static JTabbedPane tabbedPane  = new JTabbedPane();

    /**
     * main method
     * create a JVN object nammed IRC for representing the Chat application
     **/
    public static void main(String argv[]) {
        try {
            createAndShowGUI();
        } catch (Exception e) {
            System.out.println("IRC problem : " + e.getMessage());
        }
    }


    /**
     * IRC Constructor
     @param jo the JVN object representing the Chat
     **/
    public MultiApp() {
        super(new GridLayout(1, 1));

        Button add = new Button("+");
        TextField data=new TextField(40);
        JToolBar addt = new JToolBar();



        addt.setLayout(new GridLayout(1, 1));
        add.addActionListener(e ->
            tabbedPane.addTab(data.getText(), null, createFrame(
                    new coolClass(data.getText())
                )
            )
        );
        addt.add(data);
        addt.add(add);
        //add(addt,BorderLayout.NORTH);
        tabbedPane.addTab("irc", null, createFrame(
                new coolClass("irc")
        ));
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, addt, tabbedPane);
        //splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(50);
        add(splitPane);
        //Add content to the window.

    }

    private JPanel createFrame(coolClass s) {

        JPanel panel = new JPanel(false);

        TextArea textt=new TextArea(10,60);
        textt.setEditable(false);
        textt.setForeground(Color.red);
        panel.add(textt);
        TextField dataa=new TextField(40);
        panel.add(dataa);
        Button read_button = new Button("read");
        s.text = textt;
        s.data = dataa;
        read_button.addActionListener(new ReadListenerMultiApp(s));
        panel.add(read_button);
        Button write_button = new Button("write");
        write_button.addActionListener(new WriteListenerMultiApp(s));
        panel.add(write_button);
        panel.setSize(545,201);
        textt.setBackground(Color.black);

        return panel;
    }
    protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }


    private static void createAndShowGUI() throws RemoteException, JvnException {
        //Create and set up the window.
        JvnServerImpl js = JvnServerImpl.jvnGetServer();
        JFrame frame = new JFrame("Server id <" + js.getUid() +">");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



        frame.add(new MultiApp(), BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
}


class coolClass implements Serializable {
   public Sentence sentence;
   public TextField data;
   public TextArea text;

    public coolClass(String s){
        this.sentence = (Sentence) JvnProxy.newInstance(new SentenceImpl(), s);
    }

}


/**
 * Internal class to manage user events (read) on the CHAT application
 **/
class ReadListenerMultiApp implements ActionListener {
    coolClass irc;

    public ReadListenerMultiApp (coolClass i) {
        irc = i;
    }

    /**
     * Management of user events
     **/
    public void actionPerformed (ActionEvent e) {
        try {
            // invoke the method
            String s = irc.sentence.read();

            // display the read value
            irc.data.setText(s);
            irc.text.append(s+"\n");

        } catch (Exception err) {
            System.out.println("IRC problem : " + err.getMessage());
        }
    }
}

/**
 * Internal class to manage user events (write) on the CHAT application
 **/
class WriteListenerMultiApp implements ActionListener {
    coolClass irc;

    public WriteListenerMultiApp (coolClass i) {
        irc = i;
    }

    /**
     * Management of user events
     **/
    public void actionPerformed (ActionEvent e) {
        try {
            // get the value to be written from the buffer
            String s = irc.data.getText();

            // invoke the method
            irc.sentence.write(s);

        } catch (Exception err) {
            System.out.println("IRC problem  : " + err.getMessage());
        }
    }
}