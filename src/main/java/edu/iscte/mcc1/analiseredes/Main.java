package edu.iscte.mcc1.analiseredes;

import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import java.util.logging.Logger;

public class Main {

    static Logger LOGGER = Logger.getLogger("Twiter app");

    public static void main(String args[]) throws Exception {

        // The factory instance is re-useable and thread safe.
        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();

       /* twitter.setOAuthConsumer("AEgjMRYpvOvpZixC3U2YwA",
                "DhtbGLivKy5zYRYRr0qwvkriCWbYI0CvV31mGSjruU");

        twitter.setOAuthAccessToken(new AccessToken(
                "632969165-t8El0Iz47HZOgOvaI7oHbvWScHOcJ6ZKQ7GPhWOC",
                "tmHwRVlGluzanBiyzRjD980ZxwemhEUNGUI3p3nUtePIS"));
*/
//        TwitterClient client = new TwitterClient(twitter);
//        client.run();

        Relationship rel = twitter.friendsFollowers()
                .showFriendship("15MpaRato", "larimaia");

        LOGGER.info("Relation = " + RelationshipType.fromRelationship(rel));
    }

}
