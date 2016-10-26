package meido.tecotaku.cn.meidomodule;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Administrator on 2016/10/7 0007.
 */
public class MService extends AccessibilityService {

    private final static String TAG = "Tagg";

    @Override public void onInterrupt() {}
    @Override protected void onServiceConnected() {super.onServiceConnected();}

    private void get(final boolean flag, final String name) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://tecotaku.cn:8082/md/get", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String resp = new String(responseBody);
                if (flag) resp = "@" + name + " " + resp;
                doSendMessage(resp);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i(TAG, "GET failed" + error.toString());
                handling = false;
            }

        });

    }
    Handler handler = new Handler (){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x001) {
                Log.i(TAG, "RELEASE LOCK");
                handling = false;
            }
            super.handleMessage(msg);
        }
    };

    private void doSendMessage (String a) {
        lastReply = a;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label",  a);
        clipboard.setPrimaryClip(clip);
        edit.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        send.performAction(AccessibilityNodeInfo.ACTION_CLICK);

    }

    private void send(String a) {
        AsyncHttpClient client = new AsyncHttpClient();
        a = hanSpec(a);
        client.get("http://tecotaku.cn:8082/md/add?word=\""+a+"\"", new AsyncHttpResponseHandler(){
            @Override public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {}
            @Override public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {}
        });
    }

    private String hanSpec(String a) {
        a = a.replaceAll("@", "");
        a = a.replaceAll("#", "");
        a = a.replaceAll("$", "");
        a = a.replaceAll("%", "");
        a = a.replaceAll("^", "");
        a = a.replaceAll("\\*", "");
        a = a.replaceAll("\\(", "");
        a = a.replaceAll("\\)", "");
        return a;
    }

    AccessibilityNodeInfo edit, send;
    boolean handling = false;
    String last,lastPeople,lastReply;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG, "Event get=============================================================================="+(handling?"handling":"freeing"));
        if (handling == true) return;
        AccessibilityNodeInfo source = getRootInActiveWindow();
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            Log.i(TAG, "Event get=============================================================================="+(source==null?"source null":"not null"));
            if(source == null) return;
            List<AccessibilityNodeInfo> a = source.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/chat_item_content_layout");
            List<AccessibilityNodeInfo> b = source.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/chat_item_nick_name");
            List<AccessibilityNodeInfo> c = source.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/input");
            List<AccessibilityNodeInfo> d = source.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/fun_btn");
            if (a==null|b==null||c==null||d==null||a.size() == 0||c.size()==0||d.size()==0) return;
            edit = c.get(0);
            send = d.get(0);
            if (a.get(a.size()-1) == null || a.get(a.size()-1).getText() == null) return;
            String l = a.get(a.size()-1).getText().toString();
            Log.i(TAG, "HANDLING MESSAGE:" + l + " LAST:" + last + " lastReply" + lastReply);

            boolean single = false;

            if (b.size() == 0) single = true;
            else single = false;



            if (l.contains("%%")||single) {
                if (!single) l = l.replaceAll("%%", "");
                boolean noneed = false;
                if (l.equals(last)||l.equals(lastReply)) return;
                Iterator iter = Constants.ts.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String key = (String)entry.getKey();
                    String val = (String)entry.getValue();
                    if (l.contains(key)){
                        noneed = true;
                        doSendMessage(val);
                    }
                }
                if (!noneed) {
                    handling = true;
                    if (single) {
                        Log.i(TAG, "SINGLE");
                        Log.i(TAG, "HANDLING MESSAGE:" + l + " LAST:" + last + " LastReply:" + lastReply);
                        get(false, "");
                    } else {
                        if (b.get(b.size()-1) == null || b.get(b.size()-1).getText() == null) return;
                        String people = b.get(b.size() - 1).getText().toString();
                        people.replaceAll(":", "");
                        Log.i(TAG, "GROUP TO :" + people);
                        Log.i(TAG, "HANDLING MESSAGE:" + l + " LAST:" + last + " LastReply:" + lastReply);
                        if (!people.equals(lastPeople)) get(true, people);
                        lastPeople = people;
                    }
                }
            }
            handler.sendEmptyMessageDelayed(0x001, 200);
            if (!l.equals(last)) send(l);
            last = l;
        }
    }
}
