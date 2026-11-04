package com.paytondev.launchpad;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** The preconditioning screen, with touch and TUNE control; the external hold service is unchanged. */
public final class MainActivity extends Activity {
    static final String EXTRA_FROM_KNOB = "from_knob";
    private static final int KEY_TUNE_UP = keyCode("KEYCODE_TUNE_UP", 223);
    private static final int KEY_TUNE_DOWN = keyCode("KEYCODE_TUNE_DOWN", 224);
    private static final int KEY_TUNE_ENTER = keyCode("KEYCODE_TUNE_ENTER", 243);
    private static final int STOP_RED = 0xffe5484d;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final List<Target> targets = new ArrayList<>();
    private FocusUi ui;
    private FrameLayout content;
    private TextView preconditionStatus, preconditionDetail, preconditionElapsed, preconditionButton;
    private TextView lowestValue, lowestCelsius, highestValue, temperatureAge;
    private View highestColumn, failureRow;
    private TemperatureGauge gauge;
    private TemperatureChart chart;
    private final CheckMark[] checkMarks = new CheckMark[6];
    private final TextView[] checkValues = new TextView[6];
    private Palette palette = Palette.LIGHT;
    private boolean carNight, compact, splitObserved;
    private int rightPadding;
    private PreconditioningService.LocalBinder vehicleService;
    private PreconditioningSnapshot preconditioning = PreconditioningSnapshot.checking();
    private BatteryTemperatureReading batteryTemperature = BatteryTemperatureReading.checking();
    private TemperatureHistory.Trace history = TemperatureHistory.Trace.EMPTY;
    private boolean vehicleBound;
    private int knobIndex = -1, toggleColor;
    private long resumedAt, enterAt;
    private boolean resumed, started, enterDown, enterLong;

    private static final class Target {
        final View view;
        final Runnable action;
        Target(View view, Runnable action) { this.view = view; this.action = action; }
        boolean enabled() { return view.isEnabled(); }
    }

