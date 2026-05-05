package com.example.gestor_tareas;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etId, etTitulo, etDescripcion;
    private Spinner spinnerEstado;
    private Button btnGuardar, btnBuscar, btnEditar, btnBorrar, btnVerTodas;
    private RecyclerView rvTareas;
    private TareaAdapter adaptador;
    private List<Tarea> listaTareas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etId          = findViewById(R.id.etId);
        etTitulo      = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        btnGuardar    = findViewById(R.id.btnGuardar);
        btnBuscar     = findViewById(R.id.btnBuscar);
        btnEditar     = findViewById(R.id.btnEditar);
        btnBorrar     = findViewById(R.id.btnBorrar);
        btnVerTodas   = findViewById(R.id.btnVerTodas);
        rvTareas      = findViewById(R.id.rvTareas);

        // Spinner con opciones de estado (igual que aprendimos con widgets básicos)
        String[] estados = {"Pendiente", "Completada"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, estados);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterSpinner);

        rvTareas.setLayoutManager(new LinearLayoutManager(this));

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { guardarTarea(); }
        });
        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { buscarTarea(); }
        });
        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { editarTarea(); }
        });
        btnBorrar.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { borrarTarea(); }
        });
        btnVerTodas.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { cargarListaTareas(); }
        });
    }

    private void guardarTarea() {
        String titulo      = etTitulo.getText().toString();
        String descripcion = etDescripcion.getText().toString();
        String estado      = spinnerEstado.getSelectedItem().toString();

        if (!titulo.isEmpty() && !descripcion.isEmpty()) {
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "tareas.db", null, 1);
            SQLiteDatabase bd = admin.getWritableDatabase();

            ContentValues registro = new ContentValues();
            registro.put("titulo", titulo);
            registro.put("descripcion", descripcion);
            registro.put("estado", estado);

            bd.insert("tareas", null, registro);
            bd.close();

            limpiarCampos();
            Toast.makeText(this, "Tarea guardada exitosamente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Llena el título y la descripción", Toast.LENGTH_SHORT).show();
        }
    }

    private void buscarTarea() {
        String id = etId.getText().toString();
        if (!id.isEmpty()) {
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "tareas.db", null, 1);
            SQLiteDatabase bd = admin.getReadableDatabase();

            android.database.Cursor fila = bd.rawQuery(
                    "SELECT titulo, descripcion, estado FROM tareas WHERE id = " + id, null);

            if (fila.moveToFirst()) {
                etTitulo.setText(fila.getString(0));
                etDescripcion.setText(fila.getString(1));
                // Seleccionar el estado en el spinner
                String estadoEncontrado = fila.getString(2);
                if (estadoEncontrado.equals("Completada")) {
                    spinnerEstado.setSelection(1);
                } else {
                    spinnerEstado.setSelection(0);
                }
                Toast.makeText(this, "Tarea encontrada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No existe tarea con ese ID", Toast.LENGTH_SHORT).show();
            }
            bd.close();
            fila.close();
        } else {
            Toast.makeText(this, "Ingresa el ID de la tarea a buscar", Toast.LENGTH_SHORT).show();
        }
    }

    private void editarTarea() {
        String id          = etId.getText().toString();
        String titulo      = etTitulo.getText().toString();
        String descripcion = etDescripcion.getText().toString();
        String estado      = spinnerEstado.getSelectedItem().toString();

        if (!id.isEmpty() && !titulo.isEmpty() && !descripcion.isEmpty()) {
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "tareas.db", null, 1);
            SQLiteDatabase bd = admin.getWritableDatabase();

            ContentValues registroNuevo = new ContentValues();
            registroNuevo.put("titulo", titulo);
            registroNuevo.put("descripcion", descripcion);
            registroNuevo.put("estado", estado);

            int actualizados = bd.update("tareas", registroNuevo, "id=" + id, null);
            bd.close();
            limpiarCampos();

            if (actualizados == 1) {
                Toast.makeText(this, "Tarea actualizada correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se encontró tarea para actualizar", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    private void borrarTarea() {
        String id = etId.getText().toString();
        if (!id.isEmpty()) {
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "tareas.db", null, 1);
            SQLiteDatabase bd = admin.getWritableDatabase();

            int borrados = bd.delete("tareas", "id=" + id, null);
            bd.close();
            limpiarCampos();

            if (borrados == 1) {
                Toast.makeText(this, "Tarea eliminada exitosamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "La tarea no existe", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Ingresa el ID de la tarea a eliminar", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarListaTareas() {
        listaTareas = new ArrayList<>();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "tareas.db", null, 1);
        SQLiteDatabase bd = admin.getReadableDatabase();

        android.database.Cursor fila = bd.rawQuery(
                "SELECT id, titulo, descripcion, estado FROM tareas", null);

        while (fila.moveToNext()) {
            int id              = fila.getInt(0);
            String titulo       = fila.getString(1);
            String descripcion  = fila.getString(2);
            String estado       = fila.getString(3);
            listaTareas.add(new Tarea(id, titulo, descripcion, estado));
        }

        bd.close();
        fila.close();

        adaptador = new TareaAdapter(listaTareas);
        rvTareas.setAdapter(adaptador);
    }

    private void limpiarCampos() {
        etId.setText("");
        etTitulo.setText("");
        etDescripcion.setText("");
        spinnerEstado.setSelection(0);
    }
}