package com.example.it_management_system;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeExecutiveFragment extends Fragment {
    private PieChart pieChart;
    private BarChart barChart;
    private LineChart lineChart;
    private DatabaseReference complaintsRef;
    private TextView totalComplaintsText;
    private TextView resolvedByYouText;
    private TextView assignedToYouText;
    private TextView claimedByYouText;
    private String currentExecutiveId;

    // Color palettes for charts
    private final int[] MATERIAL_COLORS = {
            Color.rgb(41, 128, 185),   // Blue
            Color.rgb(192, 57, 43),    // Red
            Color.rgb(39, 174, 96),    // Green
            Color.rgb(142, 68, 173),   // Purple
            Color.rgb(243, 156, 18)    // Orange
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_executive, container, false);

        initializeCharts(view);
        setupChartListeners();
        currentExecutiveId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        complaintsRef = FirebaseDatabase.getInstance().getReference("complaints");
        loadAnalytics();

        return view;
    }

    private void initializeCharts(View view) {
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        lineChart = view.findViewById(R.id.lineChart);
        totalComplaintsText = view.findViewById(R.id.totalComplaintsText);
        resolvedByYouText = view.findViewById(R.id.resolvedByYouText);
        assignedToYouText = view.findViewById(R.id.assignedToYouText);
        claimedByYouText = view.findViewById(R.id.claimedByYouText);
        // Apply common styling to all charts
        setupCommonChartProperties(pieChart);
        setupCommonChartProperties(barChart);
        setupCommonChartProperties(lineChart);
    }

    private void setupCommonChartProperties(Chart chart) {
        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDrawMarkers(true);
        chart.getDescription().setEnabled(false);

        chart.animateY(1400, Easing.EaseInOutQuad);

        // Enhance legend
        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(12f);
        legend.setXEntrySpace(10f);
        legend.setYEntrySpace(5f);}

    private void setupPieChart(ComplaintAnalytics analytics) {
        pieChart.setUsePercentValues(true);
        pieChart.setDrawEntryLabels(true);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setCenterText(generateCenterSpannableText());

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(analytics.getResolvedComplaints(), "Resolved"));
        entries.add(new PieEntry(analytics.getPendingComplaints(), "Pending"));
        entries.add(new PieEntry(analytics.getPendingComplaints(), "Claimed"));
        entries.add(new PieEntry(analytics.getPendingComplaints(), "Assigned"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(MATERIAL_COLORS);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueLinePart1OffsetPercentage(80.0f);
        dataSet.setValueLinePart1Length(0.2f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate();
    }


    private void setupBarChart(ComplaintAnalytics analytics) {
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getDescription().setEnabled(false);
        barChart.setMaxVisibleValueCount(60);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(10f);
        xAxis.setLabelRotationAngle(-45);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setSpaceTop(35f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(10f);

        barChart.getAxisRight().setEnabled(false);

        List<BarEntry> entries = new ArrayList<>();
        String[] locations = analytics.getLocationData().keySet().toArray(new String[0]);

        for (int i = 0; i < locations.length; i++) {
            entries.add(new BarEntry(i, analytics.getLocationData().get(locations[i])));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Complaints by Location");
        dataSet.setColors(MATERIAL_COLORS);
        dataSet.setDrawValues(true);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);
        data.setValueTextSize(10f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        barChart.setData(data);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(locations));
        barChart.setFitBars(true);
    }

    private void setupLineChart(ComplaintAnalytics analytics) {
        lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawLimitLinesBehindData(true);
        leftAxis.setTextSize(10f);

        lineChart.getAxisRight().setEnabled(false);

        // Create dummy time series data for demonstration
        List<Entry> entries = createTimeSeriesData(analytics);

        LineDataSet dataSet = new LineDataSet(entries, "Complaint Trends");
        styleLineDataSet(dataSet);

        LineData data = new LineData(dataSet);
        data.setValueTextSize(10f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        lineChart.setData(data);
    }
    private void setupChartListeners() {
        // Pie Chart Listener
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry pe = (PieEntry) e;
                showTooltip("Status: " + pe.getLabel() + "\nCount: " + (int)pe.getValue());
            }

            @Override
            public void onNothingSelected() {}
        });
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String details = String.format("Date: Day %d\nComplaints: %d",
                        (int)e.getX(), (int)e.getY());
                showTooltip(details);
            }

            @Override
            public void onNothingSelected() {}
        });

        // Bar Chart Listener
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) h.getX();
                String location = barChart.getXAxis().getValueFormatter().getFormattedValue(index);
                showTooltip("Location: " + location + "\nComplaints: " + (int)e.getY());
            }

            @Override
            public void onNothingSelected() {}
        });
} private void showTooltip(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // Add this method to your HomeExecutiveFragment class
    private void loadAnalytics() {
        complaintsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ComplaintAnalytics analytics = new ComplaintAnalytics();
                Map<String, Integer> locationData = new HashMap<>();
                Map<String, Integer> issueData = new HashMap<>();
                int resolved = 0, pending = 0,claimed=0,assigned=0;  int execResolved = 0, execAssigned = 0, execClaimed = 0;


                // Iterate through all complaints
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);
                    String issue = snapshot.child("title").getValue(String.class);
                    String assignedTo = snapshot.child("assignedTo").getValue(String.class);
                    String resolvedBy = snapshot.child("resolvedBy").getValue(String.class);
                    String claimedBy = snapshot.child("assignedTo").getValue(String.class);


                    // Update counts for status
                    if ("Resolved".equals(status)) {
                        resolved++;
                    } else if ("Pending".equals(status)) {
                        pending++;
                    }else if ("Claimed".equals(status)) {
                        claimed++;
                    }else if ("Assigned".equals(status)) {
                        assigned++;
                    }


                    // Update location and issue counts
                    if (location != null) {
                        locationData.put(location, locationData.getOrDefault(location, 0) + 1);
                    }
                    if (issue != null) {
                        issueData.put(issue, issueData.getOrDefault(issue, 0) + 1);
                    }
                    // Update executive-specific statistics
                    if (currentExecutiveId.equals(resolvedBy)) execResolved++;
                    if (currentExecutiveId.equals(assignedTo)) execAssigned++;
                    if (currentExecutiveId.equals(claimedBy)) execClaimed++;
                }
                updateAnalyticsDisplay(resolved, pending, claimed, assigned,
                        execResolved, execAssigned, execClaimed, locationData);
                // Set the analytics data
                analytics.setResolvedComplaints(resolved);
                analytics.setPendingComplaints(pending);
                analytics.setClaimedComplaints(claimed);
                analytics.setAssignedComplaints(assigned);
                analytics.setLocationData(locationData);
                analytics.setIssueData(issueData);

                // Setup charts with the fetched data
                setupPieChart(analytics);
                setupBarChart(analytics);
                setupLineChart(analytics);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load data: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateAnalyticsDisplay(int totalResolved, int totalPending, int totalClaimed,
                                        int totalAssigned, int execResolved, int execAssigned,
                                        int execClaimed, Map<String, Integer> locationData) {
        // Update total complaints
        int totalComplaints = totalResolved + totalPending + totalClaimed + totalAssigned+totalClaimed;
        totalComplaintsText.setText(String.valueOf(totalComplaints));

        // Update executive statistics
        resolvedByYouText.setText(String.valueOf(execResolved));
        assignedToYouText.setText(String.valueOf(execAssigned));
        claimedByYouText.setText(String.valueOf(execClaimed));

        // Update charts

    }
    private List<Entry> createTimeSeriesData(ComplaintAnalytics analytics) {
        List<Entry> entries = new ArrayList<>();
        // Create more realistic time series data
        int totalDays = 7;
        for (int i = 0; i < totalDays; i++) {
            float value = (float) (analytics.getResolvedComplaints() * (1 + Math.random() * 0.5));
            entries.add(new Entry(i, value));
        }
        return entries;
    }

    private void styleLineDataSet(LineDataSet dataSet) {
        dataSet.setColor(MATERIAL_COLORS[0]);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(MATERIAL_COLORS[0]);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(50);
        dataSet.setFillColor(MATERIAL_COLORS[0]);
        dataSet.setDrawHorizontalHighlightIndicator(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
    }

    private SpannableString generateCenterSpannableText() {
        SpannableString s = new SpannableString("complaints\nstatus");
        s.setSpan(new RelativeSizeSpan(1.7f), 0, 10, 0);
        s.setSpan(new StyleSpan(Typeface.BOLD), 0, 10, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 10, s.length(), 0);
        return s;
    }
}
    // ComplaintAnalytics class
    class ComplaintAnalytics {
        private int resolvedComplaints;
        private int pendingComplaints;
        private int claimedComplaints;
        private int assignedComplaints;
        private Map<String, Integer> locationData;
        private Map<String, Integer> issueData;
        private List<String> suggestions;

        // Getters and setters for all fields
        public int getResolvedComplaints() {
            return resolvedComplaints;
        }

        public void setResolvedComplaints(int resolvedComplaints) {
            this.resolvedComplaints = resolvedComplaints;
        }

        public int getPendingComplaints() {
            return pendingComplaints;
        }
        public int getClaimedComplaints() {
            return claimedComplaints;
        }
        public int getAssignedComplaints() {
            return assignedComplaints;
        }
        public void setClaimedComplaints(int claimedComplaints) {
            this.claimedComplaints = claimedComplaints;
        }
        public void setAssignedComplaints(int assignedComplaints) {
            this.assignedComplaints = assignedComplaints;}

        public void setPendingComplaints(int pendingComplaints) {
            this.pendingComplaints = pendingComplaints;
        }

        public Map<String, Integer> getLocationData() {
            return locationData;
        }

        public void setLocationData(Map<String, Integer> locationData) {
            this.locationData = locationData;
        }

        public Map<String, Integer> getIssueData() {
            return issueData;
        }

        public void setIssueData(Map<String, Integer> issueData) {
            this.issueData = issueData;
        }

        public List<String> getSuggestions() {
            return suggestions;
        }

        public void setSuggestions(List<String> suggestions) {
            this.suggestions = suggestions;
        }
    }

