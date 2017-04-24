package com.popdeem.sdk.uikit.fragment.multilogin;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.location.LocationListener;
import com.popdeem.sdk.R;
import com.popdeem.sdk.core.api.PDAPICallback;
import com.popdeem.sdk.core.api.PDAPIClient;
import com.popdeem.sdk.core.api.abra.PDAbraConfig;
import com.popdeem.sdk.core.api.abra.PDAbraLogEvent;
import com.popdeem.sdk.core.api.abra.PDAbraProperties;
import com.popdeem.sdk.core.location.PDLocationManager;
import com.popdeem.sdk.core.model.PDUser;
import com.popdeem.sdk.core.utils.PDLog;
import com.popdeem.sdk.core.utils.PDSocialUtils;
import com.popdeem.sdk.core.utils.PDUtils;
import com.popdeem.sdk.uikit.fragment.PDUIRewardsFragment;
import com.popdeem.sdk.uikit.fragment.PDUISocialLoginFragment;
import com.popdeem.sdk.uikit.utils.PDUIColorUtils;
import com.popdeem.sdk.uikit.utils.PDUIDialogUtils;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;

import java.util.Arrays;

/**
 * Created by dave on 21/04/2017.
 * Project: Popdeem-SDK-Android
 */

public class PDUISocialMultiLoginFragment extends Fragment implements View.OnClickListener {

    private static String TAG = PDUISocialMultiLoginFragment.class.getSimpleName();

    private View view;

    private final int LOCATION_PERMISSION_REQUEST = 90;

    private PDLocationManager mLocationManager;

    private ProgressBar mProgressFacebook;
    private ProgressBar mProgressTwitter;
    private ProgressBar mProgressInstagram;

    private TextView mRewardsInfoTextView;

    private Button mContinueButton;

    private Button mFacebookLoginButton;
    private Button mTwitterLoginButton;
    private Button mInstaLoginButton;

    private CallbackManager mCallbackManager;

    private boolean mAskForPermission = true;

    private boolean isFacebook = false, isTwitter = false, isInstagram = false;

    public PDUISocialMultiLoginFragment() {
    }

    public static PDUISocialMultiLoginFragment newInstance() {
        return new PDUISocialMultiLoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_pd_social_multi_login, container, false);


