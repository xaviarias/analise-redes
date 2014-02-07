package edu.iscte.mcc1.analiseredes.twitter;

public enum InteractionType implements TwitterEdge {

    RETWEET, MENTION;

    @Override
    public String getLabel() {
        return name();
    }

    @Override
    public long getWeigth() {
        return ordinal();
    }

    public boolean isDirected() {
        return true;
    }

}
