package com.example.exammobile_oca;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
    List<StudentModel> students;
    Context context;
    DBHelper dbHelper;
    Runnable onListChanged;

    public StudentAdapter(List<StudentModel> students, Context context, Runnable onListChanged) {
        this.students = students;
        this.context = context;
        this.onListChanged = onListChanged;
        dbHelper = new DBHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentModel student = students.get(position);
        holder.editName.setText(student.getName());
        holder.editStudentId.setText(student.getStudentId());
        holder.checkEnrolled.setChecked(student.isEnrolled());
        holder.layoutName.setError(null);
        holder.layoutStudentId.setError(null);

        holder.btnEdit.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            holder.layoutName.setError(null);
            holder.layoutStudentId.setError(null);

            String newName = String.valueOf(holder.editName.getText()).trim();
            String newStudentId = String.valueOf(holder.editStudentId.getText()).trim();

            boolean valid = true;
            if (newName.isEmpty()) {
                holder.layoutName.setError("Student name is required");
                valid = false;
            }
            if (newStudentId.isEmpty()) {
                holder.layoutStudentId.setError("Student ID is required");
                valid = false;
            }
            if (!valid) return;

            StudentModel current = students.get(pos);
            StudentModel updated = new StudentModel(current.getId(), newName, newStudentId,
                    holder.checkEnrolled.isChecked());

            if (dbHelper.updateStudent(updated)) {
                current.setName(newName);
                current.setStudentId(newStudentId);
                current.setEnrolled(holder.checkEnrolled.isChecked());
                holder.editName.clearFocus();
                holder.editStudentId.clearFocus();
                Toast.makeText(context, "Data updated", Toast.LENGTH_SHORT).show();
            } else {
                holder.layoutStudentId.setError("Student ID already exists");
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            StudentModel current = students.get(pos);
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Delete student")
                    .setMessage("Delete " + current.getName() + " (" + current.getStudentId() + ")?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        int currentPos = holder.getBindingAdapterPosition();
                        if (currentPos == RecyclerView.NO_POSITION) return;

                        dbHelper.deleteStudent(students.get(currentPos).getId());
                        students.remove(currentPos);
                        notifyItemRemoved(currentPos);
                        onListChanged.run();
                        Toast.makeText(context, "Data deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextInputLayout layoutName, layoutStudentId;
        TextInputEditText editName, editStudentId;
        CheckBox checkEnrolled;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutName = itemView.findViewById(R.id.layoutName);
            layoutStudentId = itemView.findViewById(R.id.layoutStudentId);
            editName = itemView.findViewById(R.id.editName);
            editStudentId = itemView.findViewById(R.id.editStudentId);
            checkEnrolled = itemView.findViewById(R.id.checkEnrolled);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}