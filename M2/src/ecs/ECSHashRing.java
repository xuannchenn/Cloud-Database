package ecs;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;
import shared.HashingFunction.MD5;
import shared.ZooKeeperUtils;

import java.math.BigInteger; // radix = 16 is the hexadecimal form

import java.util.*;

/**
 * This class builds the Logical Hash Ring using a TreeMap
 * The hash ring is consisted of ECSNodes that can be configurable
 */
public class ECSHashRing {

    private Logger logger = Logger.getRootLogger();
    private TreeMap<BigInteger, ECSNode> activeNodes = new TreeMap<>();


    public ECSHashRing() {
    }

    // TODO: Check
    public ECSHashRing(String jsonData) {
        Collection<ECSNode> nodes = new Gson().fromJson(
                jsonData,
                new TypeToken<List<ECSNode>>() {
                }.getType());

        for (ECSNode node : nodes) {
            addNode(new ECSNode(node));
        }
    }


    public int getSize() {
        return this.activeNodes.size();
    }

    public TreeMap<BigInteger, ECSNode> getActiveNodes() {
        return activeNodes;
    }

    // TODO
    public ECSNode getNodeByHostPort(ECSNode node) {
        return null;

    }

    public ECSNode getNodeByHash(BigInteger hash) {
        if (this.activeNodes.size() == 0)
            return null;

        if (this.activeNodes.lastKey().equals(hash)) {
            // return the first entry given the largest
            return this.activeNodes.firstEntry().getValue();
        }
        return this.activeNodes.ceilingEntry(hash).getValue();
    }


    // TODO
    public ECSNode getNodeByName(String keyName) {
        if (this.activeNodes.size() == 0) {
            logger.debug("[ECSHashRing] ring size is 0!");
            return null;
        }
        BigInteger hash = MD5.HashInBI(keyName);
        if (this.activeNodes.lastKey().equals(hash)) {
            // return the first entry given the largest
            return this.activeNodes.firstEntry().getValue();
        }

        return this.activeNodes.ceilingEntry(hash).getValue();
    }


    public ECSNode getPrevNode(String hashName) {
        if (this.activeNodes.size() == 0)
            return null;
        BigInteger currKey = MD5.HashInBI(hashName);
        if (this.activeNodes.firstKey().equals(currKey)) {
            // return the last entry given the smallest
            return this.activeNodes.lastEntry().getValue();
        }
        return this.activeNodes.lowerEntry(currKey).getValue();

    }

    public ECSNode getPrevNode(BigInteger currKey) {
        if (this.activeNodes.size() == 0)
            return null;
        if (this.activeNodes.firstKey().equals(currKey)) {
            // return the last entry given the smallest
            return this.activeNodes.lastEntry().getValue();
        }

        return this.activeNodes.lowerEntry(currKey).getValue();

    }

    public ECSNode getNextNode(String hashName) {
        if (this.activeNodes.size() == 0)
            return null;
        BigInteger currKey = MD5.HashInBI(hashName);
        if (this.activeNodes.lastKey().equals(currKey)) {
            // return the first entry given the largest
            return this.activeNodes.firstEntry().getValue();
        }
        // TODO: if works for a node not added yet
        return this.activeNodes.lowerEntry(currKey).getValue();

    }

    public ECSNode getNextNode(BigInteger currKey) {
        if (this.activeNodes.size() == 0)
            return null;
        if (this.activeNodes.lastKey().equals(currKey)) {
            // return the first entry given the largest
            return this.activeNodes.firstEntry().getValue();
        }
        return this.activeNodes.lowerEntry(currKey).getValue();

    }

    public void addNode(ECSNode node) {
        logger.debug("Current ring size: " + this.activeNodes.size());
        logger.debug("[ECSHashRing] Adding node: " + node.getNodeName());
        printNode(node);

        ECSNode prevNode = this.getPrevNode(node.getNodeHash());
        if (prevNode != null) {
            node.setNodeStartHash(prevNode.getNodeHash());
            this.activeNodes.put(prevNode.getNodeHash(), prevNode);
        }

        ECSNode nextNode = this.getNextNode(node.getNodeHash());
        if (nextNode != null) {
            nextNode.setNodeStartHash(node.getNodeHash());
            this.activeNodes.put(nextNode.getNodeHash(), nextNode);
        }

        this.activeNodes.put(node.getNodeHash(), node);

    }

    public String[] removeNode(ECSNode node) {

        logger.debug("[ECSHashRing] Removing node:");

        printNode(node);

        String[] hashRange = node.getNodeHashRange();

        ECSNode nextNode = this.getNextNode(node.getNodeHash());
        if (nextNode != null) {
            nextNode.setNodeStartHash(node.getNodeStartHash());
            this.activeNodes.put(nextNode.getNodeHash(), nextNode);
        }

        this.activeNodes.remove(node);
        return hashRange;
    }

    public void printNode(ECSNode node) {
        logger.info("\t\tnode name: " + node.getNodeName());
        logger.info("\t\tnode host: " + node.getNodeHost());
        logger.info("\t\tnode hash: " + node.getNodeHash()  );
    }


}
