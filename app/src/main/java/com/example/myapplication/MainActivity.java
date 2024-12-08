package com.example.myapplication;

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

public class MainActivity extends AppCompatActivity {
    private EditText loanField, monthField, fromField, toField;
    private CheckBox postponeCB;
    private RadioGroup radioGroup;
    private Button calculateButton;
    private LineChart lineChart;
    private View selectionLayer, graphLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectionLayer = findViewById(R.id.selectionLayer);
        graphLayer = findViewById(R.id.graphLayer);

        loanField = findViewById(R.id.loanField);
        monthField = findViewById(R.id.MonthField);
        fromField = findViewById(R.id.fromField);
        toField = findViewById(R.id.fromField2);
        postponeCB = findViewById(R.id.postponeCB);
        radioGroup = findViewById(R.id.radioGroup);
        calculateButton = findViewById(R.id.calculateButton);

        lineChart = findViewById(R.id.lineChart);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showSelectionLayer();
                } else if (tab.getPosition() == 1) {
                    showGraphLayer();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        showSelectionLayer();

        calculateButton.setOnClickListener(v -> calculateLoan());
    }

    private void showSelectionLayer() {
        selectionLayer.setVisibility(View.VISIBLE);
        graphLayer.setVisibility(View.GONE);
    }

    private void showGraphLayer() {
        selectionLayer.setVisibility(View.GONE);
        graphLayer.setVisibility(View.VISIBLE);
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

            // Apply the delay logic
            loan.calculateAndStoreMonthlyPayments();
            double[] monthlyPayments = loan.getMonthlyPaymentsData();

            if (postponeCB.isChecked()) {
                int from = Integer.parseInt(fromField.getText().toString());
                int to = Integer.parseInt(toField.getText().toString());
                double[] tmp = new double[monthlyPayments.length + Math.abs(from - to)];
                Toast.makeText(this, "Loan calculated with delay from " + from + " to " + to, Toast.LENGTH_SHORT).show();

                if ((from >= 1 && to <= months && from <= to))
                    monthlyPayments = slinkyArrays(monthlyPayments, from, to);

            }


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
        int delay = Math.abs(frm - to);
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
}
