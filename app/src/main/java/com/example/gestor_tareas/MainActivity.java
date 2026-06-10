package com.example.gestor_tareas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI
    private TextInputEditText etId, etTitulo, etDescripcion;
    private TextInputLayout tilTitulo, tilDescripcion;
    private ChipGroup chipGroupEstado;
    private Chip chipPendiente, chipCompletada;
    private MaterialButton btnGuardar, btnBuscar, btnEditar, btnBorrar, btnVerTodas;
    private RecyclerView rvTareas;

    // Firestore
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    // RecyclerView
    private TareaAdapter adaptador;
    private List<Tarea> listaTareas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Enlazar vistas
        etId = findViewById(R.id.etId);
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        tilTitulo = findViewById(R.id.tilTitulo);
        tilDescripcion = findViewById(R.id.tilDescripcion);
        chipGroupEstado = findViewById(R.id.chipGroupEstado);
        chipPendiente = findViewById(R.id.chipPendiente);
        chipCompletada = findViewById(R.id.chipCompletada);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnEditar = findViewById(R.id.btnEditar);
        btnBorrar = findViewById(R.id.btnBorrar);
        btnVerTodas = findViewById(R.id.btnVerTodas);
        rvTareas = findViewById(R.id.rvTareas);

        // RecyclerView
        listaTareas = new ArrayList<>();
        adaptador = new TareaAdapter(listaTareas);
        rvTareas.setLayoutManager(new LinearLayoutManager(this));
        rvTareas.setAdapter(adaptador);

        // TextWatchers
        etTitulo.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() < 3 && s.toString().trim().length() > 0) {
                    tilTitulo.setError("El título es muy corto (mínimo 3 caracteres)");
                } else {
                    tilTitulo.setError(null);
                }
            }
        });

        etDescripcion.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() < 5 && s.toString().trim().length() > 0) {
                    tilDescripcion.setError("La descripción es muy corta (mínimo 5 caracteres)");
                } else {
                    tilDescripcion.setError(null);
                }
            }
        });

        // Botones
        btnGuardar.setOnClickListener(v -> guardarTarea());
        btnBuscar.setOnClickListener(v -> buscarTarea());
        btnEditar.setOnClickListener(v -> editarTarea());
        btnBorrar.setOnClickListener(v -> borrarTarea());
        btnVerTodas.setOnClickListener(v -> escucharTareas());
    }

    private String getEstadoSeleccionado() {
        return chipCompletada.isChecked() ? "Completada" : "Pendiente";
    }

    // CREATE
    private void guardarTarea() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String estado = getEstadoSeleccionado();

        if (titulo.isEmpty()) {
            tilTitulo.setError("El título no puede estar vacío");
            return;
        }
        if (titulo.length() < 3) {
            tilTitulo.setError("El título es muy corto");
            return;
        }
        if (descripcion.isEmpty()) {
            tilDescripcion.setError("La descripción no puede estar vacía");
            return;
        }
        if (descripcion.length() < 5) {
            tilDescripcion.setError("La descripción es muy corta");
            return;
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        Tarea tarea = new Tarea(titulo, descripcion, estado);

        db.collection("tareas")
                .add(tarea)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Tarea guardada con ID: " + documentReference.getId(), Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("GUARDAR TAREA");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("GUARDAR TAREA");
                });
    }

    // READ en tiempo real
    private void escucharTareas() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        listenerRegistration = db.collection("tareas")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error al escuchar: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots != null) {
                        listaTareas.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                            Tarea tarea = doc.toObject(Tarea.class);
                            if (tarea != null) {
                                listaTareas.add(tarea);
                            }
                        }
                        adaptador.notifyDataSetChanged();
                        Toast.makeText(this, listaTareas.size() + " tareas encontradas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // READ por ID
    private void buscarTarea() {
        String id = etId.getText().toString().trim();
        if (id.isEmpty()) {
            Toast.makeText(this, "Ingresa el ID del documento", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("tareas").document(id)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Tarea tarea = document.toObject(Tarea.class);
                        if (tarea != null) {
                            etTitulo.setText(tarea.getTitulo());
                            etDescripcion.setText(tarea.getDescripcion());
                            if (tarea.getEstado().equals("Completada")) {
                                chipCompletada.setChecked(true);
                            } else {
                                chipPendiente.setChecked(true);
                            }
                            Toast.makeText(this, "Tarea encontrada", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No existe una tarea con ese ID", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // UPDATE
    private void editarTarea() {
        String id = etId.getText().toString().trim();
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String estado = getEstadoSeleccionado();

        if (id.isEmpty()) {
            Toast.makeText(this, "Ingresa el ID del documento a editar", Toast.LENGTH_SHORT).show();
            return;
        }
        if (titulo.isEmpty()) {
            tilTitulo.setError("El título no puede estar vacío");
            return;
        }
        if (descripcion.isEmpty()) {
            tilDescripcion.setError("La descripción no puede estar vacía");
            return;
        }

        btnEditar.setEnabled(false);
        btnEditar.setText("Guardando...");

        db.collection("tareas").document(id)
                .update("titulo", titulo, "descripcion", descripcion, "estado", estado)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tarea actualizada correctamente", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                    btnEditar.setEnabled(true);
                    btnEditar.setText("Editar");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al editar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnEditar.setEnabled(true);
                    btnEditar.setText("Editar");
                });
    }

    // DELETE
    private void borrarTarea() {
        String id = etId.getText().toString().trim();
        if (id.isEmpty()) {
            Toast.makeText(this, "Ingresa el ID del documento a borrar", Toast.LENGTH_SHORT).show();
            return;
        }

        btnBorrar.setEnabled(false);
        btnBorrar.setText("Borrando...");

        db.collection("tareas").document(id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tarea eliminada correctamente", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                    btnBorrar.setEnabled(true);
                    btnBorrar.setText("Borrar");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al borrar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnBorrar.setEnabled(true);
                    btnBorrar.setText("Borrar");
                });
    }

    private void limpiarCampos() {
        etId.setText("");
        etTitulo.setText("");
        etDescripcion.setText("");
        chipPendiente.setChecked(true);
        tilTitulo.setError(null);
        tilDescripcion.setError(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}