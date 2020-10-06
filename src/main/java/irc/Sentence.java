package irc;

import jvn.Proxy.JvnAnnotation;

public interface Sentence {

    @JvnAnnotation(type="_W")
    public void write(String text);

    @JvnAnnotation(type ="_R")
    public String read();

}
