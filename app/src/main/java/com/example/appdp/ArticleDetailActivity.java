package com.example.appdp;


import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdp.R;
import com.example.appdp.DatabaseHelper;
import com.example.appdp.Article;

public class ArticleDetailActivity extends AppCompatActivity {
    private TextView tvTitle, tvContent;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        dbHelper = DatabaseHelper.getInstance(this);

        initViews();
        loadArticle();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvArticleTitle);
        tvContent = findViewById(R.id.tvArticleContent);
    }

    private void loadArticle() {
        int articleId = getIntent().getIntExtra("article_id", -1);
        if (articleId != -1) {
            Article article = dbHelper.getArticleById(articleId);
            if (article != null) {
                tvTitle.setText(article.getTitle());
                tvContent.setText(article.getContent());
            }
        }
    }
}