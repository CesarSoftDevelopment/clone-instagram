package cesarapps.newapp.com.instagram.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
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
import java.util.List;

import cesarapps.newapp.com.instagram.R;
import cesarapps.newapp.com.instagram.activity.EditarPerfilActivity;
import cesarapps.newapp.com.instagram.activity.PerfilAmigoActivity;
import cesarapps.newapp.com.instagram.adapter.AdapterGrid;
import cesarapps.newapp.com.instagram.helper.ConfiguracaoFirebase;
import cesarapps.newapp.com.instagram.helper.UsuarioFirebase;
import cesarapps.newapp.com.instagram.model.Postagem;
import cesarapps.newapp.com.instagram.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class PerfilFragment extends Fragment {

    private ProgressBar progressBar;
    private CircleImageView imagemPerfil;
    public GridView gridViewPerfil;
    private TextView textPublicacoes, textSeguidores, textSeguindo;
    private Button buttonAcaoPerfil;
    private DatabaseReference firebaseRef;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioLogadoRef;
    private ValueEventListener valueEventListenerPerfil;
    private DatabaseReference postagensUsuarioRef;
    private AdapterGrid adapterGrid;

    //2ºPASSO: CRIAR ATRIBUTO
    private Usuario usuarioLogado;

    public PerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_perfil, container, false);

        //3ºPASSO: CONFIGURAÇÕES INICIAIS
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        usuariosRef = firebaseRef.child("usuarios");
        //11ºPASSO:
        postagensUsuarioRef = ConfiguracaoFirebase.getFirebase()
                .child("postagens")
                .child(usuarioLogado.getId());

        //CONFIGURAÇÕES DOS COMPONENTES
        inicializarComponentes(view);

        //1º COISA A FAZER  É RECUPERAR USUÁRIO LOGADO!

        //ABRE EDIÇÃO DE PERFIL
        buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), EditarPerfilActivity.class));
            }
        });

        inicializarImageLoader();

        return view;
    }

    //10ºPASSO:
    public void carregarFotosPostagem() {
        //RECUPERA AS FOTOS POSTADAS PELO USUÁRIO
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
                    urlFotos.add(postagem.getCaminhoFoto());

                }



                //CONFIGURAR
                adapterGrid = new AdapterGrid(getActivity(),
                        R.layout.grid_postagem, urlFotos);
                gridViewPerfil.setAdapter(adapterGrid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //9ºPASSO:
    public void inicializarImageLoader() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(getActivity())
                .memoryCache (new LruMemoryCache( 2  *  1024  *  1024 ))
                .memoryCacheSize ( 2  *  1024  *  1024 )
                .diskCacheSize ( 50  *  1024  *  1024 )
                .diskCacheFileCount ( 100 )
                .diskCacheFileNameGenerator (new HashCodeFileNameGenerator())
                .build();
        ImageLoader.getInstance().init(config);


    }


    public void inicializarComponentes(View view) {
        gridViewPerfil = view.findViewById(R.id.gridViewPerfil);
        progressBar = view.findViewById(R.id.progressBarPerfil);
        imagemPerfil = view.findViewById(R.id.imageEditarPerfil);
        textPublicacoes = view.findViewById(R.id.textPublicacoes);
        textSeguidores = view.findViewById(R.id.textSeguidores);
        textSeguindo = view.findViewById(R.id.textSeguindo);
        buttonAcaoPerfil = view.findViewById(R.id.buttonAcaoPerfil);
    }

    //6ºPASSO: CRIAR O MÉTODO PARA RECUPERAR DADOS DO USUÁRIO LOGADO
    private void recuperarDadosUsuarioLogado() {

        usuarioLogadoRef = usuariosRef.child(usuarioLogado.getId());
        valueEventListenerPerfil = usuarioLogadoRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //RECUPERAR DADOS DE USUÁRIO LOGADO
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

    private void recuperarFotoUsuario() {
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        //4ºPASSO: RECUPERAR FOTO DO USUÁRIO
        String caminhoFoto = usuarioLogado.getCaminhoFoto();
        if(caminhoFoto != null) {
            Uri url = Uri.parse(caminhoFoto);
            Glide.with(getActivity())
                    .load(url)
                    .into(imagemPerfil);
        }

    }

    //5ºPASSO: ONSTART
    @Override
    public void onStart() {
        super.onStart();
        //7ºPASSO: PASSAR AQUI
        recuperarDadosUsuarioLogado();

        //recuperar foto usuário
        recuperarFotoUsuario();


    }



    //8ºPASSO
    @Override
    public void onStop() {
        super.onStop();
        usuarioLogadoRef.removeEventListener(valueEventListenerPerfil);
    }


}
