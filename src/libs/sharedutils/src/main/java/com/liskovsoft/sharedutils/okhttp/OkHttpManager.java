package com.liskovsoft.sharedutils.okhttp;

import android.os.Build.VERSION;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import androidx.annotation.Nullable;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.CipherSuite;
import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Protocol;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.net.Authenticator;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.liskovsoft.sharedutils.BuildConfig;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.okhttp.interceptors.RateLimitInterceptor;
import com.liskovsoft.sharedutils.okhttp.interceptors.UnzippingInterceptor;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;

import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor;

public class OkHttpManager {

    private static final String TAG = OkHttpManager.class.getSimpleName();

    public static final long CONNECT_TIMEOUT_MS = 20_000;
    public static final long READ_TIMEOUT_MS = 20_000;
    public static final long WRITE_TIMEOUT_MS = 20_000;
    
    private static OkHttpManager sInstance;
    
    private OkHttpClient mClient;

    public static OkHttpManager instance() {
        
        if (sInstance == null)
            sInstance = new OkHttpManager();
        
        ThreadPolicy.Builder builder = new ThreadPolicy.Builder();
        builder = builder.permitNetwork();
        StrictMode.setThreadPolicy(builder.build());

        return sInstance;
    }

    public Response doPostRequest(
        String url, 
        Map<String, String> headers, 
        String postBody, 
        @Nullable String contentType
    ) {

        if (headers == null)
            headers = new HashMap<>();

        Request okHttpRequest = new Request.Builder()
            .url(url)
            .headers(Headers.of(headers))
            .post(RequestBody.create(
                contentType != null ? MediaType.parse(contentType) : null, 
                postBody
            ))
            .build();

        return handleRequest(getClient(), okHttpRequest);
    }

    public Response doGetRequest(String url) {

        Request okHttpRequest = new Request.Builder()
            .url(url)
            .get()
            .build();

        return handleRequest(getClient(), okHttpRequest);
    }

    public Response doGetRequest(
        String url, 
        OkHttpClient client, 
        Map<String, String> headers
    ) {
        
        if (headers == null)
            headers = new HashMap<>();
        
        Request okHttpRequest = new Request.Builder()
            .url(url)
            .headers(Headers.of(headers))
            .build();

        return handleRequest(client, okHttpRequest);
    }

    private Response handleRequest(OkHttpClient client, Request okHttpRequest) {
        try {
            return client.newCall(okHttpRequest).execute();
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage()); // network error
            throw new IllegalStateException("Interrupted OkHttp request to " + okHttpRequest.url(), ex);
        }
    }

    public OkHttpClient getClient() {
        
        if (mClient == null) {

            OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();

            // May help with 'java.net.ProtocolException: Too many follow-up requests: 21'
            okBuilder.dns(PublicDnsResolver.google());

            // Alter cipher list to create unique TLS fingerprint
            ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .cipherSuites(APPROVED_CIPHER_SUITES)
                .build();

            okBuilder.connectionSpecs(Arrays.asList(cs, ConnectionSpec.CLEARTEXT));

            okBuilder.connectTimeout(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            okBuilder.readTimeout(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            okBuilder.writeTimeout(WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            okBuilder.connectionPool(new ConnectionPool(20, 5, TimeUnit.MINUTES));

            configureToIgnoreCertificate(okBuilder);
            
            okBuilder.protocols(Collections.singletonList(Protocol.HTTP_1_1));

            okBuilder.addInterceptor(new UnzippingInterceptor());
            
            if (BuildConfig.DEBUG) {
            
                okBuilder.addInterceptor(new OkHttpProfilerInterceptor());
                
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                okBuilder.addInterceptor(logging);

            }

            mClient = okBuilder.build();

        }

        return mClient;
    }
    
    // This is nearly equal to the cipher suites supported in Chrome 51, current as of 2016-05-25.
    // All of these suites are available on Android 7.0; earlier releases support a subset of these
    // suites. https://github.com/square/okhttp/issues/1972
    private static final CipherSuite[] APPROVED_CIPHER_SUITES = new CipherSuite[] {
            // TLSv1.3
            CipherSuite.TLS_AES_128_GCM_SHA256,
            CipherSuite.TLS_AES_256_GCM_SHA384,
            CipherSuite.TLS_CHACHA20_POLY1305_SHA256,
            CipherSuite.TLS_AES_128_CCM_SHA256,
            // Robolectric error (no such field). Constructing manually.
            //CipherSuite.TLS_AES_256_CCM_8_SHA256,
            CipherSuite.forJavaName("TLS_AES_256_CCM_8_SHA256"),

            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,

            // Note that the following cipher suites are all on HTTP/2's bad cipher suites list. We'll
            // continue to include them until better suites are commonly available. For example, none
            // of the better cipher suites listed above shipped with Android 4.4 or Java 7.
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA, // should be commented out?
            CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA,

            // Change TLS fingerprint by altering default cipher list
            // From original fix
            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
            // From NewPipe Downloader
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA
    };

    /**
     * Fix github updates on Android 4<br/>
     * Setting testMode configuration. If set as testMode, the connection will skip certification check
     */
    @SuppressWarnings("deprecation")
    private static void configureToIgnoreCertificate(OkHttpClient.Builder builder) {
        if (VERSION.SDK_INT > 19) {
            return;
        }

        Log.w(TAG, "Ignore Ssl Certificate");
        try {

            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            //final SSLContext sslContext = SSLContext.getInstance("SSL");
            final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = new Tls12SocketFactory(sslContext.getSocketFactory());

            //builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.sslSocketFactory(sslSocketFactory);
            //builder.hostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            Log.w(TAG, "Exception while configuring IgnoreSslCertificate: " + e, e);
        }
    }
    
}