        registerCallBacks();
        setupBackButton();
        setupSocialButtons();


        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        PDAbraLogEvent.log(PDAbraConfig.ABRA_EVENT_PAGE_VIEWED, new PDAbraProperties.Builder()
                .add(PDAbraConfig.ABRA_PROPERTYNAME_SOURCE_PAGE, PDAbraConfig.ABRA_PROPERTYVALUE_PAGE_LOGINTAKEOVER)
                .create());
    }

    ////////////////////////////////////////////////////
    // Callbacks
    //////////////////////////////////////////////////

    private void registerCallBacks() {
        mLocationManager = new PDLocationManager(getActivity());
        mCallbackManager = CallbackManager.Factory.create();

        //Facebook specific callback - starts location
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mFacebookLoginButton.setText(R.string.pd_log_out_facebook_text);
                PDLog.d(PDUISocialMultiLoginFragment.class, "Facebook Login onSuccess(): " + loginResult.getAccessToken().getToken());
                checkForLocationPermissionAndStartLocationManager();
            }

            @Override
            public void onCancel() {
                PDAbraLogEvent.log(PDAbraConfig.ABRA_EVENT_CANCELLED_FACEBOOK_LOGIN, null);
                PDLog.d(PDUISocialLoginFragment.class, "Facebook Login onCancel()");
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.pd_common_facebook_login_cancelled_title_text)
                        .setMessage(R.string.pd_common_facebook_login_cancelled_message_text)
                        .setPositiveButton(android.R.string.ok, null)
                        .create()
                        .show();
            }

            @Override
            public void onError(FacebookException error) {
                PDLog.d(PDUISocialLoginFragment.class, "Facebook Login onError(): " + error.getMessage());
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.pd_common_sorry_text)
                        .setMessage(error.getMessage())
                        .setPositiveButton(android.R.string.ok, null)
                        .create()
                        .show();
            }
        });
    }

    ////////////////////////////////////////////////////
    // Social Login Buttons
    ///////////////////////////////////////////////////

    private void setupSocialButtons() {
        mFacebookLoginButton = (Button) view.findViewById(R.id.pd_facebook_login_button);
        mFacebookLoginButton.setOnClickListener(this);
        mTwitterLoginButton = (Button) view.findViewById(R.id.pd_twitter_login_button);
        mTwitterLoginButton.setOnClickListener(this);
        mInstaLoginButton = (Button) view.findViewById(R.id.pd_instagram_login_button);
        mInstaLoginButton.setOnClickListener(this);

        mProgressFacebook = (ProgressBar) view.findViewById(R.id.pd_progress_bar);
        mProgressTwitter = (ProgressBar) view.findViewById(R.id.pd_progress_bar_twitter);
        mProgressInstagram = (ProgressBar) view.findViewById(R.id.pd_progress_bar_instagram);
    }


    ////////////////////////////////////////////////////
    // Social Login Buttons Click Listeners
    //////////////////////////////////////////////////

    @Override
    public void onClick(View v) {
        final int ID = v.getId();

        if (ID == R.id.pd_facebook_login_button) {
            Log.i(TAG, "onClick: Facebook Login Selected");
            isFacebook = true;
            isTwitter = false;
            isInstagram = false;
            loginFacebook();

        } else if (ID == R.id.pd_twitter_login_button) {
            Log.i(TAG, "onClick: Twitter Login Selected");
            isFacebook = false;
            isTwitter = true;
            isInstagram = false;
            checkForLocationPermissionAndStartLocationManager();
        } else if (ID == R.id.pd_instagram_login_button) {
            Log.i(TAG, "onClick: Instagram Login Selected");
            isFacebook = false;
            isTwitter = false;
            isInstagram = true;
            checkForLocationPermissionAndStartLocationManager();
        }
    }

    ////////////////////////////////////////////////////
    // Social Login Methods                          //
    //////////////////////////////////////////////////


    /**
     * Facebook
     */
    private void loginFacebook() {
        if (PDLocationManager.isGpsEnabled(getActivity())) {
            LoginManager.getInstance().logInWithReadPermissions(PDUISocialMultiLoginFragment.this, Arrays.asList(PDSocialUtils.FACEBOOK_READ_PERMISSIONS));
        } else {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.pd_location_disabled_title_text)
                    .setMessage(R.string.pd_location_disabled_message_text)
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PDLocationManager.startLocationSettingsActivity(getActivity());
                        }
                    })
                    .create().show();
        }
    }

    /**
     * Twitter
     * @param location
     */

    private void loginTwitter(final Location location) {

        PDSocialUtils.loginWithTwitter(getActivity(), new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                if (result.data != null) {
                    connectTwitterAccount(result.data);
                } else {
                    showGenericAlert();
                }
            }

            @Override
            public void failure(TwitterException e) {
                if (getActivity() != null) {
                    PDUIDialogUtils.showBasicOKAlertDialog(getActivity(), R.string.pd_claim_twitter_button_text, e.getMessage());
                }
            }
        });
    }

    private void connectTwitterAccount(TwitterSession session){
        PDAPIClient.instance().connectWithTwitterAccount(String.valueOf(session.getUserId()),
                session.getAuthToken().token, session.getAuthToken().secret, PD_API_CALLBACK);
    }


    /**
     * Instagram
     * @param location
     */

    private void loginInstagram(final Location location) {
        PDAPIClient.instance().connectWithInstagramAccount("", "", "", new PDAPICallback<PDUser>() {
            @Override
            public void success(PDUser pdUser) {

            }

            @Override
            public void failure(int statusCode, Exception e) {

            }
        });
    }


    //////////////////////////////////////////////////////////////////////////////////
    // Back Button - just closes the login fragment to continue with a Non-Social User
    //////////////////////////////////////////////////////////////////////////////////

    private void setupBackButton() {
        ImageButton backButton = (ImageButton) view.findViewById(R.id.pd_social_login_back_button);
        backButton.setImageDrawable(PDUIColorUtils.getSocialLoginBackButtonIcon(getActivity()));
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mProgressFacebook.getVisibility() == View.GONE && mProgressTwitter.getVisibility() == View.GONE && mProgressInstagram.getVisibility() == View.GONE) {
                    PDAbraLogEvent.log(PDAbraConfig.ABRA_EVENT_CLICKED_CLOSE_LOGIN_TAKEOVER, new PDAbraProperties.Builder()
                            .add("Source", "Dismiss Button")
                            .create());
                    removeThisFragment();
                }
            }
        });
    }

    ////////////////////////////////////////////////////
    // Pop this fragment off the stack               //
    //////////////////////////////////////////////////

    public void removeThisFragment() {
        getActivity().getSupportFragmentManager().popBackStack(PDUISocialMultiLoginFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    ////////////////////////////////////////////////////
    // Location Methods                              //
    //////////////////////////////////////////////////

    private void checkForLocationPermissionAndStartLocationManager() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.pd_location_permission_title_text)
                        .setMessage(R.string.pd_location_permission_rationale_text)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                        LOCATION_PERMISSION_REQUEST);
                            }
                        })
                        .create()
                        .show();
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST);
            }
        } else {
            startLocationManagerAfterLogin();
        }
    }

    private void startLocationManagerAfterLogin() {
        if (isFacebook)
            mProgressFacebook.setVisibility(View.VISIBLE);
        if (isTwitter)
            mProgressTwitter.setVisibility(View.VISIBLE);
        if (isInstagram)
            mProgressInstagram.setVisibility(View.VISIBLE);

        mFacebookLoginButton.setVisibility(View.INVISIBLE);
        mTwitterLoginButton.setVisibility(View.INVISIBLE);
        mInstaLoginButton.setVisibility(View.INVISIBLE);

        mLocationManager.startLocationUpdates(new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    handleLocationUpdate(location);
                }
            }
        });
    }

    private void handleLocationUpdate(final Location location) {
        mLocationManager.stop();
        PDUtils.updateSavedUserLocation(location);

        if (isFacebook) {
            PDAPIClient.instance().registerUserWithFacebook(AccessToken.getCurrentAccessToken().getToken(), AccessToken.getCurrentAccessToken().getUserId(), new PDAPICallback<PDUser>() {
                @Override
                public void success(PDUser user) {
                    PDLog.d(PDUISocialLoginFragment.class, "registered with Facebook: " + user.toString());

                    PDUtils.updateSavedUser(user);
//                    updateUser(location);


                    PDAbraLogEvent.log(PDAbraConfig.ABRA_EVENT_LOGIN, new PDAbraProperties.Builder()
                            .add("Source", "Login Takeover")
                            .create());
                    PDAbraLogEvent.onboardUser();
                }

                @Override
                public void failure(int statusCode, Exception e) {
                    PDLog.d(PDUISocialLoginFragment.class, "failed register with Facebook: statusCode=" + statusCode + ", message=" + e.getMessage());

                    LoginManager.getInstance().logOut();

                    mProgressFacebook.setVisibility(View.GONE);
                    mFacebookLoginButton.setVisibility(View.VISIBLE);
                    mFacebookLoginButton.setText(R.string.pd_log_in_with_facebook_text);
                    mTwitterLoginButton.setVisibility(View.VISIBLE);
                    mInstaLoginButton.setVisibility(View.VISIBLE);

                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.pd_common_sorry_text)
                            .setMessage("An error occurred while registering. Please try again")
                            .setPositiveButton(android.R.string.ok, null)
                            .create()
                            .show();
                }
            });
        } else if (isTwitter) {
            loginTwitter(location);
        } else if (isInstagram) {
            loginInstagram(location);
        }
    }

    ////////////////////////////////////////////////////
    // Generic Methods                               //
    //////////////////////////////////////////////////

    private void showGenericAlert() {
        if (getActivity() != null) {
            PDUIDialogUtils.showBasicOKAlertDialog(getActivity(), R.string.pd_common_sorry_text, R.string.pd_common_something_wrong_text);
        }
    }
}
