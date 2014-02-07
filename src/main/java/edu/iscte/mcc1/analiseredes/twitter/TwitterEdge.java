package edu.iscte.mcc1.analiseredes.twitter;

public interface TwitterEdge {

    String getLabel();

    long getWeigth();

    boolean isDirected();

}
