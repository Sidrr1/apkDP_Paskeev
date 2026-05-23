package com.example.appdp;



import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ArticlesFragment();
            case 1:
                return new ProfileFragment();
            case 2:
                return new PlansFragment();
            case 3:
                return new RemindersListFragment();
            default:
                return new ArticlesFragment();
        }
    }

    @Override
    public int getCount() {
        return 4; // Количество вкладок
    }
}
