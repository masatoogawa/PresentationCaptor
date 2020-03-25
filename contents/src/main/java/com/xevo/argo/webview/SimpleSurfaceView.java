package com.xevo.argo.webview;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;


public class SimpleSurfaceView extends GLSurfaceView {
    private static final String TAG = SimpleSurfaceView.class.getSimpleName();

    private SimpleGLRenderer mRenderer;
    private Handler mHandler;
    private float mAngle;
    private boolean mIsDestroyed;

    public static final float PRESENTATION_BG_COLOR_A = 1.0f;
    public static final float PRESENTATION_BG_COLOR_B = (float) 0x55 / (float) 0xff;
    public static final float PRESENTATION_BG_COLOR_G = (float) 0x33 / (float) 0xff;
    public static final float PRESENTATION_BG_COLOR_R = (float) 0x33 / (float) 0xff;

    public SimpleSurfaceView(Context context) {
        super(context);
        init();
    }

    public SimpleSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mHandler = new Handler();
        mAngle = 0.0f;
        mIsDestroyed = false;
        setEGLContextClientVersion(2);
        mRenderer = new SimpleGLRenderer();
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mHandler.postDelayed(mRunnableAutoAngle, 20);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        mIsDestroyed = true;
        mHandler.removeCallbacks(mRunnableAutoAngle);
    }

    public void onInvalidate() {
        requestRender();
    }

    private Runnable mRunnableAutoAngle = new Runnable() {
        public void run() {
            if (!mIsDestroyed) {
                mAngle += 2.0f;
                mRenderer.setAngle(mAngle);
                requestRender();
//                mHandler.postDelayed(mRunnableAutoAngle, 20);
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        return true;
    }

    private class SimpleGLRenderer implements GLSurfaceView.Renderer {

        private Triangle mTriangle;
        private float[] mMVPMatrix = new float[16];
        private final float[] mProjectionMatrix = new float[16];
        private final float[] mViewMatrix = new float[16];
        private float[] mRotationMatrix = new float[16];

        public void onDrawFrame(GL10 unused) {
            float[] scratch = new float[16];
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            long time = SystemClock.uptimeMillis() % 4000L;
            mAngle = 0.090f * ((int) time);

            Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);

            Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
            Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);
            mTriangle.draw(scratch);
        }

        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            float ratio = (float) width / height;
            Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        }

        @Override
        public void onSurfaceCreated(GL10 unusedgl, javax.microedition.khronos.egl.EGLConfig config) {
            GLES20.glClearColor(
                    PRESENTATION_BG_COLOR_R,
                    PRESENTATION_BG_COLOR_G,
                    PRESENTATION_BG_COLOR_B,
                    PRESENTATION_BG_COLOR_A);
            mTriangle = new Triangle();
        }


        public int loadShader(int type, String shaderCode) {

            int shader = GLES20.glCreateShader(type);

            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);

            return shader;
        }

        public volatile float mAngle;

        public float getAngle() {
            return mAngle;
        }

        public void setAngle(float angle) {
            mAngle = angle;
        }

        private class Triangle {

            private final String vertexShaderCode =
                    "attribute vec4 vPosition;" +
                            "uniform mat4 uMVPMatrix;" +
                            "void main() {" +
                            "  gl_Position = vPosition * uMVPMatrix;" +
                            "}";

            private final String fragmentShaderCode =
                    "precision mediump float;" +
                            "uniform vec4 vColor;" +
                            "void main() {" +
                            "  gl_FragColor = vColor;" +
                            "}";

            private FloatBuffer vertexBuffer;

            static final int COORDS_PER_VERTEX = 3;

            private static final int vertexStride = 12;
            private static final int vertexCount = 3;

            float triangleCoords[] = {
                    0.0f, 0.622008459f, 0.0f,
                    -0.5f, -0.311004243f, 0.0f,
                    0.5f, -0.311004243f, 0.0f
            };

            float color[] = {
                    (float) 0xff / (float) 0xff,
                    (float) 0x00 / (float) 0xff,
                    (float) 0x00 / (float) 0xff,
                    1.0f};
            private int mProgram;
            private int mPositionHandle;
            private int mColorHandle;
            private int mMVPMatrixHandle;

            public Triangle() {
                ByteBuffer bb = ByteBuffer.allocateDirect(triangleCoords.length * 4);
                bb.order(ByteOrder.nativeOrder());
                vertexBuffer = bb.asFloatBuffer();
                vertexBuffer.put(triangleCoords);
                vertexBuffer.position(0);
                init();
            }

            public void init() {
                int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
                int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
                mProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(mProgram, vertexShader);
                GLES20.glAttachShader(mProgram, fragmentShader);
                GLES20.glLinkProgram(mProgram);
            }

            public void draw(float[] mvpMatrix) {
                GLES20.glUseProgram(mProgram);
                mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
                GLES20.glEnableVertexAttribArray(mPositionHandle);
                GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                        GLES20.GL_FLOAT, false,
                        vertexStride, vertexBuffer);
                mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
                mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
                GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
                GLES20.glUniform4fv(mColorHandle, 1, color, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
                GLES20.glDisableVertexAttribArray(mPositionHandle);
            }
        }
    }
}
