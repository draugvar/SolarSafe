package com.steve.solarsafe;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Meter.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Meter#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Meter extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //init Meters
    private CustomGauge uvi;
    private CustomGauge hi;
    static TextView value_uvi;
    static TextView value_hi;
    static ImageView imageView;

    private OnFragmentInteractionListener mListener;
    private SharedPreferences settings;

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
                    //Log.d("strIncom", strIncom);
                    if (strIncom.contains("UV_METER_BT")) {
                        String[] array = strIncom.split(";");

                        settings = getActivity().getSharedPreferences(
                                Bluetooth.deviceConnected.getAddress(),0);
                        double a = settings.getFloat("param_a", Float.parseFloat(Bluetooth.paramA));
                        double b = settings.getFloat("param_b", Float.parseFloat(Bluetooth.paramB));

                        double y = a*Double.parseDouble(array[4]) + b;
                        if(y < 0)
                            y = 0.;

                        uvi.setValue((int)(y*40));
                        value_uvi.setText(String.format("%.1f", (y*40)));
                        hi.setValue((int)(y*1000));
                        value_hi.setText(String.format("%.1f", (y*1000)));
                        if(array[1].equals("B")) {
                            imageView.setImageResource(R.drawable.battery);
                        } else {
                            imageView.setImageResource(R.drawable.supply);
                        }
                        imageView.setVisibility(View.VISIBLE);
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
     * @return A new instance of fragment Meter.
     */
    // TODO: Rename and change types and number of parameters
    public static Meter newInstance(String param1, String param2) {
        Meter fragment = new Meter();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Meter() {
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
        View root = inflater.inflate(R.layout.fragment_meter, container, false);
        init(root);
        //imageView.setImageResource();
        return root;
    }

    private void init(View view) {
        uvi = (CustomGauge) view.findViewById(R.id.gauge_uvi);
        value_uvi = (TextView) view.findViewById(R.id.value_uvi);
        hi = (CustomGauge) view.findViewById(R.id.gauge_hi);
        value_hi = (TextView) view.findViewById(R.id.value_hi);
        imageView = (ImageView) view.findViewById(R.id.energy_icon);
        Bluetooth.getMeterHandler(mHandler);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
