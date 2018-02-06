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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.util.Random;


public class PuzzleActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap imageBitmap = null;
    private PuzzleBoardView boardView;
    private Bitmap theBitmap=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);
        // This code programmatically adds the PuzzleBoardView to the UI.
        RelativeLayout container = (RelativeLayout) findViewById(R.id.puzzle_container);
        boardView = new PuzzleBoardView(this);
        // Some setup of the view.
        boardView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        container.addView(boardView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_puzzle, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            LayoutInflater inflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout=inflater.inflate(R.layout.difficultypopup, (ViewGroup) findViewById(R.id.popup_parent));
            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            final PopupWindow popupWindow = new PopupWindow(layout, width, height);
            View mainLayout=findViewById(R.id.puzzle_container);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);
            popupWindow.setTouchable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.showAtLocation(mainLayout, Gravity.CENTER_HORIZONTAL, 0, 0);
            Button ok=(Button)layout.findViewById(R.id.okbutton);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RadioGroup nums=(RadioGroup)layout.findViewById(R.id.num_group);
                    int selectedId=nums.getCheckedRadioButtonId();
                    switch (selectedId) {
                        case R.id.nine:
                            PuzzleBoard.NUM_TILES=3;
                            break;
                        case R.id.sixteen:
                            PuzzleBoard.NUM_TILES=4;
                            break;
                        case R.id.twofive:
                            PuzzleBoard.NUM_TILES=5;
                            break;
                        default:
                            Log.e("radio :", "WTF no case matched");
                    }

                    popupWindow.dismiss();
                    if(theBitmap!=null) {
                        boardView.initialize(theBitmap);
                        shuffleImage(null);
                    }
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
        chooser.putExtra(Intent.EXTRA_TITLE, "Choose images source");
        Intent[] intentArray = {takePictureIntent};
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        startActivityForResult(chooser, 11);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==11 && resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            if(data.getData()!=null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                theBitmap=bitmap;
                boardView.initialize(bitmap);
                return;
            }

            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED,
                            MediaStore.Images.ImageColumns.ORIENTATION}, MediaStore.Images.Media.DATE_ADDED,
                    null, "date_added DESC");
            if (cursor != null && cursor.moveToFirst()) {
                Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                String photoPath = uri.toString();
                cursor.close();
                if (photoPath != null) {
                    bitmap = BitmapFactory.decodeFile(photoPath);
                    theBitmap=bitmap;
                    boardView.initialize(bitmap);
                }
            }

            if (bitmap == null) {
                // for safety reasons you can
                // use thumbnail if not retrieved full sized image
                Log.e("camera result :", "couldn't retrieve the bitmap");
            }
            // do whatever you want with the Bitmap ....
        }
    }

    public void shuffleImage(View view) {
        boardView.shuffle();
    }

    public void solve(View view) {
      boardView.solve();

    }
}
