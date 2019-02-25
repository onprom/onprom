/*
 * onprom-umleditor
 *
 * DiagramShape.java
 *
 * Copyright (C) 2016-2018 Free University of Bozen-Bolzano
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

package it.unibz.inf.kaos.interfaces;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import it.unibz.inf.kaos.data.State;

import javax.swing.*;
import java.awt.*;

/**
 * Interface of diagram shapes
 *
 * @author T. E. Kalayci
 * 19-Jun-2017
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
public interface DiagramShape<T extends Diagram> {
  String getName();
  String getCleanName();
  String getLongName();
  int getStartX();
  void setStartX(int startX);
  int getStartY();
  void setStartY(int startY);
  int getCenterX();
  int getCenterY();
  int getEndX();
  int getEndY();
  State getState();
  void setState(State state);

    void translate(int diffX, int diffY);
  boolean over(int x, int y);
  boolean notSelected();
  void stickToGrid();

    boolean inside(Rectangle selectionArea);

    void draw(Graphics2D g2d);

    java.util.Optional<? extends JPanel> getForm(T panel);

    void toggleDisabled();
}
