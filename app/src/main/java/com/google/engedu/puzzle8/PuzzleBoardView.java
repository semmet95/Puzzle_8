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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;

import static com.google.engedu.puzzle8.PuzzleBoard.NUM_TILES;

public class PuzzleBoardView extends View {
    public static final int NUM_SHUFFLE_STEPS = 40;
    private Activity activity;
    private PuzzleBoard puzzleBoard;
    private ArrayList<PuzzleBoard> animation;
    private Random random = new Random();

    private boolean isSwapping;
    static int index1, index2, ctr;
    static HashMap<String, PuzzleBoard> stateMap=new HashMap<>();
    private String startKey=null;

    public PuzzleBoardView(Context context) {
        super(context);
        activity = (Activity) context;
        animation = null;
        isSwapping=false;
    }

    public void initialize(Bitmap imageBitmap) {
        stateMap.clear();
        int width = getWidth();
        puzzleBoard = new PuzzleBoard(imageBitmap, width);
        startKey=puzzleBoard.getKey();
        stateMap.put(startKey, puzzleBoard);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (puzzleBoard != null) {
            if (animation != null && animation.size() > 0) {
                puzzleBoard = animation.remove(0);
                puzzleBoard.draw(canvas);
                if (animation.size() == 0) {
                    animation = null;
                    puzzleBoard.reset();
                    Toast toast = Toast.makeText(activity, "Solved! ", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    this.postInvalidateDelayed(500);
                }
            } else if(isSwapping) {
                int y1=index1/NUM_TILES;
                int x1=index1%NUM_TILES;
                int y2=index2/NUM_TILES;
                int x2=index2%NUM_TILES;
                float incx=(x1-x2)/10f;
                float incy=(y1-y2)/10f;
                //Log.e("board :", "calling puzzleboard's draw with index1 = "+index1+" index2 = "+index2);
                puzzleBoard.draw(canvas, x1-(ctr*incx), y1-(ctr*incy), index1);
                --ctr;
                if(ctr==0) {
                    isSwapping = false;
                    postInvalidateDelayed(25);
                }
                else
                    postInvalidateDelayed(25);
            } else {
                puzzleBoard.draw(canvas);
            }
        }
    }

    public void shuffle() {
        if (animation == null && puzzleBoard != null) {
            // Do something. Then:
            for(int i=0;i<NUM_SHUFFLE_STEPS;i++) {
                ArrayList<PuzzleBoard> neighbors=puzzleBoard.neighbours();
                puzzleBoard = neighbors.get(random.nextInt(neighbors.size()));
            }
            puzzleBoard.reset();
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (animation == null && puzzleBoard != null) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (puzzleBoard.click(event.getX(), event.getY())) {
                        isSwapping=true;
                        invalidate();
                        if (puzzleBoard.resolved()) {
                            Toast toast = Toast.makeText(activity, "Congratulations!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        return true;
                    }
            }
        }
        return super.onTouchEvent(event);
    }

    public void solve() {
        HashMap<PuzzleBoard, PuzzleBoard> childParent = new HashMap<>();
        PriorityQueue<PuzzleBoard> queue=new PriorityQueue<>(4, (a, b) -> a.steps-b.steps);
        queue.add(puzzleBoard);
        PuzzleBoard destination=stateMap.get(startKey);
        animation=new ArrayList<>();
        while(!queue.isEmpty()) {
            PuzzleBoard temp=queue.poll();
            if(temp.equals(destination)) {
                while(temp!=null) {
                    animation.add(0, temp);
                    temp=childParent.get(temp);
                    Log.e("solving :", "getting path");
                }
                break;
            }
            for(PuzzleBoard x: temp.neighbours()) {
                Log.e("solving :", "child's steps = "+x.steps);
                if(x.steps<temp.steps) {
                    childParent.put(x, temp);
                    queue.add(x);
                }
            }
        }
        if(animation.size()==0)
            animation=null;
        else
            invalidate();
    }
}
