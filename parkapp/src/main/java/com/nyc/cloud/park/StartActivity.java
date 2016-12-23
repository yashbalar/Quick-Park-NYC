package com.nyc.cloud.park;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.nyc.cloud.park.View.MaterialProgressBar;
import com.nyc.cloud.park.fragments.LoginFragment;
import com.nyc.cloud.park.fragments.SignupFragment;

import java.util.ArrayList;
import java.util.List;

public class StartActivity extends AppCompatActivity {

    public static final String MyPREFERENCES = "Quick Park" ;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    public static SharedPreferences spreference;
    public static MaterialProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        spreference=getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        String token = spreference.getString(getApplicationContext().getString(R.string.token), "");
        String fullName = spreference.getString(getApplicationContext().getString(R.string.fullName), "");
        if(!token.equals("") && !fullName.equals("")){
            Intent intent = new Intent(StartActivity.this, MapsActivity.class);
            startActivity(intent);
            finish();
        }else {
            progressBar = (MaterialProgressBar) findViewById(R.id.progressbar);

            viewPager = (ViewPager) findViewById(R.id.viewpager);
            setupViewPager(viewPager);

            tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);
            setupTabIcons();
        }
    }

    private void setupTabIcons() {

        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabOne.setText("LOGIN");
        tabLayout.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabTwo.setText("REGISTER");
        tabLayout.getTabAt(1).setCustomView(tabTwo);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new LoginFragment(), "Login");
        adapter.addFragment(new SignupFragment(), "Register");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
