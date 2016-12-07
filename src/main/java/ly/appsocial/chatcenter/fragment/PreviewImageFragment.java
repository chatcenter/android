package ly.appsocial.chatcenter.fragment;


import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import ly.appsocial.chatcenter.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreviewImageFragment extends Fragment {

	public String mImagePath;

	public PreviewImageFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View layout = inflater.inflate(R.layout.fragment_preview_image, container, false);

		File image = new File(mImagePath);
		ImageView imageView = (ImageView)layout.findViewById(R.id.image);
		Picasso.with(getContext())
				.load(image)
				.fit()
				.centerInside()
				.into(imageView);

		return layout;
	}

}
