package org.pytorch.demo.objectdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Recipe> arrayList;
    private DatabaseReference database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Intent intent = getIntent();
        String class1 = intent.getStringExtra("class");

        recyclerView = findViewById(R.id.recyclerView_search);
        recyclerView.setHasFixedSize(true);  //리사이클러 뷰 기존성능 강화
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>();  //Recipe객체를 담은 어레이리스트

        database = FirebaseDatabase.getInstance().getReference();  // 파이어베이스 DB연동

        database.child("cuisine_list").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // firebase의 데이터를 받아오는 곳
                arrayList.clear(); //기존 배열이 존재하지 않도록 초기화
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ // 반복문으로 데이터 리스트 추출
                    if (snapshot.getValue().toString().contains(class1))
                    {
                        Recipe recipe = snapshot.getValue(Recipe.class); //만들어뒀던 Recipe 객체에 데이터를 담는다
                        arrayList.add(recipe);
                    }
                }
                adapter.notifyDataSetChanged(); //리스트 저장 및 새로고침
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // DB를 가져오던 중 에러 발생 시
                Log.e("SearchActivity", String.valueOf(error.toException()));
            }
        });


        adapter = new CustomAdapter(arrayList, this);
        recyclerView.setAdapter(adapter); //리사이클러뷰에 어댑터 연결

    }



}