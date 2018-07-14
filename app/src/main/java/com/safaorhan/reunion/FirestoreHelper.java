package com.safaorhan.reunion;

import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.safaorhan.reunion.model.Conversation;
import com.safaorhan.reunion.model.Message;
import com.safaorhan.reunion.model.User;
import java.util.ArrayList;
import java.util.HashMap;

public class FirestoreHelper {
    private static final String TAG = FirestoreHelper.class.getSimpleName();

    public static DocumentReference getMe() {
        String myId = FirebaseAuth
                .getInstance()
                .getUid();
        if (myId != null) {
            return getUsers()
                    .document(myId);
        }
        Log.e(TAG, "getMe: FirebaseAuth returned null!!");
        return null;
    }

    public static CollectionReference getUsers() {
        return FirebaseFirestore.getInstance()
                .collection("users");
    }

    public static void findOrCreateConversation(final DocumentReference opponentRef, final DocumentReferenceCallback callback) {
        getConversations()
                .whereEqualTo(getMe().getId(), true)
                .whereEqualTo(opponentRef.getId(), true)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                if (snapshots == null || snapshots.isEmpty()) {
                    ArrayList<DocumentReference> participants = new ArrayList<>();
                    participants.add(getMe());
                    participants.add(opponentRef);
                    Conversation conversation = new Conversation();
                    conversation.setId(getConversationId(opponentRef));
                    conversation.setParticipants(participants);
                    final DocumentReference conversationRef = getConversationRef(conversation);

                    conversationRef.set(conversation)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    HashMap<String, Object> updateFields = new HashMap<>();
                                    updateFields.put(getMe().getId(), true);
                                    updateFields.put(opponentRef.getId(), true);
                                    conversationRef.update(updateFields)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    callback.onCompleted(conversationRef);
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure", e);
                        }
                    });
                } else {
                    DocumentSnapshot snapshot = snapshots.getDocuments().get(0);
                    Conversation conversation = snapshot.toObject(Conversation.class);
                    if (conversation != null) {
                        conversation.setId(snapshot.getId());
                        callback.onCompleted(getConversationRef(conversation));
                    } else {
                        Log.e(TAG, "findOrCreateConversation: Error Couldn't find or create Conversation!!");
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure", e);
            }
        });
    }

    public static void sendMessage(final String messageText, final DocumentReference conversationRef, final SendMessageCallback callback) {
        final Message message = new Message();
        message.setText(messageText);
        message.setFrom(getMe());
        message.setConversation(conversationRef);
        FirebaseFirestore.getInstance()
                .collection("messages")
                .add(message)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        conversationRef
                                .update("lastMessage", documentReference)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        callback.onMessageSentSuccessfully(message);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding document", e);
                    }
                });
    }

    public static CollectionReference getConversations() {
        return FirebaseFirestore.getInstance().collection("conversations");
    }

    public static DocumentReference getConversationRef(Conversation conversation) {
        return getConversations().document(conversation.getId());
    }

    public static DocumentReference getUserRef(User user) {
        return getUsers().document(user.getId());
    }

    private static String getConversationId(DocumentReference opponentRef) {
        String myId = getMe().getId();
        String opponentId = opponentRef.getId();

        if (myId.equals(opponentId)) {
            throw new IllegalArgumentException("Your opponent cannot be null");
        }

        if (myId.compareTo(opponentId) > 0) {
            return opponentId + "-" + myId;
        } else {
            return myId + "-" + opponentId;
        }
    }

    public static void getUserByRef(DocumentReference userRef, final GetUserByRefCallback callback) {
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null && documentSnapshot.exists()){
                    User user = documentSnapshot.toObject(User.class);
                    user.setId(documentSnapshot.getId());
                    callback.onCompleted(user);
                }
            }
        });
    }

    public static void getUserByConversationRef(DocumentReference conversationRef, final GetUserByConversationRefCallback callback) {
        conversationRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null && documentSnapshot.exists()){
                    Conversation conversation = documentSnapshot.toObject(Conversation.class);
                    conversation.getOpponent().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot != null && documentSnapshot.exists()){
                                User user = documentSnapshot.toObject(User.class);
                                user.setId(documentSnapshot.getId());
                                callback.onCompleted(user);
                            }
                        }
                    });
                }
            }
        });
    }

    public static void messageDelivered(Message message, String messageId) {
        if (!message.getFrom().getId().equals(getMe().getId()))
        FirebaseFirestore.getInstance()
                .collection("messages")
                .document(messageId).update("delivered", true);
    }


    public interface SendMessageCallback {
        void onMessageSentSuccessfully(Message message);
    }

    public interface DocumentReferenceCallback {
        void onCompleted(DocumentReference documentReference);
    }

    public  interface GetUserByRefCallback{
        void onCompleted(User user);
    }

    public  interface GetUserByConversationRefCallback{
        void onCompleted(User user);
    }
}