package cl.aleph.gmailclient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

/**
 * Created by root on 6/24/16.
 */
public class EmailAdapter extends ArrayAdapter<EmailModel> {
    private Context context;
    private List<EmailModel> emails;

    public EmailAdapter(Context context, List<EmailModel> emails) {
        super(context, R.layout.row_email, emails);
        this.context = context;
        this.emails = emails;
    }

    static class ViewHolder {
        protected TextView from;
        protected TextView subject;
        protected TextView date;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) { // new view
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.row_email, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.from = (TextView) view.findViewById(R.id.from);
            viewHolder.subject = (TextView) view.findViewById(R.id.subject);
            viewHolder.date = (TextView) view.findViewById(R.id.date);
            view.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.from.setText(emails.get(position).getFrom());
        holder.subject.setText(emails.get(position).getJoinedSubject());
        holder.date.setText(emails.get(position).getDate());
        return view;
    }
}
