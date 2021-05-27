package com.example.chatapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.CheckedOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity
{
    private CircleImageView add_member;
    private TextView userName, userLastSeen;
    private DatabaseReference RootRef;
    private Toolbar mToolbar;
    private ImageView backButton;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    final List<Messages> messagesModel = new ArrayList<>();
    private GroupMessageAdapter adapter;
    private final Hashtable<String , Boolean> messagesListHash = new Hashtable<String , Boolean>();
    private Uri fileUri;
    private FirebaseAuth mAuth;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView GroupMessagesList;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef;
    private String checker = "",myUrl="";
    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime,messageSenderID,messagePushID;
    private ImageButton SendFilesButton;
    private ProgressDialog loadingBar;
    private StorageTask uploadTask;
    private Toolbar ChatToolBar;
    boolean adminchecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);



        currentGroupName = getIntent().getExtras().get("groupName").toString();
        System.out.println(currentGroupName);
       // Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();


        mAuth = FirebaseAuth.getInstance();

        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        RootRef = FirebaseDatabase.getInstance().getReference();



        InitializeFields();
        userName.setText(currentGroupName);


        GetUserInfo();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        add_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(adminchecker){
                    Intent chatIntent = new Intent(getApplicationContext(),GroupMemberAddActivity.class);
                    chatIntent.putExtra("S", currentGroupName);
                    startActivity(chatIntent);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Group Admin Can Add Member",Toast.LENGTH_SHORT).show();

                }


            }
        });
        checkAdmin(currentGroupName,currentUserID);
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SaveMessageInfoToDatabase();

                userMessageInput.setText("");


            }
        });

        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "Pdf Files",
                                "MS Word Files"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                builder.setTitle("Select the File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0)
                        {
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Image"),443);
                        }
                        if(i==1)
                        {
                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Select PDF"),443);
                        }
                        if(i==2)
                        {
                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent,"Select MSWORD FILE"),443);
                        }
                    }
                });
                builder.show();
            }
        });






    }

    void checkAdmin(String currentGroupName, String currentUserID)
    {
            RootRef.child("GroupsMembers").child(currentGroupName).child("admin").addValueEventListener(new ValueEventListener()  {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {

                    if(dataSnapshot.exists())
                    {
                        String adminID = dataSnapshot.getValue(String.class);
                        System.out.println("TRE"+" "+adminID+" "+currentUserID);
                        if(!adminID.contains(currentUserID) )
                        {
                            System.out.println("inside");
                            add_member.setBackgroundColor(Color.argb(1,169,169,169));
                            add_member.setClickable(false);
                            add_member.setOnLongClickListener(null);
                            adminchecker = false;
                        }
                        else
                        {
                            adminchecker = true;
                        }
                    }



                    //System.out.println("i: "+  messages.getType() + " " + messages.getMessageID());


                }



                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
    }

    @Override
    protected void onStart()
    {
     super.onStart();
//
        System.out.println(GroupNameRef);
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                System.out.println("GroupReference = " + GroupNameRef);
                Messages messages = dataSnapshot.getValue(Messages.class);
                if(!messagesListHash.containsKey(messages.getMessageID()))
                {
                    messagesListHash.put(messages.getMessageID(),true);
                    System.out.println(messages.getMessage());


                    messagesModel.add(messages);

                    adapter.notifyDataSetChanged();

                    GroupMessagesList.smoothScrollToPosition(GroupMessagesList.getAdapter().getItemCount());

                }



            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                Messages messages = dataSnapshot.getValue(Messages.class);
                System.out.println("Data Changed: " + messages.getMessageID());
                for(int z =0 ;z<messagesModel.size();z++)
                {
                    System.out.println(messagesModel.get(z).getMessageID());
                }

                //System.out.println("i: "+  messages.getType() + " " + messages.getMessageID());
                if(messagesListHash.containsKey(messages.getMessageID()))
                {
                    for(int z =0 ;z<messagesModel.size();z++)
                    {
                        
                        if(messagesModel.get(z).getMessageID().equals(messages.getMessageID()) )
                        {
                            //System.out.println(messagesList.get(z).getMessage()+"!="+messages.getMessage());
                            if(!messagesModel.get(z).getMessage().equals(messages.getMessage()))
                            {
                                //System.out.println("here");
                                messagesModel.set(z,messages);
                            }
                            break;
                        }
                    }
                    adapter.notifyDataSetChanged();

                    GroupMessagesList.smoothScrollToPosition(GroupMessagesList.getAdapter().getItemCount());
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void InitializeFields()
    {


        ChatToolBar = (Toolbar) findViewById(R.id.groupchat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
       // actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        loadingBar = new ProgressDialog(this);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.group_chat_bar, null);
        actionBar.setCustomView(actionBarView);



        userName = (TextView) findViewById(R.id.custom_profile_name);


        backButton = (ImageView) findViewById(R.id.back_button);
        //mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
      // setSupportActionBar(mToolbar);
//       getSupportActionBar().setTitle(currentGroupName);
        add_member = (CircleImageView) findViewById(R.id.add_groupmember);
        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        userMessageInput = (EditText) findViewById(R.id.input_message);
//        displayTextMessages = (TextView) findViewById(R.id.group_chat_text_display);
//        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);
        adapter = new GroupMessageAdapter(messagesModel);
        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);

        GroupMessagesList = (RecyclerView) findViewById(R.id.group_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        GroupMessagesList.setLayoutManager(linearLayoutManager);
        GroupMessagesList.setAdapter(adapter);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("heelo");
        if(requestCode == 443 && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            System.out.println("heelo1");
            //loadingBar.setTitle("Sending File");
            //loadingBar.setMessage("Please wait, we are sending file....");
            //loadingBar.setCanceledOnTouchOutside(false);

            fileUri = data.getData();
            System.out.println(fileUri);

            if(!checker.equals("image"))
            {
//                Calendar calForDate = Calendar.getInstance();
//                SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
//                currentDate = currentDateFormat.format(calForDate.getTime());
//
//                Calendar calForTime = Calendar.getInstance();
//                SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
//                currentTime = currentTimeFormat.format(calForTime.getTime());
//
//                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
//                String messagekEY = GroupNameRef.push().getKey();
//
//
//
//                 messagePushID = messagekEY;
//
//
//                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);
//                uploadTask = filePath.putFile(fileUri);
//                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                        if(task.isSuccessful())
//                        {
//                            Uri downloadUrl = task.getResult();
//                            myUrl = downloadUrl.toString();
//                            //GroupMessageKeyRef = GroupNameRef.child(messagekEY);
//                            GroupMessageKeyRef = GroupNameRef.child(messagekEY);
//                            Map messageTextBody = new HashMap();
//                            messageTextBody.put("message", myUrl);
//                            messageTextBody.put("name",fileUri.getLastPathSegment());
//                            messageTextBody.put("type", checker);
//                            messageTextBody.put("from", currentUserID);
//                            messageTextBody.put("to", currentGroupName);
//                            messageTextBody.put("messageID", messagePushID);
//                            messageTextBody.put("time", currentTime);
//                            messageTextBody.put("date", currentDate);
//
//
//
//                            GroupMessageKeyRef.updateChildren(messageTextBody);
//                            //loadingBar.dismiss();
//                        }
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                       // loadingBar.dismiss();
//                        Toast.makeText(GroupChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                        double p = (100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
//                        //loadingBar.setMessage((int)p + "% Uploading...");
//
//                    }
//                });

                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
                currentDate = currentDateFormat.format(calForDate.getTime());

                Calendar calForTime = Calendar.getInstance();
                SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
                currentTime = currentTimeFormat.format(calForTime.getTime());
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");


                String messagekEY = GroupNameRef.push().getKey();

                messagePushID = messagekEY;

                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();
                        System.out.println("message"+myUrl);
                        System.out.println("name"+fileUri.getLastPathSegment());
                        System.out.println("type"+checker);
                        System.out.println("from"+currentUserID);
                        System.out.println("to"+currentGroupName);
                        System.out.println("messageID"+messagePushID);
                        System.out.println("time"+currentTime);
                        System.out.println("date"+currentDate);
                        //GroupMessageKeyRef = GroupNameRef.child(messagekEY);
                        GroupMessageKeyRef = GroupNameRef.child(messagekEY);
                        System.out.println("group"+GroupMessageKeyRef);

                        Map messageTextBody = new HashMap();
                        messageTextBody.put("message", myUrl);
                        messageTextBody.put("name",fileUri.getLastPathSegment());
                        messageTextBody.put("type", checker);
                        messageTextBody.put("from", currentUserID);
                        messageTextBody.put("to", currentGroupName);
                        messageTextBody.put("messageID", messagePushID);
                        messageTextBody.put("time", currentTime);
                        messageTextBody.put("date", currentDate);



                        GroupMessageKeyRef.updateChildren(messageTextBody);
                    }
                });

            }
            else if(checker.equals("image"))
            {
                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
                currentDate = currentDateFormat.format(calForDate.getTime());

                Calendar calForTime = Calendar.getInstance();
                SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
                currentTime = currentTimeFormat.format(calForTime.getTime());
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");


                String messagekEY = GroupNameRef.push().getKey();

                 messagePushID = messagekEY;

                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");

                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();
                        System.out.println("message"+myUrl);
                        System.out.println("name"+fileUri.getLastPathSegment());
                        System.out.println("type"+checker);
                        System.out.println("from"+currentUserID);
                        System.out.println("to"+currentGroupName);
                        System.out.println("messageID"+messagePushID);
                        System.out.println("time"+currentTime);
                        System.out.println("date"+currentDate);
                        //GroupMessageKeyRef = GroupNameRef.child(messagekEY);
                        GroupMessageKeyRef = GroupNameRef.child(messagekEY);
                        System.out.println("group"+GroupMessageKeyRef);

                        Map messageTextBody = new HashMap();
                        messageTextBody.put("message", myUrl);
                        messageTextBody.put("name",fileUri.getLastPathSegment());
                        messageTextBody.put("type", checker);
                        messageTextBody.put("from", currentUserID);
                        messageTextBody.put("to", currentGroupName);
                        messageTextBody.put("messageID", messagePushID);
                        messageTextBody.put("time", currentTime);
                        messageTextBody.put("date", currentDate);



                        GroupMessageKeyRef.updateChildren(messageTextBody);
                    }
                });

            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText(this,"Nothing Selected, Error",Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void GetUserInfo()
    {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    private void SaveMessageInfoToDatabase()
    {
        String message = userMessageInput.getText().toString();
        String messagekEY = GroupNameRef.push().getKey();

        if (TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
        }

        else
        {
            //String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());
            //GroupMessageKeyRef = GroupNameRef.child(messagekEY);
            GroupMessageKeyRef = GroupNameRef.child(messagekEY);
            System.out.println(messagekEY);
            messagePushID = messagekEY;
            System.out.println("SenderID" + GroupMessageKeyRef);

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", message);
            messageTextBody.put("type", "text");
            messageTextBody.put("name", currentUserName);
            messageTextBody.put("from", currentUserID);
            messageTextBody.put("to", currentGroupName);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", currentTime);
            messageTextBody.put("date", currentDate);



            GroupMessageKeyRef.updateChildren(messageTextBody).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(GroupChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    userMessageInput.setText("");
                }
            });
        }
//        else
//        {
//
//
//
//            HashMap<String, Object> groupMessageKey = new HashMap<>();
//            GroupNameRef.updateChildren(groupMessageKey);
//
//            GroupMessageKeyRef = GroupNameRef.child(messagekEY);
//
//            HashMap<String, Object> messageInfoMap = new HashMap<>();
//                messageInfoMap.put("name", currentUserName);
//                messageInfoMap.put("message", message);
//                messageInfoMap.put("date", currentDate);
//                messageInfoMap.put("time", currentTime);
//            GroupMessageKeyRef.updateChildren(messageInfoMap);
//        }
    }



    private void DisplayMessages(DataSnapshot dataSnapshot)
    {
//        Iterator iterator = dataSnapshot.getChildren().iterator();
//
//        while(iterator.hasNext())
//        {
//            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
//            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
//            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
//            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();
//
//            displayTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "     " + chatDate + "\n\n\n");
//            displayTextMessages.setBackgroundResource(R.drawable.receiver_messages_layout);
//            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
//        }
    }
}
