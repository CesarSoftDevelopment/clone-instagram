package cesarapps.newapp.com.instagram.fragment;


import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.ByteArrayOutputStream;

import cesarapps.newapp.com.instagram.R;
import cesarapps.newapp.com.instagram.activity.FiltroActivity;
import cesarapps.newapp.com.instagram.helper.Permissao;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostagemFragment extends Fragment {

    private Button buttonAbrirGaleria, buttonAbrirCamera;
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;

    private String[] permissoesNecessarias = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };


    public PostagemFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_postagem, container, false);

        //VALIDAR PERMISSÕES
       Permissao.validaPermissoes(permissoesNecessarias, getActivity(), 1);

        //INICIALIZAR COMPONENTES
        buttonAbrirCamera = view.findViewById(R.id.buttonAbrirCamera);
        buttonAbrirGaleria = view.findViewById(R.id.buttonAbrirGaleria);

        //ADICIONA EVENTO DE CLICK NO BOTÃO CÂMERA
        buttonAbrirCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(i.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_CAMERA);
                }
            }
        });

        //ADICIONA EVENTO DE CLICK NO BOTÃO CÂMERA
        buttonAbrirGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_GALERIA);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == getActivity().RESULT_OK) {

            Bitmap imagem = null;

           try {
               //VALIDA O TIPO DE SELEÇÃO DA IMAGEM
               switch(requestCode) {
                   case SELECAO_CAMERA:
                       imagem =(Bitmap) data.getExtras().get("data");
                       break;
                   case SELECAO_GALERIA:
                       Uri localImagemSelecionada = data.getData();
                       imagem = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), localImagemSelecionada);

               }

               //VALIDAR PARA VER SE TEMOS UMA IMAGEM SELECIONADA
               if(imagem != null) {

                   //CONVERTE IMAGEM EM BYTE ARRAY
                   ByteArrayOutputStream baos = new ByteArrayOutputStream();
                   imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                   byte[] dadosImagem = baos.toByteArray();

                   //ENVIA IMAGEM ESCOLHIDA PARA APLICAÇÃO DE FILTRO
                   Intent i = new Intent(getActivity(), FiltroActivity.class);
                   i.putExtra("fotoEscolhida", dadosImagem);
                   startActivity(i);

               }

           }catch (Exception e) {
               e.printStackTrace();
           }

        }
    }
}
