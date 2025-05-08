/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kahoot.game_logic;

import java.io.FileReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import java.io.IOException;

// add the jar file for the json imports to libraries:
// https://repo1.maven.org/maven2/com/googlecode/json-simple/json-simple/1.1.1/json-simple-1.1.1.jar


/**
 *
 * @author Sanjida Orpi and Jenesis Blancaflor
 */

// Take in a file and produce a question and answer set to be used for a game

public class QuestionSet {
    
    private boolean file_loaded;
    
    public QuestionSet() {
        Integer rounds = 0;
        String question;
        JSONArray choices;
        long answer;
        
        try {
            Object file_object = new JSONParser().parse(new FileReader("sample_set.json"));
            JSONArray answer_set = (JSONArray)file_object;
            rounds = answer_set.size();
            System.out.println("Game rounds:" + rounds);
            
            // question set made where each has 4 choices and 1 answer
            for (Object q : answer_set) {
                JSONObject q_obj = (JSONObject) q;
                question = (String)q_obj.get("question");
                choices = (JSONArray)q_obj.get("choices");
                answer = (long)q_obj.get("answer");
            }
            
            file_loaded = true;
        
        } catch (IOException | ParseException e) {
            file_loaded = false;
            e.printStackTrace();
        }
        
    }
    
    public boolean isCreated() {
        return file_loaded;
    }
    
}
