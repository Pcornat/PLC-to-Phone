package com.example.denefflo.api_to_phone;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import PLC.PlcReader;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatsFragment extends Fragment {
    private List<String> listVarAddress = new ArrayList<>(Collections.<String>emptyList());
    private List<String> listTypeData = new ArrayList<>(Collections.<String>emptyList());
    private List<TextView> textViews;
    private ScheduledThreadPoolExecutor executor;
    private PlcReader plcReader;

    private OnFragmentInteractionListener mListener;

    public StatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StatsFragment.
     */
    public static StatsFragment newInstance() {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * To understand when this method is used, take a look at
     * <a href="https://developer.android.com/guide/components/fragments.html#Lifecycle">this</a>.
     *
     * @param savedInstanceState bundle used to have the different object when the fragment is being created
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plcReader = getArguments().getParcelable(PlcActivity.PLCREADER);
        }
    }

    /**
     * @param inflater           used to inflate the layout.
     * @param container          it contains the view
     * @param savedInstanceState bundle that can be used to get some object or fields
     * @return the view of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        textViews = new ArrayList<>(Arrays.asList(
                (TextView) view.findViewById(R.id.result_nbreHour),
                (TextView) view.findViewById(R.id.result_nbrRaz),
                (TextView) view.findViewById(R.id.result_nbrIdle)
        ));
        return view;
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        autoRefresh();
    }

    // Normally, it works

    /**
     * It lets the reader object read automatically. It refreshes the pictures or TextViews on its own.
     */
    private void autoRefresh() {
        try {
            plcReader.setDisconnect(true);
            plcReader.setSimpleConnect(false);
            executor = new ScheduledThreadPoolExecutor(listVarAddress.size());
            for (String varAddress : listVarAddress)
                for (String typeData : listTypeData)
                    for (TextView textView : textViews) {
                        plcReader.setFields(varAddress, typeData);
                        plcReader.setTextView(textView);
                        executor.scheduleWithFixedDelay(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    plcReader.run();
                                    if (plcReader.getMessageErr() != 0)
                                        dealError(plcReader.getMessageErr());
                                    else {
                                        plcReader.setText();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    dealError(e.toString());
                                }
                            }
                        }, 100, 100, TimeUnit.MILLISECONDS);
                    }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * It makes popped an error window, displaying the error message and a button to close it.
     *
     * @param error the error to display
     */
    private void dealError(String error) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_title)
                .setMessage(error)
                .setNeutralButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    /**
     * It makes popped an error window, displaying the error message and a button to close it.
     *
     * @param errorId the id of the string to display
     */
    private void dealError(int errorId) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_title)
                .setMessage(errorId)
                .setNeutralButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
