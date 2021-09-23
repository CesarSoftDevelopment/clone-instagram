package cesarapps.newapp.com.instagram.fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import cesarapps.newapp.com.instagram.R;
import cesarapps.newapp.com.instagram.activity.PerfilAmigoActivity;
import cesarapps.newapp.com.instagram.adapter.AdapterPesquisa;
import cesarapps.newapp.com.instagram.helper.ConfiguracaoFirebase;
import cesarapps.newapp.com.instagram.helper.RecyclerItemClickListener;
import cesarapps.newapp.com.instagram.helper.UsuarioFirebase;
import cesarapps.newapp.com.instagram.model.Usuario;

/**
 * A simple {@link Fragment} subclass.
 */
public class PesquisaFragment extends Fragment {

    private SearchView searchViewPesquisa;
    private RecyclerView recyclerViewPesquisa;

    private List<Usuario> listaUsuarios;
    private DatabaseReference usuariosRef;
    private AdapterPesquisa adapterPesquisa;
    private String idUsuarioLogado;


    public PesquisaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pesquisa, container, false);

        //SEMPRE QUANDO TRABALHAMOS COM FRAGMENT USAMOS O VIEW
        searchViewPesquisa = view.findViewById(R.id.searchViewPesquisa);
        recyclerViewPesquisa = view.findViewById(R.id.recyclerPesquisa);

        //CONFIGURAÇÕES INICIAIS
        listaUsuarios = new ArrayList<>();
        usuariosRef = ConfiguracaoFirebase.getFirebase()
                .child("usuarios");
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();

        //CONFIGURAR RECYCLERVIEW
        recyclerViewPesquisa.setHasFixedSize(true);
        recyclerViewPesquisa.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapterPesquisa = new AdapterPesquisa(listaUsuarios, getActivity());
        recyclerViewPesquisa.setAdapter(adapterPesquisa);

        //CONFIGURAR O EVENTO DE CLICK
        recyclerViewPesquisa.addOnItemTouchListener(new RecyclerItemClickListener(
                getActivity(),
                recyclerViewPesquisa,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Usuario usuarioSelecionado = listaUsuarios.get(position);
                        Intent i = new Intent(getActivity(), PerfilAmigoActivity.class);
                        i.putExtra("usuarioSelecionado", usuarioSelecionado);
                        startActivity(i);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));

        //CONFIGURAR O SEARCHVIEW
        searchViewPesquisa.setQueryHint("Buscar usuários");
        searchViewPesquisa.setOnQueryTextListener(new SearchView.OnQueryTextListener() {//CAPTURAR O QUE O USUÁRIO DIGITOU
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String textoDigitado = newText.toUpperCase();
                pesquisarUsuarios(textoDigitado);

                return true;
            }
        });


        return view;
    }

    private void pesquisarUsuarios(String texto) {

        //LIMPAR LISTA
        listaUsuarios.clear();

        //PESQUISAR USUÁRIOS CASO TENHA TEXTO NA PESQUISA
        if(texto.length() >= 2) {
            Query query = usuariosRef.orderByChild("nome")
                    .startAt(texto)
                    .endAt(texto + "\uf8ff");

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    //LIMPAR LISTA
                    listaUsuarios.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {

                        //VERIFICA SE É USUÁRIO LOGADO E REMOVE ELE DA LISTA
                        Usuario usuario = ds.getValue(Usuario.class);
                        if(idUsuarioLogado.equals(usuario.getId()))
                            continue; //VOLTA PARA O FOR SEM EXECUTAR O CÓDIGO ABAIXO!


                        //ADICIONA O USUÁRIO NA LISTA
                        listaUsuarios.add(usuario);

                    }

                    adapterPesquisa.notifyDataSetChanged();

                    /*
                    int total = listaUsuarios.size();

                     */
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

}
