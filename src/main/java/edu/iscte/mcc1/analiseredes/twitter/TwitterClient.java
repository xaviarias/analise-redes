package edu.iscte.mcc1.analiseredes.twitter;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import twitter4j.*;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.SortedSet;
import java.util.logging.Logger;

/**
 * @see twitter4j.Twitter#list()
 * @see twitter4j.Twitter#users()
 * @see twitter4j.Twitter#tweets()
 * @see twitter4j.Twitter#timelines()
 * @see twitter4j.Twitter#friendsFollowers()
 */
public abstract class TwitterClient {

    protected static final Logger LOGGER = Logger.getLogger(TwitterClient.class.getName());

    public static final String BASE_DIR = "D:\\Desarrollo\\Projects\\" +
            "analise-redes\\src\\main\\resources\\graphs";

    protected static final long TIME_TO_WAIT = 18 * 1000;

    protected static final Charset CHARSET = Charset.forName("UTF-8");

    protected final Twitter twitter;


    public TwitterClient(Twitter twitter) {
        this.twitter = twitter;
    }

    protected Status findStatus(final Long tweetId) {
        try {
            return TwitterInvoker.invoke(new TwitterCall<Status>() {
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
            return TwitterInvoker.invoke(new TwitterCall<Relationship>() {
                public Relationship call() throws TwitterException {
                    return twitter.friendsFollowers().showFriendship(source, target);
                }
            });
        } catch (TwitterException e) {
            LOGGER.warning("Relationship not found");
            return null;
        }
    }

    protected SortedSet<Long> readTweetsFromFile(String fileName) {
        final SortedSet<Long> statuses = Sets.newTreeSet();

        try {
            LineNumberReader reader = new LineNumberReader(
                    Files.newReader(new File(fileName), CHARSET));

            String statusId;
            while ((statusId = reader.readLine()) != null)
                statuses.add(Long.parseLong(statusId));

            return statuses;
        } catch (Exception e) {
            throw new IllegalStateException(fileName);
        }
    }

    protected Writer createWriter(String name) {
        File folder = new File(BASE_DIR);

        if (!folder.exists())
            throw new IllegalStateException(folder.getPath());

        File file = new File(folder, name + ".csv");
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

    protected void write(Writer writer, TwitterEdge edge, User source, User target) {
        write(writer, edge, source.getId(), target.getId());
    }

    protected void write(Writer writer, TwitterEdge edge, long source, long target) {

        StringBuilder builder = new StringBuilder();
        builder.append(source).append(',').append(target).append(',');
        builder.append(edge.isDirected() ? "Directed" : "Unidirected");
        builder.append(',').append(edge.getLabel()).append(',');
        builder.append(edge.getWeigth()).append(".0").append('\n');

        write(writer, builder.toString());
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
}
