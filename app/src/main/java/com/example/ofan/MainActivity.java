package com.example.ofan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.ofan.Fragment.DeviceFragment;
import com.example.ofan.Fragment.HomeFragment;
import com.example.ofan.Fragment.TimerFragment;
import com.example.ofan.adapter.ViewPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 mViewPager2;
    private BottomNavigationView mBottomNav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mBottomNav = findViewById(R.id.mBottomNav);
        replaceFragment(new HomeFragment());

        mBottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottomHome:
                        replaceFragment(new HomeFragment());
                        break;
                    case R.id.bottomTimer:
                        replaceFragment(new TimerFragment());
                        break;
                    case R.id.bottomPair:
                       replaceFragment(new DeviceFragment());
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}