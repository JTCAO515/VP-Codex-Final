package space.go2china.visepanda;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String API_BASE = "https://go2china.space";
    private static final int BG = Color.rgb(248, 250, 252);
    private static final int CARD = Color.WHITE;
    private static final int TEXT = Color.rgb(15, 23, 42);
    private static final int MUTED = Color.rgb(71, 85, 105);
    private static final int BORDER = Color.rgb(226, 232, 240);
    private static final int TEAL = Color.rgb(15, 118, 110);
    private static final int ORANGE = Color.rgb(249, 115, 22);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private SharedPreferences prefs;
    private LinearLayout content;
    private LinearLayout nav;
    private ScrollView scroller;
    private String activeTab = "plan";
    private String token = "";
    private JSONArray cities = new JSONArray();
    private JSONArray tools = new JSONArray();
    private LinearLayout chatLog;
    private EditText chatInput;
    private Spinner chatMode;
    private Spinner chatDepth;

    interface ApiSuccess {
        void onResult(String body) throws Exception;
    }

    interface ApiFailure {
        void onError(Exception error);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("visepanda", MODE_PRIVATE);
        token = prefs.getString("token", "");
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.WHITE);
            getWindow().setNavigationBarColor(Color.WHITE);
        }
        if (Build.VERSION.SDK_INT >= 23) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        buildShell();
        showPlan();
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private void buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        setContentView(root);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(18), dp(14), dp(18), dp(10));
        top.setBackgroundColor(Color.WHITE);
        root.addView(top, new LinearLayout.LayoutParams(-1, -2));

        TextView brand = text("VisePanda", 22, TEXT, true);
        brand.setLetterSpacing(0.02f);
        top.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));

        Button account = smallButton(token.length() == 0 ? "Account" : "Profile");
        account.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showAccountDialog(); }
        });
        top.addView(account);

        scroller = new ScrollView(this);
        scroller.setFillViewport(false);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(24));
        scroller.addView(content);
        root.addView(scroller, new LinearLayout.LayoutParams(-1, 0, 1));

        nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(8), dp(8), dp(8), dp(10));
        nav.setBackgroundColor(Color.WHITE);
        root.addView(nav, new LinearLayout.LayoutParams(-1, -2));
        rebuildNav();
    }

    private void rebuildNav() {
        nav.removeAllViews();
        addNav("plan", "Plan");
        addNav("ask", "Ask");
        addNav("cities", "Cities");
        addNav("tools", "Tools");
        addNav("trips", "Trips");
    }

    private void addNav(final String key, String label) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(label);
        b.setTextSize(13);
        b.setTextColor(key.equals(activeTab) ? Color.WHITE : MUTED);
        b.setBackground(pill(key.equals(activeTab) ? TEAL : Color.TRANSPARENT, key.equals(activeTab) ? TEAL : BORDER, 22));
        b.setMinHeight(dp(42));
        b.setPadding(dp(8), 0, dp(8), 0);
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { selectTab(key); }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(44), 1);
        lp.setMargins(dp(3), 0, dp(3), 0);
        nav.addView(b, lp);
    }

    private void selectTab(String key) {
        activeTab = key;
        rebuildNav();
        if ("plan".equals(key)) showPlan();
        if ("ask".equals(key)) showAsk("");
        if ("cities".equals(key)) showCities();
        if ("tools".equals(key)) showTools();
        if ("trips".equals(key)) showTrips();
        scroller.post(new Runnable() {
            @Override public void run() { scroller.scrollTo(0, 0); }
        });
    }

    private void clear() {
        content.removeAllViews();
    }

    private void showPlan() {
        activeTab = "plan";
        rebuildNav();
        clear();
        content.addView(heroCard());
        content.addView(sectionTitle("Trip Command Center", "Start here"));

        LinearLayout promptRow = row();
        addPrompt(promptRow, "First trip", "Plan a first-time 7 day China route with simple rail transfers.");
        addPrompt(promptRow, "Food route", "Compare Beijing, Shanghai, Chengdu, and Xi'an for a food-focused trip.");
        content.addView(promptRow);

        content.addView(readinessCard());
        content.addView(sectionTitle("Featured Cities", "City fit"));
        final LinearLayout featured = column();
        featured.addView(status("Loading city intelligence..."));
        content.addView(featured);
        ensureCities(new ApiSuccess() {
            @Override public void onResult(String body) {
                featured.removeAllViews();
                for (int i = 0; i < Math.min(4, cities.length()); i++) {
                    featured.addView(cityCard(cities.optJSONObject(i)));
                }
            }
        });
    }

    private View heroCard() {
        LinearLayout card = card();
        card.setBackground(heroBackground());
        TextView eyebrow = text("China travel planning for international visitors", 12, Color.rgb(15, 118, 110), true);
        card.addView(eyebrow);
        card.addView(text("VisePanda", 34, TEXT, true));
        card.addView(paragraph("Build a trip around real city fit, visa readiness, daily logistics, and a practical AI guide for international visitors."));

        final EditText destination = input("Destination idea");
        card.addView(destination);

        Button ask = primaryButton("Ask AI");
        ask.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String idea = destination.getText().toString().trim();
                if (idea.length() == 0) idea = "Plan a 7 day first-time China trip.";
                showAsk("Create a practical China travel plan for: " + idea);
            }
        });
        card.addView(ask, wideButtonLp());
        return card;
    }

    private void addPrompt(LinearLayout row, String label, final String prompt) {
        Button b = smallButton(label);
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showAsk(prompt); }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(44), 1);
        lp.setMargins(0, 0, dp(8), dp(8));
        row.addView(b, lp);
    }

    private View readinessCard() {
        LinearLayout card = card();
        card.addView(text("Readiness", 20, TEXT, true));
        String[] items = {
                "Passport valid for six months",
                "Visa or transit rule checked",
                "Payment apps linked to card",
                "Offline maps and translator ready"
        };
        for (String item : items) card.addView(checkLine(item));
        return card;
    }

    private void showAsk(String prefill) {
        activeTab = "ask";
        rebuildNav();
        clear();
        content.addView(sectionTitle("Ask VisePanda", "AI Guide"));

        LinearLayout controls = card();
        chatMode = spinner(new String[]{"Itinerary strategist", "Entry and visa analyst", "Budget analyst", "Transit planner", "Food and culture guide", "Safety checker", "City fit comparator", "General consultant"});
        chatDepth = spinner(new String[]{"Standard", "Quick", "Expert"});
        controls.addView(label("Mode"));
        controls.addView(chatMode);
        controls.addView(label("Depth"));
        controls.addView(chatDepth);
        content.addView(controls);

        chatLog = column();
        chatLog.addView(assistantBubble("Auto route ready. Choose a preset or ask a travel question."));
        content.addView(chatLog);

        LinearLayout presets = row();
        addPreset(presets, "Route audit", "Create a 10 day first-time China itinerary for a US traveler. Include rail logic, pacing, and what to skip.");
        addPreset(presets, "Entry check", "Build a China entry readiness checklist for a US passport holder visiting for tourism.");
        content.addView(presets);

        chatInput = input("Ask for a route, budget, visa checklist, or city comparison");
        chatInput.setMinLines(3);
        chatInput.setGravity(Gravity.TOP);
        content.addView(chatInput);
        if (prefill != null && prefill.length() > 0) chatInput.setText(prefill);

        Button send = primaryButton("Send");
        send.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { sendChat(); }
        });
        content.addView(send, wideButtonLp());
    }

    private void addPreset(LinearLayout row, String label, final String prompt) {
        Button b = smallButton(label);
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                chatInput.setText(prompt);
                chatInput.setSelection(chatInput.getText().length());
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(44), 1);
        lp.setMargins(0, 0, dp(8), dp(8));
        row.addView(b, lp);
    }

    private void sendChat() {
        final String message = chatInput.getText().toString().trim();
        if (message.length() == 0) {
            toast("Enter a travel question first.");
            return;
        }
        hideKeyboard();
        chatInput.setText("");
        chatLog.addView(userBubble(message));
        final TextView reply = assistantBubble("Thinking...");
        chatLog.addView(reply);
        scrollBottom();

        String mode = selectedModeId();
        String depth = selectedDepthId();
        JSONObject body = new JSONObject();
        try {
            body.put("message", message);
            body.put("provider", "auto");
            body.put("mode", mode);
            body.put("depth", depth);
        } catch (Exception ignored) {
        }

        request("POST", "/api/chat", body.toString(), false, new ApiSuccess() {
            @Override public void onResult(String raw) throws Exception {
                reply.setText(parseSseAnswer(raw));
                scrollBottom();
            }
        }, new ApiFailure() {
            @Override public void onError(Exception error) {
                reply.setText(localChatFallback(message));
            }
        });
    }

    private String selectedModeId() {
        int p = chatMode == null ? 0 : chatMode.getSelectedItemPosition();
        String[] ids = {"itinerary", "entry", "budget", "transit", "food", "safety", "city-fit", "custom"};
        return ids[Math.max(0, Math.min(p, ids.length - 1))];
    }

    private String selectedDepthId() {
        int p = chatDepth == null ? 0 : chatDepth.getSelectedItemPosition();
        String[] ids = {"standard", "quick", "expert"};
        return ids[Math.max(0, Math.min(p, ids.length - 1))];
    }

    private void showCities() {
        clear();
        content.addView(sectionTitle("City Explorer", "Destinations"));
        final EditText search = input("Search city, region, or highlight");
        content.addView(search);
        final LinearLayout list = column();
        list.addView(status("Loading destinations..."));
        content.addView(list);

        Button filter = smallButton("Search");
        filter.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { renderCities(list, search.getText().toString()); }
        });
        content.addView(filter, wideButtonLp());

        ensureCities(new ApiSuccess() {
            @Override public void onResult(String body) { renderCities(list, ""); }
        });
    }

    private void renderCities(LinearLayout list, String query) {
        list.removeAllViews();
        String q = query == null ? "" : query.toLowerCase();
        int count = 0;
        for (int i = 0; i < cities.length(); i++) {
            JSONObject city = cities.optJSONObject(i);
            if (city == null) continue;
            String hay = (city.optString("name") + " " + city.optString("province") + " " + city.optString("vibe") + " " + city.optJSONArray("highlights")).toLowerCase();
            if (q.length() == 0 || hay.contains(q)) {
                list.addView(cityCard(city));
                count++;
            }
        }
        if (count == 0) list.addView(status("No city match. Try Beijing, food, nature, or rail."));
    }

    private View cityCard(JSONObject city) {
        LinearLayout card = card();
        if (city == null) {
            card.addView(text("Beijing", 20, TEXT, true));
            card.addView(paragraph("Historic grandeur, Great Wall, hutongs, and first-time China logistics."));
            return card;
        }
        card.addView(text(city.optString("name", "City"), 20, TEXT, true));
        card.addView(text(city.optString("province", ""), 13, TEAL, true));
        card.addView(paragraph(city.optString("vibe", "")));
        card.addView(meta("Best season: " + city.optString("bestSeason", "Check season")));
        card.addView(meta("Duration: " + city.optString("duration", "Flexible")));
        JSONArray highlights = city.optJSONArray("highlights");
        if (highlights != null) {
            card.addView(meta("Highlights: " + join(highlights, 4)));
        }
        return card;
    }

    private void showTools() {
        clear();
        content.addView(sectionTitle("Travel Tools", "Before you go"));
        final LinearLayout list = column();
        list.addView(status("Loading tools..."));
        content.addView(list);
        ensureTools(new ApiSuccess() {
            @Override public void onResult(String body) {
                list.removeAllViews();
                for (int i = 0; i < tools.length(); i++) {
                    JSONObject tool = tools.optJSONObject(i);
                    if (tool != null) list.addView(toolCard(tool));
                }
            }
        });
    }

    private View toolCard(final JSONObject tool) {
        LinearLayout card = card();
        card.addView(text(tool.optString("name", "Travel tool"), 20, TEXT, true));
        card.addView(paragraph(tool.optString("description", "Practical travel helper.")));
        Button open = smallButton("Open");
        open.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showToolDetail(tool.optString("id")); }
        });
        card.addView(open, wideButtonLp());
        return card;
    }

    private void showToolDetail(final String id) {
        clear();
        content.addView(sectionTitle("Tool Detail", "Travel Tools"));
        final LinearLayout box = column();
        box.addView(status("Loading detail..."));
        content.addView(box);
        request("GET", "/api/tools/" + id, null, false, new ApiSuccess() {
            @Override public void onResult(String body) throws Exception {
                JSONObject tool = new JSONObject(body).optJSONObject("tool");
                renderToolDetail(box, tool);
            }
        }, new ApiFailure() {
            @Override public void onError(Exception error) { renderLocalTool(box, id); }
        });
        Button back = smallButton("Back to tools");
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showTools(); }
        });
        content.addView(back, wideButtonLp());
    }

    private void renderToolDetail(LinearLayout box, JSONObject tool) {
        box.removeAllViews();
        LinearLayout card = card();
        if (tool == null) {
            card.addView(text("Travel Tool", 20, TEXT, true));
            card.addView(paragraph("Tool detail is unavailable."));
            box.addView(card);
            return;
        }
        card.addView(text(tool.optString("name", "Travel Tool"), 22, TEXT, true));
        if (tool.has("summary")) card.addView(paragraph(tool.optString("summary")));
        JSONArray items = tool.optJSONArray("items");
        if (items != null) {
            for (int i = 0; i < items.length(); i++) {
                Object raw = items.opt(i);
                if (raw instanceof JSONObject) {
                    JSONObject item = (JSONObject) raw;
                    String line = item.optString("label", item.optString("english", item.optString("context", "")));
                    String extra = item.optString("amount", item.optString("pinyin", ""));
                    card.addView(checkLine(extra.length() > 0 ? line + " - " + extra : line));
                }
            }
        }
        JSONObject numbers = tool.optJSONObject("numbers");
        if (numbers != null) {
            String[] keys = {"police", "fire", "ambulance", "traffic"};
            for (String key : keys) card.addView(checkLine(key + ": " + numbers.optString(key)));
        }
        box.addView(card);
    }

    private void renderLocalTool(LinearLayout box, String id) {
        JSONObject tool = new JSONObject();
        try {
            tool.put("name", "packing".equals(id) ? "Packing Checklist" : "China Travel Tool");
            tool.put("summary", "Check passport, visa or transit rule, hotel confirmations, payment apps, offline maps, and backup translation before departure.");
        } catch (Exception ignored) {
        }
        renderToolDetail(box, tool);
    }

    private void showTrips() {
        clear();
        content.addView(sectionTitle("Trips", token.length() == 0 ? "Guest drafts" : "Saved plans"));
        content.addView(tripForm());
        final LinearLayout list = column();
        list.addView(status("Loading trips..."));
        content.addView(list);
        loadTrips(list);
    }

    private View tripForm() {
        LinearLayout card = card();
        card.addView(text("Save a trip", 20, TEXT, true));
        final EditText title = input("Trip title");
        final EditText destination = input("Destination");
        card.addView(title);
        card.addView(destination);
        Button save = primaryButton("Save trip");
        save.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String t = title.getText().toString().trim();
                if (t.length() == 0) {
                    toast("Trip title is required.");
                    return;
                }
                saveTrip(t, destination.getText().toString().trim());
            }
        });
        card.addView(save, wideButtonLp());
        return card;
    }

    private void loadTrips(final LinearLayout list) {
        if (token.length() == 0) {
            renderTrips(list, localTrips());
            return;
        }
        request("GET", "/api/trips", null, true, new ApiSuccess() {
            @Override public void onResult(String body) throws Exception {
                JSONArray trips = new JSONObject(body).optJSONArray("trips");
                renderTrips(list, trips == null ? new JSONArray() : trips);
            }
        }, new ApiFailure() {
            @Override public void onError(Exception error) {
                list.removeAllViews();
                list.addView(status("Could not load account trips. Showing guest drafts."));
                renderTrips(list, localTrips());
            }
        });
    }

    private void renderTrips(LinearLayout list, JSONArray trips) {
        list.removeAllViews();
        if (trips.length() == 0) {
            list.addView(status("No trips yet. Add a first route draft above."));
            return;
        }
        for (int i = 0; i < trips.length(); i++) {
            JSONObject trip = trips.optJSONObject(i);
            LinearLayout card = card();
            card.addView(text(trip == null ? "Trip" : trip.optString("title", "Trip"), 20, TEXT, true));
            card.addView(paragraph(trip == null ? "" : trip.optString("destination", "China")));
            list.addView(card);
        }
    }

    private void saveTrip(final String title, final String destination) {
        if (token.length() == 0) {
            JSONArray trips = localTrips();
            JSONObject trip = new JSONObject();
            try {
                trip.put("title", title);
                trip.put("destination", destination.length() == 0 ? "China" : destination);
                trips.put(0, trip);
            } catch (Exception ignored) {
            }
            prefs.edit().putString("guest_trips", trips.toString()).apply();
            toast("Guest trip saved.");
            showTrips();
            return;
        }
        JSONObject body = new JSONObject();
        try {
            body.put("title", title);
            body.put("destination", destination);
        } catch (Exception ignored) {
        }
        request("POST", "/api/trips", body.toString(), true, new ApiSuccess() {
            @Override public void onResult(String body) {
                toast("Trip saved.");
                showTrips();
            }
        }, new ApiFailure() {
            @Override public void onError(Exception error) { toast(error.getMessage()); }
        });
    }

    private JSONArray localTrips() {
        try {
            return new JSONArray(prefs.getString("guest_trips", "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private void showAccountDialog() {
        if (token.length() > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("VisePanda account")
                    .setMessage("You are signed in on this device.")
                    .setPositiveButton("Sign out", (dialog, which) -> {
                        token = "";
                        prefs.edit().remove("token").apply();
                        toast("Signed out.");
                        rebuildNav();
                    })
                    .setNegativeButton("Close", null)
                    .show();
            return;
        }
        LinearLayout box = column();
        box.setPadding(dp(12), dp(8), dp(12), 0);
        final EditText email = input("Email");
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        final EditText password = input("Password");
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        box.addView(email);
        box.addView(password);
        new AlertDialog.Builder(this)
                .setTitle("Sign in")
                .setView(box)
                .setPositiveButton("Sign in", (dialog, which) -> auth(email.getText().toString(), password.getText().toString(), false))
                .setNegativeButton("Create account", (dialog, which) -> auth(email.getText().toString(), password.getText().toString(), true))
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void auth(String email, String password, boolean register) {
        if (email.trim().length() == 0 || password.trim().length() == 0) {
            toast("Email and password are required.");
            return;
        }
        JSONObject body = new JSONObject();
        try {
            body.put("email", email.trim());
            body.put("password", password);
        } catch (Exception ignored) {
        }
        request("POST", register ? "/api/auth/register" : "/api/auth/login", body.toString(), false, new ApiSuccess() {
            @Override public void onResult(String raw) throws Exception {
                JSONObject data = new JSONObject(raw);
                String nextToken = data.optString("token");
                if (nextToken.length() > 0) {
                    token = nextToken;
                    prefs.edit().putString("token", token).apply();
                    toast("Signed in.");
                    rebuildNav();
                } else {
                    toast(data.optString("message", "Check your email verification code."));
                }
            }
        }, new ApiFailure() {
            @Override public void onError(Exception error) { toast(error.getMessage()); }
        });
    }

    private void ensureCities(final ApiSuccess success) {
        if (cities.length() > 0) {
            try {
                success.onResult("");
            } catch (Exception ignored) {
            }
            return;
        }
        request("GET", "/api/cities", null, false, new ApiSuccess() {
            @Override public void onResult(String body) throws Exception {
                cities = new JSONObject(body).optJSONArray("cities");
                if (cities == null) cities = fallbackCities();
                success.onResult(body);
            }
        }, new ApiFailure() {
            @Override public void onError(Exception error) {
                cities = fallbackCities();
                try {
                    success.onResult("");
                } catch (Exception ignored) {
                }
            }
        });
    }

    private void ensureTools(final ApiSuccess success) {
        if (tools.length() > 0) {
            try {
                success.onResult("");
            } catch (Exception ignored) {
            }
            return;
        }
        request("GET", "/api/tools", null, false, new ApiSuccess() {
            @Override public void onResult(String body) throws Exception {
                tools = new JSONObject(body).optJSONArray("tools");
                if (tools == null) tools = fallbackTools();
                success.onResult(body);
            }
        }, new ApiFailure() {
            @Override public void onError(Exception error) {
                tools = fallbackTools();
                try {
                    success.onResult("");
                } catch (Exception ignored) {
                }
            }
        });
    }

    private void request(final String method, final String path, final String body, final boolean auth, final ApiSuccess success, final ApiFailure failure) {
        executor.execute(new Runnable() {
            @Override public void run() {
                try {
                    URL url = new URL(API_BASE + path);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod(method);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(30000);
                    conn.setRequestProperty("Accept", "application/json, text/event-stream");
                    if (auth && token.length() > 0) conn.setRequestProperty("Authorization", "Bearer " + token);
                    if (body != null) {
                        conn.setDoOutput(true);
                        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                        OutputStream os = conn.getOutputStream();
                        os.write(body.getBytes("UTF-8"));
                        os.close();
                    }
                    int code = conn.getResponseCode();
                    String raw = readAll(code >= 400 ? conn.getErrorStream() : conn.getInputStream());
                    if (code >= 400) throw new Exception(errorMessage(raw, code));
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            try {
                                success.onResult(raw);
                            } catch (Exception e) {
                                failure.onError(e);
                            }
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override public void run() { failure.onError(e); }
                    });
                }
            }
        });
    }

    private String readAll(InputStream stream) throws Exception {
        if (stream == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append('\n');
        br.close();
        return sb.toString();
    }

    private String errorMessage(String raw, int code) {
        try {
            JSONObject data = new JSONObject(raw);
            JSONObject error = data.optJSONObject("error");
            if (error != null) return error.optString("message", "Request failed.");
            return data.optString("message", "Request failed.");
        } catch (Exception e) {
            return "Request failed (" + code + ").";
        }
    }

    private String parseSseAnswer(String raw) {
        StringBuilder out = new StringBuilder();
        String[] lines = raw.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.startsWith("data:")) continue;
            try {
                JSONObject event = new JSONObject(line.substring(5).trim());
                out.append(event.optString("token", ""));
            } catch (Exception ignored) {
            }
        }
        String answer = out.toString().trim();
        return answer.length() == 0 ? "I could not read the assistant response. Try again." : answer;
    }

    private String localChatFallback(String message) {
        return "I would start with Beijing, Shanghai, Xi'an, or Chengdu depending on your trip style. Check entry rules, payment setup, hotel areas, rail timing, and backup translation before booking. For a sharper plan, add nationality, travel month, total days, budget, and whether food, history, nature, or comfort matters most.";
    }

    private JSONArray fallbackCities() {
        JSONArray arr = new JSONArray();
        try {
            arr.put(city("Beijing", "Beijing", "Historic grandeur + modern capital", "Spring/Autumn", "3-7 days", "Forbidden City, Great Wall, Hutongs, Peking Duck"));
            arr.put(city("Shanghai", "Shanghai", "Chic metropolis + Shanghai-style culture", "Spring/Autumn", "2-5 days", "The Bund, French Concession, Yu Garden"));
            arr.put(city("Chengdu", "Sichuan", "Food capital + laid-back lifestyle", "Spring/Autumn", "3-6 days", "Hotpot, pandas, teahouses"));
            arr.put(city("Xi'an", "Shaanxi", "Ancient capital + Silk Road food", "Spring/Autumn", "2-4 days", "Terracotta Army, city wall, Muslim Quarter"));
        } catch (Exception ignored) {
        }
        return arr;
    }

    private JSONObject city(String name, String province, String vibe, String season, String duration, String highlights) throws Exception {
        JSONObject item = new JSONObject();
        item.put("name", name);
        item.put("province", province);
        item.put("vibe", vibe);
        item.put("bestSeason", season);
        item.put("duration", duration);
        JSONArray h = new JSONArray();
        for (String part : highlights.split(",")) h.put(part.trim());
        item.put("highlights", h);
        return item;
    }

    private JSONArray fallbackTools() {
        JSONArray arr = new JSONArray();
        try {
            arr.put(tool("packing", "Packing Checklist", "Passport, visa proof, hotel confirmations, payments, maps, and translator."));
            arr.put(tool("visa", "China Visa Guide", "Check nationality rules, entry duration, documents, and transit options."));
            arr.put(tool("phrases", "Useful Chinese Phrases", "Taxi, dining, transit, hotel, and emergency phrases."));
            arr.put(tool("emergency", "Emergency Info", "Police 110, fire 119, ambulance 120, traffic 122."));
        } catch (Exception ignored) {
        }
        return arr;
    }

    private JSONObject tool(String id, String name, String desc) throws Exception {
        JSONObject item = new JSONObject();
        item.put("id", id);
        item.put("name", name);
        item.put("description", desc);
        return item;
    }

    private String join(JSONArray arr, int max) {
        List<String> parts = new ArrayList<String>();
        for (int i = 0; i < Math.min(max, arr.length()); i++) parts.add(arr.optString(i));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(parts.get(i));
        }
        return sb.toString();
    }

    private TextView sectionTitle(String title, String eyebrow) {
        TextView view = text(eyebrow.toUpperCase() + "\n" + title, 22, TEXT, true);
        view.setPadding(0, dp(12), 0, dp(10));
        return view;
    }

    private LinearLayout card() {
        LinearLayout v = column();
        v.setPadding(dp(16), dp(16), dp(16), dp(16));
        v.setBackground(cardBackground());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(12));
        v.setLayoutParams(lp);
        return v;
    }

    private LinearLayout column() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        return v;
    }

    private LinearLayout row() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.HORIZONTAL);
        return v;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setLineSpacing(dp(2), 1.0f);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private TextView paragraph(String value) {
        TextView t = text(value, 15, MUTED, false);
        t.setPadding(0, dp(8), 0, dp(8));
        return t;
    }

    private TextView meta(String value) {
        TextView t = text(value, 13, MUTED, false);
        t.setPadding(0, dp(2), 0, dp(2));
        return t;
    }

    private TextView label(String value) {
        TextView t = text(value, 12, MUTED, true);
        t.setPadding(0, dp(8), 0, dp(4));
        return t;
    }

    private TextView status(String value) {
        TextView t = text(value, 14, MUTED, false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(18), dp(12), dp(18));
        return t;
    }

    private TextView checkLine(String value) {
        TextView t = text("• " + value, 15, TEXT, false);
        t.setPadding(0, dp(6), 0, dp(2));
        return t;
    }

    private EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextColor(TEXT);
        e.setHintTextColor(Color.rgb(100, 116, 139));
        e.setTextSize(15);
        e.setSingleLine(false);
        e.setBackground(pill(Color.WHITE, BORDER, 14));
        e.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(52));
        lp.setMargins(0, dp(8), 0, dp(8));
        e.setLayoutParams(lp);
        return e;
    }

    private Spinner spinner(String[] values) {
        Spinner s = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);
        return s;
    }

    private Button primaryButton(String label) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(label);
        b.setTextSize(15);
        b.setTextColor(Color.WHITE);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(pill(TEAL, TEAL, 16));
        return b;
    }

    private Button smallButton(String label) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(label);
        b.setTextSize(14);
        b.setTextColor(TEXT);
        b.setBackground(pill(Color.WHITE, BORDER, 16));
        return b;
    }

    private LinearLayout.LayoutParams wideButtonLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(48));
        lp.setMargins(0, dp(8), 0, dp(4));
        return lp;
    }

    private TextView userBubble(String value) {
        TextView t = text(value, 15, Color.WHITE, false);
        t.setPadding(dp(14), dp(12), dp(14), dp(12));
        t.setBackground(pill(TEAL, TEAL, 18));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(dp(42), dp(8), 0, dp(8));
        t.setLayoutParams(lp);
        return t;
    }

    private TextView assistantBubble(String value) {
        TextView t = text(value, 15, TEXT, false);
        t.setPadding(dp(14), dp(12), dp(14), dp(12));
        t.setBackground(pill(Color.WHITE, BORDER, 18));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(8), dp(24), dp(8));
        t.setLayoutParams(lp);
        return t;
    }

    private GradientDrawable cardBackground() {
        return pill(CARD, BORDER, 18);
    }

    private GradientDrawable heroBackground() {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{Color.WHITE, Color.rgb(236, 253, 245)});
        g.setStroke(dp(1), BORDER);
        g.setCornerRadius(dp(22));
        return g;
    }

    private GradientDrawable pill(int fill, int stroke, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        g.setStroke(dp(1), stroke);
        return g;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void toast(String value) {
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
    }

    private void scrollBottom() {
        scroller.postDelayed(new Runnable() {
            @Override public void run() { scroller.fullScroll(View.FOCUS_DOWN); }
        }, 80);
    }

    private void hideKeyboard() {
        try {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(chatInput.getWindowToken(), 0);
        } catch (Exception ignored) {
        }
    }
}
