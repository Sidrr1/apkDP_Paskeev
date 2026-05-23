package com.example.appdp;




import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.appdp.R;
import com.example.appdp.DatabaseHelper;
import com.example.appdp.Reminder;
import com.example.appdp.SessionManager;
import java.util.List;

public class RemindersListFragment extends Fragment {
    private ListView listViewReminders;
    private TextView tvNoReminders;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private List<Reminder> reminders;
    private ReminderAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminders, container, false);

        dbHelper = DatabaseHelper.getInstance(getActivity());
        sessionManager = new SessionManager(getActivity());

        listViewReminders = view.findViewById(R.id.listViewReminders);
        tvNoReminders = view.findViewById(R.id.tvNoReminders);

        loadReminders();

        return view;
    }

    private void loadReminders() {
        int userId = sessionManager.getUserId();
        reminders = dbHelper.getRemindersByUser(userId);

        if (reminders == null || reminders.isEmpty()) {
            tvNoReminders.setVisibility(View.VISIBLE);
            listViewReminders.setVisibility(View.GONE);
        } else {
            tvNoReminders.setVisibility(View.GONE);
            listViewReminders.setVisibility(View.VISIBLE);

            adapter = new ReminderAdapter();
            listViewReminders.setAdapter(adapter);

            // Добавляем обработчик долгого нажатия для удаления
            listViewReminders.setOnItemLongClickListener((parent, view, position, id) -> {
                showDeleteConfirmation(position);
                return true;
            });
        }
    }

    private void showDeleteConfirmation(int position) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Удалить напоминание")
                .setMessage("Вы уверены, что хотите удалить это напоминание?")
                .setPositiveButton("Да", (dialog, which) -> deleteReminder(position))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteReminder(int position) {
        Reminder reminder = reminders.get(position);
        boolean success = dbHelper.deleteReminder(reminder.getId());

        if (success) {
            Toast.makeText(getActivity(), "Напоминание удалено", Toast.LENGTH_SHORT).show();
            reminders.remove(position);
            adapter.notifyDataSetChanged();

            if (reminders.isEmpty()) {
                tvNoReminders.setVisibility(View.VISIBLE);
                listViewReminders.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(getActivity(), "Ошибка при удалении", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReminders();
    }

    private class ReminderAdapter extends ArrayAdapter<Reminder> {

        public ReminderAdapter() {
            super(getActivity(), 0, reminders);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity())
                        .inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            Reminder reminder = reminders.get(position);

            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);

            text1.setText(reminder.getTitle());
            String info = "Время: " + reminder.getReminderTime();
            if (reminder.isRepeatDaily()) {
                info += " (Ежедневно)";
            } else {
                info += " (Однократно)";
            }
            text2.setText(info);

            return convertView;
        }
    }
}