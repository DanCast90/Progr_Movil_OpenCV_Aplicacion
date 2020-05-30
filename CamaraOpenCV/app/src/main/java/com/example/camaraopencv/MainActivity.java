package com.example.camaraopencv;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ImageView imagen;
    Button btnTomar,btnCambiar,btnAbrir;
    String absolutePath="";
    private final int PHOTO_CONST=1;
    private final int GALLERY=200;
    private final int TAKE_FOTO=100;
    private  static String TAG="MainActivity";
    static {
        if (OpenCVLoader.initDebug()){
            Log.d(TAG,"openCV is configured  succesfully");
        }else{
            Log.d(TAG,"openCV is´nt configured  succesfully");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imagen=findViewById(R.id.image);
        btnCambiar=findViewById(R.id.btn_cambiar);
        btnTomar=findViewById(R.id.btn_tomar);
        btnAbrir=findViewById(R.id.btn_abrir);
        btnTomar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tomarFoto();
            }
        });
        btnCambiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options={"GRAY SCALE","HSV SCALE","HLS SCALE","BGR SCALE","cancelar"};
                final AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(options[which]=="GRAY SCALE"){
                           cambiarImagenes(Imgproc.COLOR_RGB2GRAY);
                        }else if(options[which]=="HSV SCALE"){
                           cambiarImagenes(Imgproc.COLOR_RGB2HSV);
                        }else if(options[which]=="HLS SCALE"){
                            cambiarImagenes(Imgproc.COLOR_RGB2HLS);
                        }else if(options[which]=="BGR SCALE"){
                            cambiarImagenes(Imgproc.COLOR_RGB2HSV);
                        }else if(options[which]=="cancelar"){
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();


            }
        });

        btnAbrir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarImagen();
            }
        });

    }

    private void tomarFoto(){
        Intent itomarFoto=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(itomarFoto.resolveActivity(getPackageManager())!=null){
            File archivoFoto=null;
            try{
                archivoFoto=crearArchivoFoto();
            }catch (Exception ex){
            }
            if(archivoFoto!=null){
                Uri photoUri= FileProvider.getUriForFile
                        (MainActivity.this,"com.example.camaraopencv",archivoFoto);
                itomarFoto.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                startActivityForResult(itomarFoto,TAKE_FOTO);

            }

        }

    }

    private  File crearArchivoFoto() throws IOException {
        String fecha=new SimpleDateFormat("yyyyMMdd HHmmss").format(new Date());
        String nombreImagen="imagen "+fecha;
        File storageFile=getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photoFile=File.createTempFile(nombreImagen,".jpeg",storageFile);
        absolutePath=photoFile.getAbsolutePath();
        return photoFile;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //se valida si el codigo de resultado esta bien
        if (resultCode == RESULT_OK) {
            //se hace un swtich para saber que intent regresó el codigo de resultado
            switch (requestCode){
                 // si se regresó este codigo, se cargará la foto en el imageView
                case GALLERY:
                    try {
                        //un toast para notificar al usuario que se cargo
                        Toast t = Toast.makeText(getApplicationContext(), "Foto Cargada", Toast.LENGTH_LONG);
                        // se obtiene la URI de la imagen que se seleccionó
                        Uri selectedImage = data.getData();
                        // contrario al OutputStream, aqui se lee el contenido del URI
                        InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                        // se decodifica el URI a un mapa de bits
                        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                        // el mapa de bits se agrega al imageView
                        imagen.setImageBitmap(bitmap);
                        //se muestra el Toast
                        t.show();
                    } catch (Exception e) {
                        // se captura el error y se notifica
                        Log.i("entry","no entro y doy error");
                    }
                    break;
                    //si regresó este codigo, se guarda la imagen
                case TAKE_FOTO:
                    // se crea un Toast
                    Toast t;
                    //se obtiene el URI de la foto tomada
                    Uri uri = Uri.parse(absolutePath);
                    //esa URI obtenida, se pasa al imageView
                    imagen.setImageURI(uri);
                    //se muestra el toast
                    t = Toast.makeText(getApplicationContext(), "Foto Guardada", Toast.LENGTH_LONG);
                    t.show();
                    break;

            }

        }
    }


    public void cambiarImagenes(int var){
        try {
            //se crean dos objetos de tipo Mat, los cuales son matrices para almacenar
            //valores complejos
            Mat rgb = new Mat();
            Mat gray = new Mat();
            //se obtiene el mapa de bits de la imagen que está cargada en el ImageView
            Bitmap imageBitmat = ((BitmapDrawable) imagen.getDrawable()).getBitmap();
            //se toma la longitud y anchura del mapa de bits
            int width = imageBitmat.getWidth();
            int height = imageBitmat.getHeight();
            //se crea otro mapa de bits que será el que va a ser convertido
            Bitmap grayImage = Bitmap.createBitmap(width,height,imageBitmat.getConfig());
            //se convierte el mapa de bits a la matriz MAT
            Utils.bitmapToMat(imageBitmat,rgb);
            //aqui es donde sucede la magia, se le pasan tres parametros
            // 1. la matriz de colores
            // 2. una matriz vacia donde se guardara el mapa de bits modificado
            // 3. el tipo de "filtro que se le aplica"
            Imgproc.cvtColor(rgb,gray,var);
            //se convierte la matriz MAT a un mapa de bits
            Utils.matToBitmap(gray,grayImage);
            //ese mapa de bits se argega al ImageView
            imagen.setImageBitmap(grayImage);
            //se toma el nuevo mapa de bits (el que se convierte)
            Bitmap btm=((BitmapDrawable)imagen.getDrawable()).getBitmap();
            //crea un búfer en la memoria y todos los datos enviados a la secuencia se almacenan en el búfer.
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //se comprimen esos datos
            btm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            //los datos comprimidos, se pasan a un arreglo de bytes
            byte[] byteArray = stream.toByteArray();
            //se crea un objeto de tipo File
            File imagenNueva=crearArchivoFoto();
            //entrando a un bloque TryCatch para el manejo de erorres por IO
            try{
                //se crea un objeto de tipo FileOutputStream
                // que es una secuencia de salida utilizada para escribir datos en un archivo.
                FileOutputStream fos=new FileOutputStream(imagenNueva);
                // se escribe el arreglo de bytes en el archivo anteriormente creado
                fos.write(byteArray);
                //se cierra ese proceso
                fos.close();
                //se muestra un Toast para que el usuario sepa que se realizó el proceso
                Toast t = Toast.makeText(getApplicationContext(), "Foto Correctamente modificada \n y Guardada", Toast.LENGTH_LONG);
                t.show();
            } catch (IOException e2) {
                //se captura el error
                Log.e("error","Error en el IO");
            }

        }catch (Exception e){
            //se captura un error si no hay alguna imagen cargada
            Toast.makeText(this, "Cargue primero una imagen", Toast.LENGTH_LONG).show();
        }
    }


    public void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent.createChooser(intent,"seleccione una imagen"), GALLERY);
    }


}
