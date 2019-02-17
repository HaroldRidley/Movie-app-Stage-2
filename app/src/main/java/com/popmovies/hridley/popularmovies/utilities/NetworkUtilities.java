package com.popmovies.hridley.popularmovies.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

public final class NetworkUtilities {

    private static final String API_KEY = "971bd2e612c9ad2fefa9a8a1feb82c55";


    private static final String API_KEY_QUERY_PARAM = "api_key";
    private static final String METHOD_POPULAR = "/movie/popular";
    private static final String METHOD_RATED = "/movie/top_rated";
    private static final String PAGE_QUERY_PARAM = "page";
    private static final String LANGUAGE_QUERY_PARAM = "language";
    private static final String API_URL = "https://api.themoviedb.org/3";
    private static final String METHOD_VIDEOS = "/movie/#/videos";
    private static final String METHOD_REVIEWS = "/movie/#/reviews";


    public static boolean hasConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo.isConnected();
    }

    public static URL buildUrl(String method, Map<String, String> params) {
        Uri.Builder builder = Uri.parse(API_URL + method).buildUpon();
        builder.appendQueryParameter(API_KEY_QUERY_PARAM, API_KEY);
        for (Map.Entry<String, String> param : params.entrySet()) {
            builder.appendQueryParameter(param.getKey(), param.getValue());
        }

        Uri uri = builder.build();
        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static String getResponse(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream inputStream = urlConnection.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");
            if (!scanner.hasNext()) {
                return null;
            }
            return scanner.next();

        } finally {
            urlConnection.disconnect();
        }
    }
    
    

    /**
     * Getter method to get the Language query param value
     *
     * @return the Language query param value
     */
    public static String getLanguage() { return LANGUAGE_QUERY_PARAM; }

    /**
     * Getter method to get the Page query param value
     *
     * @return the Page param value
     */
    public static String getPage() {
        return PAGE_QUERY_PARAM;
    }

    /**
     * Getter method to get the Most Popular query param value
     *
     * @return the Most Popular param value
     */
    public static String getPopular() {
        return METHOD_POPULAR;
    }

    /**
     * Getter method to get the Rated query param value
     *
     * @return the Rated param value
     */
    public static String getTopRated() {
        return METHOD_RATED;
    }

    /**
     * Getter method to get the Videos query param value
     *
     * @param movieId the id of the movie.
     * @return the Videos param value
     */
    public static String getVideos(int movieId) {
        return METHOD_VIDEOS.replace("#", String.valueOf(movieId));
    }

    /**
     * Getter method to get the Reviews query param value
     *
     * @param movieId the id of the movie.
     * @return the Reviews param value
     */
    public static String getReviews(int movieId) {
        return METHOD_REVIEWS.replace("#", String.valueOf(movieId));
    }
}
