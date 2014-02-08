package edu.iscte.mcc1.analiseredes.twitter;

import twitter4j.TwitterException;

import java.util.logging.Logger;

public class TwitterInvoker {

    private static final Logger LOGGER = Logger.getLogger(TwitterInvoker.class.getName());

    private static final int CALLS_PER_WINDOW = 180;
    private static final int RATE_WINDOW = 15 * 60 * 1000;
    private static final int TIME_TO_WAIT = RATE_WINDOW / CALLS_PER_WINDOW;

    /**
     * Calls TwitterCall until success.
     *
     * @throws TwitterException if a non-timeout exception occurs.
     */
    public static <T> T invoke(TwitterCall<T> callable) throws TwitterException {
        long timeToWait = TIME_TO_WAIT;
        for (; ; ) {
            try {
                return callable.call();
            } catch (TwitterException te) {
                if (te.getErrorCode() == TwitterErrorCode.RATE_EXCEEDED) {
                    LOGGER.warning("Too many requests!");
                    timeToWait = 0 < te.getRetryAfter() ?
                            te.getRetryAfter() : RATE_WINDOW;
                } else {
                    throw te;
                }
            } finally {
                try {
                    Thread.sleep(timeToWait);
                } catch (InterruptedException ie) {
                    LOGGER.severe("Unexpected exception");
                    throw new IllegalStateException(ie);
                }
            }
        }
    }

    private TwitterInvoker() {
    }

}
