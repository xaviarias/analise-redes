package edu.iscte.mcc1.analiseredes;

import twitter4j.TwitterException;

import java.util.logging.Logger;


public class TwitterInvoker {
    protected static final Logger LOGGER =
            Logger.getLogger(TwitterInvoker.class.getName());

    protected static final int RATE_EXCEEDED = 88;


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
                if (te.getErrorCode() == RATE_EXCEEDED) {
                    LOGGER.warning("Too many requests!");

                    try {
                        if (0 < te.getRetryAfter()) {
                            Thread.sleep(te.getRetryAfter());
                        } else {
                            Thread.sleep(15 * 60 * 1000);
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

    public static interface TwitterCall<T> {
        public T call() throws TwitterException;
    }

}
