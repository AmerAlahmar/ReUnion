package com.safaorhan.reunion.activity;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;



import com.google.firebase.firestore.DocumentReference;
import com.google.protobuf.Timestamp;
import com.safaorhan.reunion.FirestoreHelper;
import com.safaorhan.reunion.R;
import com.safaorhan.reunion.model.Conversation;
import com.safaorhan.reunion.model.Message;
import com.safaorhan.reunion.model.User;


import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getSimpleName();

    DocumentReference conversationRef;
    String conversationRefId;
    ImageButton sendMessageImageButton;
    EditText messageEditText;
    RecyclerView messagingRecyclerView;
    User me = new User();
    User opponent = new User();
    ArrayList<Message> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        sendMessageImageButton = findViewById(R.id.sendMessageImageButton);
        messageEditText = findViewById(R.id.messageEditText);
        messagingRecyclerView = findViewById(R.id.messagingRecyclerView);
        conversationRefId = getIntent().getStringExtra(Conversation.CONVERSATION_KEY);

        if (conversationRefId == null || conversationRefId.isEmpty()) {
            Log.e(TAG, "onCreate: Error, You must start activity with conversationID as a String.");
            finish();
        }
        conversationRef = FirestoreHelper.getConversationRefById(conversationRefId);

    }
}