    private final ContentObserver splitChanges = new ContentObserver(main) {
        @Override public void onChange(boolean selfChange) { applySplitScreen(); }
    };
    private final ServiceConnection vehicleConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder binder) {
            vehicleService = (PreconditioningService.LocalBinder) binder;
            updatePreconditioningViews();
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            vehicleService = null; preconditioning = PreconditioningSnapshot.checking();
            batteryTemperature = BatteryTemperatureReading.checking().update(null, SystemClock.elapsedRealtime());
            history = TemperatureHistory.Trace.EMPTY;
            updatePreconditioningViews();
        }
    };
    private final Runnable preconditioningTick = new Runnable() {
        @Override public void run() {
            if (!resumed) return;
            if (vehicleService != null) {
                preconditioning = vehicleService.snapshot();
                batteryTemperature = vehicleService.temperature();
                history = vehicleService.history();
            }
            updatePreconditioningViews(); main.postDelayed(this, 1000);
        }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_focus);
        content = (FrameLayout) findViewById(R.id.focus_content);
        carNight = DisplaySignals.night(this, getResources().getConfiguration());
        palette = Palette.of(carNight);
        ui = new FocusUi(this, palette); applyWindowColors();
        rightPadding = findViewById(R.id.focus_root).getPaddingRight();
        compact = splitScreenOn(); applySplitPadding();
        showScreen();
    }

    @Override protected void onStart() {
        super.onStart(); started = true;
        vehicleBound = bindService(new Intent(this, PreconditioningService.class), vehicleConnection, BIND_AUTO_CREATE);
    }
    @Override protected void onStop() {
        started = false;
        if (vehicleBound) { unbindService(vehicleConnection); vehicleBound = false; }
        vehicleService = null; super.onStop();
    }
    @Override protected void onResume() {
        super.onResume(); resumed = true; resumedAt = SystemClock.uptimeMillis();
        // A day/night switch while Launchpad was hidden may not have reached onConfigurationChanged.
        carNight = DisplaySignals.night(this, getResources().getConfiguration()); applyAppearance();
        try {
            getContentResolver().registerContentObserver(Settings.System.getUriFor(SplitScreen.SETTING), false, splitChanges);
            splitObserved = true;
        } catch (SecurityException e) { splitObserved = false; }
        applySplitScreen();
        main.removeCallbacks(preconditioningTick); preconditioningTick.run();
    }
    @Override protected void onPause() {
        resumed = false;
        main.removeCallbacks(preconditioningTick);
        if (splitObserved) { getContentResolver().unregisterContentObserver(splitChanges); splitObserved = false; }
        super.onPause();
    }
    @Override protected void onDestroy() {
        main.removeCallbacksAndMessages(null); super.onDestroy();
    }
    // uiMode is in configChanges, so a day/night switch arrives here instead of recreating.
    @Override public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        carNight = DisplaySignals.night(this, configuration); applyAppearance();
    }
    /** Rebuilds the screen in the resolved palette. */
    private void applyAppearance() {
        Palette next = Palette.of(carNight);
        if (next == palette) return;
        palette = next; ui = new FocusUi(this, palette); applyWindowColors();
        showScreen();
    }
    private boolean splitScreenOn() {
        try { return SplitScreen.on(Settings.System.getInt(getContentResolver(), SplitScreen.SETTING, 0)); }
        catch (SecurityException e) { return false; }
    }
    /** The map panel covers the right of this window; pad past it and use the compact layout. */
    private void applySplitScreen() {
        boolean next = splitScreenOn();
        if (next == compact) return;
        compact = next; applySplitPadding(); showScreen();
    }
    private void applySplitPadding() {
        View root = findViewById(R.id.focus_root);
        int right = compact ? SplitScreen.insetPx(getResources().getDisplayMetrics().widthPixels) : rightPadding;
        root.setPadding(root.getPaddingLeft(), root.getPaddingTop(), right, root.getPaddingBottom());
    }
    private void applyWindowColors() {
        findViewById(R.id.focus_root).setBackgroundColor(palette.background);
        getWindow().setBackgroundDrawable(new ColorDrawable(palette.background));
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent); setIntent(intent);
        // Android pauses a resumed activity before delivering onNewIntent, so its
        // visible/started state is the correct guard for the hold-to-close intent.
        if (intent.getBooleanExtra(EXTRA_FROM_KNOB, false) && started
            && SystemClock.uptimeMillis() - resumedAt > 700) finish();
    }
    private void showScreen() {
        targets.clear(); toggleColor = 0;
        content.removeAllViews();
        buildPreconditioning();
        knobIndex = KnobFocus.next(-1, 1, enabledTargets()); applyKnob();
        updatePreconditioningViews();
    }

    private TextView preconditioningButton() {
        TextView button = ui.text("Start", 26, palette.onAccent, true);
        button.setGravity(Gravity.CENTER);
        addTarget(button, new Runnable() { @Override public void run() { preconditioningAction(); } });
        button.setEnabled(false); return button;
    }
    private void preconditioningAction() {
        if (!preconditioning.canStop && !preconditioning.canStart) return;
        String action = preconditioning.canStop ? PreconditioningService.STOP
            : preconditioning.state == PreconditioningSnapshot.State.WARM
                ? PreconditioningService.START_ANYWAY : PreconditioningService.START;
        startService(new Intent(this, PreconditioningService.class).setAction(action));
        // Serialize presses until the service publishes the result.
        if (preconditionButton != null) preconditionButton.setEnabled(false);
    }

    private void buildPreconditioning() {
        LinearLayout body = ui.row();
        content.addView(body, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout control = panel(body, 0.34f, 0);
        control.addView(caption("BATTERY PRECONDITIONING"));
        preconditionStatus = ui.text("Checking", 34, palette.ink, true); preconditionStatus.setMaxLines(2);
        control.addView(preconditionStatus, below(10));
        preconditionElapsed = ui.text("Elapsed: —", 18, palette.ink, false);
        control.addView(preconditionElapsed, below(10));
        preconditionDetail = ui.text("Checking vehicle services", 16, palette.muted, false);
        preconditionDetail.setLineSpacing(0, 1.15f); preconditionDetail.setEllipsize(TextUtils.TruncateAt.END);
        preconditionDetail.setMaxLines(6);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(-1, 0, 1); detailParams.topMargin = ui.dp(14);
        control.addView(preconditionDetail, detailParams);
        preconditionButton = preconditioningButton();
        control.addView(preconditionButton, new LinearLayout.LayoutParams(ui.dp(compact ? 220 : 260), ui.dp(72)));

        LinearLayout temperature = panel(body, 0.40f, 16);
        LinearLayout readings = ui.row();
        LinearLayout lowest = ui.column(); lowest.addView(caption("LOWEST PACK"));
        LinearLayout values = ui.row(); values.setGravity(Gravity.BOTTOM);
        lowestValue = ui.text("—", 48, palette.ink, true); values.addView(lowestValue);
        lowestCelsius = ui.text("", 20, palette.muted, false);
        LinearLayout.LayoutParams celsiusParams = new LinearLayout.LayoutParams(-2, -2);
        celsiusParams.leftMargin = ui.dp(10); celsiusParams.bottomMargin = ui.dp(5); values.addView(lowestCelsius, celsiusParams);
        lowest.addView(values, below(6));
        readings.addView(lowest, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout highest = ui.column(); highest.setGravity(Gravity.RIGHT);
        highest.addView(caption("HIGHEST PACK"));
        highestValue = ui.text("—", 28, palette.ink, true); highest.addView(highestValue, below(6));
        highestColumn = highest; highest.setVisibility(View.GONE); readings.addView(highest);
        temperature.addView(readings);
        temperatureAge = ui.text("Checking…", 14, palette.muted, false); temperatureAge.setSingleLine(true);
        temperature.addView(temperatureAge, below(6));
        gauge = new TemperatureGauge(this); gauge.setPalette(palette);
        LinearLayout.LayoutParams gaugeParams = new LinearLayout.LayoutParams(-1, ui.dp(58)); gaugeParams.topMargin = ui.dp(8);
        temperature.addView(gauge, gaugeParams);
        chart = new TemperatureChart(this); chart.setPalette(palette);
        LinearLayout.LayoutParams chartParams = new LinearLayout.LayoutParams(-1, 0, 1); chartParams.topMargin = ui.dp(6);
        temperature.addView(chart, chartParams);

        LinearLayout checks = panel(body, 0.26f, 16);
        checks.addView(caption("VEHICLE CHECKS"));
        String[] labels = {"EV is READY", "Battery Conditioning", "Car Indicator", "Firmware Profile", "Temperature Source", "Failure Code"};
        for (int i = 0; i < labels.length; i++) {
            LinearLayout row = ui.row(); row.setGravity(Gravity.CENTER_VERTICAL);
            checkMarks[i] = new CheckMark(this);
            row.addView(checkMarks[i], new LinearLayout.LayoutParams(ui.dp(26), ui.dp(26)));
            LinearLayout text = ui.column();
            TextView label = ui.text(labels[i], 17, palette.ink, true); label.setSingleLine(true);
            text.addView(label);
            checkValues[i] = ui.text("Unknown", 14, palette.muted, false); checkValues[i].setSingleLine(true);
            checkValues[i].setEllipsize(TextUtils.TruncateAt.END); text.addView(checkValues[i], below(4));
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, -2, 1); textParams.leftMargin = ui.dp(12);
            row.addView(text, textParams);
            checks.addView(row, new LinearLayout.LayoutParams(-1, 0, 1));
            if (i == labels.length - 1) { failureRow = row; row.setVisibility(View.GONE); }
        }
    }
    private LinearLayout panel(LinearLayout parent, float weight, int gap) {
        LinearLayout panel = ui.column(); panel.setPadding(ui.dp(20), ui.dp(18), ui.dp(20), ui.dp(18));
        ui.background(panel, ui.rounded(palette.surface, 20, 0));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, weight); params.leftMargin = ui.dp(gap);
        parent.addView(panel, params); return panel;
    }
    private TextView caption(String value) { return ui.text(value, 12, palette.muted, true); }
    private LinearLayout.LayoutParams below(int margin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2); params.topMargin = ui.dp(margin); return params;
    }

    private void updatePreconditioningViews() {
        if (preconditionStatus == null) return;
        long now = SystemClock.elapsedRealtime();
        preconditionStatus.setText(preconditioning.title());
        if (preconditionDetail != null) preconditionDetail.setText(preconditioning.detail);
        if (preconditionElapsed != null) {
            boolean session = preconditioning.requested || preconditioning.state == PreconditioningSnapshot.State.ENDED
                || preconditioning.state == PreconditioningSnapshot.State.TIMED_OUT;
            preconditionElapsed.setVisibility(session ? View.VISIBLE : View.GONE);
            if (session) preconditionElapsed.setText("Elapsed: " + preconditioning.elapsed());
        }
        if (lowestValue != null) updateTemperatureViews(now);
        if (preconditionButton != null) {
            boolean warm = preconditioning.state == PreconditioningSnapshot.State.WARM;
            preconditionButton.setText(warm ? "Start anyway" : preconditioning.requested ? "Stop" : "Start");
            preconditionButton.setTextSize(warm ? 22 : 26);
            preconditionButton.setContentDescription(warm ? "Start preconditioning anyway" : preconditioning.requested ? "Stop preconditioning" : "Start preconditioning");
            int color = preconditioning.requested ? STOP_RED : warm ? TemperatureGauge.WARM : palette.accent;
            if (color != toggleColor) { toggleColor = color; ui.background(preconditionButton, ui.pill(color, 36)); }
            boolean enabled = preconditioning.canStart || preconditioning.canStop;
            preconditionButton.setEnabled(enabled); preconditionButton.setAlpha(enabled ? 1f : 0.4f);
            if (knobIndex >= 0 && knobIndex < targets.size() && !targets.get(knobIndex).enabled()) moveKnob(1);
            // The toggle is the only target, so it takes the knob as soon as it can be used.
            if (enabled && knobIndex != 0) { knobIndex = 0; applyKnob(); }
        }
    }
    private void updateTemperatureViews(long now) {
        Integer minimum = batteryTemperature.minimumC;
        boolean stale = batteryTemperature.stale(now);
        lowestValue.setText(minimum == null ? "—" : BatteryTemperatureReading.fahrenheit(minimum) + "°F");
        lowestValue.setTextColor(stale ? palette.muted : palette.ink);
        lowestCelsius.setText(minimum == null ? "" : minimum + "°C");
        temperatureAge.setText(batteryTemperature.age(now));
        TemperatureHistory.Sample latest = history.latest();
        Integer maximum = latest == null || stale || latest.at != batteryTemperature.receivedAt ? null : latest.maximumC;
        highestColumn.setVisibility(maximum == null ? View.GONE : View.VISIBLE);
        if (maximum != null) highestValue.setText(BatteryTemperatureReading.fahrenheit(maximum) + "°F · " + maximum + "°C");
        int threshold = vehicleService == null ? BatteryTemperature.EXPECTED_HEATING_THRESHOLD_C : vehicleService.warmThresholdC();
        gauge.setValue(minimum, threshold); chart.setTrace(history, now, threshold);
        setCheck(0, preconditioning.readyCheck(), preconditioning.readyValue());
        setCheck(1, preconditioning.enabledCheck(), preconditioning.enabledValue());
        setCheck(2, preconditioning.indicatorCheck(), preconditioning.indicatorValue());
        setCheck(3, preconditioning.supported, preconditioning.supportedValue());
        setCheck(4, minimum != null ? PreconditioningSnapshot.PASS : batteryTemperature.attempted
            ? PreconditioningSnapshot.FAIL : PreconditioningSnapshot.UNKNOWN,
            minimum != null ? "Native" : batteryTemperature.attempted ? "Not available on this firmware" : "Checking");
        failureRow.setVisibility(preconditioning.failureRaw >= 0 ? View.VISIBLE : View.GONE);
        setCheck(5, PreconditioningSnapshot.UNKNOWN, "Raw " + preconditioning.failureRaw + " · meaning unknown");
    }
    private void setCheck(int index, int state, String value) {
        checkMarks[index].setState(state); checkValues[index].setText(value);
    }

    private void addTarget(View view, Runnable action) {
        clickable(view, action); targets.add(new Target(view, action));
    }
    private void clickable(View view, final Runnable action) {
        // TUNE has one explicit selection model. Suppress the separate native
        // keyboard focus so it cannot draw a second highlight on another control.
        view.setFocusable(false);
        view.setClickable(true);
        view.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { action.run(); } });
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        int code = event.getKeyCode(); boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
        if (code == KEY_TUNE_UP || code == KEY_TUNE_DOWN || code == KeyEvent.KEYCODE_DPAD_RIGHT || code == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (down) moveKnob(code == KEY_TUNE_UP || code == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : -1);
            return true;
        }
        if (code == KEY_TUNE_ENTER || code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER) {
            if (down) {
                if (event.getRepeatCount() == 0) { enterDown = true; enterLong = false; enterAt = SystemClock.uptimeMillis(); }
                else enterLong = true;
            } else {
                if (enterDown && !enterLong && SystemClock.uptimeMillis() - enterAt < 800) activateKnob();
                enterDown = false; enterLong = false;
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
    private boolean[] enabledTargets() {
        boolean[] values = new boolean[targets.size()];
        for (int i = 0; i < values.length; i++) values[i] = targets.get(i).enabled();
        return values;
    }
    private void moveKnob(int delta) { knobIndex = KnobFocus.next(knobIndex, delta, enabledTargets()); applyKnob(); }
    private void applyKnob() {
        for (int i = 0; i < targets.size(); i++) {
            Target target = targets.get(i);
            target.view.setActivated(i == knobIndex);
            target.view.setSelected(i == knobIndex);
        }
    }
    private void activateKnob() {
        if (knobIndex < 0) moveKnob(1);
        else if (knobIndex < targets.size() && targets.get(knobIndex).enabled()) targets.get(knobIndex).action.run();
    }
    private static int keyCode(String name, int fallback) {
        int code = KeyEvent.keyCodeFromString(name); return code > 0 ? code : fallback;
    }
}
