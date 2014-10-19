package com.example.musicplayer.myAdapter;

import com.example.musicplayer.R;
import com.example.musicplayer.R.layout;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MyGridViewAdapter  extends BaseAdapter {
	public ImageButton imageButtons;
	private Context mcontext;
	
	static int currentSelectitem = -1;
	private Integer[] mImageIds = {
			R.drawable.my_local_music_button,
			R.drawable.my_collect_button,
			R.drawable.history_button,
			R.drawable.search_button,
			R.drawable.settings_button,
	};
	
	public MyGridViewAdapter(Context context)
	{
		this.mcontext = context;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mImageIds.length;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ImageView imageView = null;
		if(convertView == null)
		{
			imageView = new ImageView(mcontext);
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE); 
			//LayoutInflater inflater = LayoutInflater.from(mcontext);
			//convertView = inflater.inflate(R.layout.main_grid_view,null);
		}else{  

            imageView = (ImageView) convertView;  

        }
		imageView.setImageResource(mImageIds[position]); 
		return imageView;
	}

}
