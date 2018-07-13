package com.safaorhan.reunion.activity;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.safaorhan.reunion.FirestoreHelper;
import com.safaorhan.reunion.R;
import com.safaorhan.reunion.adapter.MessageAdapter;
import com.safaorhan.reunion.model.Conversation;
import com.safaorhan.reunion.model.User;

public class MessageActivity extends AppCompatActivity implements MessageAdapter.DataChangingListener {
    private static final String TAG = MessageActivity.class.getSimpleName();
    ImageButton sendMessageImageButton;
    EditText messageEditText;
    RecyclerView messagingRecyclerView;
    MessageAdapter messageAdapter;
    DocumentReference conversationRef;
    DocumentReference opponentUserRef;
    User opponentUser = null;
    private boolean sending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        sendMessageImageButton = findViewById(R.id.sendMessageImageButton);
        messageEditText = findViewById(R.id.messageEditText);
        messagingRecyclerView = findViewById(R.id.messagingRecyclerView);

        String conversationRefPath = getIntent().getStringExtra(Conversation.CONVERSATION_KEY);
        String opponentUserRefPath = getIntent().getStringExtra(User.OPPONENT_KEY);
        if (conversationRefPath == null || conversationRefPath.isEmpty()) {
            Log.e(TAG, "onCreate: Error, You must start activity with conversationRefPath and opponentUserRefPath as a Strings.");
            finish();
        }

        conversationRef = FirebaseFirestore.getInstance().document(conversationRefPath);
        messageAdapter = MessageAdapter.get(conversationRef);
        messageAdapter.setDataChangingListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagingRecyclerView.setLayoutManager(layoutManager);
        messagingRecyclerView.setAdapter(messageAdapter);

        if (opponentUserRefPath == null || opponentUserRefPath.isEmpty()){
            FirestoreHelper.getUserByConversationRef(conversationRef, new FirestoreHelper.GetUserByConversationRefCallback() {
                @Override
                public void onCompleted(User user) {
                    opponentUser = user;
                    //messageAdapter.setUser(opponentUser);
                    setTitle(getString(R.string.userIsMessageSenderHeader).concat(" ").concat(opponentUser.getName()));
                }
            });
        }else {
            opponentUserRef = FirebaseFirestore.getInstance().document(opponentUserRefPath);
            FirestoreHelper.getUserByRef(opponentUserRef, new FirestoreHelper.GetUserByRefCallback() {
                @Override
                public void onCompleted(User user) {
                    opponentUser = user;
                   // messageAdapter.setUser(opponentUser);
                    setTitle(getString(R.string.userIsMessageSenderHeader).concat(" ").concat(opponentUser.getName()));
                }
            });
        }

        sendMessageImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sending && isMessageValid()){
                    sending = true;
                    sendMessage(messageEditText.getText().toString());
                    messageEditText.setText("");
                }
            }
        });

    }

    private void sendMessage(String message) {
        FirestoreHelper.sendMessage(message, conversationRef, new FirestoreHelper.SendMessageCallback() {
            @Override
            public void onMessageSentSuccessfully() {
                sending = false;
            }
        });
    }

    private boolean isMessageValid() {
        if (messageEditText.getText().toString().isEmpty())
            return false;

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        messageAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        messageAdapter.stopListening();
    }

    @Override
    public void doWhenDataChange() {
        if (messageAdapter.getItemCount() > 0)
            messagingRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
    }
}