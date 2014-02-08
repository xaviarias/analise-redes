package edu.iscte.mcc1.analiseredes.twitter;

import twitter4j.TwitterException;

public interface TwitterCall<T> {
    T call() throws TwitterException;
}
