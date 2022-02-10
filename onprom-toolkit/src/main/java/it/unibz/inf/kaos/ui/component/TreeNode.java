/*
 * onprom-toolkit
 *
 * TreeNode.java
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

package it.unibz.inf.kaos.ui.component;

import com.google.common.collect.Sets;
import it.unibz.inf.kaos.data.FileType;

import javax.annotation.Nonnull;
import javax.swing.tree.DefaultMutableTreeNode;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * @author T. E. Kalayci on 26-Apr-2017
 */
public class TreeNode<T> extends DefaultMutableTreeNode {
    private final String title;
    private final FileType type;
    private final int id;
    private final ZonedDateTime timestamp = ZonedDateTime.now();

    TreeNode(int _id, String _title, FileType _type, T object) {
        super(object);
        id = _id;
        title = _title;
        type = _type;
    }

    @Override
    public TreeNode<T> getChildAt(int index) {
        return (TreeNode<T>) super.getChildAt(index);
    }

    @Override
    public T getUserObject() {
        return (T) super.getUserObject();
    }

    @Nonnull
    Set<TreeNode<T>> getChildren() {
        Set<TreeNode<T>> children = Sets.newLinkedHashSet();
        for (int i = 0; i < getChildCount(); i++) {
            children.add(getChildAt(i));
        }
        return children;
    }

    boolean removeChild(T objectToRemove) {
        for (int i = 0; i < getChildCount(); i++) {
            TreeNode childAt = getChildAt(i);
            if (childAt.getUserObject().equals(objectToRemove)) {
                remove(i);
                return true;
            }
        }
        return false;
    }

    public Optional<T> getUserObjectProvider() {
        return Optional.ofNullable(getUserObject());
    }

    public String toString() {
        if (title == null || title.isEmpty()) {
            return getUserObject().toString();
        }
        if (isRoot()) {
            return title;
        }
        return title + " (" + timestamp + ")";
    }

    public FileType getType() {
        return type;
    }

    public String getIdentifier() {
        return Integer.toString(id);
    }

    public String getTitle() {
        return title;
    }

    String getIcon() {
        return type.getDefaultExtension();
    }
}
