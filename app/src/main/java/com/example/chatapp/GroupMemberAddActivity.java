package com.example.chatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.sql.SQLOutput;

public class GroupMemberAddActivity extends AppCompatActivity {
    String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        groupName = getIntent().getStringExtra("S");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member_add);


    }

    public String getMyData() {
        return groupName;
    }
}