package com.example.dodgegame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    float aX, aY, moveX, moveY;
    boolean moved;
    boolean started;
    int enemyY;
    int enemySpeed = 20;
    int random;
    TextView button2;
    int points = 0;
    boolean speedCheck = false;
    boolean checkHit = false;
    SensorManager sensorManager;
    MediaPlayer woah;
    MediaPlayer slip;
    GameSurface gameSurface;
    volatile boolean running = false;
    int time = 61;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button2 = findViewById(R.id.textView);

        gameSurface = new GameSurface(this);
        setContentView(gameSurface);

        MediaPlayer player = MediaPlayer.create(this, R.raw.music);
        woah = MediaPlayer.create(getApplicationContext(), R.raw.woah);
        slip = MediaPlayer.create(getApplicationContext(), R.raw.slip);

        player.setLooping(true);
        player.setVolume(1.0f,1.0f);

        CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {
                time--;
            }

            @Override
            public void onFinish() {
                player.stop();
                player.release();
                woah.stop();
                woah.release();
                running = false;
                sensorManager.unregisterListener(gameSurface);
                setContentView(R.layout.activity_main);
                button2 = findViewById(R.id.textView);
                button2.setText("GAME OVER! YOU SCORED " + points + " POINTS");
            }
        };
        countDownTimer.start();
        if (player.isPlaying())
            player.stop();
        player.start();


        gameSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });


    }


    @Override
    protected void onPause() {
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameSurface.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class GameSurface extends SurfaceView implements Runnable, SensorEventListener {

        Thread gameThread;
        SurfaceHolder holder;
        Bitmap bandicoot;
        Bitmap damageBandicoot;
        Bitmap rock;
        Paint paintProperty;

        int playerX;
        int playerY;
        int playerEndX;
        int playerEndY;

        int enemyStartX;
        int enemyStartY;
        int enemyEndX;
        int enemyEndY;



        int screenWidth;
        int screenHeight;





        int newX, newY;
        public GameSurface(Context context) {
            super(context);

            holder = getHolder();
            bandicoot = BitmapFactory.decodeResource(getResources(),R.drawable.bandicoot);
            damageBandicoot = BitmapFactory.decodeResource(getResources(), R.drawable.rock);
            rock = BitmapFactory.decodeResource(getResources(), R.drawable.rock2);


            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);

            screenWidth = sizeOfScreen.x;
            screenHeight = sizeOfScreen.y;

            newX = (screenWidth/2)-bandicoot.getWidth()/2;
            newY = (screenHeight/2) + 20;

            //Add sensor manager, sensor and registering the listener here when programs become more complicated
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor accelerometer  = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, sensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(SensorManager.SENSOR_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);

            paintProperty = new Paint();
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (!started){
                aX = sensorEvent.values[0];
                aY = sensorEvent.values[1];
                started = true;
                return;
            }


            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                if (aX != sensorEvent.values[0] || aY != sensorEvent.values[1]) {
                    moveX = -(sensorEvent.values[0] - aX) * 60;
                    moveY = -(sensorEvent.values[1] - aY) * 60;
                    aX = sensorEvent.values[0];
                    aY = sensorEvent.values[1];

                    moved = true;
                    System.out.println("Values of 0: " + aX + "Values of 1 " + aY);
                    System.out.println("MoveX: " + moveX + ", MoveY " + moveY);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

        @Override
        public void run() {
            MediaPlayer rocksound = MediaPlayer.create(getApplicationContext(), R.raw.rocksound);
            while(running) {
                if (holder.getSurface().isValid() == false) {
                    continue;
                }
                paintProperty.setColor(Color.WHITE);
                paintProperty.setTextSize(55f);
                Canvas canvas = holder.lockCanvas();

                canvas.drawRGB(1, 68, 33);


                if (enemyY > screenHeight + 10) {
                    random = (int) (20 + Math.random() * (screenWidth-20) - (rock.getWidth()/2+20));
                    enemyY = 0;
                    bandicoot = BitmapFactory.decodeResource(getResources(), R.drawable.bandicoot);
                    if (checkHit == false) {
                        points++;
                    }
                    checkHit = false;
                } else {
                    enemyY += enemySpeed;
                }
                drawImage(canvas, rock, random, enemyY, 0, 0.5f);


                canvas.drawText("Crash Bandicoot Game", 275, 100, paintProperty);
                canvas.drawText("Points Scored: " + points, 437, 160, paintProperty);
                canvas.drawText("Time Left: " + time, 437, 220, paintProperty);

                enemyStartX = random + 30;
                enemyEndX = random + rock.getWidth()/2 - 30;
                enemyStartY = enemyY + 30;
                enemyEndY = enemyY + rock.getHeight()/2 - 30;

                playerX = newX + 30;
                playerY = newY + 30;
                playerEndX = newX + bandicoot.getWidth()/2 - 30;
                playerEndY = newY + bandicoot.getHeight()/2 - 30;

                Rect player = new Rect(playerX, playerY, playerEndX, playerEndY);
                Rect enemy = new Rect(enemyStartX, enemyStartY, enemyEndX, enemyEndY);




                if (rocksound.isPlaying())
                    rocksound.stop();
                if (player.intersect(enemy)) {
                    drawImage(canvas, damageBandicoot, newX, newY, 0, 1f);
                    rocksound.setVolume(1.0f, 1.0f);
                    rocksound.setLooping(false);
                    rocksound.start();
                    checkHit = true;
                } else if (checkHit) {

                    drawImage(canvas, damageBandicoot, newX, newY, 0, 1f);

                    rocksound.setVolume(1.0f, 1.0f);
                    rocksound.setLooping(false);
                    rocksound.start();

                } else {

                    drawImage(canvas, bandicoot, newX, newY, 0, 0.5f);
                }

                if (moved) {
                    if ((newX + (int) moveX) < screenWidth) {
                        newX = newX + (int) moveX;
                        System.out.println("newX: " + newX + ", newY: " + newY);


                    }


                    /*flip = moveX * 2;

                    ballX = flip;*/
                    /*if ((ballX >= screenWidth / 2 - bandicoot.getWidth() / 2) || (ballX <= -1 * screenWidth / 2 + bandicoot.getWidth() / 2)) {
                        flip = 0;
                    }*/
                    moved = false;
                } else {
                    if (!checkHit)
                        drawImage(canvas, bandicoot, newX, newY, 0, 0.5f);
                }
                holder.unlockCanvasAndPost(canvas);

            }
            rocksound.stop();
            rocksound.release();
        }
        public void resume(){
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        public void pause(){
            running = false;
            while(true){
                try{
                    gameThread.join();
                }
                catch(InterruptedException e){

                }
            }
        }
        public void drawImage(Canvas canvas, Bitmap bitmap, int x, int y, int rotationAngle, float scalingfactor){
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationAngle, bitmap.getWidth()/2, bitmap.getHeight()/2);
            matrix.postScale(scalingfactor, scalingfactor, bitmap.getWidth()/2, bitmap.getHeight()/2);
            matrix.postTranslate(x, y);
            canvas.drawBitmap(bitmap, matrix, null);
        }


        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            float x = event.getX();
            float y = event.getY();
            switch(event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    //Check if the x and y position of the touch is inside the bitmap
                    if(x > newX + 20 && x < playerEndX - 20 + bandicoot.getWidth()/2 && y > newY + 20 && y < playerEndY - 20 + bandicoot.getHeight()/2 )
                    {
                        slip.setVolume(1.0f, 1.0f);
                        slip.setLooping(false);
                        slip.start();
                        bandicoot = BitmapFactory.decodeResource(getResources(), R.drawable.fallenbandicoot1);
                    }
                    else{
                        if (woah.isPlaying())
                            woah.stop();
                        if(!speedCheck){
                            enemySpeed += 20;
                            speedCheck = true;
                            woah.start();
                        }
                        else{
                            enemySpeed -= 20;
                            speedCheck = false;
                        }
                    }
                    return true;
            }
            return false;
        }
    }// GameSurface end




}