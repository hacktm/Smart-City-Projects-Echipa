package ro.hacktm.cashacab;

import ro.hacktm.cashacab.R;
import ro.hacktm.cashacab.R.id;
import ro.hacktm.cashacab.R.layout;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	EditText username, password;
	Button loginButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		username = (EditText) findViewById(R.id.usernameEditText);
		password = (EditText) findViewById(R.id.passwordEditText);
		loginButton = (Button) findViewById(R.id.buttonLogin);
		username.setText("text");
		password.setText("text");
		loginButton.setFocusableInTouchMode(true);
		loginButton.requestFocus();
		
		loginButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String user = username.getText().toString();
				String pass = password.getText().toString();
				
				if(user.equals("")){
					Toast.makeText(getApplicationContext(), "Please enter your username", Toast.LENGTH_SHORT).show();
					return;
				}else
					if(pass.equals("")){
						Toast.makeText(getApplicationContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
						return;
					}
				
				Intent i = new Intent(LoginActivity.this, MainActivity.class);
				startActivity(i);
				finish();
				
			}
		});
		
		
	}
}
