package com.example.lab6_20203266;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20203266.databinding.ActivityLoginBinding;

// Google Identity (One Tap)
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;

// Firebase Auth
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

// Facebook
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding b;
    private FirebaseAuth auth;

    // Google One Tap
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private final ActivityResultLauncher<IntentSenderRequest> googleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), r -> {
                if (r.getResultCode() != RESULT_OK || r.getData() == null) {
                    toast("Google cancelado");
                    return;
                }
                try {
                    SignInCredential cred = oneTapClient.getSignInCredentialFromIntent(r.getData());
                    String idToken = cred.getGoogleIdToken();
                    if (idToken != null) {
                        AuthCredential c = GoogleAuthProvider.getCredential(idToken, null);
                        auth.signInWithCredential(c)
                                .addOnSuccessListener(ar -> goToMain())
                                .addOnFailureListener(e -> toast("Google error: " + e.getMessage()));
                    } else {
                        toast("Token Google nulo");
                    }
                } catch (ApiException e) {
                    toast("Google API: " + e.getMessage());
                }
            });

    // Facebook
    private CallbackManager fbCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        auth = FirebaseAuth.getInstance();

        // Si ya hay sesión y está OK, entrar
        FirebaseUser u = auth.getCurrentUser();
        if (u != null) {
            if (!isEmailPasswordOnly(u) || u.isEmailVerified()) {
                goToMain();
                return;
            }
        }

        // --- Email/Password ---
        b.btnLogin.setOnClickListener(v -> loginEmail());
        b.tvCrearCuenta.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        // --- Google One Tap ---
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = new BeginSignInRequest.Builder()
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(getString(R.string.default_web_client_id))
                                .setFilterByAuthorizedAccounts(false)
                                .build()
                ).build();

        b.btnGoogle.setOnClickListener(v -> {
            oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(result -> {
                        IntentSenderRequest req = new IntentSenderRequest
                                .Builder(result.getPendingIntent().getIntentSender())
                                .build();
                        googleLauncher.launch(req);
                    })
                    .addOnFailureListener(e -> toast("No se pudo iniciar Google: " + e.getMessage()));
        });

        // --- Facebook ---
        fbCallback = CallbackManager.Factory.create();
        b.btnFacebook.setOnClickListener(v -> {
            // limpiar cookies para poder cambiar de cuenta si estaba “pegada”
            forceFacebookReLogin();

            LoginManager.getInstance()
                    .setLoginBehavior(LoginBehavior.WEB_ONLY)
                    .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));

            LoginManager.getInstance().registerCallback(fbCallback, new FacebookCallback<LoginResult>() {
                @Override public void onSuccess(LoginResult loginResult) {
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }
                @Override public void onCancel() { toast("Facebook cancelado"); }
                @Override public void onError(FacebookException error) { toast("Facebook error: " + error.getMessage()); }
            });
        });
    }

    private boolean isEmailPasswordOnly(FirebaseUser u) {
        return u.getProviderData().size() == 2
                && "password".equals(u.getProviderData().get(1).getProviderId());
    }

    private void loginEmail() {
        String email = b.etEmail.getText() == null ? "" : b.etEmail.getText().toString().trim();
        String pass  = b.etPassword.getText() == null ? "" : b.etPassword.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) { toast("Completa correo y contraseña"); return; }
        b.progress.setVisibility(View.VISIBLE);
        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this::onEmailLoginResult);
    }

    private void onEmailLoginResult(Task<AuthResult> t) {
        b.progress.setVisibility(View.GONE);
        if (!t.isSuccessful()) {
            toast("Login error: " + (t.getException()!=null? t.getException().getMessage() : "desconocido"));
            return;
        }
        FirebaseUser u = auth.getCurrentUser();
        if (u == null) { toast("Sin usuario"); return; }
        if (isEmailPasswordOnly(u) && !u.isEmailVerified()) {
            startActivity(new Intent(this, VerifyEmailActivity.class));
            finish();
        } else {
            goToMain();
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnSuccessListener(ar -> {
                    // Pide email explícitamente (si el usuario lo tiene/autoriza)
                    requestFacebookEmail(token, this::goToMain);
                })
                .addOnFailureListener(e -> toast("Firebase/Facebook error: " + e.getMessage()));
    }

    private void requestFacebookEmail(AccessToken token, Runnable onDone) {
        GraphRequest req = GraphRequest.newMeRequest(token, (JSONObject obj, GraphResponse response) -> {
            // Si quisieras, puedes leer y guardar: String email = (obj!=null)? obj.optString("email", null) : null;
            if (onDone != null) onDone.run();
        });
        Bundle params = new Bundle();
        params.putString("fields", "id,name,email");
        req.setParameters(params);
        req.executeAsync();
    }

    private void forceFacebookReLogin() {
        try {
            LoginManager.getInstance().logOut();
            CookieManager cm = CookieManager.getInstance();
            cm.removeAllCookies(null);
            cm.flush();
        } catch (Exception ignored) {}
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void toast(String s){ Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (fbCallback != null) {
            fbCallback.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
