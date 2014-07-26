package datacomp.co.nz.datalockandroid;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;



/**
 * A simple {@link Fragment} subclass.
 *
 */
public class TabbedPagerFragment extends Fragment {
    public static final String TAG = "TabbedPagerFragment";
    MainActivity mainActivity;

    ViewPager mViewPager;
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;

    public static TabbedPagerFragment newInstance() {
        TabbedPagerFragment fragment = new TabbedPagerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public TabbedPagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tabbed_pager, container, false);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        mainActivity.setTitle("Favourites");

        mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(mainActivity.getSupportFragmentManager());
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) getView().findViewById(R.id.tabs);
        tabs.setIndicatorColorResource(R.color.brand_colour);
        tabs.setViewPager(mViewPager);
    }


    public class DemoCollectionPagerAdapter extends FragmentPagerAdapter {
        public DemoCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int pos) {
            switch(pos) {
                case 0:
                    return UsersFragment.newInstance();
                case 1:
                    return new DemoObjectFragment();
                default:
                    return new DemoObjectFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Users";
                case 1:
                    return "Events";
            }

            return "Error " + (position + 1);
        }
    }

    // Instances of this class are fragments representing a single
// object in our collection.
    public static class DemoObjectFragment extends android.support.v4.app.Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(R.layout.favourites_item, container, false);

            return rootView;
        }
    }

}
