package org.ai4db.core;

import com.alibaba.fastjson2.JSON;

public class ExpressionTree {
    private OperatorNode root;

    public ExpressionTree(OperatorNode root) {
        this.root = root;
    }

    public OperatorNode getRoot() {
        return root;
    }

    public void setRoot(OperatorNode root) {
        this.root = root;
    }


    public String parseStructure() {
        return JSON.toJSONString(this);
    }
}
