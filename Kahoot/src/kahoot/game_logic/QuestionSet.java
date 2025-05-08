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
    
    public int current_question = 0;
    public boolean file_loaded;
    JSONArray answer_set;
    public int rounds;
    
    public QuestionSet() {
        
        try {
            Object file_object = new JSONParser().parse(new FileReader("sample_set.json"));
            answer_set = (JSONArray)file_object;
            rounds = answer_set.size();
            file_loaded = true;
        
        } catch (IOException | ParseException e) {
            file_loaded = false;
            e.printStackTrace();
        }
        
    }
    
    public void changeRound() {
        current_question += 1;
    }
    
    public boolean isCreated() {
        return file_loaded;
    }
    
    public String getQuestion() {
        JSONObject q_obj = (JSONObject) answer_set.get(current_question);
        String question = (String)q_obj.get("question");
        return question;
    }
    
    public JSONArray getChoices() {
        JSONObject q_obj = (JSONObject) answer_set.get(current_question);
        JSONArray choices = (JSONArray)q_obj.get("choices");
        return choices;
    }
    
    public long getAnswer() {
        JSONObject q_obj = (JSONObject) answer_set.get(current_question);
        long answer = (long)q_obj.get("answer");
        return answer;
    }

}
