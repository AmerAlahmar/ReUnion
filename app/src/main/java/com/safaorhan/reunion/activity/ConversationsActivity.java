package com.safaorhan.reunion.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.safaorhan.reunion.R;
import com.safaorhan.reunion.adapter.ConversationAdapter;
import com.safaorhan.reunion.model.Conversation;

public class ConversationsActivity extends AppCompatActivity implements ConversationAdapter.ConversationClickListener, ConversationAdapter.DataChangedListener {
    private static final String TAG = ConversationsActivity.class.getSimpleName();
    RecyclerView recyclerView;
    ConversationAdapter conversationAdapter;
    TextView errorHolderTextView;
    ProgressBar progressBar;
    boolean haveNetwork = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);
        recyclerView = findViewById(R.id.recyclerView);
        errorHolderTextView = findViewById(R.id.errorHolderTextView);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        NetworkInfo networkInfo = getActiveNetworkInfo();

        recyclerView.setVisibility(View.GONE);
        errorHolderTextView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if (networkInfo != null && networkInfo.isConnected()) {
            haveNetwork = true;
            conversationAdapter = ConversationAdapter.get(this, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(conversationAdapter);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            haveNetwork = false;
            errorHolderTextView.setText(getString(R.string.noInternetConnectionError));
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            errorHolderTextView.setVisibility(View.VISIBLE);
        }

        if (haveNetwork)
            conversationAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (haveNetwork)
            conversationAdapter.stopListening();
    }


    @Override
    public void onConversationClick(final DocumentReference conversationRef) {
        Intent intent = new Intent(ConversationsActivity.this, MessageActivity.class);
        intent.putExtra(Conversation.CONVERSATION_KEY, conversationRef.getPath());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conversations, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_people:
                Intent intent = new Intent(this, UsersActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            return connectivityManager.getActiveNetworkInfo();
        } else {
            return null;
        }
    }

    @Override
    public void doWhenDataIsViable() {
        progressBar.setVisibility(View.GONE);
        errorHolderTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void doWhenDataIsEmpty() {
        errorHolderTextView.setText(getString(R.string.conversationActivityEmptyListError));
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        errorHolderTextView.setVisibility(View.VISIBLE);
    }
}