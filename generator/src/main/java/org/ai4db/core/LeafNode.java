package org.ai4db.core;

public class LeafNode {
    String predicate;
    int cardinality;

    public LeafNode(String predicate, int cardinality) {
        this.predicate = predicate;
        this.cardinality = cardinality;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public int getCardinality() {
        return cardinality;
    }

    public void setCardinality(int cardinality) {
        this.cardinality = cardinality;
    }
}
