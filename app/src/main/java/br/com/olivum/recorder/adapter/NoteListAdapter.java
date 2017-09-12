package br.com.olivum.recorder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import br.com.olivum.recorder.R;
import br.com.olivum.recorder.note.Note;

/**
 * Created by allann on 8/9/17.
 */

public class NoteListAdapter extends ArrayAdapter<Note> {
    public NoteListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public NoteListAdapter(Context context, int resource, List<Note> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater;

            inflater = LayoutInflater.from(getContext());

            view = inflater.inflate(R.layout.note_list_item_adapter, null);
        }

        Note info = getItem(position);

        if (info != null) {
            // Image view - Note type

            View viewType = view.findViewById(R.id.view_note_type);

            viewType.setBackgroundResource(R.drawable.ic_mic_black_24px);

            /*
            // Caption name

            TextView textViewText = view.findViewById(R.id.text_view_name);

            String text;

            if (info.getType() == Note.TYPE_AUDIO) {
                text = "Audio note";
            }
            else {
                text = "Note";
            }

            textViewText.setText(text);
            */

            // Date

            TextView textViewDate = view.findViewById(R.id.text_view_date);

            textViewDate.setText(info.getDate().length() > 0 ? info.getDate() : "No date");

            // Description

            TextView textViewBrief = view.findViewById(R.id.text_view_brief);

            textViewBrief.setText(info.getText().length() > 0 ? info.getText() : "No description.");

            /*
            // File name

            TextView textViewFileName = view.findViewById(R.id.text_view_filename);

            textViewFileName.setText(info.getFileName());
            */

        /*
            if (textViewDateTime != null) {
                // File date

                Date date = new Date(info.getDatetime());

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",
                        Locale.getDefault());

                textViewDateTime.setText(dateFormat.format(date));

                // File size

                TextView textViewSize = (TextView) view.findViewById(R.id.textview_size);

                textViewSize.setText(String.format(Locale.getDefault(), "%.3f", (float) info.getSize() / 1024f));
            }
        */
        }

        return view;
    }

}