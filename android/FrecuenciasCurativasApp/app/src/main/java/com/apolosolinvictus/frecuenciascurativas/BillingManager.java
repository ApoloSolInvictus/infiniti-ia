package com.apolosolinvictus.frecuenciascurativas;

import android.app.Activity;
import android.content.Context;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.Collections;
import java.util.List;

/** Coordinates the permanent one-time purchase without exposing billing details to the web page. */
public final class BillingManager implements PurchasesUpdatedListener {
    public interface Listener {
        void onOfferReady(String priceLabel);

        void onPurchaseStateChanged(boolean active, String message);

        void onBillingMessage(String message);
    }

    private final Context context;
    private final Listener listener;
    private final String productId;
    private final BillingClient billingClient;
    private ProductDetails productDetails;
    private boolean connected;

    public BillingManager(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        productId = context.getString(R.string.one_time_product_id);
        billingClient = BillingClient.newBuilder(this.context)
                .setListener(this)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .build())
                .enableAutoServiceReconnection()
                .build();
    }

    public void start() {
        if (connected) {
            queryProduct();
            queryPurchases(false);
            return;
        }

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    connected = true;
                    queryProduct();
                    queryPurchases(false);
                } else {
                    listener.onBillingMessage(context.getString(R.string.purchase_unavailable));
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                connected = false;
                listener.onBillingMessage(context.getString(R.string.purchase_billing_error));
            }
        });
    }

    private void queryProduct() {
        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build();
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(Collections.singletonList(product))
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, result) -> {
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK
                    || result == null
                    || result.getProductDetailsList() == null
                    || result.getProductDetailsList().isEmpty()) {
                listener.onBillingMessage(context.getString(R.string.purchase_unavailable));
                return;
            }

            productDetails = null;
            for (ProductDetails candidate : result.getProductDetailsList()) {
                if (productId.equals(candidate.getProductId())) {
                    productDetails = candidate;
                    break;
                }
            }

            if (productDetails == null
                    || productDetails.getOneTimePurchaseOfferDetailsList() == null
                    || productDetails.getOneTimePurchaseOfferDetailsList().isEmpty()) {
                listener.onBillingMessage(context.getString(R.string.purchase_unavailable));
                return;
            }

            ProductDetails.OneTimePurchaseOfferDetails offer =
                    productDetails.getOneTimePurchaseOfferDetailsList().get(0);
            listener.onOfferReady(offer.getFormattedPrice());
        });
    }

    public void launchPurchase(Activity activity) {
        if (!connected || productDetails == null) {
            listener.onBillingMessage(context.getString(R.string.purchase_unavailable));
            start();
            return;
        }

        BillingFlowParams.ProductDetailsParams productParams = BillingFlowParams.ProductDetailsParams
                .newBuilder()
                .setProductDetails(productDetails)
                .build();
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(Collections.singletonList(productParams))
                .build();
        BillingResult billingResult = billingClient.launchBillingFlow(activity, flowParams);
        if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            listener.onBillingMessage(context.getString(R.string.purchase_billing_error));
        }
    }

    public void refreshPurchases() {
        if (!connected) {
            start();
            return;
        }
        queryPurchases(true);
    }

    private void queryPurchases(boolean restoring) {
        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build();
        billingClient.queryPurchasesAsync(params, (billingResult, purchases) -> {
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                listener.onBillingMessage(context.getString(R.string.purchase_billing_error));
                return;
            }
            processPurchases(purchases, restoring);
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            processPurchases(purchases, false);
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            listener.onBillingMessage(context.getString(R.string.purchase_canceled));
        } else {
            listener.onBillingMessage(context.getString(R.string.purchase_billing_error));
        }
    }

    private void processPurchases(List<Purchase> purchases, boolean restoring) {
        boolean active = false;
        boolean pending = false;
        if (purchases != null) {
            for (Purchase purchase : purchases) {
                if (!purchase.getProducts().contains(productId)) continue;
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    active = true;
                    acknowledgeIfNeeded(purchase);
                } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                    pending = true;
                }
            }
        }

        if (pending && !active) {
            listener.onPurchaseStateChanged(false, context.getString(R.string.purchase_pending));
        } else if (active) {
            String message = restoring
                    ? context.getString(R.string.purchase_restored)
                    : context.getString(R.string.purchase_active);
            listener.onPurchaseStateChanged(true, message);
        } else {
            listener.onPurchaseStateChanged(false, "");
        }
    }

    private void acknowledgeIfNeeded(Purchase purchase) {
        if (purchase.isAcknowledged()) return;
        AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        billingClient.acknowledgePurchase(params, billingResult -> {
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                listener.onBillingMessage(context.getString(R.string.purchase_billing_error));
            }
        });
    }

    public void close() {
        billingClient.endConnection();
        connected = false;
    }
}
