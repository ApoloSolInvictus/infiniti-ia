import SwiftUI

struct ContentView: View {
    private static let healingURL = URL(string: "https://infiniti-ia.com/english")!

    @StateObject private var webViewStore = WebViewStore(destinationURL: ContentView.healingURL)
    @State private var showingSoundSafety = false

    var body: some View {
        NavigationStack {
            ZStack {
                WebViewContainer(store: webViewStore)
                    .ignoresSafeArea(edges: .bottom)

                if webViewStore.isLoading {
                    ProgressView("Loading Healing Frequencies")
                        .padding(.horizontal, 18)
                        .padding(.vertical, 12)
                        .background(.ultraThinMaterial, in: Capsule())
                        .tint(.white)
                        .foregroundStyle(.white)
                }

                if let loadError = webViewStore.loadError {
                    ErrorStateView(message: loadError, retry: webViewStore.load)
                }
            }
            .background(Color(red: 0.01, green: 0.005, blue: 0.02))
            .navigationTitle("Healing Frequencies")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItemGroup(placement: .topBarLeading) {
                    Button(action: webViewStore.goBack) {
                        Label("Back", systemImage: "chevron.backward")
                    }
                    .disabled(!webViewStore.canGoBack)

                    Button(action: webViewStore.goForward) {
                        Label("Forward", systemImage: "chevron.forward")
                    }
                    .disabled(!webViewStore.canGoForward)
                }

                ToolbarItemGroup(placement: .topBarTrailing) {
                    Button(action: webViewStore.reload) {
                        Label("Reload", systemImage: "arrow.clockwise")
                    }

                    Menu {
                        ShareLink(item: webViewStore.shareURL()) {
                            Label("Share Website", systemImage: "square.and.arrow.up")
                        }

                        Button {
                            showingSoundSafety = true
                        } label: {
                            Label("Sound Safety", systemImage: "ear")
                        }
                    } label: {
                        Label("More", systemImage: "ellipsis.circle")
                    }
                }
            }
        }
        .tint(Color(red: 0.72, green: 0.16, blue: 0.95))
        .preferredColorScheme(.dark)
        .sheet(isPresented: $showingSoundSafety) {
            SoundSafetyView()
        }
    }
}

private struct ErrorStateView: View {
    let message: String
    let retry: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "wifi.exclamationmark")
                .font(.system(size: 40, weight: .medium))
                .foregroundStyle(.pink)

            Text("Unable to connect")
                .font(.title3.weight(.semibold))

            Text(message)
                .font(.callout)
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)

            Button("Try Again", action: retry)
                .buttonStyle(.borderedProminent)
        }
        .padding(28)
        .frame(maxWidth: 360)
        .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 18))
        .padding()
    }
}

private struct SoundSafetyView: View {
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Section("Listening note") {
                    Text("This is an experimental sound experience, not medical treatment or a diagnostic tool.")
                    Text("Use a comfortable volume, take breaks, and stop listening if you feel discomfort, dizziness, anxiety, or ringing in your ears.")
                }

                Section("About this app") {
                    Text("Healing Frequencies App presents the Infiniti IA Solfeggio synthesizer inside a native iPhone interface.")
                }
            }
            .navigationTitle("Sound Safety")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}
