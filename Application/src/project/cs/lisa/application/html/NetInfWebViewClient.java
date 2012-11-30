package project.cs.lisa.application.html;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import project.cs.lisa.R;
import project.cs.lisa.application.MainNetInfActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NetInfWebViewClient extends WebViewClient {

    /** Message indicator for a URL change (i.e. a link was clicked). */
    public static final String URL_WAS_UPDATED = "new_url";

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Intent intent = new Intent(URL_WAS_UPDATED);
        intent.putExtra("url", url);
        MainNetInfActivity.getActivity().sendBroadcast(intent);
        return false;
    }
    
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        img.setImageResource(R.drawable.cancel);
    }
    
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        Log.d("onPageFinished","onPageFinished");

        // clear web view cache to always load resource
        mWebView.clearCache(true);

        mBar.setVisibility(View.INVISIBLE);
        //                goButton.setEnabled(true);
        img.setImageResource(R.drawable.refresh);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view,
            String url) {

        Log.d("shouldInterceptRequest", url);
        
        /*
         * Ignore hashing raw data
         * String raw = "iVBORw0KGgoAAAANSUhEUgAAAFQAAABUCAYAAAAcaxDBAAADIUlEQVR42u3cwW3bMBQG4IygETSCNqgGKFAfe6sOvTcbxPce4g3iDeoNrA2sAQrYG1AbKFTyBDAySfFJpGlTf4D/EMSxrS8kRT7Seeq67gnxFyAAFKAARQAKUIAiAAUoQBGAAhSgAEUAClCAIgAFKEARgAIUoAhAAQpQgCIAvboYxlf1938l08pcZAqX3wGoHbRTcgCoX9AaoA6gEmpPYI1M5goqv9/QcNCnAugnSjlC+4JqAqWxVf1ZC9BPmGIEM6AWJlANZp8zQM2traNuPMauDY8V6gwANyU7qu37K0yATqPaInRzU4DOQxWmib4XUJ9fNHbVM1IyXmNLq5/Oc/obWnlvoI3mje4JYchedzGMP1gXMG1QULqAUkk20XJ0b7KcmD8O2d4BqAgG2uNpXnBjeGw50ZXU7t1YHps7oL7QOOgb8+S9y/cXpLTG7zJ/Rvk5arFDfss8e4jp+adSMG5MbfCbEnWlJnB3Cp0LrdFno3oBJcz2wTG/DEsaVOGC6gv0kBDmR0ul63JZerbel56JYQ7JGMWRSwqgZ5mjEt/PXzLKd+JRQXu4X6a5LI3lr56mRKVDgVlQqkcDPTOXlhnBBgMNugVyg1aZzVzGbha01jLaJl1ITIeWWDgsNcVM0Ip+93zTbeSA3TzTAPUrsZ1m3tuoY9nCWmfHGWJCgH4LkNxy8MAGURv+ENzXz6KdHBmV1nxks7CVNQZUznvIY4J6X/ppujl3abvXgJ44XT4ZUMthBG5yx3pr0qAnDejcwsuWUXNNFrR2KFK75gDQa9Ai0nOlCao5EsPJDi1UOXilQNSeZgvVKu/y42UercnZK62Fs4WkQHcajH9Llo50c2vXCtpqDr9mjEJyxdjzXwWotpU67KkfdVWhmSut5EA7U/WIkH4Q7gvt9+eWEt+c7e0kQa2ojpX7uTuyyYI6nVkyFJaXHLxIGnQ41VE5QOYLiimrAlVnAAe6az/THHVLVXyfR4FWA3qrRAUVqYFG/fCspzHrnnKMDZoxtxfuOR8fSIgKqsz53h64+wt6/3nwT6LgnwYAFKAARQAKUIAiAI2Qd1wqN+Bu1/KlAAAAAElFTkSuQmCC";
         * byte [] array_raw = Base64.decode(raw_, Base64.DEFAULT);
         * resource = new WebResourceResponse("image/jpg", "base64",
                    new ByteArrayInputStream(array_raw));
         */
        
        if (!url.startsWith("http")) {
            return null;
        }

        
        
        if (url.equals("http://www.google.se/images/srpr/logo3w.png")) {
            WebResourceResponse resource = null;

            try {
                resource = new WebResourceResponse("image/jpg", "base64",
                        FileUtils.openInputStream(new File(Environment.getExternalStorageDirectory() + "/cat.jpg")));
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("TAG", "Could not open file");
            }

            return resource;
        }
        
        // return super.shouldInterceptRequest(view, url);
    }
});
}
