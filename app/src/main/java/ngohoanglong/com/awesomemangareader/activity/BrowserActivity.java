package ngohoanglong.com.awesomemangareader.activity;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ngohoanglong.com.awesomemangareader.AppState;
import ngohoanglong.com.awesomemangareader.R;
import ngohoanglong.com.awesomemangareader.activity.fragment.ChapterFragment;
import ngohoanglong.com.awesomemangareader.model.Chapter;



public class BrowserActivity extends AppCompatActivity {
    private static final String TAG = "BrowserActivity";
    int currentPagePosittion = 0;

    RecyclerView rvChapterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        rvChapterList = (RecyclerView) findViewById(R.id.rvPageList);
        rvChapterList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
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
            AppState.chapeters.addAll(chapters)  ;
            currentPagePosittion = 0;
            rvChapterList.setAdapter(new PageListAdapter());
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
                ft.add(R.id.rvPageContent, ChapterFragment.newInstance(currentPagePosittion), TAG);
                ft.commit();
            }
        }
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
            if(holder!=null){
                holder.tvPageName.setText(AppState.chapeters.get(position).getTitle());
                if(position==currentPagePosittion){
                    holder.cvWrap.setCardElevation(10);
                    holder.cvWrap.setCardBackgroundColor(holder.itemView.getResources().getColor(R.color.aqua));
                    holder.tvPageName.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
                }else {
                    holder.cvWrap.setCardElevation(0);
                    holder.cvWrap.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.white));
                    holder.tvPageName.setTextColor(holder.itemView.getResources().getColor(R.color.aqua));
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentPagePosittion = position;
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
                            ft.add(R.id.rvPageContent, ChapterFragment.newInstance(currentPagePosittion), TAG);
                            ft.addToBackStack(null);
                            ft.commit();
                        }
                        else  {
                            ft.replace(R.id.rvPageContent, ChapterFragment.newInstance(currentPagePosittion), TAG);
                            ft.addToBackStack(null);
                            ft.commit();
                        }
                        notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return AppState.chapeters.size();
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

}
