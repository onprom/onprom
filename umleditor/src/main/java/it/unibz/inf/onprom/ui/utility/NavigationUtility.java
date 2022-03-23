/*
 * onprom-umleditor
 *
 * NavigationUtility.java
 *
 * Copyright (C) 2016-2019 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 * KAOS: Knowledge-Aware Operational Support project
 * (https://kaos.inf.unibz.it).
 *
 * Please visit https://onprom.inf.unibz.it for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.inf.onprom.ui.utility;

import com.google.common.collect.Sets;
import it.unibz.inf.onprom.data.*;
import it.unibz.inf.onprom.interfaces.DiagramShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.Stack;

/**
 * Various methods to navigate diagram graph
 * <p>
 * @author T. E. Kalayci
 * Date: 21-Nov-16
 */
public class NavigationUtility {
    private static final Logger logger = LoggerFactory.getLogger(NavigationUtility.class.getName());

    private NavigationUtility() {
    }

    private static synchronized void traverse(Stack<DiagramShape> path, Set<DiagramShape> onPath, Set<Set<DiagramShape>> paths, UMLClass startNode, UMLClass endNode, boolean functionalCheck) {
        onPath.add(startNode);
        //logger.info("currentNode:"+startNode);
        if (startNode.equals(endNode)) {
            path.push(endNode);
            //logger.info("found:"+path);
            paths.add(Sets.newLinkedHashSet(path));
            path.pop();
        } else {
            for (Relationship relation : startNode.getRelations()) {
                //logger.info("relation:"+relation);
                if (relation instanceof Association || relation instanceof Inheritance) {
                    if (!path.contains(relation)) {
                        if (relation.getFirstClass().equals(startNode)) {
                            if (!functionalCheck || relation.isSecondFunctional()) {
                                if (!onPath.contains(relation.getSecondClass())) {
                                    path.push(relation);
                                    traverse(path, onPath, paths, relation.getSecondClass(), endNode, functionalCheck);
                                }
                            }
                        }
                        if (relation.getSecondClass().equals(startNode)) {
                            if (!functionalCheck || relation.isFirstFunctional()) {
                                if (!onPath.contains(relation.getFirstClass())) {
                                    path.push(relation);
                                    traverse(path, onPath, paths, relation.getFirstClass(), endNode, functionalCheck);
                                }
                            }
                        }
                        if ((relation instanceof Association) && (((Association) relation).getAssociationClass() != null)) {
                            traverse(path, onPath, paths, ((Association) relation).getAssociationClass(), endNode, functionalCheck);
                        }
                    }
                }
            }
            if (startNode instanceof AssociationClass) {
                Association relation = ((AssociationClass) startNode).getAssociation();
                if (!onPath.contains(relation.getFirstClass())) {
                    path.push(relation);
                    path.push(relation.getFirstClass());
                    traverse(path, onPath, paths, relation.getFirstClass(), endNode, functionalCheck);
                }
                if (!onPath.contains(relation.getSecondClass())) {
                    path.push(relation);
                    path.push(relation.getSecondClass());
                    traverse(path, onPath, paths, relation.getSecondClass(), endNode, functionalCheck);
                }
            }
        }
        if (!path.isEmpty()) {
            path.pop();
        }
        // done exploring from startNode, so remove it from onPath
        onPath.remove(startNode);
    }

    private static Set<Set<DiagramShape>> findAllPaths(UMLClass startNode, UMLClass endNode, boolean functionalCheck) {
        Set<Set<DiagramShape>> paths = Sets.newLinkedHashSet();
        traverse(new Stack<>(), Sets.newLinkedHashSet(), paths, startNode, endNode, functionalCheck);
        return paths;
    }


    public static Set<Set<DiagramShape>> getAllPaths(UMLClass startNode, UMLClass endNode) {
        return findAllPaths(startNode, endNode, false);
    }

    public static Set<Set<DiagramShape>> getFunctionalPaths(UMLClass startNode, UMLClass endNode) {
        return findAllPaths(startNode, endNode, true);
    }

    public static boolean isConnected(UMLClass startNode, UMLClass endNode, boolean functionalCheck) {
        if (startNode.equals(endNode))
            return true;
        Stack<UMLClass> stack = new Stack<>();
        Set<UMLClass> visited = Sets.newHashSet();
        stack.push(startNode);
        while (!stack.empty()) {
            UMLClass node = stack.pop();
            if (!visited.contains(node)) {
                visited.add(node);
                for (Relationship relation : node.getRelations()) {
                    if ((relation instanceof Association) && (((Association) relation).hasAssociation(endNode))) {
                        return true;
                    }
                    if (relation.getFirstClass().equals(node)) {
                        if (!functionalCheck || relation.isSecondFunctional()) {
                            if (!visited.contains(relation.getSecondClass())) {
                                if (relation.getSecondClass().equals(endNode))
                                    return true;
                                stack.push(relation.getSecondClass());
                            }
                        }
                    } else if (relation.getSecondClass().equals(node)) {
                        if (!functionalCheck || relation.isFirstFunctional()) {
                            if (!visited.contains(relation.getFirstClass())) {
                                if (relation.getFirstClass().equals(endNode))
                                    return true;
                                stack.push(relation.getFirstClass());
                            }
                        }
                    }
                }
                if (node instanceof AssociationClass) {
                    Association relation = ((AssociationClass) node).getAssociation();
                    if (!visited.contains(relation.getFirstClass())) {
                        if (relation.getFirstClass().equals(endNode))
                            return true;
                        stack.push(relation.getFirstClass());
                    }
                    if (!visited.contains(relation.getSecondClass())) {
                        if (relation.getSecondClass().equals(endNode))
                            return true;
                        stack.push(relation.getSecondClass());
                    }
                }
            }
        }
        return false;
    }

    public static Optional<Relationship> isAdjacent(UMLClass startNode, UMLClass endNode) {
        for (Relationship relation : startNode.getRelations()) {
            if (relation.getFirstClass().equals(endNode) || relation.getSecondClass().equals(endNode)) {
                return Optional.of(relation);
            }
        }
        return Optional.empty();
    }

    static boolean checkPath(UMLClass startingNode, Set<DiagramShape> path) {
        UMLClass node = startingNode;
        for (DiagramShape shape : path) {
            if (shape instanceof Association) {
                Association association = (Association) shape;
                if (association.getFirstClass().equals(node)) {
                    if (!association.getFirstMultiplicity().isFunctional()) {
                        return false;
                    }
                    node = association.getFirstClass();
                } else if (association.getSecondClass().equals(node)) {
                    if (!association.getSecondMultiplicity().isFunctional()) {
                        return false;
                    }
                    node = association.getSecondClass();
                } else {
                    logger.error("PATH IS BROKEN!");
                    return false;
                }
            }
        }
        return true;
    }
}