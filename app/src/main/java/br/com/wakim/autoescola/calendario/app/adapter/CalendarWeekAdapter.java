package br.com.wakim.autoescola.calendario.app.adapter;

import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import br.com.wakim.autoescola.calendario.app.fragment.FragmentSumarioAulasIntervalo;

/**
 * Created by wakim on 07/09/14.
 */
public class CalendarWeekAdapter extends FragmentPagerAdapter {

	public final static int NUMBER_OF_PAGES = 5;

	private ArrayList<FragmentSumarioAulasIntervalo> fragments;

	public ArrayList<FragmentSumarioAulasIntervalo> getFragments() {
		return fragments;
	}

	// Lazily create the fragments
	public ArrayList<FragmentSumarioAulasIntervalo> getFragments(FragmentManager fm, @IdRes int containerId) {
		if (fragments == null) {
			fragments = new ArrayList<FragmentSumarioAulasIntervalo>();

			for (int i = 0; i < getCount(); i++) {

				FragmentSumarioAulasIntervalo existing = (FragmentSumarioAulasIntervalo) fm.findFragmentByTag(makeFragmentName(containerId, i));

				if(existing == null) {
					existing = new FragmentSumarioAulasIntervalo();
				}

				fragments.add(existing);
			}
		}

		return fragments;
	}

	public void setFragments(ArrayList<FragmentSumarioAulasIntervalo> fragments) {
		this.fragments = fragments;
	}

	public CalendarWeekAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		FragmentSumarioAulasIntervalo fragment = getFragments().get(position);
		return fragment;
	}

	@Override
	public int getCount() {
		// We need 4 gridviews for previous month, current month and next month,
		// and 1 extra fragment for fragment recycle
		return NUMBER_OF_PAGES;
	}

	public static String makeFragmentName(int viewId, long id) {
		return "android:switcher:" + viewId + ":" + id;
	}
}
