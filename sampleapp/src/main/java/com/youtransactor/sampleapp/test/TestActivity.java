package com.youtransactor.sampleapp.test;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.log.ILogListener;
import com.youtransactor.sampleapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TestActivity extends AppCompatActivity implements ILogListener {
    private static final String TAG = TestActivity.class.getName();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM HH:mm:ss.SSS", Locale.FRANCE);

    private Button startDaemonBtn, stopDaemonBtn;
    private EditText startRunDelayEditText, numberOfRunsEditText;
    private List<String> logsList;
    private RecyclerView rvLogs;
    private LogsAdapter adapter;

    private Ticket ticketToReproduce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);

        initView();

        UCubeAPI.registerLogListener(this);
    }

    @Override
    protected void onDestroy() {
        stopTestDaemon();

        super.onDestroy();
    }

    private void initView() {
        startDaemonBtn = findViewById(R.id.startDaemonBtn);
        stopDaemonBtn = findViewById(R.id.stopDaemonBtn);
        startRunDelayEditText = findViewById(R.id.start_run_delay);
        numberOfRunsEditText = findViewById(R.id.number_of_runs);
        rvLogs = findViewById(R.id.rvLogs);

        logsList = new ArrayList<>();
        adapter = new LogsAdapter(logsList);
        rvLogs.setAdapter(adapter);
        LinearLayoutManager lm = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        rvLogs.setLayoutManager(lm);

        /*auto test calls*/
        startDaemonBtn.setOnClickListener(v -> startTestDaemon());
        stopDaemonBtn.setOnClickListener(v -> stopTestDaemon());

        final Spinner ticketSwitch = findViewById(R.id.ticketSwitch);
        ticketSwitch.setAdapter(new TicketAdapter());
        ticketSwitch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ticketToReproduce = (Ticket) ticketSwitch.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                ticketToReproduce = null;
            }
        });
    }

    private static TestDaemon DAEMON;

    private void startTestDaemon() {
        int startRunDelay = Integer.parseInt(startRunDelayEditText.getText().toString());
        int numberOfRuns = Integer.parseInt(numberOfRunsEditText.getText().toString());

        DAEMON = new TestDaemon(ticketToReproduce, startRunDelay, numberOfRuns);

        new Thread(DAEMON).start();

        startDaemonBtn.setVisibility(View.GONE);
        stopDaemonBtn.setVisibility(View.VISIBLE);
    }

    private void stopTestDaemon() {
        if (DAEMON != null) {
            DAEMON.end();
        }

        startDaemonBtn.setVisibility(View.VISIBLE);
        stopDaemonBtn.setVisibility(View.GONE);
    }

    @Override
    public void onDebugLogged(String tag, String line) {
        String currentDate = DATE_FORMAT.format(new Date());
        logsList.add(currentDate + " " + line);
        adapter.notifyItemInserted(logsList.size() - 1);
        rvLogs.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onErrorLogged(String tag, String line) {
        String currentDate = DATE_FORMAT.format(new Date());
        logsList.add(currentDate + " " + line);
        adapter.notifyItemInserted(logsList.size() - 1);
        rvLogs.scrollToPosition(adapter.getItemCount() - 1);
    }
}
