package com.github.devnied.emvnfccard.fragment.viewPager;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.devnied.emvnfccard.R;

public class CardDetailFragment extends Fragment {

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.card_detail, container, false);
	}

}
