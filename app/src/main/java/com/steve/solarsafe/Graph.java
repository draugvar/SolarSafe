package com.steve.solarsafe;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;


public class Graph extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //toggle Button
    static boolean Lock;//whether lock the x-axis to 0-5
    static boolean AutoScrollX;//auto scroll to the last x value
    static boolean Stream = false;//Start or stop streaming
    //Button init
    //Button bXminus;
    //Button bXplus;
    ToggleButton tbLock;
    //ToggleButton tbScroll;
    ToggleButton tbStream;
    //View init
    static LinearLayout GraphView;
    static GraphView graphView;
    static GraphViewSeries Series;
    static TextView x;
    static TextView y;
    static TextView z;
    //graph value
    private static double graphToLastXValue = 0;
    private static int xView = 7;
    //Button bConnect, bDisconnect;

    private String mParam1;
    private String mParam2;
    private SharedPreferences settings;

    private OnFragmentInteractionListener mListener;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case Bluetooth.MESSAGE_READ:

                    byte[] readBuf = (byte[]) msg.obj;
                    // create string from bytes array
                    String strIncom = new String(readBuf, 0, 55);
                    if (strIncom.contains("UV_METER_BT")) {
                        String[] array = strIncom.split(";");
                        graphView.setTitle(array[0]);
                        x.setText("X: " + array[7]);
                        y.setText("Y: " + array[8]);
                        z.setText("Z: " + array[9]);

                        settings = getActivity().getSharedPreferences(
                                Bluetooth.deviceConnected.getAddress(),0);
                        double a = settings.getFloat("param_a",Float.parseFloat(Bluetooth.paramA));
                        double b = settings.getFloat("param_b",Float.parseFloat(Bluetooth.paramB));

                        Log.d("a and b", a + " || " + b);

                        double y = a*Double.parseDouble(array[4]) + b;
                        if(y < 0)
                            y = 0.;

                        if(Stream) {
                            Series.appendData(new GraphView.GraphViewData(graphToLastXValue,
                                    y * 40. ), AutoScrollX, 100);// there is another argument for  X range

                            //X-axis control
                            if (graphToLastXValue >= xView && Lock) {
                                Series.resetData(new GraphView.GraphViewData[]{});
                                graphToLastXValue = 0;
                            } else graphToLastXValue += 0.1;

                            if (Lock)
                                graphView.setViewPort(0, xView);
                            else
                                graphView.setViewPort(graphToLastXValue - xView, xView);

                            //refresh
                            GraphView.removeView(graphView);
                            GraphView.addView(graphView);
                        }
                    }
                    break;
            }
        }

        /*public boolean isFloatNumber(String num) {
            //Log.d("checkfloatNum", num);
            try {
                Double.parseDouble(num);
            } catch (NumberFormatException nfe) {
                return false;
            }
            return true;
        }*/

    };

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Graph.
     */
    // TODO: Rename and change types and number of parameters
    public static Graph newInstance(String param1, String param2) {
        Graph fragment = new Graph();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Graph() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_graph, container, false);
        init(view);
        buttonInit(view);
        return view;
    }

    void init(View view) {
        Bluetooth.getGraphHandler(mHandler);

        x = (TextView) view.findViewById(R.id.x);
        y = (TextView) view.findViewById(R.id.y);
        z = (TextView) view.findViewById(R.id.z);
        GraphView = (LinearLayout) view.findViewById(R.id.graph);
        GraphView.setBackgroundColor(Color.rgb(255, 135, 0));
        Series = new GraphViewSeries("Signal ",
                new GraphViewSeries.GraphViewSeriesStyle(Color.WHITE, 8),//color and thickness of the line
                new GraphView.GraphViewData[]{new GraphView.GraphViewData(0, 0)});
        graphView = new LineGraphView(getActivity().getApplication(), "");
        graphView.setBackgroundColor(Color.argb(160,255,255,255));
        ((LineGraphView)graphView).setDrawBackground(true);
        graphView.setViewPort(0, xView);
        graphView.setDrawingCacheBackgroundColor(Color.WHITE);
        graphView.setShowLegend(true);
        graphView.setLegendAlign(com.jjoe64.graphview.GraphView.LegendAlign.BOTTOM);
        String[] labely = new String[13];
        for (int i = 0; i < labely.length; i++) {
            labely[i] = (labely.length - 1 - i) + "";
        }
        graphView.setVerticalLabels(labely);
        //graphView.setShowHorizontalLabels(true);
        String[] labelx = new String[11];
        for (int i = 0; i < labelx.length; i++) {
            labelx[i] = i + "";
        }
        graphView.setHorizontalLabels(labelx);
        graphView.setDisableTouch(true);
        graphView.setScrollable(true);
        graphView.setScalable(true);
        graphView.setManualYAxis(true);
        graphView.setManualYAxisBounds(12, 0);
        graphView.addSeries(Series);
        GraphView.addView(graphView);
        Lock = true;//*****
        AutoScrollX = true;//*****
    }

    void buttonInit(View view) {
        /*bConnect = (Button)view.findViewById(R.id.bConnect);
        bConnect.setOnClickListener(this);
        bDisconnect = (Button)view.findViewById(R.id.bDisconnect);
        bDisconnect.setOnClickListener(this);
        //X-axis control button
        bXminus = (Button)view.findViewById(R.id.bXminus);
        bXminus.setOnClickListener(this);
        bXplus = (Button)view.findViewById(R.id.bXplus);
        bXplus.setOnClickListener(this);*/
        //
        tbLock = (ToggleButton) view.findViewById(R.id.tbLock);
        tbLock.setOnClickListener(this);
        //tbScroll = (ToggleButton)view.findViewById(R.id.tbScroll);
        //tbScroll.setOnClickListener(this);
        tbStream = (ToggleButton) view.findViewById(R.id.tbStream);
        tbStream.setOnClickListener(this);
        //init toggleButton
        Lock = true;
        AutoScrollX = true;
        Stream = false;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.bConnect:
                startActivity(new Intent("android.intent.action.BT1"));
                break;
            case R.id.bDisconnect:
                Bluetooth.disconnect();
                break;
            case R.id.bXminus:
                if (xView>1) xView--;
                break;
            case R.id.bXplus:
                if (xView<30) xView++;
                break;*/
            case R.id.tbLock:
                Lock = tbLock.isChecked();
                break;
            case R.id.tbStream:
                /*if (tbStream.isChecked()) {
                    if (Bluetooth.connectedThread != null)
                        Bluetooth.connectedThread.write("E");
                } else {
                    if (Bluetooth.connectedThread != null)
                        Bluetooth.connectedThread.write("Q");
                }*/
                Stream = tbStream.isChecked();
                break;
        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
