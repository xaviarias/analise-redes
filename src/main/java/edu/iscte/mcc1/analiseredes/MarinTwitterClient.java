package edu.iscte.mcc1.analiseredes;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import twitter4j.*;

import java.io.File;
import java.io.LineNumberReader;
import java.io.Writer;
import java.net.URL;
import java.util.Set;
import java.util.SortedSet;

import static edu.iscte.mcc1.analiseredes.RelationshipType.*;

public class MarinTwitterClient extends TwitterClient implements Runnable {

    private final Set<User> statusSet = Sets.newHashSet();


    public static void main(String args[]) throws Exception {

        // The factory instance is re-useable and thread safe.
        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();

        MarinTwitterClient client = new MarinTwitterClient(twitter);
        client.run();
    }

    public MarinTwitterClient(Twitter twitter) {
        super(twitter);
    }

    @Override
    public void run() {
        URL url = getClass().getClassLoader()
                .getResource("datasets/oscarmarin/AbrilJulio2011.txt");

        SortedSet<Long> tweets = readTweetsFromFile(url.getFile());

        Writer nodesWriter = createWriter("Morin [Nodes]");
        Writer mentionsWriter = createWriter("Mentions [Edges]");
        Writer relationshipWriter = createWriter("Relationships [Edges]");

        write(nodesWriter, "Id,Label\n");
        write(mentionsWriter, "Source,Target,Type,Label,Weight\n");
        write(relationshipWriter, "Source,Target,Type,Label,Weight\n");

        for (Long tweetId : tweets) {
            Status status = findStatus(tweetId);

            if (status != null) {
                processStatus(status, nodesWriter, mentionsWriter, relationshipWriter);
                sleep(TIME_TO_WAIT);
            }
        }
    }

    private void processStatus(Status status, Writer nodesWriter,
                               Writer mentionsWriter, Writer relationshipsWriter) {

        User user = status.getUser();
        if (!statusSet.contains(user)) {
            write(nodesWriter, user.getId() + "," + user.getScreenName() + "\n");
            statusSet.add(user);
        }

        if (status.isRetweet()) {
            User followed = status.getRetweetedStatus().getUser();

            Relationship relationship = getRelationship(
                    user.getScreenName(), followed.getScreenName());

            if (relationship != null) {
                RelationshipType relationshipType = fromRelationship(relationship);

                if (relationshipType != UNKNOWN) {
                    long source = relationship.getSourceUserId();
                    long target = relationship.getTargetUserId();

                    write(relationshipsWriter, source, target, relationshipType);
                }
            }
        }

        for (UserMentionEntity userMention : status.getUserMentionEntities()) {
            write(mentionsWriter, user.getId(), userMention.getId(), MENTION);
        }
    }

    private void write(Writer edgesWriter, long source, long target,
                       RelationshipType relationshipType) {

        StringBuilder builder = new StringBuilder();
        builder.append(source).append(",").append(target).append(",");
        builder.append(relationshipType.isDirected() ? "Directed" : "Unidirected");
        builder.append(",").append(relationshipType.name()).append(",");
        builder.append(relationshipType.ordinal()).append(".0").append('\n');

        write(edgesWriter, builder.toString());
    }

    private SortedSet<Long> readTweetsFromFile(String fileName) {
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

}
