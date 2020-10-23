package com.example.androidaptdecoder2;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.github.psambit9791.jdsp.signal.CrossCorrelation;
import com.github.psambit9791.jdsp.signal.Decimate;
import com.github.psambit9791.jdsp.signal.Generate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.System.arraycopy;
import static java.lang.System.out;

public class Decoder extends AsyncTask<File, String, Bitmap> {

    public interface AsyncResponse {
        void processFinish(Bitmap bitmap);
    }

    public AsyncResponse delegate = null;

    public Decoder(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        delegate.processFinish(bitmap);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        MainActivity.progressTextView.setText(values[0]);
        MainActivity.decoderProgressBar.setProgress(100);
    }

    @Override
    protected Bitmap doInBackground(File... files) {

        File inputFile = files[0];

        final File resampledFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "audio-resampled.wav");

        Bitmap bitmap = null;

        //use ffmpeg to resample audio to 20800 and convert to mono audio.
        publishProgress("resampling");
        long id = FFmpeg.execute("-y -i " + inputFile.toString() + " -ar 20800 -ac 1 " + resampledFile.toString());

        double startTime = System.currentTimeMillis() / 1000;
        while (!resampledFile.exists() && System.currentTimeMillis() / 1000 - startTime < 120) {
            continue;
        }

        try {
            //read the new resampled audio file
            publishProgress("reading audio file");
            double[] modulatedSamples = readAudioFile(resampledFile.getName());

            resampledFile.delete();

            //demodulate the samples
            publishProgress("demodulating");
            double[] demodulatedSamples = demodulate(modulatedSamples);

            //split the demodulated samples into smaller chunks as one big array throws an OutOfMemoryError
            int splitSize = 1000000;
            publishProgress("splitting audio into chunks");
            ArrayList<double[]> splitSamples = split(demodulatedSamples, splitSize);

            //decimate the demodulated samples by a factor of 5 to bring the sample rate down to 4160
            //these samples can be stored in one big array as it should be 5 times smaller than the demodulated samples
            publishProgress("decimating audio");
            double[] decimatedSamples = new double[0];
            for (int i = 0; i < splitSamples.size(); i++) {
                decimatedSamples  = concatenate(decimatedSamples, decimate(splitSamples.get(i), 20800, 5));
            }

            publishProgress("syncing");
            double[][] lines = sync(decimatedSamples);

            //get biggest and smallest value in the array.
            double max = 0;
            double min = 0;
            for (double[] line : lines) {
                max += Arrays.stream(line).max().getAsDouble();
                min += Arrays.stream(line).min().getAsDouble();
            }
            max = max / lines.length;
            min = min / lines.length;

            //convert the samples into 3 bitmaps, then stitch them together
            //cannot convert entire array in one go, as it throws an OutOfMemoryError
            int bitmapSplitNum = 3;
            //Bitmap bitmap = null;

            publishProgress("assembling image");
            for (int i = 0; i < bitmapSplitNum; i++) {
                int[] pixels = new int[(lines.length / bitmapSplitNum) * 2080];
                //for every line in this part of the samples array
                for (int j = 0; j < lines.length / bitmapSplitNum; j++) {
                    double[] line = lines[((lines.length / bitmapSplitNum) * i) + j];
                    //for every sample in the line
                    for (int x = 0; x < line.length; x++) {
                        //remap the sample to a pixel brightness value
                        double mappedSample = remap(line[x], min, max, 0, 255);
                        pixels[line.length * j + x] = Color.rgb((int)mappedSample, (int)mappedSample, (int)mappedSample);
                    }
                }

                //stitch the bitmaps together
                if (bitmap == null) {
                    bitmap = Bitmap.createBitmap(pixels, 2080, pixels.length / 2080, Bitmap.Config.ARGB_8888);
                } else {
                    bitmap = combineImages(bitmap, Bitmap.createBitmap(pixels, 2080, pixels.length / 2080, Bitmap.Config.ARGB_8888));
                }
            }
            publishProgress("done");


        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        startTime = System.currentTimeMillis() / 1000;
        while (bitmap == null && System.currentTimeMillis() / 1000 - startTime < 300) {
            continue;
        }

        return bitmap;
    }

