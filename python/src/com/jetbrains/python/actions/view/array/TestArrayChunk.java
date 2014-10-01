/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.python.actions.view.array;

/**
 * @author amarch
 */
public class TestArrayChunk extends ArrayChunk {
  public TestArrayChunk(String baseSlice, int rows, int columns, int rOffset, int cOffset) {
    super(baseSlice, rows, columns, rOffset, cOffset);
  }

  @Override
  void fillData(Runnable callback) {
    for (int i = 0; i < getRows(); i++) {
      for (int j = 0; j < getColumns(); j++) {
        data[i][j] = "(" + (i + rOffset) + "," + (j + cOffset) + ")";
      }
    }
    try {
      // wait for debugger connection
      Thread.sleep(400);
      callback.run();
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public int getRows() {
    return rows;
  }

  public int getColumns() {
    return columns;
  }

  public Object[][] getData() {
    return data;
  }

  public String getChunkPresentation() {
    return baseSlice + "[" + rOffset + ":" + (rOffset + rows) + ", " + cOffset + ":" + (cOffset + columns) + "]";
  }
}
