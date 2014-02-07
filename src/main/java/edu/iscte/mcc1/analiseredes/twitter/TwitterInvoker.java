package edu.iscte.mcc1.analiseredes.twitter;

import twitter4j.TwitterException;

import java.util.logging.Logger;

public class TwitterInvoker {

    protected static final Logger LOGGER = Logger.getLogger(TwitterInvoker.class.getName());

    public static final int RATE_WINDOW = 15 * 60 * 1000;


    private TwitterInvoker() {
    }

    /**
     * Calls TwitterCall until success.
     *
     * @throws TwitterException if a non-timeout exception occurs.
     */
    public static <T> T invoke(TwitterCall<T> callable) throws TwitterException {
        for (; ; ) {
            try {
                return callable.call();
            } catch (TwitterException te) {
                if (te.getErrorCode() == TwitterErrorCode.RATE_EXCEEDED) {
                    LOGGER.warning("Too many requests!");

                    try {
                        if (0 < te.getRetryAfter()) {
                            Thread.sleep(te.getRetryAfter());
                        } else {
                            Thread.sleep(RATE_WINDOW);
                        }
                    } catch (InterruptedException ie) {
                        LOGGER.severe("Unexpected exception");
                        throw new IllegalStateException(ie);
                    }
                } else {
                    throw te;
                }
            }
        }
    }

}
