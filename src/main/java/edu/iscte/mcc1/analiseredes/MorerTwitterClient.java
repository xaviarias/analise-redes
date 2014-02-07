package edu.iscte.mcc1.analiseredes;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.io.File;
import java.io.Writer;
import java.net.URL;
import java.util.*;


public class MorerTwitterClient extends TwitterClient implements Runnable {

    public MorerTwitterClient(Twitter twitter) {
        super(twitter);
    }

    public void run() {
        SortedSet<String> users = createUsers();
        Set<List<String>> pairs = createPairs(users);

        Map<String, List<Relationship>> rels = mapRelationships(pairs);
        exportToFiles(rels);
    }

    private SortedSet<String> createUsers() {
        SortedSet<String> names = new TreeSet<String>();

        URL fileUrl = getClass().getClassLoader()
                .getResource("datasets/ignaciomorer");

        if (fileUrl == null)
            throw new IllegalStateException("datasets/ignaciomorer");

        File dataset = new File(fileUrl.getFile());
        for (String file : dataset.list()) {
            names.add(file.substring(0, file.lastIndexOf('.')));
        }

        LOGGER.info("Users: " + names.toString());
        return names;
    }

    private Set<List<String>> createPairs(SortedSet<String> users) {
        Set<List<String>> product = Sets.cartesianProduct(users, users);
     /*   final Set<String[]> symmetric = new TreeSet<String[]>(new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                return 0;
            }
        });*/

        Set<List<String>> pairs = Sets.filter(product,
                new Predicate<List<String>>() {

                    @Override   // Filer groups with same user:
                    public boolean apply(List<String> input) {
                        String source = input.get(0), target = input.get(1);

                    /*    if (!source.equals(target)) {
                            symmetric.add(new String[]{source, target});
                            symmetric.add(new String[]{target, source});
                        } else {
                            return false; // Skip self
                        }

                        return !symmetric.contains(new String[]{target, source});*/
                        return !source.equals(target);
                    }
                });

        LOGGER.info("Pairs: " + pairs);
        return pairs;
    }

    private Map<String, List<Relationship>> mapRelationships(Set<List<String>> pairs) {
        final Map<String, List<Relationship>> rels =
                new HashMap<String, List<Relationship>>();

        Set<String> unknownUsers = new HashSet<String>();

        for (List<String> pair : pairs) {
            String source = pair.get(0), target = pair.get(1);
            if (unknownUsers.contains(source)) continue;

            try {
                addRelationship(rels, source, target);
            } catch (TwitterException e) {
                if (e.getErrorCode() == UNKNOWN_USER_ERROR) {
                    LOGGER.warning("Unknown user: " + source);
                    unknownUsers.add(source);
                } else {
                    LOGGER.warning("Error: " + e.getErrorMessage());
                }
            }
            sleep(TIME_TO_WAIT);
        }

        LOGGER.info("Relationships: " + rels);
        return rels;
    }

    private void addRelationship(final Map<String, List<Relationship>> rels,
                                 final String source, final String target) throws TwitterException {

        Relationship rel = getRelationship(source, target);

        if (rel != null) {
            if (!rels.containsKey(source))
                rels.put(source, new ArrayList<Relationship>());

            rels.get(source).add(rel);
        }
    }

    private void exportToFiles(/*Map<String, Long> ids, */Map<String, List<Relationship>> rels) {
        Writer nodesWriter = createWriter("Nodes");
        Writer edgesWriter = createWriter("Edges");

        write(nodesWriter, "Id,Label\n");
        write(edgesWriter, "Source,Target,Label\n");

        for (Map.Entry<String, List<Relationship>> relEntry : rels.entrySet()) {
            if (!relEntry.getValue().isEmpty()) {

                // Write node
                long sourceId = relEntry.getValue().get(0).getSourceUserId();
                write(nodesWriter, sourceId + "," + relEntry.getKey() + "\n");

                // Write edges
                for (Relationship rel : relEntry.getValue()) {
                    RelationshipType relType = RelationshipType.fromRelationship(rel);

                    if (relType != RelationshipType.UNKNOWN)
                        write(edgesWriter, sourceId + "," + rel.getTargetUserId() +
                                "," + relType + "\n");
                }

            }
        }

        close(nodesWriter);
        close(edgesWriter);
    }

}
