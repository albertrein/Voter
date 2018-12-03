package br.edu.ulbra.election.voter.validations;

import java.util.StringTokenizer;

public class ValidateName {
    public static Boolean validateName(String name){
        int count = 0;
        StringTokenizer stringToken = new StringTokenizer(name);
        while(stringToken.hasMoreTokens()){
            count++;
            if(count == 2){
                return true;
            }
        }
        return false;
    }
}