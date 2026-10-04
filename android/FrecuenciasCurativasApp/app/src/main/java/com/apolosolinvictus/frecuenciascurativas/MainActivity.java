package com.apolosolinvictus.frecuenciascurativas;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Insets;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity implements BillingManager.Listener {
    private static final String TRUSTED_HOST = "infiniti-ia.com";
    private static final int BACKGROUND = Color.rgb(3, 1, 8);
    private static final int PANEL = Color.rgb(13, 5, 20);
    private static final int ACCENT = Color.rgb(0, 240, 255);

    private LinearLayout toolbar;
    private ImageButton backButton;
    private ImageButton forwardButton;
    private ImageButton reloadButton;
    private ImageButton moreButton;
    private WebView webView;
    private ProgressBar progressBar;
    private View errorView;
    private View purchaseView;
    private TextView purchaseDescription;
    private TextView purchaseStatus;
    private Button purchaseButton;
    private BillingManager billingManager;
    private boolean webContentUnlocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BACKGROUND);
        getWindow().setNavigationBarColor(BACKGROUND);
        buildInterface();
        configureWebView();
        billingManager = new BillingManager(this, this);
        billingManager.start();
    }

    private void buildInterface() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BACKGROUND);

        toolbar = new LinearLayout(this);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(8), 0, dp(8), 0);
        toolbar.setBackgroundColor(PANEL);
        toolbar.setMinimumHeight(dp(56));

        TextView title = new TextView(this);
        title.setText(R.string.app_name);
        title.setTextColor(Color.WHITE);
        title.setTextSize(16);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setSingleLine(true);
        toolbar.addView(title, new LinearLayout.LayoutParams(0, dp(56), 1));

        backButton = createIconButton(android.R.drawable.ic_media_previous, R.string.back);
        backButton.setOnClickListener(view -> webView.goBack());
        toolbar.addView(backButton);

        forwardButton = createIconButton(android.R.drawable.ic_media_next, R.string.forward);
        forwardButton.setOnClickListener(view -> webView.goForward());
        toolbar.addView(forwardButton);

        reloadButton = createIconButton(android.R.drawable.ic_popup_sync, R.string.reload);
        reloadButton.setOnClickListener(view -> reloadPage());
        toolbar.addView(reloadButton);

        moreButton = createIconButton(android.R.drawable.ic_menu_more, R.string.more);
        moreButton.setOnClickListener(this::showMoreMenu);
        toolbar.addView(moreButton);

        FrameLayout pageFrame = new FrameLayout(this);
        webView = new WebView(this);
        pageFrame.addView(webView, new FrameLayout.LayoutParams(-1, -1));

        progressBar = new ProgressBar(this);
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(dp(44), dp(44));
        progressParams.gravity = Gravity.CENTER;
        pageFrame.addView(progressBar, progressParams);

        errorView = createErrorView();
        errorView.setVisibility(View.GONE);
        pageFrame.addView(errorView, new FrameLayout.LayoutParams(-1, -1));

        purchaseView = createPurchaseView();
        pageFrame.addView(purchaseView, new FrameLayout.LayoutParams(-1, -1));

        root.addView(toolbar, new LinearLayout.LayoutParams(-1, dp(56)));
        root.addView(pageFrame, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsets.Type.systemBars());
                toolbar.setPadding(dp(8), systemBars.top, dp(8), 0);
                return insets;
            });
            root.requestApplyInsets();
        }
    }

    private View createPurchaseView() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(BACKGROUND);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER_HORIZONTAL);
        container.setPadding(dp(28), dp(30), dp(28), dp(30));
        scrollView.addView(container, new ScrollView.LayoutParams(-1, -2));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.infiniti_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        container.addView(logo, new LinearLayout.LayoutParams(-1, dp(118)));

        TextView heading = new TextView(this);
        heading.setText(R.string.purchase_title);
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(26);
        heading.setGravity(Gravity.CENTER);
        heading.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(-1, -2);
        headingParams.topMargin = dp(14);
        container.addView(heading, headingParams);

        purchaseDescription = new TextView(this);
        purchaseDescription.setText(getString(R.string.purchase_description,
                getString(R.string.purchase_default_price)));
        purchaseDescription.setTextColor(Color.LTGRAY);
        purchaseDescription.setTextSize(16);
        purchaseDescription.setGravity(Gravity.CENTER);
        purchaseDescription.setLineSpacing(0, 1.15f);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(-1, -2);
        descriptionParams.topMargin = dp(16);
        container.addView(purchaseDescription, descriptionParams);

        purchaseStatus = new TextView(this);
        purchaseStatus.setText(R.string.purchase_loading);
        purchaseStatus.setTextColor(ACCENT);
        purchaseStatus.setTextSize(14);
        purchaseStatus.setGravity(Gravity.CENTER);
        purchaseStatus.setLineSpacing(0, 1.1f);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.topMargin = dp(18);
        container.addView(purchaseStatus, statusParams);

        TextView terms = new TextView(this);
        terms.setText(R.string.purchase_terms);
        terms.setTextColor(Color.rgb(175, 170, 185));
        terms.setTextSize(13);
        terms.setGravity(Gravity.CENTER);
        terms.setLineSpacing(0, 1.15f);
        LinearLayout.LayoutParams termsParams = new LinearLayout.LayoutParams(-1, -2);
        termsParams.topMargin = dp(18);
        container.addView(terms, termsParams);

        purchaseButton = createActionButton(R.string.purchase_buy);
        purchaseButton.setEnabled(false);
        purchaseButton.setOnClickListener(view -> billingManager.launchPurchase(this));
        LinearLayout.LayoutParams subscribeParams = new LinearLayout.LayoutParams(-1, dp(52));
        subscribeParams.topMargin = dp(24);
        container.addView(purchaseButton, subscribeParams);

        Button restoreButton = createSecondaryButton(R.string.purchase_restore);
        restoreButton.setOnClickListener(view -> billingManager.refreshPurchases());
        LinearLayout.LayoutParams restoreParams = new LinearLayout.LayoutParams(-1, dp(48));
        restoreParams.topMargin = dp(10);
        container.addView(restoreButton, restoreParams);

        Button privacyButton = createSecondaryButton(R.string.purchase_privacy);
        privacyButton.setOnClickListener(view -> openExternal(Uri.parse(getString(R.string.privacy_url))));
        LinearLayout.LayoutParams privacyParams = new LinearLayout.LayoutParams(-1, dp(48));
        privacyParams.topMargin = dp(10);
        container.addView(privacyButton, privacyParams);

        return scrollView;
    }

    private Button createActionButton(int textResource) {
        Button button = new Button(this);
        button.setText(textResource);
        button.setTextColor(Color.BLACK);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setBackgroundColor(ACCENT);
        return button;
    }

    private Button createSecondaryButton(int textResource) {
        Button button = new Button(this);
        button.setText(textResource);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setBackgroundColor(Color.rgb(34, 20, 45));
        return button;
    }

    private ImageButton createIconButton(int iconResource, int descriptionResource) {
        ImageButton button = new ImageButton(this);
        Drawable icon = getDrawable(iconResource);
        if (icon != null) {
            icon = icon.mutate();
            icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            button.setImageDrawable(icon);
        }
        button.setContentDescription(getString(descriptionResource));
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setPadding(dp(10), dp(10), dp(10), dp(10));
        button.setMinimumWidth(dp(44));
        button.setMinimumHeight(dp(44));
        return button;
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        }
        settings.setUserAgentString(settings.getUserAgentString() + " CuratiApp/1.0");

        webView.setBackgroundColor(BACKGROUND);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleNavigation(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleNavigation(Uri.parse(url));
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                errorView.setVisibility(View.GONE);
                updateNavigationButtons();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
                updateNavigationButtons();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    progressBar.setVisibility(View.GONE);
                    errorView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onOfferReady(String priceLabel) {
        runOnUiThread(() -> {
            purchaseDescription.setText(getString(R.string.purchase_description, priceLabel));
            purchaseStatus.setText("");
            purchaseButton.setEnabled(true);
        });
    }

    @Override
    public void onPurchaseStateChanged(boolean active, String message) {
        runOnUiThread(() -> {
            if (active) {
                webContentUnlocked = true;
                purchaseStatus.setText(message);
                purchaseView.setVisibility(View.GONE);
                loadHome();
            } else {
                purchaseStatus.setText(message == null || message.isEmpty() ? "" : message);
                purchaseView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onBillingMessage(String message) {
        runOnUiThread(() -> {
            purchaseStatus.setText(message);
            purchaseButton.setEnabled(true);
        });
    }

    private boolean handleNavigation(Uri uri) {
        if (uri == null) return true;
        String host = uri.getHost();
        boolean trusted = "https".equalsIgnoreCase(uri.getScheme())
                && host != null
                && (TRUSTED_HOST.equalsIgnoreCase(host) || ("www." + TRUSTED_HOST).equalsIgnoreCase(host));
        if (trusted) return false;
        openExternal(uri);
        return true;
    }

    private void openExternal(Uri uri) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.external_link_error, Toast.LENGTH_SHORT).show();
        }
    }

    private View createErrorView() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(dp(28), dp(28), dp(28), dp(28));
        container.setBackgroundColor(BACKGROUND);

        TextView heading = new TextView(this);
        heading.setText(R.string.unable_to_connect);
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(20);
        heading.setGravity(Gravity.CENTER);
        heading.setTypeface(null, android.graphics.Typeface.BOLD);
        container.addView(heading, new LinearLayout.LayoutParams(-1, -2));

        TextView message = new TextView(this);
        message.setText(R.string.page_load_error);
        message.setTextColor(Color.LTGRAY);
        message.setTextSize(15);
        message.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(-1, -2);
        messageParams.topMargin = dp(12);
        container.addView(message, messageParams);

        Button retry = createSecondaryButton(R.string.retry);
        retry.setOnClickListener(view -> reloadPage());
        LinearLayout.LayoutParams retryParams = new LinearLayout.LayoutParams(-2, -2);
        retryParams.topMargin = dp(20);
        container.addView(retry, retryParams);
        return container;
    }

    private void showMoreMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        Menu menu = popup.getMenu();
        menu.add(Menu.NONE, 1, Menu.NONE, R.string.share_site);
        menu.add(Menu.NONE, 2, Menu.NONE, R.string.sound_safety);
        menu.add(Menu.NONE, 3, Menu.NONE, R.string.privacy_policy);
        menu.add(Menu.NONE, 4, Menu.NONE, R.string.purchase_menu);
        popup.setOnMenuItemClickListener(this::handleMenuItem);
        popup.show();
    }

    private boolean handleMenuItem(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                shareSite();
                return true;
            case 2:
                showSoundSafety();
                return true;
            case 3:
                openExternal(Uri.parse(getString(R.string.privacy_url)));
                return true;
            case 4:
                purchaseView.setVisibility(View.VISIBLE);
                return true;
            default:
                return false;
        }
    }

    private void shareSite() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, getString(R.string.home_url)));
        startActivity(Intent.createChooser(share, getString(R.string.share_chooser)));
    }

    private void showSoundSafety() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.sound_safety)
                .setMessage("Esta es una experiencia sonora experimental, no un tratamiento médico ni una herramienta de diagnóstico.\n\nUsa un volumen cómodo, toma descansos y deja de escuchar si sientes incomodidad, mareo, ansiedad o zumbidos en los oídos.")
                .setPositiveButton(R.string.done, null)
                .show();
    }

    private void loadHome() {
        if (!webContentUnlocked) return;
        if (webView.getUrl() == null || !getString(R.string.home_url).equals(webView.getUrl())) {
            webView.loadUrl(getString(R.string.home_url));
        }
    }

    private void reloadPage() {
        errorView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if (!webContentUnlocked) {
            billingManager.start();
        } else if (webView.getUrl() == null) {
            webView.loadUrl(getString(R.string.home_url));
        } else {
            webView.reload();
        }
    }

    private void updateNavigationButtons() {
        if (webView == null) return;
        boolean canGoBack = webView.canGoBack();
        boolean canGoForward = webView.canGoForward();
        backButton.setEnabled(canGoBack);
        backButton.setAlpha(canGoBack ? 1f : 0.35f);
        forwardButton.setEnabled(canGoForward);
        forwardButton.setAlpha(canGoForward ? 1f : 0.35f);
    }

    @Override
    public void onBackPressed() {
        if (webContentUnlocked && webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webContentUnlocked && webView != null) webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (billingManager != null) billingManager.close();
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
        }
        super.onDestroy();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
