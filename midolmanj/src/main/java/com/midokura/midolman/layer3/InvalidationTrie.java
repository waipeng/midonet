/*
 * Copyright 2012 Midokura Pte. Ltd.
 */

package com.midokura.midolman.layer3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvalidationTrie extends RoutesTrie {

    private final static Logger log = LoggerFactory.getLogger(
        InvalidationTrie.class);

    public static Iterable<RoutesTrie.TrieNode> getAllDescendants(RoutesTrie.TrieNode node){
        if (node == null)
            return Collections.emptyList();
        List<TrieNode> descendants = new ArrayList<TrieNode>();
        Stack<TrieNode> stack = new Stack<TrieNode>();
        stack.add(node);
        while(!stack.empty()){
            TrieNode n = stack.pop();
            descendants.add(n);
            if(null != n.left)
                stack.add(n.left);
            if(null != n.right)
                stack.add(n.right);
        }
        return descendants;
    }

    public RoutesTrie.TrieNode projectRouteAndGetSubTree(Route rt) {

        boolean inLeftChild;
        RoutesTrie.TrieNode node = dstPrefixTrie;
        int rt_dst = rt.dstNetworkAddr;
        log.debug("Root {}, # roots {}", dstPrefixTrie, numRoutes);
        while (null != node && rt.dstNetworkLength >= node.bitlen
            && addrsMatch(rt_dst, node.addr, node.bitlen)) {
            log.debug("traversing {}", node);
            // The addresses match, descend to the children.
            if (rt.dstNetworkLength == node.bitlen) {
                // TODO(rossella) be more precise in future. If the route added
                // is more specific for some flow than the one in the set,
                // invalidate those flows. Eg. src routing
                return node;
            }
            // Use bit at position bitlen to decide on left or right branch.
            inLeftChild = 0 == (rt_dst & (0x80000000 >>> node.bitlen));
            node = (inLeftChild) ? node.left : node.right;
        }

        if (null != node) {

            /* Only 2 cases to consider (see addTag for a longer analysis)
            1. the node that would hold this route is a sibling of node and we'd
               need to create a node to be the parent of both
            2. the node that would hold this route should be the parent of node

            For case 1 the subtree empty. For case 2 the subtree is the tree
            whose root is node.  */
            int diffBit = findMSB(node.addr ^ rt_dst);
            // Case 1
            /*if (diffBit < node.bitlen && diffBit < rt.dstNetworkLength) { // Case 1
                return null;
            }*/
            // Case 2
            if (rt.dstNetworkLength < diffBit && rt.dstNetworkLength < node.bitlen) {
                return node;
            }

        }
        // the trie is emptu or subtree is empty
        return null;
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    public String toString() {
        return "InvalidationTrie [dstPrefixTrie=" + dstPrefixTrie + "]";
    }

}