    //stitches bitmap2 to the bottom of bitmap1
    public static Bitmap combineImages(Bitmap bitmap1, Bitmap bitmap2) {
        int height = bitmap1.getHeight() + bitmap2.getHeight();
        Bitmap cs = Bitmap.createBitmap(2080, height, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(bitmap1, 0f, 0f, null);
        comboImage.drawBitmap(bitmap2, 0f, bitmap1.getHeight(), null);

        return  cs;
    }

    //loads an audio file from its file path
    public static double[] readAudioFile(String filePath) throws IOException {
        try {
            WavFile audioFile = WavFile.openWavFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath(), filePath));
            int sampleNum = (int)audioFile.getNumFrames();

            double[] buffer =  new double[sampleNum];
            audioFile.readFrames(buffer, sampleNum);
            audioFile.close();

            return buffer;
        } catch (WavFileException e) {
            e.printStackTrace();
            return null;
        }
    }

    //decimates an array of samples with the specified sample rate by a factor of decimationFactor
    public static double[] decimate(double[] samples, int sampleRate, int decimationFactor) {
        Decimate decimator = new Decimate(samples, sampleRate);
        samples = decimator.decimate(decimationFactor);
        return samples;
    }

    //adds array2 to the end of array1
    public static double[] concatenate(double[] array1, double[] array2) {
        double[] result = new double[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return  result;
    }

    //returns a 2d array of the each line starting at the sync frame
    public static double[][] sync(double[] samples) {
        //Cross correlate the demodulated samples with a template of a sync frame (1040 hz square wave)
        Generate generate = new Generate(0, 1, 4160);
        double[] syncA = Arrays.copyOfRange(generate.generateSquareWave(1040), 2, 32);
        double[] suffix = new double[]{-1.0, -1.0, -1.0, -1.0, -1.0, -1.0};
        //shorten the length of the square wave array
        syncA = Arrays.copyOf(syncA, syncA.length + suffix.length);
        arraycopy(suffix, 0, syncA, syncA.length - suffix.length, suffix.length);
        //apply cross correlation

        ArrayList<double[]> peaks = new ArrayList<>();
        peaks.add(new double[] {0, 0});
        int minDistance = 2000;

        for (int i = 0; i < samples.length; i++) {
            CrossCorrelation crossCorrelation = new CrossCorrelation(Arrays.copyOfRange(samples, i, i + syncA.length), syncA);
            double correlation = crossCorrelation.crossCorrelate()[0];

            if (i - peaks.get(peaks.size() - 1)[0] > minDistance) {
                peaks.add(new double[] {i, correlation});
            } else if (correlation > peaks.get(peaks.size() - 1)[1]) {
                peaks.set(peaks.size() - 1, new double[] {i, correlation});
            }
        }


        double[][] lines = new double[peaks.size()][2080];
        for (int i = 0; i < peaks.size(); i++ ) {
            lines[i] = Arrays.copyOfRange(samples, (int)peaks.get(i)[0], (int)peaks.get(i)[0] + 2080);
        }

        return lines;
    }

    //splits an array into parts with a length of splitSize
    public static ArrayList<double[]> split(double[] samples, int splitSize) {
        ArrayList<double[]> splitSamples = new ArrayList<>();
        int overflow = samples.length % splitSize;
        for (int i = splitSize; i < samples.length - overflow; i += splitSize) {
            double[] split = Arrays.copyOfRange(samples, i, i + splitSize);
            splitSamples.add(split);
        }
        splitSamples.add(Arrays.copyOfRange(samples, samples.length - overflow, samples.length));

        return  splitSamples;
    }

    //remap a value to a new range
    public static double remap(double value, double low1, double high1, double low2, double high2) {
        return low2 + (value - low1) * (high2 - low2) / (high1 - low1);
    }

    //apply the demodulating equation to an array of samples
    public static double[] demodulate(double[] samples) throws IOException {
        double carrierFrequency = 2400;
        double sampleRate = 20800;
        double[] demodulatedSamples = new double[samples.length];

        //apply the demodulating equation
        double phi = 2 * Math.PI * (carrierFrequency / sampleRate);
        double currentSample;
        double previousSample = samples[0];
        for (int i = 0; i < samples.length; i++) {
            currentSample = samples[i];

            demodulatedSamples[i] = Math.sqrt(Math.pow(currentSample, 2) + Math.pow(previousSample, 2) - (2 * currentSample * previousSample * Math.cos(phi))) / Math.sin(phi);

            previousSample = currentSample;
        }

        return demodulatedSamples;
    }
}
