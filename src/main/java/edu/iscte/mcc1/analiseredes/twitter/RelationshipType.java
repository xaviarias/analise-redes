package edu.iscte.mcc1.analiseredes.twitter;

import twitter4j.Relationship;

public enum RelationshipType implements TwitterEdge {

    /**
     * Any relation.
     */
    UNKNOWN(false),

    /**
     * Source follows target.
     */
    FOLLOWER(true),

    /**
     * Source and target follow each other.
     */
    FRIEND(false);

    private final boolean directed;

    private RelationshipType(boolean directed) {
        this.directed = directed;
    }

    @Override
    public String getLabel() {
        return name();
    }

    @Override
    public long getWeigth() {
        return ordinal();
    }

    public boolean isDirected() {
        return directed;
    }

    public static RelationshipType fromRelationship(Relationship relationship) {
        if (relationship.isSourceFollowingTarget() &&
                relationship.isSourceFollowedByTarget()) {
            return RelationshipType.FRIEND;
        } else if (relationship.isSourceFollowingTarget()) {
            return RelationshipType.FOLLOWER;
        } else {
            return UNKNOWN;
        }
    }

}
