package ngohoanglong.com.awesomemangareader.activity;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ngohoanglong.com.awesomemangareader.AppState;
import ngohoanglong.com.awesomemangareader.MangaReaderApp;
import ngohoanglong.com.awesomemangareader.R;
import ngohoanglong.com.awesomemangareader.activity.fragment.UsingAsynTaskChapterFragment;
import ngohoanglong.com.awesomemangareader.activity.fragment.UsingRxjavaChapterFragment;
import ngohoanglong.com.awesomemangareader.activity.fragment.UsingServiceChapterFragment;
import ngohoanglong.com.awesomemangareader.model.Chapter;
import ngohoanglong.com.awesomemangareader.utils.ImageUtils;


public class BrowserActivity extends AppCompatActivity {
    private static final String TAG = "BrowserActivity";
    int currentPagePosittion = 0;

    RecyclerView rvChapterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        rvChapterList = (RecyclerView) findViewById(R.id.rvPageList);
        rvChapterList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        new MyAsynTask().execute();

    }

    class MyAsynTask extends AsyncTask<String, String, List<Chapter>> {

        @Override
        protected List<Chapter> doInBackground(String... params) {

            return AppState.getFile(BrowserActivity.this);
        }

        @Override
        protected void onPostExecute(List<Chapter> chapters) {
            super.onPostExecute(chapters);
            AppState.chapters.addAll(chapters);
            currentPagePosittion = 0;
            rvChapterList.setAdapter(new PageListAdapter());

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            for (Chapter chapter : AppState.chapters
                    ) {
                String tag = chapter.getTitle();
                if (getSupportFragmentManager().findFragmentByTag(tag) == null) {
                    ft.add(R.id.rvPageContent, getFragment(), tag);

                } else {
                    Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
                    ft.attach(f);
                }

            }

            ft.commit();
        }
    }

    List<Class> classes;

    {
        classes = new ArrayList<>();
        classes.add(UsingRxjavaChapterFragment.class);
        classes.add(UsingAsynTaskChapterFragment.class);
        classes.add(UsingServiceChapterFragment.class);
    }

    private int checkFragment(Fragment f) {
        if (f instanceof UsingRxjavaChapterFragment) {
            return 0;
        }
        if (f instanceof UsingAsynTaskChapterFragment) {
            return 1;
        }
        if (f instanceof UsingServiceChapterFragment) {
            return 1;
        }
        return currentPagePosittion;
    }

    private Fragment getFragment() {
        switch (useType) {
            case 0:
                return UsingRxjavaChapterFragment.newInstance(currentPagePosittion);
            case 1:
                return UsingAsynTaskChapterFragment.newInstance(currentPagePosittion);
            case 2:
                return UsingServiceChapterFragment.newInstance(AppState.chapters.get(currentPagePosittion));
        }
        return null;
    }


    private class PageListAdapter extends RecyclerView.Adapter<PageListAdapter.ViewHolder> {


        public PageListAdapter() {

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_page_horizontal, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            if (holder != null) {
                holder.tvPageName.setText(AppState.chapters.get(position).getTitle());
                if (position == currentPagePosittion) {
                    holder.cvWrap.setCardElevation(10);
                    holder.cvWrap.setCardBackgroundColor(holder.itemView.getResources().getColor(R.color.aqua));
                    holder.tvPageName.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
                } else {
                    holder.cvWrap.setCardElevation(0);
                    holder.cvWrap.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.white));
                    holder.tvPageName.setTextColor(holder.itemView.getResources().getColor(R.color.aqua));
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentPagePosittion = position;
//                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//
//                        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
//                            ft.add(R.id.rvPageContent, getFragment(), TAG);
//                            ft.addToBackStack(null);
//                            ft.commit();
//                        } else {
//                            ft.replace(R.id.rvPageContent, getFragment(), TAG);
//                            ft.addToBackStack(null);
//                            ft.commit();
//                        }

                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                        String tag = AppState.chapters.get(position).getTitle();
                        Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
                        if (f == null ) {
                            ft.add(R.id.rvPageContent, getFragment(), tag);
                        } else {
                            if(currentPagePosittion!=checkFragment(f)){
                                ft.remove(f);
                                Fragment newF = getFragment();
                                ft.replace(R.id.rvPageContent,newF , tag);
                            }else {
                                ft.replace(R.id.rvPageContent,f , tag);
                            }

                        }
                        ft.commit();


                        notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return AppState.chapters.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPageName;
            CardView cvWrap;

            public ViewHolder(View view) {
                super(view);
                tvPageName = (TextView) view.findViewById(R.id.tvPageName);
                cvWrap = (CardView) view.findViewById(R.id.cvWrap);
            }

        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    int useType = 0;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.clearDiskCache:
                MangaReaderApp.deleteAllLocalImages();
                return false;
            case R.id.clearDiskLruCache:
                ImageUtils.clearCache();

                return false;
            case R.id.useRx:
                useType = 0;
                return false;
            case R.id.useAsyn:
                useType = 1;
                return false;
//            case  R.id.useIntentService:
//                useType=2;
//                getActionBar().setTitle("Intent");
//                return false;
        }
        return super.onOptionsItemSelected(item);
    }

}
