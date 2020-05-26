package com.hcmhmt.todoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hcmhmt.todoapp.Classes.Users;

public class ActivityLogin extends AppCompatActivity {

    FirebaseDatabase _db = FirebaseDatabase.getInstance();
    DatabaseReference _ref = _db.getReference().child("Users");

    EditText _username;
    EditText _password;
    Button _signin;
    ProgressBar _pb;
    Context _context;
    Users _user = new Users();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setBackground();

        _context = getApplicationContext();

        _username = findViewById(R.id.et_login_username);
        _password = findViewById(R.id.et_login_password);
        _signin = findViewById(R.id.btn_login_signin);
        _pb = findViewById(R.id.pb_login_loading);

        _signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                formDisabled();
                progressBarActive();
                signinButtonInactive();
                if (isFormEmpty()) {
                    progressBarInactive();
                    formEnabled();
                    signinButtonActive();
                    Toast.makeText(_context, "Lütfen Bütün Alanları Doldurunuz!!", Toast.LENGTH_LONG).show();
                } else {
                    checkInformations();
                }
            }
        });

    }

    private void setBackground() {
        FirebaseDatabase.getInstance().getReference().child("Mode").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue().equals("Dark")){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /** Firebase tarafında mevcut Users tablosu üzerinden sisteme kayıtlı kullanıcılar ile girilen bilgilerin karşılaştırılması */
    private void checkInformations() {

        _ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot _dataRow : dataSnapshot.getChildren()) {
                    String _dataPassword = _dataRow.child("password").getValue().toString();
                    String _dataUsername = _dataRow.child("username").getValue().toString();
                    if(_dataPassword.equals(_user.getPassword()) && _dataUsername.equals(_user.getUsername())){
                        Toast.makeText(_context, "Bilgileriniz Doğrudur. Anasayfaya yönlendiriliyorsunuz...",Toast.LENGTH_LONG).show();
                        startActivityMain();
                    }else{
                        Toast.makeText(_context, "Lütfen Bilgileriniz Kontrol Ediniz..!",Toast.LENGTH_LONG).show();
                        progressBarInactive();
                        formEnabled();
                        signinButtonActive();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(_context, "Bir Hata Oluştu!!", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /** MainActiviy'nin başlatılma işlemi */
    private void startActivityMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /** Gerekli işlemler yapılana kadar kullanıcı etkileşimi için Giriş Yap butonunun inaktif edilmesi */
    private void signinButtonInactive() {
        _signin.setEnabled(false);
    }

    /** Gerekli işlemler yapıldıktan sonra kullanıcı etkileşimi için Giriş Yap butonunun aktif edilmesi */
    private void signinButtonActive() {
        _signin.setEnabled(true);
    }

    /** Gerekli işlemler yapılana kadar ekran da bir loading bar aktif edilmeisi */
    private void progressBarActive() {
        _pb.setVisibility(View.VISIBLE);
    }

    /** Gerekli işlemler yapıldıktan sonra ekran daki loading bar'ın inaktif edilmeisi */
    private void progressBarInactive() {
        _pb.setVisibility(View.INVISIBLE);
    }

    /** Gerekli işlemler yapılana kadar kullanıcı etkileşimi için Kullanıcı Adı ve Şifre kısımlarının inaktif edilmesi */
    private void formDisabled() {
        _username.setEnabled(false);
        _password.setEnabled(false);
    }

    /** Gerekli işlemler yapılana kadar kullanıcı etkileşimi için Kullanıcı Adı ve Şifre kısımlarının aktif edilmesi */
    private void formEnabled() {
        _username.setEnabled(true);
        _password.setEnabled(true);
    }

    /** Kullanıcı Adı ve Şifre bilgilerinin boş olup olmadığının kontrolü */
    /** Gerekli alanlar boş ise true, dolu ise false döndürür */
    private boolean isFormEmpty() {

        String _psw = _password.getText().toString().trim();
        String _uname = _username.getText().toString().trim();

        if (_username.getText().toString().trim().isEmpty() || _password.getText().toString().trim().isEmpty()) {
            return true;
        }
        _user.setPassword(_psw);
        _user.setUsername(_uname);
        return false;
    }
}
