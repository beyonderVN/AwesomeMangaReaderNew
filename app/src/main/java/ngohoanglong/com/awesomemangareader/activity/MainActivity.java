package ngohoanglong.com.awesomemangareader.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ngohoanglong.com.awesomemangareader.MangaReaderApp;
import ngohoanglong.com.awesomemangareader.R;
import ngohoanglong.com.awesomemangareader.activity.fragment.UsingAsynTaskChapterFragment;
import ngohoanglong.com.awesomemangareader.activity.fragment.UsingRxjavaChapterFragment;
import ngohoanglong.com.awesomemangareader.activity.fragment.UsingServiceChapterFragment;
import ngohoanglong.com.awesomemangareader.model.Chapter;
import ngohoanglong.com.awesomemangareader.model.Image;

public class







MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        MangaReaderApp.width = displayMetrics.widthPixels;


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(1);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }
    List<Chapter> pages = new ArrayList<>();
    ViewPagerAdapter adapter;
    private void setupViewPager(ViewPager viewPager) {


        try {
            pages = new MyAsynTask().execute().get();
            Log.d(TAG, "setupViewPager: "+ pages.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
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

        public void addFragment(Chapter page) {
            List<Class> classes = new ArrayList<>();
            classes.add(UsingAsynTaskChapterFragment.class);
            classes.add(UsingServiceChapterFragment.class);
            classes.add(UsingRxjavaChapterFragment.class);

            switch (mFragmentList.size()%classes.size()) {
                case 0:
//                mFragmentList.add(UsingRxjavaChapterFragment.newInstance(page));
                    break;
//                case 1:
//                    mFragmentList.add(UsingRxjavaChapterFragment.newInstance(page));
//                    break;
//                default:
//                    mFragmentList.add(UsingRxjavaChapterFragment.newInstance(page));
//                    break;
            }

            mFragmentTitleList.add(page.getTitle());
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    private String urlFile = "http://222.255.207.13:5000/fsdownload/0S628zt2x/JSON%20files.zip";
    InputStream getInputStreamFromAssets(Context context, String filename) {
        try {
            InputStream in = context.getAssets().open(filename);
            return in;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Chapter> getFile() {

        List<Chapter> pages = new ArrayList<>();
        try {

            InputStream in = getInputStreamFromAssets(getApplicationContext(), "JSONfiles.zip");
            ZipInputStream zipInputStream = new ZipInputStream(in);
            Log.d(TAG, "getFile: "+in);
            ZipEntry ze = null;

            while ((ze = zipInputStream.getNextEntry()) != null) {
                Log.d(TAG, "getFile: "+ze.getName());

//                FileOutputStream fout = new FileOutputStream(folder +"/"+ ze.getName());
                if (!ze.isDirectory()&&!ze.getName().contains("_")) {

                    List<String> strings = new ArrayList<>();

                    File file = new File(ze.getName());
                    int size = (int) file.length();
                    byte[] bytes = new byte[size];
                    if (zipInputStream.read(bytes, 0, bytes.length) == bytes.length) {
                        InputStreamReader isr = new InputStreamReader(zipInputStream);
                        BufferedReader input = new BufferedReader(isr);
                        String line = "";
                        while ((line = input.readLine()) != null) {
                            line = line.replaceAll("\\[|\\]|\"|,","");
                            if (line.length()>0)strings.add(line);

                        }
                    }
//                    pages.add(new Page(ze.getName(), strings));
                    // do reading, usually loop until end of file reading

                }
                zipInputStream.closeEntry();
            }
            zipInputStream.close();
        } catch (IOException e) {
            Log.d(TAG, "IOException: "+e.getMessage());
            e.printStackTrace();
        }
    return pages;

    }

    class MyAsynTask extends AsyncTask<String, String, List<Chapter>> {

        @Override
        protected List<Chapter> doInBackground(String... params) {

            return getFile();
        }

        @Override
        protected void onPostExecute(List<Chapter> pages) {
            super.onPostExecute(pages);
            for (Chapter page : pages
                    ) {
                ArrayList<Image> strings = new ArrayList<>();
                final int n = page.getImageList().size();
                for (int i = 0; i < page.getImageList().size() && i < n; i++) {
                    strings.add(page.getImageList().get(i));
                }
                adapter.addFragment(new Chapter(page.getTitle(), strings));
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
