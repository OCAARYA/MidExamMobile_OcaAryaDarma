package com.example.exammobile_oca;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "student_db";
    private static final String TB_NAME = "student";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_STUDENT_ID = "student_id";
    private static final String COL_ENROLLED = "enrolled";

    private static final String CREATE_TABLE = "CREATE TABLE " + TB_NAME + "("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_NAME + " TEXT NOT NULL, "
            + COL_STUDENT_ID + " TEXT NOT NULL UNIQUE, "
            + COL_ENROLLED + " INTEGER NOT NULL DEFAULT 0);";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TB_NAME);
        onCreate(db);
    }

    public boolean isStudentIdExists(String studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_ID + " FROM " + TB_NAME + " WHERE " + COL_STUDENT_ID + " = ?",
                new String[]{studentId});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean isStudentIdUsedByOther(String studentId, int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_ID + " FROM " + TB_NAME + " WHERE " + COL_STUDENT_ID
                        + " = ? AND " + COL_ID + " != ?",
                new String[]{studentId, String.valueOf(id)});
        boolean used = cursor.getCount() > 0;
        cursor.close();
        return used;
    }

    public boolean addStudent(StudentModel student) {
        if (isStudentIdExists(student.getStudentId())) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(COL_NAME, student.getName());
        values.put(COL_STUDENT_ID, student.getStudentId());
        values.put(COL_ENROLLED, student.isEnrolled() ? 1 : 0);
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.insert(TB_NAME, null, values);
        return result != -1;
    }

    public boolean updateStudent(StudentModel student) {
        if (isStudentIdUsedByOther(student.getStudentId(), student.getId())) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(COL_NAME, student.getName());
        values.put(COL_STUDENT_ID, student.getStudentId());
        values.put(COL_ENROLLED, student.isEnrolled() ? 1 : 0);
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.update(TB_NAME, values, COL_ID + " = ?",
                new String[]{String.valueOf(student.getId())});
        return rows > 0;
    }

    public void deleteStudent(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TB_NAME, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public List<StudentModel> getStudentList() {
        List<StudentModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_ID + ", " + COL_NAME + ", "
                + COL_STUDENT_ID + ", " + COL_ENROLLED + " FROM " + TB_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String studentId = cursor.getString(2);
                boolean enrolled = cursor.getInt(3) == 1;
                list.add(new StudentModel(id, name, studentId, enrolled));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}