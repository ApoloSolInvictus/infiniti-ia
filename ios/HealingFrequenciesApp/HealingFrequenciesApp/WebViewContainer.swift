import SwiftUI
import WebKit

final class WebViewStore: NSObject, ObservableObject, WKNavigationDelegate, WKUIDelegate {
    let destinationURL: URL

    @Published private(set) var isLoading = false
    @Published private(set) var loadError: String?
    @Published private(set) var canGoBack = false
    @Published private(set) var canGoForward = false

    private(set) var webView: WKWebView?

    init(destinationURL: URL) {
        self.destinationURL = destinationURL
        super.init()
    }

    func makeWebView() -> WKWebView {
        let configuration = WKWebViewConfiguration()
        let pagePreferences = WKWebpagePreferences()
        pagePreferences.allowsContentJavaScript = true

        configuration.defaultWebpagePreferences = pagePreferences
        configuration.allowsInlineMediaPlayback = true
        configuration.mediaTypesRequiringUserActionForPlayback = []

        let view = WKWebView(frame: .zero, configuration: configuration)
        view.navigationDelegate = self
        view.uiDelegate = self
        view.allowsBackForwardNavigationGestures = true
        view.scrollView.contentInsetAdjustmentBehavior = .never
        view.customUserAgent = "HealingFrequenciesApp/1.0"

        webView = view
        load()
        return view
    }

    func load() {
        guard let webView else { return }

        loadError = nil
        isLoading = true
        let request = URLRequest(
            url: destinationURL,
            cachePolicy: .useProtocolCachePolicy,
            timeoutInterval: 30
        )
        webView.load(request)
    }

    func reload() {
        loadError = nil
        webView?.reload()
    }

    func goBack() {
        webView?.goBack()
    }

    func goForward() {
        webView?.goForward()
    }

    func shareURL() -> URL {
        destinationURL
    }

    private func updateNavigationState(for webView: WKWebView) {
        canGoBack = webView.canGoBack
        canGoForward = webView.canGoForward
    }

    func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation?) {
        isLoading = true
        loadError = nil
        updateNavigationState(for: webView)
    }

    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation?) {
        isLoading = false
        loadError = nil
        updateNavigationState(for: webView)
    }

    func webView(_ webView: WKWebView, didFail navigation: WKNavigation?, withError error: Error) {
        finishWithError(webView, error: error)
    }

    func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation?, withError error: Error) {
        finishWithError(webView, error: error)
    }

    func webView(_ webView: WKWebView, didCommit navigation: WKNavigation?) {
        updateNavigationState(for: webView)
    }

    func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration, for navigationAction: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
        // Keep target=_blank links inside this app shell.
        if let url = navigationAction.request.url {
            webView.load(URLRequest(url: url))
        }
        return nil
    }

    private func finishWithError(_ webView: WKWebView, error: Error) {
        let nsError = error as NSError
        guard nsError.code != NSURLErrorCancelled else { return }

        isLoading = false
        loadError = "The Healing Frequencies page could not be loaded. Check your internet connection and try again."
        updateNavigationState(for: webView)
    }
}

struct WebViewContainer: UIViewRepresentable {
    @ObservedObject var store: WebViewStore

    func makeUIView(context: Context) -> WKWebView {
        store.makeWebView()
    }

    func updateUIView(_ webView: WKWebView, context: Context) {}
}
