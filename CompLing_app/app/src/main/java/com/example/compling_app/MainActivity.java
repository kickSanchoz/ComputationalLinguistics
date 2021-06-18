package com.example.compling_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);
    }

    //Очистить фокус с поля ввода по нажатии на пустое место экрана
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) { //Нажатие пальцем по экрану
            View v = getCurrentFocus(); //Получить текущий фокус
            if ( v instanceof AutoCompleteTextView) {   //Установлен ли фокус в поле AutoCompleteTextView
                Rect outRect = new Rect();  //Создание экзампляра класса прямоугольника
                v.getGlobalVisibleRect(outRect);    //Получить координаты углов прямоугольника поля EditText
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {    //Если коориднаты места нажатия не находятся в координатах прямоугольника EditText, то
                    v.clearFocus(); //Очисть фокус с поля EditText
                }
            }
            else if ( v instanceof EditText) {
                Rect outRect = new Rect();  //Создание экзампляра класса прямоугольника
                v.getGlobalVisibleRect(outRect);    //Получить координаты углов прямоугольника поля EditText
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {    //Если коориднаты места нажатия не находятся в координатах прямоугольника EditText, то
                    v.clearFocus(); //Очисть фокус с поля EditText
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0); //Т.к. фокус потерян, то убрать клавиатуру
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}