package com.larry.floaty.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {

	private FileUtils(){
	}
	
	public static void genScoreFile(){
		try {
            File file = new File("score.txt");
            if(file.exists()){
                return;
            }

            if (!file.createNewFile()){
                throw new IOException("Failed to create save file");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public static void changeScore(String score){
		try {

			File file = new File("score.txt");

            genScoreFile();

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(score);
			bw.close();

            fw.close();

			System.out.println("Score saved successfully!!");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static String loadAsString(String file){
		//Use StringBuilder only in loops like this to increase performance, not to be used everywhere
		StringBuilder result = new StringBuilder();//StringBuilder anticipates stringbuilding and allocates more 
													//memory to not have to re-allocate more everytime you make the String bigger
		try{
            FileReader fr = new FileReader(file);
			BufferedReader reader = new BufferedReader(fr);
			String buffer;
			while((buffer = reader.readLine()) != null){
				result.append(buffer).append('\n');
			}
			reader.close();
            fr.close();
		} catch(IOException e){
			e.printStackTrace();
		}
		return result.toString();//StringBuilder is an array of characters, this turns it to a string
	}
}
