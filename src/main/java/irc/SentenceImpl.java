/***
 * Sentence class : used for keeping the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc;

import jvn.Proxy.JvnAnnotation;

public class SentenceImpl implements java.io.Serializable, Sentence {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String 	data;
  
	public SentenceImpl() {
		data = new String("");
	}

	public void write(String text) {
		data = text;
	}

	public String read() {
		return data;	
	}
	
}