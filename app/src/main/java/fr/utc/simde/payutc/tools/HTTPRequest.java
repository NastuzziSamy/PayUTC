package fr.utc.simde.payutc.tools;

/**
 * Created by Samy on 24/10/2017.
 */

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPRequest {
    private static final String LOG_TAG = "_HTTPRequest";
    private String url;
    private HttpURLConnection request;
    private String response;

    private Map<String, String> postArgs;
    private Map<String, String> getArgs;
    private static Map<String, String> cookies;

    public HTTPRequest(final String url) {
        this.url = url;
        this.request = null;
        this.response = "";
        this.postArgs = new HashMap<String, String>();
        this.getArgs = new HashMap<String, String>();
        this.cookies = new HashMap<String, String>();
    }

    public int get() throws IOException {
        String get = args2String(this.getArgs, true);
        Log.d(LOG_TAG, "get: " + this.url + get);

        this.request = (HttpURLConnection) (new URL(this.url + get)).openConnection();
        this.request.setRequestMethod("GET");
        this.request.setRequestProperty("Cookie", getCookiesHeader());
        this.request.setUseCaches(false);
        this.request.setDoOutput(true);
        updateCookies(this.request.getHeaderFields().get("Set-Cookie"));

        generateResponse();
        Log.d(LOG_TAG, "code: " + Integer.toString(this.request.getResponseCode()) + ", response: " + this.getResponse());
        return this.request.getResponseCode();
    }

    public int post() throws IOException {
        String get = args2String(this.getArgs, true);
        String post = args2String(this.postArgs);
        Log.d(LOG_TAG, "post: " + this.url + get + ", data: " + post);

        this.request = (HttpURLConnection) (new URL(this.url + get)).openConnection();
        this.request.setRequestMethod("POST");
        this.request.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        this.request.setRequestProperty("Content-Length", Integer.toString(post.getBytes().length));
        this.request.setRequestProperty("Cookie", getCookiesHeader());
        this.request.setUseCaches(false);
        this.request.setDoInput(true);
        this.request.setDoOutput(true);
        this.request.getOutputStream().write(post.getBytes());
        updateCookies(this.request.getHeaderFields().get("Set-Cookie"));

        generateResponse();
        Log.d(LOG_TAG, "code: " + Integer.toString(this.request.getResponseCode()) + ", response: " + this.getResponse());
        return this.request.getResponseCode();
    }

    public Map<String, List<String>> getHeaders() {
        if (this.request == null)
            return null;

        return this.request.getHeaderFields();
    }

    public String getHeader(final String name) {
        if (this.request == null)
            return null;

        return this.request.getHeaderField(name);
    }

    public int getResponseCode() throws IOException {
        if (this.request == null)
            return Integer.parseInt(null);

        return this.request.getResponseCode();
    }

    public String getResponseMessage(final String name) throws IOException {
        if (this.request == null)
            return null;

        return this.request.getResponseMessage();
    }

    protected void generateResponse() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(this.request.getInputStream(), "UTF-8"));
        StringBuilder builder = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            builder.append(inputLine);

        in.close();

        this.response = builder.toString();
    }

    public String getResponse() throws IOException { return response; }

    protected String args2String(Map<String, String> args) throws UnsupportedEncodingException { return args2String(args, false); }
    protected String args2String(Map<String, String> args, Boolean isGet) throws UnsupportedEncodingException {
        String data = "";

        for (String arg : args.keySet())
            data += (URLEncoder.encode(arg, "UTF-8") + "=" + URLEncoder.encode(args.get(arg), "UTF-8") + "&");

        return data.equals("") ? "" : (isGet ? "?" : "") + data.substring(0, data.length() - 1);
    }

    public void setGet(Map<String, String> args) {
        this.getArgs = args;
    }

    public void setPost(Map<String, String> args) {
        this.postArgs = args;
    }

    public void addGet(final String key, final String value) {
        this.getArgs.put(key, value);
    }

    public void addPost(final String key, final String value) {
        this.postArgs.put(key, value);
    }

    synchronized String getCookiesHeader() {
        String data = "";

        for (String cookie : cookies.keySet())
            data += cookie + "=" + cookies.get(cookie) + "; ";

        return data;
    }

    synchronized void updateCookies(final List<String> cookiesHeader) {
        if (cookiesHeader != null) {
            Log.d(LOG_TAG, "cookies : " + cookiesHeader);

            for (String cookie : cookiesHeader) {
                try {
                    cookie = cookie.substring(0, cookie.indexOf(";"));
                    String cookieName = cookie.substring(0, cookie.indexOf("="));
                    String cookieValue = cookie.substring(cookie.indexOf("=") + 1);

                    this.cookies.put(cookieName, cookieValue);
                    Log.d(LOG_TAG, cookieName + " = " + cookieValue);
                }
                catch (Exception ex) {
                    Log.w(LOG_TAG, "error parsing cookie : '" + cookie + "'", ex);
                }
            }
        }
    }
}