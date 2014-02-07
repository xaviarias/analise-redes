package edu.iscte.mcc1.analiseredes;

import com.google.common.io.Files;
import twitter4j.Relationship;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.logging.Logger;

/**
 * @see twitter4j.Twitter#list()
 * @see twitter4j.Twitter#users()
 * @see twitter4j.Twitter#tweets()
 * @see twitter4j.Twitter#timelines()
 * @see twitter4j.Twitter#friendsFollowers()
 */

public abstract class TwitterClient {
    protected static final Logger LOGGER =
            Logger.getLogger(TwitterClient.class.getName());

    protected static final long TIME_TO_WAIT = 18 * 1000;

    protected static final int UNKNOWN_USER_ERROR = 163;

    protected static final Charset CHARSET = Charset.forName("UTF-8");

    protected final Twitter twitter;

    public TwitterClient(Twitter twitter) {
        this.twitter = twitter;
    }

    protected Status findStatus(final Long tweetId) {
        try {
            return TwitterInvoker.invoke(new TwitterInvoker.TwitterCall<Status>() {
                public Status call() throws TwitterException {
                    return twitter.showStatus(tweetId);
                }
            });
        } catch (TwitterException e) {
            LOGGER.warning("Status not found");
            return null;
        }
    }

    protected Relationship getRelationship(final String source, final String target) {
        try {
            return TwitterInvoker.invoke(new TwitterInvoker.TwitterCall<Relationship>() {
                public Relationship call() throws TwitterException {
                    return twitter.friendsFollowers().showFriendship(source, target);
                }
            });
        } catch (TwitterException e) {
            LOGGER.warning("Relationship not found");
            return null;
        }
    }

    protected Writer createWriter(String name) {
        File folder = new File(
                "D:\\Desarrollo\\Projects\\analise-redes\\src\\main\\resources\\gephi");

        if (!folder.exists())
            throw new IllegalStateException(folder.getPath());

        File file = new File(folder, name + "-" + System.currentTimeMillis() + ".csv");
        LOGGER.info("Exporting to file: " + file);

        try {
            if (!file.createNewFile()) {
                throw new IllegalStateException("Cannot create file: " + file);
            }

            return Files.newWriter(file, CHARSET);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected void write(Writer writer, String s) {
        try {
            writer.write(s);
            writer.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected void close(Writer writer) {
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            LOGGER.severe("Unexpected exception");
            throw new IllegalStateException(e);
        }
    }

    public static class RelationshipPair {

        String source, target;

        static RelationshipPair of(final String from, final String to) {
            return new RelationshipPair() {{
                this.source = from;
                this.target = to;
            }};
        }

        @Override
        public boolean equals(Object pair) {
            if (this == pair) return true;
            if (pair == null || getClass() != pair.getClass()) return false;

            RelationshipPair that = (RelationshipPair) pair;
            return source.equals(that.source) && target.equals(that.target);
        }

        @Override
        public int hashCode() {
            int result = source.hashCode();
            result = 31 * result + target.hashCode();
            return result;
        }

    }

}
