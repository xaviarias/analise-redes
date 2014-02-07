package edu.iscte.mcc1.analiseredes;

import twitter4j.Relationship;

public enum RelationshipType {

    /**
     * Any relation.
     */
    UNKNOWN(false),

    /**
     * Source has mentioned target.
     */
    MENTION(true),

    /**
     * Source follows target.
     */
    FOLLOWER(true),

    /**
     * Source and target follow each other.
     */
    FRIEND(false);

    private final boolean symmetric;

    private RelationshipType(boolean symmetric) {
        this.symmetric = symmetric;
    }

    public boolean isDirected() {
        return symmetric;
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
