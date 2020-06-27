/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.didyoufeelit;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

//materilal design for progress indicators https://material.io/components/progress-indicators

/* Threads
        To keep things moving (to avoid an unresponsive app), do not block the UI (main) thread
        This means stuff like network requests, number crunching, and audio playback should all be processed on their own background (worker) threads
        This frees the UI so the user can still scroll and tap and do whatever these they need to do while they wait for the other jobs to finish
        Once a background thread is done, there's no need to keep that thread active anymore

    AsyncTask
        We really don't need the full power of threading since all we want to do is run a single task (the http request) on a
        separate thread than the one that's handling events on the UI thread
        Use AsyncTask for a short-lived (a few seconds) one-off task, it has less overhead than a standard Java thread

        To use AsyncTask
        1. create a subclass of AsyncTask
        2. use doInBackground() to perform whatever task you need to do in the background that's going to take some time
            for example, making a call to another web server or running a filter on a photo
        3. use onProgressUpdate() to provide status updates to your app
        4. use onPostExecute() to get the results of the background task

    Inner Classes
        Instead of making a new file for a class, you can make a class an inner class by putting it within another class.

        For example, we want to update the UI in the onPostExecute() of the EarthquakeAsyncTask but all the UI views are part of the
        layout that was set in the MainActivity class. In order to get referenced to those views, the EarthquakeAsyncTaks can be declared
        as an inner class of the MainActivity class. The whole EarthquakeAsyncTask class definition can go inside the MainActivity class
        definition  This means the EarthquakeAsyncTask then holds an implicit reference to the outer MainActivity class and can now access
        any global variables and methods from the enclosing MainActivity class. This also means that an instance of the EarthquakeAsyncTask
        class can only exist within an instance of the MainActivity class (there is no EarthquakeAsyncTask without the enclosing MainActivity).
        In this case, EarthquakeAsyncTask in only ever used in the MainActivity so it can be marked as a private class.

        Inner classes help keep the code condensed and with fewer Java files. They are useful if the two classes are closely related, for
        example, like how the EarthquakeAsyncTask task in only even used within MainActivity.

*/

/**
 * Displays the perceived strength of a single earthquake event based on responses from people who
 * felt the earthquake.
 */
public class MainActivity extends AppCompatActivity {

    /** URL for earthquake data from the USGS dataset */
    private static final String USGS_REQUEST_URL =
            "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2016-01-01&endtime=2016-05-02&minfelt=50&minmagnitude=5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create an {@link AsyncTask} to perform the HTTP request to the given URL on a background thread.
        // When the result is received on the main UI thread, then update the UI.
        EarthquakeAsyncTask task = new EarthquakeAsyncTask();
        task.execute(USGS_REQUEST_URL);
    }

    /**
     * Update the UI with the given earthquake information.
     */
    private void updateUi(Event earthquake) {
        TextView titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(earthquake.title);

        TextView tsunamiTextView = (TextView) findViewById(R.id.number_of_people);
        tsunamiTextView.setText(getString(R.string.num_people_felt_it, earthquake.numOfPeople));

        TextView magnitudeTextView = (TextView) findViewById(R.id.perceived_magnitude);
        magnitudeTextView.setText(earthquake.perceivedStrength);
    }

    //generics used: String because the USGS URL string is the input to the doInBackground() , Void because
    // we're not using the progress part to update the user about the progress, and Event because for the result
    //we want an event object

    /**
     * {@link AsyncTask} to perform the network request on a background thread, an then update the UI
     * with the first earthquake in the response
     */
    private class EarthquakeAsyncTask extends AsyncTask<String, Void, Event> {

        /**
         * This method is invoked (or called) on a background thread, so we can perform long-running
         * operations like making a network request.
         *
         * It is NOT okay to update the UI from a background thread, so we just return an {@link Event}
         * object as the result.
         */
        protected Event doInBackground(String... urls) { //urls so this works with any String url
            // Perform the HTTP request for earthquake data and process the response.
            Event result = Utils.fetchEarthquakeData(urls[0]);
            //return the Event object
            return result;
        }

        /**
         * This method is invoked on the main UI thread after the background work has been completed.
         *
         * It IS okay to modify the UI within this method. We take the {@link Event} object (which is
         * returned from the doInBackground() method) and update the views on the screen.
         */
        protected void onPostExecute(Event result) {
            // Update the information displayed to the user.
            updateUi(result);
        }
    }
}
