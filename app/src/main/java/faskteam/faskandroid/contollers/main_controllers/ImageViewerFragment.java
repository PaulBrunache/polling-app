package faskteam.faskandroid.contollers.main_controllers;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import faskteam.faskandroid.R;


public class ImageViewerFragment extends Fragment {

    private ImageView imageView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_image_viewer, container, false);
        imageView = (ImageView) v.findViewById(R.id.image_viewer_imageview);

        //get image bitmap
        Bundle b = getArguments();
        Bitmap image = (Bitmap) b.get("image");

        imageView.setImageBitmap(image);

        return v;
    }


}
