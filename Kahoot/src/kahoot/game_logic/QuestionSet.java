/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kahoot.game_logic;

import java.io.FileNotFoundException;
import java.io.FileReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import java.io.IOException;

// add the jar file for the json imports: https://repo1.maven.org/maven2/com/googlecode/json-simple/json-simple/1.1.1/json-simple-1.1.1.jar
// add to libraries

/**
 *
 * @author Sanjida Orpi and Jenesis Blancaflor
 */

// Take in a file and produce a question and answer set to be used for a game

public class QuestionSet {
    public QuestionSet() {
        Integer rounds = 0;
        
        try {
            Object file_object = new JSONParser().parse(new FileReader("sample_set.json"));
            JSONArray answer_set = (JSONArray)file_object;
            rounds = answer_set.size();
            System.out.println("Game rounds:" + rounds);
            
            // question set made where each has 4 choices and 1 answer
            for (Object q : answer_set) {
                JSONObject q_obj = (JSONObject) q;
                String question = (String)q_obj.get("question");
                JSONArray choices = (JSONArray)q_obj.get("choices");
                long answer = (long)q_obj.get("answer");
            }
            
        
        } catch (IOException | ParseException e) {
           e.printStackTrace();
        }
        
    }
    

}
