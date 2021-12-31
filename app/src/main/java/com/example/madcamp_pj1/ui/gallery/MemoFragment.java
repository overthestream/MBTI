package com.example.madcamp_pj1.ui.gallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.madcamp_pj1.R;
import com.example.madcamp_pj1.ui.method.OnBackPressedListener;
import com.example.madcamp_pj1.ui.method.getOCR;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class MemoFragment extends Fragment implements OnBackPressedListener {

    @Override
    public void onBackPressed() {
        backToParentFragment();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_memo, container, false);
        ImageView image = rootView.findViewById(R.id.big_image);
        int position = getArguments().getInt("position");
        File filesDir = getActivity().getFilesDir();
        File file = new File(filesDir, "img" + position + ".png");
        File textFile = new File(filesDir, "img" + position + ".txt");
        File memoFile = new File(filesDir, "memo" + position + ".txt");

        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        image.setImageBitmap(bitmap);
        Button APIButton = rootView.findViewById(R.id.api_button);
        EditText editText = rootView.findViewById(R.id.memo_edit_text);

        if(textFile.exists()){
            APIButton.setVisibility(View.INVISIBLE);
            APIButton.setClickable(false);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(textFile.getPath()), StandardCharsets.UTF_8),500);

                String string = new String();
                String str;

                while ((str = br.readLine()) != null) {
                    Log.e("STRING!", str);
                    string = string.concat(str.concat("\n"));
                }
                br.close();

                TextView text = rootView.findViewById(R.id.OCR_text);
                text.setVisibility(View.VISIBLE);
                text.setText(string);

                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                );
                layoutParams.topToBottom = R.id.OCR_text;
                editText.setLayoutParams(layoutParams);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {
            Button loading = rootView.findViewById(R.id.loading_button);

            APIButton.setOnClickListener(v -> {
                APIButton.setClickable(false);
                APIButton.setVisibility(View.INVISIBLE);

                loading.setVisibility(View.VISIBLE);

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    getOCR getocr = new getOCR(getActivity());
                    String OCRResult = getocr.callCloudVision(bitmap);
                    if (OCRResult == null) {
                        OCRResult = "글귀가 검색되지 않습니다.";
                    }

                    loading.setVisibility(View.INVISIBLE);

                    TextView text = rootView.findViewById(R.id.OCR_text);
                    text.setVisibility(View.VISIBLE);
                    text.setText(OCRResult);

                    ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    );
                    layoutParams.topToBottom = R.id.OCR_text;
                    editText.setLayoutParams(layoutParams);

                    try {
                        File textSave = new File(filesDir, "img" + position + ".txt");
                        FileWriter fw = new FileWriter(textSave);
                        BufferedWriter bw =new BufferedWriter(new OutputStreamWriter(new FileOutputStream(textSave.getPath()), StandardCharsets.UTF_8),500);
                        bw.write(OCRResult);
                        bw.close();
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }, 100);


            });
        }

        if(memoFile.exists()){
            try {
                BufferedReader memoBr = null;
                memoBr = new BufferedReader(new InputStreamReader(new FileInputStream(memoFile.getPath()), StandardCharsets.UTF_8),500);

                String string = new String();
                String str;

                while ((str = memoBr.readLine()) != null) {
                    Log.e("STRING!", str);
                    string = string.concat(str.concat("\n"));
                }
                memoBr.close();
                editText.setText(string);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Button deleteButton = rootView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> {
            file.delete();
            if(textFile.exists()) {
                textFile.delete();
            }
            int count = position + 1;
            while (true){
                File oldFile = new File(filesDir, "img" + count + ".png");
                File oldText = new File(filesDir, "img" + count + ".txt");
                if(oldText.exists()){
                    File newText = new File(filesDir, "img" + (count - 1) + ".txt");
                    oldText.renameTo(newText);
                }
                if(oldFile.exists()){
                    File newName = new File(filesDir, "img" + (count - 1) + ".png");
                    oldFile.renameTo(newName);
                    count++;
                }
                else break;
            }
            backAndRefreshParentFragment(position);
        });

        Button confirmButton = rootView.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(v -> {
            if(memoFile.exists())
                memoFile.delete();
            try {
                String memo = editText.getText().toString();
                File textSave = new File(filesDir, "memo" + position + ".txt");
                FileWriter fw = new FileWriter(textSave);
                BufferedWriter bw =new BufferedWriter(new OutputStreamWriter(new FileOutputStream(textSave.getPath()), StandardCharsets.UTF_8),500);
                bw.write(memo);
                bw.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            backToParentFragment();
        });

        return rootView;
    }
    private void backToParentFragment() {
        getParentFragmentManager()
               .beginTransaction()
               .remove(MemoFragment.this)
               .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
               .commit();
    }
    private void backAndRefreshParentFragment(int position) {
        List<Fragment> fragmentList =  getParentFragmentManager().getFragments();
        for(Fragment fragment : fragmentList)
            if(fragment.getClass() == GalleryFragment.class){
                ((GalleryFragment) fragment).removeItemInAdapter(position);
                break;
            }
        getParentFragmentManager()
                .beginTransaction()
                .remove(MemoFragment.this)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

}
