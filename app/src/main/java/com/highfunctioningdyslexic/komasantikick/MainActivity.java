/*
 * Copyright 2018 highfunctioningdyslexic.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.highfunctioningdyslexic.komasantikick;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextInputLayout komasSceleCourseUrlTextInputLayout = findViewById(R.id.text_input_layout_komas_scele_course_url);
        final TextInputLayout usernameTextInputLayout = findViewById(R.id.text_input_layout_username);
        final TextInputLayout passwordTextInputLayout = findViewById(R.id.text_input_layout_password);
        final EditText komasSceleCourseUrlEditText = findViewById(R.id.edit_text_komas_scele_course_url);
        final EditText usernameEditText = findViewById(R.id.edit_text_username);
        final EditText passwordEditText = findViewById(R.id.edit_text_password);
        final Button activateDeactivateButton = findViewById(R.id.button_activate_deactivate);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        komasSceleCourseUrlEditText.setText(sharedPreferences.getString(Config.PREFERENCE_KOMAS_SCELE_COURSE_URL, Config.DEFAULT_KOMAS_SCELE_COURSE_URL));
        usernameEditText.setText(sharedPreferences.getString(Config.PREFERENCE_USERNAME, null));
        passwordEditText.setText(sharedPreferences.getString(Config.PREFERENCE_PASSWORD, null));

        if (sharedPreferences.getBoolean(Config.PREFERENCE_ACTIVE, false)) {
            komasSceleCourseUrlTextInputLayout.setEnabled(false);
            usernameTextInputLayout.setEnabled(false);
            passwordTextInputLayout.setEnabled(false);
            activateDeactivateButton.setText(R.string.deactivate);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } else {
            komasSceleCourseUrlTextInputLayout.setEnabled(true);
            usernameTextInputLayout.setEnabled(true);
            passwordTextInputLayout.setEnabled(true);
            activateDeactivateButton.setText(R.string.activate);
            usernameEditText.requestFocus();
        }

        activateDeactivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                if (!sharedPreferences.getBoolean(Config.PREFERENCE_ACTIVE, false)) {
                    boolean inputOk = true;
                    String komasSceleCourseUrl = komasSceleCourseUrlEditText.getText().toString();
                    String username = usernameEditText.getText().toString();
                    String password = passwordEditText.getText().toString();
                    if (komasSceleCourseUrl.isEmpty()) {
                        komasSceleCourseUrlTextInputLayout.setError(getString(R.string.enter_komas_scele_course_url));
                        inputOk = false;
                    }
                    if (username.isEmpty()) {
                        usernameTextInputLayout.setError(getString(R.string.enter_username));
                        inputOk = false;
                    }
                    if (password.isEmpty()) {
                        passwordTextInputLayout.setError(getString(R.string.enter_password));
                        inputOk = false;
                    }
                    if (inputOk) {
                        komasSceleCourseUrlTextInputLayout.setError(null);
                        usernameTextInputLayout.setError(null);
                        passwordTextInputLayout.setError(null);
                        komasSceleCourseUrlTextInputLayout.setEnabled(false);
                        usernameTextInputLayout.setEnabled(false);
                        passwordTextInputLayout.setEnabled(false);
                        activateDeactivateButton.setText(R.string.deactivate);
                        sharedPreferencesEditor.putString(Config.PREFERENCE_KOMAS_SCELE_COURSE_URL, komasSceleCourseUrlEditText.getText().toString());
                        sharedPreferencesEditor.putString(Config.PREFERENCE_USERNAME, usernameEditText.getText().toString());
                        sharedPreferencesEditor.putString(Config.PREFERENCE_PASSWORD, passwordEditText.getText().toString());
                        sharedPreferencesEditor.putBoolean(Config.PREFERENCE_ACTIVE, true);
                        sharedPreferencesEditor.apply();
                        startService(new Intent(MainActivity.this, KeepaliveService.class));
                    }
                } else {
                    sharedPreferencesEditor.putBoolean(Config.PREFERENCE_ACTIVE, false);
                    sharedPreferencesEditor.commit();
                    komasSceleCourseUrlTextInputLayout.setEnabled(true);
                    usernameTextInputLayout.setEnabled(true);
                    passwordTextInputLayout.setEnabled(true);
                    activateDeactivateButton.setText(R.string.activate);
                }
            }
        });
    }
}
