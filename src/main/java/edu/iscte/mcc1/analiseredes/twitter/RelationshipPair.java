package edu.iscte.mcc1.analiseredes.twitter;

public class RelationshipPair {

    public final String source, target;

    public static RelationshipPair of(final String source, final String target) {
        return new RelationshipPair(source, target);
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

    private RelationshipPair(String source, String target) {
        this.source = source;
        this.target = target;
    }

}
