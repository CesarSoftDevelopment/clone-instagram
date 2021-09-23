package cesarapps.newapp.com.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import cesarapps.newapp.com.instagram.R;
import cesarapps.newapp.com.instagram.adapter.AdapterComentario;
import cesarapps.newapp.com.instagram.helper.ConfiguracaoFirebase;
import cesarapps.newapp.com.instagram.helper.UsuarioFirebase;
import cesarapps.newapp.com.instagram.model.Comentario;
import cesarapps.newapp.com.instagram.model.Usuario;

public class ComentariosActivity extends AppCompatActivity {
    private EditText editComentario;
    private String idPostagem;
    private Usuario usuario;
    private RecyclerView recyclerComentarios;
    private AdapterComentario adapterComentario;
    private List<Comentario> listaComentarios = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private DatabaseReference comentariosRef;
    private ValueEventListener valueEventListenerComentarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);

        //INICIALIZA COMPONENTES
        editComentario = findViewById(R.id.editComentario);
        recyclerComentarios = findViewById(R.id.recyclerComentarios);

        //CONFIGURAÇÕES INICIAIS
        usuario = UsuarioFirebase.getDadosUsuarioLogado();
        firebaseRef = ConfiguracaoFirebase.getFirebase();




        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Comentários");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        //CONFIGURAÇÃO DO RECYCLERVIEW
        adapterComentario =  new AdapterComentario(listaComentarios, getApplicationContext());
        recyclerComentarios.setHasFixedSize(true);
        recyclerComentarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerComentarios.setAdapter(adapterComentario);

        //RECUPERA ID DA POSTAGEM
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            idPostagem = bundle.getString("idPostagem");
        }
    }

    private void recuperarComentarios() {
        comentariosRef = firebaseRef.child("comentarios")
                .child(idPostagem);
        valueEventListenerComentarios = comentariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaComentarios.clear();
                for(DataSnapshot ds: snapshot.getChildren()) {
                    listaComentarios.add(ds.getValue(Comentario.class));
                }
                adapterComentario.notifyDataSetChanged(); //atualizar lista de comentários



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarComentarios();
    }

    @Override
    protected void onStop() {
        super.onStop();
        comentariosRef.removeEventListener(valueEventListenerComentarios);
    }

    public void salvarComentario(View view) {
        String textoComentario = editComentario.getText().toString();
        if(textoComentario != null && !textoComentario.equals("")) {

            Comentario comentario = new Comentario();
            comentario.setIdPostagem(idPostagem);
            comentario.setIdUsuario(usuario.getId());
            comentario.setNomeUsuario(usuario.getNome());
            comentario.setCaminhoFoto(usuario.getCaminhoFoto());
            comentario.setComentario(textoComentario);
            if(comentario.salvar()) {
                Toast.makeText(this, "Comentário salvo com sucesso!",
                        Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Comentário não salvo com sucesso",
                        Toast.LENGTH_SHORT).show();

            }


        }else {
            Toast.makeText(this, "Insira o comentário antes de salvar!",
                    Toast.LENGTH_SHORT).show();
        }
        //LIMPAR COMENTÁRIO DIGITADO
        editComentario.setText("");
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}
