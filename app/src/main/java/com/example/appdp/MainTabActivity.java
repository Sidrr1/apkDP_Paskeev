package com.example.appdp;



import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appdp.R;

public class MainTabActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

        // Устанавливаем иконки для табов
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_article);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_profile);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_plan);
        tabLayout.getTabAt(3).setIcon(android.R.drawable.ic_menu_edit);

        tabLayout.getTabAt(0).setText("Статьи");
        tabLayout.getTabAt(1).setText("Профиль");
        tabLayout.getTabAt(2).setText("Планы");
        tabLayout.getTabAt(3).setText("Напоминания");
    }
}