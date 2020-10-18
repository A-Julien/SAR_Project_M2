/***
 * Irc class : simple implementation of a chat using JAVANAISE 
 * Contact: 
 *
 * Authors: 
 */

package irc;

import jvn.Proxy.JvnProxy;
import org.apache.logging.log4j.core.Appender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.Serializable;


public class MultiApp extends JPanel implements Serializable{
    public Label data;
    public String text = "";
    Sentence sentence;

    /**
     * main method
     * create a JVN object nammed IRC for representing the Chat application
     **/
    public static void main(String argv[]) {
        try {
            Sentence jo0 = (Sentence) JvnProxy.newInstance(new SentenceImpl(), "IRC0");

            // create the graphical part of the Chat application
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    //Turn off metal's use of bold fonts
                    UIManager.put("swing.boldMetal", Boolean.FALSE);
                    createAndShowGUI(jo0);
                }
            });
        } catch (Exception e) {
            System.out.println("IRC problem : " + e.getMessage());
        }
    }

    public void stressIrc(Sentence jo){
        this.sentence.write("test");
        jo.read();
    }

    /**
     * IRC Constructor
     @param jo the JVN object representing the Chat
     **/
    public MultiApp(Sentence jo) {
        super(new GridLayout(1, 1));

        sentence = jo;

        /*frame=new Frame();
        frame.setLayout(new GridLayout(1,1));
        text=new TextArea(10,60);
        text.setEditable(false);
        text.setForeground(Color.red);
        frame.add(text);
        data=new TextField(40);
        frame.add(data);
        Button read_button = new Button("read");
        read_button.addActionListener(new ReadListenerMultiApp(this));
        frame.add(read_button);
        Button write_button = new Button("write");
        write_button.addActionListener(new WriteListenerMultiApp(this));
        frame.add(write_button);
        frame.setSize(545,201);
        text.setBackground(Color.black);
        frame.setVisible(true);*/

        JTabbedPane tabbedPane = new JTabbedPane();

        JComponent panel1 = makeTextPanel("Panel #1");
        tabbedPane.addTab("Tab 1", null, panel1,"Does nothing");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);
        add(tabbedPane);
    }

    private Frame createFrame(){
        Sentence jo0 = (Sentence) JvnProxy.newInstance(new SentenceImpl(), "IRC0");
        Frame frame=new Frame();
        frame.setLayout(new GridLayout(1,1));
        TextArea text=new TextArea(10,60);
        text.setEditable(false);
        text.setForeground(Color.red);
        frame.add(text);
        TextField data=new TextField(40);
        frame.add(data);
        Button read_button = new Button("read");
        read_button.addActionListener(new ReadListenerMultiApp(this));
        frame.add(read_button);
        Button write_button = new Button("write");
        write_button.addActionListener(new WriteListenerMultiApp(this));
        frame.add(write_button);
        frame.setSize(545,201);
        text.setBackground(Color.black);
        frame.setVisible(true);
        return frame;
    }
    protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }


    private static void createAndShowGUI(Sentence ss) {
        //Create and set up the window.
        JFrame frame = new JFrame("TabbedPaneDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new MultiApp(ss), BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
}



/**
 * Internal class to manage user events (read) on the CHAT application
 **/
class ReadListenerMultiApp implements ActionListener {
    MultiApp irc;

    public ReadListenerMultiApp (MultiApp i) {
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
           // irc.text.append(s+"\n");

        } catch (Exception err) {
            System.out.println("IRC problem : " + err.getMessage());
        }
    }
}

/**
 * Internal class to manage user events (write) on the CHAT application
 **/
class WriteListenerMultiApp implements ActionListener {
    MultiApp irc;

    public WriteListenerMultiApp (MultiApp i) {
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