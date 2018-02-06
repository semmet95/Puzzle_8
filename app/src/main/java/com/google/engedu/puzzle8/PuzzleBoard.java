/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.puzzle8;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;


public class PuzzleBoard {

    static int NUM_TILES = 3;
    private static final int[][] NEIGHBOUR_COORDS = {
            { -1, 0 },
            { 1, 0 },
            { 0, -1 },
            { 0, 1 }
    };
    private static final int[] NEIGHBOUR_COORDS_2 = { 1, -1, NUM_TILES, -NUM_TILES };
    private ArrayList<PuzzleTile> tiles;

    int steps;
    //private PuzzleBoard previousBoard;

    PuzzleBoard(Bitmap bitmap, int parentWidth) {
        Bitmap bitmapCopy=Bitmap.createScaledBitmap(bitmap, parentWidth, parentWidth, false);
        tiles=new ArrayList<>(NUM_TILES*NUM_TILES);
        int childWidth=parentWidth/NUM_TILES;
        for(int i=0;i<NUM_TILES;i++)
            for(int j=0;j<NUM_TILES;j++) {
                if(i==NUM_TILES-1&&j==NUM_TILES-1)
                    break;
                Bitmap temp=Bitmap.createBitmap(bitmapCopy, j*childWidth, i*childWidth, childWidth, childWidth);
                tiles.add(new PuzzleTile(temp, i*NUM_TILES+j+1));
            }
        tiles.add(null);
    }

    PuzzleBoard(PuzzleBoard otherBoard) {
        tiles = (ArrayList<PuzzleTile>) otherBoard.tiles.clone();
        steps=otherBoard.steps+1;
        //previousBoard=otherBoard;
    }

    public void reset() {
        // Nothing for now but you may have things to reset once you implement the solver.
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return tiles.equals(((PuzzleBoard) o).tiles);
    }

    public void draw(Canvas canvas) {
        if (tiles == null) {
            return;
        }
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                tile.draw(canvas, i % NUM_TILES, i / NUM_TILES);
            }
        }
    }

    public void draw(Canvas canvas, float x, float y, int index1) {
        if (tiles == null) {
            return;
        }
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null&&i!=index1) {
                tile.draw(canvas, i % NUM_TILES, i / NUM_TILES);
            }
        }
        //Log.e("board :", "drawing at x = "+x+" y = "+y);
        PuzzleTile tile=tiles.get(index1);
        tile.draw(canvas, x, y);
    }

    public boolean click(float x, float y) {
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                if (tile.isClicked(x, y, i % NUM_TILES, i / NUM_TILES)) {
                    return tryMoving(i % NUM_TILES, i / NUM_TILES);
                }
            }
        }
        return false;
    }

    private boolean tryMoving(int tileX, int tileY) {
        for (int[] delta : NEIGHBOUR_COORDS) {
            int nullX = tileX + delta[0];
            int nullY = tileY + delta[1];
            if (nullX >= 0 && nullX < NUM_TILES && nullY >= 0 && nullY < NUM_TILES &&
                    tiles.get(XYtoIndex(nullX, nullY)) == null) {
                swapTiles(XYtoIndex(nullX, nullY), XYtoIndex(tileX, tileY));
                return true;
            }

        }
        return false;
    }

    public boolean resolved() {
        for (int i = 0; i < NUM_TILES * NUM_TILES - 1; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile == null || tile.getNumber() != i+1)
                return false;
        }
        return true;
    }

    private int XYtoIndex(int x, int y) {
        return x + y * NUM_TILES;
    }

    protected void swapTiles(int i, int j) {
        PuzzleBoardView.index1=i;
        PuzzleBoardView.index2=j;
        PuzzleBoardView.ctr=10;
        //Log.e("swapTiles :", "i = "+i+" j = "+j+" index 1 = "+PuzzleBoardView.index1+" index 2 = "+PuzzleBoardView.index2);
        PuzzleTile temp = tiles.get(i);
        tiles.set(i, tiles.get(j));
        tiles.set(j, temp);
    }

    public ArrayList<PuzzleBoard> neighbours() {
        int blankPos=15;
        for(int i=0;i<NUM_TILES*NUM_TILES;i++)
            if(tiles.get(i)==null) {
                blankPos = i + 1;
                break;
            }
        ArrayList<PuzzleBoard> neighbors=new ArrayList<>();
        for(int i=0;i<4;i++) {
            int neighs=blankPos+NEIGHBOUR_COORDS_2[i];
            if(neighs>0&&neighs<=NUM_TILES*NUM_TILES) {
                PuzzleBoard copy = new PuzzleBoard(this);
                copy.swapTiles(blankPos - 1, neighs - 1);
                String key=copy.getKey();
                PuzzleBoard temp=PuzzleBoardView.stateMap.get(copy.getKey());
                if(temp==null) {
                    PuzzleBoardView.stateMap.put(key, copy);
                    neighbors.add(copy);
                }
                else
                    neighbors.add(temp);
            }
        }
        return neighbors;
    }

    String getKey() {
        StringBuilder key=new StringBuilder();
        for(PuzzleTile x: tiles) {
            if(x==null)
                key.append("a");
            else
                key.append(x.getNumber());
        }
        return key.toString();
    }

    public int priority() {
        return 0;
    }

}
