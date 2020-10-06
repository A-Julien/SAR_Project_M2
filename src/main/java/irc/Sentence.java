/***
 * Sentence class : used for keeping the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc;

import jvn.Proxy.JvnAnnotation;

public class Sentence implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String 	data;
  
	public Sentence() {
		data = new String("");
	}

	@JvnAnnotation(type="_W")
	public void write(String text) {
		data = text;
	}

	@JvnAnnotation(type ="_R")
	public String read() {
		return data;	
	}
	
}