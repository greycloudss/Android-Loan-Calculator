package com.example.myapplication;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.*;
import com.google.android.material.tabs.TabLayout;
import loan.Annuential;
import loan.Exponential;
import loan.Linear;
import loan.Loan;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity {
    private EditText loanField, monthField, fromField, toField;
    private CheckBox postponeCB;

    private TextView fromText, toText;
    private RadioGroup radioGroup;
    private Button calculateButton;
    private LineChart lineChart;
    private View selectionLayer, graphLayer, paymentLayer;

    private TableLayout TableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectionLayer = findViewById(R.id.selectionLayer);
        graphLayer = findViewById(R.id.graphLayer);
        paymentLayer = findViewById(R.id.paymentLayer);

        loanField = findViewById(R.id.loanField);
        monthField = findViewById(R.id.MonthField);

        fromField = findViewById(R.id.fromField);
        toField = findViewById(R.id.fromField2);
        fromText = findViewById(R.id.fromText);
        toText = findViewById(R.id.toText);

        postponeCB = findViewById(R.id.postponeCB);
        radioGroup = findViewById(R.id.radioGroup);
        calculateButton = findViewById(R.id.calculateButton);

        TableLayout = findViewById(R.id.table);

        lineChart = findViewById(R.id.lineChart);

        fromText.setVisibility(View.GONE);
        toText.setVisibility(View.GONE);
        fromField.setVisibility(View.GONE);
        toField.setVisibility(View.GONE);

        RadioButton radioLin = (RadioButton)findViewById(R.id.LinearRC);
        radioLin.setChecked(true);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showSelectionLayer();
                } else if (tab.getPosition() == 1) {
                    showGraphLayer();
                } else {
                    showPaymentLayer();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        postponeCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fromText.setVisibility(View.VISIBLE);
                    toText.setVisibility(View.VISIBLE);
                    fromField.setVisibility(View.VISIBLE);
                    toField.setVisibility(View.VISIBLE);
                } else {
                    fromText.setVisibility(View.GONE);
                    toText.setVisibility(View.GONE);
                    fromField.setVisibility(View.GONE);
                    toField.setVisibility(View.GONE);
                }
            }
        });

        showSelectionLayer();

        calculateButton.setOnClickListener(v -> calculateLoan());
    }

    private void showSelectionLayer() {
        selectionLayer.setVisibility(View.VISIBLE);
        graphLayer.setVisibility(View.GONE);
        paymentLayer.setVisibility(View.GONE);
    }

    private void showGraphLayer() {
        selectionLayer.setVisibility(View.GONE);
        graphLayer.setVisibility(View.VISIBLE);
        paymentLayer.setVisibility(View.GONE);
    }

    private void showPaymentLayer() {
        selectionLayer.setVisibility(View.GONE);
        graphLayer.setVisibility(View.GONE);
        paymentLayer.setVisibility(View.VISIBLE);
    }
    private void calculateLoan() {
        try {
            double loanAmount = Double.parseDouble(loanField.getText().toString());
            int months = Integer.parseInt(monthField.getText().toString());

            Loan loan;
            int selectedLoanType = radioGroup.getCheckedRadioButtonId();
            if (selectedLoanType == R.id.ExponentialRC) {
                loan = new Exponential(5.0, loanAmount, months);
            } else if (selectedLoanType == R.id.AnnuentialRC) {
                loan = new Annuential(5.0, loanAmount, months);
            } else {
                loan = new Linear(5.0, loanAmount, months);
            }

            loan.calculateAndStoreMonthlyPayments();
            double[] monthlyPayments = loan.getMonthlyPaymentsData();

            if (postponeCB.isChecked()) {
                int from = Integer.parseInt(fromField.getText().toString());
                int to = Integer.parseInt(toField.getText().toString());
                double[] tmp = new double[monthlyPayments.length + abs(from - to)];
                Toast.makeText(this, "Loan calculated with delay from " + from + " to " + to, Toast.LENGTH_SHORT).show();

                if ((from >= 1 && to <= months && from <= to))
                    monthlyPayments = slinkyArrays(monthlyPayments, from, to);

            }

            populatePaymentTable(monthlyPayments);
            populateGraph(monthlyPayments);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid input. Please check your fields.", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateGraph(double[] monthlyPayments) {
        lineChart.clear();
        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < monthlyPayments.length; i++) {
            entries.add(new Entry(i + 1, (float) monthlyPayments[i]));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Monthly Payments");
        dataSet.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        dataSet.setValueTextColor(getResources().getColor(android.R.color.holo_green_dark));
        dataSet.setLineWidth(2f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        lineChart.invalidate();
    }


    private double[] slinkyArrays(double[] payments, int frm, int to) {
        int delay = abs(frm - to);
        double[] result = new double[payments.length + delay];

        int resultIndex = 0;
        int paymentIndex = 0;

        while (paymentIndex < payments.length) {
            if (paymentIndex == frm) {
                for (int j = 0; j < delay; j++) {
                    if (resultIndex < result.length)
                        result[resultIndex++] = 0.0;
                }

            }
            if (resultIndex < result.length) {
                result[resultIndex++] = payments[paymentIndex];
            }
            paymentIndex++;
        }

        return result;
    }


    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void populatePaymentTable(double[] monthlyPayments) {
        TableLayout tableLayout = findViewById(R.id.table); // Ensure this matches your TableLayout's ID
        tableLayout.removeAllViews(); // Clear existing rows

        int monthsPerYear = 12;
        int totalYears = (int) Math.ceil((double) monthlyPayments.length / monthsPerYear);
        double totalBalance = 0;

        // Calculate total balance
        for (double payment : monthlyPayments) {
            totalBalance += payment;
        }

        // Create header row
        TableRow headerRow = new TableRow(this);
        TextView monthHeader = new TextView(this);
        monthHeader.setText("Month");
        monthHeader.setTypeface(null, Typeface.BOLD);
        monthHeader.setPadding(16, 16, 16, 16);
        headerRow.addView(monthHeader);

        for (int year = 1; year <= totalYears; year++) {
            TextView paymentHeader = new TextView(this);
            paymentHeader.setText("Year " + year);
            paymentHeader.setTypeface(null, Typeface.BOLD);
            paymentHeader.setPadding(16, 16, 16, 16);
            headerRow.addView(paymentHeader);

            TextView balanceHeader = new TextView(this);
            balanceHeader.setText("Balance");
            balanceHeader.setTypeface(null, Typeface.BOLD);
            balanceHeader.setPadding(16, 16, 16, 16);
            headerRow.addView(balanceHeader);
        }

        tableLayout.addView(headerRow);

        for (int month = 0; month < monthsPerYear; month++) {
            TableRow row = new TableRow(this);

            TextView monthView = new TextView(this);
            monthView.setText("Month " + (month + 1));
            monthView.setPadding(16, 16, 16, 16);
            row.addView(monthView);

            for (int year = 0; year < totalYears; year++) {
                int paymentIndex = (year * monthsPerYear) + month;

                TextView paymentView = new TextView(this);
                TextView balanceView = new TextView(this);

                if (paymentIndex < monthlyPayments.length) {
                    double payment = monthlyPayments[paymentIndex];
                    paymentView.setText(String.format("%.2f", payment));
                    balanceView.setText(String.format("%.2f", calcMonth(monthlyPayments, totalBalance, paymentIndex)));
                } else {
                    paymentView.setText("-");
                    balanceView.setText("-");
                }

                paymentView.setPadding(16, 16, 16, 16);
                balanceView.setPadding(16, 16, 16, 16);
                row.addView(paymentView);
                row.addView(balanceView);
            }

            tableLayout.addView(row);
        }
    }

    double calcMonth(double[] monthlyPayments, double bal, int month) {
        for (int i = 0; i <= month; i++)
            bal -= monthlyPayments[i];


        return bal;
    }
}
