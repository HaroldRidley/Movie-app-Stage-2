package com.popmovies.hridley.popularmovies.fragments;


import android.content.Context;
import android.content.SharedPreferences;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.popmovies.hridley.popularmovies.R;
import com.popmovies.hridley.popularmovies.adapters.MoviesAdapter;
import com.popmovies.hridley.popularmovies.db.AppDatabase;
import com.popmovies.hridley.popularmovies.executors.AppExecutors;
import com.popmovies.hridley.popularmovies.models.Movie;
import com.popmovies.hridley.popularmovies.utilities.NetworkUtilities;
import com.popmovies.hridley.popularmovies.utilities.RecyclerViewScrollListener;
import com.popmovies.hridley.popularmovies.utilities.DbJsonUtilities;
import com.popmovies.hridley.popularmovies.db.MovieContract.FavoriteMovieEntry;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A Class that extends Fragment to implement the Movie List structure
 */
public class MovieGridFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private Context mContext;
    private RecyclerViewScrollListener mScrollListener;
    @BindView(R.id.rv_posters)
    RecyclerView mRecyclerView;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;
    @BindView(R.id.tv_error_message_display)
    TextView mErrorMessageDisplay;
    @BindView(R.id.sr_swipe_container)
    SwipeRefreshLayout mSwipeContainer;
    @BindString(R.string.sorting_key)
    String mPrefSortingKey;
    @BindString(R.string.sorting_default)
    String mPrefSortingDefault;
    @BindString(R.string.sorting_popular_value)
    String mPrefSortingPopularValue;
    @BindString(R.string.sorting_rated_value)
    String mPrefSortingRatedValue;
    @BindString(R.string.sorting_favorites_value)
    String mPrefSortingFavoritesValue;
    @BindString(R.string.locale_key)
    String mPrefLocaleKey;
    @BindString(R.string.locale_default)
    String mPrefLocaleDefault;
    private int mPage;
    private int mSorting;
    private static String mMovieLocale;
    private int mPosition = RecyclerView.NO_POSITION;
    private List<Movie> favMovieList;

    private static MoviesAdapter mMoviesAdapter;

    private AppDatabase mDb;

    private static final int SORTING_POPULAR = 1;
    private static final int SORTING_RATED = 2;
    private static final int SORTING_FAVORITES = 3;
    private static final String BUNDLE_MOVIES_KEY = "movieList";
    private static final String BUNDLE_PAGE_KEY = "currentPage";
    private static final String BUNDLE_SORTING_KEY = "currentSorting";
    private static final String BUNDLE_ERROR_KEY = "errorShown";

    private static final int ID_FAVORITES_LOADER = 33;

    public static final String[] FAVORITE_MOVIES_PROJECTION = {
            FavoriteMovieEntry.MOVIE_ID,
            FavoriteMovieEntry.BACKDROP_PATH,
            FavoriteMovieEntry.POSTER_PATH,
            FavoriteMovieEntry.OVERVIEW,
            FavoriteMovieEntry.TITLE,
            FavoriteMovieEntry.RELEASE_DATE,
            FavoriteMovieEntry.VOTE_AVERAGE,
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Boolean errorShown = false;
        if (null != savedInstanceState) {
            errorShown = savedInstanceState.getBoolean(BUNDLE_ERROR_KEY);
        }

        View rootView = inflater.inflate(R.layout.movie_grid, container, false);
        ButterKnife.bind(this, rootView);
        mContext = getContext();
        mDb = AppDatabase.getInstance(mContext);
        setupSharedPreferences();

        if (null != savedInstanceState && !errorShown) {
            mPage = savedInstanceState.getInt(BUNDLE_PAGE_KEY);
            mSorting = savedInstanceState.getInt(BUNDLE_SORTING_KEY);
        } else {
            mPage = 1;
        }

        final int columns = getResources().getInteger(R.integer.grid_columns);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, columns, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        mRecyclerView.setHasFixedSize(true);
        mMoviesAdapter = new MoviesAdapter();
        mRecyclerView.setAdapter(mMoviesAdapter);

        if (mSorting != SORTING_FAVORITES) {
            mScrollListener = new RecyclerViewScrollListener(gridLayoutManager, mPage) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    mPage = page;
                    loadCards();
                }
            };
            mRecyclerView.addOnScrollListener(mScrollListener);
        }

        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mErrorMessageDisplay.setVisibility(View.INVISIBLE);
                clearGridView();
                loadCards();
            }
        });
        mSwipeContainer.setColorSchemeResources(R.color.colorAccent);

        if (null != savedInstanceState && !errorShown) {
            ArrayList<Movie> movieList = savedInstanceState.getParcelableArrayList(BUNDLE_MOVIES_KEY);
            mMoviesAdapter.setMoviesData(movieList);
        } else {
            loadCards();
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        List<Movie> movieList = mMoviesAdapter.getMoviesData();
        if (null != movieList) {
            outState.putParcelableArrayList(BUNDLE_MOVIES_KEY, new ArrayList<>(movieList));
            outState.putInt(BUNDLE_PAGE_KEY, mPage);
            outState.putInt(BUNDLE_SORTING_KEY, mSorting);
        } else if (mErrorMessageDisplay.isShown()) {
            outState.putBoolean(BUNDLE_ERROR_KEY, true);
        }
    }

    /**
     * This method sets different options based on the SharedPreferences of the application
     */
    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        String sorting = sharedPreferences.getString(mPrefSortingKey, mPrefSortingDefault);
        if (sorting.equals(mPrefSortingPopularValue)) {
            mSorting = SORTING_POPULAR;
        } else if (sorting.equals(mPrefSortingRatedValue)) {
            mSorting = SORTING_RATED;
        } else if (sorting.equals(mPrefSortingFavoritesValue)) {
            mSorting = SORTING_FAVORITES;
        }

        mMovieLocale = sharedPreferences.getString(mPrefLocaleKey, mPrefLocaleDefault);

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

    /**
     * A method that invokes the AsyncTask to populate the RecyclerView,
     * it's based on the sorting option selected by the user. Default is "most popular"
     */
    public void loadCards() {
        if (mSwipeContainer.isRefreshing()) {
            mSwipeContainer.setRefreshing(false);
        }
        if (NetworkUtilities.hasConnection(mContext)) {
            switch (mSorting) {
                case SORTING_POPULAR:
                    new FetchMoviesTask().execute(
                            new String[]{
                                    NetworkUtilities.getPopular(),
                                    String.valueOf(mPage)
                            }
                    );
                    break;
                case SORTING_RATED:
                    new FetchMoviesTask().execute(
                            new String[]{
                                    NetworkUtilities.getTopRated(),
                                    String.valueOf(mPage)
                            }
                    );
                    break;
                case SORTING_FAVORITES:

                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                List<Movie> movieList = mDb.movieDAO().loadAllMovies();
                                //logic from load fav movies
                                mLoadingIndicator.setVisibility(View.INVISIBLE);
                                if (null != movieList) {
                                    mMoviesAdapter.setMoviesData(movieList);
                                    mErrorMessageDisplay.setVisibility(View.INVISIBLE);
                                } else {
                                    showErrorMessage(R.string.error_moviedb_list, mContext);
                                }
                                mSwipeContainer.setRefreshing(false);
                                // end logic
                            } catch (NullPointerException e) {
                                Toast.makeText(getActivity().getBaseContext(), "No Favorites", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    break;
            }
        } else {
            showErrorMessage(R.string.error_no_connectivity, mContext);
        }
    }

    public void loadFavoritesFromDb(List<Movie> movieList) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (null != movieList) {
            mMoviesAdapter.setMoviesData(movieList);
            mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        } else {
            showErrorMessage(R.string.error_moviedb_list, mContext);
        }
        mSwipeContainer.setRefreshing(false);
    }

    /**
     * Reset the GridView properties and adapter
     */
    public void clearGridView() {
        switch (mSorting) {
            case SORTING_POPULAR:
            case SORTING_RATED:
                mScrollListener.resetState();
                mPage = 1;
                mMoviesAdapter.clearMovieList();
                break;
            case SORTING_FAVORITES:
                mMoviesAdapter.loadCursorIntoAdapter(null);
        }
    }

    /**
     * Display the specific error message in the TextView
     *
     * @param messageId the resource id of the error string
     */
    public void showErrorMessage(int messageId, Context context) {
        mErrorMessageDisplay.setText(context.getText(messageId));
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    /**
     * Getter method for the Movie Locale
     *
     * @return a string representing the locale used to query The MovieDB service
     */
    public static String getMovieLocale() {
        return mMovieLocale;
    }

    /**
     * The background worker that executes the calls to the MovieDB service.
     * Using an Inner class to avoid convolution when having to manipulate the
     * View elements in the fragment.
     */
    public class FetchMoviesTask extends AsyncTask<String[], Void, List<Movie>> {

        private final String TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Movie> doInBackground(String[]... params) {
            String method = params[0][0];
            String page = params[0][1];
            Map<String, String> mapping = new HashMap<>();

            mapping.put(NetworkUtilities.getLanguage(), MovieGridFragment.getMovieLocale());
            mapping.put(NetworkUtilities.getPage(), String.valueOf(page));

            URL url = NetworkUtilities.buildUrl(method, mapping);

            try {
                String response = NetworkUtilities.getResponse(url);
                Log.d(TAG, response);
                JSONObject responseJson = new JSONObject(response);

                return DbJsonUtilities.getPopularMoviesListFromJson(responseJson);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Movie> movieList) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (null != movieList) {
                mMoviesAdapter.setMoviesData(movieList);
                mErrorMessageDisplay.setVisibility(View.INVISIBLE);
            } else {
                showErrorMessage(R.string.error_moviedb_list, mContext);
            }
            mSwipeContainer.setRefreshing(false);
        }
    }

}
