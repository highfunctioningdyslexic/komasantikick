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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class KeepaliveThread extends Thread {

    private static final String LOG_TAG = "KeepaliveThread";

    private static Random sRandom = new Random();

    private Context mContext;

    public KeepaliveThread(Context context) {
        mContext = context;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (mContext == null) {
                    break;
                }

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                boolean active = sharedPreferences.getBoolean(Config.PREFERENCE_ACTIVE, false);
                String komasSceleCourseUrl = sharedPreferences.getString(Config.PREFERENCE_KOMAS_SCELE_COURSE_URL, null);
                String username = sharedPreferences.getString(Config.PREFERENCE_USERNAME, null);
                String password = sharedPreferences.getString(Config.PREFERENCE_PASSWORD, null);

                if (!active || komasSceleCourseUrl == null || username == null || password == null) {
                    break;
                }

                // get cookies
                Map<String, String> cookies = Jsoup.connect("https://scele.cs.ui.ac.id/")
                        .execute()
                        .cookies();

                // login
                Map<String, String> data = new HashMap<>();
                data.put("username", username);
                data.put("password", password);
                cookies = Jsoup.connect("https://scele.cs.ui.ac.id/login/index.php?authldap_skipntlmsso=1")
                        .method(Connection.Method.POST)
                        .cookies(cookies)
                        .data(data)
                        .execute()
                        .cookies();

                // open komas scele course url
                Connection.Response response = Jsoup.connect(komasSceleCourseUrl)
                        .followRedirects(false)
                        .cookies(cookies)
                        .execute();

                if (response.statusCode() == 200) {
                    Log.i(LOG_TAG, "Keepalive success");

                    Thread.sleep(sRandom.nextInt(60000) + 30000); // random sleep between 30 and 90 seconds

                    // open komas scele course url
                    Jsoup.connect(komasSceleCourseUrl)
                            .cookies(cookies)
                            .execute();

                    Thread.sleep(sRandom.nextInt(86400000) + 43200000); // random sleep between 12 and 36 hours
                } else {
                    Log.e(LOG_TAG, "Keepalive error: " + response.statusCode() + " " + response.statusMessage());
                    sendErrorNotification();
                    Thread.sleep(3600000); // try again in an hour if failed
                }
            } catch (Throwable t0) {
                StringWriter stackTraceStringWriter = new StringWriter();
                t0.printStackTrace(new PrintWriter(stackTraceStringWriter));
                Log.e(LOG_TAG, "Keepalive error\n" + stackTraceStringWriter.toString());
                sendErrorNotification();
                try {
                    Thread.sleep(3600000); // try again in an hour if failed
                } catch (Throwable t1) {
                    stackTraceStringWriter = new StringWriter();
                    t1.printStackTrace(new PrintWriter(stackTraceStringWriter));
                    Log.e(LOG_TAG, "An error occurred\n" + stackTraceStringWriter.toString());
                }
            }
        }
    }

    private void sendErrorNotification() {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext, Config.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(mContext.getString(R.string.komas_antikick_error))
                .setContentText(mContext.getString(R.string.retrying_in_an_hour))
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                .setContentIntent(PendingIntent.getActivity(mContext, 0, new Intent(mContext, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));

        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    public void quit() {
        mContext = null;
    }
}
