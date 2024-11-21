package datastructures.dictionaries;

import cse332.datastructures.trees.BinarySearchTree;
import datastructures.worklists.ArrayStack;

import java.lang.reflect.Array;

/**
 * AVLTree must be a subclass of BinarySearchTree<E> and must use
 * inheritance and calls to superclass methods to avoid unnecessary
 * duplication or copying of functionality.
 * <p>
 * 1. Create a subclass of BSTNode, perhaps named AVLNode.
 * 2. Override the insert method such that it creates AVLNode instances
 * instead of BSTNode instances.
 * 3. Do NOT "replace" the children array in BSTNode with a new
 * children array or left and right fields in AVLNode.  This will
 * instead mask the super-class fields (i.e., the resulting node
 * would actually have multiple copies of the node fields, with
 * code accessing one pair or the other depending on the type of
 * the references used to access the instance).  Such masking will
 * lead to highly perplexing and erroneous behavior. Instead,
 * continue using the existing BSTNode children array.
 * 4. Ensure that the class does not have redundant methods
 * 5. Cast a BSTNode to an AVLNode whenever necessary in your AVLTree.
 * This will result a lot of casts, so we recommend you make private methods
 * that encapsulate those casts.
 * 6. Do NOT override the toString method. It is used for grading.
 * 7. The internal structure of your AVLTree (from this.root to the leaves) must be correct
 */

public class AVLTree<K extends Comparable<? super K>, V> extends BinarySearchTree<K, V> {

    private V previousValue;
    public AVLTree() {
        super();
        previousValue = null;
    }

    public class AVLNode extends BSTNode {
        public int height;

        public AVLNode(K key, V value, int height) {
            super(key, value);
            this.height = height;
        }
    }


    @Override
    public V insert(K key, V value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException();
        }
        if (this.root == null) {
            this.root = new AVLNode(key, value, 0);
            this.size++;
            return null;
        } else {
            this.root = insertRecursive((AVLNode)this.root, key, value);
            return previousValue;
        }

    }
    private AVLNode insertRecursive(AVLNode root, K key, V value) {
        if(root != null) {
            AVLNode current = (AVLNode) root;
            //checks if we need to go right or left
            int leftOrRight = Integer.signum(current.key.compareTo(key));
            //the node we are on is the same as the key
            if(leftOrRight == 0) {
                this.previousValue = current.value;
                current.value = value;
                return current;
                //the key is to the right of the node
            } else if(leftOrRight == -1) {
                //if the spot to the right is empty, we know the key belongs there
                if(current.children[1] == null) {
                    current.children[1] = new AVLNode(key, value, 0);
                    this.size++;
                    this.previousValue = null;
                    //otherwise, make a recursive call to analyze the right branch
                } else {
                    current.children[1] = insertRecursive((AVLNode)current.children[1], key, value);
                }
                //the key is to the left of the node
            } else {
                //if the spot to the left is empty, we know the key belongs there
                if(current.children[0] == null) {
                    current.children[0] = new AVLNode(key, value, 0);
                    this.size++;
                    this.previousValue = null;
                } else {
                    current.children[0] = insertRecursive((AVLNode)current.children[0], key, value);
                }
            }
            //gets information to update height of current node
            int rightHeight = -1;
            int leftHeight = -1;
            if(current.children[0] != null) {
                AVLNode leftChild = (AVLNode)current.children[0];
                leftHeight = leftChild.height;
            }
            if(current.children[1] != null) {
                AVLNode rightChild = (AVLNode)current.children[1];
                rightHeight = rightChild.height;
            }
            //tests to make sure is balanced, if not we rotate
            if(Math.abs(leftHeight - rightHeight) >= 2) {
                int leftLeftHeight = -1;
                int leftRightHeight = -1;
                int rightLeftHeight = -1;
                int rightRightHeight = -1;
                //all these below gather the height of all 4 grandchildren of a node
                //so we can determine which rotation(s) we must use
                if(current.children[0] != null && current.children[0].children[0] != null) {
                    AVLNode leftLeftChild = (AVLNode) current.children[0].children[0];
                    leftLeftHeight = leftLeftChild.height;
                }
                if(current.children[0] != null && current.children[0].children[1] != null) {
                    AVLNode leftRightChild = (AVLNode) current.children[0].children[1];
                    leftRightHeight = leftRightChild.height;
                }
                if(current.children[1] != null && current.children[1].children[0] != null) {
                    AVLNode rightLeftChild = (AVLNode) current.children[1].children[0];
                    rightLeftHeight = rightLeftChild.height;
                }
                if(current.children[1] != null && current.children[1].children[1] != null) {
                    AVLNode rightRightChild = (AVLNode) current.children[1].children[1];
                    rightRightHeight = rightRightChild.height;
                }
                if(leftHeight > rightHeight) {
                    //case 1
                    if(leftLeftHeight > leftRightHeight) {
                        current = rotate(current, false);
                        //case 2
                    } else {
                        current.children[0] = rotate((AVLNode)current.children[0], true);
                        current = rotate((AVLNode)current, false);
                    }
                } else {
                    //case 4
                    if(rightRightHeight > rightLeftHeight) {
                        current = rotate(current, true);
                        //case 3
                    } else {
                        current.children[1] =rotate((AVLNode)current.children[1], false);
                        current = rotate((AVLNode)current, true);
                    }
                }
                //if we didn't need to do any rotations, just correct the height
                //information
            } else {
                current.height = Math.max(leftHeight,rightHeight) +1;
            }
            return current;
        }
        return null;
    }

    //true means rotate with right node
    //false means rotate with left node
    private AVLNode rotate(AVLNode root, boolean direction) {
        AVLNode temp;
        if(direction) {
            temp =  (AVLNode) root.children[1];
        } else {
            temp =  (AVLNode) root.children[0];
        }
        if(direction) {
            root.children[1] = temp.children[0];
            temp.children[0] = root;
        } else {
            root.children[0] = temp.children[1];
            temp.children[1] = root;
        }
        int rootRightHeight = -1;
        int rootLeftHeight = -1;
        int tempRightHeight = -1;
        int tempLeftHeight = -1;
        AVLNode rootRight = (AVLNode) root.children[1];
        AVLNode rootLeft = (AVLNode) root.children[0];
        AVLNode tempRight = (AVLNode) temp.children[1];
        AVLNode tempLeft = (AVLNode) temp.children[0];
        if(rootRight != null) {
            rootRightHeight = rootRight.height;
        }
        if(rootLeft != null) {
            rootLeftHeight = rootLeft.height;
        }
        root.height = Math.max(rootRightHeight, rootLeftHeight) + 1;
        if(tempRight != null) {
            tempRightHeight = tempRight.height;
        }
        if(tempLeft != null) {
            tempLeftHeight = tempLeft.height;
        }
        temp.height = Math.max(tempRightHeight, tempLeftHeight) + 1;
        return temp;
    }

}