package copium.glndlcr.copium.player;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import copium.glndlcr.copium.R;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    Button btnPlay, btnNext, btnPrevious, btnFastForward, btnFastBackWard, btnabout_button;

    TextView txtSongName, txtSongStart, txtSongEnd;
    SeekBar seekMusicBar;
    BarVisualizer barVisualizer;


    ImageView imageView;


    String songName;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;

    ArrayList<File> mySongs;

    Thread updateSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //присвоение адреса кнопкам
        btnPlay = (Button) findViewById(R.id.BtnPlay);
        btnNext = (Button) findViewById(R.id.BtnNext);
        btnPrevious = (Button) findViewById(R.id.BtnPrevious);
        btnFastForward = (Button) findViewById(R.id.BtnFastForward);
        btnFastBackWard = (Button) findViewById(R.id.BtnFastRewind);
        btnabout_button = (Button) findViewById(R.id.about_button);

        AppCompatButton equalizerBtn = findViewById(R.id.equalizerBtn);

        equalizerBtn.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container,  EqFragment.newInstance(mediaPlayer.getAudioSessionId()))
                    .addToBackStack(null)
                    .commit();


        });


        btnabout_button.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(PlayerActivity.this, AboutActivity.class);
                startActivity(intent);

            }


        });



        txtSongName = (TextView) findViewById(R.id.SongTxt);
        txtSongStart = (TextView) findViewById(R.id.TxtSongStart);
        txtSongEnd = (TextView) findViewById(R.id.TxtSongEnd);

        seekMusicBar = (SeekBar) findViewById(R.id.SeekBar);
        barVisualizer = findViewById(R.id.wave);

        imageView = (ImageView) findViewById(R.id.MusicImage);

        //проверка играет ли песня
        if (mediaPlayer != null) {

            mediaPlayer.start();
            mediaPlayer.release();
        }

        //получение сведений из прошлого намерения
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getIntegerArrayList("songs");
        String sName = intent.getStringExtra("songname");
        position = bundle.getInt("pos");
        txtSongName.setSelected(true);

        //Извлечение имени файла из списка ArrayList
        Uri uri = Uri.parse(mySongs.get(position).toString());
        songName = mySongs.get(position).getName();
        txtSongName.setText(songName);

        //передача пути к песне в медиаплеер
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        //метод для получения конца песни
        songEndTime();

        //визуализатор
        visualizer();


        //для обновления сикбара во время воспроизведения песни
        updateSeekBar = new Thread() {
            @Override
            public void run() {

                int TotalDuration = mediaPlayer.getDuration();
                int CurrentPosition = 0;

                while (CurrentPosition < TotalDuration) {
                    try {

                        sleep(500);
                        CurrentPosition = mediaPlayer.getCurrentPosition();
                        seekMusicBar.setProgress(CurrentPosition);

                    } catch (InterruptedException | IllegalStateException e) {

                        e.printStackTrace();
                    }
                }

            }
        };


        //установка максимального прогресса сикбара на максимальную продолжительность медиафайла
        seekMusicBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();



        seekMusicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                //отслеживание хода работы сикбара и вставка его на медиаплеер
                mediaPlayer.seekTo(seekBar.getProgress());

            }
        });


        //создание обработчика для обновления текущей продолжительности
        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //получение текущей продолжительности из медиаплеера
                String currentTime = createDuration(mediaPlayer.getCurrentPosition());

                //установка текущей продолжительности в TextView
                txtSongStart.setText(currentTime);
                handler.postDelayed(this, delay);

            }
        }, delay);


        //OnClickListener для кнопок воспроизведения и паузы
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //проверка играет ли песня
                if (mediaPlayer.isPlaying()) {

                    //настройка для иконки воспроизведения
                    btnPlay.setBackgroundResource(R.drawable.play_song_icon);

                    //пауза
                    mediaPlayer.pause();

                } else {

                    //настройка иконки паузы
                    btnPlay.setBackgroundResource(R.drawable.pause_song_icon);

                    //запуск медиаплеера
                    mediaPlayer.start();

                    //анимация
                    TranslateAnimation moveAnim = new TranslateAnimation(-25, 25, -25, 25);
                    moveAnim.setInterpolator(new AccelerateInterpolator());
                    moveAnim.setDuration(600);
                    moveAnim.setFillEnabled(true);
                    moveAnim.setFillAfter(true);
                    moveAnim.setRepeatMode(Animation.REVERSE);
                    moveAnim.setRepeatCount(1);

                    imageView.startAnimation(moveAnim);

                    //вызов визуализатора
                    visualizer();
                }
            }
        });

        //операция нажатия кнопки после завершения песни
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnNext.performClick();
            }
        });


        //Реализация OnClickListener
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //остановить текущую песню
                mediaPlayer.stop();
                mediaPlayer.release();


                //получение текущей позиции песни и увеличение ее на 1
                position = ((position + 1) % mySongs.size());

                //Извлечение пути к медиа из списка массивов
                Uri uri1 = Uri.parse(mySongs.get(position).toString());


                //установка пути к медиаплееру
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri1);


                //получение названия песни и установка его в TextView
                songName = mySongs.get(position).getName();
                txtSongName.setText(songName);

                //запуск медиаплеера
                mediaPlayer.start();

                //извлечение продолжительности песни
                songEndTime();


                //анимация для картинки
                startAnimation(imageView, 360f);
                visualizer();


            }
        });



        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //оастановка плеера
                mediaPlayer.stop();
                mediaPlayer.release();


                //получение текущей позиции песни и уменьшение ее на единицу
                position = ((position - 1) % mySongs.size());
                if (position < 0)
                    position = mySongs.size() - 1;

                //извлекает путь к песне
                Uri uri1 = Uri.parse(mySongs.get(position).toString());

                //Установка пути к медиаплееру
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri1);
                songName = mySongs.get(position).getName();
                txtSongName.setText(songName);
                mediaPlayer.start();
                songEndTime();


                //анимация
                startAnimation(imageView, -360f);
                visualizer();

            }

        });


        //Реализация FastForward
        btnFastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {

                    //Получаем текущее положение и добавляем к ней 10 секунд
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);

                }
            }
        });


        //Реализация FastBackWard
        btnFastBackWard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {


                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);

                }
            }
        });

    }


    //Метод для создания анимации imageView
    public void startAnimation(View view, Float degree) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, degree);
        objectAnimator.setDuration(1000);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();

    }


    //Подготовка формата времени для TextView
    public String createDuration(int duration) {

        String time = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        time = time + min + ":";

        if (sec < 10) {

            time += "0";

        }
        time += sec;
        return time;

    }


    //Настройка визуализатора
    public void visualizer() {

        //Извлекаем и устанавлеваем текущий медиа id визуализатору
        int audioSessionId = mediaPlayer.getAudioSessionId();
        if (audioSessionId != -1) {
            barVisualizer.setAudioSessionId(audioSessionId);
        }
    }

    //Метод извлечения длительности текущей песн и вставки его в TextView
    public void songEndTime() {
        String endTime = createDuration(mediaPlayer.getDuration());
        txtSongEnd.setText(endTime);
    }


    //закрытие визуализатора
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (barVisualizer != null)
            barVisualizer.release();
    }


}

