package edu.iscte.mcc1.analiseredes.twitter;

import twitter4j.TwitterException;

public interface TwitterCall<T> {
    public T call() throws TwitterException;
}
