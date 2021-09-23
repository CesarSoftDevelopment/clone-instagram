package cesarapps.newapp.com.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cesarapps.newapp.com.instagram.R;
import cesarapps.newapp.com.instagram.adapter.AdapterGrid;
import cesarapps.newapp.com.instagram.helper.ConfiguracaoFirebase;
import cesarapps.newapp.com.instagram.helper.UsuarioFirebase;
import cesarapps.newapp.com.instagram.model.Postagem;
import cesarapps.newapp.com.instagram.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilAmigoActivity extends AppCompatActivity {
    private GridView gridViewPerfil;
    private Usuario usuarioSelecionado;
    private Usuario usuarioLogado;
    private Button buttonAcaoPerfil;
    private CircleImageView imagePerfil;
    private DatabaseReference usuariosRef;
    private DatabaseReference seguidoresRef;
    private DatabaseReference usuarioAmigoRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference postagensUsuarioRef;
    private ValueEventListener valueEventListenerPerfilAmigo;
    private AdapterGrid adapterGrid;

    private DatabaseReference firebaseRef;
    private String idUsuarioLogado;
    private List<Postagem> postagens;

    private TextView textPublicacoes, textSeguidores, textSeguindo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_amigo);

        //CONFIGURAÇÕES INICIAIS
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        usuariosRef = firebaseRef.child("usuarios");
        seguidoresRef = firebaseRef.child("seguidores");
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();

        //INICIALIZAR COMPONENTES
        inicializarComponentes();

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Perfil");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        //RECUPERAR USUÁRIO SELECIONADO
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            usuarioSelecionado = (Usuario) bundle.getSerializable("usuarioSelecionado");

            //CONFIGURAR REFERÊNCIA PARA POSTAGENS USUÁRIO
            postagensUsuarioRef = ConfiguracaoFirebase.getFirebase()
                    .child("postagens")
                    .child(usuarioSelecionado.getId());

            //CONFIGURAR O NOME DO USUÁRIO NA TOOLBAR
            getSupportActionBar().setTitle(usuarioSelecionado.getNome());

            //RECUPERAR FOTO DO USUÁRIO
            String caminhoFoto = usuarioSelecionado.getCaminhoFoto();
            if(caminhoFoto != null) {
                Uri url = Uri.parse(caminhoFoto);
                Glide.with(PerfilAmigoActivity.this)
                        .load(url)
                        .into(imagePerfil);
            }

        }

        //INICIALIZAR IMAGE LOADER
        inicializarImageLoader();

        //CARREGA AS FOTOS POSTADAS PELO USUÁRIO
        carregarFotosPostagem();

        //ABRE A FOTO CLICADA
        gridViewPerfil.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Postagem postagem = postagens.get(position);
                Intent i = new Intent(getApplicationContext(), VisualizarPostagemActivity.class);

                i.putExtra("postagem", postagem);
                i.putExtra("usuario", usuarioSelecionado);

                startActivity(i);
            }
        });



    }

    public void inicializarImageLoader() {

        ImageLoaderConfiguration config = new ImageLoaderConfiguration
            .Builder(this)
                .memoryCache (new LruMemoryCache( 2  *  1024  *  1024 ))
                .memoryCacheSize ( 2  *  1024  *  1024 )
                .diskCacheSize ( 50  *  1024  *  1024 )
                .diskCacheFileCount ( 100 )
                .diskCacheFileNameGenerator (new HashCodeFileNameGenerator())
            .build();
        ImageLoader.getInstance().init(config);


    }

    public void carregarFotosPostagem() {

        //RECUPERA AS FOTOS POSTADAS PELO USUÁRIO
        postagens = new ArrayList<>();
        postagensUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //CONFIGURAR O TAMANHO DO GRID
                int tamanhoGrid = getResources().getDisplayMetrics().widthPixels;
                int tamanhoImagem = tamanhoGrid / 3;
                gridViewPerfil.setColumnWidth(tamanhoImagem);

                List<String> urlFotos = new ArrayList<>();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Postagem postagem = ds.getValue(Postagem.class);
                    postagens.add(postagem);
                    urlFotos.add(postagem.getCaminhoFoto());

                }

                //CONFIGURAR
                adapterGrid = new AdapterGrid(getApplicationContext(),
                        R.layout.grid_postagem, urlFotos);
                gridViewPerfil.setAdapter(adapterGrid);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void recuperarDadosUsuarioLogado() {

        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        usuarioLogadoRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //RECUPERAR DADOS DE USUÁRIO LOGADO
                        usuarioLogado = snapshot.getValue(Usuario.class);

                        /*VERIFICA SE USUÁRIO JÁ ESTÁ SEGUINDO
                        AMIGO SELECIONADO!
                         */
                        verificarSegueUsuarioAmigo();


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void verificarSegueUsuarioAmigo() {

        DatabaseReference seguidorRef = seguidoresRef
                .child(usuarioSelecionado.getId())
                .child(idUsuarioLogado);

        seguidorRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            //Já está seguindo
                            habilitarBotaoSeguir(true);

                        }else {
                            //Ainda não está seguindo
                            habilitarBotaoSeguir(false);


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );

    }

    private void habilitarBotaoSeguir(boolean segueUsuario) {
        if(segueUsuario) {
            buttonAcaoPerfil.setText("Seguindo");
        }else {
            buttonAcaoPerfil.setText("Seguir");

            //Adicionar evento para seguir usuário
            buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Salvar seguidor
                    salvarSeguidor(usuarioLogado, usuarioSelecionado);
                }
            });

        }



    }

    private void salvarSeguidor(Usuario uLogado, Usuario uAmigo) {
        HashMap<String, Object> dadosUsuarioLogado = new HashMap<>();
        dadosUsuarioLogado.put("nome", uLogado.getNome());
        dadosUsuarioLogado.put("CaminhoFoto", uLogado.getCaminhoFoto());
        DatabaseReference seguidorRef = seguidoresRef
                .child(uAmigo.getId())
                .child(uLogado.getId());
        seguidorRef.setValue(dadosUsuarioLogado);

        //ALTERAR BOTAO ACAO SEGUINDO
        buttonAcaoPerfil.setText("Seguindo");
        buttonAcaoPerfil.setOnClickListener(null);

        //INCLEMENTAR SEGUINDO DO USUÁRIO LOGADO
        int seguindo = uLogado.getSeguindo() + 1;
        HashMap<String, Object> dadosSeguindo = new HashMap<>();
        dadosSeguindo.put("seguindo", seguindo);
        DatabaseReference usuarioSeguindo = usuariosRef
                .child(uLogado.getId());
        usuarioSeguindo.updateChildren(dadosSeguindo);

        //INCLEMENTAR SEGUINDO DO AMIGO
       int seguidores = uAmigo.getSeguidores() + 1;
        HashMap<String, Object> dadosSeguidores = new HashMap<>();
        dadosSeguidores.put("seguidores", seguidores);
        DatabaseReference usuarioSeguidores = usuariosRef
                .child(uAmigo.getId());
        usuarioSeguidores.updateChildren(dadosSeguidores);

    }


    @Override //-> chamado após o oncreate no ciclo de vida da activity
    protected void onStart() {
        super.onStart();
        //RECUPERAR DADOS DE AMIGO SELECIONADO
        recuperarDadosPerfilAmigo();

        //RECUPEAR DADOS USUÁRIO LOGADO
        recuperarDadosUsuarioLogado();
    }

    @Override
    protected void onStop() {
        super.onStop();
        usuarioAmigoRef.removeEventListener(valueEventListenerPerfilAmigo);
    }

    //ACESSANDO O NÓ USUÁRIO E PEGANDO A O ID DO USUÁRIO NO FIREBASE PRESTA ATENÇÃO!!!!!!!!!!!!!
    private void recuperarDadosPerfilAmigo() {
        usuarioAmigoRef = usuariosRef.child(usuarioSelecionado.getId());
        valueEventListenerPerfilAmigo = usuarioAmigoRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Usuario usuario = snapshot.getValue(Usuario.class);

                        String postagens = String.valueOf(usuario.getPostagens());
                        String seguindo = String.valueOf(usuario.getSeguindo());
                        String seguidores = String.valueOf(usuario.getSeguidores());

                        //Configura valores recuperados
                        textPublicacoes.setText(postagens);
                        textSeguindo.setText(seguindo);
                        textSeguidores.setText(seguidores);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    public void inicializarComponentes() {
        imagePerfil = findViewById(R.id.imageEditarPerfil);
        buttonAcaoPerfil = findViewById(R.id.buttonAcaoPerfil);
        buttonAcaoPerfil.setText("Carregando");
        textPublicacoes = findViewById(R.id.textPublicacoes);
        textSeguidores = findViewById(R.id.textSeguidores);
        textSeguindo = findViewById(R.id.textSeguindo);
        gridViewPerfil = findViewById(R.id.gridViewPerfil);

    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}
