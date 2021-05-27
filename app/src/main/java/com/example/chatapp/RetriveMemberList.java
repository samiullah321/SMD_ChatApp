//package com.example.chatapp;

import android.os.AsyncTask;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

//public class RetriveMemberList extends AsyncTask<String,Integer, List<String>> {
//    DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
//
//    List<String> memberList = new ArrayList<String>();
////    protected List<String> doInBackground(String... groupName) {
//////        System.out.println("data1"+);
//////        System.out.println("G1"+groupName + "G2" + groupName[0] );
//////        RootRef.child("GroupsMembers").child(groupName[0]).child("members")
//////                .addValueEventListener(new ValueEventListener()  {
//////                    @Override
//////                    public void onDataChange(DataSnapshot dataSnapshot)
//////                    {
//////                        System.out.println("data1"+dataSnapshot);
//////                        if(dataSnapshot.exists())
//////                        {
//////                            GenericTypeIndicator<ArrayList<String>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<String>>() {};
//////                            System.out.println("here");
//////                            memberList = (ArrayList<String>)dataSnapshot.getValue(genericTypeIndicator);
//////                            System.out.println("ABC"+memberList.toString());
//////                        }
//////
//////
//////
//////                        //System.out.println("i: "+  messages.getType() + " " + messages.getMessageID());
//////
//////
//////                    }
//////
//////
//////
//////                    @Override
//////                    public void onCancelled(DatabaseError databaseError) {
//////
//////                    }
//////
//////                });
//////
//////
//////        return memberList;
////    }
//
//    protected void onProgressUpdate(Integer... progress) {
//        setProgressPercent(progress[0]);
//    }
//
//    private void setProgressPercent(Integer progress) {
//    }
//
//
//
//
//
//
//}
