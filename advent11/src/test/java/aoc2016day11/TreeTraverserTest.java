package aoc2016day11;

/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.  See the License for the specific language governing permissions and limitations
 * under the License.
 */


import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@code TreeTraverser}.
 *
 * @author Louis Wasserman
 */
public class TreeTraverserTest  {
    private static final class Tree {
        final char value;
        final List<Tree> children;

        public Tree(char value, Tree... children) {
            this.value = value;
            this.children = Arrays.asList(children);
        }
    }

    private static final class BinaryTree {
        final char value;
        @Nullable
        final BinaryTree left;
        @Nullable
        final BinaryTree right;

        private BinaryTree(char value, BinaryTree left, BinaryTree right) {
            this.value = value;
            this.left = left;
            this.right = right;
        }
    }

    private static final TreeTraverser<Tree> ADAPTER = new TreeTraverser<Tree>() {
        @Override
        public Stream<Tree> children(Tree node) {
            return node.children.stream();
        }
    };

    private static final TreeTraverser<Tree> ADAPTER_USING_USING =
            TreeTraverser.using(
                    node -> node.children.stream());

    private static final BinaryTreeTraverser<BinaryTree> BIN_ADAPTER =
            new BinaryTreeTraverser<BinaryTree>() {

                @Override
                public Optional<BinaryTree> leftChild(BinaryTree node) {
                    return Optional.ofNullable(node.left);
                }

                @Override
                public Optional<BinaryTree> rightChild(BinaryTree node) {
                    return Optional.ofNullable(node.right);
                }
            };

    //        h
    //      / | \
    //     /  e  \
    //    d       g
    //   /|\      |
    //  / | \     f
    // a  b  c
    static final Tree a = new Tree('a');
    static final Tree b = new Tree('b');
    static final Tree c = new Tree('c');
    static final Tree d = new Tree('d', a, b, c);
    static final Tree e = new Tree('e');
    static final Tree f = new Tree('f');
    static final Tree g = new Tree('g', f);
    static final Tree h = new Tree('h', d, e, g);

    //      d
    //     / \
    //    b   e
    //   / \   \
    //  a   c   f
    //         /
    //        g
    static final BinaryTree ba = new BinaryTree('a', null, null);
    static final BinaryTree bc = new BinaryTree('c', null, null);
    static final BinaryTree bb = new BinaryTree('b', ba, bc);
    static final BinaryTree bg = new BinaryTree('g', null, null);
    static final BinaryTree bf = new BinaryTree('f', bg, null);
    static final BinaryTree be = new BinaryTree('e', null, bf);
    static final BinaryTree bd = new BinaryTree('d', bb, be);

    static String iterationOrder(final Iterator<Tree> iterator) {
        return iterationOrder(() -> iterator);
    }

    static String iterationOrder(Iterable<Tree> iterable) {
        StringBuilder builder = new StringBuilder();
        for (Tree t : iterable) {
            builder.append(t.value);
        }
        return builder.toString();
    }

    static String binaryIterationOrder(Iterator<BinaryTree> iterator) {
        return binaryIterationOrder(() -> iterator);
    }

    static String binaryIterationOrder(Iterable<BinaryTree> iterable) {
        StringBuilder builder = new StringBuilder();
        for (BinaryTree t : iterable) {
            builder.append(t.value);
        }
        return builder.toString();
    }

    @Test
    public void testPreOrder() {
        assertThat(iterationOrder(ADAPTER.preOrderTraversal(h))).isEqualTo("hdabcegf");
        assertThat(binaryIterationOrder(BIN_ADAPTER.preOrderTraversal(bd))).isEqualTo("dbacefg");
    }

    @Test
    public void testPostOrder() {
        assertThat(iterationOrder(ADAPTER.postOrderTraversal(h))).isEqualTo("abcdefgh");
        assertThat(binaryIterationOrder(BIN_ADAPTER.postOrderTraversal(bd))).isEqualTo("acbgfed");
    }

    @Test
    public void testBreadthOrder() {
        assertThat(iterationOrder(ADAPTER.breadthFirstTraversal(h))).isEqualTo("hdegabcf");
        assertThat(binaryIterationOrder(BIN_ADAPTER.breadthFirstTraversal(bd))).isEqualTo("dbeacfg");
    }

    @Test
    public void testInOrder() {
        assertThat(binaryIterationOrder(BIN_ADAPTER.inOrderTraversal(bd))).isEqualTo("abcdegf");
    }

    @Test
    public void testUsing() {
        assertThat(iterationOrder(ADAPTER_USING_USING.preOrderTraversal(h))).isEqualTo("hdabcegf");
    }

    interface Assertion {
        void isEqualTo(String expected);
    }

    private static Assertion forActual(final String actual) {
        return expected -> {
            assertEquals(expected, actual);
        };
    }

    private static Assertion assertThat(String actual) {
        return forActual(actual);
    }

//    public void testNulls() {
//        NullPointerTester tester = new NullPointerTester();
//        tester.testAllPublicInstanceMethods(ADAPTER);
//        tester.testAllPublicInstanceMethods(BIN_ADAPTER);
//    }
}