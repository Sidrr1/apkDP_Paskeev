package com.example.appdp;



import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.appdp.R;
import com.example.appdp.DatabaseHelper;
import com.example.appdp.Article;
import com.example.appdp.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class ArticlesFragment extends Fragment {
    private ListView listViewArticles;
    private TextView tvNoArticles;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private List<Article> articles;
    private ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_articles, container, false);

        try {
            dbHelper = DatabaseHelper.getInstance(getActivity());
            sessionManager = new SessionManager(getActivity());

            listViewArticles = view.findViewById(R.id.listViewArticles);
            tvNoArticles = view.findViewById(R.id.tvNoArticles);

            loadArticles();

            listViewArticles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Article article = articles.get(position);
                    Intent intent = new Intent(getActivity(), ArticleDetailActivity.class);
                    intent.putExtra("article_id", article.getId());
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (tvNoArticles != null) {
                tvNoArticles.setText("Ошибка: " + e.getMessage());
                tvNoArticles.setVisibility(View.VISIBLE);
            }
        }

        return view;
    }

    private void loadArticles() {
        try {
            String addictionType = sessionManager.getAddictionType();

            if (addictionType == null || addictionType.isEmpty()) {
                tvNoArticles.setText("Сначала выберите зависимость");
                tvNoArticles.setVisibility(View.VISIBLE);
                listViewArticles.setVisibility(View.GONE);
                return;
            }

            articles = dbHelper.getArticlesByAddictionType(addictionType);

            if (articles == null || articles.isEmpty()) {
                tvNoArticles.setText("Нет статей для выбранной зависимости");
                tvNoArticles.setVisibility(View.VISIBLE);
                listViewArticles.setVisibility(View.GONE);
            } else {
                List<String> titles = new ArrayList<>();
                for (Article article : articles) {
                    titles.add(article.getTitle() + "\n" + article.getDescription());
                }

                adapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_list_item_1, titles);
                listViewArticles.setAdapter(adapter);
                tvNoArticles.setVisibility(View.GONE);
                listViewArticles.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            tvNoArticles.setText("Ошибка загрузки: " + e.getMessage());
            tvNoArticles.setVisibility(View.VISIBLE);
        }
    }
}