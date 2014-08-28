package com.roomorama.caldroid;

import java.util.ArrayList;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * MonthPagerAdapter holds 4 fragments, which provides fragment for current
 * month, previous month and next month. The extra fragment helps for recycle
 * fragments.
 * 
 * @author thomasdao
 * 
 */
public class MonthPagerAdapter extends FragmentPagerAdapter {

	private ArrayList<DateGridFragment> fragments;
	private FragmentManager fm;
	private Integer viewId;

	// Lazily create the fragments
	public ArrayList<DateGridFragment> getFragments() {

		if (fragments == null) {
			fragments = new ArrayList<DateGridFragment>();

			for (int i = 0; i < getCount(); i++) {
				DateGridFragment frag = null;

				if(viewId == null || (frag = findExistingFragment(viewId, i)) == null) {
					frag = new DateGridFragment();
				}

				fragments.add(frag);
			}
		}

		return fragments;
	}

	DateGridFragment findExistingFragment(int viewId, int position) {
		return (DateGridFragment) fm.findFragmentByTag(makeFragmentName(viewId, position));
	}

	public void setFragments(ArrayList<DateGridFragment> fragments) {
		this.fragments = fragments;
	}

	public MonthPagerAdapter(FragmentManager fm) {
		super(fm);
		this.fm = fm;
	}

	public MonthPagerAdapter(FragmentManager fm, Integer viewId) {
		this(fm);

		this.viewId = viewId;
	}

	@Override
	public Fragment getItem(int position) {
		DateGridFragment fragment = getFragments().get(position);
		return fragment;
	}

	@Override
	public int getCount() {
		// We need 4 gridviews for previous month, current month and next month,
		// and 1 extra fragment for fragment recycle
		return CaldroidFragment.NUMBER_OF_PAGES;
	}

	protected String makeFragmentName(int viewId, long id) {
		return "android:switcher:" + viewId + ":" + id;
	}
}
