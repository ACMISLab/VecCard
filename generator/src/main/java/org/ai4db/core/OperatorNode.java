package org.ai4db.core;

import java.util.Collections;
import java.util.List;

public interface OperatorNode {
    OperatorNode childOperatorNode = null;
    List<LeafNode> childLeafNodes = Collections.emptyList();


    public OperatorNode getChildOperatorNode();
    public void setChildOperatorNode(OperatorNode childOperatorNode);
    public List<LeafNode> getChildLeafNodes();
    public void setChildLeafNodes(List<LeafNode> childLeafNodes);

}
