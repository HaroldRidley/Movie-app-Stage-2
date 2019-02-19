package com.popmovies.hridley.popularmovies.fragments;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.popmovies.hridley.popularmovies.R;
import com.popmovies.hridley.popularmovies.adapters.ReviewsAdapter;
import com.popmovies.hridley.popularmovies.adapters.VideosAdapter;
import com.popmovies.hridley.popularmovies.db.AppDatabase;
import com.popmovies.hridley.popularmovies.executors.AppExecutors;
import com.popmovies.hridley.popularmovies.models.Movie;
import com.popmovies.hridley.popularmovies.models.Review;
import com.popmovies.hridley.popularmovies.models.Video;
import com.popmovies.hridley.popularmovies.utilities.DbJsonUtilities;
import com.popmovies.hridley.popularmovies.utilities.NetworkUtilities;
import com.popmovies.hridley.popularmovies.utilities.SpannableUtilities;
import com.popmovies.hridley.popularmovies.db.MovieContract;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MovieDetailFragment extends Fragment {

    private Context mContext;
    private Movie mMovie;
    private VideosAdapter mVideosAdapter;
    private ReviewsAdapter mReviewsAdapter;
    private AppDatabase mDb;

    @BindView(R.id.iv_movie_detail_backdrop)
    ImageView mMovieBackdropImageView;
    @BindView(R.id.pb_movie_detail_poster)
    ProgressBar mMoviePosterProgressBar;
    @BindView(R.id.tv_movie_detail_vote_average)
    TextView mMovieVoteAverageTextView;
    @BindView(R.id.tv_movie_detail_release_date)
    TextView mMovieReleaseDateTextView;
    @BindView(R.id.tv_movie_detail_overview)
    TextView mMovieOverviewTextView;
    @BindView(R.id.tv_movie_detail_poster_error)
    TextView mMoviePosterErrorTextView;

    @BindView(R.id.rv_videos)
    RecyclerView mVideosRecyclerView;
    @BindView(R.id.rv_reviews)
    RecyclerView mReviewsRecyclerView;
    @BindView(R.id.iv_movie_favorite)
    ImageView mMovieFavoriteImageView;

    @BindString(R.string.movie_detail_vote_average)
    String mDetailVoteAvgLabel;
    @BindString(R.string.movie_detail_release_date)
    String mDetailReleaseDateLabel;
    @BindString(R.string.movie_detail_overview)
    String mDetailOverviewLabel;

    @BindString(R.string.movie_favorite_off_toast_msg)
    String mFavOffToastMsg;
    @BindString(R.string.movie_favorite_on_toast_msg)
    String mFavOnToastMsg;

    public static final String PARCELABLE_MOVIE_KEY = "movieObject";
    private static final String BUNDLE_VIDEOS_KEY = "videoList";
    private static final String BUNDLE_REVIEWS_KEY = "reviewList";

    private static final String DETAIL_ELEMENT_VIDEOS = "videos";
    private static final String DETAIL_ELEMENT_REVIEWS = "reviews";

    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity().getApplicationContext();
        mDb = AppDatabase.getInstance(mContext);
        mMovie = null;
        if (getArguments().containsKey(PARCELABLE_MOVIE_KEY)) {
            mMovie = getArguments().getParcelable(PARCELABLE_MOVIE_KEY);
        }

        if (null != mMovie) {
            View rootView = inflater.inflate(R.layout.movie_detail, container, false);
            ButterKnife.bind(this, rootView);
            getActivity().setTitle(mMovie.getOriginalTitle());


            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                   final Movie currentMovie = mDb.movieDAO().loadMovieById(mMovie.getId());

                    Handler handler = new Handler(Looper.getMainLooper());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(null!= currentMovie){
                                MovieDetailFragment.showFavorite(mMovieFavoriteImageView, true);
                            }else{
                                MovieDetailFragment.showFavorite(mMovieFavoriteImageView, false);
                            }
                        }
                    });

                }
            });

            Picasso.with(mContext)
                    .load(mMovie.buildBackdropPath(mContext))
                    .into(mMovieBackdropImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            mMoviePosterProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            mMoviePosterProgressBar.setVisibility(View.GONE);
                            mMoviePosterErrorTextView.setRotation(-20);
                            mMoviePosterErrorTextView.setVisibility(View.VISIBLE);
                        }
                    });

            mMovieVoteAverageTextView.append(SpannableUtilities.makeBold(mDetailVoteAvgLabel));
            mMovieVoteAverageTextView.append(mMovie.getVoteAverage());
            mMovieReleaseDateTextView.append(SpannableUtilities.makeBold(mDetailReleaseDateLabel));
            mMovieReleaseDateTextView.append(mMovie.getReleaseDate());
            mMovieOverviewTextView.append(SpannableUtilities.makeBold(mDetailOverviewLabel));
            mMovieOverviewTextView.append(mMovie.getOverview());

            LinearLayoutManager videosLinearLayoutManager = new LinearLayoutManager(mContext);
            mVideosRecyclerView.setLayoutManager(videosLinearLayoutManager);

            mVideosRecyclerView.setHasFixedSize(true);
            mVideosAdapter = new VideosAdapter();
            mVideosRecyclerView.setAdapter(mVideosAdapter);

            LinearLayoutManager reviewsLinearLayoutManager = new LinearLayoutManager(mContext);
            mReviewsRecyclerView.setLayoutManager(reviewsLinearLayoutManager);

            mReviewsRecyclerView.setHasFixedSize(true);
            mReviewsAdapter = new ReviewsAdapter();
            mReviewsRecyclerView.setAdapter(mReviewsAdapter);

            if (null != savedInstanceState) {
                ArrayList<Video> videoList = savedInstanceState.getParcelableArrayList(BUNDLE_VIDEOS_KEY);
                mVideosAdapter.setVideosData(videoList);
                ArrayList<Review> reviewList = savedInstanceState.getParcelableArrayList(BUNDLE_REVIEWS_KEY);
                mReviewsAdapter.setReviewsData(reviewList);
            } else {
                loadElements(DETAIL_ELEMENT_VIDEOS, mMovie.getId());
                loadElements(DETAIL_ELEMENT_REVIEWS, mMovie.getId());
            }
            return rootView;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        List<Video> videosList = mVideosAdapter.getVideosData();
        if (null != videosList) {
            ArrayList<Video> videoArrayList = new ArrayList<>(videosList);
            outState.putParcelableArrayList(BUNDLE_VIDEOS_KEY, videoArrayList);
        }

        List<Review> reviewsList = mReviewsAdapter.getReviewsData();
        if (null != reviewsList) {
            ArrayList<Review> reviewArrayList = new ArrayList<>(reviewsList);
            outState.putParcelableArrayList(BUNDLE_REVIEWS_KEY, reviewArrayList);
        }
    }

    /**
     * A method that invokes the AsyncTask to populate the details required, for example
     * video trailer or reviews.
     *
     * @param element the element type to load
     * @param movieId the movie id for the specific videos we need
     */
    public void loadElements(String element, int movieId) {
        if (NetworkUtilities.hasConnection(mContext)) {
            String method;
            switch (element) {
                case DETAIL_ELEMENT_VIDEOS:
                    method = NetworkUtilities.getVideos(movieId);
                    String[] videos = new String[]{method};
                    new FetchVideosTask().execute(videos);
                    break;
                case DETAIL_ELEMENT_REVIEWS:
                    method = NetworkUtilities.getReviews(movieId);
                    String[] reviews = new String[]{method};
                    new FetchReviewsTask().execute(reviews);
                    break;
            }
        }
    }

    /**
     * A mehtod to modify favorite icon
     */
    private static void showFavorite(ImageView movieImageView, Boolean isFav){
        if (isFav){
            movieImageView.setBackgroundResource(R.drawable.ic_favorite_red_24dp);

        }else{
            movieImageView.setBackgroundResource(R.drawable.ic_favorite_border_red_24dp);
        }
    }

    /**
     * This method performs the insert or delete of a movie from the favorite database
     */
    @OnClick(R.id.iv_movie_favorite)
    public void favoriteMovie() {

        //if currently favorite, we delete it
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Handler handler = new Handler(Looper.getMainLooper());
                Movie movie = mDb.movieDAO().loadMovieById(mMovie.getId());
                if (null != movie) {
                    mDb.movieDAO().deleteMovie(movie);

                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity().getBaseContext(), mFavOffToastMsg, Toast.LENGTH_LONG).show();
                            mMovieFavoriteImageView.setBackgroundResource(R.drawable.ic_favorite_border_red_24dp);
                        }
                    });

                } else {
                    mDb.movieDAO().insertMovie(mMovie);
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity().getBaseContext(), mFavOnToastMsg, Toast.LENGTH_LONG).show();
                            mMovieFavoriteImageView.setBackgroundResource(R.drawable.ic_favorite_red_24dp);
                        }
                    });

                }
            }
        });

    }

    /**
     * The background worker that executes the calls to the MovieDB service
     * Using an Inner class to avoid convolution when having to manipulate the
     * View elements in the fragment.
     */
    public class FetchVideosTask extends AsyncTask<String[], Void, List<Video>> {

        private final String TAG = FetchVideosTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Video> doInBackground(String[]... params) {
            String method = params[0][0];
            Map<String, String> mapping = new HashMap<>();

            mapping.put(NetworkUtilities.getLanguage(), MovieGridFragment.getMovieLocale());

            URL url = NetworkUtilities.buildUrl(method, mapping);

            try {
                String response = NetworkUtilities.getResponse(url);
                Log.d(TAG, response);
                JSONObject responseJson = new JSONObject(response);

                return DbJsonUtilities.getVideosListFromJson(responseJson);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Video> videoList) {
            if (!(videoList.isEmpty())) {
                mVideosAdapter.setVideosData(videoList);
            }
        }
    }

    /**
     * The background worker that executes the calls to the MovieDB service
     * Using an Inner class to avoid convolution when having to manipulate the
     * View elements in the fragment.
     */
    public class FetchReviewsTask extends AsyncTask<String[], Void, List<Review>> {

        private final String TAG = FetchReviewsTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Review> doInBackground(String[]... params) {
            String method = params[0][0];
            Map<String, String> mapping = new HashMap<>();

            mapping.put(NetworkUtilities.getLanguage(), MovieGridFragment.getMovieLocale());

            URL url = NetworkUtilities.buildUrl(method, mapping);

            try {
                String response = NetworkUtilities.getResponse(url);
                Log.d(TAG, response);
                JSONObject responseJson = new JSONObject(response);

                return DbJsonUtilities.getReviewsListFromJson(responseJson);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Review> reviewList) {
            if (!(reviewList.isEmpty())) {
                mReviewsAdapter.setReviewsData(reviewList);
            }
        }
    }

}
