package org.ai4db.core;

import java.util.Collections;
import java.util.List;

public class AndOperatorNode implements OperatorNode {
    private OperatorNode childOperatorNode;
    public List<LeafNode> childLeafNodes;

    @Override
    public OperatorNode getChildOperatorNode() {
        return this.childOperatorNode;
    }

    @Override
    public void setChildOperatorNode(OperatorNode childOperatorNode) {
        this.childOperatorNode = childOperatorNode;
    }

    @Override
    public List<LeafNode> getChildLeafNodes() {
        return this.childLeafNodes;
    }

    @Override
    public void setChildLeafNodes(List<LeafNode> childLeafNodes) {
        this.childLeafNodes = childLeafNodes;
    }
}
