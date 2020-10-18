/***
 * Irc class : simple implementation of a chat using JAVANAISE 
 * Contact: 
 *
 * Authors: 
 */

package irc;

import jvn.Proxy.JvnProxy;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;


public class IrcProxy implements Serializable{
    public TextArea		text;
    public TextField	data;
    Frame 			frame;
    Sentence sentence;


    /**
     * main method
     * create a JVN object nammed IRC for representing the Chat application
     **/
    public static void main(String argv[]) {
        try {
            Sentence jo0 = (Sentence) JvnProxy.newInstance(new SentenceImpl(), "IRC0");

            // create the graphical part of the Chat application
            IrcProxy irc = new IrcProxy(jo0);
            //stressIrc(irc, jo0);

        } catch (Exception e) {
            System.out.println("IRC problem : " + e.getMessage());
        }
    }

    public void stressIrc(Sentence jo){
        this.sentence.write("test");
        jo.read();
    }

    public static void stressIrc(IrcProxy irc, Sentence jo){
        while(true) {
            String s1 = "test";
            irc.sentence.write(s1);

            String s0 = irc.sentence.read();
            irc.data.setText(s0);
            irc.text.append(s0 + "\n");
        }
    }

    /**
     * IRC Constructor
     @param jo the JVN object representing the Chat
     **/
    public IrcProxy(Sentence jo) {
        sentence = jo;
        frame=new Frame();
        frame.setLayout(new GridLayout(1,1));
        text=new TextArea(10,60);
        text.setEditable(false);
        text.setForeground(Color.red);
        frame.add(text);
        data=new TextField(40);
        frame.add(data);
        Button read_button = new Button("read");
        read_button.addActionListener(new readListenerProxy(this));
        frame.add(read_button);
        Button write_button = new Button("write");
        write_button.addActionListener(new writeListenerProxy(this));
        frame.add(write_button);
        frame.setSize(545,201);
        text.setBackground(Color.black);
        frame.setVisible(true);
    }
}


/**
 * Internal class to manage user events (read) on the CHAT application
 **/
class readListenerProxy implements ActionListener {
    IrcProxy irc;

    public readListenerProxy (IrcProxy i) {
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
class writeListenerProxy implements ActionListener {
    IrcProxy irc;

    public writeListenerProxy (IrcProxy i) {
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