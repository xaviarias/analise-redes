package edu.iscte.mcc1.analiseredes;

import com.google.common.collect.Sets;
import edu.iscte.mcc1.analiseredes.twitter.InteractionType;
import edu.iscte.mcc1.analiseredes.twitter.RelationshipPair;
import edu.iscte.mcc1.analiseredes.twitter.RelationshipType;
import edu.iscte.mcc1.analiseredes.twitter.TwitterClient;
import twitter4j.*;

import java.io.File;
import java.io.Writer;
import java.net.URL;
import java.util.Set;
import java.util.SortedSet;

import static edu.iscte.mcc1.analiseredes.twitter.RelationshipType.*;

public class MarinTwitterClient extends TwitterClient implements Runnable {

    private final Set<User> statusSet = Sets.newHashSet();

    private final Set<RelationshipPair> retweetPairs = Sets.newHashSet();
    private final Set<RelationshipPair> mentionPairs = Sets.newHashSet();
    private final Set<RelationshipPair> relationshipPairs = Sets.newHashSet();

    private final Writer nodesWriter;
    private final Writer retweetsWriter;
    private final Writer mentionsWriter;
    private final Writer relationshipsWriter;


    public static void main(String args[]) throws Exception {

        // The factory instance is re-useable and thread safe.
        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();

        MarinTwitterClient client = new MarinTwitterClient(twitter);
        client.run();
    }

    public MarinTwitterClient(Twitter twitter) {
        super(twitter);

        nodesWriter = createWriter("AbrilJulio2011 [Nodes]");
        retweetsWriter = createWriter("Retweets [Edges]");
        mentionsWriter = createWriter("Mentions [Edges]");
        relationshipsWriter = createWriter("Relationships [Edges]");

        write(nodesWriter, "Id,Label\n");
        write(retweetsWriter, "Source,Target,Type,Label,Weight\n");
        write(mentionsWriter, "Source,Target,Type,Label,Weight\n");
        write(relationshipsWriter, "Source,Target,Type,Label,Weight\n");
    }

    @Override
    public void run() {
        File tweetsFile = new File(getSourceRoot(),
                "src/main/resources/datasets/oscarmarin/AbrilJulio2011.txt");

        SortedSet<Long> tweets = readTweetsFromFile(tweetsFile);

        for (Long tweetId : tweets) {
            Status status = findStatus(tweetId);

            if (status != null) {
                addUser(status.getUser());
                addRetweet(status);
                addMentions(status);

                sleep(TIME_TO_WAIT);
            }
        }
    }

    private void addUser(User user) {
        if (!statusSet.contains(user)) {
            write(nodesWriter, user.getId() + "," + user.getScreenName() + "\n");
            statusSet.add(user);
        }
    }

    private void addRetweet(Status status) {
        if (status.isRetweeted()) {
            User user = status.getUser();
            User retweeted = status.getRetweetedStatus().getUser();

            String source = user.getScreenName(), target = retweeted.getScreenName();
            RelationshipPair pair = RelationshipPair.of(source, target);

            if (!retweetPairs.contains(pair)) {
                write(retweetsWriter, InteractionType.RETWEET, user, retweeted);
                retweetPairs.add(pair);
            }

            addRelationship(pair);
        }
    }

    private void addMentions(Status status) {
        for (UserMentionEntity userMention : status.getUserMentionEntities()) {
            String source = status.getUser().getScreenName(), target = userMention.getScreenName();
            RelationshipPair pair = RelationshipPair.of(source, target);

            if (!mentionPairs.contains(pair)) {
                long sourceId = status.getUser().getId(), targetId = userMention.getId();
                write(mentionsWriter, InteractionType.MENTION, sourceId, targetId);
                mentionPairs.add(pair);
            }

            addRelationship(pair);
        }
    }

    private void addRelationship(RelationshipPair pair) {
        if (!relationshipPairs.contains(pair)) {
            Relationship relationship = getRelationship(pair.source, pair.target);

            if (relationship != null) {
                RelationshipType relationshipType = fromRelationship(relationship);

                if (relationshipType != UNKNOWN) {
                    long sourceId = relationship.getSourceUserId();
                    long targetId = relationship.getTargetUserId();

                    write(relationshipsWriter, relationshipType, sourceId, targetId);
                }
                relationshipPairs.add(pair);
            }
        }
    }

}
