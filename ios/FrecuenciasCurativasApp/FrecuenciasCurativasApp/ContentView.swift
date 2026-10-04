import SwiftUI
import SafariServices

struct ContentView: View {
    private static let healingURL = URL(string: "https://infiniti-ia.com/free")!
    private static let privacyURL = URL(string: "https://infiniti-ia.com/privacidad")!

    @StateObject private var webViewStore = WebViewStore(destinationURL: ContentView.healingURL)
    @State private var showingSoundSafety = false
    @State private var showingPrivacyPolicy = false

    var body: some View {
        NavigationStack {
            ZStack {
                WebViewContainer(store: webViewStore)
                    .ignoresSafeArea(edges: .bottom)

                if webViewStore.isLoading {
                    ProgressView("Cargando Frecuencias Curativas")
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
            .navigationTitle("Frecuencias Curativas")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItemGroup(placement: .topBarLeading) {
                    Button(action: webViewStore.goBack) {
                        Label("Atrás", systemImage: "chevron.backward")
                    }
                    .disabled(!webViewStore.canGoBack)

                    Button(action: webViewStore.goForward) {
                        Label("Adelante", systemImage: "chevron.forward")
                    }
                    .disabled(!webViewStore.canGoForward)
                }

                ToolbarItemGroup(placement: .topBarTrailing) {
                    Button(action: webViewStore.reload) {
                        Label("Recargar", systemImage: "arrow.clockwise")
                    }

                    Menu {
                        ShareLink(item: webViewStore.shareURL()) {
                            Label("Compartir sitio", systemImage: "square.and.arrow.up")
                        }

                        Button {
                            showingSoundSafety = true
                        } label: {
                            Label("Seguridad sonora", systemImage: "ear")
                        }

                        Button {
                            showingPrivacyPolicy = true
                        } label: {
                            Label("Política de privacidad", systemImage: "hand.raised")
                        }
                    } label: {
                        Label("Más", systemImage: "ellipsis.circle")
                    }
                }
            }
        }
        .tint(Color(red: 0.72, green: 0.16, blue: 0.95))
        .preferredColorScheme(.dark)
        .sheet(isPresented: $showingSoundSafety) {
            SoundSafetyView()
        }
        .sheet(isPresented: $showingPrivacyPolicy) {
            PrivacyPolicyView(url: Self.privacyURL)
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

            Text("No se pudo conectar")
                .font(.title3.weight(.semibold))

            Text(message)
                .font(.callout)
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)

            Button("Intentar de nuevo", action: retry)
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
                Section("Nota de escucha") {
                    Text("Esta es una experiencia sonora experimental, no un tratamiento médico ni una herramienta de diagnóstico.")
                    Text("Usa un volumen cómodo, toma descansos y deja de escuchar si sientes incomodidad, mareo, ansiedad o zumbidos en los oídos.")
                }

                Section("Acerca de esta app") {
                    Text("Frecuencias Curativas App presenta el sintetizador Solfeggio de Infiniti IA dentro de una interfaz nativa para iPhone.")
                }
            }
            .navigationTitle("Seguridad sonora")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Listo") {
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}

private struct PrivacyPolicyView: UIViewControllerRepresentable {
    let url: URL

    func makeUIViewController(context: Context) -> SFSafariViewController {
        SFSafariViewController(url: url)
    }

    func updateUIViewController(_ viewController: SFSafariViewController, context: Context) {}
}
