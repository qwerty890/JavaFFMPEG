package main;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main class.
 */
public class Main {

    private final String ffmpegPath = "C:\\inne\\ffmpeg\\bin\\ffmpeg.exe";
    private String[] ffmpegCmd = {};
    private File[] wavFilesWithAfadeEfect = {};

    private final String mainMusicDirectory = "C:\\Users\\fholo\\OneDrive\\Pulpit\\muzykaNaKiermaszBozonarodzeniowyGrudzien2017";
    private final String[] foldersLocations = {
            "C:\\Users\\fholo\\OneDrive\\Pulpit\\muzykaNaKiermaszMp3Grudzien2017",                 /*0*/
            mainMusicDirectory +"\\wav\\",                      /*1*/
            mainMusicDirectory + "\\normalizedMusic\\",         /*2*/
            mainMusicDirectory + "\\normalizedText\\",          /*3*/
            mainMusicDirectory + "\\affades\\",                 /*4*/
            mainMusicDirectory + "\\affadesWithoutSpace\\",     /*5*/
            mainMusicDirectory + "\\text\\",                    /*6*/
    };
    private final String[] extensions = {".mp3", ".wav"};

    public Main () throws IOException, UnsupportedAudioFileException {

//        for (int counter = 0; counter < foldersLocations.length; counter++) {
//            File musicDir = new File(foldersLocations[counter]);
//
//            if (!musicDir.exists()) {
//                musicDir.mkdir();
//            }
//        }
//
//        /*## konwertuje pliki mp3 na wav*/
//        final File[] mp3Files = loadFilesWithExtension(foldersLocations[0], extensions[0]);
//
//        for(File mp3file : mp3Files) {
//            convertAudioToWav(mp3file.getPath(), foldersLocations[1] + mp3file.getName().replace(extensions[0], "") + extensions[1]);
//        }
//
//        /*## przeniesienie plikow czytanych do nowej lokacji i usuniecie w starej*/
        File[] wavFiles = loadFilesWithExtension(foldersLocations[1], extensions[1]);
//
//        for (File wavFile : wavFiles) {
//            if ( wavFile.getName().contains("czytane") ) {
//                runCommands(ffmpegPath, "-i", wavFile.getPath(), foldersLocations[6] + wavFile.getName());
//            }
//        }
//
//        for (File wavFile : wavFiles) {
//            if (wavFile.getName().contains("czytane")) {
//                System.out.println(wavFile.delete());
//            }
//        }
//
//        wavFiles = loadFilesWithExtension(foldersLocations[1], extensions[1]);
//
//        for (File wavFile : wavFiles) {
//            if (wavFile.getName().contains("czytane")) {
//                wavFile.delete();
//            }
//        }
//
//        /*## nalozenie efektu affade dla piosenek*/
//        wavFiles = loadFilesWithExtension(foldersLocations[1], ".wav");
//
//        for (File wavFile : wavFiles) {
//            createAfadeEffects(wavFile.getPath(), foldersLocations[4] + wavFile.getName()+ ".wav");
//            wavFile.delete();
//        }

//
//      /*## usuniecie spacji z nazw plikow*/
//        wavFilesWithAfadeEfect = loadFilesWithExtension("muzyka//wavAffade//", ".wav");
//
//        for (File wavFile : wavFilesWithAfadeEfect) {
//            wavFile.renameTo(new File("muzyka//wavAfadeWithoutSpace//" + wavFile.getName()
//                    .replace(" ", "_")
//                    .replace(".mp3", "")
//                    .replace(".wav.", ".")
//
//            ));
//            System.out.println(wavFile);
//        }

        /*## usuwanie przerwy na koncu plikow piosenek*/
        wavFiles = loadFilesWithExtension(foldersLocations[4], ".wav");
        for (File wavFile : wavFiles) {
            float duration = getDuration(wavFile.getPath());
            runCommands(ffmpegPath, "-ss", String.valueOf( 0 ), "-i", wavFile.getPath(), "-t", String.valueOf(duration - 5)
            , "-c", "copy", mainMusicDirectory + "\\out\\" + wavFile.getName());
        }

//        /*## łączenie plików*/
        wavFilesWithAfadeEfect = loadFilesWithExtension(foldersLocations[4], ".wav");
        List <String> args = new ArrayList<>();
        int fileCounter = 0;
        args.add(ffmpegPath);
        for (File wavFile : wavFilesWithAfadeEfect) {
            args.add("-i");
            args.add(wavFile.getPath());

        }

        args.add("-filter_complex");

        String allFilesLabels = "";
        args.add("\"");
        float duration = 0f;
        for (File wavFile : wavFilesWithAfadeEfect) {
            float myDuration = getDuration(wavFile.getPath()) * 1000;
            duration += getDuration(wavFile.getPath()) * 1000;
//            (duration * 0.02f)
            args.add("[" + (fileCounter + 1) +"]adelay=" + (duration -  (duration * 0.02f) ) +"|" + (duration - (duration * 0.02f) ) + "[a"+ fileCounter +"];");
            allFilesLabels += "[a"+ fileCounter +"]";
            fileCounter++;

            if (fileCounter == wavFilesWithAfadeEfect.length - 1) {
                fileCounter++;
                break;
            }
        }

        args.add("[0]" + allFilesLabels + "amix=" + fileCounter);

        args.add("\"");
        args.add(mainMusicDirectory + "\\merged.wav");

        String [] params = args.toArray(new String[args.size()]);

        runCommands(params);
//
//        /*##Normalizacja plikow audio*/
//        runCommands(ffmpegPath, "-i", "muzyka//mergedAllInOne.wav", "-filter:a", "loudnorm ", "muzyka//mergedNormalized.wav");

    }

    private File[] loadFilesWithExtension(final String filesDirectory, final String extension) {
        File dir = new File(filesDirectory);
        File[] files = dir.listFiles((d, name) -> name.endsWith(extension));
        return files;
    }

    private void convertAudioToWav(final String inputAudioPath, final String outputAudioPath) throws IOException {
        runCommands(ffmpegPath, "-i", inputAudioPath, outputAudioPath);
    }

    private void createAfadeEffects(final String inputPath, final String outputPath) throws IOException, UnsupportedAudioFileException {
        float duration = getDuration(inputPath);
        runCommands(ffmpegPath, "-i", inputPath, "-af", "\"afade=t=in:st=0:d=10,afade=t=out:st=" + (duration - 15) + ":d=10\"", outputPath);
    }

    private float getDuration(final String audioFilePath) throws IOException, UnsupportedAudioFileException {
        File file = new File(audioFilePath);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioInputStream.getFormat();
        long audioFileLength = file.length();
        int frameSize = format.getFrameSize();
        float frameRate = format.getFrameRate();
        float durationInSeconds = (audioFileLength / (frameSize * frameRate));
        return durationInSeconds;
    }

    private void runCommands(final String ... commandsArray) throws IOException {
        Runtime.getRuntime().exec(commandsArray);
    }

    public static void main (String [] args) throws IOException, UnsupportedAudioFileException {

        new Main();

    }

}
